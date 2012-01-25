/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: DocumentParser.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Allows to parse an xml file.
 * @author Florent Benoit
 */
public final class DocumentParser {

    /**
     * Utility class.
     */
    private DocumentParser() {

    }

    /**
     * Builds a new Document for a given xml file.
     * @param url the URL of the the XML file.
     * @param isValidating validate or not the xml file ?
     * @param entityResolver the entityResolver used to validate document (if
     *        validating = true)
     * @throws DocumentParserException if creating of builder fails or parsing
     *         fails.
     * @return an application object.
     */
    public static Document getDocument(final URL url, final boolean isValidating, final EntityResolver entityResolver)
            throws DocumentParserException {
        // build factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // XML files use schemas.
        factory.setNamespaceAware(true);
        factory.setValidating(isValidating);

        // ignore white space can only be set if parser is validating
        if (isValidating) {
            factory.setIgnoringElementContentWhitespace(true);
            factory.setAttribute("http://apache.org/xml/features/validation/schema", Boolean.valueOf(isValidating));
            factory.setAttribute("http://apache.org/xml/features/validation/schema-full-checking", Boolean.valueOf(true));
        }

        // Add schema location
        if (isValidating) {
            // Needs to get the version attribute and then set the schema
            // location. For this, get the version by parsing the document
            // without validation
            Document detectDocument = getDocument(url, false, entityResolver);

            // Root element = <persistence>
            Element persistenceRootElement = detectDocument.getDocumentElement();
            String version = XMLUtils.getAttributeValue(persistenceRootElement, "version");

            if ("1.0".equals(version)) {
                factory.setAttribute("http://apache.org/xml/properties/schema/external-schemaLocation",
                        "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd");
            } else if ("2.0".equals(version)) {
                factory.setAttribute("http://apache.org/xml/properties/schema/external-schemaLocation",
                        "http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd");
            } else {
                // This is a required attribute, needs to be set by the user !
                throw new DocumentParserException("Cannot detect the version of the Persistence schema from the URL '" + url
                        + "'");
            }

        }

        // Build a document builder
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new DocumentParserException("Cannot build a document builder", e);
        }

        // Error handler (throwing exceptions)
        builder.setErrorHandler(new EasyBeansErrorHandler());

        // Dummy entity resolver if there is none
        if (entityResolver == null) {
            builder.setEntityResolver(new EmptyEntityResolver());
        } else {
            builder.setEntityResolver(entityResolver);
        }

        // Parse document
        URLConnection urlConnection = null;
        try {
            urlConnection = url.openConnection();
        } catch (IOException e) {
            throw new DocumentParserException("Cannot open a connection on URL '" + url + "'", e);
        }
        urlConnection.setDefaultUseCaches(false);
        Reader reader = null;
        try {
            reader = new InputStreamReader(urlConnection.getInputStream());
        } catch (IOException e) {
            throw new DocumentParserException("Cannot build an input stream reader on URL '" + url + "'", e);
        }

        InputSource inputSource = new InputSource(reader);
        Document document = null;
        try {
            document = builder.parse(inputSource);
        } catch (SAXException e) {
            throw new DocumentParserException("Cannot parse the XML file '" + url + "'.", e);
        } catch (IOException e) {
            throw new DocumentParserException("Cannot parse the XML file '" + url + "'.", e);
        } finally {
            // close InputStream when parsing is finished
            try {
                reader.close();
            } catch (IOException e) {
                throw new DocumentParserException("Cannot close the inputsource of the XML file'" + url + "'.", e);
            }
        }

        return document;
    }

}
