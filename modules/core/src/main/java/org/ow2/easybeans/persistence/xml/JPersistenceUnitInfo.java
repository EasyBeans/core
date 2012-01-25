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
 * $Id: JPersistenceUnitInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence.xml;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.ow2.easybeans.api.loader.EZBClassLoader;

/**
 * Implementation of the PersistenceUnitInfo interface. It is given to the
 * persistence provider to create entity managers.
 * @author Florent Benoit
 */
public class JPersistenceUnitInfo implements PersistenceUnitInfo {

    /**
     * Name of the persistence unit. Corresponds to the &lt;name&gt; element in
     * the persistence.xml file.
     */
    private String persistenceUnitName = null;

    /**
     * Persistence provider implementation class name.
     */
    private String persistenceProviderClassName = null;

    /**
     * Transaction type of the entity managers created by the
     * EntityManagerFactory. The transaction type corresponds to the
     * transaction-type attribute in the persistence.xml file.
     */
    private PersistenceUnitTransactionType transactionType = null;

    /**
     * JTA-enabled data source.
     */
    private DataSource jtaDataSource = null;

    /**
     * The non-JTA-enabled data source.
     */
    private DataSource nonJtaDataSource = null;

    /**
     * JTA-enabled data source name.
     */
    private String jtaDataSourceName = null;

    /**
     * The non-JTA-enabled data source name.
     */
    private String nonJtaDataSourceName = null;

    /**
     * The list of mapping file names that the persistence provider must load to
     * determine the mappings for the entity classes.
     */
    private List<String> mappingFileNames = null;

    /**
     * The list of JAR file URLs that the persistence provider must look in to
     * find the entity classes that must be managed by EntityManagers of this
     * name.
     */
    private List<URL> jarFiles = null;

    /**
     * URL for the jar file or directory that is the root of the persistence
     * unit. (If the persistence unit is rooted in the WEB-INF/classes
     * directory, this will be the URL of that directory.)
     */
    private URL persistenceUnitRootUrl = null;

    /**
     * The list of the names of the classes that the persistence provider must
     * add it to its set of managed classes. Each name corresponds to a named
     * &lt;class&gt; element in the persistence.xml file.
     */
    private List<String> managedClassNames = null;

    /**
     * Whether classes in the root of the persistence unit that have not been
     * explicitly listed are to be included in the set of managed classes. This
     * value corresponds to the &lt;exclude-unlisted-classes&gt; element in the
     * persistence.xml file.
     */
    private boolean excludeUnlistedClasses = false;

    /**
     * Properties object that may contain vendor-specific properties contained
     * in the persistence.xml file.
     */
    private Properties properties = null;

    /**
     * ClassLoader that the provider may use to load any classes, resources, or
     * open URLs.
     */
    private ClassLoader classLoader = null;

    /**
     * URL object that points to the persistence.xml file.
     */
    private URL persistenceXmlFileUrl = null;

    /**
     * Persistence provider (instantiated object).
     */
    private PersistenceProvider persistenceProvider = null;

    /**
     * Shared cache mode.
     */
    private SharedCacheMode sharedCacheMode = null;

    /**
     * Validation mode.
     */
    private ValidationMode validationMode = null;

    /**
     * XML Persistence Schema version.
     */
    private String persistenceXMLSchemaVersion = null;

    /**
     * Default constructor.
     */
    public JPersistenceUnitInfo() {
        this.properties = new Properties();
        this.mappingFileNames = new ArrayList<String>();
        this.managedClassNames = new ArrayList<String>();
        this.jarFiles = new ArrayList<URL>();
    }

