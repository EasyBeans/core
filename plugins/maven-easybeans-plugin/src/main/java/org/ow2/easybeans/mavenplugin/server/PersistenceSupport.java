/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: PersistenceSupport.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.container.EZBEventContainerStarting;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;

/**
 * Manage the persistence provider support. This class receives events from
 * the server and report all container starting events to its owns listeners.
 * @author Vincent Michaud
 */
public class PersistenceSupport implements EZBEventListener {

    /****************************************************/
    /*     Supported persistence providers constants    */
    /****************************************************/

    /**
     * The version of the server.
     */
    private static final String SERVER_VERSION = Version.getServerVersion();

    /**
     * Dependencies of supported persistence providers.
     */
    private static final String[][] DEPENDENCIES = {
        {"org.ow2.easybeans", "easybeans-jpa-eclipselink-dependency", SERVER_VERSION, "pom" },
        {"org.ow2.easybeans", "easybeans-jpa-hibernate-dependency", SERVER_VERSION, "pom" },
        {"org.ow2.easybeans", "easybeans-jpa-openjpa-dependency", SERVER_VERSION, "pom" },
        {"org.ow2.easybeans", "easybeans-jpa-toplink-essentials-dependency", SERVER_VERSION, "pom" }
    };

    /**
     * Implementions of supported persistence providers.
     */
    private static final String[] IMPLEMENTATIONS = {
        "org.eclipse.persistence.jpa.PersistenceProvider",
        "org.hibernate.ejb.HibernatePersistence",
        "org.apache.openjpa.persistence.PersistenceProviderImpl",
        "oracle.toplink.essentials.PersistenceProvider"
    };


    /****************************************************/
    /*     Constants for getDependencies() function     */
    /****************************************************/

    /**
     * Index of Eclipselink persistence provider. Used as first argument of
     * getDependencies() function.
     */
    public static final int JPA_ECLIPSELINK = 0;

    /**
     * Index of Hibernate persistence provider. Used as first argument of
     * getDependencies() function.
     */
    public static final int JPA_HIBERNATE = 1;

    /**
     * Index of OpenJPA persistence provider. Used as first argument of
     * getDependencies() function.
     */
    public static final int JPA_OPENJPA = 2;

    /**
     * Index of Toplink persistence provider. Used as first argument of
     * getDependencies().
     */
    public static final int JPA_TOPLINK = 3;

    /**
     * GroupID information. Used as second argument of getDependencies() function.
     */
    public static final int GROUP_ID = 0;

    /**
     * ArtifactID information. Used as second argument of getDependencies() function.
     */
    public static final int ARTIFACT_ID = 1;

    /**
     * Version information. Used as second argument of getDependencies() function.
     */
    public static final int VERSION = 2;

    /**
     * Type information. Used as second argument of getDependencies() function.
     */
    public static final int TYPE = 3;


    /****************************************************/
    /*                Fields of the class               */
    /****************************************************/

    /**
     * Listeners of the notification from audit component.
     */
    private final List<IPersistenceListener> listeners = new LinkedList<IPersistenceListener>();

    /**
     * The EasyBeans event component.
     */
    private final EZBEventComponent eventComponent;


    /****************************************************/
    /*                 Members functions                */
    /****************************************************/

    /**
     * Constructor.
     * @param eventComponent The EasyBeans event component
     */
    public PersistenceSupport(final EZBEventComponent eventComponent) {
        this.eventComponent = eventComponent;
    }

    /**
     * Add a listener.
     * @param listener The listener
     */
    public void addListener(final IPersistenceListener listener) {
        if (listeners.size() == 0) {
            this.eventComponent.registerEventListener(this);
        }
        listeners.add(listener);
    }

    /**
     * Remove a listener.
     * @param listener The listener
     */
    public void removeListener(final IPersistenceListener listener) {
        boolean deleted = false;
        while (listeners.remove(listener)) {
            deleted = true;
        }
        if (listeners.size() == 0 && deleted) {
            this.eventComponent.unregisterEventListener(this);
        }
    }

    /**
     * Get the implementation of the persistence provider.
     * @param indexPersistenceProvider The index of the persistence provider
     * @return The name of the class
     */
    public static String getImplementation(final int indexPersistenceProvider) {
        return IMPLEMENTATIONS[indexPersistenceProvider];
    }

    /**
     * Get the implementation of the persistence provider.
     * @param implementationPersistenceProvider The class name of the persistence provider
     * @return The index of the persistence provider, or -1 if the implementation is
     *         not supported
     */
    private static int getImplementation(final String implementationPersistenceProvider) {
        if (implementationPersistenceProvider != null) {
            for (int i = 0; i < IMPLEMENTATIONS.length; i++) {
                if (IMPLEMENTATIONS[i].equals(implementationPersistenceProvider)) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Get the number of supported persistence providers.
     * @return The number of supported persistence providers.
     */
    public static int getSupportedPersistenceProviders() {
        return IMPLEMENTATIONS.length;
    }

    /**
     * Get information on the persistence provider.
     * @param indexPersistenceProvider The index of the persistence provider
     * @param typeInfo The type of the information
     * @return The information requested
     */
    public static String getDependencies(final int indexPersistenceProvider, final int typeInfo) {
        String result = DEPENDENCIES[indexPersistenceProvider][typeInfo];
        if (result == null) {
            return "";
        }
        return result;
    }

    /****************************************************/
    /*        Implementation of EZBEventListener        */
    /****************************************************/

    /**
     * Handle the event. Get all implementations of persistence providers
     * and notify this information to the listeners.
     * @param event The event to handle.
     */
    public void handle(final IEvent event) {
        EZBEventContainerStarting containerEvent = (EZBEventContainerStarting) event;
        EZBPersistenceUnitManager persistenceUnitManager = containerEvent.getPersistenceUnitManager();
    }

    /**
     * Check whether the listener wants to handle this event.
     * @param event The event to check.
     * @return True if the listener wants to handle this event, false otherwise.
     */
    public boolean accept(final IEvent event) {
        return (event instanceof EZBEventContainerStarting);
    }

    /**
     * Get the event priority.
     * @return A normal synchrone priority
     */
    public EventPriority getPriority() {
        return EventPriority.SYNC_NORM;
    }

    /**
     * Get the event provider filter. The event provider filter is a regular
     * expression that define which event provider the listener needs to listen.
     * @return The event provider filter.
     */
    public String getEventProviderFilter() {
        return ".*";
    }
}
