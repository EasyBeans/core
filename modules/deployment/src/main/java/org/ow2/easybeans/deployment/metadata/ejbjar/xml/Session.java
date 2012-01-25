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
 * $Id: Session.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.List;
import java.util.ArrayList;

/**
 * Specific configuration for Session beans.
 * @author Florent BENOIT
 */
public class Session extends AbsSpecificBean {

    /**
     * Web service endpoint address.
     */
    private String endpointAddress = null;

    /**
     * Web services generated web context.
     */
    private String contextRoot;

    /**
     * name of the realm if the EJB is secured.
     */
    private String realmName;

    /**
     * Transport guarantee value (NONE, INTEGRAL, CONFIDENTIAL)
     */
    private String transportGuarantee;

    /**
     * Authentication method (NONE, BASIC, DIGEST, CLIENT-CERT)
     */
    private String authMethod;

    /**
     * List of http-methods.
     */
    private List<String> httpMethods = new ArrayList<String>();

    /**
     * @return the endpoint address for this bean (only applicable if bean if a webservice)
     */
    public String getEndpointAddress() {
        return this.endpointAddress;
    }

    /**
     * Set the endpoint address of this bean (do not include the context-root).
     * @param endpointAddress address of this webservice endpoint
     */
    public void setEndpointAddress(final String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(String transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<String> httpMethods) {
        this.httpMethods = httpMethods;
    }
}
