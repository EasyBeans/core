/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.strategy;

import java.util.List;

import org.ow2.easybeans.api.bean.info.EZBBeanNamingInfo;
import org.ow2.easybeans.api.naming.EZBJNDINamingInfo;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Strategy used for Java EE 6 naming with java:global, java:app, etc.
 * @author Florent Benoit
 */
public class JavaEE6NamingStrategy implements EZBNamingStrategy {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(JavaEE6NamingStrategy.class);


    /**
     * Gets the JNDI name for a given bean.
     * @param beanInfo Bean information.
     * @return JNDI name for this beanInfo.
     */
    public EZBJNDINamingInfo getJNDINaming(final EZBBeanNamingInfo beanInfo) {

        String appName = beanInfo.getJavaEEApplicationName();
        String moduleName = beanInfo.getModuleName();
        String beanName = beanInfo.getName();
        String interfaceName = beanInfo.getInterfaceName();
        String interfaceSuffix =  "!".concat(interfaceName);



        // Compute global JNDI name (java:global[/app-name]/<module-name>/<bean-name>
        String jndiName = "java:global/";

        // Application name (optional)
        if (appName != null) {
            jndiName = jndiName.concat(appName).concat("/");
        }

        // Module name
        jndiName = jndiName.concat(moduleName).concat("/");

        // Bean name
        jndiName = jndiName.concat(beanName);

        String suffixedJNDIName = jndiName.concat(interfaceSuffix);

        // Create info
        JNDINamingInfo jndiNamingInfo = new JNDINamingInfo(suffixedJNDIName);
        // Aliases to add
        List<String> aliases = jndiNamingInfo.aliases();

        // Add alias if there is a single interface
        if (beanInfo.isSingleInterface()) {
            aliases.add(jndiName);
        }


        // Aliases
        // java:app prefix
        String javaAppAlias = "java:app/".concat(moduleName).concat("/").concat(beanName);
        aliases.add(javaAppAlias.concat(interfaceSuffix));
        // simplified alias
        if (beanInfo.isSingleInterface()) {
            aliases.add(javaAppAlias);
        }

        // java:module
        String javaModuleAlias = "java:module/".concat(beanName);
        aliases.add(javaModuleAlias.concat(interfaceSuffix));
        // simplified alias
        if (beanInfo.isSingleInterface()) {
            aliases.add(javaModuleAlias);
        }


        // mapeddName alias
        if (beanInfo.getMappedName() != null) {
            // mappedName as this if defined if only one interface
            if (beanInfo.isSingleInterface()) {
                jndiNamingInfo.aliases().add(beanInfo.getMappedName());
            } else {
                // Use only the mappedName for the remote interface
                if ("Remote".equals(beanInfo.getMode()) || "RemoteHome".equals(beanInfo.getMode())) {
                    jndiNamingInfo.aliases().add(beanInfo.getMappedName());
                } else {
                    // use a prefix
                    jndiNamingInfo.aliases().add(beanInfo.getMappedName().concat("@").concat(beanInfo.getMode()));
                }
            }
        }


        LOGGER.debug("Create JNDI info ''{0}'' mode=''{1}''", jndiNamingInfo, beanInfo.getMode());
        // return info object
        return jndiNamingInfo;
    }

}
