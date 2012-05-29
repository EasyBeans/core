/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.embeddable;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBException;
import javax.ejb.embeddable.EJBContainer;
import javax.naming.Context;
import javax.naming.NamingException;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.container.JContainerConfig;
import org.ow2.easybeans.deployment.EasyBeansDeployableInfo;
import org.ow2.easybeans.naming.NamingManager;
import org.ow2.easybeans.naming.context.ContextImpl;
import org.ow2.easybeans.server.Embedded;
import org.ow2.easybeans.server.EmbeddedException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.url.URLUtils;


/**
 * Implementation of the EJB Container SPI.
 * @author Florent Benoit
 */
public class EasyBeansEJBContainer extends EJBContainer {

    /**
     * System config file.
     */
    public static final String EASYBEANS_SYSTEM_FILE_PROPERTY = "easybeans.embeddable.config.path";


    /**
     * Default XML file.
     */
    public static final String DEFAULT_XML_FILE = "org/ow2/easybeans/embeddable/easybeans-default.xml";

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(EasyBeansEJBContainer.class);

    /**
     * Embedded instance.
     */
    private Embedded embedded = null;

    /**
     * Properties of this container.
     */
    private Map<?, ?> properties = null;

    /**
     * Build a new container around the given embedded instance of EasyBeans.
     * @param embedded the given instance
     * @param properties some properties specified by the caller
     */
    public EasyBeansEJBContainer(final Embedded embedded, final Map<?, ?> properties) {
        this.embedded = embedded;
        this.properties = properties;
    }

    /**
     * Start the embedded core.
     */
    public void start() {

        URL xmlConfigurationURL = null;

        // User system configuration ?
        String systemPath = System.getProperty(EASYBEANS_SYSTEM_FILE_PROPERTY);
        if (systemPath != null) {
            xmlConfigurationURL = URLUtils.fileToURL(new File(systemPath));
        } else {
            xmlConfigurationURL = Thread.currentThread().getContextClassLoader().getResource(DEFAULT_XML_FILE);
        }


        // Add the configuration URL to the existing list
        this.embedded.getServerConfig().getConfigurationURLs().add(xmlConfigurationURL);

        // Per spec, needs to analyze the CLASSPATH of the JVM
        String classpath = System.getProperty("java.class.path");
        String[] classpathElements = classpath.split(File.pathSeparator);



        Context applicationContext = new ContextImpl("app");
        Context moduleContext = new ContextImpl("module");


        // Build a list of EJB3 container if some are found
        List<EZBContainer> containers = new ArrayList<EZBContainer>();

        if (classpathElements != null) {
            for (String classpathElement : classpathElements) {

                // Get archive (file, directory) for the given element
                IArchive archive = ArchiveManager.getInstance().getArchive(new File(classpathElement));

                // Scan the archive
                IDeployable<?> deployable;
                try {
                    deployable = DeployableHelper.getDeployable(archive);
                } catch (DeployableHelperException e) {
                    throw new EJBException("Cannot get a deployable for the archive '" + archive + "'", e);
                }

                if (deployable instanceof EJB3Deployable) {
                    EasyBeansDeployableInfo deployableInfo = new EasyBeansDeployableInfo();
                    deployable.addExtension(deployableInfo);
                    JContainerConfig containerConfig = new JContainerConfig(deployable);
                    deployableInfo.setContainerConfiguration(containerConfig);

                    // Names and java Contexts
                    //containerConfig.setApplicationName(applicationName);
                    /*containerConfig.setModuleName(moduleName);*/
                    containerConfig.setModuleContext(moduleContext);
                    containerConfig.setAppContext(applicationContext);
                    containers.add(this.embedded.createContainer(deployable));
                }
            }
        }

        LOGGER.info("Found ''{0}'' containers : ''{1}''", Integer.valueOf(containers.size()), containers);

        try {
            this.embedded.start();
        } catch (EmbeddedException e) {
            throw new EJBException("Cannot start the embedded instance", e);
        }



        // Build a new instance of java: context
        try {
            NamingManager.setClientContainerComponentContext(NamingManager.getInstance().createEnvironmentContext("server", null,
                    moduleContext, applicationContext));
        } catch (NamingException e) {
            throw new EJBException("Cannot build java: context", e);
        }

        for (EZBContainer container : containers)  {
            try {
                container.setClassLoader(Thread.currentThread().getContextClassLoader());
                container.start();
            } catch (EZBContainerException e) {
                throw new EJBException("Cannot start the container", e);
            }
        }

    }

    /**
     * Close the given EJB container which is calling the stop method.
     */
    @Override
    public void close() {
        try {
            this.embedded.stop();
        } catch (EmbeddedException e) {
           throw new EJBException("Unable to stop container", e);
        }

    }

    /**
     * @return a context as specified by the SPI.
     */
    @Override
    public Context getContext() {
        return this.embedded.getContext();
    }

}
