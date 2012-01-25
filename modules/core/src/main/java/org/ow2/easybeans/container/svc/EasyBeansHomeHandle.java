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
 * $Id: EasyBeansHomeHandle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.svc;

import javax.ejb.EJBHome;
import javax.ejb.HomeHandle;

/**
 * This class is used by the EJB 2.1 clients in order to get the Home Handle to
 * the EJB.
 * @author Florent Benoit
 */
public class EasyBeansHomeHandle implements HomeHandle {

    /**
     * UID used for the serialization.
     */
    private static final long serialVersionUID = 4616866467275141589L;

    /**
     * Build a HomeHandle based on the given ejbHome. The home is serializable
     * (proxy) so it can be stored directly.
     * @param ejbHome the ejb home proxy.
     */
    public EasyBeansHomeHandle(final EJBHome ejbHome) {
        this.ejbHome = ejbHome;
    }

    /**
     * Proxy object acting as the EJBHome.
     */
    private transient EJBHome ejbHome = null;

    /**
     * Obtain the home object represented by this handle.
     * @return the home object represented by this handle.
     */
    public EJBHome getEJBHome() {
        return this.ejbHome;
    }

}
