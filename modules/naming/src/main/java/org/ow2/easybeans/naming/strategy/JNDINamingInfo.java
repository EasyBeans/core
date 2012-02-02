/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.strategy;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.naming.EZBJNDINamingInfo;

/**
 * Defines a JNDI information for a given EJB3 proxy.
 * @author Florent Benoit
 */
public class JNDINamingInfo implements EZBJNDINamingInfo {

    /**
     * JNDI name.
     */
    private String jndiName = null;

    private List<String> aliases = null;

    public JNDINamingInfo(final String jndiName) {
        this.jndiName = jndiName;
        this.aliases = new ArrayList<String>();
    }

    public List<String> aliases() {
        return this.aliases;
    }

    public void setAliases(final List<String> aliases) {
        this.aliases = aliases;
    }

    /**
     * @return the JNDI name.
     */
    public String jndiName() {
        return this.jndiName;
    }

    @Override
    public String toString() {
        return JNDINamingInfo.class.getSimpleName().concat("[jndiName=").concat(this.jndiName).concat(", aliases=").concat(
                this.aliases.toString()).concat("]");
    }



}
