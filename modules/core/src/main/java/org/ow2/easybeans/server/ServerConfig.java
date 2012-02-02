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
 * $Id: ServerConfig.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.server;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.EZBConfigurationExtension;
import org.ow2.easybeans.api.EZBServerConfig;

/**
 * Defines a configuration class that can be used to start the embedded EJB3 server.
 * @author Florent Benoit
 */
public class ServerConfig implements EZBServerConfig {

    /**
     * List of path to lookup for deploying modules.
     */
    private List<File> deployDirectories = null;

    /**
     * Wait at the end of the start.
     */
    private boolean shouldWait = true;

    /**
     * Init JACC at startup ?
     */
    private boolean initJACC = true;

    /**
     * Use of MBeans.
     */
    private boolean useMBeans = true;

    /**
     * Start the JMX connector ?
     */
    private boolean startJMXConnector = true;

    /**
     * use EasyBeans naming mechanism or one of the embedded server.
     */
    private boolean useNaming = true;

    /**
     * List of configuration extensions instances.
     */
    private List<EZBConfigurationExtension> extensionFactories = null;

    /**
     * Is the Directory scanning activated.
     */
    private boolean directoryScanningEnabled = true;

    /**
     * Add the ejb container components at startup ? (true by default)
     */
    private boolean addEmbeddedComponents = true;

    /**
     * Register the EasyBeans deployer MBean ?
     */
    private boolean registerDeployerMBean = true;

    /**
     * Register the J2EEServer MBean ? (it shouldn't be bound if it is linked to another server)
     */
    private boolean registerJ2EEServerMBean = true;

    /**
     * Auto configure components (by intializing them and starting them).
     * Should be disable if components are started independently.
     */
    private boolean autoConfigureComponents = true;

    /**
     * Should the server stop its components during shutdown process ?
     */
    private boolean stopComponentsDuringShutdown = true;

    /**
     * A description of the embedded server.
     */
    private String description = null;

    /**
     * List of configurations URL to use.
     */
    private LinkedList<URL> configurationURLs = null;

    /**
     * A Map of String to Object used for injection resolution.
     */
    private Map<String, Object> configurationMap = null;

    /**
     * Use or not the EasyBeans v1 legacy naming strategy.
     * @return true if this is the case.
     */
    private boolean useLegacyNamingStrategy = false;

    /**
     * Constructor.
     */
    public ServerConfig() {
        this.deployDirectories = new ArrayList<File>();
        this.extensionFactories = new ArrayList<EZBConfigurationExtension>();
        this.configurationURLs = new LinkedList<URL>();
        this.configurationMap = new HashMap<String, Object>();
    }

    /**
     * @return the list of path for loading/looking Java EE modules.
     */
    public List<File> getDeployDirectories() {
        return this.deployDirectories;
    }

    /**
     * Sets the list of path for loading/looking Java EE modules.
     * @param deployDirectories the list of path for loading/looking Java EE modules.
     */
    public void setDeployDirectories(final List<File> deployDirectories) {
        this.deployDirectories = deployDirectories;
    }

    /**
     * Add a directory to the list of path for loading/looking Java EE modules.
     * @param deployDirectory a path for loading/looking Java EE modules.
     */
    public void addDeployDirectory(final File deployDirectory) {
        this.deployDirectories.add(deployDirectory);
    }


    /**
     * @return true if the server should wait when starting embedded server.
     */
    public boolean shouldWait() {
        return this.shouldWait;
    }

    /**
     * Sets if the server will loop at the end of it's startup.
     * @param shouldWait true/false
     */
    public void setShouldWait(final boolean shouldWait) {
        this.shouldWait = shouldWait;
    }


    /**
     * Use or not the MBeans.
     * @return true if this is the case.
     */
    public boolean isUsingMBeans() {
        return this.useMBeans;
    }

    /**
     * Sets the value for using MBeans.
     * @param useMBeans the boolean value.
     */
    public void setUseMBeans(final boolean useMBeans) {
        this.useMBeans = useMBeans;
    }

    /**
     * Use or not the EasyBeans naming system.
     * @return true if this is the case.
     */
    public boolean isUsingNaming() {
        return this.useNaming;
    }

    /**
     * Sets the value for using the EasyBeans naming system.
     * @param useNaming the boolean value.
     */
    public void setUseNaming(final boolean useNaming) {
        this.useNaming = useNaming;
    }

    /**
     * Adds an {@link EasyBeansConfigurationExtension} in the Facory list.
     * @param extension the factory FQN.
     */
    public void addExtensionFactory(final EZBConfigurationExtension extension) {
        this.extensionFactories.add(extension);
    }

    /**
     * @return Returns the list of {@link EasyBeansConfigurationExtension}.
     */
    public List<EZBConfigurationExtension> getExtensionFactories() {
        return this.extensionFactories;
    }

