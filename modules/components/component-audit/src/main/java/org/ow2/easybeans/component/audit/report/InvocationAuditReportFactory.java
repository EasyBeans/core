/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: InvocationAuditReportFactory.java 5650 2010-11-04 14:50:58Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.audit.report;

import static org.ow2.util.marshalling.Serialization.storeObject;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationBegin;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationEnd;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationError;
import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.util.auditreport.impl.InvocationAuditReport;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class build report based on invocations events.
 * @author Mathieu ANCELIN
 */
public final class InvocationAuditReportFactory {
    /**
     * The name of the onMessage method of a MDB.
     */
    private static final String ON_MESSAGE_METHOD = "onMessage_javax.jms.Message";

    /**
     * The value of the start level.
     */
    private static final int START_LEVEL = -1;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(InvocationAuditReportFactory.class);

    /**
     * The report created but not endded.
     */
    private HashMap<Long, InvocationAuditReport> waitingReports;

    /**
     * The constructor of the factory.
     */
    public InvocationAuditReportFactory() {
        this.waitingReports = new HashMap<Long, InvocationAuditReport>();
    }

    /**
     * This method create a minimal report with start informations and put it in
     * a map, waiting for ending informations.
     * @param start the time (in ns) the invocation starts.
     * @param method the method requested.
     * @param current the current thread.
     * @param event the invocation event.
     * @param providerId the provider name.
     * @param callerRoles the roles used for the invocation.
     * @param caller the caller identity
     */
    public void prepareAuditReport(final long start, final String method, final Thread current,
            final EZBEventBeanInvocationBegin event, final String providerId, final Principal[] callerRoles,
            final Principal caller) {
        String[] roles = null;
        if (callerRoles != null) {
            roles = new String[callerRoles.length];
            int i = 0;
            for (Principal callerPrincipalRole : callerRoles) {
                roles[i++] = callerPrincipalRole.getName();
            }
        }
        int freeMemoryBefore = (int) (Runtime.getRuntime().freeMemory());
        int totalMemoryBefore = (int) (Runtime.getRuntime().totalMemory());

        Object[] arguments = event.getArguments();
        String[] params = null;
        if (arguments != null && arguments.length > 0) {
            int i = 0;
            params = new String[arguments.length];
            for (Object parameter : arguments) {
                params[i++] = parameter.toString();
            }
        }

        InvocationAuditReport reporttmp = new InvocationAuditReport(start, event.getTime(), method, event
                .getEventProviderId(), params, current, freeMemoryBefore, totalMemoryBefore, roles, caller
                .getName(), lengthArgs(event.getArguments()));
        reporttmp.setMethodStackTrace(cleanupStackTrace(event.getStackTraceElements()));
        reporttmp.setKeyID(event.getKeyID());

        this.waitingReports.put(event.getInvocationNumber(), reporttmp);

    }

    /**
     * Cleanup the stack trace elements to ave the right stack.
     * @param stackTrace the given stack
     * @return the cleanup stack
     */
    protected static StackTraceElement[] cleanupStackTrace(final StackTraceElement[] stackTrace) {
        List<StackTraceElement> cleanList = new ArrayList<StackTraceElement>();
        boolean inEasyBeansCode = false;
        boolean afterEasyBeansCode = false;
        for (StackTraceElement stackElement : stackTrace) {
            String className = stackElement.getClassName();
            if (className != null) {
                // remove the first java.lang. methods that are occuring before EasyBeans code
                if (!inEasyBeansCode && className.startsWith("java.lang.Thread")) {
                    continue;
                }
                // remove EasyBeans code that is present after the User code
                if (!afterEasyBeansCode && className.startsWith("org.ow2.easybeans.")) {
                    inEasyBeansCode = true;
                    continue;
                }
                // Do not remove anymore EasyBeans elements and add all elements
                afterEasyBeansCode = true;
                cleanList.add(stackElement);
            }
        }
        return cleanList.toArray(new StackTraceElement[cleanList.size()]);
    }


    /**
     * @param arguments the array of arguments (may be null)
     * @return the size of arguments
     */
    private static int lengthArgs(final Object[] arguments) {
        if (arguments == null) {
            return 0;
        }

        try {
          return storeObject(arguments).length;
        } catch (Exception e) {
            // Should be a Not Serializable arguments
            logger.debug("Unable to get argument size", e);
            return -1;
        }
    }

    /**
     * Return a new report well endded.
     * @param stop the time the event occured.
     * @param event the end event.
     * @param providerId the provider.
     * @return a new report.
     */
    public InvocationAuditReport getAuditReport(final long stop, final EZBEventBeanInvocationEnd event, final String providerId) {
        long totalGarbageCollections = 0;
        long garbageCollectionTime = 0;
        for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
            long count = gc.getCollectionCount();
            if (count >= 0) {
                totalGarbageCollections += count;
            }
            long time = gc.getCollectionTime();
            if (time >= 0) {
                garbageCollectionTime += time;
            }
        }
        int freeMemoryAfter = (int) (Runtime.getRuntime().freeMemory());
        int totalMemoryAfter = (int) (Runtime.getRuntime().totalMemory());
        InvocationAuditReport finished = this.waitingReports.get(event.getInvocationNumber());
        if (finished != null) {
            finished.setFreeMemoryAfter(freeMemoryAfter);
            finished.setTotalMemoryAfter(totalMemoryAfter);
            if (finished.getBusinessMethod().equals(ON_MESSAGE_METHOD)) {
                finished.setMethodReturn(event.getResult());
            } else {
                try {
                    finished.setMethodReturn(((EJBResponse) event.getResult()).getValue());
                } catch (Exception e) {
                    finished.setMethodReturn(null);
                }
            }
            finished.setRequestStop(stop);
            finished.setSweepMarkTime(totalGarbageCollections);
            finished.setScavengeTime(garbageCollectionTime);
            this.waitingReports.remove(event.getInvocationNumber());
            return finished;
        }
        return null;
    }

    /**
     * Return a new report endded with an error.
     * @param stop the time the event occured.
     * @param event the error event.
     * @param providerId the provider.
     * @return a new report.
     */
    public InvocationAuditReport getAuditReport(final long stop, final EZBEventBeanInvocationError event,
            final String providerId) {
        int freeMemoryAfter = (int) (Runtime.getRuntime().freeMemory());
        int totalMemoryAfter = (int) (Runtime.getRuntime().totalMemory());
        InvocationAuditReport finished = this.waitingReports.get(event.getInvocationNumber());
        if (finished != null) {
            finished.setFreeMemoryAfter(freeMemoryAfter);
            finished.setTotalMemoryAfter(totalMemoryAfter);
            finished.setRequestStop(stop);
            this.waitingReports.remove(event.getInvocationNumber());
            return finished;
        }
        return null;
    }

}
