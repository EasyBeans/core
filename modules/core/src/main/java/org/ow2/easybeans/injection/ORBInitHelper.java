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
 * $Id: ORBInitHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.injection;

import org.omg.CORBA.INITIALIZE;
import org.omg.CORBA.ORB;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This helper allows to get the current ORB of the system.
 * @author Florent Benoit
 */
public final class ORBInitHelper {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ORBInitHelper.class);

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
        try {
            return ORB.init();
        } catch (INITIALIZE e) {
            logger.debug("Unable to get CORBA ORB",  e);
            // No corba available
            return null;
        }
    }

}
