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
 * $Id: AbsDeployer.java 6036 2011-10-27 09:57:07Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.loader.EasyBeansClassLoader;
import org.ow2.easybeans.persistence.PersistenceUnitManager;
import org.ow2.easybeans.persistence.api.PersistenceXmlFileAnalyzerException;
import org.ow2.easybeans.persistence.xml.JPersistenceUnitInfo;
import org.ow2.easybeans.persistence.xml.PersistenceXmlFileAnalyzer;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.EARDeployable;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.EJBDeployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployable.LibDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.ee.deploy.api.deployer.IDeployer;
import org.ow2.util.ee.deploy.impl.deployer.AbsDeployerList;
import org.ow2.util.ee.deploy.impl.helper.EarUnpackOpts;
import org.ow2.util.ee.deploy.impl.helper.UnpackDeployableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Abstract class that defines common methods for deployer.
 * @author Florent Benoit
 */
public abstract class AbsDeployer<T extends IDeployable<T>> extends AbsDeployerList<T> implements IDeployer<T> {

    /**
     * Folder to create in tmp folder.
     */
    public static final String DEFAULT_FOLDER = "EasyBeans-Deployer";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(AbsDeployer.class);

    /**
     * Embedded server linked to this deployer.
     */
    private EZBServer embedded = null;


    /**
     * Map between String representation of an URL and the associated Deployable that has been deployed.
     */
    private Map<String, IDeployable<?>> deployedDeployables = null;

    /**
     * Default constructor.
     */
    public AbsDeployer() {
        this.deployedDeployables = new HashMap<String, IDeployable<?>>();
    }


    /**
     * Flag the given deployable as deployed.
     * @param path the path to the given deployable
     * @param deployable the deployable to register
     */
    public void setDeployed(final String path, final IDeployable<?> deployable) {
        this.deployedDeployables.put(path, deployable);
    }

    /**
     * Unflag the given deployable as deployed.
     * @param deployable the deployable to unregister
     */
    public void unsetDeployed(final IDeployable<?> deployable) {
        try {
            this.deployedDeployables.remove(deployable.getArchive().getURL().toExternalForm());
        } catch (ArchiveException e) {
            throw new IllegalStateException("Cannot get URL of the deployable '" + deployable + "'.", e);
        }
    }

    /**
     * Get the given deployable if it has been deployed.
     * @param urlString the string representation of the URL of the deployable
     * @return the Deployable object.
     */
    public IDeployable<?> getDeployedDeployable(final String urlString) {
        return this.deployedDeployables.get(urlString);
    }


    /**
     * @return the embedded instance used by this server.
     */
    public EZBServer getEmbedded() {
        return this.embedded;
    }

    /**
     * Receive Embedded instance for this deployer.
     * @param embedded the given instance of the embedded server.
     */
    public void setEmbedded(final EZBServer embedded) {
        this.embedded = embedded;
    }

