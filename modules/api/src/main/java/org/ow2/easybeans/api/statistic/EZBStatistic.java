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
 * $Id: EZBStatistic.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.statistic;

import javax.management.j2ee.statistics.Statistic;

/**
 * Interface for all EasyBeans statistics.
 * @author missonng
 */
public interface EZBStatistic {
    /**
     * Get the statistic name.
     * @return The statistic name.
     */
    String getName();

    /**
     * Get the statistic description.
     * @return The statistic description.
     */
    String getDescription();

    /**
     * Get the statistic unit.
     * @return The statistic unit.
     */
    String getUnit();

    /**
     * Get the statistic start time.
     * @return The statistic start time.
     */
    long getStartTime();

    /**
     * Get the statistic last sample time.
     * @return The statistic last sample time.
     */
    long getLastSampleTime();

    /**
     * Get the statistic String representation.
     * @return The statistic String representation.
     */
    String getValue();

    /**
     * Get the statistic JSR77 representation.
     * @return The statistic JSR77 representation.
     */
    Statistic getJSR77Statistic();

    /**
     * Get the statistic id.
     * @return The statistic factory id.
     */
    String getStatisticId();

    /**
     * Get the statistic factory id.
     * @return The statistic factory id.
     */
    String getStatisticFactoryId();

    /**
     * Get the statistic provider id.
     * @return The statistic provider id.
     */
    String getStatisticProviderId();

    /**
     * Reset the statistic.
     */
    void reset();

    /**
     * Activate the statistic.
     */
    void activate();

    /**
     * Deactivate the statistic.
     */
    void deactivate();

    /**
     * Get the statistic availability.
     * @return The statistic availability.
     */
    boolean isAvailable();
}
