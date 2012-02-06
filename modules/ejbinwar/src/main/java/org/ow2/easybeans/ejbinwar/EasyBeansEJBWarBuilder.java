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

package org.ow2.easybeans.ejbinwar;


import java.util.Map;

import javax.naming.Context;

import org.ow2.easybeans.container.JContainerConfig;
import org.ow2.easybeans.deployment.EasyBeansDeployableInfo;
import org.ow2.easybeans.resolver.api.EZBApplicationJNDIResolver;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployable.WARDeployable;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.api.helper.IDeployableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Builder of EJB3 Deployable from a War deployable and some parameters.
 * @author Florent Benoit
 */
public class EasyBeansEJBWarBuilder {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(EasyBeansEJBWarBuilder.class);

    /**
     * DeployableHelper.
     */
    private IDeployableHelper deployableHelper = null;

    /**
     * @param warDeployable the War deployable to transform
     * @param properties the properties to help to build the EJB3
     * @return an EJB3 deployable from the given WAR
     */
    public EJB3Deployable getEJBFromWarDeployable(final WARDeployable warDeployable, final Map<?, ?> properties) {
        IArchive warArchive = warDeployable.getArchive();

        // Build EJB/War archive.
        EJBInWarArchive ejbInWarArchive;
        try {
            ejbInWarArchive = new EJBInWarArchive(warArchive);
        } catch (ArchiveException e) {
            throw new IllegalStateException("Cannot build archive", e);
        }

        // Check if there are EJB inside the archive
        EJB3Deployable ejb3Deployable = getEJB3(ejbInWarArchive);
        if (ejb3Deployable == null) {
            return null;
        }

        // Get parameters from properties
        ClassLoader classLoader = (ClassLoader) properties.get(ClassLoader.class);
        EZBApplicationJNDIResolver applicationJNDIResolver = (EZBApplicationJNDIResolver) properties
                .get(EZBApplicationJNDIResolver.class);


        String moduleName = (String) properties.get("module.name");
        String applicationName = (String) properties.get("application.name");

        Context moduleContext = (Context) properties.get("module.context");
        Context appContext = (Context) properties.get("application.context");

        // Store parameters in the Deployable Info metadata
        EasyBeansDeployableInfo easyBeansDeployableInfo = new EasyBeansDeployableInfo();

        // Define classloader
        easyBeansDeployableInfo.setClassLoader(classLoader);

        // Set the JNDI resolver
        easyBeansDeployableInfo.setApplicationJNDIResolver(applicationJNDIResolver);

        // Do not unpack again, war has already been unpacked
        easyBeansDeployableInfo.setHasToBeUnpacked(Boolean.FALSE);

        // Module Name and application Name
        JContainerConfig containerConfig = new JContainerConfig(ejb3Deployable);
        easyBeansDeployableInfo.setContainerConfiguration(containerConfig);

        // Names and java Contexts
        containerConfig.setApplicationName(applicationName);
        containerConfig.setModuleName(moduleName);
        containerConfig.setModuleContext(moduleContext);
        containerConfig.setAppContext(appContext);


        // Add extension on the deployable
        ejb3Deployable.addExtension(easyBeansDeployableInfo);

        return ejb3Deployable;

    }

    /**
     * Try to see if the archive contains EJB.
     * @param ejbInWarArchive the wrapped war archive
     * @return true if the given war contains XML desc or EJB annotations.
     */
    private EJB3Deployable getEJB3(final EJBInWarArchive ejbInWarArchive) {

        IDeployable<?> deployable = null;
        try {
            deployable = this.deployableHelper.getDeployable(ejbInWarArchive);
        } catch (DeployableHelperException e) {
            LOGGER.error("Unable to get deployable from the archive ''{0}''", ejbInWarArchive, e);
        }

        // Check deployable
        if (deployable != null && deployable instanceof EJB3Deployable) {
            return EJB3Deployable.class.cast(deployable);
        }
        // Not an EJB3, return null
        return null;
    }

    /**
     * Sets the deployable helper.
     * @param deployableHelper the given deployable helper.
     */
    public void setDeployableHelper(final IDeployableHelper deployableHelper) {
        this.deployableHelper = deployableHelper;
    }


}
