/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EJBJarInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IEJBJarInfo;

/**
 * Manages beans of an ejbjar.
 * @author Florent Benoit
 */
public class EJBJarInfo implements IEJBJarInfo {

    /**
     * List of beans (info).
     */
    private List<IBeanInfo> beans = null;

    /**
     * Default constructor.
     */
    public EJBJarInfo() {
        this.beans = new ArrayList<IBeanInfo>();
    }

    /**
     * Adds info about a bean.
     * @param beanInfo the info of a bean.
     */
    public void addBeanInfo(final IBeanInfo beanInfo) {
        beans.add(beanInfo);
    }


    /**
     * Gets the beans info.
     * @return info on all beans of an ejb-jar file.
     */
    public List<IBeanInfo> getBeanInfos() {
        return beans;
    }

}
