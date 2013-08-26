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
 * $Id: EasyBeansDeployer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.deployment.InjectionHolder;
import org.ow2.easybeans.persistence.EZBPersistenceUnitManager;
import org.ow2.easybeans.resolver.ApplicationJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.EARDeployable;
import org.ow2.util.ee.deploy.api.deployable.EJBDeployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployer.DeployerException;
import org.ow2.util.ee.deploy.api.deployer.IDeployer;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Implementation of the Deployer for EasyBeans in standalone mode (or default
 * mode). It can deploy EJB-JAR or EAR deployable
 * @author Florent Benoit
 */
public class EasyBeansDeployer extends AbsDeployer implements IDeployer {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EasyBeansDeployer.class);

    /**
     * Deploy a deployable. It can be an EJB jar, EAR, WAR, etc.
     * @param deployable a given deployable
     * @throws DeployerException if the deployment is not done.
     */
    @Override
    public void deploy(final IDeployable deployable) throws DeployerException {
        checkSupportedDeployable(deployable);
        if (deployable instanceof EJBDeployable) {
            deployEJB((EJBDeployable) deployable);
        } else if (deployable instanceof EARDeployable) {
            // needs to unpack it before deploying it
            EARDeployable earDeployable = unpackEARDeployable((EARDeployable) deployable);
            deployEAR(earDeployable);
        }
    }

    /**
     * Undeploy the given deployable. It can be an EJB jar, EAR, WAR, etc.
     * @param deployable a given deployable to undeploy
     * @throws DeployerException if the undeploy operation fails.
     */
    @Override
    public void undeploy(final IDeployable deployable) throws DeployerException {
        checkSupportedDeployable(deployable);
        if (deployable instanceof EJBDeployable) {
            throw new UnsupportedOperationException("Single EJB jar should not be removed by this deployer");
        } else if (deployable instanceof EARDeployable) {
            undeployEJB3FromEAR((EARDeployable) deployable);
        }



    }

    /**
     * Deploy an EAR (called by the deploy method).
     * @param earDeployable a given EAR deployable
     * @throws DeployerException if the deployment is not done.
     */
    protected void deployEAR(final EARDeployable earDeployable) throws DeployerException {

        // Needs to deploy all the EJB containers of the EAR with the same
        // classloader
        logger.info("Deploying {0}", earDeployable);

        // Get EJBs of this EAR
        List<EJBDeployable<?>> ejbs = earDeployable.getEJBDeployables();

        // Build classloader
        ClassLoader appClassLoader = getClassLoaderForEAR(earDeployable);

        // Get Persistence unit manager
        EZBPersistenceUnitManager persistenceUnitManager = getPersistenceUnitManager(earDeployable, appClassLoader);

        // Get Extra libraries
        List<IArchive> libArchives = getLibArchives(earDeployable);

        // Create containers for each EJB deployable
        List<EZBContainer> containers = new ArrayList<EZBContainer>();
        for (EJBDeployable<?> ejb : ejbs) {
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
            container.setClassLoader(appClassLoader);

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

    }

    /**
     * Check that the given deployable is supported by this deployer. If it is
     * not supported, throw an error.
     * @param deployable the deployable that needs to be deployed
     * @throws DeployerException if this deployable is not supported.
     */
    private void checkSupportedDeployable(final IDeployable deployable) throws DeployerException {
        if (!(deployable instanceof EARDeployable || deployable instanceof EJBDeployable)) {
            throw new DeployerException("The deployable '" + deployable + "' is not supported by this deployer");
        }
    }

}
