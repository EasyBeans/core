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
 * $Id: SchemaEntityResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.xml;


import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Entity resolver allowing to find schema within the classloader.
 * @author Florent Benoit
 */
public class SchemaEntityResolver implements EntityResolver {

    /**
     * Map where the schemas URLs are stored.
     */
    private Map<String, String> schemasUrls = null;

    /**
     * Constructor. Finds the XSD with classloader.
     * @param schemas the name of the schemas to resolve.
     */
    public SchemaEntityResolver(final String[] schemas) {
        schemasUrls = new HashMap<String, String>();
        URL url = null;
        for (int i = 0; i < schemas.length; i++) {
            url = SchemaEntityResolver.class.getResource("/" + schemas[i]);
            if (url == null) {
                throw new IllegalStateException("'" + schemas[i] + "' was not found in the current classloader !");
            }
            String urlString = url.toString();
            String id = urlString.substring(urlString.lastIndexOf('/') + 1);
            schemasUrls.put(id, urlString);

        }
    }

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
    public InputSource resolveEntity(final String publicId, final String systemId) throws IOException, SAXException {

        String localPath = null;

        if (systemId != null) {
            // Can be a schema
            if (systemId.toLowerCase().endsWith(".xsd")) {
                // Retrieve basename
                String baseName = systemId.substring(systemId.lastIndexOf('/') + 1);

                // Registred ?
                localPath = schemasUrls.get(baseName);
            }
        }

        // schema not found
        if (localPath == null) {
            throw new SAXException("No XSD found for '" + systemId + "'.");
        }

        // Return the local path source
        return (new InputSource(localPath));
    }

}
