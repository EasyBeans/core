/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: MethodInfo.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ow2.easybeans.api.bean.info.IAccessTimeoutInfo;
import org.ow2.easybeans.api.bean.info.ILockTypeInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJEjbAccessTimeout;
import org.ow2.util.ee.metadata.ejbjar.api.struct.ILockType;


/**
 * Allows to represent data of a given method.
 * @author Florent Benoit
 */
public class MethodInfo implements IMethodInfo {

    /**
     * Name.
     */
    private String name = null;

    /**
     * Parameters.
     */
    private List<String> parameterList = null;

    /**
     * return type.
     */
    private String returnType = null;

    /**
     * Exception list.
     */
    private List<String> exceptionList = null;

    /**
     * Method descriptor.
     */
    private String descriptor = null;

    /**
     * AccessTimeout for the given method ?
     */
    private IAccessTimeoutInfo accessTimeout = null;

    /**
     * LockType.
     */
    private ILockTypeInfo lockType = null;

    /**
     * Transacted ?
     */
    private boolean transacted = false;


    /**
     * @return true if this method is afterBeginMethod
     */
    private boolean isAfterBegin = false;

    /**
     * @return true if this method is beforeCompletionMethod
     */
    private boolean isBeforeCompletion = false;

    /**
     * @return true if this method is afterCompletionMethod
     */
    private boolean isAfterCompletion = false;

    /**
     * Build a new method Info based on the given metadata.
     * @param methodMetadata the metadata
     */
    public MethodInfo(final EasyBeansEjbJarMethodMetadata methodMetadata) {
        this.parameterList = new ArrayList<String>();

        // Init
        this.name = methodMetadata.getMethodName();
        String descriptor = methodMetadata.getJMethod().getDescriptor();

        this.returnType = Type.getReturnType(descriptor).getDescriptor();

        String[] exceptions = methodMetadata.getJMethod().getExceptions();
        if (exceptions != null) {
            this.exceptionList = Arrays.asList(exceptions);
        } else {
            this.exceptionList = new ArrayList<String>();
        }

        Type[] argumentTypes = Type.getArgumentTypes(descriptor);
        if (argumentTypes != null) {
            for (Type argumentType : argumentTypes) {
                String className = argumentType.getClassName();
                if (Type.ARRAY == argumentType.getSort()) {
                    className = argumentType.getDescriptor().replace('/', '.');
                }
                this.parameterList.add(className);
            }
        }

        this.descriptor = methodMetadata.getJMethod().getDescriptor();


        // Transacted ?
        this.transacted = methodMetadata.isTransacted();

        EasyBeansEjbJarClassMetadata classMetadata = null;
        if (methodMetadata.isInherited()) {
            classMetadata = methodMetadata.getOriginalEasyBeansClassMetadata();
        } else {
            classMetadata = methodMetadata.getClassMetadata();
        }


        // Lock ?
        ILockType beanlock = classMetadata.getLockType();
        ILockType methodLock = methodMetadata.getLockType();
        // Use Bean Lock if not specified on the method
        if (methodLock == null && beanlock != null) {
            methodLock = beanlock;
        }
        // If specified, add it
        if (methodLock != null) {
            this.lockType = ILockTypeInfo.valueOf(methodLock.toString().toUpperCase());
        }


        // access timeout ?
        IJEjbAccessTimeout beanAccessTimeout = classMetadata.getJavaxEjbAccessTimeout();
        IJEjbAccessTimeout methodAccessTimeout = methodMetadata.getJavaxEjbAccessTimeout();

        // Use Bean Access Timeout if not specified on the method
        if (methodAccessTimeout == null && beanAccessTimeout != null) {
            methodAccessTimeout = beanAccessTimeout;
        }
        // If specified, add it
        if (methodAccessTimeout != null) {
            this.accessTimeout = new AccessTimeoutInfo(methodAccessTimeout.getValue(), methodAccessTimeout.getUnit());
        }


    }


    /**
     * @return descriptor of the method
     */
    public String getDescriptor() {
        return this.descriptor;
    }

    /**
     * @return name of the method
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return parameters of the method
     */
    public List<String> getParameters() {
        return this.parameterList;
    }

    /**
     * @return the return type of the method
     */
    public String getReturnType() {
        return this.returnType;
    }

    /**
     * @return exception of this method.
     */
    public List<String> getExceptions() {
        return this.exceptionList;
    }

    /**
     * @return true if this method is transacted
     */
    public boolean isTransacted() {
        return this.transacted;
    }

    /**
     * @return AccessTimeout
     */
    public IAccessTimeoutInfo getAccessTimeout() {
        return this.accessTimeout;
    }

    /**
     * @return locking strategy
     */
    public ILockTypeInfo getLockType() {
        return this.lockType;
    }

    /**
     * @return true if this method is afterBeginMethod
     */
    public boolean isAfterBegin() {
        return this.isAfterBegin;
    }

    /**
     * Sets this method as a afterBeginMethod.
     */
    public void setAfterBegin() {
        this.isAfterBegin = true;
    }

    /**
     * @return true if this method is beforeCompletionMethod
     */
    public boolean isBeforeCompletion() {
        return this.isBeforeCompletion;
    }

    /**
     * Sets this method as a beforeCompletionMethod.
     */
    public void setBeforeCompletion() {
        this.isBeforeCompletion = true;
    }

    /**
     * @return true if this method is afterCompletionMethod
     */
    public boolean isAfterCompletion() {
        return this.isAfterCompletion;
    }

    /**
     * Sets this method as a afterCompletionMethod.
     */
    public void setAfterCompletion() {
        this.isAfterCompletion = true;
    }

}
