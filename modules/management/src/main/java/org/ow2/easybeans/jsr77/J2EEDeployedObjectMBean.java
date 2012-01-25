/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: J2EEDeployedObjectMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.MBeanException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * J2EEDeployedObject JSR77 MBean.
 * @param <T> ManagedObject type
 * @author Guillaume Sauthier
 * @author Florent BENOIT
 */
public class J2EEDeployedObjectMBean<T extends EZBJ2EEManagedObject> extends J2EEManagedObjectMBean<T> {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(J2EEDeployedObjectMBean.class);

    /**
     * J2EE server key.
     */
    public static final String J2EESERVER_KEY = "J2EEServer";

    /**
     * Creates a J2EEDeployedObject.
     * @throws MBeanException if creation fails.
     */
    public J2EEDeployedObjectMBean() throws MBeanException {
        super();
    }

    /**
     * @return Returns the XML Deployment Descriptors of the Module.
     */
    public String getDeploymentDescriptor() {
        // TODO implement it !
        // The returned value should be a recomputed deploymentDescriptor
        // merging XML + annotations
        return "TO BE IMPLEMENTED";
    }

    /**
     * @return Returns the J2EEServer ObjectName.
     */
    public String getServer() {
        String serverName = null;
        // Read ObjectName of this MBean
        try {
            ObjectName on = new ObjectName(getObjectName());
            // build ObjectName
            serverName = getJ2EEServer(on.getDomain(), on.getKeyProperty(J2EESERVER_KEY)).toString();
        } catch (MalformedObjectNameException e) {
            throw new IllegalStateException("Cannot build Server object name", e);
        }
        return serverName;
    }

    /**
     * Gets ObjectName for a J2EEServer MBean.
     * @param domainName domain name
     * @param serverName server name
     * @return the created ObjectName
     */
    public static ObjectName getJ2EEServer(final String domainName, final String serverName) {
        try {
            StringBuffer sb = new StringBuffer(domainName);
            sb.append(":j2eeType=J2EEServer");
            sb.append(",name=");
            sb.append(serverName);
            return new ObjectName(sb.toString());
        } catch (javax.management.MalformedObjectNameException e) {
            logger.error("Cannot build ObjectName for the J2EEServer", e);
            return null;
        }
    }

}