    /**
     * Build and return a classloader for the given EAR.
     * @param earDeployable the given EAR
     * @return a classloader
     * @throws DeployerException if the classloader cannot be built
     */
    protected ClassLoader getClassLoaderForEAR(final EARDeployable earDeployable) throws DeployerException {
        // Get EJBs of this EAR
        List<EJBDeployable<?>> ejbs = earDeployable.getEJBDeployables();

        // Get libraries of this EAR
        List<LibDeployable> libs = earDeployable.getLibDeployables();

        // Create array of URLs with EJBs + Libraries
        List<URL> urls = new ArrayList<URL>();
        for (EJBDeployable<?> ejb : ejbs) {
            try {
                urls.add(ejb.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the Archive '" + ejb.getArchive() + "'.", e);
            }
        }
        for (LibDeployable lib : libs) {
            try {
                urls.add(lib.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the Archive '" + lib.getArchive() + "'.", e);

            }
        }

        // Create classloader with these URLs
        final URL[] arrayURLs = urls.toArray(new URL[urls.size()]);


        PrivilegedAction<EasyBeansClassLoader> privilegedAction = new PrivilegedAction<EasyBeansClassLoader>() {
            public EasyBeansClassLoader run() {
                return new EasyBeansClassLoader(arrayURLs, Thread.currentThread().getContextClassLoader());
            }
        };
        return AccessController.doPrivileged(privilegedAction);
    }

    /**
     * Gets the persistence unit manager for the given EAR and classloader.
     * @param earDeployable the ear deployable
     * @param appClassLoader the classloader used as deployable
     * @return the given persistence unit manager
     */
    protected PersistenceUnitManager getPersistenceUnitManager(final EARDeployable earDeployable,
            final ClassLoader appClassLoader) {
        // Analyze libraries to detect persistence archive (only once for now
        // and for all libraries)
        // Get libraries of this EAR
        List<LibDeployable> libs = earDeployable.getLibDeployables();
        PersistenceUnitManager persistenceUnitManager = null;
        for (LibDeployable lib : libs) {
            PersistenceUnitManager builtPersistenceUnitManager = null;
            try {
                JPersistenceUnitInfo[] persistenceUnitInfos =
                        PersistenceXmlFileAnalyzer.analyzePersistenceXmlFile(lib.getArchive());
                if (persistenceUnitInfos != null) {
                    builtPersistenceUnitManager =
                            PersistenceXmlFileAnalyzer.loadPersistenceProvider(persistenceUnitInfos, appClassLoader);
                }
            } catch (PersistenceXmlFileAnalyzerException e) {
                throw new IllegalStateException("Failure when analyzing the persistence.xml file", e);
            }

            // Existing manager and new manager found
            if (persistenceUnitManager != null) {
                if (builtPersistenceUnitManager != null) {
                    // Add the persistence unit infos to the existing
                    // persistence unit manager
                    persistenceUnitManager.addExtraPersistenceUnitInfos(builtPersistenceUnitManager.getPersistenceUnitInfos());
                }
            } else {
                // New persistence manager use the built manager
                persistenceUnitManager = builtPersistenceUnitManager;
            }
        }
        return persistenceUnitManager;
    }

    /**
     * Gets Archives of the libraries of this EAR.
     * @param earDeployable the given EAR deployable.
     * @return list of archives
     */
    protected List<IArchive> getLibArchives(final EARDeployable earDeployable) {

        // Build list
        List<IArchive> libArchives = new ArrayList<IArchive>();

        // Get data of all libraries
        for (LibDeployable lib : earDeployable.getLibDeployables()) {
            libArchives.add(lib.getArchive());
        }

        return libArchives;
    }


    /**
     * Deploy an EJB (called by the deploy method).
     * @param ejbDeployable a given EJB deployable
     * @throws DeployerException if the deployment is not done.
     */
    protected void deployEJB(final EJBDeployable ejbDeployable) throws DeployerException {
        if (getEmbedded().isStopped() || getEmbedded().isStopping()) {
            logger.warn("Deployable ''{0}'' won't be deployed as the EasyBeans instance has been stopped", ejbDeployable);
            return;
        }

        logger.info("Deploying {0}", ejbDeployable);
        EZBContainer container = getEmbedded().createContainer(ejbDeployable);
        try {
            container.start();
        } catch (EZBContainerException e) {
            getEmbedded().removeContainer(container);
            throw new DeployerException("Cannot deploy the given EJB '" + ejbDeployable + "'.", e);
        }
    }

    /**
     * Build an instance of the given class.
     * @param clazz the class to instantiate
     * @return a new object
     * @throws DeployerException if the class can't be loaded
     */
    protected static Object newInstance(final Class clazz) throws DeployerException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException e) {
            throw new DeployerException("Cannot make an instance of the class '" + clazz + "'.", e);
        } catch (IllegalAccessException e) {
           throw new DeployerException("Cannot make an instance of the class '" + clazz + "'.", e);
        }
    }

    /**
     * Build an instance by using the given constructor and given parameters.
     * @param constructor the constructor to use
     * @param parameters the parameters of the given constructor
     * @return a new object
     * @throws DeployerException if the class can't be loaded
     */
    protected static Object newInstance(final Constructor constructor, final Object... parameters)
            throws DeployerException {
        try {
            return constructor.newInstance(parameters);
        } catch (IllegalArgumentException e) {
            throw new DeployerException("Cannot create a classloader with constructor '" + constructor + "'", e);
        } catch (InstantiationException e) {
            throw new DeployerException("Cannot create a classloader with constructor '" + constructor + "'", e);
        } catch (IllegalAccessException e) {
            throw new DeployerException("Cannot create a classloader with constructor '" + constructor + "'", e);
        } catch (InvocationTargetException e) {
            throw new DeployerException("Cannot create a classloader with constructor '" + constructor + "'", e);
        }
    }



    /**
     * Load the given class with its given classname.
     * @param className the name of the class to load
     * @return the class object
     * @throws DeployerException if the class can't be loaded
     */
    protected static Class loadClass(final String className) throws DeployerException {
        return loadClass(className, null);
    }

