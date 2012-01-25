/**
 * EasyBeans
 * Copyright (C) 2006-2012 Bull S.A.S.
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
 * $Id: Activator.java 6142 2012-01-25 14:11:48Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.agent;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Dictionary;
import java.util.List;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;
import org.ow2.easybeans.osgi.configuration.ComponentConfiguration;
import org.ow2.easybeans.osgi.configuration.XMLConfigurationExtractor;
import org.ow2.util.execution.ExecutionResult;
import org.ow2.util.execution.IExecution;
import org.ow2.util.execution.IRunner;
import org.ow2.util.execution.helper.RunnableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This Agent Activator ease the start/stop process for EasyBeans.
 *
 * @author Guillaume Sauthier
 */
public class Activator implements BundleActivator {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(Activator.class);

    private static final String EASYBEANS_CORE_CONFIG_FILE = "org/ow2/easybeans/server/easybeans-core.xml";
    private static final String EASYBEANS_DEFAULT_CONFIG_FILE = "org/ow2/easybeans/server/easybeans-default.xml";
    private static final String EASYBEANS_USER_CONFIG_FILE = "easybeans.xml";
    private static final String EASYBEANS_USER_CONFIG_FILE_DIR = "org.ow2.easybeans.osgi.conf.dir";

    /**
     * List of bundles to be started by the agent.
     * WARNING: Sorting matters !
     */
    private static final String[] BUNDLE_SYMBOLIC_NAMES = {
            "org.ow2.bundles.ow2-util-event-impl",
            "org.ow2.bundles.ow2-util-jmx-impl",
            "org.ow2.easybeans.component.carol",
            "org.ow2.easybeans.component.quartz",
            "org.ow2.easybeans.component.jotm",
            "org.ow2.easybeans.component.jca.workmanager",
            "org.ow2.easybeans.component.joram",
            "org.ow2.easybeans.component.event",
            "org.ow2.easybeans.component.jmx",
            "org.ow2.easybeans.component.statistic",
            "org.ow2.easybeans.component.hsqldb",
            "org.ow2.easybeans.component.jdbcpool",
            "org.ow2.easybeans.core" };

    // private LogService logService = null;

    /**
     * Starts the EasyBeans bundle in the right order.
     * {@inheritDoc}
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @SuppressWarnings("unchecked")
    public void start(final BundleContext bc) throws Exception {

        // Lookup the ConfigAdmin service
        ServiceReference configAdminReference = bc.getServiceReference(ConfigurationAdmin.class.getName());

        bc.getServiceReference(LogReaderService.class.getName());

        /*if (logServiceReference != null){
            // TODO check for a easybeans osgi property that turns log on/off
            LogReaderService logReaderService = (LogReaderService) bc.getService(logServiceReference);
            logReaderService.addLogListener(new SimpleLogListenerImpl());
        }*/

        // If no config admin service is available log and exit
        if (configAdminReference == null) {
            throw new IllegalStateException("The OSGi ConfigAdmin Service is not available. EasyBeans cannot start!");
        }

        // Get the ConfigAdmin service
        ConfigurationAdmin configAdminService = (ConfigurationAdmin) bc.getService(configAdminReference);

        // Use the XMLConfiguratorExtractor ClassLoader for resource search.
        ClassLoader newClassLoader = XMLConfigurationExtractor.class.getClassLoader();
        URL xmlConfigurationURL = null;
        InputStream xmlConfigurationStream = null;

        // Try to load EasyBeans configuration from ConfigAdmin
        Configuration[] cores = configAdminService.listConfigurations("(service.pid=org.ow2.easybeans.configuration)");
        if (cores != null && (cores.length != 0)) {
            // Got configuration, use the first one
            Configuration coreConfig = cores[0];
            String xml = (String) coreConfig.getProperties().get("configuration.content");

            // If such a property exists, wrap it in an InputStream for future usage
            if (xml != null) {
                xmlConfigurationStream = new ByteArrayInputStream(xml.getBytes());
            }
        }

