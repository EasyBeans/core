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
 * $Id: JContainer3.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;

import static org.ow2.easybeans.container.mdb.MDBMessageEndPointFactory.DEFAULT_ACTIVATION_SPEC_NAME;
import static org.ow2.util.marshalling.Serialization.loadObject;
import static org.ow2.util.marshalling.Serialization.storeObject;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.ejb.ScheduleExpression;
import javax.ejb.TimerConfig;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ResourceAdapter;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerCallbackInfo;
import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.api.EZBContainerLifeCycleCallback;
import org.ow2.easybeans.api.EZBPermissionManager;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.LifeCycleCallbackException;
import org.ow2.easybeans.api.PermissionManagerException;
import org.ow2.easybeans.api.audit.EZBAuditComponent;
import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IEJBJarInfo;
import org.ow2.easybeans.api.bean.info.ITimerInfo;
import org.ow2.easybeans.api.bean.info.IWebServiceInfo;
import org.ow2.easybeans.api.binding.BindingException;
import org.ow2.easybeans.api.binding.EZBBindingFactory;
import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.api.event.bean.EZBClusteredBeanEvent;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBStatisticComponent;
import org.ow2.easybeans.component.itf.JMSComponent;
import org.ow2.easybeans.container.info.ApplicationExceptionInfo;
import org.ow2.easybeans.container.info.BeanInfo;
import org.ow2.easybeans.container.info.BusinessMethodsInfoHelper;
import org.ow2.easybeans.container.info.EJBJarInfo;
import org.ow2.easybeans.container.info.MessageDrivenInfo;
import org.ow2.easybeans.container.info.MethodInfo;
import org.ow2.easybeans.container.info.SessionBeanInfo;
import org.ow2.easybeans.container.info.SessionSynchronizationInfoHelper;
import org.ow2.easybeans.container.info.TimerInfo;
import org.ow2.easybeans.container.info.security.SecurityInfoHelper;
import org.ow2.easybeans.container.info.ws.WebServiceInfo;
import org.ow2.easybeans.container.managedbean.ManagedBeanFactory;
import org.ow2.easybeans.container.mdb.MDBMessageEndPointFactory;
import org.ow2.easybeans.container.mdb.helper.MDBResourceAdapterHelper;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.easybeans.container.session.singleton.SingletonSessionFactory;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.container.session.stateless.StatelessSessionFactory;
import org.ow2.easybeans.deployment.Deployment;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.helper.JavaContextHelper;
import org.ow2.easybeans.deployment.helper.JavaContextHelperException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EJB;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansWebservices;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.MessageDrivenBean;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.WebserviceEndpoint;
import org.ow2.easybeans.deployment.resolver.JNDIResolverHelper;
import org.ow2.easybeans.enhancer.Enhancer;
import org.ow2.easybeans.enhancer.EnhancerException;
import org.ow2.easybeans.enhancer.interceptors.EasyBeansInvocationContextFactory;
import org.ow2.easybeans.enhancer.lib.ProxyClassEncoder;
import org.ow2.easybeans.event.bean.EventMessageDrivenInfo;
import org.ow2.easybeans.event.container.EventContainerStarted;
import org.ow2.easybeans.event.container.EventContainerStarting;
import org.ow2.easybeans.event.container.EventContainerStopped;
import org.ow2.easybeans.event.container.EventContainerStopping;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleClusteredBean;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStarted;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStarting;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStopped;
import org.ow2.easybeans.event.lifecycle.EventLifeCycleStopping;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.loader.EasyBeansClassLoader;
import org.ow2.easybeans.naming.BeanNamingInfoHelper;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.naming.JNDINamingInfoHelper;
import org.ow2.easybeans.naming.context.ContextImpl;
import org.ow2.easybeans.naming.strategy.EasyBeansV1NamingStrategy;
import org.ow2.easybeans.naming.strategy.JavaEE6NamingStrategy;
import org.ow2.easybeans.naming.strategy.ManagedBeanNamingStrategy;
import org.ow2.easybeans.persistence.EZBExtendedEntityManager;
import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.easybeans.persistence.EZBPersistenceXmlAnalyzer;
import org.ow2.easybeans.persistence.PersistenceXmlAnalyzerException;
import org.ow2.easybeans.persistence.basic.BasicPersistenceXmlAnalyzer;
import org.ow2.easybeans.proxy.binding.BindingManager;
import org.ow2.easybeans.proxy.reference.EJBHomeCallRef;
import org.ow2.easybeans.proxy.reference.EJBLocalHomeCallRef;
import org.ow2.easybeans.proxy.reference.LocalCallRef;
import org.ow2.easybeans.proxy.reference.ManagedBeanRef;
import org.ow2.easybeans.proxy.reference.NoInterfaceLocalCallRef;
import org.ow2.easybeans.proxy.reference.RemoteCallRef;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.security.permissions.PermissionManager;
import org.ow2.easybeans.server.Embedded;
import org.ow2.easybeans.transaction.JTransactionManager;
import org.ow2.easybeans.util.ExtensorSupport;
import org.ow2.easybeans.util.topological.NodeWrapper;
import org.ow2.easybeans.util.topological.TopologicalSort;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.metadata.common.api.struct.IJAnnotationSqlDataSourceDefinition;
import org.ow2.util.ee.metadata.ejbjar.api.exceptions.EJBJARMetadataException;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IApplicationException;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IDependsOn;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJEjbSchedule;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JActivationConfigProperty;
import org.ow2.util.ee.metadata.ws.api.struct.IWebServiceMarker;
import org.ow2.util.ee.metadata.ws.api.view.IWebservicesView;
import org.ow2.util.ee.metadata.ws.api.xml.struct.IPortComponent;
import org.ow2.util.ee.metadata.ws.api.xml.struct.IWebservices;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.event.impl.EventDispatcher;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.pool.api.Pool;
import org.ow2.util.scan.api.metadata.IClassMetadata;

/**
 * Defines an EJB3 container.
 *
 * @author Florent Benoit
 *         Contributors:
 *         S. Ali Tokmen (JNDI naming strategy)
 */
