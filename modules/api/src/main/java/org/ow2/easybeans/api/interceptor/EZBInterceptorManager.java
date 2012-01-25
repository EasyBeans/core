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
 * $Id: EZBInterceptorManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.interceptor;

import org.ow2.easybeans.api.container.EZBEJBContext;

/**
 * Manage the interceptors and the lifecycle of the interceptors.
 * @author Florent Benoit
 */
public interface EZBInterceptorManager {

    /**
     * Initialize the interceptors instance.
     */
    void injectedByEasyBeans();

    /**
     * @return the EasyBeans Context.
     */
    EZBEJBContext<?> getEasyBeansContext();

    /**
     * Sets the EasyBeans Context.
     * @param ezbejbcontext the given context.
     */
    void setEasyBeansContext(EZBEJBContext<?> ezbejbcontext);

    /**
     * Gets the interceptor specified by its classname.
     * @param classname the given class name
     * @return the instance of the interceptor
     */
    Object getInterceptor(final String classname);

}
