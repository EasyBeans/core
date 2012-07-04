/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: Embedded.java 5748 2011-02-28 17:13:13Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.server;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.ow2.easybeans.api.EZBConfigurationExtension;
import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EZBServerConfig;
import org.ow2.easybeans.api.EasyBeansInterceptor;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.api.pool.EZBManagementPool;
import org.ow2.easybeans.component.ComponentManager;
import org.ow2.easybeans.component.Components;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBDepMonitorComponent;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBStatisticComponent;
import org.ow2.easybeans.component.itf.RegistryComponent;
import org.ow2.easybeans.container.JContainer3;
import org.ow2.easybeans.container.JContainerConfig;
import org.ow2.easybeans.deployer.EasyBeansDeployer;
import org.ow2.easybeans.deployer.IRemoteDeployer;
import org.ow2.easybeans.deployer.RemoteDeployer;
import org.ow2.easybeans.deployment.EasyBeansDeployableInfo;
import org.ow2.easybeans.deployment.api.EZBDeployableInfo;
import org.ow2.easybeans.deployment.helper.listener.EnvEntriesExtensionListener;
import org.ow2.easybeans.deployment.helper.listener.JavaCompExtensionListener;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStarted;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStarting;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStopped;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStopping;
import org.ow2.easybeans.jmx.CommonsModelerException;
import org.ow2.easybeans.jmx.CommonsModelerHelper;
import org.ow2.easybeans.jmx.JMXRemoteException;
import org.ow2.easybeans.jmx.JMXRemoteHelper;
import org.ow2.easybeans.jmx.MBeanServerException;
import org.ow2.easybeans.jmx.MBeanServerHelper;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.resolver.ServerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBServerJNDIResolver;
import org.ow2.easybeans.rpc.api.RMIServerRPC;
import org.ow2.easybeans.rpc.rmi.server.RMIServerRPCImpl;
import org.ow2.easybeans.security.jacc.PolicyProvider;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.ee.deploy.api.deployer.IDeployerManager;
import org.ow2.util.ee.deploy.impl.deployer.DeployerManager;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.event.api.IEventListener;
import org.ow2.util.event.impl.EventDispatcher;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;


/**
 * Allows to create an embedded EJB3 server.
 * @author Florent Benoit
 */
public class Embedded implements EZBServer {

    /**
     * Default sleep value (for server loop).
     */
    private static final int SLEEP_VALUE = 10000;

    /**
     * Core XML file (that will load Quartz component, etc).
     */
    public static final String CORE_XML_FILE = "org/ow2/easybeans/server/easybeans-core.xml";

    /**
     * Default deployment directory.
     */
    public static final String DEFAULT_DEPLOY_DIRECTORY = "easybeans-deploy";

    /**
     * Deprecated default deployment directory.
     */
    public static final String DEPRECATED_DEFAULT_DEPLOY_DIRECTORY = "ejb3s";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(Embedded.class);

    /**
     * Internal (global) counter of all embedded instance created.
     */
    private static int counter = 0;

    /**
     * ID of this embedded server.
     */
    private Integer id = null;

    /**
     * Configuration of this server.
     */
    private EZBServerConfig serverConfig = null;

    /**
     * Map of managed ejb3 containers.
     */
    private Map<String, EZBContainer> containers = null;

    /**
     * Deployer instance.
     */
    private RemoteDeployer deployer = null;

    /**
     * List of components that have been defined.
     */
    private Components components = null;

    /**
     * Manager of components.
     */
    private ComponentManager componentManager = null;

    /**
     * Management pool used to managed the pools (creating instance, etc.).
     */
    private EZBManagementPool managementThreadPool;

    /**
     * Server started ?
     */
    private boolean started = false;

    /**
     * Server stopped ?
     */
    private boolean stopped = false;

    /**
     * Server stopping ?
     */
    private boolean stopping = false;

    /**
     * RPC Invoker object.
     */
    private RMIServerRPC invoker = null;

