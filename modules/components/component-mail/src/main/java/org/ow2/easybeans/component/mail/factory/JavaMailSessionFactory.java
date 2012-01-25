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
 * $Id: JavaMailSessionFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import java.util.Hashtable;

import javax.mail.Session;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * JNDI factory for session factory.
 * @author Florent BENOIT
 */
public class JavaMailSessionFactory extends AbsJavaMailFactory implements ObjectFactory {

    /**
     * Type of object created by this factory.
     */
    public static final String FACTORY_TYPE = "javax.mail.Session";

    /**
     * Creates an object using the location or reference information specified.
     * @param obj The possibly null object containing location or reference
     *        information that can be used in creating an object.
     * @param name The name of this object relative to <code>nameCtx</code>,
     *        or null if no name is specified.
     * @param nameCtx The context relative to which the <code>name</code>
     *        parameter is specified, or null if <code>name</code> is relative
     *        to the default initial context.
     * @param environment The possibly null environment that is used in creating
     *        the object.
     * @return The object created; null if an object cannot be created.
     * @exception Exception if this object factory encountered an exception
     */
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment)
            throws Exception {

        // Check if the reference classname is valid
        Reference reference = (Reference) obj;
        if (!FACTORY_TYPE.equals(reference.getClassName())) {
            return null;
        }

        // Create and return a new Session object
        Session session = Session.getInstance(getSessionProperties(reference), getAuthenticator(reference));

        return session;
    }

}
