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
 * $Id: EZBBindingFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.binding;

/**
 * Interface used for each Binding Factory called by the Container.
 * @author Florent BENOIT
 */
public interface EZBBindingFactory {

    /**
     * Binds a reference with the given JNDI name and the given referenceable
     * object.
     * @param ref the EasyBeans reference object
     * @throws BindingException if the bind fails.
     */
    void bind(final EZBRef ref) throws BindingException;

    /**
     * Unbinds an object with the given JNDI name.
     * @param ref the EasyBeans reference object
     * @throws BindingException if the bind fails.
     */
    void unbind(final EZBRef ref) throws BindingException;
}
