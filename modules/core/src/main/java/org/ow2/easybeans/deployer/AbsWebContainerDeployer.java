/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: AbsWebContainerDeployer.java 6036 2011-10-27 09:57:07Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.deployment.InjectionHolder;
import org.ow2.easybeans.deployment.api.EZBInjectionHolder;
import org.ow2.easybeans.loader.EasyBeansClassLoader;
import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.easybeans.resolver.ApplicationJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.util.url.URLUtils;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.CARDeployable;
import org.ow2.util.ee.deploy.api.deployable.EARDeployable;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.EJBDeployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployable.LibDeployable;
import org.ow2.util.ee.deploy.api.deployable.WARDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.file.FileUtils;
import org.ow2.util.file.FileUtilsException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class manage the deployment of EAR for web container. It extracts EJB3
 * and send them to EasyBeans while the War are given to the web container.
 * @author Florent Benoit
 */
public abstract class AbsWebContainerDeployer<T extends IDeployable<T>> extends AbsDeployer<T> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(AbsWebContainerDeployer.class);

    /**
     * Deploy the WAR files present in the given EAR.
     * @param earDeployable the EAR containing the WARs
     * @param earURL the EAR URL
     * @param earClassLoader the EAR classloader
     * @param parentClassLoader the parent classloader (EJB) to use
     * @param ejbInjectionHolder the EasyBeans injection holder object
     * @throws DeployerException if the wars are not deployed.
     */
    protected abstract void deployWARs(final EARDeployable earDeployable, final URL earURL, final ClassLoader earClassLoader,
            final ClassLoader parentClassLoader, final EZBInjectionHolder ejbInjectionHolder) throws DeployerException;

    /**
     * Deploy an EAR (called by the deploy method).
     * @param earDeployable a given EAR deployable
     * @throws DeployerException if the deployment is not done.
     */
    protected void deployEAR(final EARDeployable earDeployable) throws DeployerException {

        logger.info("Deploying {0}", earDeployable);

        // Get URL of this EAR
        URL earURL = null;
        try {
            earURL = earDeployable.getArchive().getURL();
        } catch (ArchiveException e) {
            throw new DeployerException("Cannot get the URL for the deployable '" + earDeployable + "'.", e);
        }

        // Create a Root classloader for this EAR
        // Empty classloader
        ClassLoader earClassLoader = new URLClassLoader(new URL[0], Thread.currentThread().getContextClassLoader());

        // Get EJBs of this EAR
        List<EJB3Deployable> ejb3s = earDeployable.getEJB3Deployables();

        // Get the URLs of EJB, WEB and Clients
        List<URL> urlsEJB = new ArrayList<URL>();
        for (EJB3Deployable ejb : ejb3s) {
            try {
                urlsEJB.add(ejb.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the archive '" + ejb.getArchive() + "'", e);
            }
        }
        List<URL> urlsWAR = new ArrayList<URL>();
        for (WARDeployable war : earDeployable.getWARDeployables()) {
            try {
                urlsWAR.add(war.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the archive '" + war.getArchive() + "'", e);
            }
        }
        List<URL> urlsClient = new ArrayList<URL>();
        for (CARDeployable car : earDeployable.getCARDeployables()) {
            try {
                urlsClient.add(car.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the archive '" + car.getArchive() + "'", e);
            }
        }

        // Get libraries of this EAR
        List<LibDeployable> libs = earDeployable.getLibDeployables();

        // Create array of URLs with EJBs + Libraries
        List<URL> urls = new ArrayList<URL>();
        for (EJBDeployable<?> ejb : ejb3s) {
            try {
                urls.add(ejb.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the Archive '" + ejb.getArchive() + "'.", e);
            }
        }
        for (LibDeployable lib : libs) {
            try {
                urls.add(lib.getArchive().getURL());
            } catch (ArchiveException e) {
                throw new DeployerException("Cannot get the URL for the Archive '" + lib.getArchive() + "'.", e);

            }
        }

        // Create classloader with these URLs
        URL[] arrayURLs = urls.toArray(new URL[urls.size()]);
        ClassLoader ejbClassLoader = new EasyBeansClassLoader(arrayURLs, Thread.currentThread().getContextClassLoader());

        // Get Persistence unit manager
        EZBPersistenceUnitManager persistenceUnitManager = getPersistenceUnitManager(earDeployable, ejbClassLoader);

        // Get Extra libraries
        List<IArchive> libArchives = getLibArchives(earDeployable);


        // Create containers for each EJB deployable
        List<EZBContainer> containers = new ArrayList<EZBContainer>();
        for (EJBDeployable<?> ejb : ejb3s) {
            containers.add(getEmbedded().createContainer(ejb));
        }

        // Create Resolver for EAR
        EZBApplicationJNDIResolver applicationJNDIResolver = new ApplicationJNDIResolver();

        // Create EasyBeans injection Holder
        InjectionHolder ejbInjectionHolder = new InjectionHolder();
        ejbInjectionHolder.setPersistenceUnitManager(persistenceUnitManager);
        ejbInjectionHolder.setJNDIResolver(applicationJNDIResolver);

        // Configure containers
        for (EZBContainer container : containers) {
            // Set the classloader that needs to be used
            container.setClassLoader(ejbClassLoader);

            // Set application name
            container.getConfiguration().setApplicationName(earDeployable.getModuleName());

            // Add persistence context found
            container.setPersistenceUnitManager(persistenceUnitManager);

            // Add the metadata
            container.setExtraArchives(libArchives);

            // set parent JNDI Resolver
            EZBContainerJNDIResolver containerJNDIResolver = container.getConfiguration().getContainerJNDIResolver();
            containerJNDIResolver.setApplicationJNDIResolver(applicationJNDIResolver);

            // Add child on application JNDI Resolver
            applicationJNDIResolver.addContainerJNDIResolver(containerJNDIResolver);

            // Resolve container
            try {
                container.resolve();
            } catch (EZBContainerException e) {
                throw new DeployerException("Cannot resolve the container '" + container.getArchive() + "'.", e);
            }

        }

        // Start containers
        for (EZBContainer container : containers) {
            try {
                container.start();
            } catch (EZBContainerException e) {
                logger.error("Cannot start container {0}", container.getName(), e);
            }
        }

        // Deploy Web App
        deployWARs(earDeployable, earURL, earClassLoader, ejbClassLoader, ejbInjectionHolder);

    }

    /**
     * Undeploy an EAR (called by the undeploy method).
     * @param tmpEARDeployable a given EAR deployable
     * @throws DeployerException if the undeployment is not done.
     */
    protected void undeployEAR(final EARDeployable tmpEARDeployable) throws DeployerException {
        logger.info("Undeploying {0}", tmpEARDeployable);

        // From which deployable get the containers deployed
        EARDeployable earDeployable = tmpEARDeployable;

        // Check if this archive has been unpacked ?
        EARDeployable unpackedDeployable = earDeployable.getUnpackedDeployable();
        if (unpackedDeployable != null) {
            earDeployable = unpackedDeployable;
        }

        // Need to undeploy Wars from the EAR
        List<WARDeployable> wars = earDeployable.getWARDeployables();
        if (wars != null) {
            for (WARDeployable war : wars) {
                // undeploy the given war
                try {
                    undeployWAR(war);
                } catch (DeployerException e) {
                    logger.error("Cannot undeploy the WAR deployable ''{0}''", war, e);
                }
            }
        }

        // Undeploy EJB3s
        undeployEJB3FromEAR(earDeployable);

        logger.info("''{0}'' EAR Deployable is now undeployed", tmpEARDeployable);
    }

    /**
     * Undeploy the given deployable. It can be an EJB jar, EAR, WAR, etc.
     * @param deployable a given deployable to undeploy
     * @throws DeployerException if the undeploy operation fails.
     */
    @Override
    public void undeploy(final IDeployable<T> deployable) throws DeployerException {
        if (EARDeployable.class.isAssignableFrom(deployable.getClass())) {
            undeployEAR(EARDeployable.class.cast(deployable));
        } else {
            throw new UnsupportedOperationException("Undeploy only .ear files");
        }
    }

    /**
     * Undeploy an given WAR (called by the undeploy method).
     * @param warDeployable a given WAR deployable
     * @throws DeployerException if the undeployment is not done.
     */
    protected abstract void undeployWAR(final WARDeployable warDeployable) throws DeployerException;

    /**
     * Unpack the given war into a temp directory.
     * @param warFile the war to unpack
     * @param war data on the given war
     * @param earURL url of the EAR that contains the war
     * @return the File to the unpack directory
     * @throws DeployerException if unpack fails
     */
    protected File unpack(final File warFile, final WARDeployable war, final URL earURL) throws DeployerException {
        // Get EAR application name
        String earName = URLUtils.urlToFile(earURL).getName();

        // unpack Root directory
        String rootUnpackDir = System.getProperty("java.io.tmpdir") + File.separator + System.getProperty("user.name")
                + "-EasyBeans-unpack" + File.separator;

        // Unpack directory
        File unpackDir = new File(rootUnpackDir, earName + File.separator + warFile.getName());

        // Build a JarFile on the war
        JarFile packedJar;
        try {
            packedJar = new JarFile(warFile);
        } catch (IOException e) {
            throw new DeployerException("The war file '" + warFile + "' is not a valid war file", e);
        }

        // Unpack the war
        try {
            FileUtils.unpack(packedJar, unpackDir);
        } catch (FileUtilsException e) {
            throw new DeployerException("Cannot unpack the file '" + packedJar + "' in the directory '" + unpackDir + "'.", e);
        }

        return unpackDir;

    }

    /**
     * Deploy a deployable. It can be an EJB jar, EAR, WAR, etc.
     * @param deployable a given deployable
     * @throws DeployerException if the deployment is not done.
     */
    @Override
    public void deploy(final IDeployable<T> deployable) throws DeployerException {
        checkSupportedDeployable(deployable);
        if (deployable instanceof EJBDeployable) {
            deployEJB((EJBDeployable<?>) deployable);
        } else if (EARDeployable.class.isAssignableFrom(deployable.getClass())) {
            // needs to unpack it before deploying it
            EARDeployable earDeployable = unpackEARDeployable(EARDeployable.class.cast(deployable));
            deployEAR(earDeployable);
        }
    }
    /**
     * Check that the given deployable is supported by this deployer. If it is
     * not supported, throw an error.
     * @param deployable the deployable that needs to be deployed
     * @throws DeployerException if this deployable is not supported.
     */
    private void checkSupportedDeployable(final IDeployable<?> deployable) throws DeployerException {
        if (!(EARDeployable.class.isAssignableFrom(deployable.getClass()) || EJBDeployable.class.isAssignableFrom(deployable
                .getClass()))) {
            throw new DeployerException("The deployable '" + deployable + "' is not supported by this deployer");
        }
    }
}
