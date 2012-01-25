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
 * $Id: URLUtilsException.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.url;

/**
 * The class URKUtilsException indicates conditions that a reasonable
 * application might want to catch.
 * @author Florent Benoit
 */
public class URLUtilsException extends Exception {

    /**
     * Uid of serializable class.
     */
    private static final long serialVersionUID = -6516098191414053742L;

    /**
     * Constructs a new FileUtilsException with no detail message.
     */
    public URLUtilsException() {
        super();
    }

    /**
     * Constructs a new FileUtilsException with the specified message.
     * @param message the detail message.
     */
    public URLUtilsException(final String message) {
        super(message);
    }

    /**
     * Constructs a new FileUtilsException with the specified message and
     * throwable.
     * @param message the detail message.
     * @param t the original cause
     */
    public URLUtilsException(final String message, final Throwable t) {
        super(message, t);
    }
}
