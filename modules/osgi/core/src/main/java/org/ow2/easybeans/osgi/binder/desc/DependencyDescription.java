/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: DependencyDescription.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.desc;

import org.ow2.easybeans.osgi.annotation.Multiplicity;
import org.ow2.easybeans.osgi.binder.listener.IDependencyListener;

/**
 * Describes a dependency on an OSGi service interface.
 * @author Guillaume Sauthier
 */
public abstract class DependencyDescription {

    /**
     * Name of the dependency.
     */
    private final String name;

    /**
     * Java interface of the dependency.
     */
    private Class<?> serviceInterface;

    /**
     * Optional filtering support (LDAP filter).
     */
    private String filter;

    /**
     * Dependency cardinality.
     */
    private Multiplicity multiplicity;

    /**
     * Creates a new named dependency.
     * @param name name of the dependency
     */
    public DependencyDescription(final String name) {
        this.name = name;
    }

    /**
     * @param serviceInterface the serviceInterface to set
     */
    public void setServiceInterface(final Class<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    /**
     * @return the serviceInterface
     */
    public Class<?> getServiceInterface() {
        return serviceInterface;
    }

    /**
     * @param filter the filter to set
     */
    public void setFilter(final String filter) {
        this.filter = filter;
    }

    /**
     * @return the filter
     */
    public String getFilter() {
        return filter;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param multiplicity the multiplicity to set
     */
    public void setMultiplicity(final Multiplicity multiplicity) {
        this.multiplicity = multiplicity;
    }

    /**
     * @return the multiplicity
     */
    public Multiplicity getMultiplicity() {
        return multiplicity;
    }

    /**
     * Creates a new {@link IDependencyListener} object for the given instance.
     * @param instance instance to be injected
     * @return a new {@link IDependencyListener}
     */
    public abstract IDependencyListener createListener(final Object instance);

}
