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
 * $Id: EJBLocalRequestImpl.java 5412 2010-03-17 13:25:06Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc;

import org.ow2.easybeans.rpc.api.EJBLocalRequest;

/**
 * Defines an implementation of a local call request.
 * @author Florent Benoit
 */
public class EJBLocalRequestImpl implements EJBLocalRequest {

    /**
     * Arguments of the method.
     */
    private Object[] args = null;

    /**
     * Id of the bean (ie, for stateful).
     */
    private Long beanId = null;

    /**
     * Hashing of the method.
     * @see <a href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method hashing of RMI</a>
     */
    private Long methodHash;

    /**
     * Invoked business interface name (if any).
     */
    private String invokedBusinessInterfaceName = null;

    /**
     * This request is part of a remote request ? When we invoke remote request,
     * there is at the end a local request but it shouldn't be seen as a local
     * request
     */
    private boolean calledFromRemoteRequest = false;

    /**
     * Build a new Local Request.
     * @param methodHash the hash of the method
     * @param args the arguments of the method
     * @param beanId the bean ID (stateful)
     * @param invokedBusinessInterfaceName the name of the invoked business
     *        interface (may be null)
     */
    public EJBLocalRequestImpl(final Long methodHash, final Object[] args, final Long beanId,
            final String invokedBusinessInterfaceName) {
        this.methodHash = methodHash;
        this.args = args;
        this.beanId = beanId;
        this.invokedBusinessInterfaceName = invokedBusinessInterfaceName;
    }


    /**
     * @see <a * href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method hashing of RMI</a>
     * @return the hash of this method
     */
    public Long getMethodHash() {
        return this.methodHash;
    }

    /**
     * @return the id of the bean.
     */
    public Long getBeanId() {
        return this.beanId;
    }

    /**
     * @return the argument of the request (send by the client).
     */
    public Object[] getMethodArgs() {
        return this.args;
    }

    /**
     * @return name of the business interface used for invoking the method (if any).
     */
    public String getInvokedBusinessInterfaceName() {
        return this.invokedBusinessInterfaceName;
    }

    /**
     * @return true if this request is invoked remotely
     */
    public boolean isCalledFromRemoteRequest() {
        return this.calledFromRemoteRequest;
    }

    /**
     * Configure if this request is called remotely or not
     * @param calledFromRemoteRequest true/false
     */
    public void setCalledFromRemoteRequest(final boolean calledFromRemoteRequest) {
        this.calledFromRemoteRequest = calledFromRemoteRequest;
    }

}
