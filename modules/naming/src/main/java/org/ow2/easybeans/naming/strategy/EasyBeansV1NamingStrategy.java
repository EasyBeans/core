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
 * $Id: DefaultNamingStrategy.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.strategy;

import org.ow2.easybeans.api.bean.info.EZBBeanNamingInfo;
import org.ow2.easybeans.api.naming.EZBJNDINamingInfo;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;

/**
 * EasyBeans v1 naming strategy.
 * @author Florent Benoit
 */
public class EasyBeansV1NamingStrategy implements EZBNamingStrategy {

    /**
     * Gets the JNDI name for a given bean.
     * @param beanInfo Bean information.
     * @return JNDI name for this beanInfo.
     */
    public EZBJNDINamingInfo getJNDINaming(final EZBBeanNamingInfo beanInfo) {

        String jndiName;
        if (beanInfo.getMappedName() == null) {
            String mode = beanInfo.getMode();
            jndiName = beanInfo.getBeanClassName() + "_" + beanInfo.getInterfaceName();
            // Append @Local or @Remote
            if (mode != null && ("Local".equals(mode) || "Remote".equals(mode))) {
                jndiName += "@" + mode;
            }
        } else {
            jndiName = beanInfo.getMappedName();
        }

        JNDINamingInfo jndiNamingInfo = new JNDINamingInfo(jndiName);

        // No aliases
        return jndiNamingInfo;
    }

}
