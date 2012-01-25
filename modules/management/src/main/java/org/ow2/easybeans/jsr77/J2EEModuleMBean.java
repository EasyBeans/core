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
 * $Id: J2EEModuleMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.MBeanException;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;

/**
 * J2EEModule JSR77 MBean.
 * @param <T> ManagedObject type
 * @author Guillaume Sauthier
 * @author Florent BENOIT
 */
public class J2EEModuleMBean<T extends EZBJ2EEManagedObject> extends J2EEDeployedObjectMBean<T> {

    /**
     * Create the mbean.
     * @throws MBeanException if the super constructor fails.
     */
    public J2EEModuleMBean() throws MBeanException {
        super();
    }

    /**
     * @return Returns the ObjectNames of the Java VMs.
     */
    public String[] getJavaVMs() {
        // TODO to be implemented
        // The returned value should be an array of VMs ObjectNames
        String[] vms = new String[1];
        vms[0] = "TO BE IMPLEMENTED !";
        return vms;
    }
}
