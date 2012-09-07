/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: MethodHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Allow to load a java.lang.reflect.method from its metadata.
 * @author Florent Benoit
 */
public final class MethodHelper {

    /**
     * Utility class.
     */
    private MethodHelper() {

    }

    /**
     * Gets a signature entry for a given method.
     * @param methodMetadata the metadata used to extract signature
     * @return a signature string
     */
    public static String getSignature(final EasyBeansEjbJarMethodMetadata methodMetadata) {
        return methodMetadata.getJMethod().getName() + methodMetadata.getJMethod().getDescriptor().hashCode();
    }


    /**
     * Allow to get a reflect method from its metadata.
     * @param classname the name of the class that is containing the method
     * @param jMethod the method info
     * @param classLoader the classloader used to load the method's class
     * @return an instance of the method
     */
    public static Method getMethod(final String classname, final JMethod jMethod, final ClassLoader classLoader) {
        // load the class
        Class<?> clazz;
        try {
            clazz = classLoader.loadClass(classname.replace("/", "."));
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the class '" + classname + "'.", e);
        }

        // Get method name
        String methodName = jMethod.getName();
        // Get arguments
        Type[] argumentTypes = Type.getArgumentTypes(jMethod.getDescriptor());

        // Transform string arguments into class objects
        Class<?>[] parameterTypes = null;
        if (argumentTypes != null) {
            List<Class<?>> parameters = new ArrayList<Class<?>>();
            for (Type type : argumentTypes) {
                try {
                    String parameterClassName = type.getClassName().replace("/", ".");
                    Class<?> parameterClass = null;
                    // load parameters
                    if ("int".equals(parameterClassName)) {
                        parameterClass = int.class;
                    } else if ("byte".equals(parameterClassName)) {
                        parameterClass = byte.class;
                    } else if ("char".equals(parameterClassName)) {
                        parameterClass = char.class;
                    } else if ("long".equals(parameterClassName)) {
                        parameterClass = long.class;
                    } else if ("short".equals(parameterClassName)) {
                        parameterClass = short.class;
                    } else if ("float".equals(parameterClassName)) {
                        parameterClass = float.class;
                    } else if ("double".equals(parameterClassName)) {
                        parameterClass = double.class;
                    } else if ("boolean".equals(parameterClassName)) {
                        parameterClass = boolean.class;
                    } else if (Type.ARRAY == type.getSort()) {
                        parameterClass = Class.forName(type.getDescriptor().replace('/', '.'), true, classLoader);
                    } else {
                        parameterClass = classLoader.loadClass(parameterClassName);
                    }
                    parameters.add(parameterClass);
                } catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Unable to load the class '" + type.getClassName() + "'.", e);
                }
            }
            parameterTypes = parameters.toArray(new Class[parameters.size()]);
        }

        // Load the method
        try {
            return clazz.getMethod(methodName, parameterTypes);
        } catch (SecurityException e) {
            throw new IllegalStateException("Unable to get the method '" + methodName + "'.", e);
        } catch (NoSuchMethodException e) {
            // try with the private attribute
            try {
                Method m = clazz.getDeclaredMethod(methodName, parameterTypes);
                if (!m.isAccessible()) {
                    m.setAccessible(true);
                }
                return m;
            } catch (SecurityException e1) {
                throw new IllegalStateException("Unable to get the method '" + methodName + "'.", e1);
            } catch (NoSuchMethodException e1) {
                throw new IllegalStateException("Unable to get the method '" + methodName + "'.", e1);
            }
        }
    }

}
