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
 * $Id: ServerSecurityInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.propagation.rmi.jrmp.interceptors;

import java.io.IOException;

import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInfo;
import org.ow2.easybeans.security.propagation.context.SecurityContext;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;

/**
 * @author Florent Benoit
 */
public class ServerSecurityInterceptor implements JServerRequestInterceptor {

    /**
     * Name of the interceptor.
     */
    private static final String NAME = "JRMPServerSecurityInterceptor";

    /**
     * Receive request from client.
     * @param jServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occurs with the ObjectOutput
     */
    public void receive_request(final JServerRequestInfo jServerRequestInfo) throws IOException {
        // Check if a security context was received
        SecurityServiceContext securityServiceContext = (SecurityServiceContext) jServerRequestInfo
                .get_request_service_context(SecurityServiceContext.SEC_CTX_ID);
        if (securityServiceContext != null) {
            // Sets Security context received by client
            SecurityCurrent.getCurrent().setSecurityContext(securityServiceContext.getSecurityContext());
        }
    }

    /**
     * Send reply with context to the client.
     * @param jServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_reply(final JServerRequestInfo jServerRequestInfo) throws IOException {
        SecurityCurrent.getCurrent().setSecurityContext(new SecurityContext());
    }

    /**
     * Gets the name of this interceptor.
     * @return name of the interceptor.
     */
    public String name() {
        return NAME;
    }

    /**
     * Send exception with context. Not used.
     * @param jServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_exception(final JServerRequestInfo jServerRequestInfo) throws IOException {

    }

    /**
     * Not used by this interceptor.
     * @param jServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_other(final JServerRequestInfo jServerRequestInfo) throws IOException {

    }

}
