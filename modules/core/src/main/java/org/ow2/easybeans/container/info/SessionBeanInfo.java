/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: SessionBeanInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;


import org.ow2.easybeans.api.bean.info.ISessionBeanInfo;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJEjbStatefulTimeout;

/**
 * This class contains information for a session bean.
 * It is used at the runtime.
 * @author Florent Benoit
 */
public class SessionBeanInfo extends BeanInfo implements ISessionBeanInfo {

    private IJEjbStatefulTimeout statefulTimeout;

    /**
     * Default constructor.
     */
    public SessionBeanInfo(final IJEjbStatefulTimeout statefulTimeout) {
        super();
        this.statefulTimeout = statefulTimeout;
    }


    public IJEjbStatefulTimeout getStatefulTimeout() {
        return statefulTimeout;
    }
}
