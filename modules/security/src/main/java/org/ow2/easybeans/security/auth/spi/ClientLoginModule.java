/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: ClientLoginModule.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.auth.spi;

import java.util.Map;

import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

import org.ow2.easybeans.security.api.EZBSecurityContext;
import org.ow2.easybeans.security.api.EZBSecurityCurrent;
import org.ow2.easybeans.security.propagation.context.SecurityContext;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;

/**
 * This class is used to propagate the Principal and roles to the server.<br>
 * It doesn't make any authentication.
 * @author Florent Benoit
 */
public class ClientLoginModule implements LoginModule {

    /**
     * Subject used.
     */
    private Subject subject = null;

    /**
     * Options for this login module.
     */
    private Map<String, ?> options = null;

    /**
     * Set SecurityContext for all the JVM ?.
     */
    private boolean globalContext = false;

    /**
     * Initialize this LoginModule.
     * <p>
     * This method is called by the <code>LoginContext</code> after this
     * <code>LoginModule</code> has been instantiated. The purpose of this
     * method is to initialize this <code>LoginModule</code> with the relevant
     * information. If this <code>LoginModule</code> does not understand any
     * of the data stored in <code>sharedState</code> or <code>options</code>
     * parameters, they can be ignored.
     * <p>
     * @param subject the <code>Subject</code> to be authenticated.
     *        <p>
     * @param callbackHandler a <code>CallbackHandler</code> for communicating
     *        with the end user (prompting for usernames and passwords, for
     *        example).
     *        <p>
     * @param sharedState state shared with other configured LoginModules.
     *        <p>
     * @param options options specified in the login <code>Configuration</code>
     *        for this particular <code>LoginModule</code>.
     */
    public void initialize(final Subject subject, final CallbackHandler callbackHandler,
            final Map<String, ?> sharedState,
            final Map<String, ?> options) {
        this.subject = subject;
        this.options = options;
    }

    /**
     * Method to authenticate a Subject (phase 1). The implementation of this
     * method authenticates a Subject. For example, it may prompt for Subject
     * information such as a username and password and then attempt to verify
     * the password. This method saves the result of the authentication attempt
     * as private state within the LoginModule.
     * @return true if the authentication succeeded, or false if this
     *         LoginModule should be ignored.
     * @throws LoginException if the authentication fails
     */
    public boolean login() throws LoginException {
        // set context for all the JVM or not ?
        String useGlobalCtx = (String) this.options.get("globalCtx");
        if ((useGlobalCtx != null) && (Boolean.valueOf(useGlobalCtx).booleanValue())) {
            this.globalContext = true;
        }
        return true;
    }

    /**
     * Method to commit the authentication process (phase 2). This method is
     * called if the LoginContext's overall authentication succeeded (the
     * relevant REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules
     * succeeded). If this LoginModule's own authentication attempt succeeded
     * (checked by retrieving the private state saved by the login method), then
     * this method associates relevant Principals and Credentials with the
     * Subject located in the LoginModule. If this LoginModule's own
     * authentication attempted failed, then this method removes/destroys any
     * state that was originally saved.
     * @return true if this method succeeded, or false if this LoginModule
     *         should be ignored.
     * @throws LoginException if the commit fails
     */
    public boolean commit() throws LoginException {


        // Gets the current
        EZBSecurityCurrent current = SecurityCurrent.getCurrent();

        // Build a new security context
        EZBSecurityContext context = new SecurityContext(this.subject);

        // Set it globally or in the current thread
        if (this.globalContext) {
            SecurityCurrent.setGlobalSecurityContext(context);
        } else {
            current.setSecurityContext(context);
        }

        return true;
    }

    /**
     * Method to abort the authentication process (phase 2). This method is
     * called if the LoginContext's overall authentication failed. (the relevant
     * REQUIRED, REQUISITE, SUFFICIENT and OPTIONAL LoginModules did not
     * succeed). If this LoginModule's own authentication attempt succeeded
     * (checked by retrieving the private state saved by the login method), then
     * this method cleans up any state that was originally saved.
     * @return true if this method succeeded, or false if this LoginModule
     *         should be ignored.
     * @throws LoginException if the abort fails
     */
    public boolean abort() throws LoginException {

        // Do nothing (as all is done in the commit() phase)
        return true;
    }

    /**
     * Method which logs out a Subject. An implementation of this method might
     * remove/destroy a Subject's Principals and Credentials.
     * @return true if this method succeeded, or false if this LoginModule
     *         should be ignored.
     * @throws LoginException if the logout fails
     */
    public boolean logout() throws LoginException {

        // Build a new anonymous context
        SecurityContext context = new SecurityContext(SecurityContext.ANONYMOUS_SUBJECT);

        // Gets the current
        EZBSecurityCurrent current = SecurityCurrent.getCurrent();

        // Set it globally or in the current thread
        if (this.globalContext) {
            SecurityCurrent.setGlobalSecurityContext(context);
        } else {
            current.setSecurityContext(context);
        }

        return true;

    }

}
