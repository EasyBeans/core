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
 *
 */
package org.ow2.easybeans.deployment.metadata.ejbjar.view;

import static java.lang.annotation.ElementType.FIELD;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.util.scan.api.IMetadataViewFactory;
import org.ow2.util.scan.api.annotation.ViewTarget;
import org.ow2.util.scan.api.metadata.IMetadata;

@ViewTarget({FIELD})
public class EasyBeansFieldViewFactory implements IMetadataViewFactory<EasyBeansEjbJarFieldMetadata> {

    /**
     *
     */
    private static final long serialVersionUID = 5689673947046351341L;

    public EasyBeansEjbJarFieldMetadata build(IMetadata metadata) {
        return new EasyBeansEjbJarFieldMetadata(metadata);
    }



}
