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
 * $Id: ENCManager.java 6131 2012-01-18 08:47:31Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.interceptors;

import javax.naming.Context;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.naming.NamingInterceptor;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Detects the server type and return the correct interceptor to use depending
 * of the application server. It could be EasyBeans, JOnAS or Tomcat.
 * @author Florent Benoit
 */
public final class ENCManager {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ENCManager.class);

    /**
     * Class used for the interceptor.
     */
    private static Class<? extends NamingInterceptor> encInterceptor = null;



    /**
     * No public constructor (utility class).
     */
    private ENCManager() {

    }

    /**
     * Sets the ENC interceptor to use for the naming.
     * @param userEncInterceptor the interceptor class.
     */
    public static void setInterceptorClass(final Class<? extends NamingInterceptor> userEncInterceptor) {
        if (encInterceptor != null) {
            throw new IllegalStateException("The interceptor class has already been set with '" + encInterceptor
                    + "', cannot update it with '" + userEncInterceptor + "'.");
        }
        encInterceptor = userEncInterceptor;
    }

    /**
     * @return the class to use depending of the application server.
     */
    public static Class<? extends NamingInterceptor> getInterceptorClass() {

        // Not found, load default EZB ENC.
        if (encInterceptor == null) {
            if (encInterceptor == null) {
                encInterceptor = EZBENCInterceptor.class;
                logger.debug("Using EasyBeans ENC for the naming.");
            }
        }
        return encInterceptor;
    }

    /**
     * Init the context for the given factory.
     * @param factory the given factory
     * @param context the context associated to the container URL.
     */
    public static void initContext(final Factory factory, final Context context) {
        try {
            encInterceptor.newInstance().initContext(factory.getId(), context);
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot init the context for the factory '" + factory.getClassName()
                    + "' of container '" + factory.getContainer().getName() + "'.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot init the context for the factory '" + factory.getClassName()
                    + "' of container '" + factory.getContainer().getName() + "'.", e);
        }
    }

    /**
     * Remove the context associated to a factory.
     * @param factory the given factory
     */
    public static void removeContext(final Factory factory) {
        try {
            encInterceptor.newInstance().removeContext(factory.getId());
        } catch (InstantiationException e) {
            throw new IllegalStateException("Cannot remove the context for the factory '" + factory.getClassName()
                    + "' of container '" + factory.getContainer().getName() + "'.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Cannot remove the context for the factory '" + factory.getClassName()
                    + "' of container '" + factory.getContainer().getName() + "'.", e);
        }
    }

}
