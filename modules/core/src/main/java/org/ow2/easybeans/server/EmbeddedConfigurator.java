/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: EmbeddedConfigurator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.server;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.xmlconfig.XMLConfiguration;
import org.ow2.util.xmlconfig.XMLConfigurationException;
import org.ow2.util.xmlconfig.properties.SystemPropertyResolver;


/**
 * Allows to configure an embedded server with an XML configuration file.
 * @author Florent Benoit
 */
public final class EmbeddedConfigurator {

    /**
     * Name of the Default XML configuration file.
     */
    private static final String CONFIGURATION_FILE_NAME = "easybeans.xml";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EmbeddedConfigurator.class);


    /**
     * Utility class, no public constructor.
     */
    private EmbeddedConfigurator() {

    }

    /**
     * Configure the given embedded server with the given XML configuration file
     * URL.
     * @param embedded the embedded server to configure.
     * @param xmlConfigurationURL the URL to the xml configuration file.
     * @return the configured embedded instance.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    @Deprecated
    public static Embedded init(final Embedded embedded, final URL xmlConfigurationURL) throws EmbeddedException {
        configure(embedded, xmlConfigurationURL);
        return embedded;
    }

    /**
     * Configure the given embedded server with the given XML configuration file
     * URL.
     * @param embedded the embedded server to configure.
     * @param xmlConfigurationURLs the URLs to the xml configuration files.
     * @param contextualInstances A Map of String to Object used for injection resolution.
     * @return the configured embedded instance.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    public static Embedded init(final Embedded embedded,
                                final List<URL> xmlConfigurationURLs,
                                final Map<String, Object> contextualInstances) throws EmbeddedException {
        configure(embedded, xmlConfigurationURLs, contextualInstances);
        return embedded;
    }

    /**
     * Create and configure an embedded server with the given XML configuration
     * file URL.
     * @param xmlConfigurationURL the URL to the xml configuration file.
     * @return the configured embedded instance.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    @Deprecated
    public static Embedded create(final URL xmlConfigurationURL) throws EmbeddedException {
        return init(new Embedded(), xmlConfigurationURL);
    }

    /**
     * Create and configure an embedded server with the XML configuration file
     * URL found in classpath.
     * @return the configured embedded instance.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    @Deprecated
    public static Embedded create() throws EmbeddedException {
        URL xmlConfigurationURL = Thread.currentThread().getContextClassLoader().getResource(CONFIGURATION_FILE_NAME);
        if (xmlConfigurationURL == null) {
            throw new EmbeddedException("No configuration file with name '" + CONFIGURATION_FILE_NAME
                    + "' was found in classpath.");
        }
        return create(xmlConfigurationURL);
    }

    /**
     * Configure the given embedded server with the xml configuration file.
     * @param embedded the embedded server to configure.
     * @param xmlConfigurationURL the URL to the xml configuration file.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    private static void configure(final Embedded embedded, final URL xmlConfigurationURL) throws EmbeddedException {
        configure(embedded, Collections.singletonList(xmlConfigurationURL), null);
    }

    /**
     * Configure the given embedded server with the xml configuration file.
     * @param embedded the embedded server to configure.
     * @param xmlConfigurationURLs the URLs to the xml configuration files.
     * @param contextualInstances A Map of String to Object used for injection resolution.
     * @throws EmbeddedException if the embedded configuration fails.
     */
    public static void configure(final Embedded embedded,
                                  final List<URL> xmlConfigurationURLs,
                                  final Map<String, Object> contextualInstances) throws EmbeddedException {
        long tStart = System.currentTimeMillis();
        logger.debug("Starting configuration of EasyBeans server");
        XMLConfiguration xmlConfiguration = new XMLConfiguration("easybeans-mapping.xml");

        // Set the source configurations
        if (xmlConfigurationURLs != null) {
            for (URL configuration : xmlConfigurationURLs) {
                xmlConfiguration.addConfigurationFile(configuration);
            }
        }

        // Resolve properties as system properties
        xmlConfiguration.setPropertyResolver(new SystemPropertyResolver());

        // Use the map if not null
        if (contextualInstances != null) {
            xmlConfiguration.setContextualInstances(contextualInstances);
        }

        try {
            xmlConfiguration.configure(embedded);
        } catch (XMLConfigurationException e) {
            throw new EmbeddedException("Cannot configure the embedded server", e);
        }
        logger.debug("Configuration done in : {0} ms", Long.valueOf((System.currentTimeMillis() - tStart)));
    }

}
