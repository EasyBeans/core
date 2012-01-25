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
 * $Id: EZBEventBeanInvocationBegin.java 5468 2010-04-21 12:44:14Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.event.bean;

import java.security.Principal;

/**
 * Interface for all bean invocation begin event.
 * @author Florent Benoit
 */
public interface EZBEventBeanInvocationBegin extends EZBEventBeanInvocation {
    /**
     * Get the bean invocation arguments.
     * @return The bean invocation arguments.
     */
    Object[] getArguments();

    /**
     * @return roles of the current authentified user (if any)
     */
    Principal[] getCallerRoles();

    /**
     * @return username of the current authentified user (if any)
     */
    Principal getCallerPrincipal();

    /**
     * @return stack elements
     */
    StackTraceElement[] getStackTraceElements();

    /**
     * @return the key id.
     */
    String getKeyID();

}
