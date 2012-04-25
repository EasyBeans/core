/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.topological;

/**
 * Defines a wrapper around a given object wrapped on a node.
 * This allows to wrap objects and use the topological sort
 * @param <T> the type of the object wrapped
 * @author Florent Benoit
 */
public class NodeWrapper<T> extends NodeImpl {

    /**
     * Object wrapped.
     */
    private T wrappedObject = null;

    /**
     * Build a new wrapper with the given name and wrapped object.
     * @param name the given node name
     * @param wrappedObject the wrapped object
     */
    public NodeWrapper(final String name, final T wrappedObject) {
        super(name);
        this.wrappedObject = wrappedObject;
    }


    /**
     * @return the wrapped Object.
     */
    public T getWrapped() {
        return this.wrappedObject;
    }
}
