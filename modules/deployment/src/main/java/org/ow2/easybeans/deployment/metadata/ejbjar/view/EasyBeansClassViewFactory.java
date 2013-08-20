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

import static java.lang.annotation.ElementType.TYPE;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.util.scan.api.IMetadataViewFactory;
import org.ow2.util.scan.api.annotation.ViewTarget;
import org.ow2.util.scan.api.metadata.IMetadata;

@ViewTarget({TYPE})
public class EasyBeansClassViewFactory implements IMetadataViewFactory<EasyBeansEjbJarClassMetadata> {


    public EasyBeansEjbJarClassMetadata build(IMetadata metadata) {
        return new EasyBeansEjbJarClassMetadata(metadata);
    }



}
