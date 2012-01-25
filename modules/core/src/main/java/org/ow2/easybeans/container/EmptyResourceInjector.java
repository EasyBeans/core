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
 * $Id: EmptyResourceInjector.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;

import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.injection.ResourceInjector;

/**
 * Provides an empty {@link ResourceInjector} that do nothing.
 * This class aims to be sub classed.
 * @author Guillaume Sauthier
 */
public class EmptyResourceInjector implements ResourceInjector {

    /**
     * Called <b>before</b> EasyBeans resolution and injection of dependencies.
     * @param bean the Bean instance.
     */
    public void postEasyBeansDestroy(final EasyBeansBean bean) {
        // Do nothing
    }

    /**
     * Called <b>after</b> EasyBeans resolution and injection of dependencies.
     * @param bean the Bean instance.
     */
    public void postEasyBeansInject(final EasyBeansBean bean) {
        // Do nothing
    }

    /**
     * Called <b>after</b> @PreDestroy.
     * @param bean the Bean instance.
     */
    public void preEasyBeansInject(final EasyBeansBean bean) {
        // Do nothing
    }

}
