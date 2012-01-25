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
 * $Id: FieldInjectorListener.java 3841 2008-08-21 08:43:38Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.listener;

import java.lang.reflect.Field;
import java.util.Collection;

import org.osgi.framework.ServiceReference;
import org.ow2.easybeans.osgi.binder.desc.DependencyDescription;
import org.ow2.easybeans.osgi.binder.util.ReflectionHelper;

/**
 * @author Guillaume
 *
 */
public class FieldInjectorListener implements IDependencyListener {

    private final Object bean;

    private final Field field;

    private final DependencyDescription description;


    public FieldInjectorListener(final Object instance,
                                 final Field field,
                                 final DependencyDescription description) {

        bean = instance;
        this.field = field;
        this.description = description;
    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#added(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void added(final ServiceReference ref, final Object service) {

        if (!ReflectionHelper.isNullableObject(service)) {
            switch(description.getMultiplicity()) {
            case MULTIPLE:
                // first get the existing value
                Collection collection = ReflectionHelper.getFieldValue(bean, field, Collection.class);
                // then add the new value
                collection.add(service);
                break;
            case SINGLE:
                // directly inject
                ReflectionHelper.setFieldValue(bean, field, service);
                break;
            }
        }
    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#changed(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    public void changed(final ServiceReference ref, final Object service) {
        // Do nothing for field

    }

    /* (non-Javadoc)
     * @see org.ow2.easybeans.osgi.binder.internal.IServiceDependencyListener#removed(org.osgi.framework.ServiceReference, java.lang.Object)
     */
    @SuppressWarnings("unchecked")
    public void removed(final ServiceReference ref, final Object service) {
        if (!ReflectionHelper.isNullableObject(service)) {
            switch (description.getMultiplicity()) {
            case MULTIPLE:
                // first get the existing value
                Collection collection = ReflectionHelper.getFieldValue(bean, field, Collection.class);
                // then remove the value
                collection.remove(service);
                break;
            case SINGLE:
                // directly inject
                ReflectionHelper.setFieldValue(bean, field, null);
                break;
            }
        }
    }

}
