/**
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 1999-2004 Bull S.A.
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
 * $Id: SecurityServiceContext.java 5468 2010-04-21 12:44:14Z benoitf $
 * --------------------------------------------------------------------------
 *
 */
package org.ow2.easybeans.security.propagation.rmi.jrmp.interceptors;

import org.objectweb.carol.rmi.jrmp.interceptor.JServiceContext;
import org.ow2.easybeans.security.api.EZBSecurityContext;

/**
 * Context exchanged between client/server.
 * @author Florent Benoit
 */
public class SecurityServiceContext implements JServiceContext {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = 4150443412309988096L;

    /**
     * Security context id.
     */
    public static final int SEC_CTX_ID = 2503;

    /**
     * Security context.
     */
    private EZBSecurityContext securityContext = null;

    /**
     * Constructor.
     * @param securityContext the RMI (Serializable) Security Context
     */
    public SecurityServiceContext(final EZBSecurityContext securityContext) {
        this.securityContext = securityContext;
    }

    /**
     * Gets the security context.
     * @return the Security context
     */
    public EZBSecurityContext getSecurityContext() {
        return this.securityContext;
    }

    /**
     * @return the Context id.
     */
    public int getContextId() {
        return SEC_CTX_ID;
    }

}