        // Fallback resource search if we didn't find a Configuration
        if (xmlConfigurationStream == null) {
            // Load resources from ClassLoader

            // User specified configuration Location
            String location = bc.getProperty(EASYBEANS_USER_CONFIG_FILE_DIR) + File.separator + EASYBEANS_USER_CONFIG_FILE;

            File file = new File(location);

            // See if the specified file exists
            if (!file.exists()) {
                xmlConfigurationURL = newClassLoader.getResource(EASYBEANS_DEFAULT_CONFIG_FILE);
                logger.warn("Cannot find User configuration (easybeans.xml) for Easybeans. Using default configuration provided by ''{0}''", xmlConfigurationURL);
            } else {
                logger.info("Using Easybeans Configuration file ''{0}''", file);
                xmlConfigurationURL = file.toURL();
            }

            if (xmlConfigurationURL == null) {
                // Should not happen
                throw new IllegalStateException(
                        "Both EasyBeans user and default configuration files are missing or not accessible. Easybeans cannot start!");
            }
        }

        // Always load default configuration (basis)
        URL coreConfigurationURL = newClassLoader.getResource(EASYBEANS_CORE_CONFIG_FILE);
        if (coreConfigurationURL == null) {
            throw new IllegalStateException(
                    "EasyBeans core configuration file is missing or not accessible. Easybeans cannot start!");
        }


        IRunner<Collection<ComponentConfiguration>> runner = new RunnableHelper<Collection<ComponentConfiguration>>();
        ExecutionResult<Collection<ComponentConfiguration>> result = null;

        // Final parameters
        final URL core = coreConfigurationURL;
        final URL extension = xmlConfigurationURL;
        final InputStream extensionStream = xmlConfigurationStream;

        // Executing under a selected TCCL
        result = runner.execute(newClassLoader, new IExecution<Collection<ComponentConfiguration>>() {

            public Collection<ComponentConfiguration> execute() throws Exception {
                // Extract configurations for the core components
                XMLConfigurationExtractor coreConfigurationExtractor = new XMLConfigurationExtractor(core);
                Collection<ComponentConfiguration> allComponentsConfigurations = coreConfigurationExtractor
                        .getComponentConfigurations();

                // Extract configurations for other components
                XMLConfigurationExtractor otherConfigurationExtractor = null;
                // Use Stream if it was defined
                if (extensionStream != null) {
                    otherConfigurationExtractor = new XMLConfigurationExtractor(extensionStream);
                } else {
                    otherConfigurationExtractor = new XMLConfigurationExtractor(extension);
                }
                allComponentsConfigurations.addAll(otherConfigurationExtractor.getComponentConfigurations());
                return allComponentsConfigurations;
            }

        });

        // If there was a failure, re-throw the Exception
        if (result.getException() != null) {
            throw result.getException();
        }

        Collection<ComponentConfiguration> componentConfigurations = result.getResult();


        List<String> componentInstancesRequired = new ArrayList<String>();

        // Now, compute all factory PID (component classname) that we needs
        for (ComponentConfiguration componentConfiguration : componentConfigurations) {
            componentInstancesRequired.add(componentConfiguration.getComponentSymbolicName());
        }


        // Track the component being registered.
        EasyBeansComponentTracker tracker = new EasyBeansComponentTracker(bc, componentInstancesRequired);
        tracker.open();

        // First, delete all the existing configuration of all bundles
        // Because the configuration is found through the EasyBeans XML file
        for (ComponentConfiguration componentConfiguration : componentConfigurations) {
            String thisComponentFactoryPID = componentConfiguration.getComponentSymbolicName();

            // Delete the configuration
            Configuration[] existing = configAdminService.listConfigurations("(service.factoryPid=" + thisComponentFactoryPID
                    + ")");
            if (existing != null) {
                for (Configuration c : existing) {
                    logger.debug("Deleting existing configuration for pid={0}: {1}", thisComponentFactoryPID, c);
                    c.delete();
                }
            }
        }

