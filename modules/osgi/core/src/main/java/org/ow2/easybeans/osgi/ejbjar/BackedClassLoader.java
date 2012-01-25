/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: BackedClassLoader.java 5487 2010-04-30 14:51:15Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.ejbjar;

import java.net.URL;

import org.osgi.framework.BundleContext;
import org.ow2.easybeans.loader.EasyBeansClassLoader;

/**
 * A Class loader that is backed by a {@link BundleContext} taht is to be used by EJBJar's. This classloader first tryes
 * to find the classes in the parent classloader and if it can't it then tries to find them in them through the bundle.
 *
 * @author David Alves
 * @version $Revision$
 */
public class BackedClassLoader extends EasyBeansClassLoader implements Cloneable {

    private final BundleContext bc;

    private final ClassLoader coreClassLoader;

    private final URL[] urls;

    // private static Log logger = LogFactory.getLog(BackedClassLoader.class);

    public BackedClassLoader(URL[] urls, ClassLoader coreClassLoader, BundleContext bc) {
        super(urls, coreClassLoader);
        this.coreClassLoader = coreClassLoader;
        this.urls = urls;
        // logger.info("Initiated BackedClassLoader for bundle {0}", bc.getBundle().getSymbolicName());
        this.bc = bc;
        setAlwaysTransform(true);
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#getResource(java.lang.String)
     */
    @Override
    public URL getResource(String name) {
        URL resource = null;
        boolean caughtException = false;
        try {
            // logger.info("Trying to lookup resource: {0} in Core ClassLoader", name);
            resource = super.getResource(name);
        } catch (RuntimeException e) {
            caughtException = true;
        }
        if (resource == null || caughtException) {
            // logger.info("Trying to lookup resource: {0} in Bundle's: {1} ClassLoader", name, bc.getBundle()
            // .getSymbolicName());
            resource = bc.getBundle().getResource(name);
        }
        return resource;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.ClassLoader#loadClass(java.lang.String)
     */
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        boolean caughtException = false;
        Class<?> clazz = null;
        try {
            // logger.info("Trying to lookup Class: {0} in Core ClassLoader", name);
            return super.loadClass(name);
        } catch (Exception e) {
            caughtException = true;
        }
        if (clazz == null || caughtException) {
            // logger.info("Trying to lookup resource: {0} in Bundle's: {1} ClassLoader", name, bc.getBundle()
            // .getSymbolicName());
            clazz = bc.getBundle().loadClass(name);
        }
        return clazz;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        return new BackedClassLoader(urls, coreClassLoader, bc);
    }

}
