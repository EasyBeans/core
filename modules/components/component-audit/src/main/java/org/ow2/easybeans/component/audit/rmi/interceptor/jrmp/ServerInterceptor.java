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
 * $Id: ServerInterceptor.java 5629 2010-10-12 15:50:41Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.audit.rmi.interceptor.jrmp;

import java.io.IOException;

import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JServerRequestInterceptor;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.auditreport.api.ICurrentInvocationID;
import org.ow2.util.auditreport.impl.CurrentInvocationID;

/**
 * @author Florent Benoit
 */
public class ServerInterceptor implements JServerRequestInterceptor {

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
        AuditServiceContext auditServiceContext = (AuditServiceContext) jServerRequestInfo
                .get_request_service_context(AuditServiceContext.AUDIT_CTX_ID);
        if (auditServiceContext != null) {
            // Gets Audit ID received by client
            IAuditID clientID = auditServiceContext.getAuditID();

            // Get current object
            ICurrentInvocationID currentInvocationID = CurrentInvocationID.getInstance();

            // If client ID is null, it will be seen as a new invocation
            currentInvocationID.init(clientID);
        }

    }

    /**
     * Send reply with context to the client.
     * @param jServerRequestInfo the jrmp server request information
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_reply(final JServerRequestInfo jServerRequestInfo) throws IOException {
        // cleanup
        CurrentInvocationID.getInstance().setAuditID(null);
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
