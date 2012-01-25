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
 * $Id: EasyBeansStartMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.easybeans.mavenplugin.mapping.ServerConfigLocation;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentBehavior;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentCore;
import org.ow2.util.maven.plugin.deployment.api.IPluginServer;
import org.ow2.util.maven.plugin.deployment.behavior.SimpleDeploymentBehavior;


/**
 * Class representing goal start.
 * This mojo provide a simple deployment of the current project resource.
 * @author Vincent Michaud
 * @goal start
 * @phase pre-integration-test
 */
public class EasyBeansStartMojo extends AbstractEasyBeansBoundMojo {


    /****************************************************/
    /*                   Maven Parameters               */
    /****************************************************/

    /**
     * Location of EasyBeans configuration XML files.
     * @parameter
     */
    private ServerConfigLocation serverConfig;


    /****************************************************/
    /*                 Members functions                */
    /****************************************************/


    /**
     * Create server instance with specified XML configuration file.
     * @param core The core of the mojo
     * @return Plugin server interface
     * @throws MojoExecutionException If configuration file is not found
     */
    public IPluginServer getLaunchedServer(final IDeploymentCore core) throws MojoExecutionException {
        String configXmlFile = null;
        List<String> partialConfigXmlFiles = null;

        if (this.serverConfig != null) {
            configXmlFile = this.serverConfig.getConfigFileLocation();
            partialConfigXmlFiles = this.serverConfig.getPartialConfigFileLocations();
        }
        return EasyBeansServerFactory.getLocalLaunchedServer(configXmlFile, partialConfigXmlFiles, 
                                                             false, core.getArtifactResolver());
    }

    /**
     * Define the behavior when a file must be deployed / undeployed.
     * @param core The core of the mojo
     * @return The deployment behavior
     * @throws MojoExecutionException Execution error
     */
    public IDeploymentBehavior defineDeploymentBehavior(final IDeploymentCore core) throws MojoExecutionException {
        return new SimpleDeploymentBehavior(core.getServer());
    }
}