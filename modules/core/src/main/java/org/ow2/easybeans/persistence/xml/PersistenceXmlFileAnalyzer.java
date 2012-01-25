/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id:PersistenceXmlFileAnalyzer.java 1537 2007-07-08 15:31:22Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence.xml;

import java.net.URL;
import java.util.List;

import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;

import org.ow2.easybeans.persistence.PersistenceUnitManager;
import org.ow2.easybeans.persistence.api.PersistenceXmlFileAnalyzerException;
import org.ow2.easybeans.util.loader.ClassUtils;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Class used to analyze a given archive (by analyzing the persistence.xml file
 * if any).
 * @author Florent Benoit
 */
public final class PersistenceXmlFileAnalyzer {

    /**
     * Directory where persistence.xml file should be.
     */
    private static final String DIRECTORY_PERSISTENCE_XML_FILE = "META-INF";

    /**
    * Another possibility for persistence.xml file location in case of WARs.
    */
   private static final String WEB_DIRECTORY_PERSISTENCE_XML_FILE = "WEB-INF/classes/META-INF";

    /**
     * Name of the persistence.xml file.
     */
    private static final String PERSISTENCE_XML_FILE = "persistence.xml";

    /**
     * Name of the orm.xml file.
     */
    private static final String ORM_XML_FILE = "orm.xml";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(PersistenceXmlFileAnalyzer.class);

    /**
     * Utility class, no public constructor.
     */
    private PersistenceXmlFileAnalyzer() {
    }

    /**
     * Detects and analyze the META-INF/persistence.xml file.
     * @param archive the file to analyze (or directory) in order to find a
     *        persistence.xml file.
     * @return a list of persistence unit information.
     * @throws PersistenceXmlFileAnalyzerException if detection or analyze
     *         fails.
     */
    public static JPersistenceUnitInfo[] analyzePersistenceXmlFile(final IArchive archive)
            throws PersistenceXmlFileAnalyzerException {
        URL persistenceXmlURL = null;

        try {
            persistenceXmlURL = archive.getResource(DIRECTORY_PERSISTENCE_XML_FILE + '/' + PERSISTENCE_XML_FILE);
        } catch (ArchiveException e) {
            throw new PersistenceXmlFileAnalyzerException("Cannot check if entry '" + DIRECTORY_PERSISTENCE_XML_FILE
                    + '/' + PERSISTENCE_XML_FILE + "' is present on the file '" + archive.getName() + "'.", e);
        }

        URL ormXmlURL = null;
        try {
            ormXmlURL = archive.getResource(DIRECTORY_PERSISTENCE_XML_FILE + '/' + ORM_XML_FILE);
        } catch (ArchiveException e) {
            throw new PersistenceXmlFileAnalyzerException("Cannot check if entry '" + DIRECTORY_PERSISTENCE_XML_FILE
                    + '/' + ORM_XML_FILE + "' is present on the file '" + archive.getName() + "'.", e);
        }

        URL persistenceXmlURLWeb = null;
        try {
            persistenceXmlURLWeb = archive.getResource(WEB_DIRECTORY_PERSISTENCE_XML_FILE + '/' + PERSISTENCE_XML_FILE);
        } catch (ArchiveException e) {
            throw new PersistenceXmlFileAnalyzerException("Cannot check if entry '" + WEB_DIRECTORY_PERSISTENCE_XML_FILE
                    + '/' + PERSISTENCE_XML_FILE + "' is present on the file '" + archive.getName() + "'.", e);
        }


        // Now, do the parsing and fill the structure.
        boolean found = false;
        List<JPersistenceUnitInfo> persistenceUnitInfos = null;
        if (persistenceXmlURL != null) {
            try {
                persistenceUnitInfos = JPersistenceUnitInfoHelper.getPersistenceUnitInfoList(persistenceXmlURL);
            } catch (JPersistenceUnitInfoException e) {
                throw new PersistenceXmlFileAnalyzerException("Cannot parse the URL '" + persistenceXmlURL + "'.", e);
            }
            found = true;
        }
        List<JPersistenceUnitInfo> persistenceUnitInfosWeb = null;
        if (persistenceXmlURLWeb != null) {
            try {
                persistenceUnitInfosWeb = JPersistenceUnitInfoHelper.getPersistenceUnitInfoList(persistenceXmlURLWeb);
            } catch (JPersistenceUnitInfoException e) {
                throw new PersistenceXmlFileAnalyzerException("Cannot parse the URL '" + persistenceXmlURLWeb + "'.", e);
            }
            if (persistenceUnitInfos != null) {
                persistenceUnitInfos.addAll(persistenceUnitInfosWeb);
            } else {
                persistenceUnitInfos = persistenceUnitInfosWeb;
                found = true;
            }
        }
        if (found) {
            int i = 0;
            JPersistenceUnitInfo[] persistenceUnitInfosTab = new JPersistenceUnitInfo[persistenceUnitInfos.size()];
            for (JPersistenceUnitInfo persistenceUnitInfo : persistenceUnitInfos) {
                persistenceUnitInfosTab[i++] = persistenceUnitInfo;
                try {
                    // Set the root url
                    persistenceUnitInfo.setPersistenceUnitRootUrl(archive.getURL());
                } catch (ArchiveException e) {
                    throw new PersistenceXmlFileAnalyzerException("Cannot get the URL on the jar file '" + archive.getName()
                            + "'.", e);
                }

                // Add mapping file
                if (ormXmlURL != null) {
                    persistenceUnitInfo.addMappingFileName(DIRECTORY_PERSISTENCE_XML_FILE + '/' + ORM_XML_FILE);
                }
            }
            return persistenceUnitInfosTab;
        }
        // nothing found, return nothing(null)
        return null;
    }

