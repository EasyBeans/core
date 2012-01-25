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
 * $Id: EJBModuleMBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import javax.management.MBeanException;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.container.JContainer3;
import org.ow2.easybeans.jmx.MBeansException;
import org.ow2.easybeans.jmx.MBeansHelper;

/**
 * EJBModule JSR77 MBean for {@link org.ow2.easybeans.container.JContainer3}.
 * @author Guillaume Sauthier
 */
public class EJBModuleMBean extends J2EEModuleMBean<JContainer3> {

    /**
     * Default ModelMBean constructor.
     * @throws MBeanException if ModelMBean creation fails.
     */
    public EJBModuleMBean() throws MBeanException {
        super();
    }

    /**
     * @return Returns the ObjectNames of the EJB deployed.
     */
    public String[] getEjbs() {

        int size = getManagedComponent().getFactories().size();
        String[] ejbs = new String[size];
        int index = 0;
        for(Factory<?, ?> f : getManagedComponent().getFactories()) {
            try {
                ejbs[index] = MBeansHelper.getInstance().getObjectName(f);
            } catch (MBeansException e) {
                ejbs[index] = "";
            }

            index++;
        }
        return ejbs;
    }


}
