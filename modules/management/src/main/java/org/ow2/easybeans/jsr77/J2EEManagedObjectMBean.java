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
 * $Id: J2EEManagedObjectMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.RuntimeOperationsException;
import javax.management.modelmbean.InvalidTargetObjectTypeException;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.component.itf.EZBJmxComponent;
import org.ow2.util.jmx.impl.BaseModelMBeanExt;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * J2EEManagedObject MBean Base.
 * @author Guillaume Sauthier
 * @param <T> ManagedObject type
 */
public class J2EEManagedObjectMBean<T extends EZBJ2EEManagedObject> extends BaseModelMBeanExt {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(J2EEManagedObjectMBean.class);

    /**
     * Is the MBean an Event Provider ? (Optionnal support)
     */
    private static final boolean IS_EVENT_PROVIDER = false;

    /**
     * Is the MBean a Statistics Provider ? (Optionnal support)
     */
    private static final boolean IS_STATISTICS_PROVIDER = false;

    /**
     * Is the MBean State Manageable ? (Optionnal support)
     */
    private static final boolean IS_STATE_MANAGEABLE = false;

    /**
     * Create the mbean.
     * @throws MBeanException if the super constructor fails.
     */
    public J2EEManagedObjectMBean() throws MBeanException {
        super();
    }


    /**
     * @return the deployer (managed object)
     */
    @SuppressWarnings("unchecked")
    protected T getManagedComponent() {
        T deployer = null;
        try {
            deployer = (T) getManagedResource();
        } catch (InstanceNotFoundException e) {
            throw new IllegalStateException("Cannot get the managed resource of the MBean", e);
        } catch (RuntimeOperationsException e) {
            throw new IllegalStateException("Cannot get the managed resource of the MBean", e);
        } catch (MBeanException e) {
            throw new IllegalStateException("Cannot get the managed resource of the MBean", e);
        } catch (InvalidTargetObjectTypeException e) {
            throw new IllegalStateException("Cannot get the managed resource of the MBean", e);
        }
        return deployer;
    }

    /**
     * @return Returns true is the MBean can manage its state.
     */
    public boolean isStateManageable() {
        return IS_STATE_MANAGEABLE;
    }

    /**
     * @return Returns true if this MBean can provides JSR77 Statistics.
     */
    public boolean isStatisticsProvider() {
        return IS_STATISTICS_PROVIDER;
    }

    /**
     * @return Returns true if this MBean can provides JSR77 Events.
     */
    public boolean isEventProvider() {
        return IS_EVENT_PROVIDER;
    }

    /**
     * @return Returns the logger.
     */
    protected static final Log getLogger() {
        return logger;
    }

    @Override
    public void postRegister(final Boolean registrationDone) {
        EZBJmxComponent jmxComp = getManagedComponent().getComponent(EZBJmxComponent.class);
        if (jmxComp != null) {
            jmxComp.registerJ2EEManagedObject(getManagedComponent(), this);
        }
    }

    @Override
    public void preDeregister() throws Exception {
        EZBJmxComponent jmxComp = getManagedComponent().getComponent(EZBJmxComponent.class);
        if (jmxComp != null) {
            jmxComp.unregisterJ2EEManagedObject(getManagedComponent());
        }
    }

    /**
     * Get the BaseModelMBeanExt id.
     * @return The BaseModelMBeanExt id.
     */
    @Override
    public String getBaseModelMBeanExtId() {
        return getManagedComponent().getJ2EEManagedObjectId();
    }
}
