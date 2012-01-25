/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: Components.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component;

import java.util.List;

import org.ow2.easybeans.component.api.EZBComponent;

/**
 * All components that have been configured.
 * @author Florent Benoit
 *
 */
public class Components {

    /**
     * List of EZBComponent.
     */
    private List<EZBComponent> components = null;

    /**
     * Gets the list of components.
     * @return the list of components.
     */
    public List<EZBComponent> getEZBComponents() {
        return components;
    }

    /**
     * Sets the list of components.
     * @param components the list of components.
     */
    public void setEZBComponents(final List<EZBComponent> components) {
        this.components = components;
    }



}
