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
 * $Id: JMXNotifier.java 5432 2010-03-24 15:00:27Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.audit.jmx;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.ObjectName;

import org.ow2.easybeans.jmx.JMXRemoteException;
import org.ow2.easybeans.jmx.MBeanServerHelper;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.util.auditreport.api.AuditorJMXObjectNames;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class send notifications on new audit reports.
 * @author Mathieu ANCELIN
 */
public final class JMXNotifier extends NotificationBroadcasterSupport implements JMXNotifierMBean {

    /**
     * The name of the notifier MBean in the JMX server.
     */
    private static final String NOTIFIER_NAME = AuditorJMXObjectNames.EJBAUDITOR_TYPE_COMPONENT + ",name=EasyBeans";

    /**
     * The logger of the class.
     */
    private static Log logger = LogFactory.getLog(JMXNotifier.class);

    /**
     * The zero value.
     */
    private static final int ZERO = 0;

    /**
     * The ObjectName of the notifier.
     */
    private ObjectName notifier = null;

    /**
     * The number of the notification.
     */
    private long seqNumber = ZERO;

    /**
     * The MBean server object.
     */
    private MBeanServer server = null;


    /**
     * @return the Mbean server.
     */
    private MBeanServer getMBeanServer() {
        try {
            return MBeanServerHelper.getMBeanServerServer();
        } catch (JMXRemoteException e) {
            throw new IllegalStateException("Cannot get an MBean server", e);
        }
    }

    /**
     * Constructor of the notifier.
     */
    public JMXNotifier() {
        super();
        try {
            this.notifier = new ObjectName(MBeansHelper.getDomainName() + NOTIFIER_NAME);
            this.server = getMBeanServer();
        } catch (Exception ex) {
            logger.error("Error while registering Easybeans Audit notifier : ", ex);
        }
        registration();
    }

    /**
     * @return the next sequence number.
     */
    private long getNextSeqNumber() {
        return this.seqNumber++;
    }

    /**
     * Register the notifier in the JMX server.
     */
    public void registration() {
        if (this.server != null) {
            try {
                if (!this.server.isRegistered(this.notifier)) {
                    this.server.registerMBean(this, this.notifier);
                }
            } catch (Exception e) {
                logger.error(e);
            }
        } else {
            logger.error("MBeanServer not found and could not be created. Not registering MBeans.");
        }
    }

    /**
     * Unregister the Mbean.
     */
    public void unregistration() {
        try {
            MBeansHelper.getInstance().unregisterMBean(this);
        } catch (MBeansException ex) {
            logger.error("Error while registering Easybeans Audit notifier : ", ex);
        }
    }

    /**
     * This method send a notification to the listeners of the MBean.
     * @param type the type of the notification.
     * @param report the report text.
     */
    public synchronized void sendAuditNotification(final String type, final String report) {
        if (report != null) {
            sendNotification(new Notification(type, this, getNextSeqNumber(), report));
        }
    }

}
