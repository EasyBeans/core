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
 * $Id: MailAddress.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail;

/**
 * Define a mail address.
 * @author Florent BENOIT
 */
public class MailAddress {

    /**
     * Email address value.
     */
    private String name = null;

    /**
     * Type of the mail (TO, CC, BCC).
     */
    private String type = null;

    /**
     * @return the type of the mail address.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of this mail address.
     * @param type the type of this mail address.
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return the name of this mail address.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this mail address.
     * @param name the given name
     */
    public void setName(final String name) {
        this.name = name;
    }
}
