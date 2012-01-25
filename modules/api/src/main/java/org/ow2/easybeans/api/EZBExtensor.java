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
 * $Id: EZBExtensor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.api;

/**
 * The {@link EZBExtensor} interface aims to provide a
 * way to add objects to instances, thus providing an extension mechanism.
 * @author Guillaume Sauthier
 */
public interface EZBExtensor {

    /**
     * Add a new extension under a given {@link Class} key.
     * Previously registed extension (if there was one) is
     * replaced by the new one. The old extension is returned.
     * @param <T> Extension type.
     * @param clazz Extenstion type
     * @param extension the extension to add
     */
    <T> T addExtension(Class<T> clazz, T extension);

    /**
     * Remove the registered extension, if one was previously
     * registered. After a call to that method, the
     * getExtension will return null for the removed type.
     * @param <T> Extension type
     * @param clazz Extension type to be removed (this is the key)
     * @return the removed Extension.
     */
    <T> T removeExtension(Class<T> clazz);

    /**
     * Return an extension implementing the given class/interface.
     * May return null if no extension was registered under the
     * given class name.
     * @param <T> Extension type
     * @param clazz The key used to retrieve the extension.
     * @return The found extension, or null if none was found.
     */
    <T> T getExtension(Class<T> clazz);
}
