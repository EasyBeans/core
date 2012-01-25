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
 * $Id: URLFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.url;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Allow to lookup java.net.URL object.
 * @author Florent BENOIT
 */
public class URLFactory implements ObjectFactory {

    /**
     * The Java type for which this factory knows how to create objects.
     */
    protected static final String FACTORY_TYPE = "java.net.URL";

    /**
     * Creates a java.net.URL object using the location or reference information
     * specified.
     * @param obj the possibly null object containing location or reference
     *        information that can be used in creating an object.
     * @param name the name of this object relative to nameCtx, or null if no
     *        name is specified.
     * @param nameCtx the context relative to which the name parameter is
     *        specified, or null if name is relative to the default initial
     *        context.
     * @param environment the possibly null environment that is used in creating
     *        the object.
     * @return a newly created java.net.URL object with the specific
     *         configuration; null if an object cannot be created.
     * @throws NamingException if this object factory encountered an exception while
     *         attempting to create an object, and no other object factories are
     *         to be tried.
     */
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment)
            throws NamingException {

        // Get the reference
        Reference ref = (Reference) obj;

        // Get the class name
        String clname = ref.getClassName();

        // Check the class name
        if (!ref.getClassName().equals(FACTORY_TYPE)) {
            throw new NamingException("Cannot create object : required type is '" + FACTORY_TYPE + "', but found type is '"
                    + clname + "'.");
        }

        URL url = null;
        String urlString = (String) ref.get("url").getContent();

        if (urlString != null) {
            try {
                url = new URL(urlString);
            } catch (MalformedURLException e) {
                NamingException ne = new NamingException("Cannot build an URL with the given string '" + urlString + "'");
                ne.initCause(e);
                throw ne;
            }
        } else {
            throw new NamingException("Can not build an object as no URL was given.");
        }

        return url;
    }

}
