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
 * $Id: ClientInterceptor.java 5629 2010-10-12 15:50:41Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.audit.rmi.interceptor.jrmp;

import java.io.IOException;

import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInfo;
import org.objectweb.carol.rmi.jrmp.interceptor.JClientRequestInterceptor;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.auditreport.api.ICurrentInvocationID;
import org.ow2.util.auditreport.impl.CurrentInvocationID;

/**
 * Manages Audit ID propagation on the client's side. (only send request)
 * @author Florent Benoit
 */
public class ClientInterceptor implements JClientRequestInterceptor {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -7761476944697213731L;

    /**
     * Interceptor name.
     */
    private static final String NAME = ClientInterceptor.class.getName();

    /**
     * Default Constructor.
     */
    public ClientInterceptor() {
    }

    /**
     * Send client context with the request.
     * @param jClientRequestInfo jri the jrmp client info
     * @exception IOException if an exception occur with the ObjectOutput
     */
    public void send_request(final JClientRequestInfo jClientRequestInfo) throws IOException {
        // Get current ID
        ICurrentInvocationID currentInvocationID = CurrentInvocationID.getInstance();

        // Existing ID ?
        IAuditID localID = currentInvocationID.getAuditID();

        // If there is an ID, propagate it
        if (localID != null) {
            localID.increment();

            // Sends the current audit id object
            jClientRequestInfo.add_request_service_context(new AuditServiceContext(localID));
        }
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
