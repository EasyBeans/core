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
 * $Id:JPersistenceUnitInfoHelper.java 1537 2007-07-08 15:31:22Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence.xml;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.sql.DataSource;

import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Class used to fill PersistenceInfo implementation class.
 * @author Florent Benoit
 */
public final class JPersistenceUnitInfoHelper {

    /**
     * Default XML configuration.
     */
    private static final String PERSISTENCE_CONFIG = "org/ow2/easybeans/persistence/conf/preconfigured-persistence-providers.xml";

    /**
     * Customized XML configuration.
     */
    private static final String DEFAULT_PERSISTENCEPROVIDER_CONFIG
        = "org/ow2/easybeans/persistence/conf/default-persistence-provider.xml";

    /**
     * System property to change the default provider.
     */
    public static final String DEFAULT_PERSISTENCE_PROVIDER = "org.ow2.easybeans.persistence.default.provider";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JPersistenceUnitInfoHelper.class);

    /**
     * Default configuration of persistence.xml (it defines the default
     * persistence provider and links to external config, one for each provider
     * class name.
     */
    private static JPersistenceUnitInfo defaultPersistenceunitInfo = null;

    /**
     * Defines the default configuration (properties) for each persistence
     * provider.
     */
    private static Map<String, JPersistenceUnitInfo> providersInfo = null;

    /**
     * Utility class, no public constructor.
     */
    private JPersistenceUnitInfoHelper() {

    }

    /**
     * Parses the given XML and complete the PersistenceUnitInfos structure
     * before returning it.
     * @param url the URL of the the Reader of the XML file.
     * @return array of jPersistenceInfo which are implementation of
     *         PersistenceUnitInfo object
     * @throws JPersistenceUnitInfoException if values are incorrect.
     */
    public static JPersistenceUnitInfo[] getPersistenceUnitInfo(final URL url) throws JPersistenceUnitInfoException {
        List<JPersistenceUnitInfo> jPersistenceunitInfos = getPersistenceUnitInfoList(url);
        return jPersistenceunitInfos.toArray(new JPersistenceUnitInfo[jPersistenceunitInfos.size()]);
    }

    /**
     * Parses the given XML and complete the PersistenceUnitInfos structure
     * before returning it.
     * @param url the URL of the the Reader of the XML file.
     * @return array of jPersistenceInfo which are implementation of
     *         PersistenceUnitInfo object
     * @throws JPersistenceUnitInfoException if values are incorrect.
     */
    public static List<JPersistenceUnitInfo> getPersistenceUnitInfoList(final URL url) throws JPersistenceUnitInfoException {
        // load default values if not already loaded.
        loadDefaultValues();

        List<JPersistenceUnitInfo> jPersistenceunitInfos = JPersistenceUnitInfoLoader.loadPersistenceUnitInfoImplList(url);

        for (JPersistenceUnitInfo jPersistenceunitInfo : jPersistenceunitInfos) {
            // analyze jta datasource
            String jtaDsName = jPersistenceunitInfo.getJtaDataSourceName();

            // try to get this datasource (if not null)
            if (jtaDsName != null && !jtaDsName.equals("")) {
                DataSource ds = null;
                try {
                    ds = (DataSource) new InitialContext().lookup(jtaDsName);
                } catch (NamingException e) {
                    // TODO: Remove
                    // try with jdbc_1
                    try {
                        logger.warn("Datasource named '" + jtaDsName
                                + "' was not found, use instead the default jndi name jdbc_1");
                        ds = (DataSource) new InitialContext().lookup("jdbc_1");
                    } catch (NamingException ne) {
                        throw new JPersistenceUnitInfoException("Cannot get jta DataSource with the JNDI name '"
                                + jtaDsName + "'.", ne);
                    }
                }
                jPersistenceunitInfo.setJtaDataSource(ds);
            }

            // analyze non jta datasource
            String nonJtaDsName = jPersistenceunitInfo.getNonJtaDataSourceName();
            // try to get this datasource (if not null)
            if (nonJtaDsName != null && !nonJtaDsName.equals("")) {
                DataSource ds = null;
                try {
                    ds = (DataSource) new InitialContext().lookup(nonJtaDsName);
                } catch (NamingException e) {
                    // TODO: Remove
                    // try with jdbc_1
                    try {
                        logger.warn("Datasource named '" + nonJtaDsName
                                + "' was not found, use instead the default jndi name jdbc_1");
                        ds = (DataSource) new InitialContext().lookup("jdbc_1");
                    } catch (NamingException ne) {
                        throw new JPersistenceUnitInfoException("Cannot get non jta DataSource with the JNDI name '"
                                + nonJtaDsName + "'.", ne);
                    }
                }
                jPersistenceunitInfo.setNonJtaDataSource(ds);
            }

            // Persistence Provider
            if (jPersistenceunitInfo.getPersistenceProviderClassName() == null
                    || jPersistenceunitInfo.getPersistenceProviderClassName().equals("")) {
                logger.debug("No persistence provider was set, set to default value {0}.", defaultPersistenceunitInfo
                        .getPersistenceProviderClassName());

                // instance is available ? set it
                if (defaultPersistenceunitInfo.getPersistenceProvider() != null) {
                    jPersistenceunitInfo.setPersistenceProvider(defaultPersistenceunitInfo.getPersistenceProvider());
                }
                jPersistenceunitInfo.setPersistenceProviderClassName(defaultPersistenceunitInfo
                        .getPersistenceProviderClassName());
            }

            // add properties for the given persistence provider
            JPersistenceUnitInfo providerDefaultConf = providersInfo.get(jPersistenceunitInfo
                    .getPersistenceProviderClassName());
            if (providerDefaultConf == null) {
                logger.debug("No default configuration for the persistence provider {0}", jPersistenceunitInfo
                        .getPersistenceProviderClassName());
            } else {
                logger.debug("Found a default configuration for the persistence provider {0}", jPersistenceunitInfo
                        .getPersistenceProviderClassName());
                Properties defaultProperties = providerDefaultConf.getProperties();
                Enumeration<?> providerPropertiesEnum = defaultProperties.propertyNames();
                while (providerPropertiesEnum.hasMoreElements()) {
                    String key = (String) providerPropertiesEnum.nextElement();
                    String value = defaultProperties.getProperty(key);
                    // set the value on the provider info
                    if (jPersistenceunitInfo.getProperties().getProperty(key) == null) {
                        jPersistenceunitInfo.getProperties().setProperty(key, value);
                        logger.debug("Setting the property {0} with value {1}", key, value);
                    }
                }

            }
        }
        return jPersistenceunitInfos;
    }

    /**
     * Loads the default values (global configuration) and then configuration of
     * each persistence provider.
     * @throws JPersistenceUnitInfoException if default configuration is not found
     */
    private static synchronized void loadDefaultValues() throws JPersistenceUnitInfoException {

        // already loaded, return.
        if (providersInfo != null) {
            return;
        }
        providersInfo = new HashMap<String, JPersistenceUnitInfo>();

        // Load default values for a persistence INFO
        ClassLoader currentCL = JPersistenceUnitInfo.class.getClassLoader();
        Enumeration<URL> urlsConfig = null;
        try {
            urlsConfig = currentCL.getResources(PERSISTENCE_CONFIG);
        } catch (IOException e) {
            throw new JPersistenceUnitInfoException("Cannot get resources with the name '" + PERSISTENCE_CONFIG
                    + "' in the context classloader '" + currentCL + "'.");
        }

        // reverse the list of URLs as we want to override the values
        LinkedList<URL> lstURLs = new LinkedList<URL>();
        while (urlsConfig.hasMoreElements()) {
            lstURLs.addFirst(urlsConfig.nextElement());
        }

        // Customization ?
        URL customizedConfig = currentCL.getResource(DEFAULT_PERSISTENCEPROVIDER_CONFIG);
        if (customizedConfig != null) {
            lstURLs.addLast(customizedConfig);
        }

        // For each URL, analyze properties (only one persistence unit is
        // expected by module)
        for (URL tmpURL : lstURLs) {
            JPersistenceUnitInfo[] jPersistenceunitInfos = JPersistenceUnitInfoLoader
                    .loadPersistenceUnitInfoImpl(tmpURL);
            // use first unit name
            if (jPersistenceunitInfos.length != 1) {
                throw new JPersistenceUnitInfoException(
                        "Each default config file should have only one persistence unit '" + tmpURL + "'.");
            }
            JPersistenceUnitInfo pInfo = jPersistenceunitInfos[0];
            String persistenceProviderClassName = pInfo.getPersistenceProviderClassName();
            if (persistenceProviderClassName != null) {
                if (defaultPersistenceunitInfo == null) {
                    defaultPersistenceunitInfo = new JPersistenceUnitInfo();
                    defaultPersistenceunitInfo.setPersistenceProviderClassName(persistenceProviderClassName);
                }
            }

            // Now extract configuration of each persistence provider
            Properties providersProperties = pInfo.getProperties();
            Enumeration<?> providerNames = providersProperties.propertyNames();
            while (providerNames.hasMoreElements()) {
                String providerName = (String) providerNames.nextElement();

                // check if there is an existing provider, else create it
                JPersistenceUnitInfo existingProviderInfo = providersInfo.get(providerName);
                if (existingProviderInfo == null) {
                    existingProviderInfo = new JPersistenceUnitInfo();
                    providersInfo.put(providerName, existingProviderInfo);
                }

                Enumeration<URL> urlsProviderConf = null;
                try {
                    urlsProviderConf = currentCL.getResources(providersProperties.getProperty(providerName));
                } catch (IOException e) {
                    throw new JPersistenceUnitInfoException("Cannot get resources with the name '" + providerName
                            + "' in the context classloader '" + currentCL + "'.");
                }
                // reverse order : used to override values
                LinkedList<URL> reverseProviderConfURLs = new LinkedList<URL>();
                while (urlsProviderConf.hasMoreElements()) {
                    reverseProviderConfURLs.addFirst(urlsProviderConf.nextElement());
                }

                if (reverseProviderConfURLs.size() == 0) {
                    logger.warn("No default properties for persistence provider class named {0}", providerName);
                }

                // for each info for a provider, set the values
                for (URL providerURLConf : reverseProviderConfURLs) {
                    JPersistenceUnitInfo[] providerPersistenceunitInfos = JPersistenceUnitInfoLoader
                            .loadPersistenceUnitInfoImpl(providerURLConf);
                    // use first unit name
                    if (providerPersistenceunitInfos.length != 1) {
                        throw new JPersistenceUnitInfoException(
                                "Each default config file should have only one persistence unit '" + providerURLConf
                                        + "'.");
                    }
                    JPersistenceUnitInfo providerInfo = providerPersistenceunitInfos[0];

                    // get provider info and set values
                    Properties providerProperties = providerInfo.getProperties();
                    Enumeration<?> providerPropertiesEnum = providerProperties.propertyNames();
                    while (providerPropertiesEnum.hasMoreElements()) {
                        String key = (String) providerPropertiesEnum.nextElement();
                        String value = providerProperties.getProperty(key);
                        // set the value on the provider info
                        existingProviderInfo.getProperties().setProperty(key, value);
                    }

                }
            }

        }

        // System property ? change the default persistence provider
        String sysPropertyPersistenceProvider = System.getProperty(DEFAULT_PERSISTENCE_PROVIDER);
        if (sysPropertyPersistenceProvider != null) {
            logger.debug("System property overriding the persistence provider ''{0}'' with the new value ''{1}''",
                    defaultPersistenceunitInfo.getPersistenceProviderClassName(), sysPropertyPersistenceProvider);
            defaultPersistenceunitInfo.setPersistenceProviderClassName(sysPropertyPersistenceProvider);
        }


        // Try with the new JPA 2.0 API
        if (defaultPersistenceunitInfo == null) {
            try {
                List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder
                .getPersistenceProviderResolver().getPersistenceProviders();
                if (persistenceProviders != null && persistenceProviders.size() > 0) {
                    defaultPersistenceunitInfo = new JPersistenceUnitInfo();
                    PersistenceProvider provider = persistenceProviders.get(0);
                    defaultPersistenceunitInfo.setPersistenceProvider(provider);
                    defaultPersistenceunitInfo.setPersistenceProviderClassName(provider.getClass().getName());
                }
            } catch (Error e) {
                logger
                        .warn("No Persistence provider has been set and no JPA 2.0 API found so no access to "
                                + "PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders() was available");
            }
        }



        if (defaultPersistenceunitInfo != null) {
            logger.debug("Default persistence provider set to value {0}.", defaultPersistenceunitInfo
                .getPersistenceProviderClassName());
        }

        if (defaultPersistenceunitInfo == null) {
            defaultPersistenceunitInfo = new JPersistenceUnitInfo();
        }

    }

    /**
     * @return the current default persistence unit info
     */
    public static JPersistenceUnitInfo getDefaultPersistenceunitInfo() {
        return defaultPersistenceunitInfo;
    }

    /**
     * Defines a new Default persistence unit info.
     * @param defaultPersistenceunitInfo the given object
     */
    public static void setDefaultPersistenceunitInfo(final JPersistenceUnitInfo defaultPersistenceunitInfo) {
        JPersistenceUnitInfoHelper.defaultPersistenceunitInfo = defaultPersistenceunitInfo;
    }

}
