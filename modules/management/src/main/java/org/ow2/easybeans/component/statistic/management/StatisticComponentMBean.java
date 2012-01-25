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
 * $Id: StatisticComponentMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.statistic.management;

import java.util.List;

import javax.management.MBeanException;

import org.apache.commons.modeler.BaseModelMBean;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_FACTORY_MODE;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_MODE;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_PROVIDER_MODE;
import org.ow2.easybeans.component.statistic.StatisticComponent;

/**
 * StatisticComponentMBean MBean Base.
 * @author missonng
 */
public class StatisticComponentMBean extends BaseModelMBean {
    /**
     * Create the mbean.
     * @throws MBeanException if the super constructor fails.
     */
    public StatisticComponentMBean() throws MBeanException {
        super();
    }

    /**
     * Get the statistic component.
     * @return The statistic component.
     */
    private StatisticComponent getStatisticComponent() {
        try {
            return (StatisticComponent) getManagedResource();
        } catch (Throwable error) {
            return null;
        }
    }

    /**
     * Get registered statistic factory ids.
     * @return The statistic factory ids.
     * @throws MBeanException If an error occurs.
     */
    public List<String> getStatisticFactoryIds() throws MBeanException {
        try {
            return getStatisticComponent().getStatisticFactoryIds();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory mode.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticFactoryMode(final String statisticFactoryId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticFactoryMode(statisticFactoryId).toString();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic factory mode for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryMode The statistic factory mode.
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticFactoryMode(final String statisticFactoryId, final String statisticFactoryMode)
            throws MBeanException {
        try {
            getStatisticComponent().setStatisticFactoryMode(statisticFactoryId,
                    STATISTIC_FACTORY_MODE.valueOf(statisticFactoryMode));
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic factory state for the given statistic factory.
     * @param statisticFactoryId The statistic factory id.
     * @return The statistic factory state.
     * @throws MBeanException If an error occurs.
     */
    public boolean getStatisticFactoryState(final String statisticFactoryId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticFactoryState(statisticFactoryId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic factory state for the given statistic factory.<br>
     * The statistic factory mode is automatically set to MANUAL.
     * @param statisticFactoryId The statistic factory id.
     * @param statisticFactoryState The statistic factory state
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticFactoryState(final String statisticFactoryId, final boolean statisticFactoryState)
            throws MBeanException {
        try {
            getStatisticComponent().setStatisticFactoryState(statisticFactoryId, statisticFactoryState);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get registered statistic provider ids.
     * @return The statistic provider ids.
     * @throws MBeanException If an error occurs.
     */
    public List<String> getStatisticProviderIds() throws MBeanException {
        try {
            return getStatisticComponent().getStatisticProviderIds();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider mode.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticProviderMode(final String statisticProviderId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticProviderMode(statisticProviderId).toString();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic provider mode for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @param statisticProviderMode The statistic provider mode.
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticProviderMode(final String statisticProviderId, final String statisticProviderMode)
            throws MBeanException {
        try {
            getStatisticComponent().setStatisticProviderMode(statisticProviderId,
                    STATISTIC_PROVIDER_MODE.valueOf(statisticProviderMode));
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic provider state for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The statistic provider state.
     * @throws MBeanException If an error occurs.
     */
    public boolean getStatisticProviderState(final String statisticProviderId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticProviderState(statisticProviderId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic provider state for the given statistic provider.<br>
     * The statistic provider mode is automatically set to MANUAL.
     * @param statisticProviderId The statistic provider id.
     * @param statisticProviderState The statistic provider state.
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticProviderState(final String statisticProviderId, final boolean statisticProviderState)
            throws MBeanException {
        try {
            getStatisticComponent().setStatisticProviderState(statisticProviderId, statisticProviderState);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get all statistic ids.<br>
     * @return The statistic ids.
     * @throws MBeanException If an error occurs.
     */
    public List<String> getStatisticIds() throws MBeanException {
        try {
            return getStatisticComponent().getStatisticIds();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic mode for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic mode.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticMode(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticMode(statisticId).toString();
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic mode for the given statistic.
     * @param statisticId The statistic id.
     * @param statisticMode The statistic mode.
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticMode(final String statisticId, final String statisticMode) throws MBeanException {
        try {
            getStatisticComponent().setStatisticMode(statisticId, STATISTIC_MODE.valueOf(statisticMode));
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic state for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic state.
     * @throws MBeanException If an error occurs.
     */
    public boolean getStatisticState(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticState(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Set the statistic state for the given statistic.<br>
     * The statistic mode is automatically set to MANUAL.
     * @param statisticId The statistic id.
     * @param statisticStateValue The statistic state.
     * @throws MBeanException If an error occurs.
     */
    public void setStatisticState(final String statisticId, final boolean statisticStateValue) throws MBeanException {
        try {
            getStatisticComponent().setStatisticState(statisticId, statisticStateValue);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Reset the given statistic.
     * @param statisticId The statistic id to reset.
     * @throws MBeanException If an error occurs.
     */
    public void resetStatistic(final String statisticId) throws MBeanException {
        try {
            getStatisticComponent().resetStatistic(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic name for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic name.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticName(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticName(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic description for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic description.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticDescription(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticDescription(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic value for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic value.
     * @throws MBeanException If an error occurs.
     */
    public String getStatisticValue(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticValue(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic start time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic start time.
     * @throws MBeanException If an error occurs.
     */
    public long getStatisticStartTime(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticStartTime(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

    /**
     * Get the statistic last sample time for the given statistic.
     * @param statisticId The statistic id.
     * @return The statistic last sample time.
     * @throws MBeanException If an error occurs.
     */
    public long getStatisticLastSampleTime(final String statisticId) throws MBeanException {
        try {
            return getStatisticComponent().getStatisticLastSampleTime(statisticId);
        } catch (EZBComponentException e) {
            throw new MBeanException(new Exception(e.getClass().getSimpleName() + " : " + e.getMessage()));
        }
    }

}
