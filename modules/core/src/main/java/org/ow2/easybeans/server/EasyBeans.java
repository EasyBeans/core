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
 * $Id: EasyBeans.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.server;

import java.io.File;
import java.net.URL;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Starts an embedded server in standalone mode.
 * @author Florent Benoit
 */
public final class EasyBeans {

    /**
     * Default XML file.
     */
    public static final String DEFAULT_XML_FILE = "org/ow2/easybeans/server/easybeans-default.xml";

    /**
     * User XML file.
     */
    public static final String USER_XML_FILE = "easybeans.xml";

    /**
     * Sleeping time.
     */
    private static final long SLEEP_TIME = 1000L;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EasyBeans.class);

    /**
     * Utility class. No public constructor.
     */
    private EasyBeans() {

    }

    /**
     * Main method called by default.
     * @param args the arguments for the main method
     * @throws Exception if failures
     */
    public static void main(final String[] args) throws Exception {

        Embedded embedded = new Embedded();

        // user configuration ?
        URL xmlConfigurationURL = Thread.currentThread().getContextClassLoader().getResource(USER_XML_FILE);

        if (xmlConfigurationURL == null) {
            xmlConfigurationURL = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_XML_FILE);
            logger.debug(
                    "No user-defined configuration file named ''{0}'' found in classpath. Using default settings from ''{1}''",
                    EasyBeans.USER_XML_FILE, xmlConfigurationURL);
        }

        // Add the configuration URL to the existing list
        embedded.getServerConfig().getConfigurationURLs().add(xmlConfigurationURL);

        // Add hook for shutdown
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(embedded));

        // Add deploy directory
        embedded.getServerConfig().addDeployDirectory(new File(Embedded.DEFAULT_DEPLOY_DIRECTORY));

        // Shutdown JVM when EasyBeans is stopped
        new ShutdownMonitorThread(embedded).start();

        // Start EasyBeans
        embedded.start();

    }

    /**
     * Class used to ensure that the JVM is killed after the Stop method of EasyBeans.
     * @author Florent BENOIT
     */
    static class ShutdownMonitorThread extends Thread {
        /**
         * Reference to the embedded object.
         */
        private Embedded embedded = null;

        /**
         * Build a new shutdown hook with the given embedded instance.
         * @param embedded the instance to stop
         */
        public ShutdownMonitorThread(final Embedded embedded) {
            this.embedded = embedded;
        }

        /**
         * Stop the embedded server.
         */
        @Override
        public void run() {
            while (!this.embedded.isStopped()) {
                try {
                    Thread.sleep(SLEEP_TIME);
                } catch (InterruptedException e) {
                    logger.debug("Error while sleeping", e);
                }
            }
            System.exit(0);
        }

    }


    /**
     * Hook that is called when process is going to shutdown.
     * @author Florent Benoit
     */
    static class ShutdownHook extends Thread {

        /**
         * Reference to the embedded object.
         */
        private Embedded embedded = null;

        /**
         * Build a new shutdown hook with the given embedded instance.
         * @param embedded the instance to stop
         */
        public ShutdownHook(final Embedded embedded) {
            this.embedded = embedded;
        }

        /**
         * Stop the embedded server.
         */
        @Override
        public void run() {
            // stop embedded if not yet stopped
            try {
                if (!this.embedded.isStopped()) {
                    this.embedded.stop();
                }
            } catch (EmbeddedException e) {
                System.err.println("Error while stopping embedded server");
                e.printStackTrace(System.err);
            }
        }
    }
}
