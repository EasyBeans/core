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
 * $Id: CommonsModelerHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jmx;

import java.net.URL;

import javax.management.ObjectName;

import org.apache.commons.modeler.Registry;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * @author Florent Benoit
 */
public final class CommonsModelerHelper {

    /**
     * Registry of commons modeler.
     */
    private static Registry registry = null;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(CommonsModelerHelper.class);

    /**
     * Utility class, no public constructor.
     */
    private CommonsModelerHelper() {

    }

    /**
     * Load the registry of managed object descriptions.
     * @throws CommonsModelerException if the MBeans cannot be registered.
     */
    public static synchronized void initRegistry() throws CommonsModelerException {

        if (registry == null) {
            // Load registry
            registry = Registry.getRegistry(null, null);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            // Load descriptors
            try {
                // EasyBeans statistic component MBean :
                registry.loadDescriptors("org.ow2.easybeans.component.statistic.management", classLoader);

                // EasyBeans specific MBeans :
                registry.loadDescriptors("org.ow2.easybeans.deployer.management", classLoader);

                // JSR 77 MBeans description
                registry.loadDescriptors("org.ow2.easybeans.jsr77", classLoader);

                // EasyBeans additionnal attributes/operations
                extendsManagedBeansDescription("org.ow2.easybeans.container.management", classLoader);
                extendsManagedBeansDescription("org.ow2.easybeans.container.session.stateful.management", classLoader);
                extendsManagedBeansDescription("org.ow2.easybeans.container.session.stateless.management", classLoader);
                extendsManagedBeansDescription("org.ow2.easybeans.server.management", classLoader);
            } catch (Exception e) {
                throw new CommonsModelerException("Cannot load descriptors of commons modeler", e);
            }

            if (logger.isDebugEnabled()) {
                String[] managedBeans = registry.findManagedBeans();
                logger.debug("List of all MBeans descriptors");
                for (String managedBean : managedBeans) {
                    logger.debug("Found managedBean {0}.", managedBean);
                }
                logger.debug("End of list of all MBeans descriptors");
            }
        }
    }

    /**
     * Load <code>mbeans-descriptors-ext.xml</code> extension files.
     *
     * @param packageLoc
     *            package name
     * @param classLoader
     *            loader where resources can be found
     * @throws Exception
     *             if the Resource is unavailable or if the update fails.
     */
    private static void extendsManagedBeansDescription(final String packageLoc,
            final ClassLoader classLoader) throws Exception {
        String resource = packageLoc.replace('.', '/');
        URL url = classLoader.getResource(resource + "/mbeans-descriptors-ext.xml");
        CommonsModelerExtension.updateDescriptors(registry, url.openStream());
    }

    /**
     * Gets the registry.
     *
     * @return registry object.
     * @throws CommonsModelerException if registry is not initialized.
     */
    public static Registry getRegistry() throws CommonsModelerException {
        initRegistry();
        return registry;
    }

    /**
     * Registers an MBean.
     * @param bean the instance to be managed.
     * @param objectName the ON to use.
     * @throws CommonsModelerException if the MBean is not registered.
     */
    public static void registerModelerMBean(final Object bean,
                                     final String objectName) throws CommonsModelerException {
        initRegistry();
        try {
            registry.registerComponent(bean, objectName, null);
        } catch (Exception e) {
            throw new CommonsModelerException("Cannot register MBean with name '" + objectName + "'.", e);
        }
    }

    /**
     * Unregister the given ObjectName.
     * @param on the ObjectName.
     */
    public static void unregisterModelerMBean(final ObjectName on) {
        if (registry != null) {
            registry.unregisterComponent(on);
        }
    }
}
