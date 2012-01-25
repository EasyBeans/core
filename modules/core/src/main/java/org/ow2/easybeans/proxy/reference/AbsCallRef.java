/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: AbsCallRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.reference;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.binding.EZBRef;

/**
 * Define a common Referenceable object used by local or remote EJB.
 * @author Florent Benoit
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public abstract class AbsCallRef implements EZBRef {

    /**
     * Property used for referencing the container ID.
     */
    public static final String CONTAINER_ID = "containerID";

    /**
     * Property used for referencing the name of the factory.
     */
    public static final String FACTORY_NAME = "factoryName";

    /**
     * Property used for referencing the interface class name.
     */
    public static final String INTERFACE_NAME = "interfaceClassName";

    /**
     * Property used for using an unique ID or not.
     */
    public static final String USE_ID = "useID";

    /**
     * Name of the interface class.
     */
    private String itfClassName = null;

    /**
     * Container id.
     */
    private String containerId = null;

    /**
     * Factory name.
     */
    private String factoryName = null;

    /**
     * useID : true if all instance build with this ref are unique (stateful), false if it references the same object (stateless).
     */
    private boolean useID;

    /**
     * JNDI name used for this reference.
     */
    private String jndiName = null;

    /**
     * Factory of this reference. (Transient value)
     */
    private transient Factory<?, ?> factory = null;


    /**
     * Constructor : build a reference.
     * @param itfClassName the name of the interface.
     * @param containerId the ID of the container.
     * @param factoryName the name of the factory
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public AbsCallRef(final String itfClassName, final String containerId, final String factoryName, final boolean useID) {
        this.itfClassName = itfClassName;
        this.containerId = containerId;
        this.factoryName = factoryName;
        this.useID = useID;
    }

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    public abstract Reference getReference() throws NamingException;


    /**
     * Adds some settings to the reference.
     * @param reference the reference to configure
     */
    protected void updateRefAddr(final Reference reference) {
        reference.add(new StringRefAddr(CONTAINER_ID, this.containerId));
        reference.add(new StringRefAddr(FACTORY_NAME, this.factoryName));
        reference.add(new StringRefAddr(INTERFACE_NAME, this.itfClassName));
        reference.add(new StringRefAddr(USE_ID, Boolean.toString(this.useID)));
    }

    /**
     * Gets the interface class name.
     * @return the name of the interface.
     */
    public String getItfClassName() {
        return this.itfClassName;
    }

    /**
     * Sets the JNDI name of this reference.
     * @param jndiName the JNDI name value.
     */
    public void setJNDIName(final String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * Gets the JNDI name of this reference.
     * @return JNDI name of this reference.
     */
    public String getJNDIName() {
        return this.jndiName;
    }

    /**
     * Sets the factory of this reference.
     * @param factory the given factory.
     */
    public void setFactory(final Factory<?, ?> factory) {
        this.factory = factory;
    }

    /**
     * Gets the factory of this reference.
     * @return the factory linked to this reference object.
     */
    public Factory<?, ?> getFactory() {
        return this.factory;
    }

}
