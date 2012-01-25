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
 * $Id: ClientSecurityInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.propagation.rmi.jrmp.interceptors;

import java.io.IOException;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;

/**
 * Manages security propagation on the client's side. (only send request)
 * @author Florent Benoit
 */
public class ClientSecurityInterceptor implements JClientRequestInterceptor {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6761192579690917252L;

    /**
     * Interceptor name.
     */
    private static final String NAME = "JRMPClientSecurityInterceptor";

    /**
     * Default Constructor.
     */
    public ClientSecurityInterceptor() {
    }

    /**
     * Send client context with the request.
     * @param jClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_request(final JClientRequestInfo jClientRequestInfo) throws IOException {
        // Sends the current security Context object
        jClientRequestInfo.add_request_service_context(new SecurityServiceContext(SecurityCurrent.getCurrent()
                .getSecurityContext()));
    }

    /**
     * Gets the name of this interceptor.
     * @return name of the interceptor
     */
    public String name() {
        return NAME;
    }

    /**
     * No receive.
     * @param jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_reply(final JClientRequestInfo jri) throws IOException {
        // nothing
    }

    /**
     * Send client context in pool.
     * @param jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_poll(final JClientRequestInfo jri) throws IOException {
        // nothing
    }

    /**
     * Receive exception interception.
     * @param jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_exception(final JClientRequestInfo jri) throws IOException {
        // nothing
    }

    /**
     * Receive other interception.
     * @param jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void receive_other(final JClientRequestInfo jri) throws IOException {
        // nothing
    }

}
