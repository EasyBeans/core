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
 * $Id: JContainer3MBean.java 5693 2011-01-10 08:46:49Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.management;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.management.MBeanException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.container.JContainer3;
import org.ow2.easybeans.jsr77.EJBModuleMBean;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

/**
 * MBean for {@link org.ow2.easybeans.container.JContainer3}.
 * @author Guillaume Sauthier
 */
public class JContainer3MBean extends EJBModuleMBean {

    /**
     * Default ModelMBean constructor.
     * @throws MBeanException if ModelMBean creation fails.
     */
    public JContainer3MBean() throws MBeanException {
        super();
    }

    /**
     * Start this container.
     */
    public void start() {
        try {
            getManagedComponent().start();
        } catch (EZBContainerException ece) {
            getLogger().error("Cannot start the EJB Container", ece);
            throw new RuntimeException(ece);
        }
    }

    /**
     * Gets the URL of the archive of this container.
     * @return the URL of the archive of this container.
     */
    public URL getUrl() {
        try {
            IDeployable<?> deployable = getManagedComponent().getDeployable();
            // Try to get the original deployable if not null
            if (deployable.getOriginalDeployable() != null) {
                deployable = deployable.getOriginalDeployable();
            }

            return deployable.getArchive().getURL();
        } catch (ArchiveException e) {
            getLogger().error("Cannot get URL on the archive", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Stop this container.
     */
    public void stop() {
        getManagedComponent().stop();
    }

    /**
     * Gets data about loading a given class.
     * @param className the class name
     * @return a string description for the given class that needs to be loaded
     */
    public String loadClass(final String className) {

        // Get container/classloader
        JContainer3 container = getManagedComponent();
        ClassLoader ejbClassLoader = container.getClassLoader();
        boolean inEAR = container.getApplicationName() != null;

        // Create XML document...

        // Create builder with factory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Cannot build document builder", e);
        }

        // Create Document
        Document document = builder.newDocument();

        // Append root element
        Element classElement = document.createElement("class");
        document.appendChild(classElement);

        // name
        classElement.setAttribute("name", className);


        boolean classNotFound = false;
        String error = null;
        Class<?> clazz = null;
        try {
            clazz = ejbClassLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            error = e.toString();
            classNotFound = true;
        } catch (Error e) {
            classNotFound = true;
            error = e.toString();
        }

        // class not found ? (add error content if NotFound)
        classElement.setAttribute("classNotFound", Boolean.toString(classNotFound));
        if (classNotFound) {
            Element errorElement = document.createElement("error");
            classElement.appendChild(errorElement);
            Text errorText = document.createTextNode(error);
            errorElement.appendChild(errorText);
        } else {
            // Class found ! Add details (if any)

            // Search if the classes was loaded from the module, from the application or from the system
            String type = null;
            if (inEAR) {
                type = "Application / EJBs";
            } else {
                type = "Application/EJBJAR (This module)";
            }


            ClassLoader classClassLoader = clazz.getClassLoader();
            ClassLoader cl = ejbClassLoader;
            boolean found = false;
            while (cl != null && !found) {

                // ClassLoader is equals to the classloader that has loaded the class
                if (cl.equals(classClassLoader)) {
                    found = true;
                }

                cl = cl.getParent();
            }

            if (!found) {
                type = "System";
            }


            // Add where the class has been found
           classElement.setAttribute("where", type);


           // ClassLoader info (if any)
           if (classClassLoader != null) {
               Element classLoaderElement = document.createElement("class-loader");
               classElement.appendChild(classLoaderElement);
               classLoaderElement.setAttribute("name", classClassLoader.getClass().getName());
               Text classLoaderText = document.createTextNode(classClassLoader.toString());
               classLoaderElement.appendChild(classLoaderText);
            }
        }

        StringWriter stringWriter = new StringWriter();
        StreamResult streamResult = new StreamResult(stringWriter);

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            throw new IllegalStateException("Unable to get a new transformer", e);
        }

        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        // transform OUTPUT
        try {
            transformer.transform(new DOMSource(document), streamResult);
        } catch (TransformerException e) {
            throw new IllegalStateException("Unable to transform the document", e);
        }

        return stringWriter.toString();
    }

    /**
     * Gets all the URL to the given resource.
     * @param resourceName the name of the resource
     * @return the list of url, if any
     */
    public URL[] getResources(final String resourceName)  {
        ClassLoader classLoader = getManagedComponent().getClassLoader();

        Enumeration<URL> urls = null;
        try {
            urls = classLoader.getResources(resourceName);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to get the resource '" + resourceName + "'.", e);
        }

        List<URL> urlsList = new ArrayList<URL>();
        while (urls.hasMoreElements()) {
            urlsList.add(urls.nextElement());
        }

        return urlsList.toArray(new URL[urlsList.size()]);
    }

}
