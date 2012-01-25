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
 * $Id: JavaURLContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.naming.pkg.java;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.ow2.easybeans.naming.NamingManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Context implementation for the "java:" namespace. <br>
 * @author Florent Benoit
 */
public class JavaURLContext implements Context {

    /**
     * java: prefix.
     */
    private static final String JAVA_PREFIX = "java:";

    /**
     * Reference on the naming manager.
     */
    private static NamingManager namingManager = null;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JavaURLContext.class);

    /**
     * Constructor.
     * @throws NamingException if naming manager is not found.
     */
    public JavaURLContext() throws NamingException {
        if (namingManager == null) {
            namingManager = NamingManager.getInstance();
        }
    }

    /**
     * Get name without the url prefix.
     * @param name the absolute name.
     * @return the relative name (without prefix).
     * @throws NamingException if the naming failed.
     */
    private String getRelativeName(final String name) throws NamingException {
        String newName = name;
        // We suppose that all names must be prefixed as this
        if (!name.startsWith(JAVA_PREFIX)) {
            logger.error("relative name! {0}" + name);
            throw new NameNotFoundException("Invalid name:" + name);
        }
        if (name.endsWith("/")) {
            newName = name.substring(JAVA_PREFIX.length() + 1);
        } else {
            newName = name.substring(JAVA_PREFIX.length());
        }

        return newName;
    }

    /**
     * Get name without the url prefix.
     * @param name the absolute name.
     * @return the relative name (without prefix).
     * @throws NamingException if the naming failed.
     */
    private Name getRelativeName(final Name name) throws NamingException {
        if (name.get(0).equals(JAVA_PREFIX)) {
            return (name.getSuffix(1));
        }
        throw new NameNotFoundException("Invalid name:" + name);
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final Name name) throws NamingException {
        return findContext().lookup(getRelativeName(name));
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final String name) throws NamingException {
        return findContext().lookup(getRelativeName(name));
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(final Name name, final Object obj) throws NamingException {
        findContext().bind(getRelativeName(name), obj);
    }

    /**
     * Binds a name to an object. All intermediate contexts and the target
     * context (that named by all but terminal atomic component of the name)
     * must already exist.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void bind(final String name, final Object obj) throws NamingException {
        findContext().bind(getRelativeName(name), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding. All
     * intermediate contexts and the target context (that named by all but
     * terminal atomic component of the name) must already exist. If the object
     * is a DirContext, any existing attributes associated with the name are
     * replaced with those of the object. Otherwise, any existing attributes
     * associated with the name remain unchanged.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(final Name name, final Object obj) throws NamingException {
        findContext().rebind(getRelativeName(name), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding. See
     * {@link #rebind(Name, Object)}for details.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(final String name, final Object obj) throws NamingException {
        findContext().rebind(getRelativeName(name), obj);
    }

    /**
     * Unbinds the named object. Removes the terminal atomic name in name from
     * the target context--that named by all but the terminal atomic part of
     * name. This method is idempotent. It succeeds even if the terminal atomic
     * name is not bound in the target context, but throws NameNotFoundException
     * if any of the intermediate contexts do not exist. Any attributes
     * associated with the name are removed. Intermediate contexts are not
     * changed.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see #unbind(String)
     */
    public void unbind(final Name name) throws NamingException {
        findContext().unbind(getRelativeName(name));
    }

    /**
     * Unbinds the named object. See {@link #unbind(Name)}for details.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void unbind(final String name) throws NamingException {
        findContext().unbind(getRelativeName(name));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. This operation is not supported (read only env.)
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(final Name oldName, final Name newName) throws NamingException {
        findContext().rename(getRelativeName(oldName), getRelativeName(newName));
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name. Not supported.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(final String oldName, final String newName) throws NamingException {
        findContext().rename(getRelativeName(oldName), getRelativeName(newName));
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. The contents of any subcontexts are not
     * included. If a binding is added to or removed from this context, its
     * effect on an enumeration previously returned is undefined.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<NameClassPair> list(final Name name) throws NamingException {
        return findContext().list(getRelativeName(name));
    }

    /**
     * Enumerates the names bound in the named context, along with the class
     * names of objects bound to them. See {@link #list(Name)}for details.
     * @param name the name of the context to list
     * @return an enumeration of the names and class names of the bindings in
     *         this context. Each element of the enumeration is of type
     *         NameClassPair.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<NameClassPair> list(final String name) throws NamingException {
        return findContext().list(getRelativeName(name));
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
        return findContext().listBindings(getRelativeName(name));
    }

    /**
     * Enumerates the names bound in the named context, along with the objects
     * bound to them. See {@link #listBindings(Name)}for details.
     * @param name the name of the context to list
     * @return an enumeration of the bindings in this context. Each element of
     *         the enumeration is of type Binding.
     * @throws NamingException if a naming exception is encountered
     */
    public NamingEnumeration<Binding> listBindings(final String name) throws NamingException {
        return findContext().listBindings(getRelativeName(name));
    }

    /**
     * Destroys the named context and removes it from the namespace. Any
     * attributes associated with the name are also removed. Intermediate
     * contexts are not destroyed. This method is idempotent. It succeeds even
     * if the terminal atomic name is not bound in the target context, but
     * throws NameNotFoundException if any of the intermediate contexts do not
     * exist. In a federated naming system, a context from one naming system may
     * be bound to a name in another. One can subsequently look up and perform
     * operations on the foreign context using a composite name. However, an
     * attempt destroy the context using this composite name will fail with
     * NotContextException, because the foreign context is not a "subcontext" of
     * the context in which it is bound. Instead, use unbind() to remove the
     * binding of the foreign context. Destroying the foreign context requires
     * that the destroySubcontext() be performed on a context from the foreign
     * context's "native" naming system.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final Name name) throws NamingException {
        findContext().destroySubcontext(getRelativeName(name));
    }

    /**
     * Destroys the named context and removes it from the namespace. See
     * {@link #destroySubcontext(Name)}for details.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final String name) throws NamingException {
        findContext().destroySubcontext(getRelativeName(name));
    }

    /**
     * Creates and binds a new context. Creates a new context with the given
     * name and binds it in the target context (that named by all but terminal
     * atomic component of the name). All intermediate contexts and the target
     * context must already exist.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(final Name name) throws NamingException {
        return findContext().createSubcontext(getRelativeName(name));
    }

    /**
     * Creates and binds a new context. See {@link #createSubcontext(Name)}for
     * details.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     */
    public Context createSubcontext(final String name) throws NamingException {
        return findContext().createSubcontext(getRelativeName(name));
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
        return findContext().lookupLink(getRelativeName(name));
    }

    /**
     * Retrieves the named object, following links except for the terminal
     * atomic component of the name. See {@link #lookupLink(Name)}for details.
     * @param name the name of the object to look up
     * @return the object bound to name, not following the terminal link (if
     *         any)
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookupLink(final String name) throws NamingException {
        return findContext().lookupLink(getRelativeName(name));
    }

    /**
     * Retrieves the parser associated with the named context. In a federation
     * of namespaces, different naming systems will parse names differently.
     * This method allows an application to get a parser for parsing names into
     * their atomic components using the naming convention of a particular
     * naming system. Within any single naming system, NameParser objects
     * returned by this method must be equal (using the equals() test).
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final Name name) throws NamingException {
        return findContext().getNameParser(getRelativeName(name));
    }

    /**
     * Retrieves the parser associated with the named context. See
     * {@link #getNameParser(Name)}for details.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final String name) throws NamingException {
        return findContext().getNameParser(getRelativeName(name));
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        Name newPrefix = (Name) name.clone();
        return newPrefix.addAll(name);
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public String composeName(final String name, final String prefix) throws NamingException {
        return prefix + "/" + name;
    }

    /**
     * Adds a new environment property to the environment of this context. If
     * the property already exists, its value is overwritten. See class
     * description for more details on environment properties.
     * @param propName the name of the environment property to add; may not be
     *        null
     * @param propVal the value of the property to add; may not be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment before
     * @throws NamingException if a naming exception is encountered
     */
    public Object addToEnvironment(final String propName, final Object propVal) throws NamingException {
        return findContext().addToEnvironment(propName, propVal);
    }

    /**
     * Removes an environment property from the environment of this context. See
     * class description for more details on environment properties.
     * @param propName the name of the environment property to remove; may not
     *        be null
     * @return the previous value of the property, or null if the property was
     *         not in the environment
     * @throws NamingException if a naming exception is encountered
     */
    public Object removeFromEnvironment(final String propName) throws NamingException {
        return findContext().removeFromEnvironment(propName);
    }

    /**
     * Retrieves the environment in effect for this context. See class
     * description for more details on environment properties. The caller should
     * not make any changes to the object returned: their effect on the context
     * is undefined. The environment of this context may be changed using
     * addToEnvironment() and removeFromEnvironment().
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return findContext().getEnvironment();
    }

    /**
     * Closes this context. This method releases this context's resources
     * immediately, instead of waiting for them to be released automatically by
     * the garbage collector. This method is idempotent: invoking it on a
     * context that has already been closed has no effect. Invoking any other
     * method on a closed context is not allowed, and results in undefined
     * behaviour.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        findContext().close();
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     * @throws NamingException if a naming exception is encountered
     */
    public String getNameInNamespace() throws NamingException {
        return JAVA_PREFIX;
    }

    /**
     * @return the Context associated with the current thread.
     * @throws NamingException if no context is found.
     */
    public Context findContext() throws NamingException {
      return namingManager.getComponentContext();
    }


}
