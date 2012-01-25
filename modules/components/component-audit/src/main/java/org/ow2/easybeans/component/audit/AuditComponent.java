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
 * $Id: AuditComponent.java 5745 2011-02-28 16:05:09Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.audit;

import java.util.LinkedList;
import java.util.Vector;

import org.ow2.carol.util.configuration.ConfigurationException;
import org.ow2.carol.util.configuration.ConfigurationRepository;
import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.audit.EZBAuditComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.audit.jmx.JMXNotifier;
import org.ow2.easybeans.component.audit.report.InvocationAuditReportFactory;
import org.ow2.easybeans.component.audit.rmi.interceptor.jrmp.Initializer;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.util.auditreport.api.ICurrentInvocationID;
import org.ow2.util.auditreport.impl.CurrentInvocationID;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * The audit component for Easybeans.
 * @author Mathieu ANCELIN
 */
public class AuditComponent implements EZBAuditComponent {

    /**
     * The JMX notifier.
     */
    private JMXNotifier jmxNotifier;

    /**
     * The event component.
     */
    private EZBEventComponent eventComponent;

    /**
     * List of audited objects.
     */
    private Vector<EZBJ2EEManagedObject> auditedObjects;

    /**
     * The logger of the class.
     */
    private static Log logger = LogFactory.getLog(AuditComponent.class);

    /**
     * Invocation audit report factory.
     */
    private InvocationAuditReportFactory invocationAuditReportFactory = null;

    /**
     * Instance of the Invocation ID.
     */
    private ICurrentInvocationID currentInvocationID = null;

    /**
     * Default constructor.
     */
    public AuditComponent() {
        this.invocationAuditReportFactory = new InvocationAuditReportFactory();
        this.currentInvocationID = CurrentInvocationID.getInstance();
    }

    /**
     * Init the component.
     * @throws org.ow2.easybeans.component.api.EZBComponentException exception.
     */
    public void init() throws EZBComponentException {
        this.jmxNotifier = new JMXNotifier();
        this.auditedObjects = new Vector<EZBJ2EEManagedObject>();
    }

    /**
     * Start the component.
     * @throws org.ow2.easybeans.component.api.EZBComponentException exception.
     */
    public void start() throws EZBComponentException {
        try {
            ConfigurationRepository.addInterceptors("jrmp", Initializer.class);
        } catch (ConfigurationException e) {
            logger.error("Cannot add JRMP interceptor", e);
        }
        logger.info("Audit component started.");
    }

    /**
     * Stop the component.
     * @throws org.ow2.easybeans.component.api.EZBComponentException exception.
     */
    public void stop() throws EZBComponentException {
        try {
            ConfigurationRepository.removeInterceptors("jrmp", Initializer.class);
        } catch (ConfigurationException e) {
            logger.error("Cannot delete JRMP interceptor", e);
        }

        LinkedList<EZBJ2EEManagedObject> tmpLinkedList = new LinkedList<EZBJ2EEManagedObject>(this.auditedObjects);
        for (EZBJ2EEManagedObject object : tmpLinkedList) {
            unregisterJ2EEManagedObject(object);
        }
        this.auditedObjects.clear();
        logger.info("Audit component stopped.");
    }

    /**
     * Set the event component.
     * @param eventComponent The event component.
     */
    public synchronized void setEventComponent(final EZBEventComponent eventComponent) {
        this.eventComponent = eventComponent;
    }

    /**
     * Add an audited bean to the audit system.
     * @param object the audited bean.
     */
    public void registerJ2EEManagedObject(final EZBJ2EEManagedObject object) {
        logger.debug("Audit on " + object.getJ2EEManagedObjectId());
        if (this.auditedObjects.contains(object)) {
            unregisterJ2EEManagedObject(object);
        }
        this.auditedObjects.add(object);
        this.eventComponent.registerEventListener(new Auditor(object.getJ2EEManagedObjectId(), this.jmxNotifier,
                this.invocationAuditReportFactory));
    }

    /**
     * Remove an audited bean from the audit system.
     * @param object the audited bean.
     */
    public void unregisterJ2EEManagedObject(final EZBJ2EEManagedObject object) {
        logger.debug(object.getJ2EEManagedObjectId() + " not audited anymore");
        if (this.auditedObjects.contains(object)) {
            this.auditedObjects.remove(object);
            this.eventComponent.unregisterEventListener(new Auditor(object.getJ2EEManagedObjectId(), this.jmxNotifier,
                    this.invocationAuditReportFactory));
        }
    }

    /**
     * @return the invocation current ID.
     */
    public ICurrentInvocationID getCurrentInvocationID() {
        return this.currentInvocationID;
    }
}
