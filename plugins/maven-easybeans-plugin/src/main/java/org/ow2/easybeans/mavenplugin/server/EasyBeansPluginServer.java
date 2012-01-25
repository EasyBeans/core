/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: EasyBeansPluginServer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.ow2.easybeans.api.EZBServerConfig;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.carol.CarolComponent;
import org.ow2.easybeans.component.itf.EZBDepMonitorComponent;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.RegistryComponent;
import org.ow2.easybeans.server.Embedded;
import org.ow2.easybeans.server.EmbeddedException;

/**
 * Implementation of IPluginServer interface. Create a local EasyBeans server
 * instance and provide operations on this. If local server is found before
 * creating, this one is used.
 * @author Vincent Michaud
 * @author Alexandre Deneux
 */
public class EasyBeansPluginServer extends AbstractEasyBeansPluginServer {

    /**
     * Default configuration XML file.
     */
    private static final String DEFAULT_CONFIG = "org/ow2/easybeans/mavenplugin/server/easybeans-default-plugin.xml";

    /**
     * Configuration which overwrite user configuration.
     */
    private static final String FORCED_CONFIG = "org/ow2/easybeans/mavenplugin/server/easybeans-forced-config.xml";

    /**
     * Server.
     */
    private Embedded embedded = null;

    /**
     * Server configuration.
     */
    private EZBServerConfig serverConfig = null;

    /**
     * The persistence listener.
     */
    private PersistenceSupport persistenceSupport = null;


    /**
     * Create an Embedded server instance with the specified XML file. If XML
     * files are invalid, default XML file is used.
     * @param configXmlFile Path of EasyBeans XML configuration file
     * @param partialConfigXmlFiles Path list of partial EasyBeans XML
     *                              configuration file.
     */
    public EasyBeansPluginServer(final String configXmlFile, final List<String> partialConfigXmlFiles) {
        super();
        if (localServerFound()) {
            this.embedded = (Embedded) getServer();
            this.serverConfig = this.embedded.getServerConfig();
            if (configXmlFile != null && configXmlFile.length() != 0) {
                getLog().info("Specific project configuration cannot be loaded in this context.");
            }
        } else {
            // If no server was found, create local Embedded instance
            this.embedded = new Embedded();
            setServer(this.embedded);
            loadUserConfiguration(configXmlFile, partialConfigXmlFiles);
        }
    }

    /**
     * Load the configuration of EasyBeans.
     * @param configXmlFile EasyBeans configuration files.
     * @param partialConfigXmlFiles Path list of partial EasyBeans XML
     *                              configuration file.
     */
    private void loadUserConfiguration(final String configXmlFile, final List<String> partialConfigXmlFiles) {

        File file = null;
        this.serverConfig = this.embedded.getServerConfig();
        List<URL> configURLs = this.serverConfig.getConfigurationURLs();

        // Add default EasyBeans configuration file.
        URL configXmlURL = getConfigXmlUrl(configXmlFile);
        configURLs.add(configXmlURL);

        // Add user configuration files.
        if (partialConfigXmlFiles != null) {
            for (String partialConfig : partialConfigXmlFiles) {

                if (partialConfig != null && !partialConfig.equals("")) {

                    file = new File(partialConfig);
                    configXmlURL = getResource(partialConfig);

                    if (file.exists() && file.isFile() && configXmlURL != null && !configURLs.contains(configXmlURL)) {
                        configURLs.add(configXmlURL);
                    } else {
                        getLog().info("Partial EasyBeans configuration file not found : " + file.getAbsolutePath());
                    }
                }
            }
        }

        // Add specific plugin configuration file.
        configXmlURL = Thread.currentThread().getContextClassLoader().getResource(FORCED_CONFIG);
        configURLs.add(configXmlURL);
    }

