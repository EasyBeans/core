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

import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansClassView;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IFieldMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;
import org.ow2.util.scan.api.metadata.IMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IClass;
import org.ow2.util.scan.api.metadata.structures.IMethod;

public class EasyBeansEjbJarClassMetadata extends EasyBeansClassView {

    public EasyBeansEjbJarClassMetadata(IMetadata classMetadata) {
        super(classMetadata);
    }


    public EasyBeansEjbJarMethodMetadata getMethodMetadata(IMethod method) {
        IMethodMetadata methodMetadata = getClassMetadata().getMethodMetadata(method);
        if (methodMetadata == null) {
            return null;
        }
        return methodMetadata.as(EasyBeansEjbJarMethodMetadata.class);
    }

    public Collection<EasyBeansEjbJarMethodMetadata> getMethodMetadataCollection() {
        List<EasyBeansEjbJarMethodMetadata> methodsViews = new ArrayList<EasyBeansEjbJarMethodMetadata>();
        if (getClassMetadata().getMethodMetadataCollection() != null) {
            for (IMethodMetadata methodMetadata : getClassMetadata().getMethodMetadataCollection()) {
                methodsViews.add(methodMetadata.as(EasyBeansEjbJarMethodMetadata.class));
            }
        }
        return methodsViews;
    }

    protected List<EasyBeansEjbJarFieldMetadata> getEasyBeansFieldMetadata(Collection<IFieldMetadata> fieldsMetadata) {
        List<EasyBeansEjbJarFieldMetadata> fieldViews = new ArrayList<EasyBeansEjbJarFieldMetadata>();
        if (fieldsMetadata != null) {
            for (IFieldMetadata fieldMetadata : fieldsMetadata) {
                fieldViews.add(fieldMetadata.as(EasyBeansEjbJarFieldMetadata.class));
            }
        }
        return fieldViews;
    }

    public List<EasyBeansEjbJarFieldMetadata> getStandardFieldMetadataCollection() {
        return getEasyBeansFieldMetadata(getClassMetadata().getFieldMetadataCollection());
    }



    public String getSuperName() {
        return getClassMetadata().getJClass().getSuperName();
    }

    public IClass getJClass() {
        return getClassMetadata().getJClass();
    }

    public String[] getInterfaces() {
        return getClassMetadata().getJClass().getInterfaces();
    }

    public void addPostConstructMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        addPostConstructMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }

    protected List<EasyBeansEjbJarMethodMetadata> getEasyBeansMetadata(List<IMethodMetadata> methodsMetadata) {
        List<EasyBeansEjbJarMethodMetadata> methodsViews = new ArrayList<EasyBeansEjbJarMethodMetadata>();
        if (methodsMetadata != null) {
            for (IMethodMetadata methodMetadata : methodsMetadata) {
                methodsViews.add(methodMetadata.as(EasyBeansEjbJarMethodMetadata.class));
            }
        }
        return methodsViews;
    }


    public List<EasyBeansEjbJarMethodMetadata> getPostConstructMethodsMetadata() {
        return getEasyBeansMetadata(getDelegatePostConstructMethodsMetadata());
    }

    public List<EasyBeansEjbJarMethodMetadata> getPreDestroyMethodsMetadata() {
        return getEasyBeansMetadata(getDelegatePreDestroyMethodsMetadata());
    }

    public List<EasyBeansEjbJarMethodMetadata> getPostActivateMethodsMetadata() {
        return getEasyBeansMetadata(getDelegatePostActivateMethodsMetadata());
    }

    public List<EasyBeansEjbJarMethodMetadata> getPrePassivateMethodsMetadata() {
        return getEasyBeansMetadata(getDelegatePrePassivateMethodsMetadata());
    }


    public void addPreDestroyMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        addPreDestroyMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }

    public void addPostActivateMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        addPostActivateMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }

    public void addPrePassivateMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        addPrePassivateMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }

    public void addAroundInvokeMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        addAroundInvokeMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }


    public List<EasyBeansEjbJarMethodMetadata> getAroundInvokeMethodMetadatas() {
        return getEasyBeansMetadata(getDelegateAroundInvokeMethodMetadatas());
    }

    public void addStandardMethodMetadata(EasyBeansEjbJarMethodMetadata easyBeansEjbJarMethodMetadata) {
        getClassMetadata().addMethodMetadata(easyBeansEjbJarMethodMetadata.getMethodMetadata());
    }

    public EjbJarArchiveMetadata getEjbJarMetadata() {
        IEjbJarMetadata parent = (IEjbJarMetadata) getClassMetadata().getParent();
        return parent.as(EjbJarArchiveMetadata.class);
    }

    public EasyBeansEjbJarClassMetadata getEasyBeansLinkedClassMetadata(String className) {
        IClassMetadata classMetadata = getLinkedClassMetadata(className);
        if (classMetadata != null) {
            return classMetadata.as(EasyBeansEjbJarClassMetadata.class);
        }
        return null;
    }

}
