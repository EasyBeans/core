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
 * $Id: EJBRemoteRequest.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.api;

import java.io.Serializable;

/**
 * Request sends to the server.
 * @author Florent Benoit
 */
public interface EJBRemoteRequest extends Serializable {

    /**
     * @return name of the method
     */
     String getMethodName();

     /**
      * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method hashing of RMI</a>
      * @return the hash of this method
      */
     Long getMethodHash();

     /**
      * @return the argument of the request (send by the client)
      */
     Object[] getMethodArgs();

     /**
      * @return the container id of this request. It will be used to know the container for which this request is sent.
      */
     String getContainerId();

     /**
      * @return the factory name of the container.
      */
     String getFactory();

     /**
      * @return id of the bean.
      */
     Long getBeanId();

     /**
      * @return name of the business interface used for invoking the method (if any).
      */
     String getInvokedBusinessInterfaceName();
}
