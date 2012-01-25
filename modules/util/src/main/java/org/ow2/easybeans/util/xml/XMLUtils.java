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
 * $Id: XMLUtils.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.xml;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Class with some useful methods on XML document.
 */
public final class XMLUtils {

    /**
     * Utility class, no constructor.
     */
    private XMLUtils() {
    }

    /**
     * Returns the value of the attribute of the given element.
     * @param base the element from where to search.
     * @param name of the attribute to get.
     * @return the value of this element.
     */
    public static String getAttributeValue(final Element base, final String name) {

        // get attribute of this element...
        NamedNodeMap mapAttributes = base.getAttributes();
        Node node = mapAttributes.getNamedItem(name);
        if (node != null) {
            return node.getNodeValue();
        }
        return null;
    }

    /**
     * Returns the value of the given node.
     * @param ns the namespace.
     * @param base the element from where to search.
     * @param name of the element to get.
     * @return the value of this element.
     */
    public static String getStringValueElement(final String ns, final Element base, final String name) {
        String value = null;

        // Get element
        NodeList list = base.getElementsByTagNameNS(ns, name);
        if (list.getLength() == 1) {
            Element element = (Element) list.item(0);
            Node node = element.getFirstChild();
            if (node != null) {
                value = node.getNodeValue();
            }
        } else if (list.getLength() > 1) {
            throw new IllegalStateException("Element '" + name + "' on '" + base + "' should be unique but there are '"
                    + list.getLength() + "' elements");
        }

        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    /**
     * Returns the value of the given node.
     * @param base the element from where to search.
     * @param name of the element to get.
     * @return the value of this element.
     */
    public static String getStringValueElement(final Element base, final String name) {
        String value = null;

        // Get element
        NodeList list = base.getElementsByTagName(name);
        if (list.getLength() == 1) {
            Element element = (Element) list.item(0);
            Node node = element.getFirstChild();
            if (node != null) {
                value = node.getNodeValue();
            }
        } else if (list.getLength() > 1) {
            throw new IllegalStateException("Element '" + name + "' on '" + base + "' should be unique but there are '"
                    + list.getLength() + "' elements");
        }

        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    /**
     * Returns the value of the child node with the given name.
     * @param base the element from where to search.
     * @param name of the element to get.
     * @return the value of this element.
     */
    public static String getChildStringValueForElement(final Element base, final String name) {
        String value = null;
        NodeList nodeList = base.getChildNodes();
        if (nodeList.getLength() > 0) {
            int length = nodeList.getLength();
            for (int i = 0; i < length; i++) {
                Node node = nodeList.item(i);

                // Get an element, create an instance of the element
                if (Node.ELEMENT_NODE == node.getNodeType()) {
                    if (name.equals(node.getNodeName())) {
                        // Get value of this child
                        Node elNode = ((Element) node).getFirstChild();
                        if (elNode != null) {
                            value = elNode.getNodeValue();
                            break;
                        }
                    }
                }
            }
        }
        return value;
    }


    /**
     * Returns a Properties object matching the given node.
     * @param ns the namespace.
     * @param base the element from where to search.
     * @param name of the element to get.
     * @return the value of this element.
     */
    public static Properties getPropertiesValueElement(final String ns, final Element base, final String name) {
        Properties returnedProperties = new Properties();

        // Get element
        NodeList list = base.getElementsByTagNameNS(ns, name);
        if (list.getLength() == 1) {
            Element element = (Element) list.item(0);

            // Get property element
            NodeList properties = element.getElementsByTagNameNS(ns, "property");

            // If properties is present, analyze them and add them
            if (properties.getLength() > 0) {
                for (int i = 0; i < properties.getLength(); i++) {
                    Element elemProperty = (Element) properties.item(i);
                    String pName = getAttributeValue(elemProperty, "name");
                    String pValue = getAttributeValue(elemProperty, "value");
                    if (pName != null && pValue != null) {
                        returnedProperties.setProperty(pName, pValue);
                    }

                }
            }
        } else if (list.getLength() > 1) {
            throw new IllegalStateException("Element '" + name + "' on '" + base + "' should be unique but there are '"
                    + list.getLength() + "' elements");
        }

        return returnedProperties;
    }

    /**
     * Returns a list of value for the given node.
     * @param ns the namespace.
     * @param base the element from where to search.
     * @param name of the element to get.
     * @return the list of value of this element.
     */
    public static List<String> getStringListValueElement(final String ns, final Element base, final String name) {
        List<String> returnedlist = new ArrayList<String>();

        // Get element
        NodeList list = base.getElementsByTagNameNS(ns, name);
        int length = list.getLength();

        // Get all values of all elements
        if (length > 0) {
            for (int i = 0; i < length; i++) {
                Element element = (Element) list.item(i);
                Node node = element.getFirstChild();
                if (node != null) {
                    returnedlist.add(node.getNodeValue());
                }
            }
        }
        return returnedlist;
    }

}
