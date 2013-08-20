/**
 * EasyBeans
 * Copyright 2013 Peergreen S.A.S.
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
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;


import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.configurator.EasyBeansSessionConfigurator;
import org.ow2.easybeans.deployment.metadata.ejbjar.helper.MetadataSpecificMerge;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDeploymentDesc;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.metadata.common.impl.xml.parsing.ParsingException;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarMetadataFactory;
import org.ow2.util.ee.metadata.ejbjar.impl.configurator.EjbJarSessionConfigurator;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.IScanner;

/**
 * @author Gael Lalire
 */
public class EasyBeansEjbJarMetadataFactory extends EjbJarMetadataFactory {

    /**
     * The logger.
     */
    private static final Log logger = LogFactory.getLog(EasyBeansEjbJarMetadataFactory.class);

    /**
     * Constructor a scanner.
     * @param scanner the scanner
     */
    public EasyBeansEjbJarMetadataFactory(final IScanner scanner) {
        super(scanner);
    }

    /**
     * This method is called before metadata is filled.
     */
    @Override
    public void beforeScan(final IEjbJarMetadata ejbJarArchiveMetadata) {
        EasyBeansDD easyBeansDD = null;
        try {
          easyBeansDD = EasyBeansDeploymentDesc.getEasyBeansDD(ejbJarArchiveMetadata.getArchive());
        } catch (ParsingException e) {
            logger.warn("Exception during parsing of easybeans.xml ", e);
            easyBeansDD = null;
        }

        ejbJarArchiveMetadata.set(EasyBeansDD.class, easyBeansDD);

    }

    /**
     * Flag the bean that are lifecycle beans.
     */
    @Override
    public void afterMerge(final IEjbJarMetadata ejbJarMetadata) {
        EjbJarArchiveMetadata ejbJarArchiveMetadata = ejbJarMetadata.as(EjbJarArchiveMetadata.class);
        // Apply specific data for Beans
        List<String> beanNames = ejbJarArchiveMetadata.getBeanNames();
        for (String beanName : beanNames) {
            List<String> keys = ejbJarArchiveMetadata.getClassesnameForBean(beanName);
            for (String key : keys) {
                EasyBeansEjbJarClassMetadata easyBeansEjbJarClassMetadata = ejbJarArchiveMetadata
                .getClassForBean(beanName, key);

                for (EasyBeansEjbJarMethodMetadata ejbJarMethodMetadata : easyBeansEjbJarClassMetadata.getMethodMetadataCollection()) {
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
        for (EasyBeansEjbJarClassMetadata ejbClassMetadata : ejbJarArchiveMetadata.getEjbJarClassMetadataCollection()) {
            for (EasyBeansEjbJarMethodMetadata ejbJarMethodMetadata : ejbClassMetadata.getMethodMetadataCollection()) {

                if (ejbJarMethodMetadata.isAroundInvoke()) {
                    ejbClassMetadata.addAroundInvokeMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPostActivate()) {
                    ejbClassMetadata.addPostActivateMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPostConstruct()) {
                    ejbClassMetadata.addPostConstructMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPreDestroy()) {
                    ejbClassMetadata.addPreDestroyMethodMetadata(ejbJarMethodMetadata);
                }
                if (ejbJarMethodMetadata.isPrePassivate()) {
                    ejbClassMetadata.addPrePassivateMethodMetadata(ejbJarMethodMetadata);
                }
            }
        }

        // Merge specific stuff
        MetadataSpecificMerge.merge(ejbJarArchiveMetadata.getEjbJarMetadata());
    }

    @Override
    protected EjbJarSessionConfigurator createEjbJarSessionConfigurator(IArchive archive) {
        return new EasyBeansSessionConfigurator(archive);
    }




}
