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
 * $Id: JNDIBinderHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.injection;

import java.net.URL;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

import org.ow2.easybeans.naming.url.URLFactory;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Helper class for injecting an object in JNDI (ENC env).
 * @author Florent Benoit
 */
public final class JNDIBinderHelper {

    /**
     * Type of lookup available.
     */
    public enum JndiType {JAVA_COMP_ENV}

    /**
     * Comp prefix.
     */
    private static final String JAVA_COMP = "java:comp/";

    /**
     * ENV prefix.
     */
    private static final String JAVA_COMP_ENV = JAVA_COMP + "env";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JNDIBinderHelper.class);



    /**
     * Utility class, no public constructor.
     */
    private JNDIBinderHelper() {

    }

    /**
     * Gets the context for a given name (java: , java:comp, etc).
     * @param name the name of the context to lookup
     * @return context found or null.
     */
    public static Context getContext(final String name) {
        InitialContext ictx;
        try {
            ictx = new InitialContext();
        } catch (NamingException e) {
            logger.error("Cannot instantiate an initial context", e);
            return null;
        }

        Object o = null;
        try {
            o = ictx.lookup(name);
        } catch (NamingException e) {
            logger.error("Cannot find the JNDI name {0}", name, e);
        }
        if (o == null) {
            logger.error("No object was found for JNDI name {0}", name);
        }
        Context ctx = null;
        if (o instanceof Context) {
            ctx = (Context) o;
        } else {
            logger.error("Object not instance of context. Object = {0}", o);
        }
        return ctx;
    }

    /**
     * Bind a JNDI name object in java:comp/env/.
     * @param encName the name of the object to bind in enc
     * @param jndiName the jndi name to link.
     */
    public static void bindLinkRefEnvJndiName(final String encName, final String jndiName) {
        try {
            getContext(JAVA_COMP_ENV).rebind(encName, new LinkRef(jndiName));
        } catch (NamingException e) {
            logger.error("Cannot do a LinkRef between jndiName {0} with ENC name {1}", jndiName, encName, e);
        }
    }

    /**
     * Bind a JNDI name object in java:comp/env/.
     * @param name the name of the object to bind.
     * @param object the value of the object.
     */
    public static void bindEnvJndiName(final String name, final Object object) {
        // Avoid to bind a null object
        if (object == null) {
            logger.error("Cannot bind a null object with name {0}", name);
            return;
        }

        try {
            getContext(JAVA_COMP_ENV).rebind(name, object);
        } catch (NamingException e) {
            logger.error("Cannot bind object {0} with name {1}", object, name, e);
        }
    }

    /**
     * Bind an URL object in java:comp/env.
     * @param encName the name of the object to bind in enc
     * @param url the URL to register in the context
     */
    public static void bindLinkRefEnvURL(final String encName, final String url) {
        // Specify the factory to use with the right URL
        Reference ref = new Reference(URL.class.getName(), URLFactory.class.getName(), null);
        StringRefAddr refAddr = new StringRefAddr("url", url);
        ref.add(refAddr);
        try {
            getContext(JAVA_COMP_ENV).rebind(encName, ref);
        } catch (NamingException e) {
            logger.error("Cannot bind an URL with name {0} with ENC name {1}", url, encName, e);
        }
    }



}
