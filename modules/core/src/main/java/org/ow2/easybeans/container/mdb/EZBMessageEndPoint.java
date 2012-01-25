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
 * $Id: EZBMessageEndPoint.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb;

import javax.ejb.Timer;
import javax.resource.spi.endpoint.MessageEndpoint;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansMDB;

/**
 * EasyBeans message endpoint interface.
 * @author Florent Benoit
 */
public interface EZBMessageEndPoint extends MessageEndpoint {


    /**
     * Gets the factory of the bean.
     * @return factory of the bean.
     */
    Factory<?, ?> getEasyBeansFactory();

    /**
     * Gets the factory of the bean.
     * @return factory of the bean.
     */
    MDBMessageEndPointFactory getMDBMessageEndPointFactory();

    /**
     * Gets the wrapped Message Driven Bean object.
     * @return wrapped Message Driven Bean object.
     */
    EasyBeansMDB getEasyBeansMDB();

    /**
     * Invokes the timeout method on the bean.
     * @param timer the given EJB timer
     */
    void notifyTimeout(final Timer timer);

}
