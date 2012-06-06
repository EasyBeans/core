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
 * $Id: CallCountStatisticFactory.java 5488 2010-05-04 16:06:31Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.statistic;

import java.util.regex.Pattern;

import javax.management.j2ee.statistics.CountStatistic;

import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationEnd;
import org.ow2.easybeans.api.jmx.EZBMBeanAttribute;
import org.ow2.easybeans.api.statistic.EZBStatistic;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBJmxComponent;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.jmx.impl.AbstractMBeanAttribute;

/**
 * Statistic factory to count EJB call in EasyBeans.
 * @author missonng
 */
public class CallCountStatisticFactory extends AbstractStatisticFactory {
    /**
     * The CallCountStatisticFactory constructor.
     * @param eventComponent The event component.
     * @param jmxComponent The jmx component.
     */
    public CallCountStatisticFactory(final EZBEventComponent eventComponent, final EZBJmxComponent jmxComponent) {
        super(J2EEManagedObjectNamingHelper.getAllJ2EEManagedObjectsFilter(), eventComponent, jmxComponent);
    }

    /**
     * Create a statistic for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The created statistic.
     */
    public EZBStatistic createStatistic(final String statisticProviderId) {
        return new CallCountStatistic(statisticProviderId);
    }

    /**
     * Statistic to count EJB call in EasyBeans.
     * @author missonng
     */
    public class CallCountStatistic extends AbstractStatistic {
        /**
         * The call count.
         */
        private long count;

        /**
         * The CallCountStatistic constructor.
         * @param statisticProviderId The statistic provider id.
         */
        public CallCountStatistic(final String statisticProviderId) {
            super(statisticProviderId.matches(J2EEManagedObjectNamingHelper.getAllMethodsFilter())
                        ? "numberOfCalls_" + statisticProviderId.substring(statisticProviderId.lastIndexOf("/") + 1)
                        : "numberOfCalls",
                    "The number of calls",
                    "",
                    CallCountStatisticFactory.this.getStatisticFactoryId(),
                    statisticProviderId,
                    CallCountStatisticFactory.this.getEventComponent(),
                    CallCountStatisticFactory.this.getJmxComponent());
            addEventListener(new CallCountEventListener());
            addMBeanAttribute(new CallCountMBeanAttribute());
        }

        /**
         * Reset the statistic.
         */
        @Override
        public synchronized void reset() {
            super.reset();
            this.count = 0;
        }

        /**
         * Get the statistic String representation.
         * @return The statistic String representation.
         */
        public synchronized String getValue() {
            return (this.count + getUnit());
        }

        /**
         * Get the statistic JSR77 representation.
         * @return The statistic JSR77 representation.
         */
        public synchronized CountStatistic getJSR77Statistic() {
            return new CountStatistic() {
                public String getName() {
                    return CallCountStatistic.this.getName();
                }

                public String getDescription() {
                    return CallCountStatistic.this.getDescription();
                }

                public String getUnit() {
                    return CallCountStatistic.this.getUnit();
                }

                public long getStartTime() {
                    return CallCountStatistic.this.getStartTime();
                }

                public long getLastSampleTime() {
                    return CallCountStatistic.this.getLastSampleTime();
                }

                public long getCount() {
                    return CallCountStatistic.this.count;
                }
            };
        }

        /**
         * An EventListener for CallCountStatistic to collect data it needs.
         * @author missonng
         */
        private class CallCountEventListener implements EZBEventListener {
            /**
             * The event provider filter.
             */
            private Pattern eventProviderFilter;

            /**
             * The CallCountEventListener constructor.
             */
            public CallCountEventListener() {
                this.eventProviderFilter = Pattern.compile(
                        J2EEManagedObjectNamingHelper.getAllRelativeMethodsFilter(getStatisticProviderId()));
            }

            /**
             * Get the event provider filter.<br>
             * The event provider filter is a regular expression that define which event provider the listener needs to listen.
             * @return The event provider filter.
             */
            public String getEventProviderFilter() {
                return this.eventProviderFilter.pattern();
            }

            /**
             * Check whether the listener wants to handle this event.
             * @param event The event to check.
             * @return True if the listener wants to handle this event, false otherwise.
             */
            public boolean accept(final IEvent event) {
                try {
                    return this.eventProviderFilter.matcher(
                            ((EZBEventBeanInvocationEnd) event).getEventProviderId()).matches();
                } catch (Throwable error) {
                    return false;
                }
            }

            /**
             * Get the listener priority.
             * @return The listener priority.
             */
            public EventPriority getPriority() {
                return EventPriority.ASYNC_LOW;
            }

            /**
             * Handle the event.
             * @param event The event to handle.
             */
            public synchronized void handle(final IEvent event) {
                CallCountStatistic.this.count++;
                setLastSampleTime(System.currentTimeMillis());
            }
        }

        /**
         * A MBeanAttribute for CallCountStatistic to expose data.
         * @author missonng
         */
        private class CallCountMBeanAttribute extends AbstractMBeanAttribute implements EZBMBeanAttribute {
            /**
             * Get the MBean provider filter.<br>
             * The MBean provider filter is a regular expression that define on which MBean the attribute needs to be added.
             * @return The MBean provider filter.
             */
            public String getMBeanProviderFilter() {
                return J2EEManagedObjectNamingHelper.encodeJ2EEManagedObjectFilter(getStatisticProviderId());
            }

            /**
             * The CallCountMBeanAttribute constructor.
             */
            public CallCountMBeanAttribute() {
                super(getName(), getUnit(), getDescription(), true, false, false);
            }

            /**
             * Get the attributes value for this BaseModelMBeanExt id.
             * @param id The BaseModelMBeanExt id.
             * @return The attributes value.
             */
            public Object getValue(final String id) {
                return Long.valueOf(CallCountStatistic.this.count);
            }

            /**
             * Set the attribute value for this BaseModelMBeanExt id.
             * @param id The BaseModelMBeanExt id.
             * @param value The attributes value.
             */
            public void setValue(final String id, final Object value) {
                // Nothing to do
            }
        }
    }
}