    /**
     * JNDI Resolver.
     */
    private EZBServerJNDIResolver jndiResolver = null;

    /**
     * Deployer manager.
     */
    private IDeployerManager deployerManager = null;

    /**
     * The event dispatcher.
     */
    private IEventDispatcher dispatcher = null;

    /**
     * The provider id.
     */
    private String j2eeManagedObjectId = null;

    /**
     * The list of default naming extensions used to fill the
     * <code>java:comp</code> Context of the beans.
     */
    private List<IEventListener> defaultNamingExtensions;

    /**
     * Components are initialized only once (else it's added after each start/stop).
     */
    private boolean componentsInitialized = false;

    /**
     * Components are registered only once (else it's added after each start/stop).
     */
    private boolean componentsRegistered = false;

    /**
     * List of global interceptors classes to use.
     * @return list of classes
     */
     private List<Class<? extends EasyBeansInterceptor>> globalInterceptorsClasses = null;

    private InitialContext context = null;

    /**
     * Creates a new Embedded server.<br>
     * It will take default values of configuration.
     */
    public Embedded() {
        this.id = Integer.valueOf(counter++);
        this.containers = new ConcurrentHashMap<String, EZBContainer>();
        this.serverConfig = new ServerConfig();
        this.globalInterceptorsClasses = new ArrayList<Class<? extends EasyBeansInterceptor>>();

        // Provide the defaults Naming Extensions
        this.defaultNamingExtensions = new ArrayList<IEventListener>();
        this.defaultNamingExtensions.add(new JavaCompExtensionListener());
        this.defaultNamingExtensions.add(new EnvEntriesExtensionListener());

        // Create Server JNDI resolver
        this.jndiResolver = new ServerJNDIResolver();

        // register to the list
        EmbeddedManager.addEmbedded(this);

        // Init components
        this.components = new Components();

        // Create component manager around components
        this.componentManager = new ComponentManager(this.components);

        this.j2eeManagedObjectId = J2EEManagedObjectNamingHelper.getJ2EEManagedObjectId(this);
    }

