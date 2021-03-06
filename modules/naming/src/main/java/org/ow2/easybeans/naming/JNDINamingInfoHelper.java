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
 * $Id: BeanNamingInfoHelper.java 5733 2011-02-21 12:54:34Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.bean.info.EZBBeanNamingInfo;
import org.ow2.easybeans.api.naming.EZBJNDINamingInfo;
import org.ow2.easybeans.api.naming.EZBNamingStrategy;

/**
 * Helper class that build a JNDINamingInfo object.
 * @author Florent Benoit
 */
public final class JNDINamingInfoHelper {

    /**
     * Utility class.
     */
    private JNDINamingInfoHelper() {

    }

    /**
     * Compute list of naming infos for each strategy.
     * @param strategies the list of strategies
     * @param namingInfo the naming info
     * @return list of naming infos
     */
    public static List<EZBJNDINamingInfo> buildInfo(final List<EZBNamingStrategy> strategies,
            final EZBBeanNamingInfo namingInfo) {
        List<EZBJNDINamingInfo> jndiNamingInfos = new ArrayList<EZBJNDINamingInfo>();
        for (EZBNamingStrategy strategy : strategies) {
            jndiNamingInfos.add(strategy.getJNDINaming(namingInfo));
        }
        return jndiNamingInfos;

    }
}