    /**
     * Launch the server.
     * @param persistenceListener The listener which is called before the deployment, or null
     */
    public void start(final IPersistenceListener persistenceListener) {

        if (!this.embedded.isStarted()) {

            // Set the server will not loop at the end of it's startup.
            this.serverConfig.setShouldWait(false);

            // Disable directory scanning.
            this.serverConfig.setDirectoryScanningEnabled(false);

            // Add hook for shutdown
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(this.embedded));

            try {
                // Start and configure EasyBeans
                this.embedded.start();
                configureComponents(persistenceListener);
            } catch (EmbeddedException ex) {
                getLog().error("Unable to start.", ex);
            }
        }
    }

    /**
     * Get the version of EasyBeans server.
     * @return A String
     */
    @Override
    public String getVersion() {
        return Version.getServerVersion();
    }

    /**
     * Configure components of EasyBeans after launching.
     * @param persistenceListener The listener which is called before the deployment, or null
     * @throws EmbeddedException If a component can not be configured
     */
    private void configureComponents(final IPersistenceListener persistenceListener) throws EmbeddedException {

        // Set the keepRunning mode of the Carol component.
        RegistryComponent rc = this.embedded.getComponent(RegistryComponent.class);
        if (rc == null || !(rc instanceof CarolComponent)) {
            getLog().error("Carol component is not found. The plugin can not start the server.");
            stop();
        } else {
            ((CarolComponent) rc).setKeepRunning(true);
        }

        // Disable DepMonitor if it is launched.
        EZBDepMonitorComponent depMonitorComponent = this.embedded.getComponent(EZBDepMonitorComponent.class);
        if (depMonitorComponent != null) {
            try {
                depMonitorComponent.stop();
            } catch (EZBComponentException e) {
                throw new EmbeddedException("Can not stop the Depmonitor component", e);
            }
        }

        // Get the event component.
        EZBEventComponent eventComponent = this.embedded.getComponent(EZBEventComponent.class);
        if (eventComponent == null) {
            throw new EmbeddedException("The Event component is not found. It is necesary to run the plugin.");
        } else if (persistenceListener != null) {
            this.persistenceSupport = new PersistenceSupport(eventComponent);
            this.persistenceSupport.addListener(persistenceListener);
        }
    }


    /**
     * Find a resource.
     * @param path Path of file.
     * @return URL of resource, or null if file is not found
     */
    private static URL getResource(final String path) {
        if (path != null && path.length() != 0) {
            try {
                File file = new File(path);
                if (file.exists() && file.isFile()) {
                    return file.toURI().toURL();
                }
                return Thread.currentThread().getContextClassLoader().getResource(path);
            } catch (MalformedURLException ex) {
                getLog().error("Invalid resource path.", ex);
            }
        }
        return null;
    }

    /**
     * Get the URL of XML configuration file. If this file doesn't exist, URL
     * returned is the URL of default XML configuration file.
     * @param configXmlFile Path of EasyBeans XML configuration file
     * @return URL of found XML file.
     */
    private static URL getConfigXmlUrl(final String configXmlFile) {
        URL configXmlUrl = null;

        if (configXmlFile != null && configXmlFile.compareTo("") != 0) {
            configXmlUrl = getResource(configXmlFile);
        }

        if (configXmlUrl != null) {
            getLog().debug("Using user-defined configuration file " + configXmlFile + ".");
        } else {
            configXmlUrl = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_CONFIG);
            getLog().debug("No user-defined configuration file found in classpath.");
            getLog().debug("Using default settings from " + DEFAULT_CONFIG + ".");
        }
        return configXmlUrl;
    }

    /**
     * Determine if the server is started or not.
     * @return True if server has been started.
     */
    public boolean isStarted() {
        return this.embedded.isStarted();
    }

    /**
     * Determine if the server is a remote instance.
     * @return True if server is a remote instance.
     */
    public boolean isRemoteInstance() {
        return false;
    }

    /**
     * Hook that is called when process is going to shutdown.
     */
    static class ShutdownHook extends Thread {

        /**
         * Reference to the embedded object.
         */
        private Embedded embedded = null;

        /**
         * Build a new shutdown hook with the given embedded instance.
         * @param embedded The instance to stop
         */
        public ShutdownHook(final Embedded embedded) {
            this.embedded = embedded;
        }

        /**
         * Stop the embedded server.
         */
        @Override
        public void run() {

            // stop embedded if not yet stopped
            try {
                if (!this.embedded.isStopped()) {
                    getLog().info("Stopping EasyBeans...");
                    this.embedded.stop();
                    getLog().info("EasyBeans is now stopped.");
                }
            } catch (EmbeddedException e) {
                getLog().debug("Error while stopping embedded server.", e);
            }
        }
    }

}
