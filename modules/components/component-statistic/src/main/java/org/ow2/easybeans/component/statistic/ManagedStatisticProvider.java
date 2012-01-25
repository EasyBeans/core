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
 * $Id: ManagedStatisticProvider.java 5504 2010-05-26 14:00:17Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.statistic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.statistic.EZBStatisticProvider;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_MODE;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_PROVIDER_MODE;

/**
 * Helper class to manage statistic providers with an arborescent structure.
 * @author missonng
 */
public class ManagedStatisticProvider {
    /**
     * Default value for statistic provider state.
     */
    private static final boolean DEFAULT_STATISTIC_PROVIDER_STATE = true;

    /**
     * Default value for statistic provider mode.
     */
    private static final STATISTIC_PROVIDER_MODE DEFAULT_STATISTIC_PROVIDER_MODE = STATISTIC_PROVIDER_MODE.INHERIT;

    /**
     * The statistic provider.
     */
    private EZBStatisticProvider statisticProvider;

    /**
     * The parent managed statistic provider.
     */
    private ManagedStatisticProvider parentManagedStatisticProvider = null;

    /**
     * The children managed statistic providers.
     */
    private List<ManagedStatisticProvider> childManagedStatisticProviders = new LinkedList<ManagedStatisticProvider>();

    /**
     * The managed statistics.
     */
    private List<ManagedStatistic> managedStatistics = new LinkedList<ManagedStatistic>();

    /**
     * The managed statistic provider state.
     */
    private boolean managedStatisticProviderState;

    /**
     * The managed statistic provider mode.
     */
    private STATISTIC_PROVIDER_MODE managedStatisticProviderMode;

    /**
     * The managed statistic provider constructor.
     * @param statisticProvider The statistic provider to manage.
     * @param managedStatisticProviders The component managedStatisticProviders.
     * @param managedStatisticFactories The component managedStatisticFactories.
     */
    public ManagedStatisticProvider(final EZBStatisticProvider statisticProvider,
            final Map<String, ManagedStatisticProvider> managedStatisticProviders,
            final Map<String, ManagedStatisticFactory> managedStatisticFactories) {
        this.statisticProvider = statisticProvider;

        // define parent.
        for (ManagedStatisticProvider managedStatisticProvider : managedStatisticProviders.values()) {
            if (this.statisticProvider.getStatisticProviderId().startsWith(
                    managedStatisticProvider.statisticProvider.getStatisticProviderId() + "/")) {
                if (this.parentManagedStatisticProvider == null) {
                    this.parentManagedStatisticProvider = managedStatisticProvider;
                } else if (managedStatisticProvider.statisticProvider.getStatisticProviderId().startsWith(
                        this.parentManagedStatisticProvider.statisticProvider.getStatisticProviderId() + "/")) {
                    this.parentManagedStatisticProvider = managedStatisticProvider;
                }
            }
        }
        if (this.parentManagedStatisticProvider != null) {
            this.parentManagedStatisticProvider.childManagedStatisticProviders.add(this);
        }

        // define children.
        for (ManagedStatisticProvider managedStatisticProvider : managedStatisticProviders.values()) {
            if (managedStatisticProvider.statisticProvider.getStatisticProviderId().startsWith(
                    this.statisticProvider.getStatisticProviderId() + "/")) {
                if (managedStatisticProvider.parentManagedStatisticProvider == null) {
                    this.childManagedStatisticProviders.add(managedStatisticProvider);
                    managedStatisticProvider.parentManagedStatisticProvider = this;
                } else if (managedStatisticProvider.parentManagedStatisticProvider == this.parentManagedStatisticProvider) {
                    this.parentManagedStatisticProvider.childManagedStatisticProviders.remove(managedStatisticProvider);
                    this.childManagedStatisticProviders.add(managedStatisticProvider);
                    managedStatisticProvider.parentManagedStatisticProvider = this;
                }
            }
        }

        // create statistics
        for (ManagedStatisticFactory managedStatisticFactory : managedStatisticFactories.values()) {
            if (this.statisticProvider.getStatisticProviderId().matches(
                    managedStatisticFactory.getStatisticFactory().getStatisticProviderFilter())) {
                new ManagedStatistic(this, managedStatisticFactory);
            }
        }

        // define the state
        this.managedStatisticProviderMode = DEFAULT_STATISTIC_PROVIDER_MODE;
        this.managedStatisticProviderState = getManagedStatisticProviderState();

        update();
    }