    /**
     * @return the directoryScanningEnabled
     */
    public boolean isDirectoryScanningEnabled() {
        return this.directoryScanningEnabled;
    }

    /**
     * @param directoryScanningEnabled the directoryScanningEnabled to set
     */
    public void setDirectoryScanningEnabled(final boolean directoryScanningEnabled) {
        this.directoryScanningEnabled = directoryScanningEnabled;
    }

    /**
     * Init or not JACC at startup.
     * @param initJACC initialization of JACC provider.
     */
    public void setInitJACC(final boolean initJACC) {
        this.initJACC = initJACC;
    }

    /**
     * @return true if JACC provider needs to be initialized at startup.
     */
    public boolean initJACC() {
        return this.initJACC;
    }

    /**
     * Sets the flag for adding before the startup the core components.
     * @param addEmbeddedComponents the boolean value
     */
    public void setAddEmbeddedComponents(final boolean addEmbeddedComponents) {
        this.addEmbeddedComponents = addEmbeddedComponents;
    }

    /**
     * @return true if the core components need to be added before the startup of Embedded.
     */
    public boolean addEmbeddedComponents() {
        return this.addEmbeddedComponents;
    }

    /**
     * @return true if EasyBeans will start the JMX connector.
     */
    public boolean isStartJMXConnector() {
        return this.startJMXConnector;
    }

    /**
     * Enable or disable the JMX connector.
     * @param startJMXConnector true/false.
     */
    public void setStartJMXConnector(final boolean startJMXConnector) {
        this.startJMXConnector = startJMXConnector;
    }

    /**
     * @return true if Deployer needs to be registered.
     */
    public boolean isRegisterDeployerMBean() {
        return this.registerDeployerMBean;
    }

    /**
     * Enable or disable the Deployer MBean.
     * @param registerDeployerMBean true/false.
     */
    public void setRegisterDeployerMBean(final boolean registerDeployerMBean) {
        this.registerDeployerMBean = registerDeployerMBean;
    }

    /**
     * @return true if Deployer needs to be registered.
     */
    public boolean isRegisterJ2EEServerMBean() {
        return this.registerJ2EEServerMBean;
    }

    /**
     * Enable or disable the J2EEServer MBean.
     * @param registerJ2EEServerMBean true/false.
     */
    public void setRegisterJ2EEServerMBean(final boolean registerJ2EEServerMBean) {
        this.registerJ2EEServerMBean = registerJ2EEServerMBean;
    }

    /**
     * @return true if components are managed by EasyBeans and not externally.
     */
    public boolean isAutoConfigureComponents() {
        return this.autoConfigureComponents;
    }

    /**
     * Sets the flag for managing components in EasyBeans.
     * @param autoConfigureComponents if true, managed by EasyBeans
     */
    public void setAutoConfigureComponents(final boolean autoConfigureComponents) {
        this.autoConfigureComponents = autoConfigureComponents;
    }

    /**
     * @return a description of the embedded server
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Sets a description of the embedded server.
     * @param description a description of the embedded server
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Specify if the components should be stopped during shutdown sequence.
     * @param stopComponents true if components should be stopped during shutdown sequence
     */
    public void setStopComponentsDuringShutdown(final boolean stopComponents) {
        this.stopComponentsDuringShutdown = stopComponents;
    }

    /**
     * @return true if components should be stopped during shutdown sequence
     */
    public boolean isStopComponentsDuringShutdown() {
        return this.stopComponentsDuringShutdown;
    }

    /**
     * @return the list of the URLs that will be used to configure EasyBeans.
     */
    public LinkedList<URL> getConfigurationURLs() {
        return this.configurationURLs;
    }

    /**
     * Sets the list of the URLs used to configure EasyBeans.
     * @param configurationURLs the list of URLs
     */
    public void setConfigurationURLs(final LinkedList<URL> configurationURLs) {
        this.configurationURLs = configurationURLs;
    }

    /**
     * @return the configuration map.
     */
    public Map<String, Object> getConfigurationMap() {
        return this.configurationMap;
    }

    /**
     * Sets the configuration map.
     * @param configurationMap the given map
     */
    public void setConfigurationMap(final Map<String, Object> configurationMap) {
        this.configurationMap = configurationMap;
    }

    /**
     * Use or not the EasyBeans v1 legacy naming strategy.
     * @return true if this is the case.
     */
    public boolean isUsingLegacyNamingStrategy() {
        return this.useLegacyNamingStrategy;
    }

    /**
     * Sets the value for EasyBeans v1 legacy naming strategy.
     * @param useLegacyNamingStrategy the boolean value.
     */
    public void setUseLegacyNamingStrategy(final boolean useLegacyNamingStrategy) {
        this.useLegacyNamingStrategy = useLegacyNamingStrategy;
    }


}
