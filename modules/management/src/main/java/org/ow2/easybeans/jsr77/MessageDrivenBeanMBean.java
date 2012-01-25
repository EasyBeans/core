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
 * $Id: MessageDrivenBeanMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.MBeanException;

import org.ow2.easybeans.container.mdb.MDBMessageEndPointFactory;

/**
 * MBean for Message Driven Bean.
 * @author Florent Benoit
 */
public abstract class MessageDrivenBeanMBean extends EJBMBean<MDBMessageEndPointFactory> {

    /**
     * Creates a new Managed Object.
     * @throws MBeanException if creation fails.
     */
    public MessageDrivenBeanMBean() throws MBeanException {
        super();
    }

    /**
     * Gets the className of this SessionBean.
     * @return classname that will be instantiated to build bean instance.
     */
    @Override
    public String getClassName() {
        return getManagedComponent().getClassName();
    }
}
