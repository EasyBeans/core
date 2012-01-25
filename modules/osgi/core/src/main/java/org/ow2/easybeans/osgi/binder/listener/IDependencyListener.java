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
 * $Id: IDependencyListener.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder.listener;

import org.osgi.framework.ServiceReference;

public interface IDependencyListener {

    final String ADDED = "added";
    final String CHANGED = "changed";
    final String REMOVED = "removed";

    void added(ServiceReference ref, Object service);
    void changed(ServiceReference ref, Object service);
    void removed(ServiceReference ref, Object service);

}
