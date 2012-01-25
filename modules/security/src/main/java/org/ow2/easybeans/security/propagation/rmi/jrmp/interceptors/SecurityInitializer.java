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
 * $Id: SecurityInitializer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.propagation.rmi.jrmp.interceptors;

import org.objectweb.carol.rmi.jrmp.interceptor.JDuplicateName;
import org.objectweb.carol.rmi.jrmp.interceptor.JInitializer;
import org.objectweb.carol.rmi.jrmp.interceptor.JInitInfo;

/**
 * Initialize interceptors on client and server.
 * @author Florent Benoit.
 */
public class SecurityInitializer implements JInitializer {

    /**
     * In JRMP the 2 method( per and post init have the same consequences ...
     * @param info the JInit Information
     */
    public void pre_init(final JInitInfo info) {
        try {
            info.add_client_request_interceptor(new ClientSecurityInterceptor());
        } catch (JDuplicateName e) {
           throw new IllegalStateException("Cannot add the client interceptor for EasyBeans security", e);
        }
        try {
            info.add_server_request_interceptor(new ServerSecurityInterceptor());
        } catch (JDuplicateName e) {
            throw new IllegalStateException("Cannot add the server interceptor for EasyBeans security", e);
        }

    }

    /**
     * Nothing to do in post method.
     * @param info the JInit Information
     */
    public void post_init(final JInitInfo info) {
        // nothing
    }

}