        // Now, create the configuration
        // Iterates on the created ComponentConfigurations
        for (ComponentConfiguration componentConfiguration : result.getResult()) {
            String thisComponentFactoryPID = componentConfiguration.getComponentSymbolicName();

            // Create new Configuration.
            Configuration factoryConfiguration = configAdminService.createFactoryConfiguration(thisComponentFactoryPID, null);

            Dictionary properties = componentConfiguration.getConfigurationAsDictionary();
            factoryConfiguration.update(properties);
            logger.debug("Updated Service Factory Configuration with pid {0}", thisComponentFactoryPID);
        }

        Bundle[] bundles = bc.getBundles();

        // Start Bundles in the array order
        for (int i = 0; i < BUNDLE_SYMBOLIC_NAMES.length; i++) {

            // 1. Start the bundle with the right symbolic name
            Bundle bundle = null;
            for (int j = 0; j < bundles.length; j++) {
                bundle = bundles[j];
                if (BUNDLE_SYMBOLIC_NAMES[i].equals(bundle.getSymbolicName())) {

                    // First start the bundle
                    if (bundle.getState() == Bundle.RESOLVED || bundle.getState() == Bundle.INSTALLED) {
                        logger.debug("Starting Bundle {0}", bundle);
                        bundle.start();
                    }
                }
            }
        }

    }

    /**
     * No-one still use that method !!
     */
    public Collection<ComponentConfiguration> getComponentConfigurationForSymbolicName(final String symbolicName,
            final Collection<ComponentConfiguration> componentConfigurations) {
        Collection<ComponentConfiguration> thisComponentConfigurations = new ArrayList<ComponentConfiguration>();
        for (ComponentConfiguration componentConfiguration : componentConfigurations) {
            if (componentConfiguration.getComponentSymbolicName().startsWith(symbolicName)) {
                thisComponentConfigurations.add(componentConfiguration);
            }
        }
        return thisComponentConfigurations;
    }

    /**
     * Stops the EasyBeans bundle in the right order. {@inheritDoc}
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(final BundleContext bc) throws Exception {

        Bundle[] bundles = bc.getBundles();

        // Traverse the array starting by the end
        for (int i = BUNDLE_SYMBOLIC_NAMES.length - 1; i >= 0; i--) {
            Bundle bundle = null;
            for (int j = 0; j < bundles.length && bundle == null; j++) {
                if (BUNDLE_SYMBOLIC_NAMES[i].equals(bundles[j].getSymbolicName())) {
                    bundle = bundles[j];

                    // TODO Should use standard LogService
                    logger.info("Stopping Bundle {0}", bundle);
                    try {
                        bundle.stop();
                    } catch (Exception e) {
                        logger.error("Cannot stop the bundle {0}", bundle, e);
                    }
                }

            }
        }
    }

    static class SimpleLogListenerImpl implements LogListener {

        /*
         * (non-Javadoc)
         *
         * @see org.osgi.service.log.LogListener#logged(org.osgi.service.log.LogEntry)
         */
        public void logged(final LogEntry entry) {
            String logLevel = null;

            switch (entry.getLevel()) {
            case LogService.LOG_DEBUG:
                logLevel = "DEBUG";
                break;

            case LogService.LOG_INFO:
                logLevel = "INFO";
                break;
            case LogService.LOG_WARNING:
                logLevel = "WARNING";
                break;
            case LogService.LOG_ERROR:
                logLevel = "ERROR";
                break;
            default:
                logLevel = "INFO";
            }

            DateFormat dateFormat = DateFormat.getTimeInstance();
            System.out.println(logLevel
                    + " - "
                    + dateFormat.format(new Date(entry.getTime()))
                    + " ["
                    + (entry.getServiceReference() != null ? "S: "
                            + entry.getServiceReference().getProperty(Constants.SERVICE_PID) : "B: "
                            + entry.getBundle().getSymbolicName()) + "] - " + entry.getMessage() + ".");
            if (entry.getException() != null) {
                System.out.println(" Exception: " + entry.getException().getMessage());
                entry.getException().printStackTrace(System.out);
            }
        }
    }

}
