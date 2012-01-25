/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: BindingManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.binding;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.binding.EZBBindingFactory;

/**
 * Manager of the binding factories.
 * @author Florent BENOIT
 */
public final class BindingManager {

    /**
     * List of binding factories.
     */
    private List<EZBBindingFactory> bindingFactories = null;

    /**
     * Unique instance of this manager.
     */
    private static BindingManager unique = null;

    /**
     * Hide constructor, should use getInstance() method.
     */
    private BindingManager() {
        this.bindingFactories = new ArrayList<EZBBindingFactory>();

        // Add the default factory
        this.bindingFactories.add(new JNDIBindingFactory());
    }

    /**
     * Register a factory.
     * @param factory the binding factory to add.
     */
    public void registerFactory(final EZBBindingFactory factory) {
        bindingFactories.add(factory);
    }

    /**
     * Unregister a factory.
     * @param factory the binding factory to remove.
     */
    public void unregisterFactory(final EZBBindingFactory factory) {
        bindingFactories.remove(factory);
    }

    /**
     * Gets the instance.
     * @return the unique instance.
     */
    public static BindingManager getInstance() {
        if (unique == null) {
            unique = new BindingManager();
        }
        return unique;
    }

    /**
     * @return list of factories
     */
    public List<EZBBindingFactory> getFactories() {
        return bindingFactories;
    }
}
