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
 * $Id: JNDIBeanData.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver;

import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;

/**
 * Data about Bean JNDI Name entry.
 * @author Florent Benoit
 */
public class JNDIBeanData extends JNDIData implements EZBJNDIBeanData {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6037590776547042416L;
    /**
     * Bean Name.
     */
    private String beanName;

    /**
     * Constructor.
     * @param name the JNDI name
     * @param beanName the name of the bean
     */
    public JNDIBeanData(final String name, final String beanName) {
        super(name);
        this.beanName = beanName;
    }

    /**
     * @return the bean name associated to this JNDI Name.
     */
    public String getBeanName() {
        return this.beanName;
    }

    /**
     * @return string representation of the class.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(JNDIData.class.getSimpleName());
        sb.append("[name=");
        sb.append(getName());
        sb.append(", beanName=");
        sb.append(this.beanName);
        sb.append("]");
        return sb.toString();
    }
}
