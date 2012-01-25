/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
 * Contact: easybeans@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * --------------------------------------------------------------------------
 * $Id: EasyBeansEjbJarDeployableMetadataConfigurator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.configurator;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.impl.configurator.EjbJarDeployableMetadataConfigurator;
import org.ow2.util.scan.api.configurator.IClassConfigurator;
import org.ow2.util.scan.api.metadata.structures.JClass;

/**
 *
 * @author Gael Lalire
 */
public class EasyBeansEjbJarDeployableMetadataConfigurator
        extends
        EjbJarDeployableMetadataConfigurator<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> {

    /**
     * Constructor.
     * @param ejbDeployable the deployable
     */
    public EasyBeansEjbJarDeployableMetadataConfigurator(final EJB3Deployable ejbDeployable) {
        super(new EjbJarArchiveMetadata(ejbDeployable));
    }

    @Override
    protected EasyBeansEjbJarClassMetadata createClassMetadata(final JClass jClass, final EjbJarArchiveMetadata ejbJarArchiveMetadata) {
        EasyBeansEjbJarClassMetadata easyBeansEjbJarClassMetadata = new EasyBeansEjbJarClassMetadata();
        easyBeansEjbJarClassMetadata.setJClass(jClass);
        easyBeansEjbJarClassMetadata.setEjbJarArchiveMetadata(ejbJarArchiveMetadata);
        return easyBeansEjbJarClassMetadata;
    }

    @Override
    protected IClassConfigurator createClassMetadataConfigurator(final EasyBeansEjbJarClassMetadata classMetadata,
            final EjbJarArchiveMetadata ejbJarArchiveMetadata, final boolean annotationParsingDesactived) {
        return new EasyBeansEjbJarClassMetadataConfigurator(classMetadata, ejbJarArchiveMetadata, annotationParsingDesactived);
    }

}
