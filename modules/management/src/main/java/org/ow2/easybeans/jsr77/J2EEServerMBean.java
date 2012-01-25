/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: J2EEServerMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import java.util.Map;

import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.server.Version;

/**
 * J2EEServer MBean for JSR 77.
 * @author Guillaume Sauthier
 */
public class J2EEServerMBean extends J2EEManagedObjectMBean<EZBServer> {

    /**
     * @throws MBeanException if creation fails
     * @throws RuntimeOperationsException if creation fails
     */
    public J2EEServerMBean() throws MBeanException, RuntimeOperationsException {
        super();
    }

    /**
     * @return Returns the ObjectNames of the deployed objects.
     */
    public String[] getDeployedObjects() {
        Map<String, EZBContainer> containers = getManagedComponent().getContainers();
        int size = containers.size();
        String[] deployedObjects = new String[size];

        int index = 0;
        for(EZBContainer container : containers.values()) {

            try {
                deployedObjects[index] = MBeansHelper.getInstance().getObjectName(container);
            } catch (MBeansException e) {
                deployedObjects[index] = "";
            }

            index++;
        }

        return deployedObjects;
    }

    /**
     * non-sens in EasyBeans.
     * @return Returns the ObjectName of the deployed Resources.
     */
    public String[] getResources() {
        // TODO to be implemented
        throw new UnsupportedOperationException("Not implemented yet !");
    }

    /**
     * @return Returns the ObjectNames of the Java VMs.
     */
    public String[] getJavaVMs() {
        // TODO to be implemented
        throw new UnsupportedOperationException("Not implemented yet !");
    }

    /**
     * @return Returns the Server Vendor name.
     */
    public String getServerVendor() {
        return "OW2";
    }

    /**
     * @return Returns the Server version.
     */
    public String getServerVersion() {
        return Version.getVersion();
    }

}
