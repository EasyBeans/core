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
 * $Id: Registrable.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.handler;

import org.osgi.framework.ServiceRegistration;
import org.osgi.framework.BundleContext;

/**
 * This interface has to be implemented by classes with instances that needs to
 * be registered as OSGi services.
 * @author Guillaume Sauthier
 */
public interface Registrable {

    /**
     * Register the proxy as an OSGi service.
     * @param proxy the object to be registered
     * @param context OSGi BundlContext
     */
    ServiceRegistration registerService(Object proxy, BundleContext context);
}
