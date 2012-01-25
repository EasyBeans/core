/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EJBLocalRequest.java 5412 2010-03-17 13:25:06Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.api;

/**
 * Defines a local call request.
 * @author Florent Benoit
 */
public interface EJBLocalRequest {

    /**
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method hashing of RMI</a>
     * @return the hash of this method
     */
    Long getMethodHash();

    /**
     * @return the id of the bean.
     */
     Long getBeanId();

    /**
     * @return the argument of the request (send by the client).
     */
    Object[] getMethodArgs();

    /**
     * @return name of the business interface used for invoking the method (if any).
     */
    String getInvokedBusinessInterfaceName();

    /**
     * @return true if this request is invoked remotely
     */
    boolean isCalledFromRemoteRequest();

}
