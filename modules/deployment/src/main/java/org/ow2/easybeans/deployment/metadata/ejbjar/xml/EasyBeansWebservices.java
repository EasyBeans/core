 /**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EasyBeansWebservices.java 4703 2009-02-25 10:04:21Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.ArrayList;
import java.util.List;

/**
 * The EasyBeansWebservices reopresents the webservices part of the
 * specific EZB deployment descriptor.
 *
 * @author Guillaume Sauthier
 */
public class EasyBeansWebservices {

    /**
     * Webservices Endpoint informations.
     */
    private List<WebserviceEndpoint> webserviceEndpoints;

    public EasyBeansWebservices() {
        this.webserviceEndpoints = new ArrayList<WebserviceEndpoint>();
    }

    public List<WebserviceEndpoint> getWebserviceEndpoints() {
        return this.webserviceEndpoints;
    }

    public void setWebserviceEndpoints(final List<WebserviceEndpoint> webserviceEndpoints) {
        this.webserviceEndpoints = webserviceEndpoints;
    }
}
