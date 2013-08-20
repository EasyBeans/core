/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: AccessChecker.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.checks;

import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.util.scan.api.metadata.structures.IMethod;

/**
 * Utility class for cheking access.
 * @author Florent Benoit
 */
public final class AccessChecker {

    /**
     * Utility class, no public constructor.
     */
    private AccessChecker() {

    }

    /**
     * Validate that a given method don't use a given access mode.
     * @param acc the access mode to refuse.
     * @param jMethod method to check.
     * @param desc the description of the access.
     * @param className the name of the class of the given method.
     */
    public static void ensureNoAccess(final int acc, final IMethod jMethod, final String desc, final String className) {
        if ((jMethod.getAccess() & acc) == acc) {
            throw new InterceptorsValidationException("The method '" + jMethod + "' of the class '" + className
                    + "' is not compliant on the method access. It shouldn't use the '" + desc + "' keyword.");
        }
    }
}
