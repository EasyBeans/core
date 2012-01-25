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
 * $Id: JNDIData.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver;

import java.io.Serializable;

import org.ow2.easybeans.resolver.api.EZBJNDIData;

/**
 * Data about JNDI Name entry.
 * @author Florent Benoit
 */
public class JNDIData implements EZBJNDIData, Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 3880747959480562539L;

    /**
     * JNDI Name.
     */
    private String name;


    /**
     * Constructor.
     * @param name the JNDI name
     */
    public JNDIData(final String name) {
        this.name = name;
    }

    /**
     * @return the JNDI Name.
     */
    public String getName() {
        return this.name;
    }



    /**
     * @return string representation of the class.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(JNDIData.class.getSimpleName());
        sb.append("[name=");
        sb.append(this.name);
        sb.append("]");
        return sb.toString();
    }

}
