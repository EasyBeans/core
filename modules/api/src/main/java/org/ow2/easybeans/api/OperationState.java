/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: OperationState.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

/**
 * Defines types of operations.
 * @author Florent Benoit
 */
public enum OperationState {

    /**
     * Container is calling dependency injection methods.
     */
    DEPENDENCY_INJECTION,

    /**
     * LifeCycle callback interceptor methods.
     */
    LIFECYCLE_CALLBACK_INTERCEPTOR,

    /**
     * afterBegin method of a SessionSynchronization.
     */
    AFTER_BEGIN,

    /**
     * beforeCompletion method of a SessionSynchronization.
     */
    BEFORE_COMPLETION,

    /**
     * afterCompletion method of a SessionSynchronization.
     */
    AFTER_COMPLETION,

    /**
     * Business method.
     */
    BUSINESS_METHOD


}
