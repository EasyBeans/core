/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: JavaContextHelper.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.helper;

import java.util.Arrays;

import javax.naming.Context;
import javax.naming.NamingException;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.event.naming.JavaContextNamingEvent;
import org.ow2.easybeans.naming.NamingManager;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMethodMetadata;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Builds a Context (java: context).
 * @author Florent Benoit
 */
public final class JavaContextHelper {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JavaContextHelper.class);

    /**
     * Size of the getter prefix.
     */
    private static final int LENGTH = "get".length();

    /**
     * Utility class.
     */
    private JavaContextHelper() {

    }

    /**
     * Build a new context for the given bean.
     * @param bean the bean for which there is a need to build a context.
     * @param easyBeansFactory the factory to build EJBContext
     * @param dispatcher the dispatcher for naming events
     * @param containerConfig the container configuration
     * @return a java: context.
     * @throws JavaContextHelperException if environment cannot be built
     */
    public static Context build(final EasyBeansEjbJarClassMetadata bean,
                                final Factory<?, ?> easyBeansFactory,
                                final IEventDispatcher dispatcher,
                                final EZBContainerConfig containerConfig)
            throws JavaContextHelperException {

        Context javaCtx = null;
        try {
            javaCtx = NamingManager.getInstance().createEnvironmentContext(bean.getClassName(),
                    containerConfig.getEnvContext(), containerConfig.getModuleContext(), containerConfig.getAppContext());
        } catch (NamingException e) {
            throw new IllegalStateException("Cannot build a new environment", e);
        }

        // Send event for java: filling
        JavaContextNamingEvent event = new JavaContextNamingEvent("java:", javaCtx, easyBeansFactory, bean);
        dispatcher.dispatch(event);

        // Check if synchronous listeners have thrown some exceptions
        if (!event.getThrowables().isEmpty()) {
            throw new JavaContextHelperException("Cannot fill java:",
                                                 event.getThrowables().get(0));
        }

        return javaCtx;
    }

    /**
     * Ensures that jndiName is valid and return the default value if it is a null value.
     * @param jndiName to check
     * @param fieldAnnotationMetadata attribute's metadata
     * @return jndi name value.
     */
    public static String getJndiName(final String jndiName, final EasyBeansEjbJarFieldMetadata fieldAnnotationMetadata) {
        String newJndiName = jndiName;
        if (jndiName == null || "".equals(jndiName)) {
            logger.debug("Name property undefined.");
            String descriptor = fieldAnnotationMetadata.getClassMetadata().getJClass().getName();
            newJndiName = descriptor.replace("/", ".") + "/" + fieldAnnotationMetadata.getFieldName();
            logger.debug("Getting environment's entry with default JNDI name: {0}", newJndiName);
        }
        return newJndiName;
    }


    /**
     * Ensures that jndiName is valid and return the default value if it is a null value.
     * @param jndiName to check
     * @param methodMetaData attribute's metadata
     * @return jndi name value.
     */
    public static String getJndiName(final String jndiName, final EasyBeansEjbJarMethodMetadata methodMetaData) {
        String newJndiName = jndiName;
        if (jndiName == null || "".equals(jndiName)) {
            logger.debug("Property name not defined.");
            StringBuilder propertyBuilder = new StringBuilder(methodMetaData.getMethodName());
            propertyBuilder.delete(0, LENGTH);
            propertyBuilder.setCharAt(0, Character.toLowerCase(propertyBuilder.charAt(0)));
            String descriptor = methodMetaData.getClassMetadata().getJClass().getName();
            propertyBuilder.insert(0, descriptor.replace("/", ".") + "/");
            newJndiName = propertyBuilder.toString();
            logger.debug("Getting environment's entry with default JNDI name: {0}",  newJndiName);
        }

        return newJndiName;
    }

    /**
     * Ensure that this method is a valid setter method and return ASM type of the first arg of the method.
     * @param methodMetaData the metadata to check
     * @return ASM type of the first arg of the method.
     */
    public static Type getSetterMethodType(final IEjbJarMethodMetadata methodMetaData) {
        JMethod jMethod = methodMetaData.getJMethod();
        // Should be a setter
        if (!jMethod.getName().startsWith("set") || jMethod.getName().equalsIgnoreCase("set")) {
            throw new IllegalStateException("Method '" + jMethod
                    + "' is invalid. Should be in the setter form setXXX().");
        }

        // Get type of interface
        // Get arguments of the method.
        Type[] args = Type.getArgumentTypes(jMethod.getDescriptor());
        if (args.length != 1) {
            throw new IllegalStateException("Method args '" + Arrays.asList(args) + "' for method '" + jMethod
                    + "' are invalid. Length should be of 1.");
        }
        return args[0];
    }

}
