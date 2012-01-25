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
 * $Id: CommonsModelerExtension.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jmx;

import java.io.InputStream;

import org.apache.commons.modeler.AttributeInfo;
import org.apache.commons.modeler.ConstructorInfo;
import org.apache.commons.modeler.FieldInfo;
import org.apache.commons.modeler.ManagedBean;
import org.apache.commons.modeler.NotificationInfo;
import org.apache.commons.modeler.OperationInfo;
import org.apache.commons.modeler.ParameterInfo;
import org.apache.commons.modeler.Registry;
import org.apache.commons.modeler.util.DomUtil;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Utility class used to extends mbeans-descriptors.
 * @author Guillaume Sauthier
 */
public final class CommonsModelerExtension {

    /**
     * Private empty constructor for Utility class.
     */
    private CommonsModelerExtension() { }

    /**
     * Update the given Registry (and particulary the inner ManagedBean).
     * @param registry Registry to be updated.
     * @param stream mbeans-descriptors-ext.xml content
     * @throws Exception when something fails.
     */
    public static void updateDescriptors(final Registry registry, final InputStream stream) throws Exception {

        try {
            Document doc = DomUtil.readXml(stream);
            // Ignore for now the name of the root element
            Node descriptorsN = doc.getDocumentElement();
            // Node descriptorsN=DomUtil.getChild(doc, "mbeans-descriptors");
            if (descriptorsN == null) {
                return;
            }

            Node firstMbeanN = null;
            if ("mbean".equals(descriptorsN.getNodeName())) {
                firstMbeanN = descriptorsN;
            } else {
                firstMbeanN = DomUtil.getChild(descriptorsN, "mbean");
            }

            if (firstMbeanN == null) {
                return;
            }

            // Process each <mbean> element
            for (Node mbeanN = firstMbeanN; mbeanN != null; mbeanN = DomUtil.getNext(mbeanN)) {

                processMBeanNode(registry, mbeanN);
             }

        } catch (Exception ex) {
            // TODO Add log statement
        }
    }