public class JContainer3 implements EZBContainer {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JContainer3.class);

    /**
     * Id of this container.
     */
    private String id = null;

    /**
     * Classloader use to manage this archive.
     */
    private ClassLoader classLoader = null;

    /**
     * Deployment for the managed archive.
     */
    private Deployment deployment = null;

    /**
     * Container available.
     */
    private boolean available = false;

    /**
     * Deployment stuff has been resolved ?
     */
    private boolean resolved = false;

    /**
     * Enhancement done ?
     */
    private boolean enhanced = false;

    /**
     * Map of managed ejb3 factories.
     */
    private Map<String, Factory<?, ?>> factories = null;

    /**
     * Persistence manager object which manages all persistence-unit associated
     * to this container.
     */
    private EZBPersistenceUnitManager persistenceUnitManager = null;

    /**
     * JContainer Configuration.
     */
    private EZBContainerConfig configuration = null;

    /**
     * PermissionManager for the security permissions.
     */
    private EZBPermissionManager permissionManager = null;

    /**
     * Info on an ejb-jar file.
     */
    private IEJBJarInfo ejbJarInfo = null;

    /**
     * List of all the Reference that have been bound.
     */
    private List<EZBRef> bindingReferences = null;

    /**
     * {@link org.ow2.easybeans.api.EZBExtensor} implementation.
     */
    private final ExtensorSupport extensor = new ExtensorSupport();

    /**
     * Map used for the enhancer.
     */
    private Map<String, Object> enhancerMap = null;

    /**
     * The event dispatcher.
     */
    private IEventDispatcher dispatcher = null;

    /**
     * The provider id.
     */
    private String j2eeManagedObjectId = null;

    /**
     * Persistence XML Analyzer.
     */
    private EZBPersistenceXmlAnalyzer persistenceXmlAnalyzer;


    /**
     * The key of the returned map is the name of the persistence unit.
     */
    private final InheritableThreadLocal<Map<String, EZBExtendedEntityManager>> currentExtendedPersistenceContexts
            = new InheritableThreadLocal<Map<String, EZBExtendedEntityManager>>();

    /**
     * Build a new container on the given archive.
     *
     * @param config The JContainer configuration storing the archive (jar file
     *               or exploded).
     */
    public JContainer3(final EZBContainerConfig config) {
        this();
        setContainerConfig(config);
        this.bindingReferences = new ArrayList<EZBRef>();
    }

    /**
     * Default constructor. Must be used in conjonction with setContainerConfig().
     */
    protected JContainer3() {

    }

    /**
     * Configure this JContainer. Must be called before start().
     *
     * @param config ContainerConfiguration instance.
     */
    protected void setContainerConfig(final EZBContainerConfig config) {
        if (this.available) {
            throw new IllegalStateException("Cannot change the EZBContainer configuration after start().");
        }
        this.configuration = config;
        this.id = String.valueOf(System.identityHashCode(this));
        this.deployment = new Deployment(this.configuration);
        this.factories = new HashMap<String, Factory<?, ?>>();
    }

    /**
     * Gets the id of this container.
     *
     * @return string id.
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Resolve the metadata and analyze deployment descriptors. May be called
     * before the start method. If not already called, it will be called inside
     * the start method.
     *
     * @throws EZBContainerException if resolve step has failed
     */
    @Override
    public void resolve() throws EZBContainerException {
        // Analyze files
        long tStart = System.currentTimeMillis();
        try {
            this.deployment.analyze(this.classLoader);
        } catch (EJBJARMetadataException e) {
            throw new EZBContainerException("Cannot analyze archive '" + getArchive().getName() + "'.", e);
        } catch (ResolverException e) {
            throw new EZBContainerException("Cannot resolve some annotations in the archive '" + getName()
                    + "'.", e);
        }

        // Naming strategies ?
        if (getConfiguration().getNamingStrategies().size() == 0) {
            // Add a default strategy
            getConfiguration().getNamingStrategies().add(new JavaEE6NamingStrategy());
        }
        if (getConfiguration().getEZBServer().getServerConfig().isUsingLegacyNamingStrategy()) {
            getConfiguration().getNamingStrategies().add(new EasyBeansV1NamingStrategy());
        }

        // Build JNDI resolver
        JNDIResolverHelper jndiResolver = new JNDIResolverHelper(this);

        // populate the JNDI Resolver
        jndiResolver.addDeployment(this.deployment);

        // Add the resolver in the map
        this.enhancerMap = new HashMap<String, Object>();
        this.enhancerMap.put(EZBContainerJNDIResolver.class.getName(), getConfiguration().getContainerJNDIResolver());

        // This is now resolved
        this.resolved = true;

        if (logger.isDebugEnabled()) {
            logger.debug("Analyze elapsed during : " + (System.currentTimeMillis() - tStart) + " ms");
        }

    }


    /**
     * Start this container.
     *
     * @throws EZBContainerException if starting fails.
     */
    @Override
    public void start() throws EZBContainerException {

        long tStart = System.currentTimeMillis();

        // Create the event dispatcher
        if (this.dispatcher == null) {
            this.dispatcher = new EventDispatcher();
        }
        this.dispatcher.start();

        this.j2eeManagedObjectId = J2EEManagedObjectNamingHelper.getJ2EEManagedObjectId(this);

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

        // Register to audit component.
        EZBAuditComponent auditComponent = getComponent(EZBAuditComponent.class);
        if (auditComponent != null) {
            auditComponent.registerJ2EEManagedObject(this);
        }

        // Dispatch life cycle event.
        this.dispatcher.dispatch(new EventLifeCycleStarting(this.j2eeManagedObjectId));

        // Gets URL of the archive
        final URL url;
        try {
            url = getArchive().getURL();
        } catch (ArchiveException e) {
            throw new EZBContainerException("Cannot get URL on the archive '" + getName() + "'.", e);
        }
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        // Define classloader if it was not yet defined (ear case -->
        // classloader already set)
        if (this.classLoader == null) {
            PrivilegedAction<EasyBeansClassLoader> privilegedAction = new PrivilegedAction<EasyBeansClassLoader>() {
                @Override
                public EasyBeansClassLoader run() {
                    return new EasyBeansClassLoader(new URL[]{url}, old);
                }
            };
            this.classLoader = AccessController.doPrivileged(privilegedAction);
        }
        // FIXME keep resolve ? keep classloader ?
        // Resolve deployment stuff if not already done
        if (!this.resolved) {
            resolve();
        }

        // Run enhancer
        if (!this.enhanced) {
            enhance(true);
        }


        // Send notification to callbacks
        if (getCallbacksLifeCycle().size() > 0) {
            EZBContainerCallbackInfo info = getContainer3CallbackInfo();
            for (EZBContainerLifeCycleCallback callback : getCallbacksLifeCycle()) {
                try {
                    callback.start(info);
                } catch (Throwable t) {
                    // Protection from malicious code
                    logger.error("{0}.start() failed", callback.getClass().getName(), t);
                }
            }
        }

        try {
            MBeansHelper.getInstance().registerMBean(this);
        } catch (MBeansException e) {
            // TODO what to do here ? log or exception ?
            logger.error("Cannot register Container MBeans for " + getArchive().getName(), e);
        }

        // Register resolver
        getEmbedded().getJNDIResolver().addContainerResolver(this.configuration.getContainerJNDIResolver());

        // Start ordered factories
        for (Factory<?, ?> factory : getOrderedFactories()) {
            try {
                factory.start();
            } catch (FactoryException e) {
                throw new EZBContainerException("Cannot start the given factory '" + factory + "'", e);
            }
        }

        // Display infos
        if (logger.isInfoEnabled()) {
            // Compute some statistics
            int sfsb = 0;
            int slsb = 0;
            int mdb = 0;
            int singletons = 0;
            int mb = 0;
            for (Factory<?, ?> factory : this.factories.values()) {
                if (factory instanceof StatelessSessionFactory) {
                    slsb++;
                } else if (factory instanceof StatefulSessionFactory) {
                    sfsb++;
                } else if (factory instanceof SingletonSessionFactory) {
                    singletons++;
                } else if (factory instanceof MDBMessageEndPointFactory) {
                    mdb++;
                } else if (factory instanceof ManagedBeanFactory) {
                    mb++;
                }
            }
            logger.info("Container ''{0}'' [{1} SLSB, {2} SFSB, {3} Sing, {4} MDB, {5} MB] started in {6} ms", getArchive().getName(),
                    Integer.valueOf(slsb), Integer.valueOf(sfsb), Integer.valueOf(singletons), Integer.valueOf(mdb), Integer.valueOf(mb),
                    Long.valueOf((System.currentTimeMillis() - tStart)));
        }

        this.dispatcher.dispatch(new EventContainerStarted(this.j2eeManagedObjectId, getArchive(),
                this.persistenceUnitManager, this.configuration));
        this.dispatcher.dispatch(new EventLifeCycleStarted(this.j2eeManagedObjectId));

        this.available = true;

    }

    protected EZBPersistenceXmlAnalyzer getPersistenceXmlAnalyzer() {
        if (persistenceXmlAnalyzer == null) {
            BasicPersistenceXmlAnalyzer basicPersistenceXmlAnalyzer = new BasicPersistenceXmlAnalyzer();
            basicPersistenceXmlAnalyzer.setTransactionManager(JTransactionManager.getTransactionManager());
            this.persistenceXmlAnalyzer = basicPersistenceXmlAnalyzer;
        }
        return this.persistenceXmlAnalyzer;
    }


    /**
     * @return an order of factories based on DependsOn order.
     */
    protected List<Factory<?, ?>> getOrderedFactories() {

        // First, for each factory, create a Node wrapper
        Map<String, NodeWrapper<Factory<?, ?>>> nodeWrappers = new HashMap<String, NodeWrapper<Factory<?, ?>>>();

        // Put for each bean name a wrapper for a factory
        List<String> beanNames = new ArrayList<String>();
        for (Factory<?, ?> factory : this.factories.values()) {
            IBeanInfo beanInfo = factory.getBeanInfo();
            String beanName = beanInfo.getName();
            beanNames.add(beanName);
            nodeWrappers.put(beanName, new NodeWrapper<Factory<?, ?>>(beanInfo.getName(), factory));
        }

        // Now, for each bean, try to add the depencies for a given wrapper
        for (NodeWrapper<Factory<?, ?>> nodeWrapper : nodeWrappers.values()) {
            for (String beanDependency : nodeWrapper.getWrapped().getBeanInfo().getDependsOn()) {
                NodeWrapper<Factory<?, ?>> dependencyNode = nodeWrappers.get(beanDependency);
                if (dependencyNode != null) {
                    logger.info("Add dependency between bean ''{0}'' and bean ''{1}''", nodeWrapper.getName(), dependencyNode
                            .getName());
                    // Add dependency
                    nodeWrapper.addDependency(dependencyNode);
                    // As there is a dependency from one node to this node, flag the node as startup node
                    dependencyNode.getWrapped().getBeanInfo().setStartup(true);
                }
            }
        }

        // Now, apply a sort
        List<NodeWrapper<Factory<?, ?>>> sortedList = TopologicalSort.sort(nodeWrappers.values());

        List<Factory<?, ?>> sortedFactories = new ArrayList<Factory<?, ?>>();
        List<String> updatedNames = new ArrayList<String>();
        for (NodeWrapper<Factory<?, ?>> nodeWrapper : sortedList) {
            sortedFactories.add(nodeWrapper.getWrapped());
            updatedNames.add(nodeWrapper.getName());
        }

        logger.debug("List transformed from ''{0}'' to ''{1}''", beanNames, updatedNames);

        return sortedFactories;


    }


    /**
     * Run the enhancer on the container.
     *
     * @param createBeanFactories if needs to create bean factories or only perform classic enhancement
     *
     * @throws EZBContainerException if enhancement fails
     */
    @Override
    public void enhance(final boolean createBeanFactories) throws EZBContainerException {
        if (!this.resolved) {
            throw new EZBContainerException("Cannot run enhancer without resolving classes");
        }

        if (this.enhanced) {
            return;
        }

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(this.classLoader);
            Enhancer enhancer = new Enhancer(this.classLoader, this.deployment.getEjbJarArchiveMetadata(), this.enhancerMap);

            long tStartEnhancing = System.currentTimeMillis();
            try {
                enhancer.enhance();
            } catch (EnhancerException ee) {
                throw new EZBContainerException("Cannot run enhancer on archive '" + getName() + "'.", ee);
            } catch (RuntimeException e) {
                // Catch Exception as some exceptions can be runtime exception
                throw new EZBContainerException("Cannot run enhancer on archive '" + getName() + "'.", e);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Enhancement elapsed during : " + (System.currentTimeMillis() - tStartEnhancing) + " ms");
            }

            // Create Beans Factories
            if (createBeanFactories) {

                // Check if there is META-INF/persistence.xml file
                EZBPersistenceUnitManager analyzedPersistenceUnitManager = null;
                try {
                    analyzedPersistenceUnitManager =
                            getPersistenceXmlAnalyzer().analyzePersistenceXmlFile(getArchive(), getClassLoader());

                    // Dispatch life cycle event.
                    if (this.dispatcher != null) {
                        this.dispatcher.dispatch(new EventContainerStarting(this.j2eeManagedObjectId, getArchive(),
                                analyzedPersistenceUnitManager, this.configuration));
                    }
                } catch (PersistenceXmlAnalyzerException e) {
                    throw new EZBContainerException("Cannot analyze the persistence.xml file in the archive", e);
                }

                // No previous manager
                if (this.persistenceUnitManager == null) {
                    this.persistenceUnitManager = analyzedPersistenceUnitManager;
                } else {
                    // merge old and new.
                    if (analyzedPersistenceUnitManager != null) {
                        this.persistenceUnitManager.merge(analyzedPersistenceUnitManager);
                    }
                }


                if (this.dispatcher == null) {
                    this.dispatcher = new EventDispatcher();
                }
                createBeanFactories();
            }

            // cleanup
            this.deployment.reset();
            enhancer = null;
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            this.enhanced = true;
        }

    }


    /**
     * Create the factories of the beans (session and MDB).
     *
     * @throws EZBContainerException if binding fails.
     */
    protected void createBeanFactories() throws EZBContainerException {

        // Create context if not yet done
        Context moduleContext = getConfiguration().getModuleContext();
        if (moduleContext == null) {
            // Create a new module context shared by all this container
            getConfiguration().setModuleContext(new ContextImpl("module"));
        }

        Context appContext = getConfiguration().getAppContext();
        if (appContext == null) {
            // Create a new app context shared by all this container
            getConfiguration().setAppContext(new ContextImpl("app"));
        }


        // Check that the event component is here
        EZBEventComponent eventComponent = getComponent(EZBEventComponent.class);
        if (eventComponent == null) {
            throw new EZBContainerException("Event component is required !");
        }

        // Create/Start/Register a new event dispatcher
        IEventDispatcher eventDispatcher = eventComponent.createEventDispatcher();
        eventDispatcher.start();

        // The topic is common for all EZBContainer and Factory
        eventComponent.getEventService().registerDispatcher(Embedded.NAMING_EXTENSION_POINT,
                eventDispatcher);

        logger.debug("EventService instance {0}", eventComponent.getEventService());

        // Wrap in a try/finally block to be able to stop/unregister the dispatcher afterall
        try {
            this.ejbJarInfo = new EJBJarInfo();
            // bind session beans
            EjbJarArchiveMetadata ejbMetadata = this.deployment.getEjbJarArchiveMetadata();
            if (ejbMetadata != null) {
                List<String> beanNames = this.deployment.getEjbJarArchiveMetadata().getBeanNames();
                for (String beanName : beanNames) {
                    for (EasyBeansEjbJarClassMetadata classAnnotationMetadata : this.deployment.getEjbJarArchiveMetadata()
                            .getClassesForBean(beanName)) {
                        Factory<?, ?> factory = null;

                        // Check class is a bean
                        if (!classAnnotationMetadata.isBean()) {
                            continue;
                        }

                        // Check bean name is matching (can happen with super class also being beans)
                        if (!(!classAnnotationMetadata.isSession() && !classAnnotationMetadata.isMdb() &&
                                                            classAnnotationMetadata.isManagedBean()) &&
                                !beanName.equals(classAnnotationMetadata.getJCommonBean().getName())) {
                            continue;
                        }


                        if (classAnnotationMetadata.isSession()) {
                            factory = createSessionBeanFactory(classAnnotationMetadata);
                        } else if (classAnnotationMetadata.isMdb()) {
                            factory = createMessageDrivenBeanFactory(classAnnotationMetadata);
                        } else if (classAnnotationMetadata.isManagedBean()) {
                            factory = createManagedBeanFactory(classAnnotationMetadata);
                        }

                        // Post-Configure the created factories.
                        if (factory != null) {

                            // Adds more runtime info
                            IBeanInfo beanInfo = factory.getBeanInfo();

                            // EJB Name
                            if (!classAnnotationMetadata.isSession() && !classAnnotationMetadata.isMdb() &&
                                    classAnnotationMetadata.isManagedBean()) {
                                beanInfo.setName(classAnnotationMetadata.getManagedBeanName());
                            } else {
                                beanInfo.setName(classAnnotationMetadata.getJCommonBean().getName());
                            }
                            // Adds security info.
                            beanInfo.setSecurityInfo(SecurityInfoHelper.getSecurityInfo(classAnnotationMetadata));

                            // Adds Business method info.
                            beanInfo.setBusinessMethodsInfo(BusinessMethodsInfoHelper.getMethods(classAnnotationMetadata));

                            // Adds Business method info.
                            beanInfo.setSessionSynchronizationMethodsInfo(
                                    SessionSynchronizationInfoHelper.getMethods(classAnnotationMetadata));

                            // Cluster config
                            beanInfo.setCluster(classAnnotationMetadata.getCluster());

                            if (classAnnotationMetadata.getJAnnotationSqlDataSourceDefinitions() != null) {
                                for (IJAnnotationSqlDataSourceDefinition annotationSqlDataSourceDefinition : classAnnotationMetadata
                                        .getJAnnotationSqlDataSourceDefinitions()) {
                                    beanInfo.getDataSourceDefinitions().add(annotationSqlDataSourceDefinition);
                                }
                            }
                            // Set invocation context factor

                            if (Boolean.getBoolean("easybeans.dynamicinterceptors")) {
                                factory.setInvocationContextFactory(new EasyBeansInvocationContextFactory(classAnnotationMetadata,
                                        this.classLoader));
                            }
                            // Sets the bean info
                            this.ejbJarInfo.addBeanInfo(beanInfo);

                            // Build java: Context
                            Context javaContext;
                            try {
                                javaContext = JavaContextHelper.build(classAnnotationMetadata, factory, eventDispatcher,
                                        getConfiguration());
                            } catch (JavaContextHelperException e) {
                                throw new EZBContainerException("Cannot build environment", e);
                            }

                            // Set java: context
                            factory.setJavaContext(javaContext);

                            // Add Management
                            if (classAnnotationMetadata.isSession() || classAnnotationMetadata.isMdb()) {
                                // TODO check if we have to register an MBean for ManagedBeans
                                try {
                                    MBeansHelper.getInstance().registerMBean(factory);
                                } catch (MBeansException me) {
                                    throw new EZBContainerException("Cannot register the factory MBean", me);
                                }
                            }

                            // Pool config
                            poolConfiguration(factory, classAnnotationMetadata);

                            // Init factory
                            try {
                                factory.init();
                            } catch (FactoryException e) {
                                throw new EZBContainerException("Cannot initialize the factory.", e);
                            }

                            // Add the factory to the managed factories
                            this.factories.put(beanName, factory);

                        }
                    }
                }
                //Register clustered beans if necessary.
                eventDispatcher.dispatch(new EventLifeCycleClusteredBean(Embedded.NAMING_EXTENSION_POINT,
                        EZBClusteredBeanEvent.STARTING, this.bindingReferences));

                // Bind references only once
                bindReferences();

                // Permission Manager.
                try {
                    this.permissionManager = new PermissionManager(getArchive().getURL(), this.ejbJarInfo);
                    // translate metadata into permission
                    this.permissionManager.translateMetadata();
                    // commit
                    this.permissionManager.commit();
                } catch (PermissionManagerException e) {
                    throw new EZBContainerException("Cannot create permission manager", e);
                } catch (ArchiveException e) {
                    throw new EZBContainerException("Cannot create permission manager", e);
                }

            }
        } finally {
            eventDispatcher.stop();
            eventComponent.getEventService().unregisterDispatcher(Embedded.NAMING_EXTENSION_POINT);
        }
    }

    /**
     * Bind the collected EZBRef using the registered BindingFactories.
     *
     * @throws EZBContainerException If the beforeBind() callbacks or
     *                               the BindingFactory.bind() methods thrown Exceptions
     */
    protected void bindReferences() throws EZBContainerException {

        // Register the Binding References
        // Cannot be null: references are inited during instance creation
        for (EZBRef reference : this.bindingReferences) {

            // Invoke callbacks
            List<EZBContainerLifeCycleCallback> lifeCycleCallbacks = getCallbacksLifeCycle();
            if (!lifeCycleCallbacks.isEmpty()) {
                EZBContainerCallbackInfo info = getContainer3CallbackInfo();
                for (EZBContainerLifeCycleCallback lifeCycleCallback : lifeCycleCallbacks) {
                    try {
                        lifeCycleCallback.beforeBind(info, reference);
                    } catch (LifeCycleCallbackException e) {
                        throw new EZBContainerException("Cannot invoke the callback before binding.", e);
                    }
                }
            }

            // Bind it
            for (EZBBindingFactory bindingFactory : BindingManager.getInstance().getFactories()) {
                try {
                    bindingFactory.bind(reference);
                } catch (BindingException e) {
                    logger.warn("Cannot bind the reference ''{0}'' on the binding factory ''{1}'' for the container ''{2}''.",
                            reference, bindingFactory, getArchive().getName(), e);

                }
            }

        }

    }

    /**
     * Configure pool on the bean factory.
     *
     * @param factory      the given factory to configure
     * @param beanMetadata the metadata of the bean
     */
    protected void poolConfiguration(final Factory<?, ?> factory, final EasyBeansEjbJarClassMetadata beanMetadata) {
        // Pool parameters ?
        IPoolConfiguration poolConfiguration = beanMetadata.getPoolConfiguration();
        if (poolConfiguration != null) {
            Pool<?, ?> pool = factory.getPool();
            if (pool != null) {
                pool.setPoolConfiguration(poolConfiguration);
            } else {
                logger.warn("Cannot configure Pool for factory {0} for bean class {1} as pool is not initialized", factory,
                        beanMetadata);
            }
        }
    }

    /**
     * Gets the activation spec object.
     *
     * @param mdbMetadata the metadata of the MDB
     *
     * @return the activation spec value or the default one if no activation spec was specified
     */
    protected String getActivationSpec(final EasyBeansEjbJarClassMetadata mdbMetadata) {
        String activationSpec = null;

        EasyBeansDD easybeansDD = this.deployment.getEjbJarArchiveMetadata().getEasyBeansDD();
        if (easybeansDD != null) {
            EJB ejb = easybeansDD.getEJB();
            if (ejb != null) {
                // get MDB
                List<MessageDrivenBean> mdbList = ejb.getMessageDrivenBeans();
                if (mdbList != null) {
                    for (MessageDrivenBean mdb : mdbList) {
                        if (mdb.getEjbName().equals(mdbMetadata.getJCommonBean().getName())) {
                            String mdbActivationSpec = mdb.getActivationSpec();
                            if (mdbActivationSpec != null) {
                                activationSpec = mdbActivationSpec;
                                logger.debug("Using for mdb ''{0}'' the activation Spec ''{1}''", mdb.getEjbName(),
                                        mdbActivationSpec);
                            }
                        }
                    }
                }
            }
        }
        if (activationSpec == null) {
            activationSpec = DEFAULT_ACTIVATION_SPEC_NAME;
        }

        return activationSpec;

    }


    /**
     * Creates the given message driven bean factory.
     *
     * @param messageDrivenBean the message driven bean class metadata.
     *
     * @return the build factory.
     *
     * @throws EZBContainerException if the message driven bean cannot be
     *                               created.
     */
    protected Factory<?, ?> createMessageDrivenBeanFactory(final EasyBeansEjbJarClassMetadata messageDrivenBean)
            throws EZBContainerException {
        String className = messageDrivenBean.getClassName().replace('/', '.');

        // Activation Spec object
        String activationSpecName = getActivationSpec(messageDrivenBean);


        // get Activation spec object
        ActivationSpec activationSpec = null;
        try {
            activationSpec = (ActivationSpec) new InitialContext().lookup(activationSpecName);
        } catch (NamingException e1) {
            throw new EZBContainerException("Cannot get the activation spec with the name '"
                    + activationSpecName + "'.", e1);
        }

        // Marshall the given object in order to be sure to get a new Object each time
        if (activationSpec instanceof Serializable) {
            byte[] byteArgs;
            try {
                byteArgs = storeObject((Serializable) activationSpec);
            } catch (IOException e) {
                throw new EZBContainerException("Cannot serialize the activation spec object '" + activationSpec + "'.", e);
            }

            // Then load object from this array of bytes
            try {
                activationSpec = (ActivationSpec) loadObject(byteArgs);
            } catch (IOException e) {
                throw new EZBContainerException("Cannot load activation spec from the serialized object.", e);
            } catch (ClassNotFoundException e) {
                throw new EZBContainerException("Cannot load activation spec from the serialized object.", e);
            }

        }

        // get ResourceAdapter object
        ResourceAdapter resourceAdapter = null;
        try {
            resourceAdapter = MDBResourceAdapterHelper.getResourceAdapter(activationSpecName,
                    (Embedded) getConfiguration().getEZBServer());
        } catch (ResourceException e) {
            throw new EZBContainerException("Cannot get the resource adapter for this MDB factory", e);
        }

        // Associate Resource Adapter with ActivationSpec object (if not set)
        if (activationSpec.getResourceAdapter() == null) {
            try {
                activationSpec.setResourceAdapter(resourceAdapter);
            } catch (ResourceException e) {
                throw new EZBContainerException("Cannot associate resource adapter with activation spec object", e);
            }
        }

        // Get JMS Component
        JMSComponent jmsComponent = null;
        jmsComponent = getEmbedded().getComponent(JMSComponent.class);

        // Create factory
        MDBMessageEndPointFactory mdbMessageEndPointFactory = null;
        try {
            mdbMessageEndPointFactory = new MDBMessageEndPointFactory(className, this, activationSpec, resourceAdapter,
                    jmsComponent);
        } catch (FactoryException e) {
            throw new EZBContainerException("Cannot build the MDB MessageEndPoint factory", e);
        }

        // build runtime information
        MessageDrivenInfo messageDrivenInfo = new MessageDrivenInfo();
        messageDrivenInfo.setApplicationExceptions(
                convertApplicationExceptionInfo(messageDrivenBean.getEjbJarDeployableMetadata().getApplicationExceptions()));
        messageDrivenInfo.setTransactionManagementType(messageDrivenBean.getTransactionManagementType());
        messageDrivenInfo.setMessageListenerInterface(messageDrivenBean.getJMessageDriven()
                .getMessageListenerInterface());
        messageDrivenInfo.setTimersInfo(convertTimersInfo(messageDrivenBean));
        messageDrivenInfo.setMessageDestinationLink(messageDrivenBean.getJMessageDriven()
                .getMessageDestinationLink());

        // MappedName ? use it as destination
        String mappedName = messageDrivenBean.getJCommonBean().getMappedName();
        if (mappedName != null && !"".equals(mappedName)) {
            messageDrivenBean.getJMessageDriven().getActivationConfigProperties().add(
                    new JActivationConfigProperty("destination", mappedName));
        }

        messageDrivenInfo.setActivationConfigProperties(messageDrivenBean.getJMessageDriven()
                .getActivationConfigProperties());

        // Dispatch message driven info before continuing
        this.dispatcher.dispatch(new EventMessageDrivenInfo(this.j2eeManagedObjectId, messageDrivenInfo), 10000);

        mdbMessageEndPointFactory.setMessageDrivenInfo(messageDrivenInfo);


        return mdbMessageEndPointFactory;

    }

    /**
     * Creates the given session bean and bind it.
     *
     * @param sessionBean the session bean class metadata.
     *
     * @return the build factory.
     *
     * @throws EZBContainerException if the session bean cannot be created
     */
    protected Factory<?, ?> createSessionBeanFactory(final EasyBeansEjbJarClassMetadata sessionBean) throws EZBContainerException {
        String className = sessionBean.getClassName().replace('/', '.');
        String factoryName = sessionBean.getJCommonBean().getName();

        SessionFactory<?> sessionFactory = null;

        if (sessionBean.isStateless()) {
            try {
                sessionFactory = new StatelessSessionFactory(className, this);
            } catch (FactoryException fe) {
                throw new EZBContainerException("Cannot build the stateless factory", fe);
            }
        } else if (sessionBean.isStateful()) {
            try {
                sessionFactory = new StatefulSessionFactory(className, this, sessionBean.hasExtendedPersistenceContext());
            } catch (FactoryException fe) {
                throw new EZBContainerException("Cannot build the stateful factory", fe);
            }
        } else if (sessionBean.isSingleton()) {
            try {
                sessionFactory = new SingletonSessionFactory(className, this);
            } catch (FactoryException fe) {
                throw new EZBContainerException("Cannot build the stateful factory", fe);
            }
        } else {
            throw new EZBContainerException("unknown session type for: " + sessionBean);
        }

        // Build runtime information
        SessionBeanInfo sessionBeanInfo = new SessionBeanInfo(sessionBean.getJavaxEjbStatefulTimeout());
        sessionBeanInfo.setTransactionManagementType(sessionBean.getTransactionManagementType());
        sessionBeanInfo.setTimersInfo(convertTimersInfo(sessionBean));
        sessionBeanInfo.setApplicationExceptions(convertApplicationExceptionInfo(sessionBean.getEjbJarDeployableMetadata().getApplicationExceptions()));

        // Only for singleton
        if (sessionBean.isSingleton()) {
            sessionBeanInfo.setStartup(sessionBean.isStartup());

            // DependsOn ?
            IDependsOn dependsOn = sessionBean.getDependsOn();
            if (dependsOn != null) {
                List<String> beanDepencies = dependsOn.getBeans();
                sessionBeanInfo.setDependsOn(beanDepencies);
            }
        }

        sessionFactory.setSessionBeanInfo(sessionBeanInfo);

        // WS ?
        IClassMetadata classMetadata = sessionBean.getClassMetadata();
        IWebservicesView webservicesView = classMetadata.as(IWebservicesView.class);

        // Build WS deploy/time info
        if (webservicesView != null && webservicesView.getWebServiceMarker() != null) {
            // Bean is annotated with @WebService or @WebServiceprovider
            IWebServiceInfo info = createWebServiceInfo(sessionBean, factoryName);
            sessionBeanInfo.setWebServiceInfo(info);
        } // else this bean is not webservices annotated

        // No interface local view
        if (sessionBean.isLocalBean()) {
            sessionBeanInfo.setNoInterfaceViewInterface(sessionBean.getClassName());
            this.bindingReferences.add(createNoInterfaceViewRef(sessionBean.getClassName(),
                    getEmbedded().getID(),
                    getId(),
                    factoryName,
                    sessionBean,
                    sessionFactory));
        }


        // get interfaces of bean
        IJLocal localItfs = sessionBean.getLocalInterfaces();
        IJRemote remoteItfs = sessionBean.getRemoteInterfaces();

        if (localItfs != null) {
            sessionBeanInfo.setLocalInterfaces(localItfs.getInterfaces());
            for (String itf : localItfs.getInterfaces()) {
                this.bindingReferences.add(createLocalItfRef(itf,
                        getEmbedded().getID(),
                        getId(),
                        factoryName,
                        sessionBean,
                        sessionFactory));
            }
        }
        if (remoteItfs != null) {
            sessionBeanInfo.setRemoteInterfaces(remoteItfs.getInterfaces());
            for (String itf : remoteItfs.getInterfaces()) {
                this.bindingReferences.add(createRemoteItfRef(itf,
                        getId(),
                        factoryName,
                        sessionBean,
                        sessionFactory));
            }
        }

        // Bind EJB 2.x Home/LocalHome interfaces
        String remoteHome = sessionBean.getRemoteHome();
        String localHome = sessionBean.getLocalHome();
        if (remoteHome != null) {
            this.bindingReferences.add(createRemoteHomeRef(remoteHome,
                    getId(),
                    factoryName,
                    sessionBean,
                    sessionFactory));
        }
        if (localHome != null) {
            this.bindingReferences.add(createLocalHomeRef(localHome,
                    getEmbedded().getID(),
                    getId(),
                    factoryName,
                    sessionBean,
                    sessionFactory));
        }

        return sessionFactory;
    }

    /**
     * Creates a ManagedBean Factory for a given ManagedBean class metadata.
     *
     * @param managedBean a Managed Bean class metadata
     * @return The created Factory
     * @throws EZBContainerException If the ManagedBean Factory can't be created
     */
    private Factory<?, ?> createManagedBeanFactory(EasyBeansEjbJarClassMetadata managedBean) throws EZBContainerException {
        String className = managedBean.getClassName().replace('/', '.');
        String factoryName = managedBean.getManagedBeanName();

        ManagedBeanFactory managedBeanFactory = null;
        try {
            managedBeanFactory = new ManagedBeanFactory(className, this);
        } catch (FactoryException fe) {
            throw new EZBContainerException("Cannot build the ManagedBean factory", fe);
        }

        // Build runtime information
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setTransactionManagementType(managedBean.getTransactionManagementType());
        beanInfo.setTimersInfo(convertTimersInfo(managedBean));
        beanInfo.setApplicationExceptions(
                convertApplicationExceptionInfo(managedBean.getEjbJarDeployableMetadata().getApplicationExceptions()));


        this.bindingReferences.add(createManagedBeanRef(managedBean.getJClass().getName(), getEmbedded().getID(), getId(), factoryName,
                managedBean, managedBeanFactory));

        managedBeanFactory.setBeanInfo(beanInfo);

        return managedBeanFactory;
    }


    /**
     * Gets timers info from a given bean.
     * @param bean the bean to analyze
     * @return a list of timers
     */
    protected List<ITimerInfo> convertTimersInfo(final EasyBeansEjbJarClassMetadata bean) {

        List<ITimerInfo> timersInfo = new ArrayList<ITimerInfo>();

        // Scan all methods
        for (EasyBeansEjbJarMethodMetadata methodMetadata : bean.getMethodMetadataCollection()) {
            List<IJEjbSchedule> schedules = methodMetadata.getJavaxEjbSchedules();
            if (schedules != null && schedules.size() > 0) {
                for (IJEjbSchedule schedule : schedules) {

                    ScheduleExpression scheduleExpression = new ScheduleExpression();
                    scheduleExpression = scheduleExpression.second(schedule.getSecond()).minute(schedule.getMinute()).hour(
                            schedule.getHour()).dayOfMonth(schedule.getDayOfMonth()).month(schedule.getMonth()).dayOfWeek(
                            schedule.getDayOfWeek()).year(schedule.getYear());
                    if (schedule.getStart() != null) {
                    scheduleExpression = scheduleExpression.start(Date.valueOf(schedule.getStart()));
                    }
                    if (schedule.getEnd() != null) {
                        scheduleExpression = scheduleExpression.start(Date.valueOf(schedule.getEnd()));
                    }

                    TimerConfig timerConfig = new TimerConfig();
                    timerConfig.setInfo(schedule.getInfo());
                    timerConfig.setPersistent(schedule.isPersistent());

                    TimerInfo timerInfo = new TimerInfo();
                    timerInfo.setScheduleExpression(scheduleExpression);
                    timerInfo.setTimerConfig(timerConfig);
                    timerInfo.setMethodInfo(new MethodInfo(methodMetadata));

                    timersInfo.add(timerInfo);

                }
            }
        }

        return timersInfo;

    }

    /**
     * Convert metadata infos into runtime info.
     *
     * @param applicationExceptions the given metadata application exceptions
     *
     * @return the converted list
     */
    protected Map<String, IApplicationExceptionInfo> convertApplicationExceptionInfo(
            final Map<String, IApplicationException> applicationExceptions) {
        Map<String, IApplicationExceptionInfo> applicationExceptionInfos = new HashMap<String, IApplicationExceptionInfo>();
        if (applicationExceptions != null) {
            Set<Entry<String, IApplicationException>> set = applicationExceptions.entrySet();
            Iterator<Entry<String, IApplicationException>> it = set.iterator();
            while (it.hasNext()) {
                Entry<String, IApplicationException> entry = it.next();
                IApplicationException e = entry.getValue();
                applicationExceptionInfos.put(entry.getKey(), new ApplicationExceptionInfo(e.rollback(), e.inherited()));
            }
        }
        return applicationExceptionInfos;
    }


    /**
     * Creates the WebServiceinfo structure holding data from the XML descriptors.
     *
     * @param sessionBean the session bean metadata
     * @param beanName    the bean's name
     *
     * @return a webservice info structure (or null is there was no XML available)
     */
    protected IWebServiceInfo createWebServiceInfo(final EasyBeansEjbJarClassMetadata sessionBean,
            final String beanName) {

        // Get the WS marker
        IWebservicesView webservicesView = sessionBean.getClassMetadata().as(IWebservicesView.class);
        if (webservicesView == null) {
            return null;
        }
        IWebServiceMarker marker = webservicesView.getWebServiceMarker();
        String name = marker.getName();

        // Use info from webservices.xml if available
        EjbJarArchiveMetadata ejbJarArchiveMetadata = this.deployment.getEjbJarArchiveMetadata();

        IPortComponent portComponent = null;
        IWebservicesView ejbJarArchiveMetadataWebservicesView = ejbJarArchiveMetadata.getEjbJarMetadata().as(IWebservicesView.class);
        if (ejbJarArchiveMetadataWebservicesView != null) {
            IWebservices webservicesDD = ejbJarArchiveMetadataWebservicesView.getWebservices12();
            if (webservicesDD != null) {
                // Resolve the port-component associated with this endpoint
                portComponent = webservicesDD.findPortComponent(beanName);

                if ((portComponent != null) && (name == null)) {
                    // Find the port name
                    name = portComponent.getName();
                }
            }
        }

        // Use info from easybeans.xml if available
        WebserviceEndpoint endpoint = null;
        EasyBeansDD easybeansDD = this.deployment.getEjbJarArchiveMetadata().getEasyBeansDD();
        if (easybeansDD != null) {

            // Iterates on additional datas
            EasyBeansWebservices webservices = easybeansDD.getWebservices();
            if ((webservices != null) && (webservices.getWebserviceEndpoints() != null)) {

                Iterator<WebserviceEndpoint> i = webservices.getWebserviceEndpoints().iterator();
                for (; i.hasNext() && (endpoint == null); ) {
                    WebserviceEndpoint browsed = i.next();

                    if (browsed.getPortComponentName().equals(name)) {
                        endpoint = browsed;
                    }
                }
            }

            // Fill the Info structure
            WebServiceInfo info = new WebServiceInfo();

            // Read name
            info.setPortComponentName(name);

            if (portComponent != null) {

                // Read wsdl-location from the webservice-description parent element
                info.setWsdlLocation(portComponent.getParent().getWsdlFile());

                // Read service QName
                info.setServiceName(portComponent.getWsdlService());

                // Read port QName
                info.setPortName(portComponent.getWsdlPort());

                // Read protocol-binding
                info.setProtocolBinding(portComponent.getProtocolBinding());

                // Read enable-mtom
                info.setMTOMEnabled(portComponent.isMTOMEnabled());

                // Read service-endpoint-interface
                if (!marker.isWebServiceProvider()) {
                    info.setServiceEndpointInterface(portComponent.getServiceEndpointInterface());
                }

                // Read HandlerChains
                info.setHandlerChains(portComponent.getHandlerChains());

            }

            // EasyBeans specific values ....
            // ---------------------------------------------------
            // manage endpoint-address
            info.setEndpointAddress(sessionBean.getWebServiceEndpointAddress());

            // manage context-root
            info.setContextRoot(sessionBean.getWebServiceContextRoot());

            // manage realm-name
            info.setRealmName(sessionBean.getWebServiceRealmName());

            // manage transport-guarantee
            info.setTransportGuarantee(sessionBean.getWebServiceTransportGuarantee());

            // manage auth-method
            info.setAuthMethod(sessionBean.getWebServiceAuthMethod());

            // manage http-methods
            info.setHttpMethods(sessionBean.getWebServiceHttpMethods());

            // manage wsdl-publication-directory
            if (endpoint != null) {
                info.setWsdlPublicationDirectory(endpoint.getWsdlPublicationDirectory());
            }

            return info;
        }

        return null;

    }

    /**
     * Stop this container.
     */
    @Override
    public void stop() {
        this.available = false;

        // Must resolve again after it has been stopped
        this.resolved = false;

        this.dispatcher.dispatch(new EventLifeCycleStopping(this.j2eeManagedObjectId));
        this.dispatcher.dispatch(new EventContainerStopping(this.j2eeManagedObjectId, getArchive(), this.configuration));

        // Unregister resolver
        getEmbedded().getJNDIResolver().removeContainerResolver(this.configuration.getContainerJNDIResolver());

        // Unregister MBean
        try {
            MBeansHelper.getInstance().unregisterMBean(this);
        } catch (MBeansException e) {
            // TODO what to do here ? log or exception ?
            logger.error("Cannot unregister Container MBeans for " + getArchive().getName(), e);
        }

        // stop each factories
        for (Factory<?, ?> f : this.factories.values()) {
            f.stop();

            // Remove MBeans
            if (!(f instanceof ManagedBeanFactory)) {
                // TODO check if we have to register an MBean for ManagedBeans
                try {
                    MBeansHelper.getInstance().unregisterMBean(f);
                } catch (MBeansException me) {
                    logger.error("Cannot unregister the factory MBean", me);
                }
            }

        }


        // Send notification to callbacks
        if (getCallbacksLifeCycle().size() > 0) {
            EZBContainerCallbackInfo info = getContainer3CallbackInfo();
            for (EZBContainerLifeCycleCallback callback : getCallbacksLifeCycle()) {
                try {
                    callback.stop(info);
                } catch (Throwable t) {
                    // Protection from malicious code
                    logger.error("{0}.stop() failed", callback.getClass().getName(), t);
                }
            }
        }
        final ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.classLoader);

        // Unregister clustered beans if necessary.
        this.dispatcher.dispatch(new EventLifeCycleClusteredBean(Embedded.NAMING_EXTENSION_POINT,
                EZBClusteredBeanEvent.STOPPING, this.bindingReferences));

        // Unbind references
        try {
            for (EZBRef reference : this.bindingReferences) {
                for (EZBBindingFactory bindingFactory : BindingManager.getInstance().getFactories()) {
                    try {
                        bindingFactory.unbind(reference);
                    } catch (BindingException e) {
                        logger.warn("Cannot unbind the reference ''{0}'' on the binding factory ''{1}'' for the container ''{2}''.",
                                reference, bindingFactory, getArchive().getName(), e);

                    }
                }
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            this.classLoader = null;
        }
        // Dispatch lifecycle event.
        this.dispatcher.dispatch(new EventContainerStopped(this.j2eeManagedObjectId, getArchive(), this.configuration));
        this.dispatcher.dispatch(new EventLifeCycleStopped(this.j2eeManagedObjectId));

        // Unregister from statistic component.
        EZBStatisticComponent statisticComponent = getComponent(EZBStatisticComponent.class);
        if (statisticComponent != null) {
            statisticComponent.unregisterJ2EEManagedObject(this);
        }

        // Unregister from audit component.
        EZBAuditComponent auditComponent = getComponent(EZBAuditComponent.class);
        if (auditComponent != null) {
            auditComponent.unregisterJ2EEManagedObject(this);
        }

        // Unregister from jmx component will be done by the mbean itself.

        // Unregister from event component.
        EZBEventComponent eventComponent = getComponent(EZBEventComponent.class);
        if (eventComponent != null) {
            eventComponent.unregisterJ2EEManagedObject(this);
        }


        // Destroy the event dispatcher.
        this.dispatcher.stop();
        this.dispatcher = null;
        this.factories = null;
        this.enhancerMap = null;
    }

    /**
     * Gets information on the container that can be given to container
     * callbacks.
     *
     * @return information on the managed container.
     */
    protected EZBContainerCallbackInfo getContainer3CallbackInfo() {
        EZBContainerCallbackInfo info = new EZBContainerCallbackInfo();
        info.setArchive(getArchive());
        info.setFactories(this.factories);
        info.setContainer(this);
        return info;
    }


    /**
     * Creates an EJBHome reference and return it.
     *
     * @param remoteHome  the name of the remote home interface that object will have.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     this bean's factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected EJBHomeCallRef createRemoteHomeRef(final String remoteHome,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final SessionFactory<?> factory) throws EZBContainerException {
        String itfClsName = remoteHome.replace('/', '.');
        try {
            this.classLoader.loadClass(itfClsName);
        } catch (ClassNotFoundException e) {
            throw new EZBContainerException(
                    "Cannot find the class '" + remoteHome + "' in Classloader '" + this.classLoader + "'.", e);
        }

        // get the name of the Remote Interface
        String remoteInterface = bean.getRemoteInterface();

        // Build a reference
        EJBHomeCallRef ejbHomeCallRef = new EJBHomeCallRef(itfClsName, containerID, factoryName, bean.isStateful(),
                remoteInterface);

        // Assign the factory
        ejbHomeCallRef.setFactory(factory);

        // Set the JNDI naming infos
        ejbHomeCallRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(this.configuration.getNamingStrategies(),
                BeanNamingInfoHelper.buildInfo(bean, itfClsName, "RemoteHome", getConfiguration())));

        return ejbHomeCallRef;

    }

    /**
     * Creates an EJB Local Home reference and return it.
     *
     * @param itf         the name of the interface that object will have.
     * @param embeddedId  the ID of the embedded server.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     this bean's factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected EJBLocalHomeCallRef createLocalHomeRef(final String itf,
            final Integer embeddedId,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final SessionFactory<?> factory) throws EZBContainerException {
        String itfClsName = itf.replace('/', '.');
        try {
            this.classLoader.loadClass(itfClsName);
        } catch (ClassNotFoundException e) {
            throw new EZBContainerException(
                    "Cannot find the class '" + itf + "' in Classloader '" + this.classLoader + "'.", e);
        }

        // Build a reference
        EJBLocalHomeCallRef ejbLocalHomeCallRef = new EJBLocalHomeCallRef(itfClsName, embeddedId, containerID, factoryName, bean
                .isStateful());

        // Assign to factory to it
        ejbLocalHomeCallRef.setFactory(factory);

        // Set the JNDI naming infos
        ejbLocalHomeCallRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(this.configuration.getNamingStrategies(),
                BeanNamingInfoHelper.buildInfo(bean, itfClsName, "LocalHome", getConfiguration())));

        return ejbLocalHomeCallRef;
    }

    /**
     * Creates a no-interface local view reference and return it.
     *
     * @param itf         the name of the interface that object will have.
     * @param embeddedId  the ID of the embedded server.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     this EJB Factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected LocalCallRef createNoInterfaceViewRef(final String itf,
            final Integer embeddedId,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final SessionFactory<?> factory) throws EZBContainerException {
        // Name of the interface is the name of the bean class
        String beanClassName = itf.replace('/', '.');
        String beanProxyClassName = ProxyClassEncoder.getProxyClassName(itf).replace('/', '.');


        // Build a reference
        NoInterfaceLocalCallRef localCallRef = new NoInterfaceLocalCallRef(beanClassName, embeddedId, containerID,
                factoryName, bean.isStateful(), beanProxyClassName);

        // Assign it to the factory
        localCallRef.setFactory(factory);

        // Set the JNDI naming infos
        localCallRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(this.configuration.getNamingStrategies(),
                BeanNamingInfoHelper.buildInfo(bean, beanClassName, "NoInterfaceLocalView", getConfiguration())));

        return localCallRef;
    }

    /**
     * Creates an EJB Local interface reference and return it.
     *
     * @param itf         the name of the interface that object will have.
     * @param embeddedId  the ID of the embedded server.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     this EJB Factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected LocalCallRef createLocalItfRef(final String itf,
            final Integer embeddedId,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final SessionFactory<?> factory) throws EZBContainerException {
        String itfClsName = itf.replace('/', '.');
        try {
            this.classLoader.loadClass(itfClsName);
        } catch (ClassNotFoundException e) {
            throw new EZBContainerException(
                    "Cannot find the class '" + itf + "' in Classloader '" + this.classLoader + "'.", e);
        }

        // Build a reference
        LocalCallRef localCallRef = new LocalCallRef(itfClsName, embeddedId, containerID, factoryName, bean.isStateful());

        // Assign it to the factory
        localCallRef.setFactory(factory);

        // Set the JNDI naming infos
        localCallRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(this.configuration.getNamingStrategies(),
                BeanNamingInfoHelper.buildInfo(bean, itfClsName, "Local", getConfiguration())));

        return localCallRef;
    }

    /**
     * Creates an EJB Remote interface reference and return it.
     *
     * @param itf         the name of the interface that object will have.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     This bean's factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected RemoteCallRef createRemoteItfRef(final String itf,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final SessionFactory<?> factory) throws EZBContainerException {
        String itfClsName = itf.replace('/', '.');
        try {
            this.classLoader.loadClass(itfClsName);
        } catch (ClassNotFoundException e) {
            throw new EZBContainerException(
                    "Cannot find the class '" + itf + "' in Classloader '" + this.classLoader + "'.", e);
        }

        // Build a reference
        RemoteCallRef remoteCallRef = new RemoteCallRef(itfClsName, containerID, factoryName, bean.isStateful());

        // Assign the factory
        remoteCallRef.setFactory(factory);

        // Set the JNDI naming infos
        remoteCallRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(this.configuration.getNamingStrategies(),
                BeanNamingInfoHelper.buildInfo(bean, itfClsName, "Remote", getConfiguration())));

        return remoteCallRef;
    }

    /**
     * Creates an ManagedBean interface reference and return it.
     *
     * @param itf         the name of the interface that object will have.
     * @param embeddedId  the ID of the embedded server.
     * @param containerID the ID of the container.
     * @param factoryName the name of the factory.
     * @param bean        the bean class associated to given interface.
     * @param factory     this ManagedBean Factory
     *
     * @return the reference.
     *
     * @throws EZBContainerException if interface cannot be loaded or if the
     *                               bind fails
     */
    protected ManagedBeanRef createManagedBeanRef(final String itf,
            final Integer embeddedId,
            final String containerID,
            final String factoryName,
            final EasyBeansEjbJarClassMetadata bean,
            final ManagedBeanFactory factory) throws EZBContainerException {
        String itfClsName = itf.replace('/', '.');
        try {
            this.classLoader.loadClass(itfClsName);
        } catch (ClassNotFoundException e) {
            throw new EZBContainerException(
                    "Cannot find the class '" + itf + "' in Classloader '" + this.classLoader + "'.", e);
        }

        // Build a reference
        ManagedBeanRef managedBeanRef = new ManagedBeanRef(itfClsName, embeddedId, containerID, factoryName, bean.isStateful());

        // Assign it to the factory
        managedBeanRef.setFactory(factory);

        // Set the JNDI naming infos
        managedBeanRef.setJNDINamingInfos(JNDINamingInfoHelper.buildInfo(
                new ArrayList<EZBNamingStrategy>(Collections.singleton(new ManagedBeanNamingStrategy())),
                BeanNamingInfoHelper.buildInfo(bean, itfClsName, null, getConfiguration())));

        return managedBeanRef;
    }

    /**
     * Gets a factory with its given name.
     *
     * @param factoryName the factory name.
     *
     * @return the factory found or null.
     */
    @Override
    public Factory<?, ?> getFactory(final String factoryName) {
        return this.factories.get(factoryName);
    }

    /**
     * @return Returns a Collection of managed Factories.
     */
    public Collection<Factory<?, ?>> getFactories() {
        return this.factories.values();
    }

    /**
     * Gets the name of this container.
     *
     * @return the name.
     */
    @Override
    public String getName() {
        return getArchive().getName();
    }

    /**
     * Gets the classloader.
     *
     * @return classloader of the container
     */
    @Override
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * Gets the deployable used by this container.
     *
     * @return the deployable.
     */
    @Override
    public IDeployable getDeployable() {
        return this.configuration.getDeployable();
    }

    /**
     * Gets the archive used by this container. It can be a .jar file or a
     * directory.
     *
     * @return the archive.
     */
    @Override
    public IArchive getArchive() {
        return this.configuration.getArchive();
    }

    /**
     * Gets the parent EZBServer instance.
     *
     * @return Returns the Embedded instance.
     */
    public EZBServer getEmbedded() {
        return this.configuration.getEZBServer();
    }

    /**
     * @return Returns the LifeCycleCallback(s) instances as a List.
     */
    protected List<EZBContainerLifeCycleCallback> getCallbacksLifeCycle() {
        return this.configuration.getCallbacks();
    }

    /**
     * Check if the container is available or not.
     *
     * @return true if the container is available.
     */
    @Override
    public boolean isAvailable() {
        return this.available;
    }

    /**
     * Gets the persistence manager object which manages all persistence-unit
     * associated to this container.
     *
     * @return persistence unit manager object
     */
    @Override
    public EZBPersistenceUnitManager getPersistenceUnitManager() {
        return this.persistenceUnitManager;
    }

    /**
     * Sets the classloader.
     *
     * @param classLoader to be used by the container
     */
    @Override
    public void setClassLoader(final ClassLoader classLoader) {
        if (this.classLoader != null) {
            throw new IllegalArgumentException("Cannot replace an existing classloader");
        }
        this.classLoader = classLoader;
    }

    /**
     * Sets the persistence manager object which manages all persistence-unit
     * associated to this container.
     *
     * @param persistenceUnitManager persistence unit manager object to set.
     */
    @Override
    public void setPersistenceUnitManager(final EZBPersistenceUnitManager persistenceUnitManager) {
        if (this.persistenceUnitManager != null) {
            throw new IllegalArgumentException("Cannot replace an existing persistenceUnitManager");
        }
        this.persistenceUnitManager = persistenceUnitManager;
    }

    /**
     * @return Returns the Container Configuration.
     */
    @Override
    public EZBContainerConfig getConfiguration() {
        return this.configuration;
    }

    /**
     * Gets the permission manager (that manages EJB permissions).
     *
     * @return permission manager.
     */
    @Override
    public EZBPermissionManager getPermissionManager() {
        return this.permissionManager;
    }

    /**
     * Sets the permission manager (that manages EJB permissions).
     *
     * @param ezbPermissionManager the EasyBeans permission manager.
     */
    @Override
    public void setPermissionManager(final EZBPermissionManager ezbPermissionManager) {
        this.permissionManager = ezbPermissionManager;
    }


    /**
     * Add extra archives for finding classes.
     *
     * @param extraArchives the given archives.
     */
    @Override
    public void setExtraArchives(final List<IArchive> extraArchives) {
        this.deployment.setExtraArchives(extraArchives);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ow2.easybeans.api.EZBExtensor#addExtension(java.lang.Class, java.lang.Object)
     */
    @Override
    public <T> T addExtension(final Class<T> clazz, final T extension) {
        return this.extensor.addExtension(clazz, extension);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ow2.easybeans.api.EZBExtensor#getExtension(java.lang.Class)
     */
    @Override
    public <T> T getExtension(final Class<T> clazz) {
        return this.extensor.getExtension(clazz);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.ow2.easybeans.api.EZBExtensor#removeExtension(java.lang.Class)
     */
    @Override
    public <T> T removeExtension(final Class<T> clazz) {
        return this.extensor.removeExtension(clazz);
    }

    /**
     * Get a reference to the first component matching the interface.
     *
     * @param <T> The interface type.
     * @param itf The interface class.
     *
     * @return The component.
     */
    @Override
    public <T extends EZBComponent> T getComponent(final Class<T> itf) {
        return getEmbedded().getComponent(itf);
    }

    /**
     * Get the J2EE managed object id.
     *
     * @return The J2EE managed object id.
     */
    @Override
    public String getJ2EEManagedObjectId() {
        return this.j2eeManagedObjectId;
    }

    /**
     * Get the event Dispatcher.
     *
     * @return The event Dispatcher.
     */
    public IEventDispatcher getEventDispatcher() {
        return this.dispatcher;
    }


    /**
     * @return the extended persistence context for the current container.
     *         This return the current map from a thread local so the data is only on a current thread.
     *         The key of the returned map is the name of the persistence unit
     */
    @Override
    public Map<String, EZBExtendedEntityManager> getCurrentExtendedPersistenceContexts() {
        return this.currentExtendedPersistenceContexts.get();
    }

    /**
     * Sets the data on the current thread.
     *
     * @param extendedPersistenceContexts a map between the persistence unit name and the associated extended persistence context.
     */
    @Override
    public void setCurrentExtendedPersistenceContexts(final Map<String, EZBExtendedEntityManager> extendedPersistenceContexts) {
        this.currentExtendedPersistenceContexts.set(extendedPersistenceContexts);
    }


}
