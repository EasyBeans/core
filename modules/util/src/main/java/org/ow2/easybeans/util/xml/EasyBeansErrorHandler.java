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
 * $Id: EasyBeansErrorHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.xml;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Define an error handler which throw an exception as Digester not (only print
 * stack trace). This handler is use for throwing/catching in a convenient way
 * the xml parsing error of xml file.
 * @author Florent Benoit
 */
public class EasyBeansErrorHandler implements ErrorHandler {

    /**
     * Receive notification of a warning.
     * @param exception exception to throw
     * @throws SAXException if an error is thrown
     */
    public void warning(final SAXParseException exception) throws SAXException {

    }

    /**
     * Receive notification of a recoverable error.
     * @param exception exception to throw
     * @throws SAXException if an error is thrown
     */
    public void error(final SAXParseException exception) throws SAXException {
        throw new SAXException("Parse Fatal Error at line " + exception.getLineNumber() + " column "
                + exception.getColumnNumber() + ": " + exception.getMessage());
    }

    /**
     * Receive notification of a non-recoverable error.
     * @param exception exception to throw
     * @throws SAXException if an error is thrown
     */
    public void fatalError(final SAXParseException exception) throws SAXException {
        throw new SAXException("Parse Fatal Error at line " + exception.getLineNumber() + " column "
                + exception.getColumnNumber() + ": " + exception.getMessage());
    }

}