    /**
     * Starts the EJB3 server.
     * @throws EmbeddedException if there is a failure while starting the
     *         server.
     */
    public void start() throws EmbeddedException {
        this.stopping = false;

        long tStart = System.currentTimeMillis();

        // Deployer manager ?
        if (this.deployerManager == null) {
            this.deployerManager = new DeployerManager();
            EasyBeansDeployer easyBeansDeployer = new EasyBeansDeployer();
            easyBeansDeployer.setEmbedded(this);
            this.deployerManager.register(easyBeansDeployer);
        }

        // Add default components like Quartz Timer service, etc.
        if (!this.componentsInitialized) {
            if (this.serverConfig.addEmbeddedComponents()) {
                // Add the core configuration first
                this.serverConfig.getConfigurationURLs().addFirst(
                        Thread.currentThread().getContextClassLoader().getResource(CORE_XML_FILE));
            }

            // Configure the given embedded instance (= this)
            try {
                if (!this.serverConfig.getConfigurationURLs().isEmpty()) {
                    logger.info("Configuring EasyBeans with the configuration URLs ''{0}''", this.serverConfig
                            .getConfigurationURLs());
                }
                EmbeddedConfigurator.init(this, this.serverConfig.getConfigurationURLs(), this.serverConfig
                        .getConfigurationMap());
            } catch (EmbeddedException e) {
                throw new EmbeddedException("Cannot configure the embedded server", e);
            }
            this.componentsInitialized = true;
        }

        // Init JACC
        if (this.serverConfig.initJACC()) {
            PolicyProvider.init();
        }

        // configure
        configureDeploy();

        if (this.serverConfig.isUsingNaming()) {
            // url.pkg for java:
            System.setProperty(Context.URL_PKG_PREFIXES, "org.ow2.easybeans.naming.pkg");
        }

        // set the deployer used for this component.
        try {
            this.deployer = new RemoteDeployer(this);
        } catch (DeployerException e) {
            throw new EmbeddedException("Cannot build a remote deployer.", e);
        }

        MBeansHelper.getInstance().activate(this.serverConfig.isUsingMBeans());
        if (this.serverConfig.isUsingMBeans()) {

            // init Modeler Registry
            try {
                CommonsModelerHelper.initRegistry();
            } catch (CommonsModelerException e) {
                throw new EmbeddedException("Cannot init MBean server", e);
            }

            // Start MBeanServer (if any)
            try {
                MBeanServerHelper.startMBeanServer();
            } catch (MBeanServerException e) {
                throw new EmbeddedException("Cannot start MBean server", e);
            }
        }

        if (this.serverConfig.isAutoConfigureComponents()) {
            // Init components
            try {
                this.componentManager.initComponents(!this.componentsRegistered);
            } catch (EZBComponentException e) {
                throw new EmbeddedException("Cannot init components", e);
            }
            this.componentsRegistered = true;

            // Start components
            try {
                this.componentManager.startComponents();
            } catch (EZBComponentException e) {
                throw new EmbeddedException("Cannot start components", e);
            }
        }

        //Create the event dispatcher
        this.dispatcher = new EventDispatcher();
        this.dispatcher.start();

        // Register to event component.
        EZBEventComponent eventComponent = getComponent(EZBEventComponent.class);
        if (eventComponent != null) {
            eventComponent.registerJ2EEManagedObject(this, this.dispatcher);
        }

        // Register to jmx component will be done by the mbean itself.

        // Register to statistic component.
        EZBStatisticComponent statisticComponent = getComponent(EZBStatisticComponent.class);
        if (statisticComponent != null) {
            statisticComponent.registerJ2EEManagedObject(this);
        }

        // Register some NamingExtension (used to fill the java:comp Context)
        if (eventComponent != null) {
            for (IEventListener extension : this.defaultNamingExtensions) {
                eventComponent.getEventService().registerListener(extension,
                                                                  NAMING_EXTENSION_POINT);
            }
        }

        // Dispatch life cycle event.
        this.dispatcher.dispatch(new EventLifeCycleStarting(this.j2eeManagedObjectId));

        if (this.serverConfig.isUsingMBeans()) {
            if (this.serverConfig.isStartJMXConnector()) {
                try {
                    JMXRemoteHelper
                            .startConnector((RegistryComponent) getComponent("org.ow2.easybeans.component.carol.CarolComponent"));
                } catch (JMXRemoteException e) {
                    throw new EmbeddedException("Cannot start JMX Remote connector", e);
                }
            }

            // register the Deployer
            if (this.serverConfig.isRegisterDeployerMBean()) {
                try {
                    MBeansHelper.getInstance().registerMBean(this.deployer);
                } catch (MBeansException e) {
                    throw new EmbeddedException("Cannot init MBeans", e);
                }
            }

            // register the Server
            if (this.serverConfig.isRegisterJ2EEServerMBean()) {
                try {
                    MBeansHelper.getInstance().registerMBean(this);
                } catch (MBeansException e) {
                    throw new EmbeddedException("Cannot init MBeans", e);
                }
            }
        }

        // Bind the RPC object
        try {
            this.invoker = new RMIServerRPCImpl(this);
        } catch (RemoteException e) {
            throw new EmbeddedException("Cannot build RPC invoker", e);
        }

        try {
            this.context = new InitialContext();
        } catch (NamingException e) {
            throw new EmbeddedException("Cannot make initial context", e);
        }

        try {
            this.context.rebind(RMIServerRPC.RPC_JNDI_NAME, this.invoker);
        } catch (NamingException e) {
            throw new EmbeddedException("Cannot bind the RPC invoker", e);
        }

        logger.info("Embedded.start.startup", Version.getVersion(), Long.valueOf(System.currentTimeMillis() - tStart));
        logger.debug("Embedded.start.created", Integer.valueOf(this.containers.size()));

        this.dispatcher.dispatch(new EventLifeCycleStarted(this.j2eeManagedObjectId));

        // It is started
        this.started = true;

        EZBDepMonitorComponent depMonitorComponent = getComponent(EZBDepMonitorComponent.class);
        if (depMonitorComponent != null) {
            try {
                depMonitorComponent.enable();
            } catch (EZBComponentException e) {
                logger.error("Cannot enable callback on depmonitor component", e);
            }
        }


        if (this.serverConfig.shouldWait()) {
            logger.info("Embedded.start.waiting");
            while (this.started) {
                try {
                    Thread.sleep(SLEEP_VALUE);
                } catch (InterruptedException e) {
                    logger.error("Cannot sleep in the thread", e);
                }
            }
        }
    }

