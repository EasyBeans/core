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
 * $Id: ManagedStatisticFactory.java 5504 2010-05-26 14:00:17Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.statistic;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.statistic.EZBStatisticFactory;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_FACTORY_MODE;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_MODE;

/**
 * Helper class to manage statistic factories with an arborescent structure.
 * @author missonng
 */
public class ManagedStatisticFactory {
    /**
     * Default value for statistic factory state.
     */
    private static final boolean DEFAULT_STATISTIC_FACTORY_STATE = true;

    /**
     * Default value for statistic factory mode.
     */
    private static final STATISTIC_FACTORY_MODE DEFAULT_STATISTIC_FACTORY_MODE = STATISTIC_FACTORY_MODE.INHERIT;

    /**
     * The statistic factory.
     */
    private EZBStatisticFactory statisticFactory;

    /**
     * The parent managed statistic factory.
     */
    private ManagedStatisticFactory parentManagedStatisticFactory = null;

    /**
     * The children managed statistic factories.
     */
    private List<ManagedStatisticFactory> childManagedStatisticFactorys = new LinkedList<ManagedStatisticFactory>();

    /**
     * The managed statistics.
     */
    private List<ManagedStatistic> managedStatistics = new LinkedList<ManagedStatistic>();

    /**
     * The managed statistic factory state.
     */
    private boolean managedStatisticFactoryState;

    /**
     * The managed statistic factory mode.
     */
    private STATISTIC_FACTORY_MODE managedStatisticFactoryMode;

    /**
     * The managed statistic factory constructor.
     * @param statisticFactory The statistic factory to manage.
     * @param managedStatisticProviders The component managedStatisticProviders.
     * @param managedStatisticFactories The component managedStatisticFactories.
     */
    public ManagedStatisticFactory(final EZBStatisticFactory statisticFactory,
            final Map<String, ManagedStatisticProvider> managedStatisticProviders,
            final Map<String, ManagedStatisticFactory> managedStatisticFactories) {
        this.statisticFactory = statisticFactory;

        // define parent.
        for (ManagedStatisticFactory managedStatisticFactory : managedStatisticFactories.values()) {
            if (this.statisticFactory.getStatisticFactoryId().endsWith(
                    managedStatisticFactory.statisticFactory.getStatisticFactoryId().substring(
                            managedStatisticFactory.statisticFactory.getStatisticFactoryId().lastIndexOf(".") + 1))) {
                if (this.statisticFactory.getStatisticFactoryId().startsWith(
                        managedStatisticFactory.statisticFactory.getStatisticFactoryId().substring(0,
                                managedStatisticFactory.statisticFactory.getStatisticFactoryId().lastIndexOf(".") + 1))) {
                    if (this.parentManagedStatisticFactory == null) {
                        this.parentManagedStatisticFactory = managedStatisticFactory;
                    } else if (managedStatisticFactory.statisticFactory.getStatisticFactoryId().startsWith(
                            this.parentManagedStatisticFactory.statisticFactory.getStatisticFactoryId().substring(0,
                                    this.parentManagedStatisticFactory.statisticFactory.getStatisticFactoryId().lastIndexOf(".") + 1))) {
                        this.parentManagedStatisticFactory = managedStatisticFactory;
                    }
                }
            }
        }
        if (this.parentManagedStatisticFactory != null) {
            this.parentManagedStatisticFactory.childManagedStatisticFactorys.add(this);
        }

        // define children.
        for (ManagedStatisticFactory managedStatisticFactory : managedStatisticFactories.values()) {
            if (managedStatisticFactory.statisticFactory.getStatisticFactoryId().endsWith(
                    this.statisticFactory.getStatisticFactoryId().substring(
                            this.statisticFactory.getStatisticFactoryId().lastIndexOf(".") + 1))) {
                if (managedStatisticFactory.statisticFactory.getStatisticFactoryId().startsWith(
                        this.statisticFactory.getStatisticFactoryId().substring(0,
                                this.statisticFactory.getStatisticFactoryId().lastIndexOf(".") + 1))) {
                    if (managedStatisticFactory.parentManagedStatisticFactory == null) {
                        this.childManagedStatisticFactorys.add(managedStatisticFactory);
                        managedStatisticFactory.parentManagedStatisticFactory = this;
                    } else if (managedStatisticFactory.parentManagedStatisticFactory == this.parentManagedStatisticFactory) {
                        this.childManagedStatisticFactorys.add(managedStatisticFactory);
                        this.parentManagedStatisticFactory.childManagedStatisticFactorys.remove(managedStatisticFactory);
                        managedStatisticFactory.parentManagedStatisticFactory = this;
                    }
                }
            }
        }

        // create statistics
        for (ManagedStatisticProvider managedStatisticProvider : managedStatisticProviders.values()) {
            if (managedStatisticProvider.getStatisticProvider().getStatisticProviderId().matches(
                    this.statisticFactory.getStatisticProviderFilter())) {
                new ManagedStatistic(managedStatisticProvider, this);
            }
        }

        // define the state
        this.managedStatisticFactoryMode = DEFAULT_STATISTIC_FACTORY_MODE;
        this.managedStatisticFactoryState = getManagedStatisticFactoryState();

        update();
    }

