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
 * $Id: SessionBeanHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean;

import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.annotations.helper.bean.session.JWSWebMethodFinder;
import org.ow2.easybeans.deployment.annotations.helper.bean.session.LocalBeanBusinessMethodHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.session.SessionBeanInterface;
import org.ow2.easybeans.deployment.annotations.helper.bean.session.SessionBusinessInterfaceFinder;
import org.ow2.easybeans.deployment.annotations.helper.bean.session.checks.SessionBeanValidator;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;

/**
 * Helper class which manages only Session bean class.
 * @author Florent Benoit
 */
public final class SessionBeanHelper {

    /**
     * Validation.
     */
    private static boolean validating = true;

    /**
     * Helper class, no public constructor.
     */
    private SessionBeanHelper() {
    }

    /**
     * Apply all helper.
     * @param sessionBean Session bean to analyze
     * @throws ResolverException if there is a failure in a resolver
     */
    public static void resolve(final EasyBeansEjbJarClassMetadata sessionBean) throws ResolverException {
        // call helpers

        // Search session bean that implements javax.ejb.SessionBean and add metadata on it
        SessionBeanInterface.resolve(sessionBean);

        // WebMethods methods should be business methods
        JWSWebMethodFinder.resolve(sessionBean);

        // Find annotated interfaces
        SessionBusinessInterfaceFinder.resolve(sessionBean);

        // Local Bean ?
        if (sessionBean.isLocalBean()) {
            LocalBeanBusinessMethodHelper.resolve(sessionBean);
        }

        if (validating) {
            SessionBeanValidator.validate(sessionBean);
        }
    }
}
