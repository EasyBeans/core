/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: StringConfigurationSource.java 4312 2008-11-13 10:14:50Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.configuration;

import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.ow2.util.xml.DocumentParserException;
import org.ow2.util.xmlconfig.source.IConfigurationSource;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Wraps a XML configuration stored as a String.
 * @author Guillaume Sauthier
 */
public class StringConfigurationSource implements IConfigurationSource {

    /**
     * The String configuration.
     */
    private String configuration;

    /**
     * Constructs a new String based {@link IConfigurationSource}.
     * @param configuration the String based configuration
     */
    public StringConfigurationSource(final String configuration) {
        this.configuration = configuration;
    }

    public Document getDocument(boolean validating) throws DocumentParserException {
        Document componentConfigurationXML;
        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            documentBuilderFactory.setNamespaceAware(true);
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            componentConfigurationXML = documentBuilder.parse(new InputSource(new StringReader(this.configuration)));
        } catch (Exception e) {
            throw new DocumentParserException("Could not load XML Configuration: " + configuration, e);
        }

        return componentConfigurationXML;
    }

    public String getName() {
        return configuration;
    }

}
