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
 * $Id: EasyBeansEjbJarDeployableFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;


import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.configurator.EasyBeansEjbJarDeployableMetadataConfigurator;
import org.ow2.easybeans.deployment.metadata.ejbjar.helper.MetadataSpecificMerge;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDeploymentDesc;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.common.impl.xml.parsing.ParsingException;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarDeployableMetadataFactory;
import org.ow2.util.ee.metadata.ejbjar.impl.configurator.EjbJarDeployableMetadataConfigurator;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.IScanner;

/**
 * @author Gael Lalire
 */
public class EasyBeansEjbJarDeployableFactory
        extends
        EjbJarDeployableMetadataFactory<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> {

    /**
     * The logger.
     */
    private static final Log logger = LogFactory.getLog(EasyBeansEjbJarDeployableFactory.class);

    /**
     * Default Constructor.
     */
    public EasyBeansEjbJarDeployableFactory() {
        super();
    }

    /**
     * Constructor a scanner.
     * @param scanner the scanner
     */
    public EasyBeansEjbJarDeployableFactory(final IScanner scanner) {
        super(scanner);
    }

    /**
     * This method is called before metadata is filled.
     */
    @Override
    public void beforeScan(final EjbJarArchiveMetadata ejbJarArchiveMetadata) {
        EasyBeansDD easyBeansDD = null;
        try {
          easyBeansDD = EasyBeansDeploymentDesc.getEasyBeansDD(ejbJarArchiveMetadata.getDeployable().getArchive());
        } catch (ParsingException e) {
            logger.warn("Exception during parsing of easybeans.xml ", e);
            easyBeansDD = null;
        }

        ejbJarArchiveMetadata.setEasyBeansDD(easyBeansDD);

    }

    /**
     * Flag the bean that are lifecycle beans.
     */
    @Override
    public void afterMerge(final EjbJarArchiveMetadata ejbJarArchiveMetadata) {
        // Apply specific data for Beans
        List<String> beanNames = ejbJarArchiveMetadata.getBeanNames();
        for (String beanName : beanNames) {
            List<String> keys = ejbJarArchiveMetadata.getClassesnameForBean(beanName);
            for (String key : keys) {
                EasyBeansEjbJarClassMetadata easyBeansEjbJarClassMetadata = ejbJarArchiveMetadata
                .getClassForBean(beanName, key);
                for (EasyBeansEjbJarMethodMetadata ejbJarMethodMetadata : easyBeansEjbJarClassMetadata
                        .getMethodMetadataCollection()) {
                    if (ejbJarMethodMetadata.isAroundInvoke()) {
                        easyBeansEjbJarClassMetadata.addAroundInvokeMethodMetadata(ejbJarMethodMetadata);
                    }
                    if (ejbJarMethodMetadata.isPostActivate()) {
                        easyBeansEjbJarClassMetadata.addPostActivateMethodMetadata(ejbJarMethodMetadata);
                    }
                    if (ejbJarMethodMetadata.isPostConstruct()) {
                        easyBeansEjbJarClassMetadata.addPostConstructMethodMetadata(ejbJarMethodMetadata);
                    }
                    if (ejbJarMethodMetadata.isPreDestroy()) {
                        easyBeansEjbJarClassMetadata.addPreDestroyMethodMetadata(ejbJarMethodMetadata);
                    }
                    if (ejbJarMethodMetadata.isPrePassivate()) {
                        easyBeansEjbJarClassMetadata.addPrePassivateMethodMetadata(ejbJarMethodMetadata);
                    }
                }
            }
        }


        // Apply specific data for other classes
        for (EasyBeansEjbJarClassMetadata easyBeansEjbJarClassMetadata : ejbJarArchiveMetadata.getEjbJarClassMetadataCollection()) {
            for (EasyBeansEjbJarMethodMetadata ejbJarMethodMetadata : easyBeansEjbJarClassMetadata
                    .getMethodMetadataCollection()) {
                if (ejbJarMethodMetadata.isAroundInvoke()) {
                    easyBeansEjbJarClassMetadata.addAroundInvokeMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPostActivate()) {
                    easyBeansEjbJarClassMetadata.addPostActivateMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPostConstruct()) {
                    easyBeansEjbJarClassMetadata.addPostConstructMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPreDestroy()) {
                    easyBeansEjbJarClassMetadata.addPreDestroyMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPrePassivate()) {
                    easyBeansEjbJarClassMetadata.addPrePassivateMethodMetadata(ejbJarMethodMetadata);
                }
            }
        }

        // Merge specific stuff
        MetadataSpecificMerge.merge(ejbJarArchiveMetadata);
    }

    /**
     * Build a new deployable metadata configurator for the given ddeployable.
     * @param ejbDeployable the given deployable
     * @return an instance of a metadataconfigurator
     */
    @Override
    protected EjbJarDeployableMetadataConfigurator<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> createEjbJarDeployableMetadataConfigurator(
            final EJB3Deployable ejbDeployable) {
        return new EasyBeansEjbJarDeployableMetadataConfigurator(ejbDeployable);
    }

    @Override
    protected EjbJarArchiveMetadata createEjbJaDeployableMetadata(final EJB3Deployable ejbDeployable) {
        return new EjbJarArchiveMetadata(ejbDeployable);
    }




}
