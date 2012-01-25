/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: PersistenceManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.persistence;

import java.io.File;
import java.util.List;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.ow2.easybeans.mavenplugin.server.IPersistenceListener;
import org.ow2.easybeans.mavenplugin.server.PersistenceSupport;
import org.ow2.util.maven.plugin.deployment.maven.MavenArtifactResolver;
import org.ow2.util.maven.plugin.deployment.maven.MavenLog;
import org.ow2.util.maven.plugin.deployment.maven.MavenPluginClassloader;


/**
 * Class for managing persistence dependencies.
 * @author Vincent Michaud
 */
public final class PersistenceManager {

    /**
     * Determine dependencies already added to the plugin classloader.
     */
    private boolean[] alreadyAdded = new boolean[PersistenceSupport.getSupportedPersistenceProviders()];

    /**
     * Number of dependencies added.
     */
    private int nbAdded = 0;

    /**
     * Determine if all dependencies are added.
     */
    private boolean allAdded = false;

    /**
     * The unique instance of the class.
     */
    private static final PersistenceManager INSTANCE = new PersistenceManager();

    /**
     * Private constructor. This class must have one instance only,
     * in the case of Maven multi-modules projects.
     */
    private PersistenceManager() {
        for (int i = 0; i < alreadyAdded.length; i++) {
            alreadyAdded[i] = false;
        }
    }

    /**
     * Get the instance of persistence manager.
     * @return The unique instance
     */
    public static PersistenceManager getInstance() {
        return INSTANCE;
    }

    /**
     * Add to the plugin classloader a persistence dependency.
     * @param index The index of the persistence provider
     * @param resolver An artifact resolver
     * @param repositories Repositories where dependencies are checked
     */
    private void loadPersistenceDependency(final int index, final MavenArtifactResolver resolver,
                                           final List<ArtifactRepository> repositories) {
        String artifactId, groupId, version, type;
        groupId = PersistenceSupport.getDependencies(index, PersistenceSupport.GROUP_ID);
        artifactId = PersistenceSupport.getDependencies(index, PersistenceSupport.ARTIFACT_ID);
        version = PersistenceSupport.getDependencies(index, PersistenceSupport.VERSION);
        type = PersistenceSupport.getDependencies(index, PersistenceSupport.TYPE);
        List<File> dependencies = resolver.resolveTransitivelyFiles(groupId, artifactId, version,
                                                                    type, null, repositories);
        for (File file : dependencies) {
            MavenLog.getLog().debug("Added to the classloader : " + file.getAbsolutePath());
        }
        MavenPluginClassloader.getInstance().addJarResources(dependencies);
    }

    /**
     * Add all supported persistence dependencies.
     * @param resolver An artifact resolver
     */
    public void loadAllDependencies(final MavenArtifactResolver resolver) {
        if (!this.allAdded) {
            List<ArtifactRepository> repositories = resolver.getPluginRepositories();
            int nbSupported = PersistenceSupport.getSupportedPersistenceProviders();

            for (int index = 0; index < nbSupported; index++) {
                if (!this.alreadyAdded[index]) {
                    loadPersistenceDependency(index, resolver, repositories);
                }
            }
            this.allAdded = true;
            this.nbAdded = nbSupported;
        }
    }

    /**
     * Get a persistence listener which load all missing
     * persistence providers dependencies.
     * @return A new persistence listener
     */
    public IPersistenceListener getPersistenceListener(final MavenArtifactResolver resolver) {
        return new IPersistenceListener() {
            public void reportRequestedPersistenceProviders(final List<Integer> persistenceProvidersImpl) {
                if (!allAdded) {
                    List<ArtifactRepository> repositories = resolver.getPluginRepositories();
                     for (int index : persistenceProvidersImpl) {
                         MavenLog.getLog().debug("Persistence provider detected : "
                                               + PersistenceSupport.getImplementation(index));

                         if (!alreadyAdded[index]) {
                             alreadyAdded[index] = true;
                             nbAdded++;
                             if (nbAdded == PersistenceSupport.getSupportedPersistenceProviders()) {
                                 allAdded = true;
                             }
                             loadPersistenceDependency(index, resolver, repositories);
                         }
                    }
                }
            }
        };
    }
}
