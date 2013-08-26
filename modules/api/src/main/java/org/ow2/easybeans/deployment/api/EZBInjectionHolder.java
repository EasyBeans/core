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
 * $Id: EZBInjectionHolder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.api;

import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.easybeans.resolver.api.EZBJNDIResolver;

/**
 * Allow to store resources used for injecting stuff.
 * For example Persistence Unit Manager, JNDI resolver, etc.
 * @author Florent BENOIT
 */
public interface EZBInjectionHolder {

    /**
     * @return persistence unit manager.
     */
    EZBPersistenceUnitManager getPersistenceUnitManager();

    /**
     * @return JNDI Resolver.
     */
    EZBJNDIResolver getJNDIResolver();
}
