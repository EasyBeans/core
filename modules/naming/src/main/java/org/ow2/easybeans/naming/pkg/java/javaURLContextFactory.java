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
 * $Id: javaURLContextFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.pkg.java;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;


/**
 * Factory used when resolving lookup on java: namespace.
 * It has to be present in java.naming.factory.url.pkgs property.
 * @author Florent Benoit
 */
public class javaURLContextFactory implements ObjectFactory {

    /**
     * @return an instance of javaURLContext for a java URL. If url is null, the
     *         result is a context for resolving java URLs. If url is a URL, the
     *         result is a context named by the URL.
     * @param url String with a "java:" prefix or null.
     * @param name Name of context, relative to ctx, or null.
     * @param ctx Context relative to which 'name' is named.
     * @param env Environment to use when creating the context *
     * @throws Exception if this object factory encountered an exception while
     *         attempting to create an object, and no other object factories are
     *         to be tried.
     */
    public Object getObjectInstance(final Object url, final Name name, final Context ctx, final Hashtable env) throws Exception {
        return new JavaURLContext();
    }
}
