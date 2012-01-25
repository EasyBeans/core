/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: SmartBootstrap.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.spi;


import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.easybeans.component.smartclient.client.AskingClassLoader;



/**
 * Defines a bootstrap that will provide class loading by using smart classloader.
 * @author Florent BENOIT
 */
public final class SmartBootstrap {

    /**
     * Default port number.
     */
    private static final int DEFAULT_PORT_NUMBER = 2503;

    /**
     * Arguments used by this bootstrap.
     */
    private String[] args = null;

    /**
     * Extra Arguments that are not for this bootstrap but the launched client.
     */
    private List<String> clientArgs = null;

    /**
     * Port number.
     */
    private int portNumber = DEFAULT_PORT_NUMBER;

    /**
     * Host of the smart server.
     */
    private String host = "localhost";

    /**
     * Classpath for the application client.
     */
    private String classpath = null;


    /**
     * Use the JDK logger (to avoid any dependency).
     */
    private static Logger logger = Logger.getLogger(SmartBootstrap.class.getName());


    /**
     * Utility class, no public constructor.
     * @param args the given args to use.
     */
    private SmartBootstrap(final String[] args) {
        this.args = args;

        this.clientArgs = new LinkedList<String>();
    }

    /**
     * Main method that will start the class.
     * @param args the given args
     */
    public static void main(final String[] args) {

        // Build a new Bootstrap...
        SmartBootstrap bootStrap = new SmartBootstrap(args);

        // ... and start it
        bootStrap.start();

    }

    /**
     * Start the bootstrap.
     */
    public void start() {
        // Analyze given args
        analyzeArgs();

        // Main class should be the first arg
        String mainClass = null;
        if (this.clientArgs.size() > 0) {
            // get value
            mainClass = this.clientArgs.get(0);
            // And remove it from the remaining args
            this.clientArgs.remove(0);
        }

        if (mainClass == null || mainClass.length() == 0) {
            usage();
            throw new IllegalArgumentException("No class to launch !");
        }

        // Build classloader
        ClassLoader classLoader = createClassLoader(this.host, this.portNumber, getUserClasspathUrls());

        // Sets it as the new one
        Thread.currentThread().setContextClassLoader(classLoader);


        logger.log(Level.INFO, "Starting Main-Class : '" + mainClass + "' by using Smart Server '" + this.host + ":"
                + this.portNumber + "' with args '" + this.clientArgs + "'.");

        // Load Main Class
        Class<?> mainClazz;
        try {
            mainClazz = classLoader.loadClass(mainClass);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the main class '" + mainClass + "'.", e);
        }

        // Get main method
        Method mainMethod;
        try {
            mainMethod = mainClazz.getMethod("main", String[].class);
        } catch (SecurityException e) {
            throw new IllegalStateException("No such method main found in the main class '" + mainClass + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("No such method main found in the main class '" + mainClass + "'.", e);
        }

        // Invoke it
        try {
            mainMethod.invoke(null, new Object[] {this.clientArgs.toArray(new String[this.clientArgs.size()])});
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Error while invoking client class " + mainClass + "'.", e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Error while invoking client class " + mainClass + "'.", e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException("Error while invoking client class " + mainClass + "'.", e);
        }


    }

    /**
     * Create a classloader for the given host and port number within a privileged block.
     * @param host the given host
     * @param portNumber the port number
     * @param userClasspathURLs an array of some URLs
     * @return a new instance of the classloader
     */
    private static AskingClassLoader createClassLoader(final String host, final int portNumber, final URL[] userClasspathURLs) {
        return AccessController.doPrivileged(
                new PrivilegedAction<AskingClassLoader>() {
                    public AskingClassLoader run() {
                        return new AskingClassLoader(host, portNumber, userClasspathURLs);
                    }
                }
        );
    }

    /**
     * Analyze arguments and extract parameters for the client container.
     * @throws IllegalArgumentException if there is an error when analyzing arguments
     */
    private void analyzeArgs() throws IllegalArgumentException {
        for (int argn = 0; argn < this.args.length; argn++) {
            String arg = this.args[argn];

            try {
                if (arg.equals("-port")) {
                    this.portNumber = Integer.valueOf(this.args[++argn]).intValue();
                    continue;
                }

                if (arg.equals("-host")) {
                    this.host = this.args[++argn];
                    continue;
                }

                if (arg.equals("-cp")) {
                    this.classpath = this.args[++argn];
                    continue;
                }

                if (arg.equals("--help") || arg.equals("-help") || arg.equals("-h") || arg.equals("-?")) {
                    usage();
                    return;
                }

                // Add argument to the application arguments
                this.clientArgs.add(arg);
            } catch (ArrayIndexOutOfBoundsException e) {
                // The next argument is not in the array
                throw new IllegalArgumentException("A required parameter was missing after the argument '" + arg + "'.", e);
            }
        }

    }

    /**
     * Gets the URL of user classpath (can be empty).
     * @return URL of user classpath (-cp arg)
     */
    private URL[] getUserClasspathUrls() {
        if (this.classpath == null) {
            return new URL[0];
        }
        String sep = File.pathSeparator;
        List<URL> clUser = new ArrayList<URL>();
        StringTokenizer tokenizer = new StringTokenizer(this.classpath, sep);
        while (tokenizer.hasMoreTokens()) {
            File file = new File(tokenizer.nextToken());
            try {
                clUser.add(file.toURI().toURL());
            } catch (MalformedURLException uue) {
                logger.log(Level.WARNING, "Cannot transform to URL the file : '" + file + "'", uue);
            }
        }
        return clUser.toArray(new URL[0]);
    }

    /**
     * Print the usage of this client.
     */
    private void usage() {
        System.out.println("Usage of this SmartBootStrap :");
        System.out.println("-------------------------------------------------------------------");
        System.out.println("java [options] easybeans-component-smartclient-xxx.jar "
                + "<My.Client.ClassName> [client-options]");
        System.out.println("-------------------------------------------------------------------");
        System.out.println(" -cp   : Specify the classpath to use for the jar client. ie: -cp <a.jar" + File.pathSeparator
                + "b.jar>");
        System.out.println(" -port : Specify the port number to use when connecting on Smart Server.");
        System.out.println(" -host : Specify the hostname of the Smart Server.");
        System.out.println("-------------------------------------------------------------------");
        System.out.println(" --help| -help | -h | -?  : Display this help.");
        System.out.println("-------------------------------------------------------------------");
    }

}
