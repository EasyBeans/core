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
 * $Id: EasyBeansSFSB.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean;

import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSFSBLifeCycle;

/**
 * Defines the interface of a stateful session bean.
 * @author Florent Benoit
 */
public interface EasyBeansSFSB extends EasyBeansSB<EasyBeansSFSB>, EasyBeansSFSBLifeCycle {

    /**
     * Gets the stateful ID of the current bean.
     * @return the id of this bean.
     */
    Long getEasyBeansStatefulID();

    /**
     * Sets the ID for this stateful bean.
     * @param easyBeansStatefulID the id to set.
     */
    void setEasyBeansStatefulID(final Long easyBeansStatefulID);

    /**
     * <p>Boolean indicating if the Bean is currently under a running transaction.</p>
     * <p>If true it prevent the been from being collected due to an StatefulTimeout.</p>
     * <p>If the bean metadata doesn't define a Stateful Timeout then it should return <code>null</code></p>
     * @return Boolean indicating if the Bean is currently under a running transaction
     */
    Boolean getInTransaction();

    /**
     * Sets the boolean indicating if the Bean is currently under a running transaction
     * @param inTransaction true if the bean is under a running transaction
     */
    void setInTransaction(final Boolean inTransaction);

    /**
     * <p>The next timeout deadline</p>
     * <p>If the bean metadata doesn't define a Stateful Timeout then it should return <code>null</code></p>
     * @return The next timeout deadline to be compared with {@link System#nanoTime()}
     */
    Long getStatefulTimeout();

    /**
     * Sets the next timeout deadline
     * @param statefulTimeout The next timeout deadline to be compared with {@link System#nanoTime()}
     */
    void setStatefulTimeout(final Long statefulTimeout);
}
