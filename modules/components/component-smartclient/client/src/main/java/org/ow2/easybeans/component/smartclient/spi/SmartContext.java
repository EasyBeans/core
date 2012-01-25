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
 * $Id: SmartContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.spi;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

/**
 * Context that use a given classloader before calling every methods.
 * @author Florent Benoit
 */
public class SmartContext implements Context {

    /**
     * Context that is wrapped.
     */
    private Context wrapped;

    /**
     * Classloader to use for all methods.
     */
    private ClassLoader classLoader;

    /**
     * Creates a context with the given wrapped context and the given
     * classloader.
     * @param wrapped the context to wrap
     * @param classLoader the classloader to use.
     */
    public SmartContext(final Context wrapped, final ClassLoader classLoader) {
        this.wrapped = wrapped;
        this.classLoader = classLoader;
    }

    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten.
     * @param propName the name of the environment property to add; may not be
     *        null
     * @param propVal the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.addToEnvironment(propName, propVal);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Binds a name to an object. Delegate to the String version.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(final Name name, final Object obj) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.bind(name, obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(final String name, final Object obj) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.bind(name, obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Closes this context.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.close();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.composeName(name, prefix);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Composes the name of this context with a name relative to this context:
     * Not supported.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(final String name, final String prefix) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.composeName(name, prefix);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Creates and binds a new context. Creates a new context with the given
     * name and binds it in the target context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public Context createSubcontext(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.createSubcontext(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public Context createSubcontext(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.createSubcontext(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.destroySubcontext(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.destroySubcontext(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the environment in effect for this context.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.getEnvironment();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     * @throws NamingException if it fails.
     */
    public String getNameInNamespace() throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.getNameInNamespace();
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.getNameParser(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.getNameParser(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.list(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.list(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them. The contents of any subcontexts are not included. If a
     * binding is added to or removed from this context, its effect on an
     * enumeration previously returned is undefined.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<Binding> listBindings(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.listBindings(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.listBindings(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.lookup(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.lookup(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. If the object bound to name is not a link,
     * returns the object itself.
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link (if
     *         any).
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.lookupLink(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. If the object bound to name is not a link,
     * returns the object itself.
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link (if
     *         any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.lookupLink(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(final Name name, final Object obj) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.rebind(name, obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.InvalidNameException
     */
    public void rebind(final String name, final Object obj) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.rebind(name, obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Removes an environment property from the environment of this context.
     * @param propName the name of the environment property to remove; may not
     *        be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(final String propName) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.removeFromEnvironment(propName);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(final Name oldName, final Name newName) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.rename(oldName, newName);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(final String oldName, final String newName) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.rename(oldName, newName);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     */
    public void unbind(final Name name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.unbind(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     * @see javax.naming.InvalidNameException
     */
    public void unbind(final String name) throws NamingException {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.unbind(name);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

}
