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
 * $Id: SmartContextFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.spi;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.ow2.easybeans.component.smartclient.client.AskingClassLoader;

/**
 * Initial Context factory used on the client side.<br>
 * It will ask the server for every class/resource not found in local.
 * @author Florent Benoit
 *
 */
public class SmartContextFactory implements InitialContextFactory {

    /**
     * Use the JDK logger (to avoid any dependency).
     */
    private static Logger logger = Logger.getLogger(SmartContextFactory.class.getName());

    /**
     * Default PROVIDER_URL.
     */
    private static final String DEFAULT_URL = "smart://localhost:2503";

    /**
     * Carol factory.
     */
    private static final String CAROL_FACTORY = "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory";

    /**
     * EasyBeans delegating factory. (by default it will be ourself)
     */
    public static final String EASYBEANS_DELEGATING_FACTORY = "easybeans.smart.delegate.factory";

    /**
     * EasyBeans JNDI factory. (by default it will be Carol)
     */
    public static final String EASYBEANS_SMART_JNDI_FACTORY = "easybeans.smart.jndi.factory";

    /**
     * Use of a classloader for associating the smart info ?.
     */
    private static boolean useSmartInfoByClassLoader = false;

    /**
     * EasyBeans factory.
     */
    public static final String EASYBEANS_FACTORY = "easybeans.rpc.rmi.factory";

    /**
     * Map between a Smart Provider_URL and associated infos.
     */
    private static Map<String, SmartContextFactoryInfo> infos = new HashMap<String, SmartContextFactoryInfo>();

    /**
     * Map between a Smart Provider_URL and associated infos.
     */
    private static Map<ClassLoader, SmartContextFactoryInfo> infosClassLoader
        = new WeakHashMap<ClassLoader, SmartContextFactoryInfo>();

    /**
     * Provider URL to use by default.
     */
    private static String defaultProviderURL = null;


    /**
     * Default constructor.<br>
     * Sets the Portable RemoteObject Wrapper class too.
     */
    public SmartContextFactory() {
        // sets our class (or the delegating factory)
        System.setProperty(EASYBEANS_FACTORY, System.getProperty(EASYBEANS_DELEGATING_FACTORY, this.getClass().getName()));
        System.setProperty("javax.rmi.CORBA.PortableRemoteObjectClass", ProDelegate.class.getName());
    }

