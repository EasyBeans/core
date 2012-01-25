/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: EZBAuditComponent.java 5468 2010-04-21 12:44:14Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.audit;

import org.ow2.easybeans.api.EZBJ2EEManagedObject;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.itf.EZBEventComponent;
import org.ow2.util.auditreport.api.ICurrentInvocationID;

/**
 * Interface for the audit component of EasyBeans.
 * @author Mathieu ANCELIN
 */
public interface EZBAuditComponent extends EZBComponent {

    /**
     * Add an audited bean to the audit system.
     * @param object the audited bean.
     */
    void registerJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Remove an audited bean from the audit system.
     * @param object the audited bean.
     */
    void unregisterJ2EEManagedObject(EZBJ2EEManagedObject object);

    /**
     * Set the event component.
     * @param eventComponent The event component.
     */
    void setEventComponent(final EZBEventComponent eventComponent);

    /**
     * @return the invocation current ID.
     */
    ICurrentInvocationID getCurrentInvocationID();
}
