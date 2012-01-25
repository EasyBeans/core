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
 * $Id: JWSWebMethodFinder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.session;

import java.util.Iterator;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxJwsWebMethod;

/**
 * This class finds the WebMethod and flag them as business methods.
 * @author Florent Benoit
 */
public final class JWSWebMethodFinder {

    /**
     * Helper class, no public constructor.
     */
    private JWSWebMethodFinder() {
    }

    /**
     * Finds &#64;WebMethod methods and set it as business method.
     * @param sessionBean Session bean to analyze
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata sessionBean) {

        Iterator<EasyBeansEjbJarMethodMetadata> itMethods = sessionBean.getMethodMetadataCollection().iterator();
        while (itMethods.hasNext()) {
            EasyBeansEjbJarMethodMetadata method = itMethods.next();
            IJavaxJwsWebMethod webMethod = method.getJavaxJwsWebMethod();
            // Annotation present and not excluded
            if (webMethod != null && !webMethod.getExclude()) {
                method.setBusinessMethod(true);
            }
        }
    }
}
