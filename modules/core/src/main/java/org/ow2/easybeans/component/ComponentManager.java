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
 * $Id: ComponentManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ow2.easybeans.api.components.EZBComponentManager;
import org.ow2.easybeans.api.components.EZBComponentRegistry;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Create and destroy components.
 * @author Florent Benoit
 */
public class ComponentManager implements EZBComponentManager {

    /**
     * If Component classname ends with "Component", safely remove it.
     */
    private static final String COMPONENT_STR = "Component";

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(ComponentManager.class);

    /**
     * Components names that are managed.
     */
    private List<String> componentNames = null;

    /**
     * Components objects. (that were set by configuration).
     */
    private Components components = null;


    /**
     * Link to the registry of components (key=component name/value=EZB component).
     */
    private ComponentRegistry componentRegistry = null;

    /**
     * Build a component manager.
     */
    public ComponentManager() {
        this.componentRegistry = new ComponentRegistry();
        this.componentNames = new ArrayList<String>();
    }

    /**
     * Build a new component manager with the given set of components.
     * @param components the given set of components
     */
    public ComponentManager(final Components components) {
        this();
        setComponents(components);
    }


    /**
     * Gets the set of components.
     * @return the set of components.
     */
    public Components getComponents() {
        return this.components;
    }

    /**
     * Sets the components object.
     * @param components the set of components.
     */
    public void setComponents(final Components components) {
        this.components = components;
    }

    /**
     * Add the given component.
     * @param component the component to register.
     * @throws EZBComponentException if the component is not added.
     */
    public void addComponent(final EZBComponent component) throws EZBComponentException {
        // Add component
        addComponent(getComponentName(component), component);
    }

    /**
     * Remove the given component.
     * @param component the component to unregister.
     * @throws EZBComponentException if the component is not removed.
     */
    public void removeComponent(final EZBComponent component) throws EZBComponentException {
        // Remove component
        String componentName = this.componentRegistry.getComponentName(component);
        this.componentNames.remove(componentName);
        this.componentRegistry.unregister(componentName);
    }

    /**
     * Gets the name for a given component.
     * @param component the component instance.
     * @return the name of the component.
     */
    private String getComponentName(final EZBComponent component) {
        // get name
        String componentName = component.getClass().getCanonicalName();

        // exist ? (increment counter to get an unique id)
        int index = 2;
        if (this.componentNames.contains(componentName)) {
            while (this.componentNames.contains(componentName)) {
                componentName = componentName + (index++);
            }
        }
        return componentName;
    }

    /**
     * Add a component.
     * @param componentName the name of the component to add
     * @param component the component to add.
     * @throws EZBComponentException if adds fails.
     */
    private void addComponent(final String componentName, final EZBComponent component) throws EZBComponentException {
        // register component
        this.componentRegistry.register(componentName, component);

        // add to manage list
        this.componentNames.add(componentName);
    }

    /**
     * Init the components by calling init() method.
     * @param registerComponents if components should be registered or not.
     * @throws EZBComponentException if initialization fails
     */
    public void initComponents(final boolean registerComponents) throws EZBComponentException {

        // Exit soon if there is no components
        if (this.components == null) {
            return;
        }

        // Register component

        List<EZBComponent> componentList = this.components.getEZBComponents();
        if (componentList != null) {
            if (registerComponents) {
                for (EZBComponent component : componentList) {
                    addComponent(component);
                }
            }


            // Call init method if any on each component
            for (String componentName : this.componentNames) {
                EZBComponent component = this.componentRegistry.getComponent(componentName);
                component.init();
            }

        }
    }

    /**
     * Start the components.
     * @throws EZBComponentException if starting is failing
     */
    public void startComponents() throws EZBComponentException {

        StringBuilder sb = new StringBuilder();
        sb.append("[ Component(s) started : ");

        // Call init method if any on each component
        for (String componentName : this.componentNames) {
            EZBComponent component = this.componentRegistry.getComponent(componentName);
            component.start();

            // append the component name
            String name = component.getClass().getSimpleName();
            // remove "Component" substring if any
            if (name.endsWith(COMPONENT_STR)) {
                name = name.substring(0, name.lastIndexOf(COMPONENT_STR));
            }
            sb.append(name);
            sb.append(" ");
        }

        sb.append("]");
        this.logger.info(sb.toString());

    }

    /**
     * Stop the components.
     */
    public void stopComponents() {

        // Call stop method if any on each component in the reverse order of the start.
        int size = this.componentNames.size();
        for (int i = size - 1; i >= 0; i--) {
            String componentName = this.componentNames.get(i);
            EZBComponent component = this.componentRegistry.getComponent(componentName);
            try {
                component.stop();
            } catch (EZBComponentException e) {
                this.logger.error("Cannot stop component with name '" + componentName + "'.", e);
            }
        }
    }

    /**
     * @return the component registry used by this manager.
     */
    public EZBComponentRegistry getComponentRegistry() {
        return this.componentRegistry;
    }

    /**
     * Get a reference to the first component matching the interface.
     * @param <T> The interface type.
     * @param itf The interface class.
     * @return The component.
     */
    public <T extends EZBComponent> T getComponent(final Class<T> itf) {
        try {
            return getComponentRegistry().getComponents(itf).get(0);
        } catch (IndexOutOfBoundsException e) {
            return getAndRegisterComponent(itf);
        }

    }
    /**
     * Allows to get and register a component.
     * @param <T> the generics
     * @param itf the itf of the component.
     * @return the first component that matches the <code>itf</code> component.
     */
    @SuppressWarnings("unchecked")
    private <T extends EZBComponent> T getAndRegisterComponent(final Class<T> itf) {
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
        try {
            for (EZBComponent component : this.components.getEZBComponents()) {
                // Component is implemeting the given interface ?
                if (Arrays.asList(component.getClass().getInterfaces()).contains(itf)) {
                    matchComponents.add((T) component);
                    try {
                        addComponent(component);
                    } catch (EZBComponentException e) {
                        this.logger.debug("Exception in component manager: ", e);
                    }

                }
            }
            return matchComponents.get(0);
        } catch (Exception e) {
            this.logger.debug("Exception in component manager", e);
            return null;
        }
    }

    /**
     * Allow to get a reference on another component.
     * @param componentName the name of the component
     * @return the component.
     */
    public EZBComponent getComponent(final String componentName) {
        return this.componentRegistry.getComponent(componentName);
    }


}
