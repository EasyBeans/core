/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: JmxComponentServiceFactory.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.component.jmx;

import java.util.Dictionary;

import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.ComponentContext;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.jmx.JmxComponent;
import org.ow2.easybeans.osgi.component.EZBComponentServiceFactory;
import org.ow2.util.jmx.api.ICommonsModelerExtService;

/**
 * A specific Service Factory for
 */
public class JmxComponentServiceFactory extends EZBComponentServiceFactory {
    private ICommonsModelerExtService service;
    
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
     */
    @Override
    public String getName() {
        return super.getName();
    }
    
    /**
     * Set the CommonsModelerExtService.
     * @param service The CommonsModelerExtService.
     */
    public synchronized void setCommonsModelerExtService(final ICommonsModelerExtService service) {
        this.service = service;
    }
    
    /**
     * Unset the CommonsModelerExtService.
     * @param service The CommonsModelerExtService.
     */
    public synchronized void unsetCommonsModelerExtService(final ICommonsModelerExtService service) {
        this.service = null;
    }
    
    @Override
    protected EZBComponent getConfiguredComponent(Class<? extends EZBComponent> componentClass, Dictionary configuration)
            throws ConfigurationException {
        JmxComponent component = (JmxComponent) super.getConfiguredComponent(componentClass, configuration);
        component.setCommonsModelerExtService(this.service);
        return component;
    }
}
