/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: RemoteDeployer.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.ow2.easybeans.api.EZBServer;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.ee.deploy.api.deployer.IDeployerManager;
import org.ow2.util.ee.deploy.api.deployer.UnsupportedDeployerException;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Deployer allowing deploy/undeploy functions on this container.
 * @author Florent Benoit
 */
public final class RemoteDeployer implements IRemoteDeployer {

    /**
     * Folder to create in tmp folder.
     */
    private static final String DEFAULT_FOLDER = "EasyBeans-" + RemoteDeployer.class.getSimpleName();

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(RemoteDeployer.class);

    /**
     * Link to the deployer manager.
     */
    private IDeployerManager deployerManager = null;

    /**
     * Build a Deployer for the given Embedded instance.
     * @param embedded the Server instance.
     * @throws DeployerException if the local deployer can't be accessed.
     */
    public RemoteDeployer(final EZBServer embedded) throws DeployerException {
        this.deployerManager = embedded.getDeployerManager();
    }


    /**
     * Dump the given bytes to a local file and then return the path to this
     * file.
     * @param fileName the name of the file to deploy
     * @param fileContent the content of the given file
     * @return the path of the deployed file
     */
    public String dumpFile(final String fileName, final byte[] fileContent) {
        logger.info("Dump file to the local filesystem with the name = ''{0}''.", fileName);

        // Dump the file on a temporary file
        File rootFolder = new File(System.getProperty("java.io.tmpdir") + File.separator + DEFAULT_FOLDER);
        rootFolder.mkdirs();

        // Create file in this folder
        File file =  new File(rootFolder, fileName);

        // Dump the content on this file
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new IllegalStateException("Cannot build an outputstream on file '" + file + "'.", e);
        }

        // Write the array of bytes
        try {
            out.write(fileContent);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write byte in outputstream", e);
        }

        // Close resource
        try {
            out.close();
        } catch (IOException e) {
            throw new IllegalStateException("Cannot close outpustream", e);
        }

        // return the local dump of the file
        return file.getPath();
    }

    /**
     * Dump the given bytes to a local file and then deploy this file to a local deployer.
     * @param fileName the name of the file to deploy
     * @param fileContent the content of the given file
     */
    public void deployFile(final String fileName, final byte[] fileContent) {

        // Deploy this local file
        deploy(dumpFile(fileName, fileContent));
    }


    /**
     * Deploy a file to a local deployer.
     * @param fileName the name of the file to deploy
     */
    public void deploy(final String fileName) {

        logger.info("Deploying ''{0}''", fileName);

        IDeployable<?> deployable = getDeployable(fileName);

        try {
            this.deployerManager.deploy(deployable);
        } catch (DeployerException e) {
            logger.error("Cannot deploy the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot deploy the deployable '" + deployable + "' : " + e.getMessage());
        } catch (UnsupportedDeployerException e) {
            logger.error("Cannot deploy the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot deploy the deployable '" + deployable + "' : " + e.getMessage());
        }
    }

    /**
     * Gets a deployable for a given file.
     * @param fileName the name of the file
     * @return a deployable for the given filename
     */
    protected IDeployable<?> getDeployable(final String fileName) {
        // Get File
        File file = new File(fileName);

        // check file
        if (!file.exists()) {
            throw new RuntimeException("The file '" + fileName + "' is not present on the filesystem.");
        }

        // Else, get the deployable
        IArchive archive = ArchiveManager.getInstance().getArchive(file);
        if (archive == null) {
            logger.error("No archive found for the invalid file ''{0}''", file);
            throw new RuntimeException("No archive found for the invalid file '" + file + "'.");
        }
        IDeployable<?> deployable;
        try {
            deployable = DeployableHelper.getDeployable(archive);
        } catch (DeployableHelperException e) {
            logger.error("Cannot get a deployable for the archive ''{0}''", archive, e);
            throw new RuntimeException("Cannot get a deployable for the archive '" + archive + "' : " + e.getMessage());
        }

        return deployable;
    }


    /**
     * Undeploy a file by using a local deployer.
     * @param fileName the name of the file to undeploy
     */
    public void undeploy(final String fileName) {
        logger.info("Undeploying ''{0}''", fileName);

        IDeployable<?> deployable = getDeployable(fileName);

        try {
            this.deployerManager.undeploy(deployable);
        } catch (DeployerException e) {
            logger.error("Cannot undeploy the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot undeploy the deployable '" + deployable + "' : " + e.getMessage());
        } catch (UnsupportedDeployerException e) {
            logger.error("Cannot undeploy the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot undeploy the deployable '" + deployable + "' : " + e.getMessage());
        }
    }


    /**
     * Checks if the given file is deployed or not.
     * @param fileName test if a given file is already deployed.
     * @return true if the given deployable is deployed.
     */
    public boolean isDeployed(final String fileName) {
        IDeployable<?> deployable = getDeployable(fileName);
        try {
            return this.deployerManager.isDeployed(deployable);
        } catch (DeployerException e) {
            logger.error("Cannot check isDeployed for  the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot check isDeployed for the deployable '" + deployable + "' : " + e.getMessage());
        } catch (UnsupportedDeployerException e) {
            logger.error("Cannot check isDeployed for  the deployable ''{0}''", deployable, e);
            throw new RuntimeException("Cannot check isDeployed for the deployable '" + deployable + "' : " + e.getMessage());
        }
    }

}
