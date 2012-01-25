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

import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;


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
     * Transacted ?
     */
    private boolean transacted = false;

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

}
