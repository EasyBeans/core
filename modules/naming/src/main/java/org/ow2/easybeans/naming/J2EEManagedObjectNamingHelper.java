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
 * $Id: J2EEManagedObjectNamingHelper.java 5733 2011-02-21 12:54:34Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.info.IMethodInfo;

/**
 * An helper class to name easybeans objects for components to identify them.
 * @author missonng
 */
public final class J2EEManagedObjectNamingHelper {

    /**
     * The root object id for easybeans managed object ids (the server itself, the applications, the beans, ...).
     */
    private static final String EASYBEANS_ROOT = "/easybeans";

    /**
     * Business method may be @Local or @Remote or nothing.
     */
    private static final String METHOD_PATTERN_SUFFIX = "@?.*$";


    /**
     * A dummy private constructor for checkstyle to be happy.
     */
    private J2EEManagedObjectNamingHelper() {

    }

    /**
     * Get the J2EE managed object id for an easybeans object.
     * @param instance The easybeans object.
     * @return The J2EE managed object id.
     * @throws IllegalArgumentException If no name is define this object.
     */
    public static String getJ2EEManagedObjectId(final Object instance) throws IllegalArgumentException {
        Class<?> clazz = instance.getClass();
        if (EZBServer.class.isAssignableFrom(clazz)) {
            //The object is an easybeans instance.

            return EASYBEANS_ROOT;
        } else if (EZBContainer.class.isAssignableFrom(clazz)) {
            // The object is an EJB container.

            EZBContainer container = (EZBContainer) instance;

            // Get the archive name (remove all character before the last slash/antislash).
            String archiveName = container.getName().replaceAll("(.*[/\\\\])", "");

            // Compute the application id.
            return EASYBEANS_ROOT + "/" + container.getApplicationName() + "/" + archiveName;
        } else if (Factory.class.isAssignableFrom(clazz)) {
            // The object is a bean factory.

            Factory<?, ?> factory = (Factory<?, ?>) instance;
            EZBContainer container = factory.getContainer();

            // Get the archive name (remove all character before the last slash/antislash).
            String archiveName = container.getName().replaceAll("(.*[/\\\\])", "");

            // Get the bean class name (remove all character before the last point).
            String factoryName = factory.getClassName().replaceAll("(.*\\.)", "");

            // Compute the bean id.
            return EASYBEANS_ROOT + "/" + container.getApplicationName() + "/" + archiveName + "/" + factoryName;
        } else {
            throw new java.lang.IllegalArgumentException("Name is not define for argument of type " + instance.getClass());
        }
    }

    /**
     * Helper method to get a method signature.
     * @param method The method.
     * @return The method signature.
     */
    public static String getMethodSignature(final Method method) {
        String signature = method.getName();

        for (Class<?> clazz : method.getParameterTypes()) {
            signature += "_" + clazz.getName();
        }

        return signature;
    }

    /**
     * Helper method to get a method signature.
     * @param method The method.
     * @return The method signature.
     */
    public static String getMethodSignature(final IMethodInfo method) {
        String signature = method.getName();

        for (String parameter : method.getParameters()) {
            signature += "_" + parameter;
        }

        return signature;
    }

    /**
     * Helper method to get the bean methods managed object ids.
     * @param factory The beans factory.
     * @return The bean methods managed object ids.
     */
    public static List<String> getBeanMethodsManagedObjectIds(final Factory<?, ?> factory) {
        List<String> result = new LinkedList<String>();

        List<IMethodInfo> businessMethodsInfo = factory.getBeanInfo().getBusinessMethodsInfo();

        // Build methods ids.
        final String factoryId = getJ2EEManagedObjectId(factory);
        for (final IMethodInfo method : businessMethodsInfo) {
            result.add(factoryId + "/" + getMethodSignature(method));
        }
        return result;
    }

    /**
     * Get a regular expression matching easybeans root id.
     * @return The EasybeansRootFilter.
     */
    public static String getEasybeansRootFilter() {
        // Encode easybeans root id in case it contains regular expression specials characters.
        return encodeJ2EEManagedObjectFilter(EASYBEANS_ROOT);
    }

    /**
     * Get a regular expression matching all J2EE managed objects.
     * @return The AllJ2EEManagedObjectsFilter.
     */
    public static String getAllJ2EEManagedObjectsFilter() {
        return getEasybeansRootFilter() + "(/[^/]+){0,4}";
    }

    /**
     * Get a regular expression matching all applications.
     * @return The AllApplicationsFilter.
     */
    public static String getAllApplicationsFilter() {
        return getEasybeansRootFilter() + "/[^/]+";
    }

    /**
     * Get a regular expression matching all jars.
     * @return The AllJarsFilter.
     */
    public static String getAllJarsFilter() {
        return getAllApplicationsFilter() + "/[^/]+";
    }

