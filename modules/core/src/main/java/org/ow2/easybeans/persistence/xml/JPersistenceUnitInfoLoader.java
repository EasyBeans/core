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
 * $Id: JPersistenceUnitInfoLoader.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence.xml;

import static javax.persistence.spi.PersistenceUnitTransactionType.JTA;
import static javax.persistence.spi.PersistenceUnitTransactionType.RESOURCE_LOCAL;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;

import org.ow2.easybeans.util.xml.DocumentParser;
import org.ow2.easybeans.util.xml.DocumentParserException;
import org.ow2.easybeans.util.xml.XMLUtils;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Class used to fill PersistenceUnitInfo implementation class by loading an
 * XML.
 * @author Florent Benoit
 */
public final class JPersistenceUnitInfoLoader {

    /**
     * Persistence namespace.
     */
    private static final String PERSISTENCE_NS = "http://java.sun.com/xml/ns/persistence";

    /**
     * &lt;persistence-unit&gt; element.
     */
    private static final String PERSISTENCE_UNIT = "persistence-unit";

    /**
     * Path of persistence.xml within the module (the jar or ear).
     */
    private static final String PERSISTENCE_FILE_PATH = "/META-INF/persistence.xml";
    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JPersistenceUnitInfoLoader.class);

    /**
     * Validating with schema ?
     */
    private static boolean validating = true;

    /**
     * Utility class, no public constructor.
     */
    private JPersistenceUnitInfoLoader() {

    }

    /**
     * Load the persistence.xml file.
     * @param url the URL of the the Reader of the XML file.
     * @throws JPersistenceUnitInfoException if parsing of XML file fails.
     * @return an application object.
     */
    public static List<JPersistenceUnitInfo> loadPersistenceUnitInfoImplList(final URL url)
            throws JPersistenceUnitInfoException {


        logger.debug("Analyzing url {0}", url);

        // List of PersistenceUnitInfo objects
        List<JPersistenceUnitInfo> jPersistenceUnitInfos = new ArrayList<JPersistenceUnitInfo>();

        // Get document
        Document document = null;
        try {
            document = DocumentParser.getDocument(url, validating, new PersistenceUnitEntityResolver());
        } catch (DocumentParserException e) {
           throw new JPersistenceUnitInfoException("Cannot parse the url", e);
        }

        // Root element = <persistence>
        Element persistenceRootElement = document.getDocumentElement();

        // Version
        String version = XMLUtils.getAttributeValue(persistenceRootElement, "version");


        NodeList persistenceUnitInfoList = persistenceRootElement.getElementsByTagNameNS(PERSISTENCE_NS, PERSISTENCE_UNIT);

        // Loop on this list
        for (int i = 0; i < persistenceUnitInfoList.getLength(); i++) {
            Element pUnitElement = (Element) persistenceUnitInfoList.item(i);

            // Build instance that is created.
            JPersistenceUnitInfo persistenceUnitInfo = new JPersistenceUnitInfo();

            // set the URL of the persistence file.
            persistenceUnitInfo.setPersistenceXmlFileUrl(url);

            // Version
            persistenceUnitInfo.setPersistenceXMLSchemaVersion(version);

            // Provider
            String className = XMLUtils.getStringValueElement(PERSISTENCE_NS, pUnitElement, "provider");
            persistenceUnitInfo.setPersistenceProviderClassName(className);

            // Jta-data-source
            String jtaDataSourceName = XMLUtils.getStringValueElement(PERSISTENCE_NS, pUnitElement, "jta-data-source");
            persistenceUnitInfo.setJtaDataSourceName(jtaDataSourceName);

            // Non-jta-data-source
            String nonJtaDataSourceName = XMLUtils.getStringValueElement(PERSISTENCE_NS, pUnitElement, "non-jta-data-source");
            persistenceUnitInfo.setNonJtaDataSourceName(nonJtaDataSourceName);

            // mapping-file
            List<String> mappingFiles = XMLUtils.getStringListValueElement(PERSISTENCE_NS, pUnitElement, "mapping-file");
            for (String mappingFileName : mappingFiles) {
                persistenceUnitInfo.addMappingFileName(mappingFileName);
            }

            // jar-file
            List<String> jarFiles = XMLUtils.getStringListValueElement(PERSISTENCE_NS, pUnitElement, "jar-file");
            for (String jarFileName : jarFiles) {
                // Get the URL of the jar files and check that a corresponding file really exists
                try {
                    String persistencePath = url.getPath();
                    // find the index of /META-INF/persistence.xml
                    int index = persistencePath.indexOf(PERSISTENCE_FILE_PATH);
                    // find the index of the previous /
                    int index1 = persistencePath.substring(0, index).lastIndexOf('/');
                    String basePath = persistencePath.substring(0, index1 + 1);
                    String jarPath = basePath.concat(jarFileName);
                    logger.debug("Got a JAR path {0}.", jarPath);
                    URL jarUrl =  new URL(jarPath);
                    File jarFile = new File(jarUrl.getPath());
                    if (jarFile != null && jarFile.exists()) {
                        persistenceUnitInfo.addJarFile(jarUrl);
                    } else {
                        logger.warn(">> Found an inexistent JAR declaration {0}.", jarPath);
                    }
                } catch (java.net.MalformedURLException me) {
                    logger.error("Got MalformedURLException {0}", me);
                    throw new JPersistenceUnitInfoException("Problem with jar-file " + jarFileName);
                } catch (IndexOutOfBoundsException e) {
                    logger.error("Got IndexOutOfBoundsException {0}", e);
                    throw new JPersistenceUnitInfoException("Problem with jar-file " + jarFileName);
                }
            }


            // class (managed class)
            List<String> classes = XMLUtils.getStringListValueElement(PERSISTENCE_NS, pUnitElement, "class");
            for (String managedClassName : classes) {
                persistenceUnitInfo.addClass(managedClassName);
            }

            // exclude-unlisted-classes
            String excluded = XMLUtils.getStringValueElement(PERSISTENCE_NS, pUnitElement, "exclude-unlisted-classes");
            persistenceUnitInfo.setExcludeUnlistedClasses("true".equals(excluded));

            // properties
            Properties props = XMLUtils.getPropertiesValueElement(PERSISTENCE_NS, pUnitElement, "properties");
            persistenceUnitInfo.setProperties(props);

            // Name attribute
            String name = XMLUtils.getAttributeValue(pUnitElement, "name");
            persistenceUnitInfo.setPersistenceUnitName(name);

            // transaction-type attribute
            String transactionType = XMLUtils.getAttributeValue(pUnitElement, "transaction-type");
            if ("JTA".equals(transactionType)) {
                persistenceUnitInfo.setTransactionType(JTA);
            } else if ("RESOURCE_LOCAL".equals(transactionType)) {
                persistenceUnitInfo.setTransactionType(RESOURCE_LOCAL);
            } else {
                logger.warn("No transaction-type defined. Set to default JTA transaction-type");
                persistenceUnitInfo.setTransactionType(JTA);
            }

            // Only for JPA 2.0

            if ("2.0".equals(version)) {
                /*
                 * The enum javax.persistence.SharedCacheMode defines the use of
                 * caching. The persis- tence.xml shared-cache-mode element has no
                 * default value. The getSharedCacheMode method must return
                 * UNSPECIFIED if the shared-cache-mode element has not been
                 * specified for the persistence unit.
                 */

                String sharedCacheMode = XMLUtils.getAttributeValue(pUnitElement, "shared-cache-mode");
                if ("ALL".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.ALL);
                } else if ("NONE".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.NONE);
                } else if ("ENABLE_SELECTIVE".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.ENABLE_SELECTIVE);
                } else if ("DISABLE_SELECTIVE".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.DISABLE_SELECTIVE);
                } else if ("UNSPECIFIED".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.UNSPECIFIED);
                } else {
                    logger.warn("No SharedCacheMode defined. Set to default UNSPECIFIED");
                    persistenceUnitInfo.setSharedCacheMode(SharedCacheMode.UNSPECIFIED);
                }

                // validation-mode
                String validationMode = XMLUtils.getAttributeValue(pUnitElement, "validation-mode");
                if ("AUTO".equals(validationMode)) {
                    persistenceUnitInfo.setValidationMode(ValidationMode.AUTO);
                } else if ("CALLBACK".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setValidationMode(ValidationMode.CALLBACK);
                } else if ("NONE".equals(sharedCacheMode)) {
                    persistenceUnitInfo.setValidationMode(ValidationMode.NONE);
                } else {
                    logger.warn("No Validation Mode defined. Set to default AUTO");
                    persistenceUnitInfo.setValidationMode(ValidationMode.AUTO);
                }
            }

            jPersistenceUnitInfos.add(persistenceUnitInfo);

        }
        return jPersistenceUnitInfos;
    }

    /**
     * Load the persistence.xml file.
     * @param url the URL of the the Reader of the XML file.
     * @throws JPersistenceUnitInfoException if parsing of XML file fails.
     * @return an application object.
     */
    public static JPersistenceUnitInfo[] loadPersistenceUnitInfoImpl(final URL url)
    throws JPersistenceUnitInfoException {
        List<JPersistenceUnitInfo> jPersistenceUnitInfos = loadPersistenceUnitInfoImplList(url);
        return jPersistenceUnitInfos.toArray(new JPersistenceUnitInfo[jPersistenceUnitInfos.size()]);
    }
}
