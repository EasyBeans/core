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
 * $Id: JEJBResponse.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc;

import org.ow2.easybeans.rpc.api.EJBResponse;
import org.ow2.easybeans.rpc.api.RPCException;

/**
 * Implementation of the EJBResponse interface.
 * @author Florent Benoit
 */
public class JEJBResponse implements EJBResponse {

    /**
     * Id for serializable class.
     */
    private static final long serialVersionUID = 5854172126038516858L;

    /**
     * Value of the response.
     */
    private Object value;

    /**
     * Bean id.
     */
    private Long beanId = null;

    /**
     * RPC Exception (if any).
     */
    private RPCException rpcException;

    /**
     * Bean removed ?
     */
    private boolean removed = false;

    /**
     * Gets the value.
     * @return value of response.
     */
    public Object getValue() {
        return value;
    }

    /**
     * Sets the value of the response.
     * @param value response's value.
     */
    public void setValue(final Object value) {
       this.value = value;
    }


    /**
     * @return id of the bean.
     */
    public Long getBeanId() {
        return beanId;
    }

    /**
     * Sets the bean Id.
     * @param beanId the id of the bean.
     */
    public void setBeanId(final Long beanId) {
        this.beanId = beanId;
    }

    /**
     * @return RPC exception of the invocation (if any).
     */
    public RPCException getRPCException() {
        return rpcException;
    }


    /**
     * Sets the RPC Exception (if any).
     * @param rpcException the given exception
     */
    public void setRPCException(final RPCException rpcException) {
        this.rpcException = rpcException;
    }

    /**
     * @return true if the bean has been removed
     */
    public boolean isRemoved() {
        return removed;
    }

    /**
     * Sets the removed flag.
     * @param removed if bean has been removed.
     */
    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

}
