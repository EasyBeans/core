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
 * $Id: EasyBeansEjbJarClassMetadataConfigurator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.configurator;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.impl.configurator.EjbJarClassMetadataConfigurator;
import org.ow2.util.pool.impl.visitor.PoolVisitor;
import org.ow2.util.scan.api.configurator.IFieldConfigurator;
import org.ow2.util.scan.api.configurator.IMethodConfigurator;
import org.ow2.util.scan.api.metadata.structures.JField;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Configurator used to register annotations specific to EasyBeans.
 * @author Gael Lalire
 */
public class EasyBeansEjbJarClassMetadataConfigurator
        extends
        EjbJarClassMetadataConfigurator<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> {

    /**
     * EasyBeansEjbJarClassMetadataConfigurator.
     * @param easyBeansEjbJarClassMetadata class metadata
     * @param ejbJarArchiveMetadata parent
     * @param annotationParsingDesactived annotationParsingDesactived
     */
    public EasyBeansEjbJarClassMetadataConfigurator(final EasyBeansEjbJarClassMetadata easyBeansEjbJarClassMetadata,
            final EjbJarArchiveMetadata ejbJarArchiveMetadata, final boolean annotationParsingDesactived) {
        super(easyBeansEjbJarClassMetadata, ejbJarArchiveMetadata, annotationParsingDesactived);
    }

    /**
     * register annotation visitor.
     */
    @Override
    protected void registerAnnotationVisitor() {
        super.registerAnnotationVisitor();
        // Add @Pool specific annotation
        getAnnotationVisitors().put(PoolVisitor.TYPE, new PoolVisitor(getClassMetadata()));

    }

    @Override
    protected IFieldConfigurator createFieldMetadataConfigurator(final JField field,
            final EasyBeansEjbJarClassMetadata classMetadata, final boolean annotationParsingDesactived) {
        EasyBeansEjbJarFieldMetadata fieldMetadata = new EasyBeansEjbJarFieldMetadata(field, classMetadata);
        return new EasyBeansEjbJarFieldMetadataConfigurator(fieldMetadata, annotationParsingDesactived);
    }

    @Override
    protected IMethodConfigurator createMethodMetadataConfigurator(final JMethod method,
            final EasyBeansEjbJarClassMetadata classMetadata, final boolean annotationParsingDesactived) {
        EasyBeansEjbJarMethodMetadata methodMetadata = new EasyBeansEjbJarMethodMetadata(method, classMetadata);
        return new EasyBeansEjbJarMethodMetadataConfigurator(methodMetadata, annotationParsingDesactived);
    }

}
