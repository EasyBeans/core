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
 * $Id: EventBeanInvocationBegin.java 5468 2010-04-21 12:44:14Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.bean;

import java.security.Principal;

import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocationBegin;
import org.ow2.easybeans.security.api.EZBSecurityContext;
import org.ow2.easybeans.security.propagation.context.SecurityContext;

/**
 * EasyBeans bean invocation begin events.
 * @author missonng
 */
public class EventBeanInvocationBegin extends AbstractEventBeanInvocation implements EZBEventBeanInvocationBegin {
    /**
     * The next invocation number.
     */
    private static long nextNumber = 1;

    /**
     * The bean invocation arguments.
     */
    private Object[] arguments = null;

    /**
     * Security context.
     */
    private EZBSecurityContext securityContext = null;

    /**
     * Caller is in runAs ?
     */
    private boolean runAsMode;

    /**
     * Stack trace of the caller.
     */
    private StackTraceElement[] stackTraceElements = null;

    /**
     * ID.
     */
    private String keyID = null;

    /**
     * The default constructor.
     * @param source The event source.
     * @param arguments The invocation arguments.
     * @param securityContext the security context
     * @param runAsMode runAsMode
     */
    public EventBeanInvocationBegin(final String source, final Object[] arguments, final EZBSecurityContext securityContext,
            final boolean runAsMode) {
        super(source, EventBeanInvocationBegin.nextNumber++);
        this.arguments = arguments;
        this.securityContext = securityContext;
        if (securityContext == null) {
            this.securityContext = new SecurityContext();
        }

        this.runAsMode = runAsMode;
    }

    /**
     * Get the bean invocation arguments.
     * @return The bean invocation arguments.
     */
    public Object[] getArguments() {
        return this.arguments;
    }

    /**
     * @return roles of the current authentified user (if any)
     */
    public Principal[] getCallerRoles() {
        return this.securityContext.getCallerRoles(this.runAsMode);
    }

    /**
     * @return username of the current authentified user (if any)
     */
    public Principal getCallerPrincipal() {
        return this.securityContext.getCallerPrincipal(this.runAsMode);
    }

    /**
     * @return stack elements
     */
    public StackTraceElement[] getStackTraceElements() {
        return this.stackTraceElements;
    }

    /**
     * Sets the stack trace elements.
     * @param stackTraceElements the given stack
     */
    public void setStackTraceElements(final StackTraceElement[] stackTraceElements) {
        this.stackTraceElements = stackTraceElements;
    }

    /**
     * @return the key id.
     */
    public String getKeyID() {
        return this.keyID;
    }

    /**
     * @param keyID the key id.
     */
    public void setKeyID(final String keyID) {
        this.keyID = keyID;
    }

}
