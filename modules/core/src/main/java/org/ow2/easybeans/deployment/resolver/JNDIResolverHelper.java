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
 * $Id: JNDIResolverHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.deployment.resolver;

import java.net.URL;
import java.util.List;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.bean.info.EZBBeanNamingInfo;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;
import org.ow2.easybeans.deployment.Deployment;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.naming.BeanNamingInfoHelper;
import org.ow2.easybeans.resolver.JNDIBeanData;
import org.ow2.easybeans.resolver.JNDIData;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBJNDIData;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;
import org.ow2.util.ee.metadata.ejbjar.api.xml.struct.IAssemblyDescriptor;
import org.ow2.util.ee.metadata.ejbjar.api.xml.struct.IEJB3;
import org.ow2.util.ee.metadata.ejbjar.api.xml.struct.IMessageDestination;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Classes used to resolve the JNDI Name for a given deployment. Note that only
 * the first name returned by the EZBNamingStrategy is used for resolving JNDI
 * Names, all other names are ignored.
 * @author Florent Benoit<br>
 *         Contributors: S. Ali Tokmen (JNDI naming strategy)
 */
public class JNDIResolverHelper {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(JNDIResolverHelper.class);

    /**
     * Link to the a container resolver.
     */
    private EZBContainerJNDIResolver containerJNDIResolver = null;

    /**
     * Container configuration.
     */
    private EZBContainerConfig containerConfiguration = null;


    /**
     * Constructor (to be used for specifying the application name).
     * @param container the given Container
     */
    public JNDIResolverHelper(final EZBContainer container) {
        this.containerJNDIResolver = container.getConfiguration().getContainerJNDIResolver();
        this.containerConfiguration = container.getConfiguration();
    }

    /**
     * Add a given deployment object to this resolver.
     * @param deployment to add to the resolver.
     */
    public void addDeployment(final Deployment deployment) {

        // Get metadata for jar file analyzed
        EjbJarArchiveMetadata ejbJarAnnotationMetadata = deployment.getEjbJarArchiveMetadata();

        // Get Archive
        IArchive archive = deployment.getArchive();

        // Extract URL from archive
        URL url;
        try {
            url = archive.getURL();
        } catch (ArchiveException e) {
            throw new IllegalStateException("Cannot get the URL on the archive '" + archive + "'.", e);
        }

        EZBNamingStrategy namingStrategy = null;
        EZBContainerConfig config = deployment.getConfiguration();
        if (config != null) {
            List<EZBNamingStrategy> strategies = deployment.getConfiguration().getNamingStrategies();
            if (strategies.size() == 1) {
                namingStrategy = strategies.get(0);
            } else if (strategies.size() > 0) {
                this.logger.warn("Using first strategy in the list '" + strategies + "'.");
                namingStrategy = strategies.get(0);
            }
        }

        // Takes the first naming strategy
        if (namingStrategy == null) {
            throw new IllegalStateException("Cannot have empty strategy for deployment " + deployment.getArchive().getName());

        }

        // Add info extracted from metadata
        analyzeMetadata(ejbJarAnnotationMetadata, namingStrategy, url);
    }

