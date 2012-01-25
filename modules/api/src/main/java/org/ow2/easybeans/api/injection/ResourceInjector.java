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
 * $Id: ResourceInjector.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.api.injection;

import org.ow2.easybeans.api.bean.EasyBeansBean;

/**
 * The <code>ResourceInjector</code> interface is a way for other systems
 * to hook inside EasyBeans at injection time.
 * This is needed, for example, when a WebServiceContext resource is required.
 * EasyBeans doesn't support such Resource type, so an external injector is needed.
 * @author Guillaume Sauthier
 */
public interface ResourceInjector {

    /**
     * Called <b>before</b> EasyBeans resolution and injection of dependencies.
     * @param bean the Bean instance.
     */
    void preEasyBeansInject(EasyBeansBean bean);

    /**
     * Called <b>after</b> EasyBeans resolution and injection of dependencies.
     * @param bean the Bean instance.
     */
    void postEasyBeansInject(EasyBeansBean bean);

    /**
     * Called <b>after</b> @PreDestroy.
     * @param bean the Bean instance.
     */
    void postEasyBeansDestroy(EasyBeansBean bean);
}
