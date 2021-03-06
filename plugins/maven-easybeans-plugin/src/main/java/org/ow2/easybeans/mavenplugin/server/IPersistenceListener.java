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
 * $Id: IPersistenceListener.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.util.List;

/**
 * Interface determining the action executed by the listener,
 * before a deployment of a ressource.
 * @author Vincent Michaud
 */
public interface IPersistenceListener {

    /**
     * Determine the action performed when the audit is received.
     * @param persistenceProvidersImpl Implementations of required persistence providers.
     *        The value of integers in the list can be: PersistenceSupport.JPA_ECLIPSELINK,
     *        PersistenceSupport.JPA_HIBERNATE, PersistenceSupport.JPA_OPENJPA,
     *        and PersistenceSupport.JPA_TOPLINK. The list can not be null or empty.
     */
    void reportRequestedPersistenceProviders(final List<Integer> persistenceProvidersImpl);
}
