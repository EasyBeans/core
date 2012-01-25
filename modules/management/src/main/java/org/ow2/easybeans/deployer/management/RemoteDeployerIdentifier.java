/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: RemoteDeployerIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer.management;

import org.ow2.easybeans.deployer.IRemoteDeployer;
import org.ow2.easybeans.management.CommonsManagementIdentifier;

/**
 * Generates an ObjectName for an EasyBeans Deployer MBean.
 * @author Guillaume Sauthier
 */
public class RemoteDeployerIdentifier extends CommonsManagementIdentifier<IRemoteDeployer> {

    /**
     * JMX MBean Type.
     */
    private static final String TYPE = "Deployer";

    /**
     * {@inheritDoc}
     */
    public String getAdditionnalProperties(final IRemoteDeployer instance) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePropertyValue(final IRemoteDeployer instance) {
        return "EasyBeans";
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeValue() {
        return TYPE;
    }

}
