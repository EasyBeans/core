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
 * $Id: JPoolWrapperFactory.java 5629 2010-10-12 15:50:41Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session;

import org.ow2.util.pool.api.PoolException;
import org.ow2.util.pool.impl.PoolEntryStatistics;
import org.ow2.util.pool.impl.PoolFactory;
import org.ow2.util.pool.impl.enhanced.api.basic.CreatePoolItemException;
import org.ow2.util.pool.impl.enhanced.api.clue.basiccluemanager.IClueAccessor;
import org.ow2.util.pool.impl.enhanced.manager.optional.IPoolItemRemoveManager;

/**
 * Wrapper for the Simple pool factory.
 * @param <InstanceType> the type instance.
 * @param <Clue> the clue's type.
 * @author Florent Benoit
 */
public class JPoolWrapperFactory<InstanceType, Clue> implements PoolFactory<InstanceType, Clue> {

    /**
     * Factory which manage instances.
     */
    private IPoolItemRemoveManager<InstanceType> factory;

    /**
     * Factory which manage clues.
     */
    private IClueAccessor<InstanceType, Clue> clueFactory;

    /**
     * Default constructor (no clue).
     * @param factory the instance factory
     */
    public JPoolWrapperFactory(final IPoolItemRemoveManager<InstanceType> factory) {
        this(factory, null);
    }

    /**
     * Constructor with a given clue's factory.
     * @param factory the instance factory
     * @param clueFactory the clue factory
     */
    public JPoolWrapperFactory(final IPoolItemRemoveManager<InstanceType> factory,
            final IClueAccessor<InstanceType, Clue> clueFactory) {
        this.factory = factory;
        this.clueFactory = clueFactory;
    }

    /**
     * Create a given instance with the given clue (if any).
     * @return the instance
     * @param clue the given clue
     * @throws PoolException if no instance is created
     */
    public InstanceType create(final Clue clue) throws PoolException {
        InstanceType instance = null;
        try {
            instance = this.factory.createPoolItem();
        } catch (CreatePoolItemException e) {
            throw new PoolException("Cannot create item", e);
        }

        // Update clue on the instance
        if (this.clueFactory != null) {
            this.clueFactory.setClue(instance, clue);
        }

        return instance;
    }

    /**
     * @return true if the instance is matching the given clue
     * @param instance the instance to check
     * @param clue the clue to check
     */
    public boolean isMatching(final InstanceType instance, final Clue clue) {
        if (this.clueFactory == null) {
            return true;
        }

        if (clue == null) {
            return true;
        }

        // Compare clue
        return clue.equals(this.clueFactory.getClue(instance));
    }

    /**
     * Remove the given instance.
     * @param instance the instance
     */
    public void remove(final InstanceType instance) {
        this.factory.poolItemRemoved(instance);

    }

    /**
     * Validate the given instance.
     * @param instance the instance
     * @param stats the given statistics
     * @return true if all is going fine
     */
    public boolean validate(final InstanceType instance, final PoolEntryStatistics stats) {
        return true;
    }

}
