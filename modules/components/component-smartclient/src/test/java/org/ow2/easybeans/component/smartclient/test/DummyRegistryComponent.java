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
 * $Id: DummyRegistryComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.test;

import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.RegistryComponent;

/**
 * Allow to use this component in the tests of the smart factory.
 * @author Florent BENOIT
 *
 */
public class DummyRegistryComponent implements RegistryComponent {

    /**
     * Default provider URL.
     */
    private String providerURL = null;

    /**
     * Sets the default Provider URL.
     * @param providerURL the provider URL that is used by default.
     */
    public void setProviderURL(final String providerURL) {
        this.providerURL = providerURL;
    }

    /**
     * Gets the default Provider URL.
     * @return the provider URL that is used by default.
     */
    public String getProviderURL() {
        return this.providerURL;
    }


    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

    }


    /**
     * Start method.<br/>
     * This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

    }


    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {

    }
}
