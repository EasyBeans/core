/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
 * Contact: easybeans@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: TestSingleSmartFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.test;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.ow2.easybeans.component.smartclient.client.AskingClassLoader;
import org.ow2.easybeans.component.smartclient.server.SmartClientEndPointComponent;
import org.ow2.easybeans.component.smartclient.spi.SmartContextFactory;
import org.ow2.util.url.URLUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Tests for the Smart Factory.
 * @author Florent BENOIT
 */
public class TestSingleSmartFactory {

    /**
     * RMi registry port.
     */
    public static final int RMI_REGISTRY_PORT = 15100;

    /**
     * Other RMi registry port.
     */
    public static final int RMI2_REGISTRY_PORT = 15102;


    /**
     * Smart endpoint port number.
     */
    public static final int SMART_ENDPOINT_PORT = 15101;

    /**
     * Smart endpoint 2 port number.
     */
    public static final int SMART2_ENDPOINT_PORT = 15103;

    /**
     * RMi Factory.
     */
    private static final String JRMP_RMI_FACTORY = "com.sun.jndi.rmi.registry.RegistryContextFactory";

    /**
     * Tmp directory for dumping files.
     */
    private File tmpDir = null;

    /**
     * Registry object.
     */
    private Registry registry = null;

    /**
     * Other Registry object.
     */
    private Registry registry2 = null;

    /**
     * Endpoint.
     */
    private SmartClientEndPointComponent endpoint = null;

    /**
     * Other Endpoint.
     */
    private SmartClientEndPointComponent endpoint2 = null;

    /**
     * Endpoint classloader.
     */
    private ClassLoader endpointClassLoader = null;

    /**
     * Initial Context.
     */
    private Context initialContext = null;

    /**
     * Other Initial Context.
     */
    private Context initialContext2 = null;

    /**
     * Initialize a registry and smart context.
     * @throws Exception if components are not initialized
     */
    @BeforeClass
    public void init() throws Exception {
        // Create a registry
        this.registry = LocateRegistry.createRegistry(RMI_REGISTRY_PORT);
        // Create the associated registry component
        DummyRegistryComponent registryComponent = new DummyRegistryComponent();
        registryComponent.setProviderURL("rmi://localhost:" + RMI_REGISTRY_PORT);

        // Create a registry
        this.registry2 = LocateRegistry.createRegistry(RMI2_REGISTRY_PORT);
        // Create the associated registry component
        DummyRegistryComponent registry2Component = new DummyRegistryComponent();
        registry2Component.setProviderURL("rmi://localhost:" + RMI2_REGISTRY_PORT);

        // Dir for dumping generated class
        this.tmpDir = new File(System.getProperty("java.io.tmpdir") + File.separator + System.getProperty("user.name")
                + File.separator + "easybeans-smart-test");
        if (!this.tmpDir.exists()) {
            this.tmpDir.mkdirs();
        }

        // Create a Smart Endpoint component on a special classloader in order
        // to provide class only on this classloader and to be able to download
        // the class

        final URL tmpDirURL = URLUtils.fileToURL(this.tmpDir);

        PrivilegedAction<URLClassLoader> privilegedAction = new PrivilegedAction<URLClassLoader>() {
            public URLClassLoader run() {
                return new URLClassLoader(new URL[] {tmpDirURL}, Thread.currentThread().getContextClassLoader());
            }
        };
        this.endpointClassLoader = AccessController.doPrivileged(privilegedAction);

        ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.endpointClassLoader);
            this.endpoint = new SmartClientEndPointComponent();
            this.endpoint.setPortNumber(SMART_ENDPOINT_PORT);
            this.endpoint.setRegistryComponent(registryComponent);
            this.endpoint.init();
            this.endpoint.start();

