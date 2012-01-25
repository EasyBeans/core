/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: XMLConfigurationExtractor.java 3057 2008-04-30 16:02:08Z sauthieg $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.configuration;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.util.Property;
import org.ow2.easybeans.server.Embedded;
import org.ow2.util.xmlconfig.XMLConfiguration;
import org.ow2.util.xmlconfig.XMLConfigurationException;
import org.ow2.util.xmlconfig.mapping.ClassMapping;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * The Class XMLConfigurationExtractor.
 *
 * @author David Alves
 * @version $Revision$
 */
public class XMLConfigurationExtractor extends XMLConfiguration {

    /** The Constant XML_CONFIG_PROPERTY. */
    public static final String XML_CONFIG_PROPERTY = "xmlconfig";

    /** The Constant CLASS_NAME_CONFIG_PROPERTY. */
    public static final String CLASS_NAME_CONFIG_PROPERTY = "classname";

    /** The Constant INDEX_CONFIG_PROPERTY. */
    public static final String INDEX_CONFIG_PROPERTY = "index";

    /** The default parent component class. */
    private static Class<?> defaultParentComponentClass = EZBComponent.class;

    /** The component configurations. */
    private Collection<ComponentConfiguration> componentConfigurations;

    /** The component types to indexes map. */
    private Map<String, Integer> componentTypesToIndexesMap;

    /**
     * Sets the default parent component class.
     *
     * @param anotherDefaultCompoentClass the new default parent component class
     */
    public static void setDefaultParentComponentClass(final Class<?> anotherDefaultCompoentClass) {
        defaultParentComponentClass = anotherDefaultCompoentClass;
    }

    /**
     * Instantiates a new XML configuration extractor.
     *
     * @param xmlConfigurationURL the xml configuration URL
     *
     * @throws XMLConfigurationException the XML configuration exception
     */
    public XMLConfigurationExtractor(final URL xmlConfigurationURL) throws XMLConfigurationException {
        super(xmlConfigurationURL, "easybeans-mapping.xml");
        init();
    }

    /**
     * Instantiates a new XML configuration extractor.
     *
     * @param stream the xml configuration {@link InputStream}
     *
     * @throws XMLConfigurationException the XML configuration exception
     */
    public XMLConfigurationExtractor(final InputStream stream) throws XMLConfigurationException {
        super(stream, "easybeans-mapping.xml");
        init();
    }

    private void init() throws XMLConfigurationException {
        componentConfigurations = new ArrayList<ComponentConfiguration>();
        componentTypesToIndexesMap = new HashMap<String, Integer>();
        Embedded embedded = new Embedded();
        configure(embedded);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.ow2.easybeans.xmlconfig.XMLConfiguration#setAttributes(org.w3c.dom.Element, java.lang.Object,
     *      org.ow2.easybeans.xmlconfig.mapping.ClassMapping)
     */
    @Override
    protected void setAttributes(final Element node, final Object object, final ClassMapping classMapping)
            throws XMLConfigurationException {

        // Check if it is a EZBComponent
        if (!defaultParentComponentClass.isInstance(object)) {
            return;
        }

        ComponentConfiguration componentConfiguration = new ComponentConfiguration(object);
        //Extract the corresponding configuration XML

        String componentConfigurationXml = null;

        try {
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();

            // fetch parent node (components)
            Node parentNode = node.getParentNode();

            // fetch grand parent node (easybeans)
            Node grandParentNode = parentNode.getParentNode();

            // components node
            Node componentNode = node;

            // Build the new document
            Document document = documentBuilder.newDocument();
            Node newEasybeansNode = document.importNode(grandParentNode, false);
            document.appendChild(newEasybeansNode);
            Node newComponentsNode = document.importNode(parentNode, false);
            newEasybeansNode.appendChild(newComponentsNode);
            Node newComponentNode = document.importNode(componentNode, true);
            newComponentsNode.appendChild(newComponentNode);
            DOMSource domSource = new DOMSource(document);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter stringWriter = new StringWriter();
            Result result = new StreamResult(stringWriter);
            transformer.transform(domSource, result);
            componentConfigurationXml = stringWriter.toString().trim();
        } catch (Exception e) {
            throw new XMLConfigurationException("Could not parse XMl configuration file", e);
        }
        // set the XML config as a property
        Property classname = new Property();
        classname.setName(CLASS_NAME_CONFIG_PROPERTY);
        classname.setValue(object.getClass().getName());
        componentConfiguration.addProperty(classname);
        Property xmlConfiguration = new Property();
        xmlConfiguration.setName(XML_CONFIG_PROPERTY);
        xmlConfiguration.setValue(componentConfigurationXml);
        componentConfiguration.addProperty(xmlConfiguration);
        // Set the index in the componentConfiguration (for components with multiple instances)
        Integer index = componentTypesToIndexesMap.get(componentConfiguration.getComponentClass().getName());
        index = index == null ? new Integer(0) : ++index;
        Property indexProperty = new Property();
        indexProperty.setName(INDEX_CONFIG_PROPERTY);
        indexProperty.setValue(index + "");
        componentTypesToIndexesMap.put(componentConfiguration.getComponentClass().getName(), index);
        componentConfiguration.setIndex(index);
        componentConfigurations.add(componentConfiguration);
    }

    /**
     * Gets the component configurations.
     *
     * @return the componentConfigurations
     */
    public Collection<ComponentConfiguration> getComponentConfigurations() {
        return componentConfigurations;
    }

    /**
     * Gets the component configurations for symbolic name.
     *
     * @param symbolicName the symbolic name
     *
     * @return the component configurations for symbolic name
     */
    public Collection<ComponentConfiguration> getComponentConfigurationsForSymbolicName(final String symbolicName) {
        Collection<ComponentConfiguration> thisComponentConfigurations = new ArrayList<ComponentConfiguration>();
        for (ComponentConfiguration componentConfiguration : componentConfigurations) {
            if (componentConfiguration.getComponentSymbolicName().equals(symbolicName)) {
                thisComponentConfigurations.add(componentConfiguration);
            }
        }
        return thisComponentConfigurations;
    }

}
