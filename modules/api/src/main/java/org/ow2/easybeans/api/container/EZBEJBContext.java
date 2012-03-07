/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EZBEJBContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.container;

import java.util.Map;

import javax.ejb.EJBContext;
import javax.ejb.TimerService;

import org.ow2.easybeans.api.Factory;

/**
 * Context that will be stored in the bean object.
 * Allow to gets the bean of this context.
 * @author Florent Benoit
 * @param <FactoryType> an EasyBeans factory
 */
public interface EZBEJBContext<FactoryType extends Factory<?, ?>> extends EJBContext {

    /**
     * Gets the factory of this context.
     * @return factory used by this context.
     */
    FactoryType getFactory();

    /**
     * Get access to the EJB Timer Service.
     * @return Timer service.
     */
    TimerService getInternalTimerService();

    /**
     * @return the context data associated to the current invocation.
     */
    Map<String, Object> getContextData();
}
