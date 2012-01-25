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
 * $Id: JavaMailSessionRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import javax.naming.NamingException;
import javax.naming.Reference;

/**
 * Defines a referenceable object for Session Mail object.
 * @author Florent BENOIT
 */
public class JavaMailSessionRef extends AbsJavaMailRef {

    /**
     * Build a new javax.mail.Session reference.
     */
    public JavaMailSessionRef() {
        super();
    }

    /**
     * Return the type of the factory.
     * @return the type of the mail factory
     */
    @Override
    public String getType() {
        return JavaMailSessionFactory.FACTORY_TYPE;
    }

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    @Override
    public Reference getReference() throws NamingException {

        // Build the reference for the JavaMailSession factory
        Reference reference = new Reference(getType(), JavaMailSessionFactory.class.getName(), null);

        // Update the reference
        updateRefAddr(reference);

        return reference;
    }

}
