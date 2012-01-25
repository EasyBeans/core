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
 * $Id: EasyBeansLifeCycle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.lifecycle;


/**
 * Defines the lifecycle of an enterprise bean.
 * @author Florent Benoit
 */
public interface EasyBeansLifeCycle {

    /**
     * The PostConstruct callback occurs before the first business method
     * invocation on the bean.<br>
     * This is at a point after which any dependency injection has been
     * performed by the container.<br>
     * The PostConstruct callback method executes in an unspecified transaction
     * and security context.
     */
    void postConstructEasyBeansLifeCycle();

    /**
     * The PreDestroy callback notification signals that the instance is in the
     * process of being removed by the container.<br>
     * In the PreDestroy callback method, the instance typically releases the
     * resources that it has been holding.<br>
     * The PreDestroy callback method executes in an unspecified transaction and
     * security context.<br>
     */
    void preDestroyEasyBeansLifeCycle();

}
