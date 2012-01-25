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
 * $Id: EZBJ2EEManagedObject.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import org.ow2.easybeans.component.api.EZBComponent;

/**
 * This interface represents an EasyBeans EJB Container.
 * @author missonng
 */
public interface EZBJ2EEManagedObject {
    /**
     * Get the J2EE managed object id.
     * @return The J2EE managed object id.
     */
    String getJ2EEManagedObjectId();

    /**
     * Get a reference to the first component matching the interface.
     * @param <T> The interface type.
     * @param itf The interface class.
     * @return The component.
     */
    <T extends EZBComponent> T getComponent(final Class<T> itf);
}
