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

import java.util.Arrays;
import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarDeployableFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.archive.impl.MemoryArchive;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.metadata.DeployableMetadataException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.scan.api.ScanException;
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
    private EjbJarArchiveMetadata ejbJarAnnotationMetadata;

    /**
     * Create metadata.
     * @throws ScanException if scan issue
     * @throws DeployableHelperException if deployable helper issue
     * @throws DeployableMetadataException if metadata issue
     * @throws ResolverException if resolve fails
     */
    @BeforeClass
    public void init() throws ScanException, DeployableMetadataException, DeployableHelperException, ResolverException {
        MemoryArchive memoryArchive = new MemoryArchive();
        memoryArchive.addClassResource(getClass().getClassLoader(), Arrays.asList(MyEjb.class.getName(), WebserviceSLSB.class.getName()));
        memoryArchive.addResource("META-INF/easybeans.xml", TestMetadata.class.getResource("test-ws-easybeans.xml"));

        EasyBeansEjbJarDeployableFactory deployableFactory = new EasyBeansEjbJarDeployableFactory();
        this.ejbJarAnnotationMetadata = deployableFactory.createDeployableMetadata(EJB3Deployable.class.cast(DeployableHelper.getDeployable(memoryArchive)));

        // ResolverHelper.resolve(this.ejbJarAnnotationMetadata);
    }

    /**
     * Test the pool annotation (ezb specific).
     */
    @Test
    public void testPool() {
        EasyBeansEjbJarClassMetadata classMetadata = this.ejbJarAnnotationMetadata.getScannedClassMetadata(MyEjb.class.getName()
                .replace('.', '/'));
        Assert.assertTrue(classMetadata.isStateful());
        IPoolConfiguration poolConfiguration = classMetadata.getPoolConfiguration();
        Assert.assertNotNull(poolConfiguration);
        Assert.assertEquals(poolConfiguration.getMax(), MyEjb.MAX);
    }

    /**
     * Test the easybeans.xml parsing for WS add-ons
     */
    @Test
    public void testWebservicesValuesFromEasyBeansXML() {
        EasyBeansEjbJarClassMetadata classMetadata = this.ejbJarAnnotationMetadata.getEjbJarClassMetadataForEjbName("WebserviceSLSB");
        Assert.assertTrue(classMetadata.isStateless());

        String contextRoot = classMetadata.getWebServiceContextRoot();
        String endpointAddress = classMetadata.getWebServiceEndpointAddress();
        String realmName = classMetadata.getWebServiceRealmName();
        String transportGuarantee = classMetadata.getWebServiceTransportGuarantee();
        String authMethod = classMetadata.getWebServiceAuthMethod();
        List<String> httpMethods = classMetadata.getWebServiceHttpMethods();

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
