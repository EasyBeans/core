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
 * $Id: AbstractEasyBeansUnboundMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import java.util.List;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.easybeans.mavenplugin.mapping.ServerConfigLocation;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentUnboundMojo;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentCore;
import org.ow2.util.maven.plugin.deployment.api.IPluginServer;
import org.ow2.util.maven.plugin.deployment.core.UnboundDeploymentCore;


/**
 * Class representing goal which must not be bound to the Maven project lifecycle.
 * This mojo provide a simple deployment of the current project resource.
 * @author Vincent Michaud
 */
public abstract class AbstractEasyBeansUnboundMojo extends AbstractEasyBeansMojo implements IDeploymentUnboundMojo {

    /****************************************************/
    /*                   Maven Parameters               */
    /****************************************************/

    /**
     * The interval in milliseconds to scan EJBs for change.
     * @parameter default-value="1000"
     */
    private long scanInterval;

    /**
     * If hot deployment is automatic or manual.
     * @parameter default-value="true"
     */
    private boolean autoDeployment;

    /**
     * Define if EAR are explored during the checking of deployable.
     * @parameter default-value="false"
     */
    private boolean exploreEAR;
    
    /**
     * Location of EasyBeans configuration XML files.
     * @parameter
     */
    private ServerConfigLocation serverConfig;

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
     * If this property is enabled, all supported persistence providers
     * are automatically added to the plugin classloader.
     * @parameter default-value="false"
     */
    private boolean supportAllPersistenceManager;


    /****************************************************/
    /*       IDeploymentUnboundMojo implementation      */
    /****************************************************/

    /**
     * Get the interval in milliseconds to scan EJBs for change.
     * @return The scan interval
     */
    public long getScanInterval() {
        return this.scanInterval;
    }

    /**
     * Determine If hot deployment is automatic or manual.
     * @return True if automatic deployment is enabled, false otherwise
     */
    public boolean isAutoDeploymentEnabled() {
        return this.autoDeployment;
    }

    /**
     * Determine if EAR are explored during the checking of deployable.
     * @return True if EAR are explored
     */
    public boolean isExploredEAR() {
        return this.exploreEAR;
    }

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
        return EasyBeansServerFactory.getLaunchedServer(configXmlFile, partialConfigXmlFiles, this.hostname,
                                                        this.stopPort, this.supportAllPersistenceManager,
                                                        core.getArtifactResolver());
    }

    /**
     * Define the core of the mojo.
     * @return The core of the mojo
     * @throws MojoExecutionException Execution error
     */
    public IDeploymentCore defineMojoCore() throws MojoExecutionException {
        return new UnboundDeploymentCore(this);
    }
}
