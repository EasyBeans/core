/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: ComponentRegistry.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.ow2.easybeans.api.components.EZBComponentRegistry;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Registry that manages components. It allows to get components.
 * @author Florent Benoit
 */
public class ComponentRegistry implements EZBComponentRegistry {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ComponentRegistry.class);

    /**
     * Map of components.<br/> Name <--> Implementation of the component
     */
    private Map<String, EZBComponent> components = null;

    /**
     * Constructor.
     */
    public ComponentRegistry() {
        // init map
        this.components = new HashMap<String, EZBComponent>();
    }

    /**
     * Register a component.
     * @param componentName the name of the component to register
     * @param component the component to register.
     * @throws EZBComponentException if registering fails.
     */
    public void register(final String componentName, final EZBComponent component) throws EZBComponentException {
        // Existing ?
        if (this.components.containsKey(componentName)) {
            throw new EZBComponentException("Cannot register the component with the name '" + componentName
                    + "'. There is an existing component with this name.");
        }

        this.logger.debug("Registering component with name {0}.", componentName);
        this.components.put(componentName, component);
    }

    /**
     * Unregister a component.
     * @param componentName the component name to unregister.
     * @throws EZBComponentException if unregistering fails.
     */
    public void unregister(final String componentName) throws EZBComponentException {
        // Exist ?
        if (!this.components.containsKey(componentName)) {
            throw new EZBComponentException("No component with the name '" + componentName
                    + "' found. Component not unregistered");
        }

        this.logger.info("Unregistering component with name {0}.", componentName);
        this.components.remove(componentName);
    }

    /**
     * Unregister a component.
     * @param component the instance of the component to unregister.
     * @throws EZBComponentException if unregistering fails.
     */
    public void unregister(final EZBComponent component) throws EZBComponentException {
        String name = null;

        // Find component
        Set<String> keys = this.components.keySet();
        for (String key : keys) {
            EZBComponent foundComponent = this.components.get(key);
            if (foundComponent.equals(component)) {
                // got it !
                name = key;
                break;
            }
        }
        // found --> unregister.
        if (name != null) {
            unregister(name);
        }
        throw new EZBComponentException("No component found in the registry with the given component '" + component + "'.");

    }

    /**
     * Allow to get a reference on another component.
     * @param componentName the name of the component
     * @return the component.
     */
    public EZBComponent getComponent(final String componentName) {
        return this.components.get(componentName);
    }

    /**
     * @param component EZBComponent instance.
     * @return Returns the component name from the EZBComponent instance.
     */
    public String getComponentName(final EZBComponent component) {

        // Iterates over the components to find the component's name
        String match = null;
        for (Iterator<String> i = this.components.keySet().iterator();
            i.hasNext() && (match == null);) {
            String key = i.next();
            EZBComponent candidate = this.components.get(key);
            if (component.equals(candidate)) {
                match = key;
                break;
            }
        }
        if (match == null) {
            throw new IllegalStateException("Each component should be registered in the registry. No component found for '"
                    + component + "'.");
        }
        return match;
    }


    /**
     * Get the components that implements the given interface.
     * @param itf the given interface
     * @return an array of components implementing the given interface
     * @param <T> an interface extending EZBComponent.
     */
    @SuppressWarnings("unchecked")
    public <T extends EZBComponent> List<T> getComponents(final Class<T> itf) {
        // Check not null
        if (itf == null) {
            throw new IllegalArgumentException("Cannot find component with a null interface");
        }

        // Check interface
        if (!itf.isInterface()) {
            throw new IllegalArgumentException("The given class '" + itf + "' is not an interface");
        }

        // Iterates over the components to find a matching component
        List<T> matchComponents = new ArrayList<T>();
        for (EZBComponent component : this.components.values()) {
            // Component is implemeting the given interface ?
            if (Arrays.asList(component.getClass().getInterfaces()).contains(itf)) {
                   matchComponents.add((T) component);
            }
        }
        return matchComponents;
    }
}
