/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: EasyBeansServerFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.easybeans.mavenplugin.persistence.PersistenceManager;
import org.ow2.easybeans.mavenplugin.server.EasyBeansPluginServer;
import org.ow2.easybeans.mavenplugin.server.IPersistenceListener;
import org.ow2.easybeans.mavenplugin.server.RemoteEasyBeansPluginServer;
import org.ow2.util.maven.plugin.deployment.api.IPluginServer;
import org.ow2.util.maven.plugin.deployment.maven.MavenArtifactResolver;
import org.ow2.util.maven.plugin.deployment.maven.MavenLog;


/**
 * Provide an access to local or remote instance of EasyBeans.
 * @author Vincent Michaud
 */
public final class EasyBeansServerFactory {

    /**
     * Constructor.
     */
    private EasyBeansServerFactory() {
    }


    /**
     * Create and launch a local EasyBeans server instance with specified XML
     * core and configuration files.
     * @param configXmlFile Path of EasyBeans XML configuration file
     * @param partialConfigXmlFiles Path list of partial EasyBeans XML
     *                              configuration file.
     * @param supportAllPersistenceManager Determine if all supported
     *        persistence providers dependencies are automatically added
     *        to the plugin classloader at the server launch.
     * @param resolver An artifact resolver
     * @return Instance of local and launched EasyBeans server
     * @throws MojoExecutionException Execution error
     */
    public static IPluginServer getLocalLaunchedServer(final String configXmlFile,
                                                       final List<String> partialConfigXmlFiles,
                                                       final boolean supportAllPersistenceManager,
                                                       final MavenArtifactResolver resolver)
                                                       throws MojoExecutionException {

        if (configXmlFile != null && !configXmlFile.equals("")) {
            File file = new File(configXmlFile);
            if (!file.exists() || !file.isFile()) {
                throw new MojoExecutionException("EasyBeans can not be started. "
                          + "Your EasyBeans configuration file is not found at [" + file.getAbsolutePath() + "].");
            }
        }

        EasyBeansPluginServer server = new EasyBeansPluginServer(configXmlFile, partialConfigXmlFiles);
        IPersistenceListener persistenceListener = null;
        PersistenceManager manager = PersistenceManager.getInstance();
        
        if (supportAllPersistenceManager) {
            manager.loadAllDependencies(resolver);
        } else {
            persistenceListener = manager.getPersistenceListener(resolver);
        }

        server.start(persistenceListener);

        if (!server.isStarted()) {
            throw new MojoExecutionException("EasyBeans can not be started. "
                      + "Maybe another instance of server is launched.");
        }

        return server;
    }


    /**
     * Get an instance of launched remote EasyBeans server.
     * @param hostname Domaine name used to contact EasyBeans.
     * @param stopPort Port number used to contact EasyBeans.
     * @return An instance of launched remote EasyBeans server, or null is
     *         nothing is found
     */
    public static IPluginServer getRemoteLaunchedServer(final String hostname, final int stopPort)
            throws MojoExecutionException {

        RemoteEasyBeansPluginServer server = null;
        try {
            server = new RemoteEasyBeansPluginServer(hostname, stopPort);
        } catch (IOException ex) {
            throw new MojoExecutionException("No EasyBeans server found at \"" + hostname + ":" + stopPort + "\"");
        }
        return server;
    }



    /**
     * Get an instance of launched EasyBeans server.
     * @param configXmlFile Path of EasyBeans XML configuration file
     * @param partialConfigXmlFiles Path list of partial EasyBeans XML
     *                              configuration file.
     * @param hostname Domaine name used to contact EasyBeans
     * @param stopPort Port number used to contact EasyBeans
     * @return An instance of launched EasyBeans server
     * @throws MojoExecutionException Execution error
     */
    public static IPluginServer getLaunchedServer(final String configXmlFile,
                                                  final List<String> partialConfigXmlFiles,
                                                  final String hostname, final int stopPort,
                                                  final boolean supportAllPersistenceManager,
                                                  final MavenArtifactResolver resolver)
                                                  throws MojoExecutionException {
        IPluginServer server = null;
        try {
            server = getRemoteLaunchedServer(hostname, stopPort);
            MavenLog.getLog().info("EasyBeans server found at : " + hostname + ":" + stopPort);
        } catch (MojoExecutionException ex) {
            server = getLocalLaunchedServer(configXmlFile, partialConfigXmlFiles, 
                                            supportAllPersistenceManager, resolver);
        }
        return server;
    }

}