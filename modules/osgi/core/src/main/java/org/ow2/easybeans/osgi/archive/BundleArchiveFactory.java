/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: BundleArchiveFactory.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.archive;

import org.osgi.framework.BundleContext;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.api.IArchiveFactory;

/**
 * EZBArchive extension supporting EjbJars provided in OSGi Bundles.
 * @author Guillaume Sauthier
 */
public class BundleArchiveFactory implements IArchiveFactory<BundleContext> {

    /**
     * @param bc The OSGi BundleContext associated with the Bundle.
     * @return Returns a {@link BundleArchive} instance.
     * @see org.ow2.util.archive.api.IArchiveFactory#create(java.lang.Object)
     */
    public IArchive create(final BundleContext bc) {
        return new BundleArchive(bc);
    }

    /**
     * @return the class supported by this factory
     */
    public Class<BundleContext> getSupportedClass() {
        return BundleContext.class;
    }

}
