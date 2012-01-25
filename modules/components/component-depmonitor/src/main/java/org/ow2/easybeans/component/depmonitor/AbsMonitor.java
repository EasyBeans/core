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
 * $Id: AbsMonitor.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.depmonitor;

import static org.ow2.util.url.URLUtils.urlToFile;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.server.EmbeddedException;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.IDeployerManager;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Abstract class that is providing common stuff for monitors.
 * Implementation of monitor may extend this class.
 * @author Florent Benoit
 */
public abstract class AbsMonitor implements EZBMonitor, Runnable {

    /**
     * Sleep time for the thread of the cleaner (5s).
     */
    private static final long SLEEP_TIME = 5000L;

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(AbsMonitor.class);

    /**
     * Waiting time between each scan.
     */
    private long waitTime = SLEEP_TIME;

    /**
     * Map between a File (monitored) and the last updated file.
     */
    private Map<File, Long> modifiedFiles = null;

    /**
     * List of deployed files (by this monitor).
     */
    private Map<File, IDeployable<?>> deployed = null;

    /**
     * List of File that have a failed deployment (by this monitor).
     */
    private List<File> failed = null;

    /**
     * Initializing period ?.
     */
    private boolean bootInProgress = false;

    /**
     * Stop order received ?
     */
    private boolean stopped = false;

    /**
     * Deployer Manager.
     */
    private IDeployerManager deployerManager;

    /**
     * Directories to scan.
     */
    private List<String> scanDirectories = null;

    /**
     * Directory to scan.
     */
    private String directory = null;


    /**
     * Embedded server which is monitored.
     */
    private EZBServer embedded = null;

    /**
     * Builds a new monitor by initializing lists.
     * @throws EZBMonitorException if there is an exception for this monitor.
     */
    public AbsMonitor() throws EZBMonitorException {
        this.modifiedFiles = new HashMap<File, Long>();
        this.deployed = new ConcurrentHashMap<File, IDeployable<?>>();
        this.failed = new ArrayList<File>();


        //TODO: Change these steps when EasyBeans will use all the deployer stuff

        // Get the embedded
        this.embedded = EmbeddedManager.getEmbedded(Integer.valueOf(0));
        this.deployerManager = this.embedded.getDeployerManager();
    }

    /**
     * Start this monitor.
     * @throws EZBMonitorException if it can't be started
     */
    public void start() throws EZBMonitorException {
        // Start the boot process.
        this.bootInProgress = true;

        // Initialize containers
        try {
            detectNewArchives();
        } catch (EZBMonitorException e) {
            this.logger.error("Cannot scan for new archives", e);
        }

        // No more in the boot process.
        this.bootInProgress = false;

        // Period is defined ? launch a thread
        if (this.waitTime > 0) {
            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.setName(this.getClass().getName());
            thread.start();
        }
    }

    /**
     * Start the thread of this class It will clean all the work entries.
     */
    public void run() {

        for (;;) {
            // Stop the thread
            if (this.stopped) {
                return;
            }

            // Check if existing EJB3 container have not been modified ?
            for (EZBContainer container : getEmbedded().getContainers().values()) {
                // Look only single EJB-JAR
                if (container.isAvailable() && container.getApplicationName() == null) {
                    checkContainer(container);
                }
            }

            // Check new archives/containers to start
            try {
                detectNewArchives();
            } catch (Exception e) { // Catch all exception (including runtime)
                this.logger.error("Problem when trying to find and deploy new archives", e);
            }

            // Undeploy/ReDeploy archives for deployed modules
            try {
                checkModifiedDeployables();
            } catch (Exception e) { // Catch all exception (including runtime)
                this.logger.error("Problem when checking current deployables", e);
            }

            // Thread wait
            try {
                Thread.sleep(this.waitTime);
            } catch (InterruptedException e) {
                throw new RuntimeException("Thread fail to sleep");
            }
        }
    }

    /**
     * Filter to skip system file.
     */
    private static final FilenameFilter ARCHIVE_NAME_FILTER = new FilenameFilter() {

        public boolean accept(final File dir, final String name) {
            if (name.startsWith(".")) {
                return false;
            }
            return true;
        }

    };

