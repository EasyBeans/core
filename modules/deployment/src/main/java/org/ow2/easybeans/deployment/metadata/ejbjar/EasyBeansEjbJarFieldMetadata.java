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

import org.ow2.util.ee.metadata.common.api.ICommonFieldMetadataView;
import org.ow2.util.ee.metadata.common.impl.view.CommonView;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IFieldMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;
import org.ow2.util.scan.api.metadata.structures.IField;


/**
 *
 */
public class EasyBeansEjbJarFieldMetadata extends CommonView implements ICommonFieldMetadataView {

    /**
     *
     */
    private static final long serialVersionUID = -1150060196089118619L;

    private final IMetadata metadata;

    public EasyBeansEjbJarFieldMetadata(IMetadata metadata) {
        super(metadata);
        this.metadata = metadata;
    }

    public IFieldMetadata getFieldMetadata() {
        return (IFieldMetadata) metadata;
    }

    public IField getJField() {
        return getFieldMetadata().getJField();
    }

    public String getFieldName() {
        return getJField().getName();
    }

    public EasyBeansEjbJarClassMetadata getClassMetadata() {
        IClassMetadata classMetadata = (IClassMetadata) metadata.getParent();
        return classMetadata.as(EasyBeansEjbJarClassMetadata.class);
    }


}
