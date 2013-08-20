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

import static org.ow2.util.ee.metadata.common.api.struct.ITransactionAttributeType.REQUIRED;

import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansMethodView;
import org.ow2.util.ee.metadata.common.api.struct.ITransactionAttributeType;
import org.ow2.util.marshalling.Serialization;
import org.ow2.util.marshalling.SerializationException;
import org.ow2.util.scan.api.configurator.IViewConfigurator;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;
import org.ow2.util.scan.api.metadata.IMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IMethod;
import org.ow2.util.scan.impl.metadata.MethodMetadata;

public class EasyBeansEjbJarMethodMetadata extends EasyBeansMethodView {

    /**
     *
     */
    private static final long serialVersionUID = -5496916028636922454L;

    public EasyBeansEjbJarMethodMetadata(IMethod method, EasyBeansEjbJarClassMetadata parent) {
        super(new MethodMetadata(method, parent.getClassMetadata()));
        // add views
        List<IViewConfigurator> viewConfigurators = parent.getClassMetadata().getViewConfigurators();
        for (IViewConfigurator viewConfigurator : viewConfigurators) {
            getMethodMetadata().addViewConfigurator(viewConfigurator);
        }
    }



    public EasyBeansEjbJarMethodMetadata(IMetadata methodMetadata) {
        super(methodMetadata);
    }

    public EasyBeansEjbJarClassMetadata getClassMetadata() {
        IClassMetadata iClassMetadata = getMethodMetadata().getClassMetadata();
        return iClassMetadata.as(EasyBeansEjbJarClassMetadata.class);
    }

    public void setInherited(boolean inherited, EasyBeansEjbJarClassMetadata parent) {
        setInherited(inherited, parent.getClassMetadata());
    }


    public EasyBeansEjbJarClassMetadata getOriginalEasyBeansClassMetadata() {
        IClassMetadata originalClassMetadata = getOriginalClassMetadata();
        if (originalClassMetadata != null) {
            return originalClassMetadata.as(EasyBeansEjbJarClassMetadata.class);
        }
        return null;
    }

    public EasyBeansEjbJarMethodMetadata clone(EasyBeansEjbJarClassMetadata newParent) {
        // duplicate metadata without parent
        IMethodMetadata methodMetadata = getMethodMetadata();
        IMethodMetadata clonedMetadata = null;
        IClassMetadata parent = methodMetadata.getClassMetadata();
        try {
            clonedMetadata = Serialization.cloneObject(methodMetadata);
            clonedMetadata.setClassMetadata(newParent.getClassMetadata());
        } catch (SerializationException e) {
            throw new IllegalStateException("Cannot clone method for '" + methodMetadata.getJMethod().getName() + "'", e);
        } finally {
            methodMetadata.setClassMetadata(parent);
        }
        return new EasyBeansEjbJarMethodMetadata(clonedMetadata);

    }

    public void setJMethod(IMethod method) {
        getMethodMetadata().setJMethod(method);
    }

    /**
     * Sets the inheritance of this method.
     * @param privateSuperCallGenerated true if a method needs to be generated for a super private call method.
     * @param originalClassMetadata the metadata of the original class (not
     *        inherited)
     */
    public void setPrivateSuperCallGenerated(final boolean privateSuperCallGenerated, final EasyBeansEjbJarClassMetadata originalClassMetadata, final int inheritanceLevel) {
        setPrivateSuperCallGenerated(privateSuperCallGenerated, originalClassMetadata.getClassMetadata(), inheritanceLevel);
    }

    @Override
    public ITransactionAttributeType getTransactionAttributeType() {
        ITransactionAttributeType superType = super.getTransactionAttributeType();
        if (superType == null) {
            return REQUIRED;
        }
        return superType;
    }


}