    /**
     * Scan all files present in the deploy directory and deploy them. (if not
     * deployed).
     * @throws EZBMonitorException if there is a problem during the scan
     */
    private void detectNewArchives() throws EZBMonitorException {

        // Get the list of deploy directories
        this.scanDirectories = Arrays.asList(this.directory);

        for (String deployDirectoryString : this.scanDirectories) {

            File deployDirectory = new File(deployDirectoryString);


            try {
                deployDirectory = deployDirectory.getCanonicalFile();
            } catch (IOException e) {
                throw new EZBMonitorException("Cannot get file on '" + deployDirectory + "'", e);
            }

            // get files
            File[] files = deployDirectory.listFiles(ARCHIVE_NAME_FILTER);

            // next directory if there are no files to scan.
            if (files == null) {
                continue;
            }

            // analyze each file to detect new modules that are not yet
            // deployed.
            for (File f : files) {
                // already deployed ?
                if (this.deployed.containsKey(f)) {
                    // yes, then check other files
                    continue;
                }

                // This module has failed previously ?
                if (this.failed.contains(f)) {
                    // If the module hasn't been updated, no need to deploy it
                    // again as it will fails again
                    if (!hasBeenUpdated(f)) {
                        continue;
                    }
                    // Cleanup the previous failure and try again the deployment
                    this.failed.remove(f);
                }

                // Else, get the deployable
                IArchive archive = ArchiveManager.getInstance().getArchive(f);
                if (archive == null) {
                    this.logger.warn("Ignoring invalid file ''{0}''", f);
                    continue;
                }
                IDeployable<?> deployable;
                try {
                    deployable = DeployableHelper.getDeployable(archive);
                } catch (DeployableHelperException e) {
                    throw new EZBMonitorException("Cannot get a deployable for the archive '" + archive + "'", e);
                }
                if (!this.bootInProgress) {
                    // wait that files are fully copied before deploying the
                    // files
                    try {
                        Thread.sleep(SLEEP_TIME);
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Thread fail to sleep");
                    }
                }
                this.logger.debug("Detect a new Deployable ''{0}'' and deploying it.", deployable);

                // Now, deploy the file
                try {
                    this.deployerManager.deploy(deployable);
                } catch (Exception e) {
                    // Deployment of this deployable has failed
                    this.failed.add(f);
                    throw new EZBMonitorException("Cannot deploy the deployable '" + deployable + "'", e);
                }

                // deployed is ok
                this.deployed.put(f, deployable);
            }
        }
    }

    /**
     * Check if the given file has been updated since the last check.
     * @param file the file to test
     * @return true if the archive has been updated
     */
    protected boolean hasBeenUpdated(final File file) {

        // get lastmodified for this URL
        long previousLastModified = 0;
        Long l = this.modifiedFiles.get(file);
        if (l != null) {
            previousLastModified = l.longValue();
        }

        long updatedModified = getLastModified(file);

        // first check. nothing to do
        if (previousLastModified == 0) {
            // Store initial time
            this.modifiedFiles.put(file, Long.valueOf(updatedModified));
            return false;
        }
        // URL has been updated since the last time
        if (updatedModified > previousLastModified) {
            this.modifiedFiles.put(file, Long.valueOf(updatedModified));
            return true;
        }

        return false;
    }

