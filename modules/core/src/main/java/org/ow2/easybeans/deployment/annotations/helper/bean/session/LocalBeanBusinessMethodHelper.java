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
 * $Id: SessionBusinessInterfaceFinder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.session;

import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Apply business method = true on all public methods.
 * @author Florent Benoit
 */
public final class LocalBeanBusinessMethodHelper {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(LocalBeanBusinessMethodHelper.class);

    /**
     * Helper class, no public constructor.
     */
    private LocalBeanBusinessMethodHelper() {
    }


    /**
     * Found all business methods of a bean.<br>
     * A business method is a method from one of the local or remote interfaces.
     * @param classAnnotationMetadata class to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        for (EasyBeansEjbJarMethodMetadata methodData : classAnnotationMetadata.getMethodMetadataCollection()) {
            if ((Opcodes.ACC_PUBLIC & methodData.getJMethod().getAccess()) == Opcodes.ACC_PUBLIC) {
                // Do not intercept lifecycle methods
                if (methodData.isLifeCycleMethod()) {
                    continue;
                }
                if (!"<init>".equals(methodData.getMethodName())) {
                    methodData.setBusinessMethod(true);
                }
            }
        }
    }

}
