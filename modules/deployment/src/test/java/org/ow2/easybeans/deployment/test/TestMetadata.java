/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: TestMetadata.java 6087 2012-01-16 12:47:04Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMetadataFactory;
import org.ow2.util.archive.impl.MemoryArchive;
import org.ow2.util.ee.deploy.api.deployable.metadata.DeployableMetadataException;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.exceptions.EJBJARMetadataException;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.scan.api.ScanException;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.impl.ASMScannerImpl;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * TestMetadata.
 * @author Gael Lalire
 */
public class TestMetadata {

    /**
     * Archive metadata.
     */
    private IEjbJarMetadata ejbJarAnnotationMetadata;

    /**
     * Create metadata.
     * @throws EJBJARMetadataException
     * @throws ScanException if scan issue
     * @throws DeployableHelperException if deployable helper issue
     * @throws DeployableMetadataException if metadata issue
     * @throws ResolverException if resolve fails
     */
    @BeforeClass
    public void init() throws EJBJARMetadataException  {
        MemoryArchive memoryArchive = new MemoryArchive();
        memoryArchive.addClassResource(getClass().getClassLoader(), Arrays.asList(MyEjb.class.getName(), WebserviceSLSB.class.getName()));
        memoryArchive.addResource("META-INF/easybeans.xml", TestMetadata.class.getResource("test-ws-easybeans.xml"));


        EasyBeansEjbJarMetadataFactory deployableFactory =  new EasyBeansEjbJarMetadataFactory(new ASMScannerImpl());
        this.ejbJarAnnotationMetadata = deployableFactory.createArchiveMetadata(memoryArchive);
    }

    /**
     * Test the pool annotation (ezb specific).
     */
    @Test
    public void testPool() {
        IClassMetadata classMetadata = this.ejbJarAnnotationMetadata.getScannedClassMetadata(MyEjb.class.getName()
                .replace('.', '/'));
        EasyBeansEjbJarClassMetadata classView = classMetadata.as(EasyBeansEjbJarClassMetadata.class);
        assertNotNull(classView);
        assertTrue(classView.isStateful());
        IPoolConfiguration poolConfiguration = classView.getPoolConfiguration();
        assertNotNull(poolConfiguration);
        assertEquals(poolConfiguration.getMax(), MyEjb.MAX);
    }

    /**
     * Test the easybeans.xml parsing for WS add-ons
     */
    @Test
    public void testWebservicesValuesFromEasyBeansXML() {
        IClassMetadata classMetadata = this.ejbJarAnnotationMetadata.getEjbJarClassMetadataForEjbName("WebserviceSLSB");
        EasyBeansEjbJarClassMetadata classView = classMetadata.as(EasyBeansEjbJarClassMetadata.class);
        assertNotNull(classView);
        Assert.assertTrue(classView.isStateless());

        String contextRoot = classView.getWebServiceContextRoot();
        String endpointAddress = classView.getWebServiceEndpointAddress();
        String realmName = classView.getWebServiceRealmName();
        String transportGuarantee = classView.getWebServiceTransportGuarantee();
        String authMethod = classView.getWebServiceAuthMethod();
        List<String> httpMethods = classView.getWebServiceHttpMethods();

        Assert.assertEquals(contextRoot, "/mine");
        Assert.assertEquals(endpointAddress, "/ping");
        Assert.assertEquals(realmName, "myRealm");
        Assert.assertEquals(transportGuarantee, "CONFIDENTIAL");
        Assert.assertEquals(authMethod, "NONE");

        Assert.assertEquals(httpMethods.size(), 2);
        Assert.assertEquals(httpMethods.get(0), "GET");
        Assert.assertEquals(httpMethods.get(1), "POST");

    }

}
