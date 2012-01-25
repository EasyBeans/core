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
 * $Id: AbsInheritedParametersMojo.java 6143 2012-01-25 14:15:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.maven.plugin.osgi;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.project.MavenProject;
import org.ow2.util.maven.osgi.launcher.core.AbsMojo;
import org.ow2.util.maven.osgi.launcher.core.Debug;
import org.ow2.util.maven.osgi.launcher.core.Module;

/**
 * Allows to inherit parameters by using "properties" keyword on parameters.
 * @author Florent Benoit
 */
public abstract class AbsInheritedParametersMojo extends AbsMojo {


    /**
     * Add the parameter for inheritance with property name = attribute name.
     * @parameter property="fork"
     */
    private boolean forkIgnoreThisAttribute = false;

    /**
     * Add the parameter for inheritance with property name = attribute name
     * Path of the build directory.
     * @parameter expression="${project.build.directory}"
     *            property="buildDirectory"
     */
    private File buildDirectoryIgnoreThisAttribute;

    /**
     * Artifact Factory used to create Artifacts.
     * @component
     */
    private ArtifactFactory innerArtifactFactory;

    /**
     * Artifact Resolver used to resolve artifacts.
     * @component
     */
    private ArtifactResolver innerArtifactResolver;

    /**
     * The maven project.
     * @parameter expression="${project}" property="project"
     * @required
     * @readonly
     */
    private MavenProject projectIgnoreThisAttribute;

    /**
     * Local Maven repository used by the Artifact Resolver.
     * @parameter expression="${localRepository}" property="localRepository"
     */
    private ArtifactRepository localRepositoryIgnoreThisAttribute;

    /**
     * List of Maven repositories (remote) used by the Artifact Resolver.
     * @parameter expression="${project.remoteArtifactRepositories}"
     *            property="repositories"
     */
    private List<?> repositoriesIgnoreThisAttribute;

    /**
     * List of directories that contains bundles.
     * @parameter property="bundleDirectories"
     * @optional
     */
    private File[] bundleDirectoriesIgnoreThisAttribute = null;

    /**
     * Debug the forked JVM for the start of the framework ? (default is false)
     * @parameter property="debug"
     * @optional
     */
    private Debug debugIgnoreThisAttribute = new Debug();

    /**
     * List of Modules that needs to be installed/started.
     * @parameter property="modules"
     * @optional
     */
    private Module[] modulesIgnoreThisAttribute;

    /**
     * List of artifacts used as dependencies for the launcher.
     * @parameter property="frameworkDependencies"
     * @optional
     */
    private org.ow2.util.maven.osgi.launcher.core.Dependency[] frameworkDependenciesIgnoreThisAttribute;

    /**
     * Configuration file.
     * @parameter property="configurationFile"
     * @optional
     */
    private File configurationFileIgnoreThisAttribute = null;

    /**
     * Bundle configuration file (define bootstrap bundles).
     * @parameter property="bundleConfigurationFile"
     * @optional
     */
    private File bundleConfigurationFileIgnoreThisAttribute = null;

    /**
     * An ordered list of Properties files containing elements of the
     * configuration. They will be merged and resolved together to obtain the
     * final configuration.
     * @parameter property="configurationsFiles"
     * @optional
     */
    private File[] configurationsFilesIgnoreThisAttribute;

    /**
     * JVM Properties (System properties to set).
     * @parameter property="jvmProperties"
     * @optional
     */
    private Properties jvmPropertiesIgnoreThisAttribute;

    /**
     * List of bundles to stop in the given order (by their symbolic name).
     * @parameter property="stopOrderBundles"
     * @optional
     */
    private String[] stopOrderBundlesIgnoreThisAttribute = null;

    /**
     * List of bundles to ignore (by their symbolic name).
     * @parameter property="excludedBundles"
     * @optional
     */
    private String[] excludedBundlesIgnoreThisAttribute = null;

    /**
     * List of path for the user Classpath.
     * @parameter property="userClasspath"
     * @optional
     */
    private String[] userClasspathIgnoreThisAttribute = null;

    /**
     * Waiting value after the stop.
     * @parameter property="waitAfterStop"
     * @optional
     */
    private long waitAfterStopIgnoreThisAttribute = 0;

    /**
     * Waiting value after the stop.
     * @parameter property="waitAfterStart"
     * @optional
     */
    protected long waitAfterStart = 0;

    /**
     * Init.
     */
    protected void init() {
        this.artifactFactory = this.innerArtifactFactory;
        this.artifactResolver = this.innerArtifactResolver;
    }

}
