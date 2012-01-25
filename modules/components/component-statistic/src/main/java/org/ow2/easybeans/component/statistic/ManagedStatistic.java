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
 * $Id: ManagedStatistic.java 5504 2010-05-26 14:00:17Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.statistic;

import org.ow2.easybeans.api.statistic.EZBStatistic;
import org.ow2.easybeans.component.itf.EZBStatisticComponent.STATISTIC_MODE;

/**
 * Helper class to manage statistics with an arborescent structure.
 * @author missonng
 *
 */
public class ManagedStatistic {
    /**
     * Default value for statistic mode.
     */
    private static final STATISTIC_MODE DEFAULT_STATISTIC_MODE = STATISTIC_MODE.AUTO;

    /**
     * The statistic.
     */
    private EZBStatistic statistic;

    /**
     * The managed statistic provider.
     */
    private ManagedStatisticProvider managedStatisticProvider = null;

    /**
     * The managed statistic factory.
     */
    private ManagedStatisticFactory managedStatisticFactory = null;

    /**
     * The managed statistic state.
     */
    private boolean managedStatisticState;

    /**
     * The managed statistic mode.
     */
    private STATISTIC_MODE managedStatisticMode;

    /**
     * The managed statistic constructor.
     * @param managedStatisticProvider The managed statistic provider.
     * @param managedStatisticFactory The managed statistic factory.
     */
    public ManagedStatistic(final ManagedStatisticProvider managedStatisticProvider,
            final ManagedStatisticFactory managedStatisticFactory) {
        this.statistic = managedStatisticFactory.getStatisticFactory().createStatistic(
                managedStatisticProvider.getStatisticProvider().getStatisticProviderId());

        this.managedStatisticProvider = managedStatisticProvider;
        this.managedStatisticProvider.getManagedStatistics().add(this);

        this.managedStatisticFactory = managedStatisticFactory;
        this.managedStatisticFactory.getManagedStatistics().add(this);

        setManagedStatisticMode(DEFAULT_STATISTIC_MODE);
    }

    /**
     * Destroy the managed statistic.
     */
    public void destroy() {
        this.statistic.deactivate();
        this.managedStatisticProvider.getManagedStatistics().remove(this);
        this.managedStatisticFactory.getManagedStatistics().remove(this);
    }

    /**
     * Helper method to propagate changes in the tree.
     */
    public void update() {
        this.managedStatisticState = getManagedStatisticState();

        if (getManagedStatisticState()) {
            this.statistic.activate();
        } else {
            this.statistic.deactivate();
        }
    }

    /**
     * Get the statistic.
     * @return The statistic.
     */
    public EZBStatistic getStatistic() {
        return this.statistic;
    }

    /**
     * Get the managed statistic mode.
     * @return The managed statistic mode.
     */
    public STATISTIC_MODE getManagedStatisticMode() {
        return this.managedStatisticMode;
    }

    /**
     * Set the managed statistic mode.
     * @param managedStatisticMode The managed statistic mode.
     */
    public void setManagedStatisticMode(final STATISTIC_MODE managedStatisticMode) {
        boolean oldManagedStatisticState = getManagedStatisticState();
        this.managedStatisticMode = managedStatisticMode;

        if (getManagedStatisticState() != oldManagedStatisticState) {
            update();
        }
    }

    /**
     * Get the managed statistic state.
     * @return The managed statistic state.
     */
    public boolean getManagedStatisticState() {
        if (getManagedStatisticMode() == STATISTIC_MODE.MANUAL) {
            return this.managedStatisticState;
        }

        return this.managedStatisticFactory.getManagedStatisticFactoryState()
            && this.managedStatisticProvider.getManagedStatisticProviderState();
    }

    /**
     * Set the managed statistic state.<br>
     * The mode will be set to manual automatically.
     * @param managedStatisticState The managed statistic state.
     */
    public void setManagedStatisticState(final boolean managedStatisticState) {
        boolean oldManagedStatisticState = getManagedStatisticState();
        this.managedStatisticState = managedStatisticState;

        if (getManagedStatisticMode() == STATISTIC_MODE.AUTO) {
            setManagedStatisticMode(STATISTIC_MODE.MANUAL);
        }

        if (getManagedStatisticState() != oldManagedStatisticState) {
            update();
        }
    }
}
