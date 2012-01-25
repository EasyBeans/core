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
 * $Id: JNDILookupHelper.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.injection;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.Reference;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Helper class for injecting a JNDI Name in the bean.
 * @author Florent Benoit
 */
public final class JNDILookupHelper {

    /**
     * Type of lookup available.
     */
    public enum JndiType { REGISTRY, JAVA_COMP, JAVA_COMP_ENV, JAVA}

    /**
     * Comp prefix.
     */
    private static final String JAVA_COMP = "java:comp/";

    /**
     * ENV prefix.
     */
    private static final String JAVA_COMP_ENV = JAVA_COMP + "env/";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JNDILookupHelper.class);



    /**
     * Utility class, no public constructor.
     */
    private JNDILookupHelper() {

    }

    /**
     * Gets a JNDI name object.
     * @param name the name of the object to lookup.
     * @return object found for the given JNDI name, else null.
     */
    public static Object getJndiName(final String name) {
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

        // Display some meaningful information if the Reference was not processed
        if (o instanceof Reference) {
            Reference ref = (Reference) o;
            logger.warn("Reference not processed: " + ref);
            logger.warn("LookupError({0}): The ObjectFactory({1}) was not found with TCCL {2}",
                        name,
                        ref.getFactoryClassName(),
                        Thread.currentThread().getContextClassLoader());
        }
        return o;
    }

    /**
     * Gets a JNDI name object in java:comp/env/.
     * @param name the name of the object to lookup.
     * @return object found for the given JNDI name, else null.
     */
    public static Object getEnvJndiName(final String name) {
        return getJndiName(JAVA_COMP_ENV + name);
    }

    /**
     * Gets a JNDI name object in java:comp/.
     * @param name the name of the object to lookup.
     * @return object found for the given JNDI name, else null.
     */
    public static Object getCompJndiName(final String name) {
        return getJndiName(JAVA_COMP + name);
    }


    /**
     * Gets a JNDI name object in java:.
     * @param name the name of the object to lookup.
     * @return object found for the given JNDI name, else null.
     */
    public static Object getJavaJndiName(final String name) {
        return getJndiName(name);
    }
}
