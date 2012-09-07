/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id: AbsFactory.java 5650 2010-11-04 14:50:58Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.resource.spi.work.WorkManager;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBTimerService;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.audit.EZBAuditComponent;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.api.bean.info.ITimerInfo;
import org.ow2.easybeans.api.components.EZBComponentRegistry;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationBegin;
import org.ow2.easybeans.api.injection.EasyBeansInjectionException;
import org.ow2.easybeans.api.injection.ResourceInjector;
import org.ow2.easybeans.api.interceptor.EZBInvocationContextFactory;
import org.ow2.easybeans.api.pool.EZBManagementPool;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBStatisticComponent;
import org.ow2.easybeans.component.itf.EZBWorkManagerComponent;
import org.ow2.easybeans.component.itf.TimerComponent;
import org.ow2.easybeans.event.bean.EventBeanInvocationBegin;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.naming.NamingManager;
import org.ow2.easybeans.naming.interceptors.ENCManager;
import org.ow2.easybeans.rpc.api.EJBRemoteRequest;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.util.Hash;
import org.ow2.easybeans.security.api.EZBSecurityContext;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.auditreport.api.ICurrentInvocationID;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.Pool;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.enhanced.ReusableThreadPoolFactory;
import org.ow2.util.pool.impl.enhanced.api.IPool;
import org.ow2.util.pool.impl.enhanced.api.thread.IReusableThread;

/**
 * Abstract factory which implements common and defaults methods.<br>
 * It should be extended by Bean factories.
 * @param <PoolType> the type of bean instance.
 * @author Florent Benoit
 */
