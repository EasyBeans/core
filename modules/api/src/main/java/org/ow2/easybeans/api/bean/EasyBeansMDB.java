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
 * $Id: EasyBeansMDB.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean;

import javax.transaction.xa.XAResource;

import org.ow2.easybeans.api.bean.lifecycle.EasyBeansMDBLifeCycle;

/**
 * Defines the interface of a MDB.
 * @author Florent Benoit
 */
public interface EasyBeansMDB extends EasyBeansBean, EasyBeansMDBLifeCycle {

    /**
     * Gets the XAResource of this message end point.
     * @return the XAResource of this message end point
     */
    XAResource getXaResource();

    /**
     * Sets the XAResource of this message end point.
     * @param xaResource the XAResource of this message end point
     */
     void setXaResource(final XAResource xaResource);

}