    /**
     * Destroy the statistic provider.
     */
    public void destroy() {
        if (this.parentManagedStatisticProvider != null) {
            this.parentManagedStatisticProvider.childManagedStatisticProviders.remove(this);
            for (ManagedStatisticProvider managedStatisticProvider : this.childManagedStatisticProviders) {
                boolean oldManagedStatisticProviderState = managedStatisticProvider.getManagedStatisticProviderState();

                managedStatisticProvider.parentManagedStatisticProvider = this.parentManagedStatisticProvider;
                this.parentManagedStatisticProvider.childManagedStatisticProviders.add(managedStatisticProvider);

                if (managedStatisticProvider.getManagedStatisticProviderState() != oldManagedStatisticProviderState) {
                    managedStatisticProvider.update();
                }
            }
        } else {
            for (ManagedStatisticProvider managedStatisticProvider : this.childManagedStatisticProviders) {
                boolean oldManagedStatisticProviderState = managedStatisticProvider.getManagedStatisticProviderState();

                managedStatisticProvider.parentManagedStatisticProvider = null;

                if (managedStatisticProvider.getManagedStatisticProviderState() != oldManagedStatisticProviderState) {
                    managedStatisticProvider.update();
                }
            }
        }

        // Use a copy of the list (to avoid the modification that will occur on the original list)
        List<ManagedStatistic> copyList =  new ArrayList<ManagedStatistic>(this.managedStatistics);
        for (ManagedStatistic managedStatistic : copyList) {
            managedStatistic.destroy();
        }
    }

    /**
     * Helper method to propagate changes in the tree.
     */
    public void update() {
        this.managedStatisticProviderState = getManagedStatisticProviderState();

        for (ManagedStatisticProvider managedStatisticProvider : this.childManagedStatisticProviders) {
            if (managedStatisticProvider.getManagedStatisticProviderMode() == STATISTIC_PROVIDER_MODE.INHERIT) {
                managedStatisticProvider.update();
            }
        }

        for (ManagedStatistic managedStatistic : this.managedStatistics) {
            if (managedStatistic.getManagedStatisticMode() == STATISTIC_MODE.AUTO) {
                managedStatistic.update();
            }
        }
    }

    /**
     * Get the statistic provider.
     * @return The statistic provider.
     */
    public EZBStatisticProvider getStatisticProvider() {
        return this.statisticProvider;
    }

    /**
     * Get managed statistics associate with this provider.
     * @return The managed statistics.
     */
    public List<ManagedStatistic> getManagedStatistics() {
        return this.managedStatistics;
    }

    /**
     * Get the managed statistic provider mode.
     * @return The managed statistic provider mode.
     */
    public STATISTIC_PROVIDER_MODE getManagedStatisticProviderMode() {
        return this.managedStatisticProviderMode;
    }

    /**
     * Set the managed statistic provider mode.
     * @param managedStatisticProviderMode The managed statistic provider mode.
     */
    public void setManagedStatisticProviderMode(final STATISTIC_PROVIDER_MODE managedStatisticProviderMode) {
        boolean oldManagedStatisticProviderState = getManagedStatisticProviderState();
        this.managedStatisticProviderMode = managedStatisticProviderMode;

        if (getManagedStatisticProviderState() != oldManagedStatisticProviderState) {
            update();
        }
    }

    /**
     * Get the managed statistic provider state.
     * @return The managed statistic provider state.
     */
    public boolean getManagedStatisticProviderState() {
        if (getManagedStatisticProviderMode() == STATISTIC_PROVIDER_MODE.MANUAL) {
            return this.managedStatisticProviderState;
        }

        if (this.parentManagedStatisticProvider != null) {
            return this.parentManagedStatisticProvider.getManagedStatisticProviderState();
        }

        return DEFAULT_STATISTIC_PROVIDER_STATE;
    }

    /**
     * Set the managed statistic provider state.<br>
     * The mode will be set to manual automatically.
     * @param managedStatisticProviderState The managed statistic provider state.
     */
    public void setManagedStatisticProviderState(final boolean managedStatisticProviderState) {
        boolean oldManagedStatisticProviderState = getManagedStatisticProviderState();
        this.managedStatisticProviderState = managedStatisticProviderState;

        if (getManagedStatisticProviderMode() == STATISTIC_PROVIDER_MODE.INHERIT) {
            setManagedStatisticProviderMode(STATISTIC_PROVIDER_MODE.MANUAL);
        }

        if (getManagedStatisticProviderState() != oldManagedStatisticProviderState) {
            update();
        }
    }
}
