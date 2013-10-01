/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Copyright (C) 2013 Peergreen S.A.S.
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
 * $Id: NamingManager.java 5736 2011-02-22 08:27:11Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.omg.CORBA.ORB;
import org.ow2.easybeans.naming.context.ContextImpl;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Manages the java: context used by components.
 * @author Florent Benoit
 */
public final class NamingManager {

    /**
     * Sub context name.
     */
    private static final String COMP_SUBCONTEXT = "comp";

    /**
     * Sub context name.
     */
    private static final String ENV_SUBCONTEXT = "env";

    /**
     * app sub context name.
     */
    private static final String APP_SUBCONTEXT = "app";

    /**
     * module sub context name.
     */
    private static final String MODULE_SUBCONTEXT = "module";


    /**
     * Sub context name.
     */
    private static final String GLOBAL_SUBCONTEXT = "global";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(NamingManager.class);

    /**
     * Associate a context to a thread.
     */
    private static ThreadLocal<Context> threadContext = new ThreadLocal<Context>();

    /**
     * Initial Context.
     */
    private InitialContext ictx = null;


    /**
     * Static context used by client container. One context for all the JVM.
     */
    private static Context clientCtx = null;

    /**
     * Singleton management: - the constructor is private. - use static method
     * getInstance to retrieve/create the instance.
     */
    private static NamingManager unique = null;

    /**
     * UserTransaction object, to be shared by all components.
     */
    private UserTransaction userTransaction = null;

    /**
     * Create the naming manager.
     * @throws NamingException if no initial context is built
     */
    private NamingManager() throws NamingException {
            this.ictx = new InitialContext();
    }

    /**
     * Return the unique instance of a NamingManager.
     * @return NamingManager the unique instance.
     * @throws NamingException if it failed.
     */
    public static synchronized NamingManager getInstance() throws NamingException {
        if (unique == null) {
            unique = new NamingManager();
        }
        return unique;
    }

    /**
     * Get the initialContext used in this jonas server.
     * @return InitialContext the initial context.
     */
    public InitialContext getInitialContext() {
        return this.ictx;
    }

    /**
     * Create Context for application and component environments. (formally
     * known as createComponentContext)
     * @param namespace namespace to used for the Context
     * @param envContext the env context
     * @param moduleContext the module context
     * @param appContext the application context
     * @return a java: context with comp/ subcontext
     * @throws NamingException if the creation of the java: context failed.
     */
    public Context createEnvironmentContext(final String namespace, final Context envContext, final Context moduleContext,
            final Context appContext) throws NamingException {

        // Create a new environment
        ContextImpl ctx = new ContextImpl(namespace);

        // Create subContext
        ContextImpl compCtx = (ContextImpl) ctx.createSubcontext(COMP_SUBCONTEXT);

        // Create comp/env context or reuse
        if (envContext != null) {
            compCtx.addBinding(ENV_SUBCONTEXT, envContext);
        } else {
            compCtx.createSubcontext(ENV_SUBCONTEXT);
        }

        // Add global
        ctx.addBinding(GLOBAL_SUBCONTEXT, this.ictx);

        // For EJB3, module subcontext is not comp context
        if (moduleContext != null) {
            ctx.addBinding(MODULE_SUBCONTEXT, moduleContext);
        } else {
            ctx.addBinding(MODULE_SUBCONTEXT, new ContextImpl("moduleContext"));
        }


        // App context (if not defined, reuse module context)
        Context appCtx = null;
        if (appContext == null) {
            appCtx = new ContextImpl("appContext");
        } else {
            appCtx = appContext;
        }

        ctx.addBinding(APP_SUBCONTEXT, appCtx);

        // Bind java:comp/UserTransaction
        if (this.userTransaction == null) {
            try {
                this.userTransaction = (UserTransaction) this.ictx.lookup("javax.transaction.UserTransaction");
            } catch (NamingException e) {
                logger.error("Cannot lookup UserTransaction.", e);
            }
        }
        if (this.userTransaction != null) {
            compCtx.rebind("UserTransaction", this.userTransaction);
        }

        // bind ORB
        try {
            ORB orb = ORBInitHelper.getORB();
            if (orb != null) {
                compCtx.rebind("ORB", orb);
            }
        } catch (NamingException e) {
            logger.error("Cannot bind ORB", e);
        }

        return ctx;
    }

    /**
     * Get the Context associated with the current thread or to a class loader.
     * @return Context the component context.
     * @throws NamingException When operation is not allowed
     */
    public Context getComponentContext() throws NamingException {

        Context ctx = null;

        // Check if there is a context to the local thread
        // For ejbs
        ctx = threadContext.get();
        if (ctx != null) {
            return ctx;
        }

        // Check static context. use in client. One context per JVM.
        if (clientCtx != null) {
            ctx = clientCtx;
            if (ctx != null) {
                return ctx;
            }
        }

        // No context found. This is outside of a j2ee component or server
        // component.
        throw new NamingException("No java: context for components running outside EasyBeans.");
    }

    /**
     * Associate this CompNamingContext with the current thread.
     * This method should be called before the call to the business method.
     * After, resetComponentContext should be called to reset the context.
     * @param ctx the context to associate to the current thread.
     * @return Context the context of the thread
     */
    public Context setComponentContext(final Context ctx) {
        Context ret = threadContext.get();
        threadContext.set(ctx);
        return ret;
    }

    /**
     * Set back the context with the given value.
     * Don't return the previous context, use setComponentContext() method for this.
     * @param ctx the context to associate to the current thread.
     */
    public void resetComponentContext(final Context ctx) {
        threadContext.set(ctx);
    }


    /**
     * Set the context used by client container (per JVM instead of per thread).
     * @param ctx the context to set
     */
    public static void setClientContainerComponentContext(final Context ctx) {
        clientCtx = ctx;
    }


    public void setUserTransaction(UserTransaction userTransaction) {
        this.userTransaction = userTransaction;
    }


}
