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
 * $Id: ManagementPool.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;

import org.ow2.easybeans.api.pool.EZBManagementPool;
import org.ow2.util.pool.impl.enhanced.EnhancedCluePoolFactory;
import org.ow2.util.pool.impl.enhanced.EnhancedPoolFactory;
import org.ow2.util.pool.impl.enhanced.api.IPool;
import org.ow2.util.pool.impl.enhanced.api.thread.IReusableThread;

/**
 * Defines an implementation of the management pool for EasyBeans.
 * @author Florent Benoit
 */
public class ManagementPool implements EZBManagementPool {

    /**
     * Management pool.
     */
    private IPool<IReusableThread> pool;

    /**
     * Enhanced factory.
     */
    private EnhancedPoolFactory enhancedPoolFactory = null;

    /**
     * Enhanced clue factory.
     */
    private EnhancedCluePoolFactory enhancedCluePoolFactory = null;


    /**
     * Build an instance for the given management pool.
     * @param pool the given pool
     */
    public ManagementPool(final IPool<IReusableThread> pool) {
        this.pool = pool;
        this.enhancedPoolFactory = new EnhancedPoolFactory(pool);
        this.enhancedCluePoolFactory = new EnhancedCluePoolFactory(pool);

    }

    /**
     * @return the management pool.
     */
    public IPool<IReusableThread> getPool() {
        return this.pool;
    }

    /**
     * @return enhanced pool factory.
     */
    public EnhancedPoolFactory getEnhancedPoolFactory() {
        return this.enhancedPoolFactory;
    }

    /**
     * @return enhanced clue pool factory.
     */
    public EnhancedCluePoolFactory getEnhancedCluePoolFactory() {
        return this.enhancedCluePoolFactory;
    }

}
