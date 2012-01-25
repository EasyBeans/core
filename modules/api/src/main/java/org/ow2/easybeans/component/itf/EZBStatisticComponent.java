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
 * $Id: EZBStatisticComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import java.net.URL;
import java.util.List;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.statistic.EZBStatisticFactory;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;

/**
 * Interface for the EasyBeans statistic component.
 * @author missonng
 */
public interface EZBStatisticComponent extends EZBComponent {
    /**
     * STATISTIC_PROVIDER_MODE define how statistic provider state should be managed.<br>
     * INHERIT : The statistic provider state is inherited from its parent.<br>
     * MANUAL : The statistic provider state is define manually.
     * @author missonng
     */
    public static enum STATISTIC_PROVIDER_MODE {INHERIT, MANUAL}

    /**
     * STATISTIC_FACTORY_MODE define how statistic factory state should be managed.<br>
     * INHERIT : The statistic factory state is inherited from its parent.<br>
     * MANUAL : The statistic factory state is define manually.
     * @author missonng
     */
    public static enum STATISTIC_FACTORY_MODE {INHERIT, MANUAL}

    /**
     * STATISTIC_FACTORY_MODE define how statistic state should be managed.<br>
     * AUTO : The statistic state is the result of a logic AND between its provider state and its factory state.<br>
     * MANUAL : The statistic state is define manually.
     * @author missonng
     */
    public static enum STATISTIC_MODE {AUTO, MANUAL}

    /**
     * Register a new statistic factory.<br>
     * If a statistic factory with the same id is already registered, it will be unregistered first.
     * @param statisticFactory The statistic factory to register.
     */
    void registerStatisticFactory(EZBStatisticFactory statisticFactory);

    /**
     * Unregister a statistic factory.
     * @param statisticFactory The statistic factory to unregister.
     */
    void unregisterStatisticFactory(EZBStatisticFactory statisticFactory);

    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     */
    void registerJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Unregister a J2EE managed object.
     * @param object The J2EE managed object to unregister.
     */
    void unregisterJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Import a new Statistic factory from the given URL.<br>
     * The factory is downloaded, then compiled, then instanciated and finally registered in the component.
     * @param url The file to download.
     * @throws EZBComponentException If an error occurs while downloading, compiling, instanciating, or registering the factory.
     */
    void importStatisticFactory(URL url) throws EZBComponentException;

    /**
     * Get registered statistic factory ids.
     * @return The statistic factory ids.
     * @throws EZBComponentException If an error occurs.
     */
    List<String> getStatisticFactoryIds() throws EZBComponentException;

    /**
     * Get the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory mode.
     * @throws EZBComponentException If an error occurs.
     */
    STATISTIC_FACTORY_MODE getStatisticFactoryMode(final String statisticFactoryId) throws EZBComponentException;

    /**
     * Set the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryMode The statistic factory mode.
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticFactoryMode(final String statisticFactoryId, final STATISTIC_FACTORY_MODE statisticFactoryMode)
        throws EZBComponentException;

    /**
     * Get the statistic factory state for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory state.
     * @throws EZBComponentException If an error occurs.
     */
    boolean getStatisticFactoryState(final String statisticFactoryId) throws EZBComponentException;

    /**
     * Set the statistic factory state for the given statistic factory.<br>
     * The statistic factory mode is automatically set to MANUAL.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryState The statistic factory state
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticFactoryState(final String statisticFactoryId, final boolean statisticFactoryState)
        throws EZBComponentException;

    /**
     * Get registered statistic provider ids.
     * @return The statistic provider ids.
     * @throws EZBComponentException If an error occurs.
     */
    List<String> getStatisticProviderIds() throws EZBComponentException;

    /**
     * Get the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider mode.
     * @throws EZBComponentException If an error occurs.
     */
    STATISTIC_PROVIDER_MODE getStatisticProviderMode(final String statisticProviderId) throws EZBComponentException;

    /**
     * Set the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @param statisticProviderMode The statistic provider mode.
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticProviderMode(final String statisticProviderId, final STATISTIC_PROVIDER_MODE statisticProviderMode)
        throws EZBComponentException;

    /**
     * Get the statistic provider state for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider state.
     * @throws EZBComponentException If an error occurs.
     */
    boolean getStatisticProviderState(final String statisticProviderId) throws EZBComponentException;

    /**
     * Set the statistic provider state for the given statistic provider.<br>
     * The statistic provider mode is automatically set to MANUAL.
     * @param statisticProviderId  The statistic provider id.
     * @param statisticProviderState The statistic provider state.
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticProviderState(final String statisticProviderId, final boolean statisticProviderState)
        throws EZBComponentException;

    /**
     * Get all statistic ids.<br>
     * @return The statistic ids.
     * @throws EZBComponentException If an error occurs.
     */
    List<String> getStatisticIds() throws EZBComponentException;

    /**
     * Get the statistic mode for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic mode.
     * @throws EZBComponentException If an error occurs.
     */
    STATISTIC_MODE getStatisticMode(final String statisticId) throws EZBComponentException;

    /**
     * Set the statistic mode for the given statistic.
     * @param statisticId The statistic id.
     * @param statisticMode The statistic mode.
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticMode(final String statisticId, final STATISTIC_MODE statisticMode) throws EZBComponentException;

    /**
     * Get the statistic state for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic state.
     * @throws EZBComponentException If an error occurs.
     */
    boolean getStatisticState(final String statisticId) throws EZBComponentException;

    /**
     * Set the statistic state for the given statistic.<br>
     * The statistic mode is automatically set to MANUAL.
     * @param statisticId The statistic id.
     * @param statisticStateValue The statistic state.
     * @throws EZBComponentException If an error occurs.
     */
    void setStatisticState(final String statisticId, final boolean statisticStateValue) throws EZBComponentException;

    /**
     * Reset the given statistic.
     * @param statisticId The statistic id to reset.
     * @throws EZBComponentException If an error occurs.
     */
    void resetStatistic(final String statisticId) throws EZBComponentException;

    /**
     * Get the statistic name for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic name.
     * @throws EZBComponentException If an error occurs.
     */
    String getStatisticName(final String statisticId) throws EZBComponentException;

    /**
     * Get the statistic description for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic description.
     * @throws EZBComponentException If an error occurs.
     */
    String getStatisticDescription(final String statisticId) throws EZBComponentException;

    /**
     * Get the statistic value for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic value.
     * @throws EZBComponentException If an error occurs.
     */
    String getStatisticValue(final String statisticId) throws EZBComponentException;

    /**
     * Get the statistic start time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic start time.
     * @throws EZBComponentException If an error occurs.
     */
    long getStatisticStartTime(final String statisticId) throws EZBComponentException;

    /**
     * Get the statistic last sample time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic last sample time.
     * @throws EZBComponentException If an error occurs.
     */
    long getStatisticLastSampleTime(final String statisticId) throws EZBComponentException;
}
