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
 * $Id: EZBBeanNamingInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

/**
 * Naming information for a bean.
 * @author S. Ali Tokmen
 */
public interface EZBBeanNamingInfo {

    /**
     * @return the name of the bean.
     */
    String getName();

    /**
     * @return Java EE application name (if bean is in an EAR).
     */
    String getJavaEEApplicationName();

    /**
     * @return Name of the bean class.
     */
    String getBeanClassName();

    /**
     * @return Name of the interface.
     */
    String getInterfaceName();

    /**
     * @return Local, Remote or null.
     */
    String getMode();

    /**
     * @return The MappedName attribute, may be null.
     */
    String getMappedName();
}