    /**
      * Creates an Initial Context for beginning name resolution.
      * Special requirements of this context are supplied
      * @param environment the given environment.
      * @return the context.
      * @throws NamingException if no context can be built.
    */
    @SuppressWarnings("unchecked")
    public Context getInitialContext(final Hashtable environment)
            throws NamingException {


        SmartContextFactoryInfo info = checkInit(environment);

        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(info.getClassLoader());

        // Previous JNDI Factory
        String oldInitialContextFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);
        try {

            // JNDI Factory set in the environment ?
            String jndiFactory = (String) environment.get(EASYBEANS_SMART_JNDI_FACTORY);

            // JNDI factory set as system property ?
            if (jndiFactory == null) {
                jndiFactory = System.getProperty(EASYBEANS_SMART_JNDI_FACTORY);
            }

            // Else, use default factory
            if (jndiFactory == null) {
                jndiFactory = CAROL_FACTORY;
            }

            // set the right factory
            environment.put(Context.INITIAL_CONTEXT_FACTORY, jndiFactory);
            environment.put(Context.PROVIDER_URL, info.getProviderURL());

            // return wrapped context
            return new SmartContext(new InitialContext(environment), info.getClassLoader());
        } finally {
            Thread.currentThread().setContextClassLoader(old);
            // Restore InitialContext factory if defined
            if (oldInitialContextFactory != null) {
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY, oldInitialContextFactory);
            }
        }

    }

    /**
     * Ensure that all is setup.
     * It has to work for each PROVIDER_URL
     * @param environment the InitialContext env.
     * @return data about the smart factory.
     * @throws NamingException if there is an exception
     */
    protected static SmartContextFactoryInfo checkInit(final Hashtable<?, ?> environment) throws NamingException {

        // Gets the provider url
        String currentProviderURL = (String) environment.get(Context.PROVIDER_URL);

        if (currentProviderURL == null) {
            if (defaultProviderURL == null) {
                logger.log(Level.WARNING, "No PROVIDER_URL setting found, use the default URL '" + DEFAULT_URL + "'.");
                currentProviderURL = DEFAULT_URL;
            } else {
                currentProviderURL = defaultProviderURL;
            }
        }

        // Data existing for the given provider URL ?
        SmartContextFactoryInfo info = null;

        // Use classloader as parameter ?
        if (useSmartInfoByClassLoader) {
            info = infosClassLoader.get(Thread.currentThread().getContextClassLoader());
        } else {
            info = infos.get(currentProviderURL);
        }

        // Not found, need to initialize
        if (info == null) {
            try {

                info = new SmartContextFactoryInfo();

                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Initializing Smart Factory with remote URL '" + currentProviderURL + "'.");
                }

                // extract host
                String host = getHostOfUrl(currentProviderURL);

                // extract port
                int portNumber = getPortOfUrl(currentProviderURL);

                // build the classloader to use.
                AskingClassLoader classLoader = createClassLoader(host, portNumber);
                info.setClassLoader(classLoader);

                // Set the classloader for the ProDelegate class
                ProDelegate.setClassLoader(classLoader);

                String providerURL = classLoader.getProviderURL();
                if (logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE, "Got remote PROVIDER_URL '" + providerURL + "'.");
                }
                info.setProviderURL(providerURL);

                // Add it to the list
                infos.put(currentProviderURL, info);
                // And add also the real provider URL
                infos.put(providerURL, info);

                if (useSmartInfoByClassLoader) {
                    infosClassLoader.put(Thread.currentThread().getContextClassLoader(), info);
                }

            } catch (Exception e) {
                NamingException ne = new NamingException("Cannot get a remote ClassLoader");
                ne.initCause(e);
                throw ne;
            }
        }

        return info;
    }

    /**
     * Create a classloader for the given host and port number within a privileged block.
     * @param host the given host
     * @param portNumber the port number
     * @return a new instance of the classloader
     */
    private static AskingClassLoader createClassLoader(final String host, final int portNumber) {
        return AccessController.doPrivileged(
                new PrivilegedAction<AskingClassLoader>() {
                    public AskingClassLoader run() {
                        return new AskingClassLoader(host, portNumber);
                    }
                }
        );
    }

    /**
     * Parses the given url, and returns the port number. 0 is given in error
     * case)
     * @param url given url on which extract port number
     * @return port number of the url
     * @throws NamingException if URL is invalid
     */
    public static int getPortOfUrl(final String url) throws NamingException {
        int portNumber = 0;
        try {
            StringTokenizer st = new StringTokenizer(url, ":");
            st.nextToken();
            st.nextToken();
            if (st.hasMoreTokens()) {
                StringTokenizer lastst = new StringTokenizer(st.nextToken(), "/");
                String pts = lastst.nextToken().trim();
                int i = pts.indexOf(',');
                if (i > 0) {
                    pts = pts.substring(0, i);
                }
                portNumber = new Integer(pts).intValue();
            }
            return portNumber;
        } catch (Exception e) {
            // don't rethrow original exception. only URL name is important
            throw new NamingException("Invalid URL '" + url + "'. It should be on the format <protocol>://<hostname>:<port>");
        }
    }

    /**
     * Parses the given url, and returns the hostname.
     * @param url given url on which extract hostname
     * @return hostname of the url
     * @throws NamingException if URL is invalid
     */
    private static String getHostOfUrl(final String url) throws NamingException {
        String host = null;
        // this would be simpler with a regexp :)
        try {
            // url is of the form protocol://<hostname>:<port>
            String[] tmpSplitStr = url.split(":");

            // array should be of length = 3
            // get 2nd element (should be //<hostname>)
            String tmpHost = tmpSplitStr[1];

            // remove //
            String[] tmpSplitHost = tmpHost.split("/");

            // Get last element of the array to get hostname
            host = tmpSplitHost[tmpSplitHost.length - 1];
        } catch (Exception e) {
            // don't rethrow original exception. only URL name is important
            throw new NamingException("Invalid URL '" + url + "'. It should be on the format <protocol>://<hostname>:<port>");
        }
        return host;
    }

    /**
     * Allow to change the provider URL used by default.
     * @param providerURL the given provider URL
     */
    public static void setDefaultProviderUrl(final String providerURL) {
        defaultProviderURL = providerURL;
    }

    /**
     * Use the classloader as a key for AskingClassloader ?.
     * @param useOfClassLoader the given value.
     */
    public static void setUseClassLoader(final boolean useOfClassLoader) {
        useSmartInfoByClassLoader = useOfClassLoader;
    }
}
