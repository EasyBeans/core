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
 * $Id: JPolicyException.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.jacc.provider;

/**
 * The JPolicyException occurs if the JPolicy object can not be created.
 * @author Florent Benoit
 */
public class JPolicyException extends Exception {

    /**
     * Serial version uid (serialization).
     */
    private static final long serialVersionUID = 8379966277572408042L;

    /**
     * Constructs a new JPolicyException with the specified message.
     * @param message the detail message.
     */
    public JPolicyException(final String message) {
        super(message);
    }

    /**
     * Constructs a new JPolicyException with the specified message.
     * @param message the detail message.
     * @param throwable the exception
     */
    public JPolicyException(final String message, final Throwable throwable) {
        super(message, throwable);
    }
}
