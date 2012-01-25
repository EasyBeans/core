/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: MBeansHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jmx;

import java.util.Hashtable;
import java.util.Map;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.ow2.easybeans.api.jmx.EZBManagementIdentifier;
import org.ow2.easybeans.util.loader.ClassUtils;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Singleton object.Creates the MBeans and register them.
 * @author florent
 *
 */
public final class MBeansHelper {
    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MBeansHelper.class);

    /**
     * Singleton instance.
     */
    private static MBeansHelper instance = null;

    /**
     * The Identifier in charge of creating the right ObjectName for a given
     * instance.
     */
    private Map<Class, EZBManagementIdentifier> identifiers = null;

    /**
     * Is Management activated ?
     */
    private boolean activate;

    /**
     * Domain name to enforce if set.
     */
    private static String domainName = null;

    /**
     * Server name to enforce if set.
     */
    private static String serverName = null;

    /**
     * Singleton class, no public constructor.
     */
    private MBeansHelper() {
        identifiers = new Hashtable<Class, EZBManagementIdentifier>();
    }

    /**
     * @return Returns the Singleton MBeansHelper instance.
     */
    public static MBeansHelper getInstance() {
        if (instance == null) {
            instance = new MBeansHelper();
        }
        return instance;
    }

    /**
     * Activate the MBeans registration.
     * @param activate <code>true</code> if mbeans should be
     *        registered, <code>false</code> otherwise.
     */
    public void activate(final boolean activate) {
        this.activate = activate;
    }

    /**
     * Register the instance as a ModelMBean using the delegate.
     * @param <T> instance Type
     * @param instance Object instance to be managed
     * @throws MBeansException if registration fails.
     */
    public <T> void registerMBean(final T instance) throws MBeansException {

        if (activate) {
            String on = getObjectName(instance);

            // register
            try {
                CommonsModelerHelper.registerModelerMBean(instance, on);
            } catch (CommonsModelerException e) {
                throw new MBeansException("Cannot register MBean", e);
            }
        }
    }

    /**
     * Unregister the given Object.
     * @param <T> instance Type
     * @param instance Instance to be deregistered.
     * @throws MBeansException if unregistration fails.
     */
    public <T> void unregisterMBean(final T instance) throws MBeansException {

        if (activate) {
            String on = getObjectName(instance);

            // unregister
            try {
                CommonsModelerHelper.unregisterModelerMBean(new ObjectName(on));
            } catch (MalformedObjectNameException e) {
                logger.warn("Cannot unregister MBean '" + on + "' : " + e.getMessage());
            } catch (NullPointerException e) {
                logger.warn("Cannot unregister MBean '" + on + "' : " + e.getMessage());
            }
        }
    }

    /**
     * @param <T> instance Type
     * @param instance Object instance to be managed
     * @return Returns the instance ObjectName.
     * @throws MBeansException if registration fails.
     */
    public <T> String getObjectName(final T instance) throws MBeansException {

        EZBManagementIdentifier<T> identifier = getIdentifier(instance);

        // gather the ObjectName
        if (identifier != null) {
            StringBuilder sb = new StringBuilder();

            sb.append(identifier.getDomain());
            sb.append(":");
            sb.append(identifier.getTypeProperty());
            String additionnal = identifier.getAdditionnalProperties(instance);
            if (additionnal != null && (!"".equals(additionnal))) {
                sb.append(",");
                sb.append(additionnal);
            }
            sb.append(",");
            sb.append("name=");
            sb.append(identifier.getNamePropertyValue(instance));
            return sb.toString();
        }

        return null;

    }

    /**
     * @param <T> instance type
     * @param instance instance to be managed.
     * @return Returns an {@link EZBManagementIdentifier} for the given Resource type.
     * @throws MBeansException if the Identifier cannot be returned.
     */
    @SuppressWarnings("unchecked")
    private <T> EZBManagementIdentifier<T> getIdentifier(final T instance) throws MBeansException {

        // try to use the cached identifier
        Class clz = instance.getClass();
        if (identifiers.containsKey(clz)) {
            return identifiers.get(clz);
        }

        // the identifier was not cached
        String mbeanClassname = instance.getClass().getName();
        String mbeanPackage = mbeanClassname.substring(0, mbeanClassname
                .lastIndexOf(".") + 1);

        // looking for a class named :
        // <mbean-package>.management.<mbean-model-name>Identifier
        // ex : org.ow2.easybeans.container.management.JContainer3Identifier
        String identifierClassname = mbeanPackage + "management." + clz.getSimpleName() + "Identifier";

        // Instantiate it ...
        try {
            Class<? extends EZBManagementIdentifier> cls = ClassUtils.forName(
                    identifierClassname).asSubclass(EZBManagementIdentifier.class);
            EZBManagementIdentifier<T> id = cls.newInstance();

            // Sets domain if there is a need to override value
            if (domainName != null) {
                id.setDomain(domainName);
            }

            // Sets Server if there is a need to override value
            if (serverName != null) {
                id.setServerName(serverName);
            }

            // cache the identifier
            identifiers.put(clz, id);

            return id;
        } catch (ClassNotFoundException e) {
            throw new MBeansException("Identifier Class not found", e);
        } catch (InstantiationException e) {
            throw new MBeansException("Identifier Class not instantiated", e);
        } catch (IllegalAccessException e) {
            throw new MBeansException("Identifier Class not instantiated", e);
        }
    }

    /**
     * Gets the domain name used by this helper.
     * @return the domain name used by this helper.
     */
    public static String getDomainName() {
        return MBeansHelper.domainName;
    }

    /**
     * Sets the domain name to be used by this helper.
     * @param domainName the domain name to be used by this helper
     */
    public static void setDomainName(final String domainName) {
        MBeansHelper.domainName = domainName;
    }

    /**
     * Gets the server name used by this helper.
     * @return the domain name used by this helper.
     */
    public static String getServerName() {
        return MBeansHelper.serverName;
    }

    /**
     * Sets the server name used by this helper.
     * @param serverName the server name used by this helper.
     */
    public static void setServerName(final String serverName) {
        MBeansHelper.serverName = serverName;
    }



}
