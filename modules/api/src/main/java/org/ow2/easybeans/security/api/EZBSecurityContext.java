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
 * $Id: EZBSecurityContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.api;

import java.security.Principal;

import javax.security.auth.Subject;

/**
 * Interface used to describe operations on a security context.
 * @author Florent Benoit
 */
public interface EZBSecurityContext {

    /**
     * Gets the caller's principal.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return principal of the caller.
     */
    Principal getCallerPrincipal(final boolean runAsBean);

    /**
     * Enters in run-as mode with the given subject.<br>
     * The previous subject is stored and will be restored when run-as mode will
     * be ended.
     * @param runAsSubject the subject to used in run-as mode.
     * @return the previous subject.
     */
    Subject enterRunAs(final Subject runAsSubject);

    /**
     * Ends the run-as mode and then restore the context stored by container.
     * @param oldSubject subject kept by container and restored.
     */
    void endsRunAs(final Subject oldSubject);

    /**
     * Gets the caller's roles.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return array of roles of the caller.
     */
    Principal[] getCallerRoles(final boolean runAsBean);

}