    /**
     * Stops the EJB3 server.
     * @throws EmbeddedException if container cannot be stopped.
     */
    public synchronized void stop() throws EmbeddedException {
        // ensure started
        if (!this.started) {
            throw new EmbeddedException("Cannot stop the server as it is not started.");
        }
        this.stopping = true;

        if (this.dispatcher != null) {
            this.dispatcher.dispatch(new EventLifeCycleStopping(this.j2eeManagedObjectId));
        }

        // Stop the containers
        // Use a ListIterator to allow us to safely remove EZBContainers
        // from the List being processed.
        List<EZBContainer> containersList = new ArrayList<EZBContainer>(this.containers.values());
        ListIterator<EZBContainer> li = containersList.listIterator();
        while (li.hasNext()) {
            EZBContainer container = li.next();
            container.stop();
            removeContainer(container);
        }

        // Unregister MBeans
        if (this.serverConfig.isUsingMBeans()) {

            if (this.serverConfig.isStartJMXConnector()) {
                // Stop the JMX Connector
                try {
                    JMXRemoteHelper.stopConnector();
                } catch (JMXRemoteException e) {
                    // Only log the Exception
                    logger.debug("Cannot stop JMX Remote connector", e);
                    // and continue ...
                }
            }

            // Unregister the Deployer
            if (this.serverConfig.isRegisterDeployerMBean()) {
                try {
                    MBeansHelper.getInstance().unregisterMBean(this.deployer);
                } catch (MBeansException e) {
                    // Only log the Exception
                    logger.error("Cannot unregister Deployer MBean", e);
                }
            }

            // Unregister the Server
            if (this.serverConfig.isRegisterJ2EEServerMBean()) {
                try {
                    MBeansHelper.getInstance().unregisterMBean(this);
                } catch (MBeansException e) {
                    // Only log the Exception
                    logger.error("Cannot unregister Embedded MBean", e);
                }
            }
        }

        // Unbind the RPCInvoker Remote Object
        try {
            new InitialContext().unbind(RMIServerRPC.RPC_JNDI_NAME);
        } catch (NamingException e) {
            // Only log the Exception
            logger.error("Cannot unbind the RPC invoker", e);
        }

        // Unexport
        try {
            PortableRemoteObject.unexportObject(this.invoker);
        } catch (NoSuchObjectException e) {
            // Only log the Exception
            logger.error("Cannot unexport RPC invoker", e);
        }

        // Dispatch lifecycle event.
        this.dispatcher.dispatch(new EventLifeCycleStopped(this.j2eeManagedObjectId));

        // Unregister from statistic component.
        EZBStatisticComponent statisticComponent = getComponent(EZBStatisticComponent.class);
        if (statisticComponent != null) {
            statisticComponent.unregisterJ2EEManagedObject(this);
        }

        // Unregister from jmx component will be done by the mbean itself.

        // Unregister from event component.
        EZBEventComponent eventComponent = getComponent(EZBEventComponent.class);
        if (eventComponent != null) {
            eventComponent.unregisterJ2EEManagedObject(this);

            // Unregister the NamingExtensions (used to fill the java:comp Context)
            for (IEventListener extension : this.defaultNamingExtensions) {
                eventComponent.getEventService().unregisterListener(extension);
            }

        }

        // Destroy the event dispatcher.
        this.dispatcher.stop();
        this.dispatcher = null;

        // Stop the components
        if (this.serverConfig.isStopComponentsDuringShutdown()) {
            this.componentManager.stopComponents();
        }

        // Stop JACC
        if (this.serverConfig.initJACC()) {
            try {
                PolicyProvider.stop();
            } catch (Exception e) {
                // Only log the Exception
                logger.error("Unable to stop the JACC provider", e);
            }
        }

        logger.info("Embedded.stop.stopped", Version.getVersion());

        // EasyBeans is stopped
        this.started = false;
        this.stopped = true;
        this.stopping = false;
    }

