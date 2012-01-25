/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EasyBeansEjbJarFieldMetadata.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;

import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarFieldMetadata;
import org.ow2.util.scan.api.metadata.structures.JField;

/**
 * This class represents the annotation metadata of a field.
 * @author Florent Benoit
 */
public class EasyBeansEjbJarFieldMetadata
        extends
        EjbJarFieldMetadata<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -5220250112264050579L;

    /**
     * Constructor.
     * @param jField the field on which we will set/add metadata
     * @param classAnnotationMetadata the parent metadata.
     */
    public EasyBeansEjbJarFieldMetadata(final JField jField, final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        super(jField, classAnnotationMetadata);
    }

}
