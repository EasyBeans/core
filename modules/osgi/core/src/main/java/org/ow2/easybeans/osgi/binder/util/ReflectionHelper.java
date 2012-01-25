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
 * $Id: ReflectionHelper.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.apache.felix.dependencymanager.DefaultNullObject;
import org.osgi.framework.ServiceReference;

/**
 * Helper class for reflection.
 * @author Guillaume Sauthier
 */
public final class ReflectionHelper {

    /**
     * Extract the value from a field.
     * @param <T> expected class
     * @param bean instance
     * @param field field
     * @param type expected class
     * @return the field's value
     */
    public static <T> T getFieldValue(final Object bean,
                                      final Field field,
                                      final Class<T> type) {
        Object value = null;
        boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            try {
                value = field.get(bean);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unable to get value from the bean '" + bean
                        + "' for the field '" + field + "'.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to get value from the bean '" + bean
                        + "' for the field '" + field + "'.", e);
            }
        } finally {
            field.setAccessible(isAccessible);
        }

        return type.cast(value);
    }

    /**
     * Set the value in the field
     * @param bean instance to be injected
     * @param field field
     * @param value injected value
     */
    public static void setFieldValue(final Object bean,
                                     final Field field,
                                     final Object value) {

        boolean isAccessible = field.isAccessible();
        try {
            field.setAccessible(true);
            try {
                field.set(bean, value);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unable to inject value '" + value + "' in the bean '" + bean
                        + "' for the field '" + field + "'.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to inject value '" + value + "' in the bean '" + bean
                        + "' for the field '" + field + "'.", e);
            }
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    /**
     * Sets the value on the given method of the given instance.
     * @param <T> the type of the value
     * @param method the method to call
     * @param bean the instance on which the method will be called
     * @param value the value to set
     */
    public static <T> void invokeMethod(final Method method, final Object bean, final T value) {
        boolean isAccessible = method.isAccessible();
        try {
            method.setAccessible(true);
            try {
                method.invoke(bean, value);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Unable to call method '" + method + "' in the bean '" + bean
                        + "' for the value '" + value + "'.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Unable to call method '" + method + "' in the bean '" + bean
                        + "' for the value '" + value + "'.", e);
            } catch (InvocationTargetException e) {
                throw new IllegalStateException("Unable to call method '" + method + "' in the bean '" + bean
                        + "' for the value '" + value + "'.", e);
            }
        } finally {
            method.setAccessible(isAccessible);
        }

    }

    /**
     * Extract the dependency name from the method name.
     * Recognized pattern are:
     * <ul>
     *   <li>bind[DependencyName]</li>
     *   <li>unbind[DependencyName]</li>
     *   <li>set[DependencyName]</li>
     *   <li>reset[DependencyName]</li>
     *   <li>unset[DependencyName]</li>
     *   <li>add[DependencyName]</li>
     *   <li>remove[DependencyName]</li>
     * </ul>
     * [DependencyName] will then have its first char lower cased.
     * @param method
     * @return the dependency name
     */
    public static String getNameFromMethod(final Method method) {
        String methodName = method.getName();
        String dependencyName = null;
        if (methodName.startsWith("bind")) {
            dependencyName = removePrefixFromMethod(methodName, "bind");
        } else if (methodName.startsWith("unbind")) {
            dependencyName = removePrefixFromMethod(methodName, "unbind");
        } else if (methodName.startsWith("set")) {
            dependencyName = removePrefixFromMethod(methodName, "set");
        } else if (methodName.startsWith("reset")) {
            dependencyName = removePrefixFromMethod(methodName, "reset");
        } else if (methodName.startsWith("unset")) {
            dependencyName = removePrefixFromMethod(methodName, "unset");
        } else if (methodName.startsWith("add")) {
            dependencyName = removePrefixFromMethod(methodName, "add");
        } else if (methodName.startsWith("remove")) {
            dependencyName = removePrefixFromMethod(methodName, "remove");
        } else {
            throw new IllegalStateException("Unrecognized method name pattern");
        }

        dependencyName = dependencyName.substring(0, 1)
                                       .toLowerCase()
                                       .concat(dependencyName.substring(1));
        return dependencyName;
    }

    /**
     * @param methodName name of the method
     * @param prefix prefix to be removed
     * @return the method name minus it's prefix
     */
    private static String removePrefixFromMethod(final String methodName,
                                                 final String prefix) {
        return methodName.substring(prefix.length());
    }

    /**
     * Extract the service interface from the method.
     * @param method analyzed method
     * @return the service interface
     * @throws IllegalStateException if the method is invalid
     */
    public static Class<?> findServiceInterface(final Method method) {
        Class<?>[] parameters = method.getParameterTypes();
        Class<?> type = null;
        switch(parameters.length) {
        case 1:
            // Rejects Object|ServiceReference type
            type = parameters[0];
            rejectInvalidTypes(type);
            return type;
        }
        throw new IllegalStateException("Needs a service interface Class argument");
    }

    /**
     * Reject type that we cannot inject.
     * @param type checked type
     * @throws IllegalStateException if type is invalid
     */
    private static void rejectInvalidTypes(final Class<?> type) {
        if (type.equals(Object.class) || type.equals(ServiceReference.class)) {
            throw new IllegalStateException("Invalid parameter type: " + type + ". A service interface is required, or serviceInterface should be described in the annotation");
        }
    }

    /**
     * Check if the to be injected value is a Nullable object or not.
     * @param service bean to check
     * @return <code>true</code> if the bean is a nullable object
     */
    public static boolean isNullableObject(final Object service) {
        boolean isNullObject = false;
        Class<?> itf = service.getClass();
        if (Proxy.isProxyClass(itf)) {
            InvocationHandler handler = Proxy.getInvocationHandler(service);
            if (DefaultNullObject.class.isAssignableFrom(handler.getClass())) {
                isNullObject = true;
            }
        }
        return isNullObject;
    }

}
