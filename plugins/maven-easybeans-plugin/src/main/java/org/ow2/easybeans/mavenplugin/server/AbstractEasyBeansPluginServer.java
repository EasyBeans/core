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
 * $Id: AbstractEasyBeansPluginServer.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.io.File;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EZBServerException;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.EARDeployable;
import org.ow2.util.ee.deploy.api.deployable.EJBDeployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.ee.deploy.api.deployer.IDeployerManager;
import org.ow2.util.ee.deploy.api.deployer.UnsupportedDeployerException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.maven.plugin.deployment.api.IPluginServer;



/**
 * Abstract implementation of IPluginServer interface. Try to get a local
 * EasyBeans server instance and provide operations on this if the operation is
 * a success.
 * @author Vincent Michaud
 */
public abstract class AbstractEasyBeansPluginServer implements IPluginServer {

    /**
     * Interface of EasyBeans server.
     */
    private EZBServer server = null;

    /**
     * Logger.
     */
    private static Log logger;

    /**
     * If a local EasyBeans server instance is found.
     */
    private boolean localServerFound = false;


    /**
     * Abstract constructor.
     */
    public AbstractEasyBeansPluginServer() {
        logger = LogFactory.getLog(AbstractEasyBeansPluginServer.class);
        this.server = EmbeddedManager.getEmbedded(Integer.valueOf(0));

        if (this.server != null) {
            logger.info("Reusing previous instance of the Embedded instance.");
            this.localServerFound = true;
        }
    }

    /**
     * Undeploy a specific EJB or EAR from the server.
     * @param file File to undeploy
     * @return The archive created in order to identify the resource, or
     *         null if error
     */
    protected IArchive removeArchiveIfDeployed(final File file) {
        IArchive archive = ArchiveManager.getInstance().getArchive(file);
        if (archive != null) {
            EZBContainer container = this.server.findContainer(archive);
            if (container != null) {
                this.server.removeContainer(container);
            }
        }
        return archive;
    }


    /**
     * Deploy an EJB or an EAR.
     * If deployable file is already deployed, redeploy the file.
     * @param filename A deployable
     */
    public void deployArchive(final String filename) {
        IDeployerManager deployer = this.server.getDeployerManager();
        if (deployer != null) {
            File file = new File(filename);
            try {
                IArchive archive = removeArchiveIfDeployed(file);

                if (archive != null) {
                    IDeployable deployable = DeployableHelper.getDeployable(archive);
                    if (deployable instanceof EJBDeployable || deployable instanceof EARDeployable) {
                        deployer.deploy(deployable);
                    } else {
                        logger.warn("Archive \"" + file.getName() + "\" not available.");
                    }
                }
            } catch (DeployerException ex) {
                logger.error("Archive \"" + file.getName() + "\" not deployed.", ex);
            } catch (UnsupportedDeployerException ex) {
                logger.error("Archive \"" + file.getName() + "\" not deployed.", ex);
            } catch (DeployableHelperException ex) {
                logger.error("Unable to create deployable with file \"" + file.getName() + "\".", ex);
            }
        }
    }

    /**
     * Get the version of EasyBeans server.
     * If the server is a local instance, the result is the same
     * as Version.getPluginVersion(). If the server is a remote
     * instance, the result is the version of the remote server.
     * @return A String
     */
    public abstract String getVersion();

    /**
     * Undeploy a deployed EJB or EAR.
     * @param filename A deployable deployed on EasyBeans
     */
    public void undeployArchive(final String filename) {
        if (this.server.getDeployerManager() != null) {
            File file = new File(filename);
            if (removeArchiveIfDeployed(file) == null) {
                logger.debug("Archive \"" + file.getName() + "\" not available.");
            }
        }
    }

    /**
     * Stop EasyBeans server.
     */
    public synchronized void stop() {
        try {
            this.server.stop();
        } catch (EZBServerException ex) {
            logger.error(ex);
        }
    }

    /**
     * Determine if the server is stopped or not.
     * @return True if server has been stopped.
     */
    public synchronized boolean isStopped() {
        return this.server.isStopped();
    }

    /**
     * Determine if server is a local found instance.
     * @return If server is a local found instance
     */
    public boolean localServerFound() {
        return this.localServerFound;
    }

    /**
     * Set an instance of EasyBeans server.
     * @param server EasyBeans server interface
     */
    public synchronized void setServer(final EZBServer server) {
        this.server = server;
    }

    /**
     * Get an instance of EasyBeans server.
     * @return EasyBeans server interface
     */
    public synchronized EZBServer getServer() {
        return this.server;
    }


    /**
     * Get EasyBeans implementation of log.
     * @return Log
     */
    public static Log getLog() {
        return logger;
    }

}
