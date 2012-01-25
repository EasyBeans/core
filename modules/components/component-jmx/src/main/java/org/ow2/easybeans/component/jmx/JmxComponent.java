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
 * $Id: JmxComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jmx;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.jmx.EZBMBeanAttribute;
import org.ow2.easybeans.api.jmx.EZBMBeanOperation;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBJmxComponent;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.util.jmx.api.IBaseModelMBeanExt;
import org.ow2.util.jmx.api.ICommonsModelerExtService;
import org.ow2.util.jmx.api.IMBeanAttribute;
import org.ow2.util.jmx.api.IMBeanOperation;
import org.ow2.util.jmx.impl.CommonsModelerExtService;

/**
 * Implementation of the EasyBeans JMX component.
 * @author missonng
 */
public class JmxComponent implements EZBJmxComponent {
    /**
     * The CommonsModelerExtService.
     */
    private ICommonsModelerExtService service;

    /**
     * The component registered attributes.
     */
    private LinkedList<IMBeanAttribute> attributes = new LinkedList<IMBeanAttribute>();

    /**
     * The component registered operations.
     */
    private LinkedList<IMBeanOperation> operations = new LinkedList<IMBeanOperation>();

    /**
     * The component J2EEManagedObject.
     */
    private LinkedList<EZBJ2EEManagedObject> objects = new LinkedList<EZBJ2EEManagedObject>();

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public synchronized void init() throws EZBComponentException {
        //if nobody define the JmxService instance (OSGi, ...), the component create its own instance.
        if (this.service == null) {
            this.service = new CommonsModelerExtService();
            this.service.start();
        }
    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public synchronized void start() throws EZBComponentException {

    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public synchronized void stop() throws EZBComponentException {
        for (IMBeanAttribute attribute : this.attributes) {
            this.service.unregisterAttribute(attribute);
        }

        for (IMBeanOperation operation : this.operations) {
            this.service.unregisterOperation(operation);
        }

        for (EZBJ2EEManagedObject object : this.objects) {
            this.service.unregisterBaseModelMBeanExt(object.getJ2EEManagedObjectId());
        }

        this.attributes.clear();
        this.operations.clear();
        this.objects.clear();
    }

    /**
     * Set the CommonsModelerExtService to use.
     * @param service The CommonsModelerExtService to use.
     */
    public synchronized void setCommonsModelerExtService(final ICommonsModelerExtService service) {
        this.service = service;
    }

    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     * @param mbean the BaseModelMBean for this J2EE managed object.
     */
    public synchronized void registerJ2EEManagedObject(final EZBJ2EEManagedObject object, final IBaseModelMBeanExt mbean) {
        if (EZBServer.class.isAssignableFrom(object.getClass())) {
            registerEZBServer((EZBServer) object, mbean);
        } else if (EZBContainer.class.isAssignableFrom(object.getClass())) {
            registerEZBContainer((EZBContainer) object, mbean);
        } else if (Factory.class.isAssignableFrom(object.getClass())) {
            registerEZBFactory((Factory<?, ?>) object, mbean);
        }
    }

    /**
     * Unregister a J2EE managed object.
     * @param object The J2EE managed object to unregister.
     */
    public synchronized void unregisterJ2EEManagedObject(final EZBJ2EEManagedObject object) {
        if (EZBServer.class.isAssignableFrom(object.getClass())) {
            unregisterEZBServer((EZBServer) object);
        } else if (EZBContainer.class.isAssignableFrom(object.getClass())) {
            unregisterEZBContainer((EZBContainer) object);
        } else if (Factory.class.isAssignableFrom(object.getClass())) {
            unregisterEZBFactory((Factory<?, ?>) object);
        }
    }

    /**
     * Register a new mbean attribute.<br>
     * The attribute will automatically be register with each MBean matching his filter.
     * @param mbeanAttribute The mbean attribute to register.
     */
    public synchronized void registerMBeanAttribute(final EZBMBeanAttribute mbeanAttribute) {
        this.service.registerAttribute(mbeanAttribute, mbeanAttribute.getMBeanProviderFilter());
        this.attributes.add(mbeanAttribute);
    }

    /**
     * Unregister a mbean attribute.
     * @param mbeanAttribute The mbean attribute to unregister.
     */
    public synchronized void unregisterMBeanAttribute(final EZBMBeanAttribute mbeanAttribute) {
        this.service.unregisterAttribute(mbeanAttribute);
        this.attributes.remove(mbeanAttribute);
    }

    /**
     * Register a new mbean operation.<br>
     * The operation will automatically be register with each MBean matching his filter.
     * @param mbeanOperation The mbean operation to register.
     */
    public synchronized void registerMBeanOperation(final EZBMBeanOperation mbeanOperation) {
        this.service.registerOperation(mbeanOperation, mbeanOperation.getMBeanProviderFilter());
        this.operations.add(mbeanOperation);
    }

    /**
     * Unregister a mbean operation.
     * @param mbeanOperation The mbean operation to unregister.
     */
    public synchronized void unregisterMBeanOperation(final EZBMBeanOperation mbeanOperation) {
        this.service.unregisterOperation(mbeanOperation);
        this.operations.remove(mbeanOperation);
    }

    /**
     * Helper method to register a EZBServer.
     * @param server The EZBServer to register.
     * @param mbean The BaseModelMBeanExt for this EZBServer.
     */
    private void registerEZBServer(final EZBServer server, final IBaseModelMBeanExt mbean) {
        this.service.registerBaseModelMBeanExt(server.getJ2EEManagedObjectId(), mbean);
        this.objects.add(server);
    }

    /**
     * Helper method to unregister a EZBServer.
     * @param server The EZBServer to unregister.
     */
    private void unregisterEZBServer(final EZBServer server) {
        this.service.unregisterBaseModelMBeanExt(server.getJ2EEManagedObjectId());
        this.objects.remove(server);
    }

    /**
     * Helper method to register a EZBContainer.
     * @param container The EZBContainer to register.
     * @param mbean The BaseModelMBeanExt for this EZBContainer.
     */
    private void registerEZBContainer(final EZBContainer container, final IBaseModelMBeanExt mbean) {
        this.service.registerBaseModelMBeanExt(container.getJ2EEManagedObjectId(), mbean);
        this.objects.add(container);
    }

    /**
     * Helper method to unregister a EZBContainer.
     * @param container The EZBContainer to unregister.
     */
    private void unregisterEZBContainer(final EZBContainer container) {
        this.service.unregisterBaseModelMBeanExt(container.getJ2EEManagedObjectId());
        this.objects.remove(container);
    }

    /**
     * Helper method to register a Factory.
     * @param factory The Factory to register.
     * @param mbean The BaseModelMBeanExt for this Factory.
     */
    private void registerEZBFactory(final Factory<?, ?> factory, final IBaseModelMBeanExt mbean) {
        this.service.registerBaseModelMBeanExt(factory.getJ2EEManagedObjectId(), mbean);
        this.objects.add(factory);

        List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
        for (final String method : methods) {
            this.service.registerBaseModelMBeanExt(method, mbean);
        }
    }

    /**
     * Helper method to unregister a Factory.
     * @param factory The Factory to unregister.
     */
    private void unregisterEZBFactory(final Factory<?, ?> factory) {
        this.service.unregisterBaseModelMBeanExt(factory.getJ2EEManagedObjectId());
        this.objects.remove(factory);

        List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
        for (final String method : methods) {
            this.service.unregisterBaseModelMBeanExt(method);
        }
    }
}
