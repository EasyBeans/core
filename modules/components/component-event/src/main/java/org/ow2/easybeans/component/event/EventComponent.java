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
 * $Id: EventComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.event;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.event.api.IEventListener;
import org.ow2.util.event.api.IEventService;
import org.ow2.util.event.impl.EventDispatcher;
import org.ow2.util.event.impl.EventService;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Implementation of the EasyBeans event component.
 * @author missonng
 */
public class EventComponent implements EZBEventComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EventComponent.class);

    /**
     * The component event service.
     */
    private IEventService service;

    /**
     * The component event listeners.
     */
    private LinkedList<IEventListener> listeners = new LinkedList<IEventListener>();

    /**
     * The component J2EEManagedObject.
     */
    private LinkedList<EZBJ2EEManagedObject> objects = new LinkedList<EZBJ2EEManagedObject>();

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public synchronized void init() throws EZBComponentException {
      //if nobody define the EventService instance (OSGi, ...), the component create its own instance.
        if (this.service == null) {
            this.service = new EventService();
            this.service.start();
        }
    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public synchronized void start() throws EZBComponentException {
        logger.info("started.");
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public synchronized void stop() throws EZBComponentException {
        for (IEventListener listener : this.listeners) {
            this.service.unregisterListener(listener);
        }

        for (EZBJ2EEManagedObject object : this.objects) {
            this.service.unregisterDispatcher(object.getJ2EEManagedObjectId());
        }

        this.listeners.clear();
        this.objects.clear();
        logger.info("stopped.");
    }

    /**
     * Set the event service to use.
     * @param service The event service to use.
     */
    public synchronized void setEventService(final IEventService service) {
        this.service = service;
    }

    /**
     * @return the event service to use
     */
    public synchronized IEventService getEventService() {
        return this.service;
    }

    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     * @param dispatcher The event dispatcher for this J2EE managed object.
     */
    public synchronized void registerJ2EEManagedObject(final EZBJ2EEManagedObject object, final IEventDispatcher dispatcher) {
        if (EZBServer.class.isAssignableFrom(object.getClass())) {
            registerEZBServer((EZBServer) object, dispatcher);
        } else if (EZBContainer.class.isAssignableFrom(object.getClass())) {
            registerEZBContainer((EZBContainer) object, dispatcher);
        } else if (Factory.class.isAssignableFrom(object.getClass())) {
            registerEZBFactory((Factory<?, ?>) object, dispatcher);
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
     * Register a new event listener.<br>
     * The listener will automatically be register with each dispatcher matching his filter.
     * @param eventListener The listener to register.
     */
    public synchronized void registerEventListener(final EZBEventListener eventListener) {
        this.service.registerListener(eventListener, eventListener.getEventProviderFilter());
        this.listeners.add(eventListener);
    }

    /**
     * Unregister an event listener.
     * @param eventListener The listener to unregister.
     */
    public synchronized void unregisterEventListener(final EZBEventListener eventListener) {
        this.service.unregisterListener(eventListener);
        this.listeners.remove(eventListener);
    }

    /**
     * Creates a new IEventDispatcher.
     *
     * @return a new IEventDispatcher.
     */
    public IEventDispatcher createEventDispatcher() {
        return new EventDispatcher();
    }

    /**
     * Helper method to register a EZBServer.
     * @param server The EZBServer to register.
     * @param dispatcher The EventDispatcher for this EZBServer.
     */
    private void registerEZBServer(final EZBServer server, final IEventDispatcher dispatcher) {
        this.service.registerDispatcher(server.getJ2EEManagedObjectId(), dispatcher);
        this.objects.add(server);
    }

    /**
     * Helper method to unregister a EZBServer.
     * @param server The EZBServer to unregister.
     */
    private void unregisterEZBServer(final EZBServer server) {
        this.service.unregisterDispatcher(server.getJ2EEManagedObjectId());
        this.objects.remove(server);
    }

    /**
     * Helper method to register a EZBContainer.
     * @param container The EZBContainer to register.
     * @param dispatcher The EventDispatcher for this EZBContainer.
     */
    private void registerEZBContainer(final EZBContainer container, final IEventDispatcher dispatcher) {
        this.service.registerDispatcher(container.getJ2EEManagedObjectId(), dispatcher);
        this.objects.add(container);
    }

    /**
     * Helper method to unregister a EZBContainer.
     * @param container The EZBContainer to unregister.
     */
    private void unregisterEZBContainer(final EZBContainer container) {
        this.service.unregisterDispatcher(container.getJ2EEManagedObjectId());
        this.objects.remove(container);
    }

    /**
     * Helper method to register a Factory.
     * @param factory The Factory to register.
     * @param dispatcher The EventDispatcher for this Factory.
     */
    private void registerEZBFactory(final Factory<?, ?> factory, final IEventDispatcher dispatcher) {
        this.service.registerDispatcher(factory.getJ2EEManagedObjectId(), dispatcher);
        this.objects.add(factory);

        List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
        for (final String method : methods) {
            this.service.registerDispatcher(method, dispatcher);
        }
    }

    /**
     * Helper method to unregister a Factory.
     * @param factory The Factory to unregister.
     */
    private void unregisterEZBFactory(final Factory<?, ?> factory) {
        this.service.unregisterDispatcher(factory.getJ2EEManagedObjectId());
        this.objects.remove(factory);

        List<String> methods = J2EEManagedObjectNamingHelper.getBeanMethodsManagedObjectIds(factory);
        for (final String method : methods) {
            this.service.unregisterDispatcher(method);
        }
    }
}
