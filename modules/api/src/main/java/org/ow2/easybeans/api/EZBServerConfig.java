/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EZBServerConfig.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import java.io.File;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Defines the EasyBeans configuration.
 * @author Florent Benoit
 */
public interface EZBServerConfig {

    /**
     * @return the list of path for loading/looking Java EE modules.
     */
    List<File> getDeployDirectories();

    /**
     * Sets the list of path for loading/looking Java EE modules.
     * @param deployDirectories the list of path for loading/looking Java EE modules.
     */
    void setDeployDirectories(final List<File> deployDirectories);

    /**
     * Add a directory to the list of path for loading/looking Java EE modules.
     * @param deployDirectory a path for loading/looking Java EE modules.
     */
    void addDeployDirectory(final File deployDirectory);


    /**
     * @return true if the server should wait when starting embedded server.
     */
    boolean shouldWait();

    /**
     * Sets if the server will loop at the end of it's startup.
     * @param shouldWait true/false
     */
    void setShouldWait(final boolean shouldWait);


    /**
     * Use or not the MBeans.
     * @return true if this is the case.
     */
    boolean isUsingMBeans();

    /**
     * Sets the value for using MBeans.
     * @param useMBeans the boolean value.
     */
    void setUseMBeans(final boolean useMBeans);

    /**
     * Use or not the EasyBeans naming system.
     * @return true if this is the case.
     */
    boolean isUsingNaming();

    /**
     * Sets the value for using the EasyBeans naming system.
     * @param useNaming the boolean value.
     */
    void setUseNaming(final boolean useNaming);

    /**
     * Adds an {@link EZBConfigurationExtension} in the Facory list.
     * @param extension the factory FQN.
     */
    void addExtensionFactory(final EZBConfigurationExtension extension);

    /**
     * @return Returns the list of {@link EZBConfigurationExtension}.
     */
    List<EZBConfigurationExtension> getExtensionFactories();

    /**
     * @return the directoryScanningEnabled
     */
    boolean isDirectoryScanningEnabled();

    /**
     * @param directoryScanningEnabled the directoryScanningEnabled to set
     */
    void setDirectoryScanningEnabled(final boolean directoryScanningEnabled);


    /**
     * Init or not JACC at startup.
     * @param initJACC initialization of JACC provider.
     */
    void setInitJACC(final boolean initJACC);

    /**
     * @return true if JACC provider needs to be initialized at startup.
     */
    boolean initJACC();

    /**
     * Sets the flag for adding before the startup the core components.
     * @param addEmbeddedComponents the boolean value
     */
    void setAddEmbeddedComponents(final boolean addEmbeddedComponents);

    /**
     * @return true if the core components need to be added before the startup of Embedded.
     */
    boolean addEmbeddedComponents();

    /**
     * @return true if EasyBeans will start the JMX connector.
     */
    boolean isStartJMXConnector();

    /**
     * Enable or disable the JMX connector.
     * @param startJMXConnector true/false.
     */
    void setStartJMXConnector(final boolean startJMXConnector);

    /**
     * @return true if Deployer needs to be registered.
     */
    boolean isRegisterDeployerMBean();

    /**
     * Enable or disable the Deployer MBean.
     * @param registerDeployerMBean true/false.
     */
    void setRegisterDeployerMBean(final boolean registerDeployerMBean);

    /**
     * @return true if Deployer needs to be registered.
     */
    boolean isRegisterJ2EEServerMBean();

    /**
     * Enable or disable the J2EEServer MBean.
     * @param registerJ2EEServerMBean true/false.
     */
    void setRegisterJ2EEServerMBean(final boolean registerJ2EEServerMBean);

    /**
     * @return true if components are managed by EasyBeans and not externally.
     */
    boolean isAutoConfigureComponents();

    /**
     * Sets the flag for managing components in EasyBeans.
     * @param autoConfigureComponents if true, managed by EasyBeans
     */
    void setAutoConfigureComponents(final boolean autoConfigureComponents);

    /**
     * @return a description of the embedded server
     */
    String getDescription();

    /**
     * Sets a description of the embedded server.
     * @param description a description of the embedded server
     */
    void setDescription(final String description);

    /**
     * Specify if the components should be stopped during shutdown sequence.
     * @param stopComponents true if components should be stopped during shutdown sequence
     */
    void setStopComponentsDuringShutdown(final boolean stopComponents);

    /**
     * @return true if components should be stopped during shutdown sequence
     */
    boolean isStopComponentsDuringShutdown();

    /**
     * @return the list of the URLs that will be used to configure EasyBeans.
     */
    LinkedList<URL> getConfigurationURLs();

    /**
     * Sets the list of the URLs used to configure EasyBeans.
     * @param configurationURLs the list of URLs
     */
    void setConfigurationURLs(final LinkedList<URL> configurationURLs);

    /**
     * @return the configuration map.
     */
    Map<String, Object> getConfigurationMap();

    /**
     * Sets the configuration map.
     * @param configurationMap the given map
     */
    void setConfigurationMap(final Map<String, Object> configurationMap);

}