    /**
     * Sets the server configuration (not the components).
     * @param serverConfig the given configuration
     */
    public void setServerConfig(final ServerConfig serverConfig) {
        // check status
        if (this.started) {
            logger.debug("Cannot set the server configuration when server has been started.");
            return;
        }
        this.serverConfig = serverConfig;
    }

    /**
     * Gets a container managed by this server.
     * @param id the container id.
     * @return the container if it is found, else null.
     */
    public EZBContainer getContainer(final String id) {
        return this.containers.get(id);
    }

    /**
     * Gets a container managed by this server.
     * @param archive the archive used by the given container.
     * @return the container if it is found, else null.
     */
    public EZBContainer findContainer(final IArchive archive) {
        // Invalid archive
        if (archive == null) {
            return null;
        }
        // Search a container for the given archive.
        for (EZBContainer container : this.containers.values()) {
            if (archive.equals(container.getArchive())) {
                return container;
            }
        }
        return null;
    }

    /**
     * Configure the server deployment by using the given configuration.
     * @throws EmbeddedException if deploy cannot be configured
     */
    protected void configureDeploy() throws EmbeddedException {

        // For backward compliance
        if (this.serverConfig.isDirectoryScanningEnabled()) {
            // Check if monitor component is here ?
            List<EZBDepMonitorComponent> depMonitorComponents = this.componentManager.getComponentRegistry().getComponents(
                    EZBDepMonitorComponent.class);
            if (depMonitorComponents == null || depMonitorComponents.size() == 0) {
                // Deprecated way of using monitors
                logger.warn("Directory monitoring is now managed by a component and "
                        + "shouldn''t be set through a server-config property." + " A <depmonitor> component needs to be added.");

            }
        }
    }

    /**
     * Creates and adds an ejb3 container to the managed container.
     * @param deployable the container deployable.
     * @return the created container.
     */
    public EZBContainer createContainer(final IDeployable<?> deployable) {
        EZBContainerConfig jConfig = null;

        // Existing configuration ?
        EZBDeployableInfo deployableInfo = (EZBDeployableInfo) deployable.getExtension(EasyBeansDeployableInfo.class);
        if (deployableInfo != null) {
            EZBContainerConfig readContainerConfig = deployableInfo.getContainerConfiguration();
            if (readContainerConfig != null) {
                jConfig = readContainerConfig;
            }
        }

        if (jConfig == null) {
            jConfig = new JContainerConfig(deployable);
        }
        jConfig.setEZBServer(this);
        EZBContainer container = new JContainer3(jConfig);
        addContainer(container);

        return container;
    }

    /**
     * Add an already created container.
     * @param container the EZBContainer to be added.
     */
    public void addContainer(final EZBContainer container) {
        // Add extensions
        callJContainerConfigExtensions(container.getConfiguration());

        String id = container.getId();
        this.containers.put(id, container);
    }

    /**
     * Remove a given container.
     * @param container the container to be removed.
     */
    public void removeContainer(final EZBContainer container) {
        this.containers.remove(container.getId());

        logger.info("Container ''{0}'' removed", container.getArchive().getName());
    }

    /**
     * Adapt the JContainerConfig for all
     * {@link EasyBeansConfigurationExtension}.
     * @param jcc the JContainerConfig to adapt.
     */
    private void callJContainerConfigExtensions(final EZBContainerConfig jcc) {
        for (EZBConfigurationExtension extension : this.serverConfig.getExtensionFactories()) {
            try {
                extension.configure(jcc);
            } catch (Throwable t) {
                // prevent malicious code to break everything ...
                logger.info("Failed to configure JContainerConfig with {0}", extension.getClass().getName());
            }
        }
    }

