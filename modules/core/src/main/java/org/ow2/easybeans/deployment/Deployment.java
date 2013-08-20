/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: Deployment.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.annotations.helper.ResolverHelper;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMetadataFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.ee.metadata.ejbjar.api.exceptions.EJBJARMetadataException;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.IClassesLocator;
import org.ow2.util.scan.api.IScanner;
import org.ow2.util.scan.api.classlocator.ArchiveClassesLocator;
import org.ow2.util.scan.api.classlocator.ClassLoaderClassesLocator;
import org.ow2.util.scan.impl.ASMScannerImpl;
import org.ow2.util.url.URLUtils;

/**
 * This class will parse given ejb-jar file and completes metadata by using
 * resolver.
 * @author Florent Benoit
 *         Contributors:
 *             S. Ali Tokmen (added support for prefixes)
 */
public class Deployment {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(Deployment.class);

    /**
     * Archive which will be analyzed.
     */
    private IArchive archive = null;

    /**
     * Extra archives.
     */
    private List<IArchive> extraArchives = null;

    /**
     * Metadata of configurator.
     */
    private EjbJarArchiveMetadata ejbJarArchiveMetadata;

    /**
     * EasyBeans deployment configuration.
     */
    private EZBContainerConfig configuration;

    /**
     * Scanner.
     */
    private final IScanner scanner;

    /**
     * Constructor.
     * @param archive Archive to deploy using the default deployment
     *                configuration.
     */
    public Deployment(final IArchive archive) {
        this.archive = archive;
        this.scanner = new ASMScannerImpl();
        reset();
    }

    /**
     * Constructor.
     * @param configuration Deployment configuration.
     */
    public Deployment(final EZBContainerConfig configuration) {
        this(configuration.getArchive());
        this.configuration = configuration;
    }

    /**
     * Build a new Annotation Deployment Analyzer.
     */
    public void reset() {
        this.ejbJarArchiveMetadata = null;
    }

    /**
     * Analyzes the jarFile.
     * @throws EJBJARMetadataException  if scan fails
     * @throws ResolverException if resolver fails.
     */
    public void analyze() throws EJBJARMetadataException, ResolverException  {
        analyze(null);
    }

    /**
     * Analyzes the jarFile.
     * @param classLoader a classloader to use to find classes outside archive
     * @throws EJBJARMetadataException  if scan fails
     * @throws ResolverException if resolver fails.
     */
    @SuppressWarnings("boxing")
    public void analyze(final ClassLoader classLoader) throws EJBJARMetadataException, ResolverException  {


        if (this.configuration.getModuleName() == null) {
            //TODO: could be configured through XML
            try {
                String shortName = URLUtils.shorterName(this.archive.getURL());
                int dotPos = shortName.lastIndexOf('.');
                if (dotPos != -1) {
                    shortName = shortName.substring(0, dotPos);
                }
                this.configuration.setModuleName(shortName);
            } catch (ArchiveException e) {
                throw new IllegalArgumentException("Unable to shorter URL '" + this.archive.getName() + "'.", e);
            }
        }

        // Create metadata
        EasyBeansEjbJarMetadataFactory metadataFactory = getEjbJarMetadataFactory(scanner);

        // TODO gaellalire : metadata should be contruct directly with ear
        List<IClassesLocator> lib = null;

        if (this.extraArchives != null) {
            lib = new ArrayList<IClassesLocator>();
            for (IArchive archive : this.extraArchives) {
                lib.add(new ArchiveClassesLocator(archive));
            }
        }
        if (classLoader != null) {
            if (lib == null) {
                lib = new ArrayList<IClassesLocator>();
            }
            lib.add(new ClassLoaderClassesLocator(classLoader));
        }

        IEjbJarMetadata ejbArchiveMetadata = metadataFactory.createArchiveMetadata(archive, lib);

        this.ejbJarArchiveMetadata = ejbArchiveMetadata.as(EjbJarArchiveMetadata.class);

        // Complete metadata
        long tResolverStart = System.currentTimeMillis();
        ResolverHelper.resolve(this.ejbJarArchiveMetadata, this.configuration.getEZBServer());
        // time if debugging
        if (logger.isDebugEnabled()) {
            long tResolverEnd = System.currentTimeMillis();
            if (logger.isDebugEnabled()) {
                logger.debug("Resolver on metadata from {0} took {1} ms.'", this.archive.getName(),
                        (tResolverEnd - tResolverStart));
            }
        }

        if (logger.isDebugEnabled()) {
            for (EasyBeansEjbJarClassMetadata classAnnotationMetadata : this.ejbJarArchiveMetadata
                    .getEjbJarClassMetadataCollection()) {
                logger.debug("Result for class = " + classAnnotationMetadata);
            }
        }
    }

    /**
     * @return the archive metadata
     */
    public EjbJarArchiveMetadata getEjbJarArchiveMetadata() {
        return this.ejbJarArchiveMetadata;
    }

    /**
     * Add extra archives for finding classes.
     * @param extraArchives the given archives.
     */
    public void setExtraArchives(final List<IArchive> extraArchives) {
        this.extraArchives = extraArchives;
    }

    /**
     * @return the archive of this deployment.
     */
    public IArchive getArchive() {
        return this.archive;
    }

    /**
     * @return the EasyBeans deployment configuration, will be null if the
     *         {@link Deployment#Deployment(IArchive)} constructor has been
     *         used.
     */
    public EZBContainerConfig getConfiguration() {
        return this.configuration;
    }

    /**
     * @return Deployable factory.
     */
    protected EasyBeansEjbJarMetadataFactory getEjbJarMetadataFactory(IScanner scanner) {
        // Allows to override this factory
        return new EasyBeansEjbJarMetadataFactory(scanner);
    }

    /**
     * Set archive metadata.
     * @param ejbJarArchiveMetadata meta data
     */
    protected void setEjbJarArchiveMetadata(final EjbJarArchiveMetadata ejbJarArchiveMetadata) {
        this.ejbJarArchiveMetadata = ejbJarArchiveMetadata;
    }

    /**
     * Get extra archives for finding classes.
     * @return archives
     */
    protected List<IArchive> getExtraArchives() {
        return this.extraArchives;
    }


}
