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
 * $Id: EJBResponse.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.api;

import java.io.Serializable;

/**
 * Response received from the server.
 * @author Florent Benoit
 */
public interface EJBResponse extends Serializable {

    /**
     * @return the value returned by the server wrapped in the response.
     */
    Object getValue();

    /**
     * Sets the value of the response.
     * @param o the value.
     */
    void setValue(Object o);


    /**
     * @return id of the bean.
     */
    Long getBeanId();

    /**
     * Sets the bean Id.
     * @param beanId the id of the bean.
     */
    void setBeanId(Long beanId);


    /**
     * @return true if the bean has been removed
     */
    boolean isRemoved();

    /**
     * Sets the removed flag.
     * @param removed if bean has been removed.
     */
    void setRemoved(boolean removed);


    /**
     * @return RPC exception of the invocation (if any).
     */
    RPCException getRPCException();


    /**
     * Sets the RPC Exception (if any).
     * @param rpcException the given exception
     */
    void setRPCException(RPCException rpcException);
}
