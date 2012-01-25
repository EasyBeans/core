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
 * $Id: EJBMBean.java 5511 2010-05-27 14:26:50Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.MBeanException;

import org.ow2.easybeans.container.AbsFactory;

/**
 * EJB JSR77 MBean.
 * @author Guillaume Sauthier
 *
 * @param <F> Bean Factory Type
 */
public abstract class EJBMBean<F extends AbsFactory> extends J2EEManagedObjectMBean<F> {

    /**
     * Creates an EJBMBean.
     * @throws MBeanException if creation fails.
     */
    public EJBMBean() throws MBeanException {
        super();
    }


    /**
     * @return items that are busy in the pool.
     */
    public int getPoolItemsBusy() {
        return getManagedComponent().getPool().getState().getBusyItemCount();
    }

    /**
     * @return items that are available in the pool.
     */
    public int getPoolItemsAvailable() {
        return getManagedComponent().getPool().getState().getAvailableItemCount();
    }

    /**
     * @return miminum size of the pool.
     */
    public int getPoolMin() {
        return getManagedComponent().getPool().getPoolConfiguration().getMin();
    }

    /**
     * @return maximum size of the pool.
     */
    public int getPoolMax() {
        return getManagedComponent().getPool().getPoolConfiguration().getMax();
    }

    /**
     * @return maximum size of the waiters.
     */
    public int getPoolMaxWaiters() {
        return getManagedComponent().getPool().getPoolConfiguration().getMaxWaiters();
    }

    /**
     * @return spare size of the pool.
     */
    public int getPoolSpare() {
        return getManagedComponent().getPool().getPoolConfiguration().getSpare();
    }

}