    /**
     * Destroy the managed statistic factory.
     */
    public void destroy() {
        if (this.parentManagedStatisticFactory != null) {
            this.parentManagedStatisticFactory.childManagedStatisticFactorys.remove(this);
            for (ManagedStatisticFactory managedStatisticFactory : this.childManagedStatisticFactorys) {
                boolean oldManagedStatisticFactoryState = managedStatisticFactory.getManagedStatisticFactoryState();

                managedStatisticFactory.parentManagedStatisticFactory = this.parentManagedStatisticFactory;
                this.parentManagedStatisticFactory.childManagedStatisticFactorys.add(managedStatisticFactory);

                if (managedStatisticFactory.getManagedStatisticFactoryState() != oldManagedStatisticFactoryState) {
                    managedStatisticFactory.update();
                }
            }
        } else {
            for (ManagedStatisticFactory managedStatisticFactory : this.childManagedStatisticFactorys) {
                boolean oldManagedStatisticFactoryState = managedStatisticFactory.getManagedStatisticFactoryState();

                managedStatisticFactory.parentManagedStatisticFactory = null;

                if (managedStatisticFactory.getManagedStatisticFactoryState() != oldManagedStatisticFactoryState) {
                    managedStatisticFactory.update();
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
        this.managedStatisticFactoryState = getManagedStatisticFactoryState();

        for (ManagedStatisticFactory managedStatisticFactory : this.childManagedStatisticFactorys) {
            if (managedStatisticFactory.getManagedStatisticFactoryMode() == STATISTIC_FACTORY_MODE.INHERIT) {
                managedStatisticFactory.update();
            }
        }

        for (ManagedStatistic managedStatistic : this.managedStatistics) {
            if (managedStatistic.getManagedStatisticMode() == STATISTIC_MODE.AUTO) {
                managedStatistic.update();
            }
        }
    }

    /**
     * Get the statistic factory.
     * @return The statistic factory.
     */
    public EZBStatisticFactory getStatisticFactory() {
        return this.statisticFactory;
    }

    /**
     * Get managed statistics associate with this factory.
     * @return The managed statistics.
     */
    public List<ManagedStatistic> getManagedStatistics() {
        return this.managedStatistics;
    }

    /**
     * Get the managed statistic factory mode.
     * @return The managed statistic factory mode.
     */
    public STATISTIC_FACTORY_MODE getManagedStatisticFactoryMode() {
        return this.managedStatisticFactoryMode;
    }

    /**
     * Set the managed statistic factory mode.
     * @param managedStatisticFactoryMode The managed statistic factory mode.
     */
    public void setManagedStatisticFactoryMode(final STATISTIC_FACTORY_MODE managedStatisticFactoryMode) {
        boolean oldManagedStatisticFactoryState = getManagedStatisticFactoryState();
        this.managedStatisticFactoryMode = managedStatisticFactoryMode;

        if (getManagedStatisticFactoryState() != oldManagedStatisticFactoryState) {
            update();
        }
    }

    /**
     * Get the managed statistic factory state.
     * @return The managed statistic factory state.
     */
    public boolean getManagedStatisticFactoryState() {
        if (getManagedStatisticFactoryMode() == STATISTIC_FACTORY_MODE.MANUAL) {
            return this.managedStatisticFactoryState;
        }

        if (this.parentManagedStatisticFactory != null) {
            return this.parentManagedStatisticFactory.getManagedStatisticFactoryState();
        }

        return DEFAULT_STATISTIC_FACTORY_STATE;
    }

    /**
     * Set the managed statistic factory state.<br>
     * The mode will be set to manual automatically.
     * @param managedStatisticFactoryState The managed statistic factory state.
     */
    public void setManagedStatisticFactoryState(final boolean managedStatisticFactoryState) {
        boolean oldManagedStatisticFactoryState = getManagedStatisticFactoryState();
        this.managedStatisticFactoryState = managedStatisticFactoryState;

        if (getManagedStatisticFactoryMode() == STATISTIC_FACTORY_MODE.INHERIT) {
            setManagedStatisticFactoryMode(STATISTIC_FACTORY_MODE.MANUAL);
        }

        if (getManagedStatisticFactoryState() != oldManagedStatisticFactoryState) {
            update();
        }
    }
}
