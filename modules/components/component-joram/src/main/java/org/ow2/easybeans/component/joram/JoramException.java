/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id: JoramException.java 5372 2010-02-25 13:36:26Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.joram;

/**
 * Class used when there is an exception in joram service.
 * @author Florent Benoit
 */
public class JoramException extends Exception {

    /**
     * Id for serializable class.
     */
    private static final long serialVersionUID = -1461838516761300498L;

    /**
     * Constructs a new runtime exception with <code>null</code> as its detail
     * message. The cause is not initialized, and may subsequently be
     * initialized by a call to {@link #initCause}.
     */
    public JoramException() {
        super();
    }

    /**
     * Constructs a new runtime exception with the specified detail message. The
     * cause is not initialized, and may subsequently be initialized by a call
     * to {@link #initCause}.
     * @param message the detail message. The detail message is saved for later
     *        retrieval by the {@link #getMessage()} method.
     */
    public JoramException(final String message) {
        super(message);
    }

    /**
     * Constructs a new runtime exception with the specified detail message and
     * cause.
     * <p>
     * Note that the detail message associated with <code>cause</code> is
     * <i>not</i> automatically incorporated in this runtime exception's detail
     * message.
     * @param message the detail message (which is saved for later retrieval by
     *        the {@link #getMessage()} method).
     * @param cause the cause (which is saved for later retrieval by the
     *        {@link #getCause()} method). (A <tt>null</tt> value is
     *        permitted, and indicates that the cause is nonexistent or
     *        unknown.)
     */
    public JoramException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