    /**
     * Sets the classloader.
     * @param classLoader that the provider may use to load any classes,
     *        resources, or open URLs.
     */
    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * Sets the name of the persistence unit.
     * @param persistenceUnitName the given name
     */
    public void setPersistenceUnitName(final String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    /**
     * @return The name of the Persistence unit that is being created.
     *         Corresponds to the &lt;name&gt; element in persistence.xml
     */
    public String getPersistenceUnitName() {
        return this.persistenceUnitName;
    }

    /**
     * Adds a jar file to the list of JAR file URLs that the persistence
     * provider must look in to find the entity classes that must be managed by
     * EntityManagers of this name.
     * @param jarFile URL of the jar file
     */
    public void addJarFile(final URL jarFile) {
        this.jarFiles.add(jarFile);
    }

    /**
     * Sets the JTA-enabled data source.
     * @param jtaDataSource given datasource.
     */
    public void setJtaDataSource(final DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    /**
     * Sets the non-JTA-enabled data source.
     * @param nonJtaDataSource given datasource.
     */
    public void setNonJtaDataSource(final DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    /**
     * Sets the JTA-enabled data source name.
     * @param jtaDataSourceName given name.
     */
    public void setJtaDataSourceName(final String jtaDataSourceName) {
        this.jtaDataSourceName = jtaDataSourceName;
    }

    /**
     * Sets the non-JTA-enabled data source name.
     * @param nonJtaDataSourceName given name.
     */
    public void setNonJtaDataSourceName(final String nonJtaDataSourceName) {
        this.nonJtaDataSourceName = nonJtaDataSourceName;
    }

    /**
     * Adds a filename to the list of mapping file names.
     * @param mappingFileName name of the mapping file to add.
     */
    public void addMappingFileName(final String mappingFileName) {
        this.mappingFileNames.add(mappingFileName);
    }

    /**
     * Sets the persistence provider implementation class name.
     * @param persistenceProviderClassName name of the class.
     */
    public void setPersistenceProviderClassName(final String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    /**
     * Sets the persistence provider object.
     * @param persistenceProvider the persistence provider object used.
     */
    public void setPersistenceProvider(final PersistenceProvider persistenceProvider) {
        this.persistenceProvider = persistenceProvider;
    }

    /**
     * Sets the URL object that points to the persistence.xml file.
     * @param persistenceXmlFileUrl URL pointing to persistence.xml file
     */
    public void setPersistenceXmlFileUrl(final URL persistenceXmlFileUrl) {
        this.persistenceXmlFileUrl = persistenceXmlFileUrl;
    }

    /**
     * Sets the properties use by this unit.
     * @param properties object with key/value.
     */
    public void setProperties(final Properties properties) {
        this.properties = properties;
    }

    /**
     * @return The name of the persistence provider implementation class.
     *         Corresponds to the &lt;provider&gt; element in persistence.xml
     */
    public String getPersistenceProviderClassName() {
        return this.persistenceProviderClassName;
    }

    /**
     * @return The persistence provider implementation object.
     */
    public PersistenceProvider getPersistenceProvider() {
        return this.persistenceProvider;
    }

    /**
     * @return the JTA-enabled data source to be used by the persistence
     *         provider. The data source corresponds to the named
     *         &lt;jta-data-source&gt; element in persistence.xml
     */
    public DataSource getJtaDataSource() {
        return this.jtaDataSource;
    }

    /**
     * @return The non-JTA-enabled data source to be used by the persistence
     *         provider when outside the container, or inside the container when
     *         accessing data outside the global transaction. The data source
     *         corresponds to the named &lt;non-jta-data-source&gt; element in
     *         persistence.xml
     */
    public DataSource getNonJtaDataSource() {
        return this.nonJtaDataSource;
    }

    /**
     * @return The list of mapping file names that the persistence provider must
     *         load to determine the mappings for the entity classes. The
     *         mapping files must be in the standard XML mapping format, be
     *         uniquely named and be resource-loadable from the application
     *         classpath. This list will not include the entity-mappings.xml
     *         file if one was specified. Each mapping file name corresponds to
     *         a &lt;mapping-file&gt; element in persistence.xml
     */
    public List<String> getMappingFileNames() {
        return this.mappingFileNames;
    }

    /**
     * @return The list of JAR file URLs that the persistence provider must look
     *         in to find the entity classes that must be managed by
     *         EntityManagers of this name. The persistence archive jar itself
     *         will always be the last entry in the list. Each jar file URL
     *         corresponds to a named &lt;jar-file&gt; element in
     *         persistence.xml
     */
    public List<URL> getJarFiles() {
        return this.jarFiles;
    }

    /**
     * @return Properties object that may contain vendor-specific properties
     *         contained in the persistence.xml file. Each property corresponds
     *         to a &lt;property&gt; element in persistence.xml
     */
    public Properties getProperties() {
        return this.properties;
    }

    /**
     * @return ClassLoader that the provider may use to load any classes,
     *         resources, or open URLs.
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * @return URL object that points to the persistence.xml file; useful for
     *         providers that may need to re-read the persistence.xml file. If
     *         no persistence.xml file is present in the persistence archive,
     *         null is returned.
     */
    public URL getPersistenceXmlFileUrl() {
        return this.persistenceXmlFileUrl;
    }

    /**
     * Gets the jta datasource name.
     * @return jta datasource name.
     */
    public String getJtaDataSourceName() {
        return this.jtaDataSourceName;
    }

    /**
     * Gets the non jta datasource name.
     * @return non jta datasource name
     */
    public String getNonJtaDataSourceName() {
        return this.nonJtaDataSourceName;
    }

    /**
     * @return The transaction type of the entity managers created by the
     *         EntityManagerFactory. The transaction type corresponds to the
     *         transaction-type attribute in the persistence.xml file.
     */
    public PersistenceUnitTransactionType getTransactionType() {
        return this.transactionType;
    }

    /**
     * Sets the transaction type of the entity managers created by the
     * EntityManagerFactory.
     * @param transactionType The transaction type corresponds to the
     *        transaction-type attribute in the persistence.xml file.
     */
    public void setTransactionType(final PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    /**
     * @return The list of JAR file URLs that the persistence provider must
     *         examine for managed classes of the persistence unit. Each jar
     *         file URL corresponds to a named &lt;jar-file&gt; element in the
     *         persistence.xml file.
     */
    public List<URL> getJarFileUrls() {
        return this.jarFiles;
    }

    /**
     * Sets the URL for the jar file or directory that is the root of the
     * persistence unit.
     * @param persistenceUnitRootUrl root url of persistence unit
     */
    public void setPersistenceUnitRootUrl(final URL persistenceUnitRootUrl) {
        this.persistenceUnitRootUrl = persistenceUnitRootUrl;
    }

    /**
     * @return The URL for the jar file or directory that is the root of the
     *         persistence unit. (If the persistence unit is rooted in the
     *         WEB-INF/classes directory, this will be the URL of that
     *         directory.)
     */
    public URL getPersistenceUnitRootUrl() {
        return this.persistenceUnitRootUrl;
    }

    /**
     * Adds a class that the persistence provider must add it to its set of
     * managed classes.
     * @param className name of the class
     */
    public void addClass(final String className) {
        this.managedClassNames.add(className);
    }

    /**
     * @return The list of the names of the classes that the persistence
     *         provider must add it to its set of managed classes. Each name
     *         corresponds to a named &lt;class&gt; element in the
     *         persistence.xml file.
     */
    public List<String> getManagedClassNames() {
        return this.managedClassNames;
    }

    /**
     * Sets the boolean defining if the persistence unit that have not been
     * explicitly listed are to be included in the set of managed classes.
     * @param excludeUnlistedClasses true/false
     */
    public void setExcludeUnlistedClasses(final boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    /**
     * @return Whether classes in the root of the persistence unit that have not
     *         been explicitly listed are to be included in the set of managed
     *         classes. This value corresponds to the
     *         &lt;exclude-unlisted-classes&gt; element in the persistence.xml
     *         file.
     */
    public boolean excludeUnlistedClasses() {
        return this.excludeUnlistedClasses;
    }

    /**
     * Add a transformer supplied by the provider that will be called for every
     * new class definition or class redefinition that gets loaded by the loader
     * returned by the PersistenceInfo.getClassLoader method. The transformer
     * has no effect on the result returned by the
     * PersistenceInfo.getTempClassLoader method. Classes are only transformed
     * once within the same classloading scope, regardless of how many
     * persistence units they may be a part of.
     * @param transformer A provider-supplied transformer that the Container
     *        invokes at class-(re)definition time
     */
    public void addTransformer(final ClassTransformer transformer) {
        if (this.classLoader instanceof EZBClassLoader) {
            EZBClassLoader currentCL = (EZBClassLoader) this.classLoader;
            currentCL.addTransformer(transformer);
            return;
        }
        throw new IllegalStateException("Cannot add the given transformer as ClassLoader is not an EasyBeans classloader");
    }

    /**
     * Return a new instance of a ClassLoader that the provider may use to
     * temporarily load any classes, resources, or open URLs. The scope and
     * classpath of this loader is exactly the same as that of the loader
     * returned by PersistenceInfo.getClassLoader. None of the classes loaded by
     * this class loader will be visible to application components. The
     * container does not use or maintain references to this class loader after
     * returning it to the provider.
     * @return Temporary ClassLoader with same visibility as current loader
     */
    public ClassLoader getNewTempClassLoader() {
        if (this.classLoader instanceof EZBClassLoader) {
            EZBClassLoader currentCL = (EZBClassLoader) this.classLoader;
            return currentCL.duplicate();
        }
        // else, try to see if it's an URL classLoader
        if (this.classLoader instanceof URLClassLoader) {
            return new URLClassLoader(((URLClassLoader) this.classLoader).getURLs(), this.classLoader.getParent());
        }

        throw new IllegalStateException("Cannot build a new temporary classloader");
    }

    /**
     * Returns the schema version of the persistence.xml file.
     * @return persistence.xml schema version
     */
    public String getPersistenceXMLSchemaVersion() {
        return this.persistenceXMLSchemaVersion;
    }

    /**
     * Sets the schema version of the persistence.xml file.
     * @param persistenceXMLSchemaVersion the persistence.xml schema version
     */
    public void setPersistenceXMLSchemaVersion(final String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    /**
     * Returns the specification of how the provider must use
     * a second-level cache for the persistence unit.
     * The result of this method corresponds to the shared-cache-mode
     * element in the persistence.xml file.
     * @return the second-level cache mode that must be used by the
     * provider for the persistence unit
     */
    public SharedCacheMode getSharedCacheMode() {
        return this.sharedCacheMode;
    }

    /**
     * Sets the specification of how the provider must use
     * a second-level cache for the persistence unit.
     * The result of this method corresponds to the shared-cache-mode
     * element in the persistence.xml file.
     * provider for the persistence unit
     * @param sharedCacheMode the given shared cache mode
     */
    public void setSharedCacheMode(final SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    /**
     * Returns the validation mode to be used by the persistence
     * provider for the persistence unit. The validation mode
     * corresponds to the validation-mode element in the
     * persistence.xml file.
     * @return the validation mode to be used by the
     * persistence provider for the persistence unit
     */
    public ValidationMode getValidationMode() {
        return this.validationMode;
    }


    /**
     * Sets the validation mode to be used by the persistence
     * provider for the persistence unit. The validation mode
     * corresponds to the validation-mode element in the
     * persistence.xml file.
     * @param validationMode the validation mode to be used by the
     * persistence provider for the persistence unit
     */
    public void setValidationMode(final ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

}
