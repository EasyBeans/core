/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: StatisticComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.statistic;

import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.statistic.EZBStatisticFactory;
import org.ow2.easybeans.api.statistic.EZBStatisticProvider;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBJmxComponent;
import org.ow2.easybeans.component.itf.EZBStatisticComponent;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.easybeans.statistic.CallCountStatisticFactory;
import org.ow2.easybeans.statistic.MeanCallTimeStatisticFactory;
import org.ow2.easybeans.statistic.TotalCallTimeStatisticFactory;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Implementation of the EasyBeans event component.
 * @author missonng
 */
public class StatisticComponent implements EZBStatisticComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(StatisticComponent.class);

    /**
     * The event component.
     */
    private EZBEventComponent eventComponent;

    /**
     * The JMX component.
     */
    private EZBJmxComponent jmxComponent;

    /**
     * The managed statistics map.
     */
    private Map<String, ManagedStatistic> managedStatistics = new HashMap<String, ManagedStatistic>();

    /**
     * The managed statistic providers map.
     */
    private Map<String, ManagedStatisticProvider> managedStatisticProviders = new HashMap<String, ManagedStatisticProvider>();

    /**
     * The managed statistic factories map.
     */
    private Map<String, ManagedStatisticFactory> managedStatisticFactories = new HashMap<String, ManagedStatisticFactory>();

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public synchronized void init() throws EZBComponentException {

    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public synchronized void start() throws EZBComponentException {
        // register core factories
        registerStatisticFactory(new CallCountStatisticFactory(this.eventComponent, this.jmxComponent));
        registerStatisticFactory(new MeanCallTimeStatisticFactory(this.eventComponent, this.jmxComponent));
        registerStatisticFactory(new TotalCallTimeStatisticFactory(this.eventComponent, this.jmxComponent));

        try {
            MBeansHelper.getInstance().registerMBean(this);
        } catch (MBeansException e) {
            // Nothing to do.
        }
        logger.info("Statistics component started with factories: " + this.managedStatisticFactories.keySet());
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public synchronized void stop() throws EZBComponentException {
        // unregister all factories
        List<ManagedStatisticFactory> managedStatisticFactories = new LinkedList<ManagedStatisticFactory>();
        managedStatisticFactories.addAll(this.managedStatisticFactories.values());
        for (ManagedStatisticFactory managedStatisticFactory : managedStatisticFactories) {
            managedStatisticFactory.destroy();
        }

        // unregister all providers
        List<ManagedStatisticProvider> managedStatisticProviders = new LinkedList<ManagedStatisticProvider>();
        managedStatisticProviders.addAll(this.managedStatisticProviders.values());
        for (ManagedStatisticProvider managedStatisticProvider : managedStatisticProviders) {
            managedStatisticProvider.destroy();
        }

        this.managedStatisticProviders.clear();
        this.managedStatisticFactories.clear();
        this.managedStatistics.clear();
        logger.info("Stopped");
    }

    /**
     * Set the event component.
     * @param eventComponent The event component.
     */
    public synchronized void setEventComponent(final EZBEventComponent eventComponent) {
        this.eventComponent = eventComponent;
    }

    /**
     * Set the jmx component.
     * @param jmxComponent The jmx component.
     */
    public synchronized void setJmxComponent(final EZBJmxComponent jmxComponent) {
        this.jmxComponent = jmxComponent;
    }

    /**
     * Register a new statistic factory.<br>
     * If a statistic factory with the same id is already registered, it will be
     * unregistered first.
     * @param statisticFactory The statistic factory to register.
     */
    public synchronized void registerStatisticFactory(final EZBStatisticFactory statisticFactory) {
        unregisterStatisticFactory(statisticFactory);

        ManagedStatisticFactory managedStatisticFactory = new ManagedStatisticFactory(statisticFactory,
                this.managedStatisticProviders, this.managedStatisticFactories);

        this.managedStatisticFactories.put(statisticFactory.getStatisticFactoryId(), managedStatisticFactory);

        for (ManagedStatistic managedStatistic : managedStatisticFactory.getManagedStatistics()) {
            this.managedStatistics.put(managedStatistic.getStatistic().getStatisticId(), managedStatistic);
        }
    }

    /**
     * Unregister a statistic factory.
     * @param statisticFactory The statistic factory to unregister.
     */
    public synchronized void unregisterStatisticFactory(final EZBStatisticFactory statisticFactory) {
        ManagedStatisticFactory managedStatisticFactory = this.managedStatisticFactories.remove(statisticFactory
                .getStatisticFactoryId());

        if (managedStatisticFactory != null) {
            for (ManagedStatistic managedStatistic : managedStatisticFactory.getManagedStatistics()) {
                this.managedStatistics.remove(managedStatistic.getStatistic().getStatisticId());
            }

            managedStatisticFactory.destroy();
        }
    }

    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     */
    public synchronized void registerJ2EEManagedObject(final EZBJ2EEManagedObject object) {
        if (EZBServer.class.isAssignableFrom(object.getClass())) {
            registerEZBServer((EZBServer) object);
        } else if (EZBContainer.class.isAssignableFrom(object.getClass())) {
            registerEZBContainer((EZBContainer) object);
        } else if (Factory.class.isAssignableFrom(object.getClass())) {
            registerEZBFactory((Factory<?, ?>) object);
        }
    }

    /**
     * Unregister a J2EE managed object.
     * @param object The J2EE managed object to unregister.
     */
    public synchronized void unregisterJ2EEManagedObject(final EZBJ2EEManagedObject object) {
        if (EZBServer.class.isAssignableFrom(object.getClass())) {
            unregisterEZBServer((EZBServer) object);
        } else if (EZBContainer.class.isAssignableFrom(object.getClass())) {
            unregisterEZBContainer((EZBContainer) object);
        } else if (Factory.class.isAssignableFrom(object.getClass())) {
            unregisterEZBFactory((Factory<?, ?>) object);
        }
    }

    /**
     * Import a new Statistic factory from the given URL.<br>
     * The factory is downloaded, then compiled, then instanciated and finally
     * registered in the component.
     * @param url The file to download.
     * @throws EZBComponentException If an error occurs while downloading,
     *         compiling, instanciating, or registering the factory.
     */
    public synchronized void importStatisticFactory(final URL url) throws EZBComponentException {
        throw new EZBComponentException("Function not yet implemented");
    }

    /**
     * Get registered statistic factory ids.
     * @return The statistic factory ids.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized List<String> getStatisticFactoryIds() throws EZBComponentException {
        return new LinkedList<String>(this.managedStatisticFactories.keySet());
    }

    /**
     * Get the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized STATISTIC_FACTORY_MODE getStatisticFactoryMode(final String statisticFactoryId)
            throws EZBComponentException {
        ManagedStatisticFactory managedStatisticFactory = this.managedStatisticFactories.get(statisticFactoryId);

        if (managedStatisticFactory == null) {
            throw new EZBComponentException("Cannot find statisticFactoryId " + statisticFactoryId);
        }

        return managedStatisticFactory.getManagedStatisticFactoryMode();
    }

    /**
     * Set the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryMode The statistic factory mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticFactoryMode(final String statisticFactoryId,
            final STATISTIC_FACTORY_MODE statisticFactoryMode) throws EZBComponentException {
        ManagedStatisticFactory managedStatisticFactory = this.managedStatisticFactories.get(statisticFactoryId);

        if (managedStatisticFactory == null) {
            throw new EZBComponentException("Cannot find statisticFactoryId " + statisticFactoryId);
        }

        managedStatisticFactory.setManagedStatisticFactoryMode(statisticFactoryMode);
    }

    /**
     * Get the statistic factory state for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory state.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized boolean getStatisticFactoryState(final String statisticFactoryId) throws EZBComponentException {
        ManagedStatisticFactory managedStatisticFactory = this.managedStatisticFactories.get(statisticFactoryId);

        if (managedStatisticFactory == null) {
            throw new EZBComponentException("Cannot find statisticFactoryId " + statisticFactoryId);
        }

        return managedStatisticFactory.getManagedStatisticFactoryState();
    }

    /**
     * Set the statistic factory state for the given statistic factory.<br>
     * The statistic factory mode is automatically set to MANUAL.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryState The statistic factory state
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticFactoryState(final String statisticFactoryId, final boolean statisticFactoryState)
            throws EZBComponentException {
        ManagedStatisticFactory managedStatisticFactory = this.managedStatisticFactories.get(statisticFactoryId);

        if (managedStatisticFactory == null) {
            throw new EZBComponentException("Cannot find statisticFactoryId " + statisticFactoryId);
        }

        managedStatisticFactory.setManagedStatisticFactoryState(statisticFactoryState);
    }

    /**
     * Get registered statistic provider ids.
     * @return The statistic provider ids.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized List<String> getStatisticProviderIds() throws EZBComponentException {
        return new LinkedList<String>(this.managedStatisticProviders.keySet());
    }

    /**
     * Get the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized STATISTIC_PROVIDER_MODE getStatisticProviderMode(final String statisticProviderId)
            throws EZBComponentException {
        ManagedStatisticProvider managedStatisticProvider = this.managedStatisticProviders.get(statisticProviderId);

        if (managedStatisticProvider == null) {
            throw new EZBComponentException("Cannot find statisticProviderId " + statisticProviderId);
        }

        return managedStatisticProvider.getManagedStatisticProviderMode();
    }

    /**
     * Set the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @param statisticProviderMode The statistic provider mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticProviderMode(final String statisticProviderId,
            final STATISTIC_PROVIDER_MODE statisticProviderMode) throws EZBComponentException {
        ManagedStatisticProvider managedStatisticProvider = this.managedStatisticProviders.get(statisticProviderId);

        if (managedStatisticProvider == null) {
            throw new EZBComponentException("Cannot find statisticProviderId " + statisticProviderId);
        }

        managedStatisticProvider.setManagedStatisticProviderMode(statisticProviderMode);
    }

    /**
     * Get the statistic provider state for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider state.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized boolean getStatisticProviderState(final String statisticProviderId) throws EZBComponentException {
        ManagedStatisticProvider managedStatisticProvider = this.managedStatisticProviders.get(statisticProviderId);

        if (managedStatisticProvider == null) {
            throw new EZBComponentException("Cannot find statisticProviderId " + statisticProviderId);
        }

        return managedStatisticProvider.getManagedStatisticProviderState();
    }

    /**
     * Set the statistic provider state for the given statistic provider.<br>
     * The statistic provider mode is automatically set to MANUAL.
     * @param statisticProviderId The statistic provider id.
     * @param statisticProviderState The statistic provider state.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticProviderState(final String statisticProviderId, final boolean statisticProviderState)
            throws EZBComponentException {
        ManagedStatisticProvider managedStatisticProvider = this.managedStatisticProviders.get(statisticProviderId);

        if (managedStatisticProvider == null) {
            throw new EZBComponentException("Cannot find statisticProviderId " + statisticProviderId);
        }

        managedStatisticProvider.setManagedStatisticProviderState(statisticProviderState);
    }

    /**
     * Get all statistic ids.
     * @return The statistic ids.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized List<String> getStatisticIds() throws EZBComponentException {
        return new LinkedList<String>(this.managedStatistics.keySet());
    }

    /**
     * Get the statistic mode for the given statistic.
     * @param statisticId The statistic.
     * @return The statistic mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized STATISTIC_MODE getStatisticMode(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getManagedStatisticMode();
    }

    /**
     * Set the statistic mode for the given statistic.
     * @param statisticId The statistic.
     * @param statisticMode The statistic mode.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticMode(final String statisticId, final STATISTIC_MODE statisticMode)
            throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        managedStatistic.setManagedStatisticMode(statisticMode);
    }

    /**
     * Get the statistic state for the given statistic.
     * @param statisticId The statistic.
     * @return The statistic state.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized boolean getStatisticState(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getManagedStatisticState();
    }

    /**
     * Set the statistic state for the given statistic.<br>
     * The statistic mode is automatically set to MANUAL.
     * @param statisticId The statistic.
     * @param statisticStateValue The statistic state.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void setStatisticState(final String statisticId, final boolean statisticStateValue)
            throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        managedStatistic.setManagedStatisticState(statisticStateValue);
    }

    /**
     * Reset the given statistic.
     * @param statisticId The statistic to reset.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized void resetStatistic(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        managedStatistic.getStatistic().reset();
    }

    /**
     * Get the statistic name for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic name.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized String getStatisticName(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getStatistic().getName();
    }

    /**
     * Get the statistic description for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic description.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized String getStatisticDescription(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getStatistic().getDescription();
    }

    /**
     * Get the statistic value for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic value.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized String getStatisticValue(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getStatistic().getValue();
    }

    /**
     * Get the statistic start time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic start time.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized long getStatisticStartTime(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getStatistic().getStartTime();
    }

    /**
     * Get the statistic last sample time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic last sample time.
     * @throws EZBComponentException If an error occurs.
     */
    public synchronized long getStatisticLastSampleTime(final String statisticId) throws EZBComponentException {
        ManagedStatistic managedStatistic = this.managedStatistics.get(statisticId);

        if (managedStatistic == null) {
            throw new EZBComponentException("Cannot find statistic " + statisticId);
        }

        return managedStatistic.getStatistic().getLastSampleTime();
    }

    /**
     * Helper method to register a EZBServer.
     * @param server The EZBServer to register.
     */
    private void registerEZBServer(final EZBServer server) {
        unregisterEZBServer(server);

        ManagedStatisticProvider managedStatisticProvider = new ManagedStatisticProvider(new EZBStatisticProvider() {
            public String getStatisticProviderId() {
                return server.getJ2EEManagedObjectId();
            }
        }, this.managedStatisticProviders, this.managedStatisticFactories);

        this.managedStatisticProviders.put(server.getJ2EEManagedObjectId(), managedStatisticProvider);

        for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
            this.managedStatistics.put(managedStatistic.getStatistic().getStatisticId(), managedStatistic);
        }
    }

    /**
     * Helper method to unregister a EZBServer.
     * @param server The EZBServer to unregister.
     */
    private void unregisterEZBServer(final EZBServer server) {
        ManagedStatisticProvider managedStatisticProvider =
            this.managedStatisticProviders.remove(server.getJ2EEManagedObjectId());

        if (managedStatisticProvider != null) {
            for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
                this.managedStatistics.remove(managedStatistic.getStatistic().getStatisticId());
            }

            managedStatisticProvider.destroy();
        }
    }

    /**
     * Helper method to register a EZBContainer.
     * @param container The EZBContainer to register.
     */
    private void registerEZBContainer(final EZBContainer container) {
        unregisterEZBContainer(container);

        ManagedStatisticProvider managedStatisticProvider = new ManagedStatisticProvider(new EZBStatisticProvider() {
            public String getStatisticProviderId() {
                return container.getJ2EEManagedObjectId();
            }
        }, this.managedStatisticProviders, this.managedStatisticFactories);

        this.managedStatisticProviders.put(container.getJ2EEManagedObjectId(), managedStatisticProvider);

        for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
            this.managedStatistics.put(managedStatistic.getStatistic().getStatisticId(), managedStatistic);
        }
    }

    /**
     * Helper method to unregister a EZBContainer.
     * @param container The EZBContainer to unregister.
     */
    private void unregisterEZBContainer(final EZBContainer container) {
        ManagedStatisticProvider managedStatisticProvider =
            this.managedStatisticProviders.remove(container.getJ2EEManagedObjectId());

        if (managedStatisticProvider != null) {
            for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
                this.managedStatistics.remove(managedStatistic.getStatistic().getStatisticId());
            }

            managedStatisticProvider.destroy();
        }
    }

    /**
     * Helper method to register a Factory.
     * @param factory The Factory to register.
     */
    private void registerEZBFactory(final Factory<?, ?> factory) {
        unregisterEZBFactory(factory);

        ManagedStatisticProvider managedStatisticProvider = new ManagedStatisticProvider(new EZBStatisticProvider() {
            public String getStatisticProviderId() {
                return factory.getJ2EEManagedObjectId();
            }
        }, this.managedStatisticProviders, this.managedStatisticFactories);

        this.managedStatisticProviders.put(factory.getJ2EEManagedObjectId(), managedStatisticProvider);

        for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
            this.managedStatistics.put(managedStatistic.getStatistic().getStatisticId(), managedStatistic);
        }

        List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
        for (final String method : methods) {
            managedStatisticProvider = new ManagedStatisticProvider(new EZBStatisticProvider() {
                public String getStatisticProviderId() {
                    return method;
                }
            }, this.managedStatisticProviders, this.managedStatisticFactories);

            this.managedStatisticProviders.put(method, managedStatisticProvider);

            for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
                this.managedStatistics.put(managedStatistic.getStatistic().getStatisticId(), managedStatistic);
            }
        }
    }

    /**
     * Helper method to unregister a Factory.
     * @param factory The Factory to unregister.
     */
    private void unregisterEZBFactory(final Factory<?, ?> factory) {
        ManagedStatisticProvider managedStatisticProvider =
            this.managedStatisticProviders.remove(factory.getJ2EEManagedObjectId());

        if (managedStatisticProvider != null) {
            for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
                this.managedStatistics.remove(managedStatistic.getStatistic().getStatisticId());
            }

            managedStatisticProvider.destroy();

            List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
            for (String method : methods) {
                managedStatisticProvider =  this.managedStatisticProviders.remove(method);

                if (managedStatisticProvider != null) {
                    for (ManagedStatistic managedStatistic : managedStatisticProvider.getManagedStatistics()) {
                        this.managedStatistics.remove(managedStatistic.getStatistic().getStatisticId());
                    }

                    managedStatisticProvider.destroy();
                }
            }
        }
    }
}
