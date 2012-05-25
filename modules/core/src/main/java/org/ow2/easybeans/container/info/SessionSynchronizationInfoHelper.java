/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: BusinessMethodsInfoHelper.java 5629 2010-10-12 15:50:41Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;

/**
 * Helper class used to retrieve session synchronization methods.
 * @author Florent Benoit
 */
public final class SessionSynchronizationInfoHelper {

    /**
     * Utility class.
     */
    private SessionSynchronizationInfoHelper() {

    }

    /**
     * Gets business methods of this metadata.
     * @param classMetadata the given bean class.
     * @return a list of methods
     */
    public static List<IMethodInfo> getMethods(final EasyBeansEjbJarClassMetadata classMetadata) {
        List<IMethodInfo> methodInfoList = new ArrayList<IMethodInfo>();
        for (EasyBeansEjbJarMethodMetadata methodMetadata : classMetadata.getMethodMetadataCollection()) {

            boolean toAdd = false;
            MethodInfo methodInfo = new MethodInfo(methodMetadata);


            if (methodMetadata.isAfterBegin()) {
                methodInfo.setAfterBegin();
                toAdd = true;
            }
            if (methodMetadata.isBeforeCompletion()) {
                methodInfo.setBeforeCompletion();
                toAdd = true;
            }
            if (methodMetadata.isAfterCompletion()) {
                methodInfo.setAfterCompletion();
                toAdd = true;
            }
            if (toAdd) {
                methodInfoList.add(methodInfo);
            }

        }
        return methodInfoList;
    }
}
