/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: ClassUtils.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.loader;

/**
 * The <code>ClassUtils</code> class is the central point used to load classes.
 * @author Guillaume Sauthier
 */
public final class ClassUtils {

    /**
     * Default constructor.
     */
    private ClassUtils() {}

    /**
     * Look up the class in the Tread Context ClassLoader and in the "current" ClassLoader.
     * @param className The class name to load
     * @return the corresponding Class instance
     * @throws ClassNotFoundException if the Class was not found.
     */
    public static Class forName(final String className) throws ClassNotFoundException {
        // Load classes from different classloaders :
        // 1. Thread Context ClassLoader
        // 2. ClassUtils ClassLoader

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Class cls = null;

        try {
            // Try with TCCL
            cls = Class.forName(className, true, tccl);
        } catch (ClassNotFoundException cnfe) {

            // Try now with the classloader used to load ClassUtils
            ClassLoader current = ClassUtils.class.getClassLoader();
            try {
                cls = Class.forName(className, true, current);
            } catch (ClassNotFoundException cnfe2) {
                // If this is still unknown, throw an Exception
                throw cnfe2;
            }
        }

        return cls;
    }

    /**
     * Look up the class in the Tread Context ClassLoader and in the "current" ClassLoader.
     * @param className The class name to load
     * @param clazz a class used to get classloader
     * @return the corresponding Class instance
     * @throws ClassNotFoundException if the Class was not found.
     */
    public static Class forName(final String className, final Class clazz) throws ClassNotFoundException {
        // Load classes from different classloaders :
        // 1. Thread Context ClassLoader
        // 2. ClassUtils ClassLoader

        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        Class cls = null;

        try {
            // Try with TCCL
            cls = Class.forName(className, true, tccl);
        } catch (ClassNotFoundException cnfe) {

            // Try now with the classloader used to load ClassUtils
            ClassLoader current = clazz.getClassLoader();
            if (current != null) {
                try {
                    cls = Class.forName(className, true, current);
                } catch (ClassNotFoundException cnfe2) {
                    // If this is still unknown, throw an Exception
                    throw new ClassNotFoundException("Class Not found in current ThreadClassLoader '" + tccl + "' and in '" + current + "' classloaders.", cnfe2);
                }
            } else {
                // rethrow exception
                throw cnfe;
            }
        }

        return cls;
    }


}