    /**
     * Get a regular expression matching all beans.
     * @return The getAllBeansFilter.
     */
    public static String getAllBeansFilter() {
        return getAllJarsFilter() + "/[^/]+";
    }

    /**
     * Get a regular expression matching all methods.
     * @return The getAllMethodsFilter.
     */
    public static String getAllMethodsFilter() {
        return getAllBeansFilter() + "/[^/]+";
    }

    /**
     * Get a regular expression matching all applications relatively to another J2EE managed object.
     * @param j2eeManagedObjectId The J2EE managed object id.
     * @return The AllRelativeApplicationsFilter.
     */
    public static String getAllRelativeApplicationsFilter(final String j2eeManagedObjectId) {
        if (j2eeManagedObjectId.matches(getEasybeansRootFilter())) {
            // The id represent an easybeans instance.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllApplicationsFilter())) {
            // The id represent an application.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId);
        }

        throw new java.lang.IllegalArgumentException("Cannot proccess RelativeApplicationsFilter from id " + j2eeManagedObjectId);
    }

    /**
     * Get a regular expression matching all jars relatively to another J2EE managed object.
     * @param j2eeManagedObjectId The J2EE managed object id.
     * @return The AllRelativeJarsFilter.
     */
    public static String getAllRelativeJarsFilter(final String j2eeManagedObjectId) {
        if (j2eeManagedObjectId.matches(getEasybeansRootFilter())) {
            // The id represent an easybeans instance.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllApplicationsFilter())) {
            // The id represent an application.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllJarsFilter())) {
            // The id represent a jar.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId);
        }

        throw new java.lang.IllegalArgumentException("Cannot proccess RelativeApplicationsFilter from id " + j2eeManagedObjectId);
    }

    /**
     * Get a regular expression matching all beans relatively to another J2EE managed object.
     * @param j2eeManagedObjectId The J2EE managed object id.
     * @return The getAllRelativeBeansFilter.
     */
    public static String getAllRelativeBeansFilter(final String j2eeManagedObjectId) {
        if (j2eeManagedObjectId.matches(getEasybeansRootFilter())) {
            // The id represent an easybeans instance.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllApplicationsFilter())) {
            // The id represent an application.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllJarsFilter())) {
            // The id represent a jar.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllBeansFilter())) {
            // The id represent a bean.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId);
        }

        throw new java.lang.IllegalArgumentException("Cannot proccess RelativeBeansFilter from id " + j2eeManagedObjectId);
    }

    /**
     * Get a regular expression matching all methods relatively to another J2EE managed object.
     * @param j2eeManagedObjectId The J2EE managed object id.
     * @return The getAllRelativeMethodsFilter.
     */
    public static String getAllRelativeMethodsFilter(final String j2eeManagedObjectId) {
        if (j2eeManagedObjectId.matches(getEasybeansRootFilter())) {
            // The id represent an easybeans instance.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllApplicationsFilter())) {
            // The id represent an application.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllJarsFilter())) {
            // The id represent a jar.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllBeansFilter())) {
            // The id represent a bean.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + "/[^/]+";
        } else if (j2eeManagedObjectId.matches(getAllMethodsFilter())) {
            // The id represent a method.
            return encodeJ2EEManagedObjectFilter(j2eeManagedObjectId) + METHOD_PATTERN_SUFFIX;
        }

        throw new java.lang.IllegalArgumentException("Cannot proccess RelativeMethodsFilter from id " + j2eeManagedObjectId);
    }

    /**
     * Encode a J2EE managed object id to get a regular expression matching it.
     * @param j2eeManagedObjectId The J2EE managed object id.
     * @return The J2EE managed object filter.
     */
    public static String encodeJ2EEManagedObjectFilter(final String j2eeManagedObjectId) {
        // escape all backslash characters.
        String result = j2eeManagedObjectId.replace("\\", "\\\\");

        // escape all point characters.
        result = result.replace(".", "\\.");

        // escape all asterisk characters.
        result = result.replace("*", "\\*");

        // escape all caret characters.
        result = result.replace("^", "\\^");

        // escape all dollar sign characters.
        result = result.replace("$", "\\$");

        // escape all plus sign characters.
        result = result.replace("+", "\\+");

        // escape all vertical bar characters.
        result = result.replace("|", "\\|");

        // escape all question mark characters.
        result = result.replace("?", "\\?");

        // escape all opening parenthesis characters.
        result = result.replace("(", "\\(");

        // escape all closing parenthesis characters.
        result = result.replace(")", "\\)");

        // escape all opening bracket characters.
        result = result.replace("[", "\\[");

        // escape all closing bracket characters.
        result = result.replace("]", "\\]");

        // escape all opening brace characters.
        result = result.replace("{", "\\{");

        // escape all closing brace characters.
        result = result.replace("}", "\\}");

        return result;
    }
}
