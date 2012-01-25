/**
 * EasyBeans
 * Copyright (C) 2007-2008 Bull S.A.S.
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
 * $Id: JNDIBindingFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.binding;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.api.binding.BindingException;
import org.ow2.easybeans.api.binding.EZBBindingFactory;
import org.ow2.easybeans.api.binding.EZBRef;

/**
 * Binding of the reference in the JNDI/Registry.
 * @author Florent BENOIT
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public class JNDIBindingFactory implements EZBBindingFactory {

    /**
     * Binds a reference with the given JNDI name and the given referenceable
     * object.
     * @param ref the EasyBeans reference object
     * @throws BindingException if the bind fails.
     */
    public void bind(final EZBRef ref) throws BindingException {
        try {
            new InitialContext().rebind(ref.getJNDIName(), ref);
        } catch (NamingException e) {
            throw new BindingException(
                    "Cannot bind the object with JNDI name '" + ref.getJNDIName() + "' and reference '" + ref + "'.", e);
        }
    }

    /**
     * Unbinds an object with the given JNDI name.
     * @param ref the EasyBeans reference object.
     * @throws BindingException if any unbind fails.
     */
    public void unbind(final EZBRef ref) throws BindingException {
        Object toUnbind;
        try {
            toUnbind = new InitialContext().lookupLink(ref.getJNDIName());
        } catch (NamingException e) {
            toUnbind = null;
        }
        if (toUnbind != null) {
            try {
                new InitialContext().unbind(ref.getJNDIName());
            } catch (NamingException e) {
                throw new BindingException("Cannot unbind the object with JNDI name '" + ref.getJNDIName() + "'.", e);
            }
        }
    }

}
