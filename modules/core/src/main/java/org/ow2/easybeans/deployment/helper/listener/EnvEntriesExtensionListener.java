/**
 * EasyBeans
 * Copyright (C) 2008-2011 Bull S.A.S.
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
 * $Id: EnvEntriesExtensionListener.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.helper.listener;

import javax.naming.Context;
import javax.naming.NamingException;

import org.ow2.easybeans.event.naming.JavaContextNamingEvent;
import org.ow2.util.ee.metadata.common.api.struct.IEnvEntry;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This ExtensionListener is dedicated to build env entries into
 * the <code>java:comp/env</code> Context.
 * @author Guillaume Sauthier
 * @author Florent Benoit
 */
public class EnvEntriesExtensionListener extends AbstractExtensionListener {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(EnvEntriesExtensionListener.class);


    /**
     * Only accepts the event if it's an EnvNamingEvent.
     * @param event tested event
     * @return <code>true</code> if the proposed event is of the expected type
     */
    @Override
    public boolean accept(final IEvent event) {
        if (event instanceof JavaContextNamingEvent) {
            JavaContextNamingEvent namingEvent = (JavaContextNamingEvent) event;

            // source/event-provider-id attribute is used to filter the destination
            if ("java:".equals(namingEvent.getEventProviderId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Process the event.
     * @param event the event
     */
    public void handle(final IEvent event) {

        JavaContextNamingEvent javaContextNamingEvent = (JavaContextNamingEvent) event;
        String beanName = javaContextNamingEvent.getFactory().getBeanInfo().getName();

        Context javaContext = javaContextNamingEvent.getJavaContext();
        Context javaCompEnvCtx = null;
        try {
            javaCompEnvCtx = (Context) javaContext.lookup("comp/env");
        } catch (NamingException e) {
            throwException(javaContextNamingEvent, new IllegalStateException("Cannot lookup java:comp/env element.", e));
        }

        Context javaModuleCtx = null;
        try {
            javaModuleCtx = (Context) javaContext.lookup("module");
        } catch (NamingException e) {
            throwException(javaContextNamingEvent, new IllegalStateException("Cannot lookup java:module element.", e));
        }

        Context javaAppCtx = null;
        try {
            javaAppCtx = (Context) javaContext.lookup("app");
        } catch (NamingException e) {
            throwException(javaContextNamingEvent, new IllegalStateException("Cannot lookup java:app element.", e));
        }

        Context javaGlobalCtx = null;
        try {
            javaGlobalCtx = (Context) javaContext.lookup("global");
        } catch (NamingException e) {
            throwException(javaContextNamingEvent, new IllegalStateException("Cannot lookup java:global element.", e));
        }

        for (IEnvEntry envEntry : javaContextNamingEvent.getBeanMetadata().getEnvEntryCollection()) {
            String name = envEntry.getName();
            Object value = getEnvEntryValue(envEntry, javaContextNamingEvent);
            // if null, no entry in java:comp/env as specified chapter 15.4.1
            if (value != null) {

                Context bindContext = javaCompEnvCtx;

                // if name starts with "java:" locates the correct namespace
                if (name != null && name.startsWith("java:")) {

                    // check authorized context
                    if (name.startsWith("java:module/")) {
                        bindContext = javaModuleCtx;
                        name = name.substring("java:module/".length());
                    } else if (name.startsWith("java:app/")) {
                        bindContext = javaAppCtx;
                        name = name.substring("java:app/".length());
                    } else if (name.startsWith("java:global/")) {
                        bindContext = javaGlobalCtx;
                        name = name.substring("java:global/".length());
                    } else if (name.startsWith("java:comp/env/")) {
                        bindContext = javaCompEnvCtx;
                        name = name.substring("java:comp/env/".length());
                    } else {
                        throwException(javaContextNamingEvent, new IllegalStateException("Invalid Env Entry name '" + name
                                + "' for bean '" + beanName + "'."));
                    }

                }

                try {
                    bindContext.rebind(name, value);
                } catch (NamingException e) {
                    throwException(javaContextNamingEvent, new IllegalStateException("Cannot bind element '" + name
                            + "' for bean '" + beanName + "'.", e));
                }
            }
        }
    }

    /**
     * Gets the value for a given env-entry.
     * @param envEntry the element representing env-entry.
     * @param event the processed event
     * @return the value associated to the given element.
     */
    private Object getEnvEntryValue(final IEnvEntry envEntry, final JavaContextNamingEvent event) {
        final String type = envEntry.getType();
        final String value = envEntry.getValue();

        Object returnedValue = null;

        if (Boolean.class.getName().equals(type)) {
            if ("true".equalsIgnoreCase(value)) {
                returnedValue = Boolean.TRUE;
            } else if ("false".equalsIgnoreCase(value)) {
                returnedValue = Boolean.FALSE;
            }
        } else if (String.class.getName().equals(type)) {
            returnedValue = value;
        } else if (Integer.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Integer(value);
            }
        } else if (Character.class.getName().equals(type)) {
            if (value != null) {
                if (value.length() != 1) {
                    throwException(event, new IllegalStateException("The value '" + value
                                          + "' is not a valid value for env-entry of type java.lang.Character."));
                }
                returnedValue = Character.valueOf(value.charAt(0));
            }
        } else if (Double.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Double(value);
            }
        } else if (Byte.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Byte(value);
            }
        } else if (Short.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Short(value);
            }
        } else if (Long.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Long(value);
            }
        } else if (Float.class.getName().equals(type)) {
            if (value != null) {
                returnedValue = new Float(value);
            }
        } else if (Class.class.getName().equals(type)) {
            if (value != null) {
                // Instantiate the given class object
                try {
                    returnedValue = event.getFactory().getContainer().getClassLoader().loadClass(value);
                } catch (ClassNotFoundException e) {
                    throwException(event, new IllegalStateException("Unable to load the class '" + value + "' for env-entry '"
                            + envEntry.getName() + "' of Bean '" + event.getFactory().getBeanInfo().getName() + "'.", e));
                }
            }
        } else {

            // Check enum type only if it is not null

            if (value != null) {
                Class<?> clazz = null;
                // Instantiate the given class object and check if it is an enum
                // class
                try {
                    clazz = event.getFactory().getContainer().getClassLoader().loadClass(type);
                    // It's an enum class !
                    if (clazz.isEnum()) {
                        Object[] constants = clazz.getEnumConstants();
                        if (constants != null && constants.length > 1) {
                            // Search expected value in the constants
                            boolean found = false;
                            int i = 0;
                            while (!found && i < constants.length) {
                                String constant = constants[i].toString();
                                if (value.equals(constant)) {
                                    found = true;
                                    return constants[i];
                                }
                                i++;
                            }
                        }

                    }
                } catch (ClassNotFoundException e) {
                    this.logger.debug("Unable to load type for value ''{0}'' ", value, e);
                }
            }
            // No value specified, exit
            if (value != null) {
                throwException(event, new IllegalStateException(type + " is not a valid type for env-entry '" + envEntry.getName()
                    + "'."));
            }
        }
        return returnedValue;
    }
}
