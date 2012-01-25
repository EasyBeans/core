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
 * $Id: NamingInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.naming;

import javax.naming.Context;

import org.ow2.easybeans.api.EasyBeansInterceptor;

/**
 * The naming interceptors need to implements this interface.
 * @author Florent Benoit
 */
public interface NamingInterceptor extends EasyBeansInterceptor {


    /**
     * Init the context for the given name.
     * @param id the id.
     * @param context the context associated to a factory's id.
     */
    void initContext(final String id, final Context context);

    /**
     * Remove the context associated to a given id.
     * @param id the id.
     */
    void removeContext(final String id);

}
