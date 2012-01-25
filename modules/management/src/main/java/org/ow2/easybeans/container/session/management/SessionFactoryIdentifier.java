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
 * $Id: SessionFactoryIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session.management;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;
import org.ow2.easybeans.jsr77.JSR77ManagementIdentifier;

/**
 * Generates an ObjectName for an session factory MBean.
 *
 * @author Guillaume Sauthier
 * @param <T> SessionFactory subclass
 */
public abstract class SessionFactoryIdentifier<T extends SessionFactory> extends
        JSR77ManagementIdentifier<T> {

    /**
     * {@inheritDoc}
     */
    public String getAdditionnalProperties(final T instance) {
        StringBuilder sb = new StringBuilder();
        // EJBModule=?
        EZBContainer container = instance.getContainer();
        String parent = null;
        try {
            parent = MBeansHelper.getInstance().getObjectName(container);
            sb.append(getParentNameProperty(parent));
            sb.append(",");
            sb.append(getPropertyNameValue(parent, "J2EEServer"));
            sb.append(",");
            sb.append(getPropertyNameValue(parent, "J2EEApplication"));
        } catch (MBeansException e) {
            getLogger().warn("Cannot retrieve parent ObjectName for ''{0}''", sb);
        }

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePropertyValue(final T instance) {
        return instance.getClassName();
    }

}
