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
 * $Id: JContainer3Identifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.management;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.jsr77.JSR77ManagementIdentifier;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;

/**
 * Generates an ObjectName for an EZBContainer MBean.
 *
 * @author Guillaume Sauthier
 */
public class JContainer3Identifier extends
        JSR77ManagementIdentifier<EZBContainer> {

    /**
     * JMX MBean Type.
     */
    private static final String TYPE = "EJBModule";

    /**
     * {@inheritDoc}
     */
    public String getAdditionnalProperties(final EZBContainer instance) {
        StringBuilder sb = new StringBuilder();
        // J2EEServer=?
        EZBServer server = instance.getConfiguration().getEZBServer();
        String parent = null;
        try {
            parent = MBeansHelper.getInstance().getObjectName(server);
            sb.append(getParentNameProperty(parent));

            // Append the Application
            sb.append(",J2EEApplication=");
            sb.append(instance.getConfiguration().getApplicationName());

        } catch (MBeansException e) {
            getLogger().warn("Cannot retrieve parent ObjectName for ''{0}''", sb);
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePropertyValue(final EZBContainer instance) {
        try {
            IDeployable<?> deployable = instance.getDeployable();
            // Try to get the original deployable is not null
            if (deployable.getOriginalDeployable() != null) {
                deployable = deployable.getOriginalDeployable();
            }

            return shorterName(deployable.getArchive().getURL());
        } catch (ArchiveException e) {
            getLogger().warn("Cannot get URL for the container ''{0}''", e);
        }
        return instance.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeValue() {
        return TYPE;
    }

}
