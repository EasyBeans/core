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
 * $Id: ExtensorSupport.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util;

import java.util.HashMap;
import java.util.Map;

import org.ow2.easybeans.api.EZBExtensor;

/**
 * This class aims to ease usage of {@link EZBExtensor} interface.
 * @author Guillaume Sauthier
 */
public class ExtensorSupport implements EZBExtensor {

    /**
     * Store the registered extensions.
     */
    private Map<Class<?>, Object> extensions = new HashMap<Class<?>, Object>();

    /**
     * @see org.ow2.easybeans.api.EZBExtensor#addExtension(java.lang.Class, java.lang.Object)
     */
    public <T> T addExtension(final Class<T> clazz, final T extension) {
        return clazz.cast(this.extensions.put(clazz, extension));
    }

    /**
     * @see org.ow2.easybeans.api.EZBExtensor#getExtension(java.lang.Class)
     */
    public <T> T getExtension(final Class<T> clazz) {
        return clazz.cast(this.extensions.get(clazz));
    }

    /**
     * @see org.ow2.easybeans.api.EZBExtensor#removeExtension(java.lang.Class)
     */
    public <T> T removeExtension(final Class<T> clazz) {
        return clazz.cast(this.extensions.remove(clazz));
    }

}
