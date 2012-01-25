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
 * $Id: PoolWrapper.java 5511 2010-05-27 14:26:50Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session;

import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.pool.api.IPoolState;
import org.ow2.util.pool.api.Pool;
import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.enhanced.EnhancedPool;

/**
 * Wrapper of an Enhanced pool.
 * @param <PoolType> the kind of pool that is accepted.
 * @author Florent Benoit
 */
public class PoolWrapper<PoolType extends EasyBeansBean> implements Pool<PoolType, Long> {

    /**
     * The wrapped enhanced pool.
     */
    private EnhancedPool<PoolType> pool;

    /**
     * Build a new wrapper instance on the given pool.
     * @param pool the given pool to wrap
     */
    public PoolWrapper(final EnhancedPool<PoolType> pool) {
        this.pool = pool;
    }

    /**
     * Callback used to discard the given instance.
     * @param instance the given instance
     * @throws PoolException if discarding fails
     */
    public void discard(final PoolType instance) throws PoolException {
        this.pool.discard(instance);

    }

    /**
     * Get a new item on the pool.
     * @return a new element
     * @throws PoolException if there is a problem
     */
    public PoolType get() throws PoolException {
        return this.pool.get();
    }

    /**
     * Always throws an exception as the clue parameter is not managed.
     * @param clue the given clue that is provided
     * @throws PoolException as the clue parameter is not managed.
     * @return nothing as there is an exception thrown
     */
    public PoolType get(final Long clue) throws PoolException {
        throw new PoolException("No clue are managed by this wrapper");
    }

    /**
     * Callback used when the instance is released.
     * @param instance the given instance
     * @throws PoolException if there is a problem
     */
    public void release(final PoolType instance) throws PoolException {
        this.pool.release(instance);
    }

    /**
     * Defines the pool configuration for this pool.
     * @param poolConfiguration the pool configuration
     */
    public void setPoolConfiguration(final IPoolConfiguration poolConfiguration) {
        this.pool.setPoolConfiguration(poolConfiguration);
    }

    /**
     * Start the pool.
     * @throws PoolException if the startup fails
     */
    public void start() throws PoolException {
        this.pool.start();
    }

    /**
     * Stop the pool.
     * @throws PoolException if the shutdown fails
     */
    public void stop() throws PoolException {
        this.pool.stop();
    }

    /**
     * @return state of this pool.
     */
    public IPoolState getState() {
        return this.pool.getState();
    }

    /**
     * @return poolConfiguration the pool configuration
     */
    public IPoolConfiguration getPoolConfiguration() {
        return this.pool.getPoolConfiguration();
    }


}