    /**
     * Adds the given metadata to the resolver.
     * @param ejbJarArchiveMetadata the metadata for a given jar file
     * @param namingStrategy JNDI naming strategy.
     * @param url the url of the archive
     */
    private void analyzeMetadata(final EjbJarArchiveMetadata ejbJarArchiveMetadata, final EZBNamingStrategy namingStrategy,
            final URL url) {

        // For each bean, get the interfaces and add the jndiName mapping.
        List<String> beanNames = ejbJarArchiveMetadata.getBeanNames();
        for (String beanName : beanNames) {
            List<String> keys = ejbJarArchiveMetadata.getClassesnameForBean(beanName);
            for (String key : keys) {
                EasyBeansEjbJarClassMetadata classAnnotationMetadata =  ejbJarArchiveMetadata.getClassForBean(beanName, key);
                if (classAnnotationMetadata.isBean()) {

                    // Look at local interfaces
                    IJLocal localItfs = classAnnotationMetadata.getLocalInterfaces();
                    IJRemote remoteItfs = classAnnotationMetadata.getRemoteInterfaces();
                    if (localItfs != null) {
                        for (String itf : localItfs.getInterfaces()) {
                            EZBBeanNamingInfo namingInfo = BeanNamingInfoHelper.buildInfo(classAnnotationMetadata, itf, "Local",
                                    this.containerConfiguration);
                            addInterface(namingInfo, namingStrategy, url);
                        }
                    }

                    // Look at remote interfaces
                    if (remoteItfs != null) {
                        for (String itf : remoteItfs.getInterfaces()) {
                            EZBBeanNamingInfo namingInfo = BeanNamingInfoHelper.buildInfo(classAnnotationMetadata, itf, "Remote",
                                    this.containerConfiguration);
                            addInterface(namingInfo, namingStrategy, url);
                        }
                    }

                    // Remote Home
                    String remoteHome = classAnnotationMetadata.getRemoteHome();
                    if (remoteHome != null) {
                        EZBBeanNamingInfo namingInfo = BeanNamingInfoHelper.buildInfo(classAnnotationMetadata, remoteHome,
                                "RemoteHome", this.containerConfiguration);
                        addInterface(namingInfo, namingStrategy, url);
                    }

                    // Local Home
                    String localHome = classAnnotationMetadata.getLocalHome();
                    if (localHome != null) {
                        EZBBeanNamingInfo namingInfo = BeanNamingInfoHelper.buildInfo(classAnnotationMetadata, localHome,
                                "LocalHome", this.containerConfiguration);
                        addInterface(namingInfo, namingStrategy, url);
                    }


                    // No Interface Local view
                    if (classAnnotationMetadata.isLocalBean()) {
                        EZBBeanNamingInfo namingInfo = BeanNamingInfoHelper.buildInfo(classAnnotationMetadata,
                                classAnnotationMetadata.getClassName().replace("/", "."), "NoInterfaceLocalView",
                                this.containerConfiguration);
                        addInterface(namingInfo, namingStrategy, url);
                    }

                }
            }
        }

        // Message Destination
        IEJB3 ejb3DD = ejbJarArchiveMetadata.getEjb3();
        if (ejb3DD != null) {
            IAssemblyDescriptor assemblyDescriptor = ejb3DD.getAssemblyDescriptor();
            if (assemblyDescriptor != null) {
                List<IMessageDestination> messageDestinationList = assemblyDescriptor.getMessageDestinationlist();
                if (messageDestinationList != null) {
                    for (IMessageDestination messageDestination : messageDestinationList) {
                        String messageDestinationName = messageDestination.getName();
                        String mappedName = messageDestination.getMappedName();
                        if (mappedName != null) {
                            EZBJNDIData jndiData = new JNDIData(mappedName);
                            this.containerJNDIResolver.addMessageDestinationJNDIName(messageDestinationName, jndiData);
                        } else {
                            this.logger.warn("Found a message-destination with name ''{0}'' without mapped name",
                                    messageDestinationName);
                        }

                    }
                }
            }
        }
    }

    /**
     * Add the jndi name for a given interface.
     * @param beanNamingInfo bean naming information
     * @param namingStrategy JNDI naming strategy
     * @param containerURL the name of the jar file which contains the ejbs.
     */
    private void addInterface(final EZBBeanNamingInfo beanNamingInfo, final EZBNamingStrategy namingStrategy,
            final URL containerURL) {
        // Get the JNDI name
        String jndiName = namingStrategy.getJNDINaming(beanNamingInfo).jndiName();

        // interface name
        String interfaceName = beanNamingInfo.getInterfaceName();

        // bean name
        String beanName = beanNamingInfo.getName();

        // Add entry on the resolver
        this.containerJNDIResolver.addEJBJNDIName(interfaceName, new JNDIBeanData(jndiName, beanName));

    }

}
