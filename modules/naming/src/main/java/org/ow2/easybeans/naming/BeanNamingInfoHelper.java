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
 * $Id: BeanNamingInfoHelper.java 5733 2011-02-21 12:54:34Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansClassView;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJCommonBean;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJLocal;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJRemote;

/**
 * Helper class that build a BeanNamingInfo object.
 * @author Florent Benoit
 */
public final class BeanNamingInfoHelper {

    /**
     * Utility class.
     */
    private BeanNamingInfoHelper() {

    }

    /**
     * Build a BeanNamingInfo class with the given parameters.
     * @param beanClassMetadata the metadata of the bean
     * @param interfaceName Name of the interface.
     * @param mode local/remote/...
     * @param containerConfig the container configuration
     * @return a BeanNamingInfo instance
     */
    public static BeanNamingInfo buildInfo(final EasyBeansClassView beanClassMetadata, final String interfaceName,
            final String mode, final EZBContainerConfig containerConfig) {
        IJCommonBean commonBean = beanClassMetadata.getJCommonBean();

        String moduleName = containerConfig.getModuleName();
        String javaEEApplicationName = containerConfig.getApplicationName();

        // Compute interface numbers
        int interfaceNumbers = 0;
        IJLocal localItfs = beanClassMetadata.getLocalInterfaces();
        IJRemote remoteItfs = beanClassMetadata.getRemoteInterfaces();
        String remoteHome = beanClassMetadata.getRemoteHome();
        String localHome = beanClassMetadata.getLocalHome();
        if (localItfs != null) {
            interfaceNumbers += localItfs.getInterfaces().size();
        }
        if (remoteItfs != null) {
            interfaceNumbers += remoteItfs.getInterfaces().size();
        }
        if (remoteHome != null) {
            interfaceNumbers++;
        }
        if (localHome != null) {
            interfaceNumbers++;
        }

        if (beanClassMetadata.isLocalBean()) {
            interfaceNumbers++;
        }

        BeanNamingInfo beanNamingInfo = new BeanNamingInfo();
        if (!beanClassMetadata.isSession() && !beanClassMetadata.isMdb() &&
                beanClassMetadata.isManagedBean()) {
            beanNamingInfo.setName(beanClassMetadata.getManagedBeanName());
        } else {
            beanNamingInfo.setName(commonBean.getName());
            beanNamingInfo.setMappedName(commonBean.getMappedName());
        }
        beanNamingInfo.setBeanClassName(beanClassMetadata.getClassMetadata().getClassName());
        beanNamingInfo.setInterfaceName(interfaceName);
        beanNamingInfo.setMode(mode);
        beanNamingInfo.setModuleName(moduleName);
        beanNamingInfo.setJavaEEApplicationName(javaEEApplicationName);

        // Single interface
        beanNamingInfo.setSingleInterface(1 == interfaceNumbers);

        return beanNamingInfo;

    }
}
