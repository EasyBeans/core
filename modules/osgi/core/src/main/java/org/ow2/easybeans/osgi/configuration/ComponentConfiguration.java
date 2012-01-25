/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: ComponentConfiguration.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.configuration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.ow2.easybeans.component.util.Property;

/**
 * Class representing the configuration of a given Easybeans component.
 * Components can be parent component (i.e the components that inherit from a
 * given class. This class is by default {@link org.ow2.easybeans.component.api.EZBComponent}) of child
 * components (i.e subcomponents of a given parent component).
 * @author David Alves
 */
public class ComponentConfiguration {

    /** The component class. */
    private Class<?> componentClass;

    /** The properties. */
    private Collection<Property> properties;

    /** The index. */
    private int index;

    /**
     * Instantiates a new component configuration.
     * @param componentObject the component object
     */
    public ComponentConfiguration(final Object componentObject) {
        this.componentClass = componentObject.getClass();
        properties = new ArrayList<Property>();
    }

    /**
     * Adds the property.
     * @param componentConfigurationProperty the given property
     */
    public void addProperty(final Property componentConfigurationProperty) {
        properties.add(componentConfigurationProperty);
    }

    /**
     * Gets the component class.
     * @return the componentClass
     */
    public Class<?> getComponentClass() {
        return componentClass;
    }

    /**
     * Gets the properties.
     * @return the properties
     */
    public Collection<Property> getProperties() {
        return properties;
    }

    /**
     * Gets the index.
     * @return the index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the index.
     * @param index the index to set
     */
    public void setIndex(final int index) {
        this.index = index;
    }

    public String getComponentSymbolicName() {
        return this.componentClass.getName().toLowerCase();
    }

    @SuppressWarnings("unchecked")
    public Dictionary getConfigurationAsDictionary() {
        Hashtable properties = new Hashtable();
        for (Property property : this.properties) {
            properties.put(property.getName(), property.getValue());
        }
        return properties;
    }
}
