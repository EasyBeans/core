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
 * $Id: AbstractStatisticFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.statistic;

import org.ow2.easybeans.api.statistic.EZBStatisticFactory;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBJmxComponent;

/**
 * Abstract implementation of the EZBStatisticFactory class.
 * @author missonng
 */
public abstract class AbstractStatisticFactory implements EZBStatisticFactory {
    /**
     * The statistic provider filter.
     */
    private String statisticProviderFilter;

    /**
     * The event component.
     */
    private EZBEventComponent eventComponent;

    /**
     * The jmx component.
     */
    private EZBJmxComponent jmxComponent;

    /**
     * The AbstractStatisticFactory constructor.
     * @param statisticProviderFilter The statistic provider filter.
     * @param eventComponent The event component.
     * @param jmxComponent The jmx component.
     */
    public AbstractStatisticFactory(final String statisticProviderFilter,
            final EZBEventComponent eventComponent, final EZBJmxComponent jmxComponent) {
        this.statisticProviderFilter = statisticProviderFilter;
        this.eventComponent = eventComponent;
        this.jmxComponent = jmxComponent;
    }

    /**
     * Get the statistic factory id.
     * @return The statistic factory id.
     */
    public String getStatisticFactoryId() {
        return this.getClass().getName();
    }

    /**
     * Get the statistic provider filter.<br>
     * The statistic provider filter is a regular expression that define for which provider a statistic should be created.
     * @return The statistic provider filter.
     */
    public String getStatisticProviderFilter() {
        return this.statisticProviderFilter;
    }

    /**
     * Get the event component.
     * @return The event component.
     */
    public EZBEventComponent getEventComponent() {
        return this.eventComponent;
    }

    /**
     * Get the jmx component.
     * @return The jmx component.
     */
    public EZBJmxComponent getJmxComponent() {
        return this.jmxComponent;
    }
}
