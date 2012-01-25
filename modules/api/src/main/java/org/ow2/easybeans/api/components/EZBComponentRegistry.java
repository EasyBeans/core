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
 * $Id: EZBComponentRegistry.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.components;

import java.util.List;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;

/**
 * This interface is the registry of the EasyBeans Components.
 * @author Florent Benoit
 */
public interface EZBComponentRegistry {

    /**
     * Allow to get a reference on another component.
     * @param componentName the name of the component
     * @return the component.
     */
    EZBComponent getComponent(final String componentName);

    /**
     * @param component EZBComponent instance.
     * @return Returns the component name from the EZBComponent instance.
     */
    String getComponentName(final EZBComponent component);

    /**
     * Get the components that implements the given interface.
     * @param itf the given interface
     * @return an array of components implementing the given interface
     * @param <T> an interface extending EZBComponent.
     */
    <T extends EZBComponent> List<T> getComponents(final Class<T> itf);

    /**
     * Register a component.
     * @param componentName the name of the component to register
     * @param component the component to register.
     * @throws EZBComponentException if registering fails.
     */
    void register(final String componentName, final EZBComponent component) throws EZBComponentException;

    /**
     * Unregister a component.
     * @param componentName the component name to unregister.
     * @throws EZBComponentException if unregistering fails.
     */
    void unregister(final String componentName) throws EZBComponentException;

    /**
     * Unregister a component.
     * @param component the instance of the component to unregister.
     * @throws EZBComponentException if unregistering fails.
     */
    void unregister(final EZBComponent component) throws EZBComponentException;
}
