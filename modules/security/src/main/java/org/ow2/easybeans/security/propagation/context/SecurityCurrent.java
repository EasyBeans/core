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
 * $Id: SecurityCurrent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.propagation.context;

import org.ow2.easybeans.security.api.EZBSecurityContext;
import org.ow2.easybeans.security.api.EZBSecurityCurrent;

/**
 * Manages the current security context associated to the current thread.
 * @author Florent Benoit
 */
public class SecurityCurrent implements EZBSecurityCurrent {

    /**
     * Inherited Local thread used to keep the security context.
     */
    private static InheritableThreadLocal<EZBSecurityContext> threadLocal;

    /**
     * Static Security Context that is applied on all threads (used for heavy
     * client).
     */
    private static EZBSecurityContext globalContext = null;

    /**
     * Default security context.
     */
    private static final EZBSecurityContext DEFAULT_CTX = new SecurityContext();

    /**
     * Init the thread
     */
    static {
        threadLocal = new InheritableThreadLocal<EZBSecurityContext>();
        threadLocal.set(new SecurityContext());
    }

    /**
     * Unique instance of this current object.
     */
    private static EZBSecurityCurrent unique = null;

    /**
     * Return the unique instance of this object.
     * @return SecurityCurrent return the current
     */
    public static EZBSecurityCurrent getCurrent() {
        if (unique == null) {
            // Build a default implementation
            unique = new SecurityCurrent();
        }
        return unique;
    }

    /**
     * Associates the given security context to the current thread.
     * @param securityContext Security context to associate to the current
     *        thread.
     */
    public void setSecurityContext(final EZBSecurityContext securityContext) {
        threadLocal.set(securityContext);
    }

    /**
     * Associates the given security context to all threads (JVM).
     * @param securityContext Security context to associate to the JVM
     */
    public static void setGlobalSecurityContext(final EZBSecurityContext securityContext) {
        globalContext = securityContext;
    }

    /**
     * Gets the current context.
     * @return SecurityContext return the Security context associated to the
     *         current thread or the JVM
     */
    public EZBSecurityContext getSecurityContext() {
        if (globalContext != null) {
            return globalContext;
        }
        if (threadLocal.get() != null) {
            return threadLocal.get();
        }

        // else, never null context.
        return DEFAULT_CTX;
    }


    /**
     * Sets the security current instance to use.
     * @param current the given instance.
     */
    public static void setSecurityCurrent(final EZBSecurityCurrent current) {
        if (unique != null) {
            throw new IllegalStateException("Unable to set the unique instance. It is already set");
        }
        unique = current;
    }

}
