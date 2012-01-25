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
 * $Id: MessageDrivenBean.java 5491 2010-05-06 09:52:29Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

/**
 * Specific configuration for Message Driven beans.
 * @author Florent BENOIT
 */
public class MessageDrivenBean extends AbsSpecificBean {

    /**
     * Activation Spec Name.
     */
    private String activationSpec = null;

    /**
     * @return the activation spec name.
     */
    public String getActivationSpec() {
        return this.activationSpec;
    }

    /**
     * Sets the activation spec name.
     * @param activationSpec the given name
     */
    public void setActivationSpec(final String activationSpec) {
        this.activationSpec = activationSpec;
    }

}