    /**
     * Check if the current deployables that are deployed have been updated. If
     * it is the case, undeploy them and then deploy it again (except for EJB3
     * Deployable where there is a stop/start).
     * @throws EmbeddedException if the redeployment fails
     */
    protected void checkModifiedDeployables() throws EmbeddedException {
        // Get list of files that are deployed
        Set<File> files = this.deployed.keySet();

        // Nothing to do if no modules are deployed.
        if (files == null) {
            return;
        }

        // For each deployed module that is not an EJB3, check if the module has
        // been updated
        for (File f : files) {
            IDeployable<?> deployable = this.deployed.get(f);

            // Not yet deployed ?
            if (deployable == null) {
                continue;
            }

            // EJB3 are managed in a different way
            if (EJB3Deployable.class.isAssignableFrom(deployable.getClass())) {
                continue;
            }

            // File has been removed
            if (!f.exists()) {
                // undeploy
                this.logger.info("Deployable ''{0}'' has been removed on the filesystem, undeploy it", deployable);
                try {
                    this.deployerManager.undeploy(deployable);
                    // Perform a garbage collector to avoid file lock during redeployment
                    System.gc();
                } catch (Exception e) {
                    this.logger.error("Undeploy of the deployable '" + deployable + "' has failed", e);
                    this.failed.add(f);
                } finally {
                    // even in error case, the file should have been removed
                    this.deployed.remove(f);
                }
                continue;
            }

            // Update has been detected, need to undeploy and then to deploy
            // again
            if (hasBeenUpdated(f)) {
                this.logger.info("Deployable ''{0}'' has been updated, reloading it", deployable);
                try {
                    this.deployerManager.undeploy(deployable);
                    this.deployed.remove(f);
                    // Perform a garbage collector to avoid file lock during redeployment
                    System.gc();
                } catch (Exception e) {
                    this.logger.error("Undeploy of the deployable '" + deployable + "' has failed", e);
                    // Deployment has failed, it is now undeployed
                    this.deployed.remove(f);
                    this.failed.add(f);
                }

                // Get a new deployable
                IArchive archive = ArchiveManager.getInstance().getArchive(f);
                if (archive == null) {
                    this.logger.warn("Ignoring invalid file ''{0}''", f);
                    continue;
                }
                IDeployable<?> newDeployable;
                try {
                    newDeployable = DeployableHelper.getDeployable(archive);
                } catch (DeployableHelperException e) {
                    this.logger.error("Cannot get a deployable for the archive '" + archive + "'", e);
                    continue;
                }
                try {
                    this.deployerManager.deploy(newDeployable);
                    // Store the new deployable
                    this.deployed.put(f, newDeployable);
                    // Perform a garbage collector to avoid file lock
                    System.gc();
                } catch (Exception e) {
                    // Deployment of this deployable has failed
                    this.failed.add(f);
                    throw new EmbeddedException("Cannot redeploy the deployable '" + deployable + "'.", e);
                }
            }

        }

    }

    /**
     * Check a container (and its archive) and see if there is a need to reload
     * the container.
     * @param container the container to monitor.
     */
    protected void checkContainer(final EZBContainer container) {

        // get archive
        IArchive archive = container.getArchive();

        // Get URL
        URL url = null;
        try {
            url = archive.getURL();
        } catch (ArchiveException e1) {
            this.logger.warn("Cannot get URL on the container {0}", archive.getName());
            return;
        }
        File file = urlToFile(url);
        // No file archive, means that it has been removed
        if (!file.exists()) {
            this.logger.info("Archive ''{0}'' has been removed, then the associated EJB3 container is stopping", archive
                    .getName());
            try {
                container.stop();
                getEmbedded().removeContainer(container);
            } finally {
                this.deployed.remove(file);
            }

            return;
        }

        // container was modified, need to relaunch it
        if (hasBeenUpdated(file)) {
            this.logger.info("Container with archive {0} was modified. Reloading...", archive.getName());
            try {
                container.stop();
                getEmbedded().removeContainer(container);
            } finally {
                this.deployed.remove(file);
            }
            try {
                container.start();
                getEmbedded().addContainer(container);
            } catch (EZBContainerException e) {
                this.deployed.remove(file);
                this.logger.error("Error while restarting archive {0}.", archive.getName(), e);
            }
        }

    }

    /**
     * Gets the last modified attribute of a given archive.<br> If it is a
     * directory, returns the last modified file of the archive.
     * @param archive the archive to monitor.
     * @return the last modified version of the given archive.
     */
    protected long getLastModified(final File archive) {
        if (archive.isFile()) {
            return archive.lastModified();
        }
        // else
        File[] files = archive.listFiles();
        long last = 0;
        if (files != null) {
            for (File f : files) {
                last = Math.max(last, getLastModified(f));
            }
        }
        return last;
    }


    /**
     * Stop this monitor.
     * @throws EZBMonitorException if it can't be stopped
     */
    public void stop() throws EZBMonitorException {
        this.stopped = true;
    }

    /**
     * Gets the embedded object.
     * @return embedded object
     */
    public EZBServer getEmbedded() {
        return this.embedded;
    }

    /**
     * @return the waiting time.
     */
    public long getWaitTime() {
        return this.waitTime;
    }

    /**
     * Sets the waiting time between each period.
     * @param waitTime the time to wait
     */
    public void setWaitTime(final long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * @return the directory to monitor
     */
    public String getDirectory() {
        return this.directory;
    }

    /**
     * Sets the directory to monitor.
     * @param directory the given directory
     */
    public void setDirectory(final String directory) {
        this.directory = directory;
    }

    /**
     * @return the logger used by this monitor.
     */
    public Log getLogger() {
        return this.logger;
    }
}
