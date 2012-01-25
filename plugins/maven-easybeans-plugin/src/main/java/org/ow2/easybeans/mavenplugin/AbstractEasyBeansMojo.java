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
 * $Id: AbstractEasyBeansMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import java.util.List;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentMojo;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentCore;
import org.ow2.util.maven.plugin.deployment.mapping.Deployables;


/**
 * Abstract base class managing additional deployed archives specified in the POM.
 * @author Vincent Michaud
 */
public abstract class AbstractEasyBeansMojo extends AbstractMojo implements Contextualizable, IDeploymentMojo {

    /****************************************************/
    /*                   Maven Parameters               */
    /****************************************************/

    /**
     * The local repository.
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;

    /**
     * The current maven project.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * All Maven projects.
     * @parameter expression="${reactorProjects}"
     * @required
     * @readonly
     */
    private List<MavenProject> reactorProjects;

    /**
     * All additional deployables of current project.
     * @parameter
     */
    private Deployables deployables;

    /**
     * The group ID of the plugin.
     * @parameter expression="${plugin.groupId}"
     * @readonly
     * @required
     */
    private String pluginGroupId;

    /**
     * The artifact ID of the plugin.
     * @parameter expression="${plugin.artifactId}"
     * @readonly
     * @required
     */
    private String pluginArtifactId;

    /**
     * The version of the plugin.
     * @parameter expression="${plugin.version}"
     * @readonly
     * @required
     */
    private String pluginVersion;


    /****************************************************/
    /*                   Maven Components               */
    /****************************************************/

    /**
     * The component used to resolve additional artifacts required.
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * The component used to create artifact instances.
     * @component
     */
    private ArtifactFactory artifactFactory;

   /**
    * The component used to get metadata.
    * @component
    */
    private ArtifactMetadataSource metadataSource;

    /**
     * Project builder.
     * @component
     */
    private MavenProjectBuilder projectBuilder;


    /****************************************************/
    /*                    Plugin fields                 */
    /****************************************************/

    /**
     * The core of the mojo.
     */
    private IDeploymentCore core = null;

    /**
     * The context of the plugin.
     */
    private Context plexusContext = null;
    
    /****************************************************/
    /*         IDeploymentMojo implementation           */
    /****************************************************/

    /**
     * Access to the Maven project.
     * @return The current Maven project
     */
    public MavenProject getProject() {
        return project;
    }

    /**
     * Access to reactor projects.
     * @return Current reactor projects
     */
    public List<MavenProject> getReactorProjects() {
        return reactorProjects;
    }

    /**
     * Access to the additional deployables of the project.
     * @return Additional deployables
     */
    public Deployables getAdditionalDeployables() {
        return this.deployables;
    }

    /**
     * Get the local repository.
     * @return The local repository
     */
    public ArtifactRepository getLocalRepository() {
        return localRepository;
    }

    /**
     * Get the artifact ID of the plugin.
     * @return The artifact ID
     */
    public String getPluginArtifactId() {
        return this.pluginArtifactId;
    }

    /**
     * Get the group ID of the plugin.
     * @return The group ID
     */
    public String getPluginGroupId() {
        return this.pluginGroupId;
    }

    /**
     * Get the version of the plugin.
     * @return The version
     */
    public String getPluginVersion() {
        return this.pluginVersion;
    }

    /**
     * Get the component used to resolve additional artifacts required.
     * @return The artifact resolver
     */
    public ArtifactResolver getArtifactResolver() {
        return this.artifactResolver;
    }

    /**
     * Get the component used to create artifact instances.
     * @return The artifact factory
     */
    public ArtifactFactory getArtifactFactory() {
        return this.artifactFactory;
    }

   /**
    * Get the component used to get metadata.
    * @return The metadata source
    */
    public ArtifactMetadataSource getMetadataSource() {
        return this.metadataSource;
    }

    /**
     * Get the component used to build Maven project.
     * @return The project builder
     */
    public MavenProjectBuilder getProjectBuilder() {
        return this.projectBuilder;
    }


    /****************************************************/
    /*                 Members functions                */
    /****************************************************/

    /**
     * Execute the mojo.
     * @throws MojoExecutionException Execution error
     */
    public void execute() throws MojoExecutionException {
        core = defineMojoCore();
        core.start();
        core = null;
    }

    /**
     * Called before the execution of the mojo.
     * Initialize the plugin classloader.
     * @param context The Plexus context
     */
    public void contextualize(final Context context) {
        this.plexusContext = context;
    }

    /**
     * Get the plexus context of the plugin.
     * @return The context
     */
    public Context getPlexusPluginContext() {
        return this.plexusContext;
    }

    /**
     * Get the core of the mojo.
     * @return The core, or null if the core is not initialized
     */
    protected IDeploymentCore getCore() {
        return this.core;
    }

    /**
     * Define the core of the mojo.
     * @return The core of the mojo
     * @throws MojoExecutionException Execution error
     */
    protected abstract IDeploymentCore defineMojoCore() throws MojoExecutionException;
}
