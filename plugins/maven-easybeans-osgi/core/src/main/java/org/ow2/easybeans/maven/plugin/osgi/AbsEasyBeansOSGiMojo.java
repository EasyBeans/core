/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id: AbsEasyBeansOSGiMojo.java 6143 2012-01-25 14:15:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.maven.plugin.osgi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.util.maven.osgi.launcher.core.BundleDesc;
import org.ow2.util.url.URLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Common stuff for bundles used by EasyBeans.
 * @author Florent Benoit
 */
public abstract class AbsEasyBeansOSGiMojo extends AbsInheritedParametersMojo {

    /**
     * Key for keeping the launcher (local or remote).
     */
    private static final String LAUNCHER_KEY = "EasyBeans-OSGi-Mojo";

    /**
     * Resource used to configure the plugin.
     */
    private static final String XML_PATH = "/" + AbsEasyBeansOSGiMojo.class.getPackage().getName().replace(".", "/")
            + "/easybeans-osgi-plugin.xml";

    /**
     * XPath factory used to parse XML.
     */
    private XPathFactory xPathFactory = null;

    /**
     * XPath instance used to parse XML.
     */
    private XPath xPath = null;

    /**
     * XPath expression shared.
     */
    private XPathExpression xPathExpr = null;

    /**
     * XML document.
     */
    private Document doc = null;

    /**
     * List of the bundles that will populate the framework.
     */
    private List<BundleDesc> frameworkBundles = null;

    /**
     * List of the interfaces that needs to be waited in order to be ready.
     */
    private List<String> frameworkWaitInterfaces = null;

    /**
     * List of the bundles used to start the testing.
     */
    private List<BundleDesc> testFrameworkBundles = null;

