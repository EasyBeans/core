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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansEjbJarView;
import org.ow2.util.ee.metadata.common.api.struct.IJInterceptors;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.xml.struct.IEJB3;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;

public class EjbJarArchiveMetadata extends EasyBeansEjbJarView {

    private final IEjbJarMetadata ejbJarMetadata;

    public EjbJarArchiveMetadata(IMetadata ejbJarMetadata) {
        super(ejbJarMetadata);
        this.ejbJarMetadata = (IEjbJarMetadata) ejbJarMetadata;
    }

    public List<String> getBeanNames() {
        return ejbJarMetadata.getBeanNames();
    }

    public List<String> getClassesnameForBean(String beanName) {
        return ejbJarMetadata.getClassesnameForBean(beanName);
    }


    public Collection<EasyBeansEjbJarClassMetadata> getClassesForBean(String beanName) {
        Collection<IClassMetadata> collection = ejbJarMetadata.getClassesForBean(beanName);
        List<EasyBeansEjbJarClassMetadata> lst = new ArrayList<EasyBeansEjbJarClassMetadata>();
        for (IClassMetadata classMetadata : collection) {
            lst.add(classMetadata.as(EasyBeansEjbJarClassMetadata.class));
        }
        return lst;
    }

    public EasyBeansEjbJarClassMetadata getScannedClassMetadata(String className) {
        IClassMetadata classMetadata = ejbJarMetadata.getScannedClassMetadata(className);
        if (classMetadata != null) {
            return classMetadata.as(EasyBeansEjbJarClassMetadata.class);
        }
        return null;
    }

    public EasyBeansEjbJarClassMetadata getClassForBean(String beanName, String className) {
        IClassMetadata classMetadata = ejbJarMetadata.getClassForBean(beanName, className);
        if (classMetadata != null) {
            return classMetadata.as(EasyBeansEjbJarClassMetadata.class);
        }
        return null;
    }

    public List<EasyBeansEjbJarClassMetadata> getEjbJarClassMetadataCollection() {
        Collection<IClassMetadata> collection = ejbJarMetadata.getEjbJarClassMetadataCollection();
        List<EasyBeansEjbJarClassMetadata> lst = new ArrayList<EasyBeansEjbJarClassMetadata>();
        for (IClassMetadata classMetadata : collection) {
            lst.add(classMetadata.as(EasyBeansEjbJarClassMetadata.class));
        }

        return lst;
    }

    public IJInterceptors getDefaultInterceptorsClasses() {
        return ejbJarMetadata.getDefaultInterceptorsClasses();
    }

    public IEJB3 getEjb3() {
        return ejbJarMetadata.getEjb3();
    }

    public IEjbJarMetadata getEjbJarMetadata() {
        return ejbJarMetadata;
    }

}
