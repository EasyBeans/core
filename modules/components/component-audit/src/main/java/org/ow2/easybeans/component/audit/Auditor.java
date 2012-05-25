/**
 * EasyBeans
 * Copyright (C) 2010-2012 Bull S.A.S.
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
 * $Id: Auditor.java 5468 2010-04-21 12:44:14Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.component.audit;

import org.ow2.easybeans.api.event.EZBEvent;
import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationBegin;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationEnd;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationError;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycle;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycleStarted;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycleStarting;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycleStopped;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycleStopping;
import org.ow2.easybeans.component.audit.jmx.JMXNotifier;
import org.ow2.easybeans.component.audit.report.InvocationAuditReportFactory;
import org.ow2.util.auditreport.impl.InvocationAuditReport;
import org.ow2.util.auditreport.impl.LifeCycleAuditReport;
import org.ow2.util.auditreport.impl.LifeCycleAuditReport.STEP;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.event.api.IEventService;

/**
 * This class listen to every event comming from its audited bean.
 * @author Mathieu ANCELIN
 */
public class Auditor implements EZBEventListener {

    /**
     * The event provider filter.
     */
    private String eventProviderFilter = ".*";

    /**
     * JMX Notifier.
     */
    private JMXNotifier jmxNotifier = null;

    /**
     * Invocation audit report factory.
     */
    private InvocationAuditReportFactory invocationAuditReportFactory = null;

    /**
     * Enable/Disable lifecycle.
     */
    private boolean lifecycleEnabled = false;

    /**
     * eventService to send audit report
     */
    private IEventService eventService;

    /**
     * The constructor of the auditor.
     * @param filter the filter of the listener.
     * @param jmxNotifier the given JMX notifier.
     * @param invocationAuditReportFactory the given invocation audit factory
     */
    public Auditor(final String filter, final JMXNotifier jmxNotifier,
            final InvocationAuditReportFactory invocationAuditReportFactory, final IEventService eventService) {
        this.eventService = eventService;
        this.eventProviderFilter = filter;
        this.jmxNotifier = jmxNotifier;
        this.invocationAuditReportFactory = invocationAuditReportFactory;
    }

    /**
     * The handle method of the listener. Called any time an event is received.
     * @param event the handled event.
     */
    public void handle(final IEvent event) {
        String eventOn = ((EZBEvent) event).getEventProviderId();
        String methodName = eventOn.substring(eventOn.lastIndexOf("/") + 1, eventOn.length());
        this.eventService.getDispatcher("EJB");
        if (EZBEventBeanInvocationBegin.class.isAssignableFrom(event.getClass())) {
            EZBEventBeanInvocationBegin e = (EZBEventBeanInvocationBegin) event;
            if (e.getEventProviderId().contains(this.eventProviderFilter)) {
                this.invocationAuditReportFactory.prepareAuditReport(e.getTime(), methodName, Thread.currentThread(), e,e.getEventProviderId(), e.getCallerRoles(), e.getCallerPrincipal());
            }
        }
        if (EZBEventBeanInvocationEnd.class.isAssignableFrom(event.getClass())) {
            EZBEventBeanInvocationEnd eventEnd = (EZBEventBeanInvocationEnd) event;
            if (eventEnd.getEventProviderId().contains(this.eventProviderFilter)) {
                this.jmxNotifier.sendAuditNotification(InvocationAuditReport.class.getName(), this.invocationAuditReportFactory.getAuditReport(eventEnd.getTime(), eventEnd, eventEnd.getEventProviderId()).toString());
            }
        }
        if (EZBEventBeanInvocationError.class.isAssignableFrom(event.getClass())) {
            EZBEventBeanInvocationError eventEnd = (EZBEventBeanInvocationError) event;
            if (eventEnd.getEventProviderId().contains(this.eventProviderFilter)) {
                this.jmxNotifier.sendAuditNotification(InvocationAuditReport.class.getName(), this.invocationAuditReportFactory
                        .getAuditReport(eventEnd.getTime(), eventEnd, eventEnd.getEventProviderId()).toString());
            }
        }
        if (this.lifecycleEnabled) {
            if (EZBEventLifeCycleStarted.class.isAssignableFrom(event.getClass())) {
                EZBEventLifeCycleStarted eventEnd = (EZBEventLifeCycleStarted) event;
                this.jmxNotifier.sendAuditNotification(LifeCycleAuditReport.class.getName(),
                        getLifeCycleAuditReport(eventEnd, this.eventProviderFilter, STEP.STARTED, Thread.currentThread())
                                .toString());
            }
            if (EZBEventLifeCycleStarting.class.isAssignableFrom(event.getClass())) {
                EZBEventLifeCycleStarting eventEnd = (EZBEventLifeCycleStarting) event;
                this.jmxNotifier.sendAuditNotification(LifeCycleAuditReport.class.getName(),
                        getLifeCycleAuditReport(eventEnd, this.eventProviderFilter, STEP.STARTING, Thread.currentThread())
                                .toString());
            }
            if (EZBEventLifeCycleStopped.class.isAssignableFrom(event.getClass())) {
                EZBEventLifeCycleStopped eventEnd = (EZBEventLifeCycleStopped) event;
                this.jmxNotifier.sendAuditNotification(LifeCycleAuditReport.class.getName(),
                        getLifeCycleAuditReport(eventEnd, this.eventProviderFilter, STEP.STOPPED, Thread.currentThread())
                                .toString());
            }
            if (EZBEventLifeCycleStopping.class.isAssignableFrom(event.getClass())) {
                EZBEventLifeCycleStopping eventEnd = (EZBEventLifeCycleStopping) event;
                this.jmxNotifier.sendAuditNotification(LifeCycleAuditReport.class.getName(),
                        getLifeCycleAuditReport(eventEnd, this.eventProviderFilter, STEP.STOPPING, Thread.currentThread())
                                .toString());
            }
        }
    }

    /**
     * Get the event provider filter.<br>
     * The event provider filter is a regular expression that define which event
     * provider the listener needs to listen.
     * @return The event provider filter.
     */
    public String getEventProviderFilter() {
        return this.eventProviderFilter;
    }

    /**
     * Check whether the listener wants to handle this event.
     * @param event The event to check.
     * @return True if the listener wants to handle this event, false otherwise.
     */
    public boolean accept(final IEvent event) {
        try {
            if (((EZBEvent) event).getEventProviderId().contains(this.eventProviderFilter)) {
                return true;
            }
            return false;
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
     * This method return a new lifecycle audit report.
     * @param event the event used to generate the report.
     * @param beanName the name of the concerned bean.
     * @param step the step of the event in the lifecycle.
     * @param current the current thread.
     * @return the builded audit report.
     */
    public LifeCycleAuditReport getLifeCycleAuditReport(final EZBEventLifeCycle event, final String beanName, final STEP step,
            final Thread current) {
        int freeMemoryBefore = (int) (Runtime.getRuntime().freeMemory());
        int totalMemoryBefore = (int) (Runtime.getRuntime().totalMemory());
        int freeMemoryAfter = (int) (Runtime.getRuntime().freeMemory());
        int totalMemoryAfter = (int) (Runtime.getRuntime().totalMemory());
        return new LifeCycleAuditReport(event.getTime(), beanName, step, current, freeMemoryBefore, totalMemoryBefore,
                freeMemoryAfter, totalMemoryAfter);
    }
}
