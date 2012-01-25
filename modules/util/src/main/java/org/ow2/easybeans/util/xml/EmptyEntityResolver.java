/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: EmptyEntityResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.xml;

import java.io.IOException;
import java.io.StringReader;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is used when no validating is required. This avoids timeout when schema are looking up on internet.<br/>
 * It returns an empty content for every schema/DTD.
 * @author Florent Benoit
 *
 */
public class EmptyEntityResolver implements EntityResolver {

    /**
     * The Parser will call this method before opening any external entity
     * except the top-level document entity.
     * @param publicId The public identifier of the external entity being
     *        referenced, or null if none was supplied.
     * @param systemId The system identifier of the external entity being
     *        referenced.
     * @return An InputSource object describing the new input source, or null to
     *         request that the parser open a regular URI connection to the
     *         system identifier.
     * @throws SAXException Any SAX exception, possibly wrapping another
     *         exception.
     * @throws IOException A Java-specific IO exception, possibly the result of
     *         creating a new InputStream or Reader for the InputSource.
     */
    public InputSource resolveEntity(final String publicId, final String systemId) throws SAXException, IOException {
        return new InputSource(new StringReader(""));
    }

}
