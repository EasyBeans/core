/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2010 Bull S.A.
 * Contact: jonas-team@objectweb.org
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
 * $Id: AuditServiceContext.java 5629 2010-10-12 15:50:41Z benoitf $
 * --------------------------------------------------------------------------
 *
 */
package org.ow2.easybeans.component.audit.rmi.interceptor.jrmp;

import org.objectweb.carol.rmi.jrmp.interceptor.JServiceContext;
import org.ow2.util.auditreport.api.IAuditID;

/**
 * Context exchanged between client/server.
 * @author Florent Benoit
 */
public class AuditServiceContext implements JServiceContext {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = -4593624315256351694L;

    /**
     * Audit context id.
     */
    public static final int AUDIT_CTX_ID = 7986;

    /**
     * Audit ID.
     */
    private IAuditID auditID = null;

    /**
     * Constructor.
     * @param auditID the Audit ID
     */
    public AuditServiceContext(final IAuditID auditID) {
        this.auditID = auditID;
    }

    /**
     * Gets the Audit ID.
     * @return the Audit ID
     */
    public IAuditID getAuditID() {
        return this.auditID;
    }

    /**
     * @return the Context id.
     */
    public int getContextId() {
        return AUDIT_CTX_ID;
    }

}
