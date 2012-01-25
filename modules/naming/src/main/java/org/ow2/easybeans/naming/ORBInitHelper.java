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
 * $Id: ORBInitHelper.java 5733 2011-02-21 12:54:34Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import org.omg.CORBA.ORB;

/**
 * This helper allows to get the current ORB of the system.
 * @author Florent Benoit
 */
public final class ORBInitHelper {

    /**
     * Utility class, no public constructor.
     */
    private ORBInitHelper() {
    }

    /**
     * Gets an ORB object.
     * @return the ORB.
     */
    public static ORB getORB() {
        return ORB.init();
    }

}