    /**
     * Load the given class with its given classname.
     * @param className the name of the class to load
     * @param classLoader the given classloader (or null to use thread context classloader)
     * @return the class object
     * @throws DeployerException if the class can't be loaded
     */
    protected static Class loadClass(final String className, final ClassLoader classLoader) throws DeployerException {
        // Load the classr
        Class clazz = null;
        try {
            if (classLoader != null) {
                clazz = classLoader.loadClass(className);
            } else {
                clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
            }
        } catch (ClassNotFoundException e) {
            throw new DeployerException("Cannot load the class '" + className + "'", e);
        }
        return clazz;
    }

    /**
     * Invoke the given method on the given object (null for static method) and
     * with the given args.
     * @param method the method to invoke
     * @param object the object on which the method is invoked
     * @param args the arguments of the method
     * @return the result of the invocation
     * @throws DeployerException if the method is not invoked
     */
    protected static Object invoke(final Method method, final Object object, final Object... args) throws DeployerException {
        try {
            return method.invoke(object, args);
        } catch (IllegalArgumentException e) {
            throw new DeployerException("Cannot invoke the method '" + method + "'", e);
        } catch (IllegalAccessException e) {
            throw new DeployerException("Cannot invoke the method '" + method + "'", e);
        } catch (InvocationTargetException e) {
            throw new DeployerException("Cannot invoke the method '" + method + "'", e);
        }
    }

    /**
     * Get the method on the given class with the given method name and the
     * given parameters.
     * @param clazz the class on which search the method
     * @param methodName the name of the method to search
     * @param parameters the class parameters of the method that is searched
     * @return the method found
     * @throws DeployerException if the method is not found
     */
    protected static Method getMethod(final Class clazz, final String methodName, final Class... parameters)
        throws DeployerException {
        try {
            return clazz.getMethod(methodName, parameters);
        } catch (SecurityException e) {
            throw new DeployerException("Cannot get the Method '" + methodName + "' on the '" + clazz + "' class.", e);
        } catch (NoSuchMethodException e) {
            throw new DeployerException("Cannot get the Method '" + methodName + "' on the '" + clazz + "' class.", e);
        }
    }


    /**
     * Undeploy EJB3s of an EAR (called by the undeploy method).
     * @param earDeployable a given EAR deployable
     * @throws DeployerException if the deployment is not done.
     */
    protected void undeployEJB3FromEAR(final EARDeployable earDeployable) throws DeployerException {
        // From which deployable get the containers deployed
        EARDeployable workingDeployable = earDeployable;

        // Check if this archive has been unpacked ?
        EARDeployable unpackedDeployable = earDeployable.getUnpackedDeployable();
        if (unpackedDeployable != null) {
            workingDeployable = unpackedDeployable;
        }

        // Get Containers of this deployable
        List<EZBContainer> containers = new ArrayList<EZBContainer>();
        for (EJB3Deployable ejb3 : workingDeployable.getEJB3Deployables()) {
            EZBContainer container = getEmbedded().findContainer(ejb3.getArchive());
            // not found
            if (container == null) {
                logger.warn("No container found for the archive ''{0}'', creation has maybe failed", ejb3.getArchive());
                continue;
            }
            // found, add it
            containers.add(container);
        }

        // Remove all these containers
        for (EZBContainer container : containers) {
            // stop it
            container.stop();

            // remove it
            getEmbedded().removeContainer(container);
        }
    }

    /**
     * Unpack the given archive in a temp folder, then build a local EARDeployable and fill it with submodules and then return it.
     * @param earDeployable the archive to unpack.
     * @return a new deployable (which is unpacked)
     * @throws DeployerException if the EAR can't be unpacked
     */
    protected EARDeployable unpackEARDeployable(final EARDeployable earDeployable) throws DeployerException {
        EarUnpackOpts earUnpackOpts = new EarUnpackOpts();
        earUnpackOpts.setWarAutoUnpacked(true);
        return UnpackDeployableHelper.unpack(earDeployable, UnpackDeployableHelper.DEFAULT_FOLDER, false, earUnpackOpts);

    }

    /**
     * Checks if the given deployable is deployed or not.
     * @param deployable test if a given deployable is already deployed.
     * @return true if it is deployed else false
     * @throws DeployerException if the undeploy operation fails.
     */
    @Override
    public boolean isDeployed(final IDeployable<T> deployable) throws DeployerException {
        throw new UnsupportedOperationException("Not yet supported");
    }


    /**
     * Checks if the given deployable is supported by the Deployer.
     * @param deployable the deployable to be checked
     * @return true if it is supported, else false.
     */
    @Override
    public boolean supports(final IDeployable<?> deployable) {
        return EARDeployable.class.isInstance(deployable) || EJB3Deployable.class.isInstance(deployable);
    }

}
