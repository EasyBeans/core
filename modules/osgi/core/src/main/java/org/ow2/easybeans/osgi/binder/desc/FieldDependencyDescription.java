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
 * $Id: FieldDependencyDescription.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.desc;

import java.lang.reflect.Field;

import org.ow2.easybeans.osgi.binder.listener.FieldInjectorListener;
import org.ow2.easybeans.osgi.binder.listener.IDependencyListener;

/**
 * {@link DependencyDescription} on a {@link Field}.
 * @author Guillaume Sauthier
 */
public class FieldDependencyDescription extends DependencyDescription {

    /**
     * field to be injected.
     */
    private final Field field;

    /**
     * @param name name of the dependency
     * @param field field to be injected
     */
    public FieldDependencyDescription(final String name, final Field field) {
        super(name);
        this.field = field;
    }

    /**
     * @return the field to set.
     */
    public Field getField() {
        return field;
    }

    @Override
    public IDependencyListener createListener(final Object instance) {
        return new FieldInjectorListener(instance, field, this);
    }

}
