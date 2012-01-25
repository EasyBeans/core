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
 * $Id: BCMapper.java 3054 2008-04-30 15:41:13Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.util;

import java.net.URL;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;

/**
 * Singleton instance used to map a bundle URL to it's BundleContext.
 * Used by patched version of JBoss ArchiveBrowser.
 * @author Guillaume Sauthier
 */
public final class BCMapper extends Hashtable<URL, BundleContext> {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1233926375070799010L;

    /**
     * Singleton instance.
     */
    private static BCMapper instance = null;

    /**
     * @return Returns the BCMapper Singleton instance.
     */
    public static BCMapper getInstance() {
        if (instance == null) {
            instance = new BCMapper();
        }
        return instance;
    }

    /**
     * Empty private constructor.
     */
    private BCMapper() { }
}
