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
 * $Id: ContextImpl.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.context;

import java.util.Hashtable;

import javax.naming.Binding;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.InvalidNameException;
import javax.naming.LinkRef;
import javax.naming.Name;
import javax.naming.NameAlreadyBoundException;
import javax.naming.NameClassPair;
import javax.naming.NameNotFoundException;
import javax.naming.NameParser;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.NotContextException;
import javax.naming.OperationNotSupportedException;
import javax.naming.RefAddr;
import javax.naming.Reference;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Implementation of Context interface.
 * @author Florent Benoit
 */
public class ContextImpl implements Context {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ContextImpl.class);

    /**
     * Environment.
     */
    private Hashtable<Object, Object> environment = null;

    /**
     * Bindings (Name <--> Object).
     */
    private Hashtable<String, Object> bindings = new Hashtable<String, Object>();

    /**
     * Parser.
     */
    private static NameParser myParser = new JavaNameParser();

    /**
     * Naming id.
     */
    private String compId;

    /**
     * Constructor.
     * @param id id of the context.
     * @param env initial environment.
     */
    @SuppressWarnings("unchecked")
    public ContextImpl(final String id, final Hashtable<Object, Object> env) {
        if (env != null) {
            // clone env to be able to change it.
            this.environment = (Hashtable<Object, Object>) (env.clone());
        } else {
            this.environment = new Hashtable<Object, Object>();
        }
        this.compId = id;
    }

    /**
     * Constructor.
     * @param id id of the context.
     */
    @SuppressWarnings("unchecked")
    public ContextImpl(final String id) {
        this(id, new Hashtable());
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to <tt>name</tt>
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final Name name) throws NamingException {
        // Just use the string version for now.
        return lookup(name.toString());
    }

    /**
     * Retrieves the named object.
     * @param name the name of the object to look up
     * @return the object bound to name
     * @throws NamingException if a naming exception is encountered
     */
    public Object lookup(final String name) throws NamingException {
        logger.debug("lookup {0}", name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            // Empty name means this context
            logger.debug("Empty name");
            return this;
        }

        if (n.size() > 1) {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // should throw exception if sub context not found!
            Context subctx = lookupCtx(n.get(0));
            return subctx.lookup(suffix);
        }
        // leaf in the env tree
        Object ret = this.bindings.get(name);
        if (ret == null) {
            logger.debug(" {0} not found.", name);
            throw new NameNotFoundException(name);
        }
        if (ret instanceof LinkRef) {
            // Handle special case of the LinkRef since I think
            // it's not handled by std NamingManager.getObjectInstance().
            // The name hidden in linkref is in the initial context.

            InitialContext ictx = new InitialContext();
            RefAddr ra = ((Reference) ret).get(0);
            try {
                ret = ictx.lookup((String) ra.getContent());
            } catch (Exception e) {
                NamingException ne = new NamingException(e.getMessage());
                ne.setRootCause(e);
                logger.error("unexpected exception {0}", e.getMessage(), e);
                throw ne;
            }
        } else if (ret instanceof Reference) {
            // Use NamingManager to build an object
            try {
                Object o = javax.naming.spi.NamingManager.getObjectInstance(ret, n, this, this.environment);
                ret = o;
            } catch (NamingException e) {
                throw e;
            } catch (Exception e) {
                NamingException ne = new NamingException(e.getMessage());
                ne.setRootCause(e);
                throw ne;
            }
            if (ret == null) {
                logger.error("Can not build an object with the reference {0}", name);
                throw new NamingException("Can not build an object with the reference '" + name + "'");
            }
        }
        return ret;

    }

    /**
     * Binds a name to an object. Delegate to the String version.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(final Name name, final Object obj) throws NamingException {
        // Just use the string version for now.
        bind(name.toString(), obj);
    }

    /**
     * Binds a name to an object.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public void bind(final String name, final Object obj) throws NamingException {
        logger.debug("bind {0}", name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.error("CompNamingContext bind empty name ?");

            throw new InvalidNameException("CompNamingContext cannot bind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            if (this.bindings.get(name) != null) {
                logger.error("CompNamingContext: trying to overbind");
                throw new NameAlreadyBoundException("CompNamingContext: Use rebind to bind over a name");
            }
            this.bindings.put(name, obj);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // must create the subcontext first if it does not exist yet.
            Context subctx;
            try {
                subctx = lookupCtx(n.get(0));
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(n.get(0));
            }
            subctx.bind(suffix, obj);
        }
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     */
    public void rebind(final Name name, final Object obj) throws NamingException {
        // Just use the string version for now.
        rebind(name.toString(), obj);
    }

    /**
     * Binds a name to an object, overwriting any existing binding.
     * @param name the name to bind; may not be empty
     * @param obj the object to bind; possibly null
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.InvalidNameException
     */
    public void rebind(final String name, final Object obj) throws NamingException {

        logger.debug("rebind {0}", name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.error("CompNamingContext rebind empty name ?");
            throw new InvalidNameException("CompNamingContext cannot rebind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            this.bindings.put(name, obj);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // must create the subcontext first if it does not exist yet.
            Context subctx;
            try {
                subctx = lookupCtx(n.get(0));
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(n.get(0));
            }
            subctx.rebind(suffix, obj);
        }
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     */
    public void unbind(final Name name) throws NamingException {
        // Just use the string version for now.
        unbind(name.toString());
    }

    /**
     * Unbinds the named object.
     * @param name the name to unbind; may not be empty
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameNotFoundException
     * @see javax.naming.InvalidNameException
     */
    public void unbind(final String name) throws NamingException {

        logger.debug("unbind {0}", name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.error("CompNamingContext rebind empty name ?");
            throw new InvalidNameException("CompNamingContext cannot unbind empty name");
        }

        if (n.size() == 1) {
            // leaf in the env tree
            if (this.bindings.get(name) == null) {
                logger.error("CompNamingContext nothing to unbind");
                throw new NameNotFoundException(name);
            }
            this.bindings.remove(name);
        } else {
            // sub context in the env tree
            String suffix = n.getSuffix(1).toString();
            // should throw exception if sub context not found!
            Context subctx = lookupCtx(n.get(0));
            subctx.unbind(suffix);
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
        // Just use the string version for now.
        rename(oldName.toString(), newName.toString());
    }

    /**
     * Binds a new name to the object bound to an old name, and unbinds the old
     * name.
     * @param oldName the name of the existing binding; may not be empty
     * @param newName the name of the new binding; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void rename(final String oldName, final String newName) throws NamingException {
        logger.debug("CompNamingContext rename {0} in {1}", oldName, newName);
        Object obj = lookup(oldName);
        rebind(newName, obj);
        unbind(oldName);
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
        // Just use the string version for now.
        return list(name.toString());
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
        logger.debug("list {0}", name);

        if (name.length() == 0) {
            // List this context
            return new NamingEnumerationImpl(this.bindings);
        }
        Object obj = lookup(name);
        if (obj instanceof Context) {
            return ((Context) obj).list("");
        }
        throw new NotContextException(name);
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
        // Just use the string version for now.
        return listBindings(name.toString());
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
        logger.debug("listBindings {0}", name);

        if (name.length() == 0) {
            // List this context
            return new BindingsImpl(this.bindings);
        }
        Object obj = lookup(name);
        if (obj instanceof Context) {
            return ((Context) obj).listBindings("");
        }
        logger.error("CompNamingContext: can only list a Context");
        throw new NotContextException(name);

    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final Name name) throws NamingException {
        // Just use the string version for now.
        destroySubcontext(name.toString());
    }

    /**
     * Destroys the named context and removes it from the namespace. Not
     * supported yet.
     * @param name the name of the context to be destroyed; may not be empty
     * @throws NamingException if a naming exception is encountered
     */
    public void destroySubcontext(final String name) throws NamingException {
        logger.error("CompNamingContext try to destroySubcontext {0}", name);
        throw new OperationNotSupportedException("CompNamingContext: destroySubcontext");
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
        // Just use the string version for now.
        return createSubcontext(name.toString());
    }

    /**
     * Creates and binds a new context.
     * @param name the name of the context to create; may not be empty
     * @return the newly created context
     * @throws NamingException if a naming exception is encountered
     * @see javax.naming.NameAlreadyBoundException
     */
    public Context createSubcontext(final String name) throws NamingException {
        logger.debug("createSubcontext {0}", name);

        Name n = new CompositeName(name);
        if (n.size() < 1) {
            logger.error("CompNamingContext createSubcontext with empty name ?");
            throw new InvalidNameException("CompNamingContext cannot create empty Subcontext");
        }

        Context ctx = null; // returned ctx
        if (n.size() == 1) {
            // leaf in the env tree: create ctx and bind it in parent.
            ctx = new ContextImpl(this.compId, this.environment);
            this.bindings.put(name, ctx);
        } else {
            // as for bind, we must create first all the subcontexts
            // if they don't exist yet.
            String suffix = n.getSuffix(1).toString();
            Context subctx;
            String newName = n.get(0);
            try {
                subctx = lookupCtx(newName);
            } catch (NameNotFoundException e) {
                subctx = createSubcontext(newName);
            }
            ctx = subctx.createSubcontext(suffix);
        }
        return ctx;
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
        // Just use the string version for now.
        return lookupLink(name.toString());
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
        logger.debug("lookupLink {0}", name);

        // To be done. For now: just return the object
        logger.error("CompNamingContext lookupLink not implemented yet!");

        return lookup(name);
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final Name name) throws NamingException {
        return myParser;
    }

    /**
     * Retrieves the parser associated with the named context.
     * @param name the name of the context from which to get the parser
     * @return a name parser that can parse compound names into their atomic
     *         components
     * @throws NamingException if a naming exception is encountered
     */
    public NameParser getNameParser(final String name) throws NamingException {
        return myParser;
    }

    /**
     * Composes the name of this context with a name relative to this context.
     * @param name a name relative to this context
     * @param prefix the name of this context relative to one of its ancestors
     * @return the composition of prefix and name
     * @throws NamingException if a naming exception is encountered
     */
    public Name composeName(final Name name, final Name prefix) throws NamingException {
        logger.error("CompNamingContext composeName not implemented yet!");

        throw new OperationNotSupportedException("CompNamingContext composeName");
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
        logger.error("CompNamingContext composeName {0} {1}", name, prefix);

        throw new OperationNotSupportedException("CompNamingContext composeName");
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
        return this.environment.put(propName, propVal);
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

        logger.debug("removeFromEnvironment {0}", propName);

        if (this.environment == null) {
            return null;
        }
        return this.environment.remove(propName);
    }

    /**
     * Retrieves the environment in effect for this context.
     * @return the environment of this context; never null
     * @throws NamingException if a naming exception is encountered
     */
    public Hashtable<?, ?> getEnvironment() throws NamingException {
        return this.environment;
    }

    /**
     * Closes this context.
     * @throws NamingException if a naming exception is encountered
     */
    public void close() throws NamingException {
        this.environment = null;
    }

    /**
     * Retrieves the full name of this context within its own namespace.
     * @return this context's name in its own namespace; never null
     */
    public String getNameInNamespace() {
        // this is used today for debug only.
        return this.compId;
    }

    // ------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------

    /**
     * Allow to add a binding in order to enhance the current context.
     * @param bindingName the name of the binding
     * @param context the context to add for this binding
     */
    public void addBinding(final String bindingName, final Context context) {
        if (this.bindings.get(bindingName) != null) {
            throw new IllegalStateException("Binding named '" + bindingName + "' already exists.");
        }
        this.bindings.put(bindingName, context);
    }

    /**
     * Find if this name is a sub context.
     * @param name the sub context name
     * @return the named Context
     * @throws NamingException When nam?ing fails
     * @see javax.naming.NameNotFoundException
     * @see javax.naming.NameAlreadyBoundException
     */
    private Context lookupCtx(final String name) throws NamingException {
        Object obj = this.bindings.get(name);
        if (obj == null) {
            throw new NameNotFoundException();
        }
        if (obj instanceof Context) {
            return (Context) obj;
        }
        throw new NameAlreadyBoundException(name);
    }


}
