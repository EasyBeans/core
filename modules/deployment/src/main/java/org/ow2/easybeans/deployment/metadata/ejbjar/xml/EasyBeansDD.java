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
 * $Id: EasyBeansDD.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.List;


/**
 * This class is describing the specific deployment descriptor of EasyBeans used in modules.
 * @author Florent BENOIT
 */
public class EasyBeansDD {

    /**
     * EJB part of this specific deployment descriptor.
     */
    private EJB ejb = null;

    /**
     * Webservices part of this specific DD.
     */
    private EasyBeansWebservices webservices = null;

    /**
     * Security role mapping part of this specific deployment descriptor.
     */
    private List<SecurityRoleMapping> securityRoleMappings = null;

    /**
     * Gets the specific part of EJBs.
     * @return the security role mapping part of EJBs.
     */
    public EJB getEJB() {
        return this.ejb;
    }

    /**
     * Sets the specific part on EJBs.
     * @param ejb the given EJB object.
     */
    public void setEJB(final EJB ejb) {
        this.ejb = ejb;
    }

    /**
     * Gets the WS specific part of EJBs.
     * @return the WS specific part of EJBs.
     */
    public EasyBeansWebservices getWebservices() {
        return this.webservices;
    }

    /**
     * Sets the WS specific part of EJBs.
     * @param webservices the given EasyBeansWebservices object.
     */
    public void setEasyBeansWebservices(final EasyBeansWebservices webservices) {
        this.webservices = webservices;
    }

    /**
     * Gets the security role mapping part.
     * @return the security role mapping part
     */
    public List<SecurityRoleMapping> getSecurityRoleMappings() {
        return this.securityRoleMappings;
    }

    /**
     * Sets the security role mapping part.
     * @param securityRoleMappings the given security role mapping object.
     */
    public void setSecurityRoleMappings(final List<SecurityRoleMapping> securityRoleMappings) {
        this.securityRoleMappings = securityRoleMappings;
    }

}
