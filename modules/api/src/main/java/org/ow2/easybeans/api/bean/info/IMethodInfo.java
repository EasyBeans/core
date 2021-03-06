/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: IMethodInfo.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

import java.util.List;


/**
 * Info of a method.
 * @author Florent Benoit
 */
public interface IMethodInfo {

    /**
     * @return name of the method
     */
    String getName();

    /**
     * @return parameters of the method
     */
    List<String> getParameters();

    /**
     * @return the return type of the method
     */
    String getReturnType();

    /**
     * @return exception of this method.
     */
    List<String> getExceptions();

    /**
     * @return descriptor of the method
     */
    String getDescriptor();

    /**
     * @return true if this method is transacted
     */
    boolean isTransacted();

    /**
     * @return AccessTimeout
     */
    IAccessTimeoutInfo getAccessTimeout();

    /**
     * @return locking strategy
     */
    ILockTypeInfo getLockType();

    /**
     * @return true if this method is afterBeginMethod
     */
    boolean isAfterBegin();

    /**
     * @return true if this method is beforeCompletionMethod
     */
    boolean isBeforeCompletion();

    /**
     * @return true if this method is afterCompletionMethod
     */
    boolean isAfterCompletion();


}
