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
 * $Id: SecurityContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.propagation.context;

import java.io.Serializable;
import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.security.auth.Subject;

import org.ow2.easybeans.security.api.EZBSecurityContext;
import org.ow2.easybeans.security.struct.JGroup;
import org.ow2.easybeans.security.struct.JPrincipal;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Security Context that is exchanged and propagated from clients to beans.<br>
 * This is also why it is a serializable object (as it has to be exchanged).<br>
 * The security contains allow to get the current principal and the roles
 * associated to this principal.<br>
 * RunAs mode is managed by keeping the previous security context.
 * @author Florent Benoit
 */
public final class SecurityContext implements EZBSecurityContext, Serializable {

    /**
     * UID for serialization.
     */
    private static final long serialVersionUID = 6612085599241360430L;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(SecurityContext.class);

    /**
     * Anonymous user name.
     */
    private static final String ANONYMOUS_USER = "EasyBeans/Anonymous";

    /**
     * Anonymous role.
     */
    private static final String ANONYMOUS_ROLE = "anonymous";

    /**
     * Anonymous subject (not authenticated).
     */
    public static final Subject ANONYMOUS_SUBJECT = buildAnonymousSubject();

    /**
     * Current subject (subject that has been authenticated).<br>
     * By default, it is the anonymous subject.
     */
    private Subject subject = ANONYMOUS_SUBJECT;

    /**
     * caller subject in run-as mode<br>
     * In run-as case, the run-as subject is set as the current subject, and the
     * previous one is kept.<br>
     * This previous subject is used to get the caller on the run-as bean.
     */
    private Subject callerInRunAsModeSubject = null;

    /**
     * Default private constructor.
     */
    public SecurityContext() {

    }

    /**
     * Build a security context with the given subject.
     * @param subject the given subject.
     */
    public SecurityContext(final Subject subject) {
        this.subject = subject;
    }

    /**
     * Enters in run-as mode with the given subject.<br>
     * The previous subject is stored and will be restored when run-as mode will
     * be ended.
     * @param runAsSubject the subject to used in run-as mode.
     * @return the previous subject.
     */
    public Subject enterRunAs(final Subject runAsSubject) {
        // keep previous
        callerInRunAsModeSubject = subject;

        // update the new one
        subject = runAsSubject;

        // return previous.
        return callerInRunAsModeSubject;
    }

    /**
     * Ends the run-as mode and then restore the context stored by container.
     * @param oldSubject subject kept by container and restored.
     */
    public void endsRunAs(final Subject oldSubject) {
        subject = oldSubject;

        // cancel caller of run-as subject (run-as mode has ended)
        callerInRunAsModeSubject = null;
    }

    /**
     * Gets the caller's principal.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return principal of the caller.
     */
    public Principal getCallerPrincipal(final boolean runAsBean) {
        Subject subject = null;

        // in run-as mode, needs to return callerInRunAsModeSubject's principal.
        if (runAsBean && callerInRunAsModeSubject != null) {
            subject = callerInRunAsModeSubject;
        } else {
            subject = this.subject;
        }

        // Then, takes the first principal found. (which is not a role)
        for (Principal principal : subject.getPrincipals(Principal.class)) {
            if (!(principal instanceof Group)) {
                return principal;
            }
        }

        // Principal was not found, severe problem as it should be there. Maybe
        // the subject was not built correctly.
        logger.error("No principal found in the current subject. Authentication should have failed when populating subject");
        throw new IllegalStateException(
                "No principal found in the current subject. Authentication should have failed when populating subject");
    }

    /**
     * Gets the caller's roles.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return list of roles of the caller.
     */
    public List<? extends Principal> getCallerRolesList(final boolean runAsBean) {
        Subject subject = null;

        // in run-as mode, needs to return callerInRunAsModeSubject's principal.
        if (runAsBean && callerInRunAsModeSubject != null) {
            subject = callerInRunAsModeSubject;
        } else {
            subject = this.subject;
        }

        // Then, takes all the roles found in this principal.
        for (Principal principal : subject.getPrincipals(Principal.class)) {
            if (principal instanceof Group) {
                return Collections.list(((Group) principal).members());
            }
        }

        // Principal was not found, severe problem as it should be there. Maybe
        // the subject was not built correctly.
        logger.error("No role found in the current subject. Authentication should have failed when populating subject");
        throw new IllegalStateException(
                "No role found in the current subject. Authentication should have failed when populating subject");
    }

    /**
     * Gets the caller's roles.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return array of roles of the caller.
     */
    public Principal[] getCallerRoles(final boolean runAsBean) {
        List<? extends Principal> callerRoles = getCallerRolesList(runAsBean);
        return callerRoles.toArray(new Principal[callerRoles.size()]);
    }

    /**
     * Build an anonymous subject when no user is authenticated.<br>
     * This is required as getCallerPrincipal() should never return null.
     * @return anonymous subject.
     */
    private static Subject buildAnonymousSubject() {
        return buildSubject(ANONYMOUS_USER, new String[] {ANONYMOUS_ROLE});
    }


    /**
     * Build a subject with the given user name and the list of roles.<br>
     * @param userName given username
     * @param roleArray given array of roles.
     * @return built subject.
     */
    public static Subject buildSubject(final String userName, final String[] roleArray) {
        List<String> roles = new ArrayList<String>();
        if (roleArray != null) {
            for (String role : roleArray) {
                roles.add(role);
            }
        }
        return buildSubject(userName, roles);
    }

    /**
     * Build a subject with the given user name and the list of roles.<br>
     * @param userName given username
     * @param roleList given list of roles.
     * @return built subject.
     */
    public static Subject buildSubject(final String userName, final List<String> roleList) {
        Subject subject = new Subject();

        // Add principal name
        Principal principalName = new JPrincipal(userName);
        subject.getPrincipals().add(principalName);

        // Add roles for this principal
        Group roles = new JGroup("roles");
        if (roleList != null) {
            for (String role : roleList) {
                roles.addMember(new JPrincipal(role));
            }
        }
        subject.getPrincipals().add(roles);

        return subject;
    }

}
