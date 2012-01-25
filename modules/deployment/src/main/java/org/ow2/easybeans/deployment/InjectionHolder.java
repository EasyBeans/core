/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: InjectionHolder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment;

import org.ow2.easybeans.deployment.api.EZBInjectionHolder;
import org.ow2.easybeans.persistence.api.EZBPersistenceUnitManager;
import org.ow2.easybeans.resolver.api.EZBJNDIResolver;

/**
 * Keeps some injection objects like persistence unit manager, jndi resolver, etc.
 * @author Florent BENOIT
 */
public class InjectionHolder implements EZBInjectionHolder {

    /**
     * Persistence unit manager.
     */
    private EZBPersistenceUnitManager persistenceUnitManager = null;

    /**
     * JNDI Resolver.
     */
    private EZBJNDIResolver jndiResolver = null;


    /**
     * Default constructor.
     */
    public InjectionHolder() {

    }

    /**
     * Sets the persistence unit manager.
     * @param persistenceUnitManager the given persistence unit manager.
     */
    public void setPersistenceUnitManager(final EZBPersistenceUnitManager persistenceUnitManager) {
        this.persistenceUnitManager = persistenceUnitManager;
    }

    /**
     * @return persistence unit manager.
     */
    public EZBPersistenceUnitManager getPersistenceUnitManager() {
        return this.persistenceUnitManager;
    }

    /**
     * Sets the JNDI Resolver.
     * @param jndiResolver the given JNDI Resolver.
     */
    public void setJNDIResolver(final EZBJNDIResolver jndiResolver) {
        this.jndiResolver = jndiResolver;
    }


    /**
     * @return JNDI Resolver.
     */
    public EZBJNDIResolver getJNDIResolver() {
        return this.jndiResolver;
    }

}
