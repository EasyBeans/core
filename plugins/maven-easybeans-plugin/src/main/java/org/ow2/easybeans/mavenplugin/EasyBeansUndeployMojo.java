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
 * $Id: EasyBeansUndeployMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentBehavior;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentCore;
import org.ow2.util.maven.plugin.deployment.api.IPluginServer;
import org.ow2.util.maven.plugin.deployment.behavior.SimpleUndeploymentBehavior;



/**
 * Undeploy project resources on a started EasyBeans instance.
 * @author Vincent Michaud
 * @goal undeploy
 * @phase post-integration-test
 */
public class EasyBeansUndeployMojo extends AbstractEasyBeansBoundMojo {

    /**
     * Port to contact to stop EasyBeans.
     * @parameter default-value="1099"
     */
    private int stopPort;

    /**
     * Hostname to contact to stop EasyBeans.
     * @parameter default-value="localhost"
     */
    private String hostname;

    /**
     * Create or get a launched server instance.
     * @param core The core of the mojo
     * @return Plugin server interface
     * @throws MojoExecutionException Execution error
     */
    public IPluginServer getLaunchedServer(final IDeploymentCore core) throws MojoExecutionException {
        return EasyBeansServerFactory.getRemoteLaunchedServer(this.hostname, this.stopPort);
    }

    /**
     * Define the behavior when a file must be deployed / undeployed.
     * @param core The core of the mojo
     * @return The deployment behavior
     * @throws MojoExecutionException Execution error
     */
    public IDeploymentBehavior defineDeploymentBehavior(final IDeploymentCore core) throws MojoExecutionException {
        return new SimpleUndeploymentBehavior(core.getServer());
    }
}