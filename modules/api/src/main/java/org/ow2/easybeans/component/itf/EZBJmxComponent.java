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
 * $Id: EZBJmxComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.api.jmx.EZBMBeanAttribute;
import org.ow2.easybeans.api.jmx.EZBMBeanOperation;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.util.jmx.api.IBaseModelMBeanExt;

/**
 * Interface for the EasyBeans JMX component.
 * @author missonng
 */
public interface EZBJmxComponent extends EZBComponent {
    /**
     * Register a new J2EE managed object.<br>
     * If a J2EE managed object with the same id is already registered, it will be unregistered first.
     * @param object The J2EE managed object to register.
     * @param mbean the BaseModelMBean for this J2EE managed object.
     */
    void registerJ2EEManagedObject(EZBJ2EEManagedObject object, IBaseModelMBeanExt mbean);

    /**
     * Unregister a J2EE managed object.
     * @param object The J2EE managed object to unregister.
     */
    void unregisterJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Register a new mbean attribute.<br>
     * The attribute will automatically be register with each MBean matching his filter.
     * @param mbeanAttribute The mbean attribute to register.
     */
    void registerMBeanAttribute(EZBMBeanAttribute mbeanAttribute);

    /**
     * Unregister a mbean attribute.
     * @param mbeanAttribute The mbean attribute to unregister.
     */
    void unregisterMBeanAttribute(EZBMBeanAttribute mbeanAttribute);

    /**
     * Register a new mbean operation.<br>
     * The operation will automatically be register with each MBean matching his filter.
     * @param mbeanOperation The mbean operation to register.
     */
    void registerMBeanOperation(EZBMBeanOperation mbeanOperation);

    /**
     * Unregister a mbean operation.
     * @param mbeanOperation The mbean operation to unregister.
     */
    void unregisterMBeanOperation(EZBMBeanOperation mbeanOperation);
}