    /**
     * Default constructor.
     */
    public AbsEasyBeansOSGiMojo() {
        super();
        this.xPathFactory = XPathFactory.newInstance();
        this.xPath = this.xPathFactory.newXPath();
        this.frameworkBundles = new ArrayList<BundleDesc>();
        this.frameworkWaitInterfaces = new ArrayList<String>();
        this.testFrameworkBundles = new ArrayList<BundleDesc>();

        // Read XML
        InputStream is = null;
        try {
            is = AbsEasyBeansOSGiMojo.class.getResourceAsStream(XML_PATH);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = null;
            try {
                builder = domFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                throw new IllegalStateException("Cannot build document builder", e);
            }

            // Get document
            try {
                this.doc = builder.parse(is);
            } catch (SAXException e) {
                throw new IllegalStateException("Cannot analyze configuration file", e);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot analyze configuration file", e);
            }
        } finally {
            // Close
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    getLog().debug("Cannot close input stream", e);
                }
            }
        }

    }

    /**
     * Execute the Maven plugin.
     * @throws MojoExecutionException if the file is not generated.
     */
    @Override
    public void execute() throws MojoExecutionException {
        init();

        // Get OSGi framework configuration
        String frameworkConfiguration = getElementName(this.doc, "//plugin/osgi.framework.configuration");
        Properties properties = new Properties();

        if (frameworkConfiguration != null) {
            try {
                properties.load(new ByteArrayInputStream(frameworkConfiguration.getBytes()));
            } catch (IOException e) {
                throw new MojoExecutionException("Unable to read properties from '" + frameworkConfiguration + "'.");
            }

            // give these properties to the configuration
            Map<String, String> osgiConfiguration = new HashMap<String, String>();
            Set<Entry<Object, Object>> entries = properties.entrySet();
            Iterator<Entry<Object, Object>> itEntries = entries.iterator();
            while (itEntries.hasNext()) {
                Entry<Object, Object> entry = itEntries.next();
                osgiConfiguration.put(entry.getKey().toString(), entry.getValue().toString());
            }
            getConfiguration().setOsgiConfiguration(osgiConfiguration);
        }

        // Get framework bundles
        this.frameworkBundles = getBundleDesc("//plugin/framework/bundles/bundle");
        getLog().debug("Framework Bundles are " + this.frameworkBundles);

        // Get test framework bundles
        this.testFrameworkBundles = getBundleDesc("//plugin/test-framework/bundles/bundle");
        getLog().debug("Test Framework bundles = " + this.testFrameworkBundles);

        // Interfaces to wait
        NodeList interfacesReadyList = getNodeList("//plugin/framework/interfaces-ready/interfaces/interface");
        if (interfacesReadyList != null) {
            for (int i = 0; i < interfacesReadyList.getLength(); i++) {
                Node node = interfacesReadyList.item(i);

                String interfaceName = node.getTextContent();
                if (interfaceName != null) {
                    this.frameworkWaitInterfaces.add(interfaceName.trim());
                }

            }
        }
        getLog().debug("Framework interfaces to wait are " + this.frameworkWaitInterfaces);
        super.execute();

    }

    /**
     * Return a list of bundle description for the given XPath expression.
     * @param expression the nodes to get from the document
     * @return a list of the bundles
     * @throws MojoExecutionException if parsing XML fails
     */
    protected List<BundleDesc> getBundleDesc(final String expression) throws MojoExecutionException {
        NodeList bundleList = getNodeList(expression);
        if (bundleList == null) {
            throw new MojoExecutionException("Unable to find bundles.");
        }

        List<BundleDesc> bundleDescList = new ArrayList<BundleDesc>();

        for (int i = 0; i < bundleList.getLength(); i++) {
            Node node = bundleList.item(i);

            String groupId = getElementName(node, "groupId");
            String artifactId = getElementName(node, "artifactId");
            String version = getElementName(node, "version");
            String type = getElementName(node, "type", "jar");
            String classifier = getElementName(node, "classifier");

            Artifact artifact = addArtifact(groupId, artifactId, version, type, classifier, null);

            // default is true and startLevel = 1
            String start = getElementName(node, "start", "true");
            String startLevel = getElementName(node, "startLevel", "1");

            BundleDesc bundleDesc = new BundleDesc();
            bundleDesc.setLocation(URLUtils.fileToURL(artifact.getFile()).toExternalForm());
            bundleDesc.setStart(Boolean.valueOf(start).booleanValue());
            bundleDesc.setStartLevel(Integer.valueOf(startLevel).intValue());

            bundleDescList.add(bundleDesc);
        }
        return bundleDescList;
    }

    /**
     * @return the framework bundles
     */
    public List<BundleDesc> getFrameworkBundles() {
        return this.frameworkBundles;
    }

    /**
     * @return the test framework bundles
     */
    public List<BundleDesc> getTestFrameworkBundles() {
        return this.testFrameworkBundles;
    }

    /**
     * @return the interfaces to wait for the framework
     */
    public List<String> getFrameworkWaitInterfaces() {
        return this.frameworkWaitInterfaces;
    }

    /**
     * Return a list of nodes for the given XPath expression.
     * @param expression the nodes to get from the document
     * @return a list of the nodes
     * @throws MojoExecutionException if parsing XML fails
     */
    protected NodeList getNodeList(final String expression) throws MojoExecutionException {
        try {
            this.xPathExpr = this.xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException("Cannot analyze XML configuration file", e);
        }
        NodeList nodelist = null;
        try {
            nodelist = (NodeList) this.xPathExpr.evaluate(this.doc, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException("Cannot analyze configuration file", e);
        }
        return nodelist;
    }

    /**
     * Return the element name of the analyzed node.
     * @param analyzeNode the node to analyze
     * @param expression the xpath expression
     * @param extra extra parameters
     * @return the name of the node
     * @throws MojoExecutionException if parsing XML fails
     */
    protected String getElementName(final Node analyzeNode, final String expression, final String... extra)
            throws MojoExecutionException {
        try {
            this.xPathExpr = this.xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException("Cannot analyze XML configuration file", e);
        }
        Node foundNode = null;
        try {
            foundNode = (Node) this.xPathExpr.evaluate(analyzeNode, XPathConstants.NODE);
        } catch (XPathExpressionException e) {
            throw new MojoExecutionException("Cannot analyze configuration file", e);
        }
        if (foundNode == null) {
            if (extra != null && extra.length > 0) {
                return extra[0];
            }
            return null;
        }
        String value = foundNode.getFirstChild().getNodeValue();
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    /**
     * Change the launcher key for this plugin.
     * @return the launcher key
     */
    @Override
    protected String getLauncherKey() {
        return LAUNCHER_KEY;
    }

}
