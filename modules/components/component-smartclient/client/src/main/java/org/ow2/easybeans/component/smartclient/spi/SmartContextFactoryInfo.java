/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: SmartContextFactoryInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.spi;

import org.ow2.easybeans.component.smartclient.client.AskingClassLoader;

/**
 * Infos associated to a Smart Provider URL.
 * @author Florent BENOIT
 */
public class SmartContextFactoryInfo {

    /**
     * Instance of classloader that is used to load class.<br>
     * This is this classloader which will download class.
     */
    private AskingClassLoader classLoader = null;

    /**
     * Provider URL to use for connecting.
     */
    private String providerURL = null;

    /**
     * @return the classloader used by this factory.
     */
    public AskingClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * Sets the given classloader.
     * @param classLoader the classloader to use
     */
    public void setClassLoader(final AskingClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * @return the Context PROVIDER_URL
     */
    public String getProviderURL() {
        return providerURL;
    }

    /**
     * Sets the given PROVIDER_URL.
     * @param providerURL the given provider URL
     */
    public void setProviderURL(final String providerURL) {
        this.providerURL = providerURL;
    }

}