    /**
     * Load in the classloader the persistence provider class.
     * @param persistenceUnitInfos a list of persistence unit information.
     * @param classLoader the classloader used to load the persistence provider
     *        class.
     * @return A persistence unit manager (which can manage the persistence contexts).
     * @throws PersistenceXmlFileAnalyzerException if detection or analyze
     *         fails.
     */
    public static PersistenceUnitManager loadPersistenceProvider(final JPersistenceUnitInfo[] persistenceUnitInfos,
                                                                 final ClassLoader classLoader)
                                                                 throws PersistenceXmlFileAnalyzerException {

        for (JPersistenceUnitInfo persistenceUnitInfo : persistenceUnitInfos) {
            // sets the classloader
            persistenceUnitInfo.setClassLoader(classLoader);

            if (persistenceUnitInfo.getPersistenceProviderClassName() == null
                    && persistenceUnitInfo.getPersistenceProvider() == null) {

                // Try with the new JPA 2.0 API
                try {
                    List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder
                            .getPersistenceProviderResolver().getPersistenceProviders();
                    if (persistenceProviders != null && persistenceProviders.size() > 0) {
                        PersistenceProvider provider = persistenceProviders.get(0);
                        persistenceUnitInfo.setPersistenceProvider(provider);
                        persistenceUnitInfo.setPersistenceProviderClassName(provider.getClass().getName());
                    } else {
                        throw new PersistenceXmlFileAnalyzerException("No Persistence provider has been set");
                    }
                } catch (Error e) {
                    throw new PersistenceXmlFileAnalyzerException(
                            "No Persistence provider has been set and no JPA 2.0 API found so no access to "
                                    + "PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders() was available");
                }


            }

            // Instatiate only if this was not already done
            if (persistenceUnitInfo.getPersistenceProvider() == null) {

                // instantiate persistence provider
                Class<?> persistenceProviderClass;
                try {
                    persistenceProviderClass = ClassUtils.forName(persistenceUnitInfo
                            .getPersistenceProviderClassName(), PersistenceXmlFileAnalyzer.class);
                } catch (ClassNotFoundException e) {
                    throw new PersistenceXmlFileAnalyzerException("Cannot load the persistence provider class '"
                            + persistenceUnitInfo.getPersistenceProviderClassName() + "'.");
                }
                PersistenceProvider persistenceProvider;
                try {
                    persistenceProvider = (PersistenceProvider) persistenceProviderClass.newInstance();
                } catch (InstantiationException e) {
                    throw new PersistenceXmlFileAnalyzerException("Cannot instantiate the persistence provider class '"
                            + persistenceUnitInfo.getPersistenceProviderClassName() + "'.", e);
                } catch (IllegalAccessException e) {
                    throw new PersistenceXmlFileAnalyzerException("Cannot instantiate the persistence provider class '"
                            + persistenceUnitInfo.getPersistenceProviderClassName() + "'.", e);
                }

                // Set persistence provider
                persistenceUnitInfo.setPersistenceProvider(persistenceProvider);
            }
        }
        // create persistence unit manager
        return new PersistenceUnitManager(persistenceUnitInfos);
    }

}
