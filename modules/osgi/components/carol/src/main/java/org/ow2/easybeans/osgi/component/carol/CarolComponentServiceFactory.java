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
 * $Id: CarolComponentServiceFactory.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.component.carol;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import javax.rmi.PortableRemoteObject;

import org.osgi.service.component.ComponentContext;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.osgi.component.EZBComponentServiceFactory;

/**
 * A specific Service Factory for Carol as it must wais for Carol Startup before actually registering the corresponding
 * component.
 */
public class CarolComponentServiceFactory extends EZBComponentServiceFactory {

    /** Needed time to finish the Carol start. */
    private static final int WAITING_TIME_MS = 1000;

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
     * (non-Javadoc)
     * @see org.ow2.easybeans.osgi.component.EZBComponentServiceFactory#postComponentStart(org.ow2.easybeans.component.api.EZBComponent)
     */
    @Override
    protected void postComponentStart(final EZBComponent ezbComponent) {
        try {
            Thread.sleep(WAITING_TIME_MS);
        } catch (InterruptedException e) {
            // Ignore
        }

        // Force the creation of a new ProDelegate object (if possible)
        // Without doing thing this, the JVM may have links to previous instances of the classes
        try {
           Method createDelegate = PortableRemoteObject.class.getDeclaredMethod("createDelegateIfSpecified", String.class);
           createDelegate.setAccessible(true);
           Object proDelegate = createDelegate.invoke(null, "javax.rmi.CORBA.PortableRemoteObjectClass");
           Field proField = PortableRemoteObject.class.getDeclaredField("proDelegate");
           proField.setAccessible(true);
           proField.set(null, proDelegate);
        } catch (Exception e) {
            System.out.println("Unable to create a new ProDelegate object");
        }




    }

}