    /**
     * @return the configuration of this server.
     */
    public EZBServerConfig getServerConfig() {
        return this.serverConfig;
    }

    /**
     * @return the containers managed by this server.
     */
    public Map<String, EZBContainer> getContainers() {
        return this.containers;
    }

    /**
     * Gets the id of this embedded server.
     * @return the id of this server.
     */
    public Integer getID() {
        return this.id;
    }

    /**
     * Gets a description of the embedded server.
     * @return a description of the embedded server
     */
    public String getDescription() {
        return this.serverConfig.getDescription();
    }

    /**
     * @return the Remote deployer instance.
     */
    public IRemoteDeployer getDeployer() {
        return this.deployer;
    }

    /**
     * Gets the components that have been defined for this embedded server.
     * @return the components.
     */
    public Components getComponents() {
        return this.components;
    }

    /**
     * Sets the components that needs to be launched.
     * @param components the set of components.
     */
    public void setComponents(final Components components) {
        this.components = components;
    }

    /**
     * Gets component with the given name.
     * @param componentName the name of the component.
     * @return the component (if any)
     */
    public EZBComponent getComponent(final String componentName) {
        // ask registry if present.
        if (this.componentManager != null) {
            return this.componentManager.getComponent(componentName);
        }
        return null;
    }

    /**
     * Get a reference to the first component matching the interface.
     * @param <T> The interface type.
     * @param itf The interface class.
     * @return The component.
     */
    public <T extends EZBComponent> T getComponent(final Class<T> itf) {
        // ask registry if present.
        if (this.componentManager != null) {
            return this.componentManager.getComponent(itf);
        }
        return null;
    }

    /**
     * @return Returns the ComponentManager used by this instance.
     */
    public ComponentManager getComponentManager() {
        return this.componentManager;
    }

    /**
     * @return true if EasyBeans has been stopped.
     */
    public boolean isStopped() {
        return this.stopped;
    }

    /**
     * @return true if EasyBeans has been started.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * @return the JNDI Resolver of this server.
     */
    public EZBServerJNDIResolver getJNDIResolver() {
        return this.jndiResolver;
    }

    /**
     * Get the J2EE managed object id.
     * @return The J2EE managed object id.
     */
    public String getJ2EEManagedObjectId() {
        return this.j2eeManagedObjectId;
    }

    /**
     * Get the event Dispatcher.
     * @return The event Dispatcher.
     */
    public IEventDispatcher getEventDispatcher() {
        return this.dispatcher;
    }

    /**
     * @return the Deployer manager.
     */
    public IDeployerManager getDeployerManager() {
        return this.deployerManager;
    }

    /**
     * Allows to set the deployer manager.
     * @param deployerManager the Deployer manager.
     */
    public void setDeployerManager(final IDeployerManager deployerManager) {
        this.deployerManager = deployerManager;
    }

    /**
     * List of global interceptors classes to use.
     * @return list of classes
     */
    public List<Class<? extends EasyBeansInterceptor>> getGlobalInterceptorsClasses() {
        return this.globalInterceptorsClasses;
    }

    /**
     * @return the management pool used to managed the pools (creating instance, etc.)
     */
    public EZBManagementPool getManagementThreadPool() {
        return this.managementThreadPool;
    }

    /**
     * Sets the management thread pool.
     * @param managementThreadPool the management pool used to managed the pools (creating instance, etc.)
     */
    public void setManagementThreadPool(final EZBManagementPool managementThreadPool) {
        this.managementThreadPool = managementThreadPool;
    }

    /**
     * @return true if EasyBeans is being stopped.
     */
    public boolean isStopping() {
        return this.stopping;
    }

    /**
     * @return context
     */
    public Context getContext() {
        return this.context;
    }

}
