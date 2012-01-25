/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: WorkManagerComponentServiceFactory.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.component.workmanager;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.itf.EZBWorkManagerComponent;
import org.ow2.easybeans.component.itf.TMComponent;
import org.ow2.easybeans.component.jotm.JOTMComponent;
import org.ow2.easybeans.osgi.component.EZBComponentServiceFactory;

/**
 * Service Factory for Work Manager component.
 * @author Florent Benoit
 */
public class WorkManagerComponentServiceFactory extends EZBComponentServiceFactory {
    /**
     * The transaction component.
     */
    private TMComponent transactionComponent;

    /**
     * This method is required to allow KnopfleFish implementation of the SCR.
     * The method needs to be present on each class implementing ManagedServiceFactory (no inheritance).
     * @param componentContext the given component context.
     */
    @Override
    public void activate(final ComponentContext componentContext) {
        super.activate(componentContext);
    }

    /**
     * This method is required to allow KnopfleFish implementation of the SCR.
     * De-activate this DS Component. This will unregister all created services.
     * @param componentContext DS {@link ComponentContext}
     */
    @Override
    public void deactivate(final ComponentContext componentContext) {
      super.deactivate(componentContext);
    }

    /**
     * This method is required to allow KnopfleFish implementation of the SCR.
     * The method needs to be present on each class implementing ManagedServiceFactory (no inheritance).
     * @return The name.
     */
    @Override
    public String getName() {
        return super.getName();
    }

    /**
     * Set the tm component.
     * @param transactionComponent The tm component.
     */
    public synchronized void setTMComponent(final JOTMComponent transactionComponent) {
        this.transactionComponent = transactionComponent;
    }

    /**
     * Unset the event component.
     * @param transactionComponent The event component.
     */
    public synchronized void unsetTMComponent(final JOTMComponent transactionComponent) {
        this.transactionComponent = null;
    }

    /**
     * Get the configured component.
     * @param componentClass The component class.
     * @param configuration The configuration.
     * @return The configured component.
     * @throws ConfigurationException If an error occurs.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected EZBComponent getConfiguredComponent(final Class<? extends EZBComponent> componentClass,
            final Dictionary configuration) throws ConfigurationException {
        EZBWorkManagerComponent component = (EZBWorkManagerComponent) super.getConfiguredComponent(componentClass, configuration);
        component.setTransactionComponent(transactionComponent);
        return component;
    }
}