public abstract class AbsFactory<PoolType extends EasyBeansBean> implements Factory<PoolType, Long> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(AbsFactory.class);

    /**
     * Old pool flag ?
     */
    protected static final String OLD_POOL = "easybeans.useSimplePool";

    /**
     * Waiting time if creation of an instance is failing.
     */
    protected static final Long WAITING_TIME_BEFORE_CREATION = Long.valueOf(1000L);

    /**
     * Limit for management pool using work manager.
     */
    private static final int MANAGEMENTPOOL_WORKMANAGER_LIMIT = 30;

    /**
     * Number of threads for management pool if there is no work manager.
     */
    private static final int MANAGEMENTPOOL_THREAD_MAX = 15;

    /**
     * Name of the class of the managed bean.
     */
    private String className = null;

    /**
     * Container that created this factory.
     */
    private EZBContainer container = null;

    /**
     * Pool that manage beans instance.
     */
    private Pool<PoolType, Long> pool = null;

    /**
     * Class used to build bean's instance.
     */
    private Class<PoolType> beanClass = null;

    /**
     * Context for java: lookups.
     */
    private Context javaContext = null;

    /**
     * Reference on the naming manager.
     */
    private NamingManager namingManager = null;

    /**
     * List of external Resources injectors.
     */
    private List<ResourceInjector> injectors = null;

    /**
     * Keep a direct reference to the method so that we don't need to compute
     * each time the method object to invoke.<br>
     * http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html
     */
    private Map<Long, Method> hashes = null;

    /**
     * Keep a direct reference to the methodInfo object so that we don't need to compute
     * each time the method object to invoke.<br>
     */
    private Map<Long, IMethodInfo> methodInfoHashes = null;

    /**
     * Id of this container.
     */
    private String id = null;

    /**
     * Timer Service for this factory.
     */
    private EZBTimerService timerService = null;

    /**
     * The event dispatcher.
     */
    private IEventDispatcher dispatcher = null;

    /**
     * The provider id.
     */
    private String j2eeManagedObjectId = null;

    /**
     * Factory started or stopped ?
     */
    private boolean started = false;

    /**
     * Invocation context factory.
     */
    private EZBInvocationContextFactory invocationContextFactory = null;

    /**
     * Current invocation ID (if audit component is available).
     */
    private ICurrentInvocationID currentInvocationID = null;

    /**
     * Inherited Local thread used to keep the operation executed by the container.
     */
    private static InheritableThreadLocal<OperationState> operationStateThreadLocal
        = new InheritableThreadLocal<OperationState>();

    /**
     * Inherited Local thread used to keep the Context data.
     */
    private InheritableThreadLocal<Map<String, Object>> contextDataThreadLocal;

    /**
     * Timer initialized.
     */
    private volatile boolean timersInitialized = false;

    /**
     *  The timer component
     */
    private TimerComponent timerComponent;

    /**
     * Builds a new factory with a given name and its container.
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @throws FactoryException if class can't be loaded.
     */
    @SuppressWarnings("unchecked")
    public AbsFactory(final String className, final EZBContainer container) throws FactoryException {
        this.className = className;
        this.container = container;
        this.id = String.valueOf(System.identityHashCode(this));
        this.j2eeManagedObjectId = J2EEManagedObjectNamingHelper.getJ2EEManagedObjectId(this);
        this.contextDataThreadLocal = new InheritableThreadLocal<Map<String, Object>>();

        Class clazz = null;
        try {
            clazz = getContainer().getClassLoader().loadClass(getClassName());
        } catch (ClassNotFoundException e) {
            throw new FactoryException("Cannot load the class for class name '" + getClassName() + "'", e);
        }
        setBeanClass(clazz);
        setHashes(Hash.hashClass(clazz));
        this.methodInfoHashes = new HashMap<Long, IMethodInfo>();

        // Use the container event dispatcher.
        this.dispatcher = ((JContainer3) this.container).getEventDispatcher();

        try {
            this.namingManager = NamingManager.getInstance();
        } catch (NamingException e) {
            throw new FactoryException("Cannot get instance of the naming manager", e);
        }

        this.injectors = container.getConfiguration().getInjectors();

        // Get a timer service if a timer component is present.
        EZBComponentRegistry registry = container.getConfiguration().getEZBServer().getComponentManager().getComponentRegistry();
        List<TimerComponent> timerComponents = registry.getComponents(TimerComponent.class);

        // use the first one if there is at least once
        if (timerComponents.size() > 0) {
            timerComponent = timerComponents.get(0);
            if (timerComponents.size() > 1) {
                logger.warn("There are {0} timer components running on this server. Only the first one will be used",
                        Integer.valueOf(timerComponents.size()));
            }

            // Build a new timer service
            this.timerService = timerComponent.getTimerService(this);
        }

    }

    /**
     * Callback called when object is gonna be removed.
     * @param instance that is being removed from the pool.
     */
    public void remove(final PoolType instance) {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getContainer().getClassLoader());
        try {
            // call callbacks
            preDestroy(instance);
        } catch (Exception e) {
            logger.error("Could not complete preDestroy method on instance", e);
        } finally {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

        // I need an extension point here for desinjection
        // call external resources injectors
        for (ResourceInjector injector : this.injectors) {
            try {
                injector.postEasyBeansDestroy(instance);
            } catch (Throwable t) {
                // Protection from malicious code
                logger.error("postEasyBeansDestroy() for {0} failed", injector.getClass().getName(), t);
            }
        }

        // Cleanup references of the instance
        instance.easyBeansCleanup();

    }

    /**
     * Injects Resources into the Bean.
     * @param instance The Bean instance to be injected.
     * @throws PoolException if resources cannot be injected.
     */
    protected void injectResources(final PoolType instance) throws PoolException {

        // call external resources injectors
        for (ResourceInjector injector : this.injectors) {
            try {
                injector.preEasyBeansInject(instance);
            } catch (Throwable t) {
                // Protection from malicious code
                logger.error("preEasyBeansInject() for {0} failed", injector.getClass().getName(), t);
            }
        }

        // Call dependency injection
        OperationState oldState = getOperationState();
        getOperationStateThreadLocal().set(OperationState.DEPENDENCY_INJECTION);
        try {
            instance.injectedByEasyBeans();
        } catch (EasyBeansInjectionException e) {
            throw new PoolException("Cannot inject resources in the created bean", e);
        } catch (RuntimeException e) {
            logger.error("Error while calling injectedByEasyBeans() method on instance {0} failed", instance, e);
            throw new PoolException("Cannot inject resources in the created bean", e);
        } finally {
            getOperationStateThreadLocal().set(oldState);
        }

        // call external resources injectors
        for (ResourceInjector injector : this.injectors) {
            try {
                injector.postEasyBeansInject(instance);
            } catch (Throwable t) {
                // Protection from malicious code
                logger.error("postEasyBeansInject() for {0} failed", injector.getClass().getName(), t);
            }
        }
    }

    /**
     * Call the predestroy lifecycle interceptors on the given instance.
     * @param instance the given instance
     */
    protected void preDestroy(final PoolType instance) {
        OperationState oldState = getOperationState();
        getOperationStateThreadLocal().set(OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR);
        try {
            instance.preDestroyEasyBeansLifeCycle();
        } finally {
            getOperationStateThreadLocal().set(oldState);
        }
    }

    /**
     * Call the postconstruct lifecycle interceptors on the given instance.
     * @param instance the given instance
     */
    protected synchronized void postConstruct(final PoolType instance) {

        try {
            // We perform only once the timer initialization (as we need one by bean class and not by bean instance)
            if (!this.timersInitialized) {
                this.timersInitialized = true;
                // Before calling postconstruct, we initialize the timers.
                // Do we have Schedule timers ?
                List<ITimerInfo> timersInfo = getBeanInfo().getTimersInfo();

                // Do we have timers ?
                if (timersInfo != null && timersInfo.size() > 0) {
                    for (ITimerInfo timerInfo : timersInfo) {
                        // Get Timer Service
                        EZBTimerService timerService = getTimerService();

                        // Schedule the timer
                        timerService.createCalendarTimer(timerInfo.getScheduleExpression(), timerInfo.getTimerConfig(), timerInfo
                                .getMethodInfo());
                    }
                }
            }

        } finally {
            OperationState oldState = getOperationState();
            getOperationStateThreadLocal().set(OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR);
            try {
                instance.postConstructEasyBeansLifeCycle();
            } finally {
                getOperationStateThreadLocal().set(oldState);
            }
        }
    }

    /**
     * Gets the computed hashes.
     * @return computed hashes
     */
    protected Map<Long, Method> getHashes() {
        return this.hashes;
    }

    /**
     * Gets the computed method info hashes.
     * @return computed hashes
     */
    protected Map<Long, IMethodInfo> getMethodInfoHashes() {
        return this.methodInfoHashes;
    }

    /**
     * Sets the hashes for the current bean class.
     * @param hashes method hashes computed as RMI hashes
     */
    protected void setHashes(final Map<Long, Method> hashes) {
        this.hashes = hashes;
    }

    /**
     * Gets the java: context.
     * @return java: context.
     */
    public Context getJavaContext() {
        return this.javaContext;
    }

    /**
     * Sets the java: context.
     * @param javaContext the java: context.
     */
    public void setJavaContext(final Context javaContext) {
        // Can be only set once
        if (this.javaContext != null) {
            throw new IllegalStateException("The javaContext can only be set once. Already set !");
        }
        this.javaContext = javaContext;

        // Set the javaContext used for ENC
        ENCManager.initContext(this, javaContext);
    }

    /**
     * Gets the bean's class.
     * @return bean class used to instantiate beans.
     */
    public Class<PoolType> getBeanClass() {
        return this.beanClass;
    }

    /**
     * Sets the bean class that will be used to build bean's instance.
     * @param beanClass the instance of the bean class name
     */
    protected void setBeanClass(final Class<PoolType> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Sets the pool used by this factory.
     * @param pool the pool which managed bean instances
     */
    protected void setPool(final Pool<PoolType, Long> pool) {
        this.pool = pool;
    }

    /**
     * Gets the container used by this factory.
     * @return container of this factory
     */
    public EZBContainer getContainer() {
        return this.container;
    }

    /**
     * Gets the className used by this factory.
     * @return classname that will be instantiated to build bean instance.
     */
    public String getClassName() {
        return this.className;
    }

    /**
     * Gets the reference on the naming manager.
     * @return the reference on the naming manager.
     */
    protected NamingManager getNamingManager() {
        return this.namingManager;
    }


    /**
     * Gets the pool used by this factory.
     * @return pool.
     */
    public Pool<PoolType, Long> getPool() {
        return this.pool;
    }

    /**
     * A request comes to the bean factory and needs to be handled.<br>
     * A response is done which contains the answer.
     * @param request the EJB request.
     * @return a response that have been processed by the factory.
     */
    public abstract EJBResponse rpcInvoke(final EJBRemoteRequest request);


    /**
     * Init the factory.
     * @throws FactoryException if the initialization fails.
     */
    public void init() throws FactoryException {
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

        EZBAuditComponent auditComponent = getComponent(EZBAuditComponent.class);
        if (auditComponent != null) {
            auditComponent.registerJ2EEManagedObject(this);
            this.currentInvocationID = auditComponent.getCurrentInvocationID();
        }

        // Init Method Info hashes
        List<IMethodInfo> methodInfos = getBeanInfo().getBusinessMethodsInfo();
        for (IMethodInfo methodInfo : methodInfos) {
            long hashTempMethod = Hash.hashMethod(methodInfo.getName(), methodInfo.getDescriptor());
            this.methodInfoHashes.put(Long.valueOf(hashTempMethod), methodInfo);
        }
    }

    /**
     * Start the factory.
     * @throws FactoryException if the startup fails.
     */
    public void start() throws FactoryException {
        // started
        this.started = true;
    }


    /**
     * Gets the id of this container.
     * @return string id.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Stops the factory.
     */
    public void stop() {
        ENCManager.removeContext(this);

        // Unregister from statistic component.
        EZBStatisticComponent statisticComponent = getComponent(EZBStatisticComponent.class);
        if (statisticComponent != null) {
            statisticComponent.unregisterJ2EEManagedObject(this);
        }

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

        // Unref the event dispatcher.
        this.dispatcher = null;

        // Stopped
        this.started = false;
    }


    /**
     * Gets the timer service of this factory.
     * @return the timer service.
     */
    public EZBTimerService getTimerService() {
        return this.timerService;
    }

    /**
     * Gets the timer component.
     * @return the timer component.
     */
    protected TimerComponent getTimerComponent() {
        return this.timerComponent;
    }

    /**
     * Get a reference to the first component matching the interface.
     * @param <T> The interface type.
     * @param itf The interface class.
     * @return The component.
     */
    public <T extends EZBComponent> T getComponent(final Class<T> itf) {
        return getContainer().getComponent(itf);
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
     * Defines the invocation context factory (for dynamic mode).
     * @param invocationContextFactory the given invocation context factory
     */
    public void setInvocationContextFactory(final EZBInvocationContextFactory invocationContextFactory) {
        this.invocationContextFactory = invocationContextFactory;
    }

    /**
     * @return the invocation context factory (for dynamic mode).
     */
    public EZBInvocationContextFactory getInvocationContextFactory() {
        return this.invocationContextFactory;
    }

    /**
     * @return the current operation state.
     */
    public OperationState getOperationState() {
        return AbsFactory.operationStateThreadLocal.get();
    }

    /**
     * @return the current operation state thread local.
     */
    public InheritableThreadLocal<OperationState> getOperationStateThreadLocal() {
        return AbsFactory.operationStateThreadLocal;
    }

    /**
     * @return a new management pool (or an existing one if data are present on the server)
     */
    protected synchronized ManagementPool getManagementPool() {

        IPool<IReusableThread> managementReusablePool = null;

        // Check if a server instance is available
        EZBManagementPool existingManagementPool = getContainer().getConfiguration().getEZBServer().getManagementThreadPool();

        // Instance found, check if the instance is one of the known type
        if (existingManagementPool != null) {
            if (existingManagementPool instanceof ManagementPool) {
                return ((ManagementPool) existingManagementPool);
            }
        }

        // Instance was not found, is there a work manager ?
        WorkManager workManager = getContainer().getConfiguration().getEZBServer().getComponent(EZBWorkManagerComponent.class);

        // There is a work manager
        if (workManager != null) {
            managementReusablePool = ReusableThreadPoolFactory.createWorkManagerThreadPool(workManager,
                    MANAGEMENTPOOL_WORKMANAGER_LIMIT);
        } else {
            // build its own management thread pool
            managementReusablePool = ReusableThreadPoolFactory.createManagementThreadPool(MANAGEMENTPOOL_THREAD_MAX);
        }

        // Sets the new instance on the server
        ManagementPool managementPool = new ManagementPool(managementReusablePool);

        getContainer().getConfiguration().getEZBServer().setManagementThreadPool(managementPool);

        // return it
        return managementPool;

    }


    /**
     * @return true if the factory is started, else false.
     */
    public boolean isStarted() {
        return this.started;
    }

    /**
     * @param source the given source of the event
     * @param params the method params
     * @return an invocation begin event.
     */
    public EZBEventBeanInvocationBegin getInvocationEventBegin(final String source, final Object[] params) {
        // Get current Security Context
        EZBSecurityContext securityContext = SecurityCurrent.getCurrent().getSecurityContext();
        boolean runAsMode = getBeanInfo().getSecurityInfo().getRunAsRole() != null;
        EventBeanInvocationBegin event = new EventBeanInvocationBegin(source, params, securityContext, runAsMode);
        event.setStackTraceElements(Thread.currentThread().getStackTrace());

        // If audit ID propagation enabled, add current ID to the event
        if (this.currentInvocationID != null) {
            IAuditID id = this.currentInvocationID.getAuditID();
            if (id != null) {
                // Store value in event
                event.setKeyID(id.getID());
            }
        }

        return event;
    }

    /**
     * @return the current invocation ID.
     */
    public ICurrentInvocationID getCurrentInvocationID() {
        return this.currentInvocationID;
    }

    /**
     * @return the current context map of the current invocation
     */
    public InheritableThreadLocal<Map<String, Object>> getContextDataThreadLocal() {
        return this.contextDataThreadLocal;
    }


}