    /**
     * @param registry Model MBean registry
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanNode(final Registry registry, final Node mbeanNode) {
        // EasyBeans Change ---------------------------------------------
        // Get the mbean name
        NamedNodeMap attrs = mbeanNode.getAttributes();
        Node n = attrs.getNamedItem("name");
        String mbeanName = n.getNodeValue();

        // Get the ManagedBean
        ManagedBean managed = registry.findManagedBean(mbeanName);
        // /EasyBeans Change ---------------------------------------------

        DomUtil.setAttributes(managed, mbeanNode);

        // Process descriptor subnode
        processMBeanDescriptorNode(managed, mbeanNode);

        // process attribute nodes
        processMBeanAttributeNode(managed, mbeanNode);

        // process constructor nodes
        processMBeanConstructorNode(managed, mbeanNode);

        // process notification nodes
        processMBeanNotificationNode(managed, mbeanNode);

        // process operation nodes
        processMBeanOperationNode(managed, mbeanNode);
    }

    /**
     * @param managed ManagedBean instance
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanOperationNode(final ManagedBean managed, final Node mbeanNode) {
        Node firstN;
        firstN = DomUtil.getChild(mbeanNode, "operation");
        for (Node descN = firstN; descN != null; descN = DomUtil.getNext(descN)) {

            // Create new operation info
            OperationInfo oi = new OperationInfo();
            DomUtil.setAttributes(oi, descN);

            // Process descriptor subnode
            Node firstDescriptorN = DomUtil.getChild(descN, "descriptor");
            if (firstDescriptorN != null) {
                Node firstFieldN = DomUtil.getChild(firstDescriptorN, "field");
                for (Node fieldN = firstFieldN; fieldN != null; fieldN = DomUtil.getNext(fieldN)) {
                    FieldInfo fi = new FieldInfo();
                    DomUtil.setAttributes(fi, fieldN);
                    oi.addField(fi);
                }
            }

            // Process parameter subnodes
            Node firstParamN = DomUtil.getChild(descN, "parameter");
            for (Node paramN = firstParamN; paramN != null; paramN = DomUtil.getNext(paramN)) {
                ParameterInfo pi = new ParameterInfo();
                DomUtil.setAttributes(pi, paramN);
                oi.addParameter(pi);
            }

            // Add this info to our managed bean info
            managed.addOperation(oi);

        }
    }

    /**
     * @param managed ManagedBean instance
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanNotificationNode(final ManagedBean managed, final Node mbeanNode) {
        Node firstN;
        firstN = DomUtil.getChild(mbeanNode, "notification");
        for (Node descN = firstN; descN != null; descN = DomUtil .getNext(descN)) {

            // Create new notification info
            NotificationInfo ni = new NotificationInfo();
            DomUtil.setAttributes(ni, descN);

            // Process descriptor subnode
            Node firstDescriptorN = DomUtil.getChild(descN, "descriptor");
            if (firstDescriptorN != null) {
                Node firstFieldN = DomUtil.getChild(firstDescriptorN, "field");
                for (Node fieldN = firstFieldN; fieldN != null; fieldN = DomUtil.getNext(fieldN)) {
                    FieldInfo fi = new FieldInfo();
                    DomUtil.setAttributes(fi, fieldN);
                    ni.addField(fi);
                }
            }

            // Process notification-type subnodes
            Node firstParamN = DomUtil.getChild(descN, "notification-type");
            for (Node paramN = firstParamN; paramN != null; paramN = DomUtil.getNext(paramN)) {
                ni.addNotifType(DomUtil.getContent(paramN));
            }

            // Add this info to our managed bean info
            managed.addNotification(ni);

        }
    }

    /**
     * @param managed ManagedBean instance
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanConstructorNode(final ManagedBean managed, final Node mbeanNode) {
        Node firstN;
        firstN = DomUtil.getChild(mbeanNode, "constructor");
        for (Node descN = firstN; descN != null; descN = DomUtil.getNext(descN)) {

            // Create new constructor info
            ConstructorInfo ci = new ConstructorInfo();
            DomUtil.setAttributes(ci, descN);

            // Process descriptor subnode
            Node firstDescriptorN = DomUtil.getChild(descN, "descriptor");
            if (firstDescriptorN != null) {
                Node firstFieldN = DomUtil.getChild(firstDescriptorN, "field");
                for (Node fieldN = firstFieldN; fieldN != null; fieldN = DomUtil.getNext(fieldN)) {
                    FieldInfo fi = new FieldInfo();
                    DomUtil.setAttributes(fi, fieldN);
                    ci.addField(fi);
                }
            }

            // Process parameter subnodes
            Node firstParamN = DomUtil.getChild(descN, "parameter");
            for (Node paramN = firstParamN; paramN != null; paramN = DomUtil.getNext(paramN)) {
                ParameterInfo pi = new ParameterInfo();
                DomUtil.setAttributes(pi, paramN);
                ci.addParameter(pi);
            }

            // Add this info to our managed bean info
            managed.addConstructor(ci);

        }
    }

    /**
     * @param managed ManagedBean instance
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanAttributeNode(final ManagedBean managed, final Node mbeanNode) {
        Node firstN;
        firstN = DomUtil.getChild(mbeanNode, "attribute");
        for (Node descN = firstN; descN != null; descN = DomUtil.getNext(descN)) {

            // Create new attribute info
            AttributeInfo ai = new AttributeInfo();
            DomUtil.setAttributes(ai, descN);

            // Process descriptor subnode
            Node descriptorN = DomUtil.getChild(descN, "descriptor");
            if (descriptorN != null) {
                Node firstFieldN = DomUtil.getChild(descriptorN, "field");
                for (Node fieldN = firstFieldN; fieldN != null; fieldN = DomUtil.getNext(fieldN)) {
                    FieldInfo fi = new FieldInfo();
                    DomUtil.setAttributes(fi, fieldN);
                    ai.addField(fi);
                }
            }

            // Add this info to our managed bean info
            managed.addAttribute(ai);

        }
    }

    /**
     * @param managed ManagedBean instance
     * @param mbeanNode mbean XML Node
     */
    private static void processMBeanDescriptorNode(final ManagedBean managed, final Node mbeanNode) {
        Node mbeanDescriptorN = DomUtil.getChild(mbeanNode, "descriptor");
        if (mbeanDescriptorN != null) {
            Node firstFieldN = DomUtil.getChild(mbeanDescriptorN, "field");
            for (Node fieldN = firstFieldN; fieldN != null; fieldN = DomUtil.getNext(fieldN)) {
                FieldInfo fi = new FieldInfo();
                DomUtil.setAttributes(fi, fieldN);
                managed.addField(fi);
            }
        }
    }
}
