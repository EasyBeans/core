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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

/**
 * Defines Application Exception info.
 * We're not relying on javax.ejb.ApplicationException as they're different methods with EJB 3.0 and 3.1
 * @author Florent Benoit
 */
public interface IApplicationExceptionInfo {

    /**
     * Rollback the transaction before the throws ?
     * @return true if needs to rollback
     */
    boolean rollback();

    /**
     * Indicates whether the application exception designation should apply to
     * subclasses of the annotated exception class.
     * @return true if inherited exceptions are also application exception
     * @since EJB 3.1 version.
     */
    boolean inherited();

}
