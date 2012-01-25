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
 * $Id: NamingEnumerationImpl.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */


package org.ow2.easybeans.naming.context;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.NoSuchElementException;

import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Implementation of the NamingEnumeration for list operations Each element is
 * of type NameClassPair.
 * @author Florent Benoit
 */
public class NamingEnumerationImpl implements NamingEnumeration<NameClassPair> {

    /**
     * list of names.
     */
    private Enumeration names;

    /**
     * List of bindings.
     */
    private Hashtable bindings;

    /**
     * Constructor. Called by list()
     * @param bindings list of bindings
     */
    NamingEnumerationImpl(final Hashtable bindings) {
        this.bindings = bindings;
        this.names = bindings.keys();
    }

    /**
     * Determines whether there are any more elements in the enumeration.
     * @return true if there is more in the enumeration ; false otherwise.
     * @throws NamingException If a naming exception is encountered while
     *         attempting to determine whether there is another element in the
     *         enumeration.
     */
    public boolean hasMore() throws NamingException {
        return names.hasMoreElements();
    }

    /**
     * Retrieves the next element in the enumeration.
     * @return The possibly null element in the enumeration. null is only valid
     *         for enumerations that can return null (e.g. Attribute.getAll()
     *         returns an enumeration of attribute values, and an attribute
     *         value can be null).
     * @throws NamingException If a naming exception is encountered while
     *         attempting to retrieve the next element. See NamingException and
     *         its subclasses for the possible naming exceptions.
     */
    public NameClassPair next() throws NamingException {
        String name = (String) names.nextElement();
        String className = bindings.get(name).getClass().getName();
        return new NameClassPair(name, className);
    }

    /**
     * Closes this enumeration.
     */
    public void close() {
    }

    /**
     * Returns the next element of this enumeration if this enumeration object
     * has at least one more element to provide.
     * @return the next element of this enumeration.
     */
    public NameClassPair nextElement() {
        try {
            return next();
        } catch (NamingException e) {
            throw new NoSuchElementException(e.toString());
        }
    }

    /**
     * Tests if this enumeration contains more elements.
     * @return <code>true</code> if and only if this enumeration object
     *         contains at least one more element to provide; <code>false</code>
     *         otherwise.
     */
    public boolean hasMoreElements() {
        try {
            return hasMore();
        } catch (NamingException e) {
            return false;
        }
    }

}