            this.endpoint2 = new SmartClientEndPointComponent();
            this.endpoint2.setPortNumber(SMART2_ENDPOINT_PORT);
            this.endpoint2.setRegistryComponent(registry2Component);
            this.endpoint2.init();
            this.endpoint2.start();
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    /**
     * Try to access to the remote factory.
     * @throws Exception if it fails
     */
    @Test
    public void testAccessSmartFactory() throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, "smart://localhost:" + SMART_ENDPOINT_PORT);
        env.put(SmartContextFactory.EASYBEANS_SMART_JNDI_FACTORY, JRMP_RMI_FACTORY);
        env.put(Context.INITIAL_CONTEXT_FACTORY, SmartContextFactory.class.getName());
        this.initialContext = new InitialContext(env);
    }

    /**
     * Try to access to the other remote factory.
     * @throws Exception if it fails
     */
    @Test
    public void testAccessSmartFactory2() throws Exception {
        Hashtable<String, String> env = new Hashtable<String, String>();
        env.put(Context.PROVIDER_URL, "smart://localhost:" + SMART2_ENDPOINT_PORT);
        env.put(SmartContextFactory.EASYBEANS_SMART_JNDI_FACTORY, JRMP_RMI_FACTORY);
        env.put(Context.INITIAL_CONTEXT_FACTORY, SmartContextFactory.class.getName());
        this.initialContext2 = new InitialContext(env);
    }

    /**
     * Check if the Initial Context when using smart context is linked to the
     * remote RMIi context.
     * @throws Exception if environment is not retrieved.
     */
    @Test(dependsOnMethods = "testAccessSmartFactory")
    public void testGetProviderURL() throws Exception {
        Assert.assertEquals(this.initialContext.getEnvironment().get(Context.PROVIDER_URL), "rmi://localhost:" + RMI_REGISTRY_PORT);
    }

    /**
     * Check if the Initial Context when using smart context is linked to the
     * remote RMIi context.
     * @throws Exception if environment is not retrieved.
     */
    @Test(dependsOnMethods = "testAccessSmartFactory2")
    public void testGetProviderURL2() throws Exception {
        Assert.assertEquals(this.initialContext2.getEnvironment().get(Context.PROVIDER_URL), "rmi://localhost:" + RMI2_REGISTRY_PORT);
    }

    /**
     * Helper method that try to load a class that was dynamically generated on
     * the endpoint side !
     * @param askingClassLoader the classloader that download classes
     * @param className the name of the class
     * @param methodContent the content of the method to check
     * @throws Exception if class is not obtained.
     */
    protected void downloadAndUseClass(final AskingClassLoader askingClassLoader, final String className,
            final String methodContent) throws Exception {

        // Define our class
        String packageName = "testSingleSmartFactory";
        String fullClassName = packageName + "." + className;

        byte[] bytes = GenerateClass.getByteForClass(fullClassName.replace('.', '/'), methodContent);

        // Dump this bytes to a classrugby chabal file (if done in memory the endpoint can't
        // get the bytes)
        File pkgDir = new File(this.tmpDir, packageName);
        pkgDir.mkdir();
        FileOutputStream fos = new FileOutputStream(new File(pkgDir, className + ".class"));
        fos.write(bytes);
        fos.close();

        // Try to find my class (that was generated at runtime !)
        Class<?> myClass = askingClassLoader.loadClass(fullClassName);
        Assert.assertNotNull(myClass, "the class '" + className + "' was not found");

        // build an instance
        Object o = myClass.newInstance();
        Method helloMethod = myClass.getMethod("hello");
        Assert.assertNotNull(helloMethod, "the method hello was not found on '" + helloMethod + "'.");

        // Call method
        Assert.assertEquals(methodContent, helloMethod.invoke(o));

    }

    /**
     * Try to load a class that was dynamically generated on the endpoint side !
     * @throws Exception if class is not obtained.
     */
    @Test
    public void testLoadingClass() throws Exception {
        AskingClassLoader askingClassLoader = new AskingClassLoader("localhost", SMART_ENDPOINT_PORT);
        downloadAndUseClass(askingClassLoader, "buildclass", "hello smart factory !");
    }

    /**
     * Try to load a class that was dynamically generated on the endpoint side !
     * @throws Exception if class is not obtained.
     */
    @Test
    public void testLoadingClass2() throws Exception {
        AskingClassLoader askingClassLoader = new AskingClassLoader("localhost", SMART2_ENDPOINT_PORT);
        downloadAndUseClass(askingClassLoader, "buildclass2", "hello smart factory 2!");
    }

    /**
     * Stop all registries started.
     * @throws Exception if registry is not stopped
     */
    @AfterClass(alwaysRun = true)
    public void stopRegistry() throws Exception {
        if (this.registry != null) {
            UnicastRemoteObject.unexportObject(this.registry, true);
            this.registry = null;
        }

        if (this.registry2 != null) {
            UnicastRemoteObject.unexportObject(this.registry2, true);
            this.registry2 = null;
        }
    }

    /**
     * Stop all endpoint started.
     * @throws Exception if endpoint is not stopped
     */
    @AfterClass(alwaysRun = true)
    public void stopEndPoint() throws Exception {
        if (this.endpoint != null) {
            this.endpoint.stop();
        }
        if (this.endpoint2 != null) {
            this.endpoint2.stop();
        }
    }

}
