/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

/**
 * Allows to create a proxified name for the class.
 * @author Florent Benoit
 */
public final class ProxyClassEncoder {

    /**
     * Proxified package name.
     */
    private static final String PREFIX_CLASSNAME = "EasyBeansProxy";

    /**
     * Package name.
     */
    private static final String PACKAGE_NAME = "org.ow2.easybeans.gen.proxy";

    /**
     * Static class.
     */
    private ProxyClassEncoder() {

    }

    /**
     * Get a proxy class name for the given class.
     * @param fullClassName the name of the class
     * @return the name of the proxy class
     */
    public static String getProxyClassName(final String fullClassName) {
        // Extract package and class name
        int slashPos = fullClassName.lastIndexOf("/");
        String packageName = fullClassName.substring(0, slashPos);
        String className = fullClassName.substring(slashPos + 1);

        return PACKAGE_NAME.concat(".").concat(packageName).concat(".").concat(PREFIX_CLASSNAME).concat(className).replace(".",
                "/");

    }
}
