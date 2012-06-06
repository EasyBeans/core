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
 * $Id: MeanCallTimeStatisticFactory.java 5508 2010-05-26 15:55:24Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.statistic;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import javax.management.j2ee.statistics.CountStatistic;

import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationBegin;
import org.ow2.easybeans.api.jmx.EZBMBeanAttribute;
import org.ow2.easybeans.api.statistic.EZBStatistic;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.component.itf.EZBJmxComponent;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.jmx.impl.AbstractMBeanAttribute;

/**
 * Statistic factory to count EJB call mean time in EasyBeans.
 * @author missonng
 */
public class MeanCallTimeStatisticFactory extends AbstractStatisticFactory {
    /**
     * The TotalCallTimeStatisticFactory constructor.
     * @param eventComponent The event component.
     * @param jmxComponent The jmx component.
     */
    public MeanCallTimeStatisticFactory(final EZBEventComponent eventComponent, final EZBJmxComponent jmxComponent) {
        super(J2EEManagedObjectNamingHelper.getAllJ2EEManagedObjectsFilter(), eventComponent, jmxComponent);
    }


    /**
     * Create a statistic for the given statistic provider.
     * @param statisticProviderId The statistic provider id.
     * @return The created statistic.
     */
    public EZBStatistic createStatistic(final String statisticProviderId) {
        return new MeanCallTimeStatistic(statisticProviderId);
    }

    /**
     * Statistic to count EJB call mean time in EasyBeans.
     * @author missonng
     */
    public class MeanCallTimeStatistic extends AbstractStatistic {
        /**
         * The total time.
         */
        private long total;

        /**
         * The total count.
         */
        private long count;

        /**
         * Map calls waiting for their end event.
         */
        private Map<Long, EZBEventBeanInvocationBegin> pendingCall;

        /**
         * The TotalCallTimeStatistic constructor.
         * @param statisticProviderId The statistic provider id.
         */
        public MeanCallTimeStatistic(final String statisticProviderId) {
            super(statisticProviderId.matches(J2EEManagedObjectNamingHelper.getAllMethodsFilter())
                        ? "averageBusinessProcessingTime_" + statisticProviderId.substring(statisticProviderId.lastIndexOf("/") + 1)
                        : "averageBusinessProcessingTime",
                    "The average processing time",
                    "ms",
                    MeanCallTimeStatisticFactory.this.getStatisticFactoryId(),
                    statisticProviderId,
                    MeanCallTimeStatisticFactory.this.getEventComponent(),
                    MeanCallTimeStatisticFactory.this.getJmxComponent());

            this.pendingCall = new HashMap<Long, EZBEventBeanInvocationBegin>();

            addEventListener(new MeanCallTimeEventListener());
            addMBeanAttribute(new MeanCallTimeMBeanAttribute());
        }

        /**
         * Reset the statistic.
         */
        @Override
        public synchronized void reset() {
            super.reset();
            this.total = 0;
            this.count = 0;
        }

        /**
         * Get the statistic String representation.
         * @return The statistic String representation.
         */
        public synchronized String getValue() {
            return ((this.count > 0 ? this.total / this.count : 0) + getUnit());
        }

        /**
         * Get the statistic JSR77 representation.
         * @return The statistic JSR77 representation.
         */
        public synchronized CountStatistic getJSR77Statistic() {
            return new CountStatistic() {
                public String getName() {
                    return MeanCallTimeStatistic.this.getName();
                }

                public String getDescription() {
                    return MeanCallTimeStatistic.this.getDescription();
                }

                public String getUnit() {
                    return MeanCallTimeStatistic.this.getUnit();
                }

                public long getStartTime() {
                    return MeanCallTimeStatistic.this.getStartTime();
                }

                public long getLastSampleTime() {
                    return MeanCallTimeStatistic.this.getLastSampleTime();
                }

                public long getCount() {
                    if (MeanCallTimeStatistic.this.count > 0) {
                        return MeanCallTimeStatistic.this.total / MeanCallTimeStatistic.this.count;
                    }

                    return 0;
                }
            };
        }

        /**
         * An EventListener for MeanCallTimeStatistic to collect data it needs.
         * @author missonng
         */
        private class MeanCallTimeEventListener implements EZBEventListener {
            /**
             * The event provider filter.
             */
            private Pattern eventProviderFilter;

            /**
             * The MeanCallTimeEventListener constructor.
             */
            public MeanCallTimeEventListener() {
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
                            ((EZBEventBeanInvocation) event).getEventProviderId()).matches();
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
                if (EZBEventBeanInvocationBegin.class.isAssignableFrom(event.getClass())) {
                    EZBEventBeanInvocationBegin e = (EZBEventBeanInvocationBegin) event;
                    MeanCallTimeStatistic.this.pendingCall.put(Long.valueOf(e.getInvocationNumber()), e);
                } else {
                    EZBEventBeanInvocation eventEnd = (EZBEventBeanInvocation) event;
                    EZBEventBeanInvocation eventBegin =
                        MeanCallTimeStatistic.this.pendingCall.remove(Long.valueOf(eventEnd.getInvocationNumber()));

                    MeanCallTimeStatistic.this.count++;
                    MeanCallTimeStatistic.this.total += eventEnd.getTime() - eventBegin.getTime();
                    setLastSampleTime(System.currentTimeMillis());
                }
            }
        }

        /**
         * A MBeanAttribute for MeanCallTimeStatistic to expose data.
         * @author missonng
         */
        private class MeanCallTimeMBeanAttribute extends AbstractMBeanAttribute implements EZBMBeanAttribute {
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
            public MeanCallTimeMBeanAttribute() {
                super(getName(), getUnit(), getDescription(), true, false, false);
            }

            /**
             * Get the attributes value for this BaseModelMBeanExt id.
             * @param id The BaseModelMBeanExt id.
             * @return The attributes value.
             */
            public Object getValue(final String id) {
                if (MeanCallTimeStatistic.this.count > 0) {
                    return Long.valueOf(MeanCallTimeStatistic.this.total / MeanCallTimeStatistic.this.count);
                }

                return Integer.valueOf(0);
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
