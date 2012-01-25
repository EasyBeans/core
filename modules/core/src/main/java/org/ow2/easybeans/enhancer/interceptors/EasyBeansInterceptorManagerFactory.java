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
 * $Id: EasyBeansInterceptorManagerFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.util.List;

import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManagerFactory;

/**
 * Allow to build interceptor manager factories.
 * @author Florent Benoit
 */
public class EasyBeansInterceptorManagerFactory implements EZBInterceptorManagerFactory {

    /**
     * List of the interceptor classes.
     */
    private List<String> classnameList = null;

    /**
     * ClassLoader used to load classes.
     */
    private ClassLoader classLoader = null;

    /**
     * Build a new factory with the name of the classes.
     * @param classnameList list of classes
     * @param classLoader the classloader used in order to load the classes
     */
    public EasyBeansInterceptorManagerFactory(final List<String> classnameList, final ClassLoader classLoader) {
        this.classnameList = classnameList;
        this.classLoader = classLoader;
    }

    /**
     * Build an instance of an interceptor manager.
     * @return instance of the interceptor manager that is handling instances of interceptors
     */
    public EZBInterceptorManager getInterceptorManager() {
        return new EasyBeansInterceptorManager(this.classnameList, this.classLoader);
    }

}
