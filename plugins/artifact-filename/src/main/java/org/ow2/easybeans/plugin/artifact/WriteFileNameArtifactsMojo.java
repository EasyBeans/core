/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
 * Contact: easybeans@ow2.org
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
 *
 * --------------------------------------------------------------------------
 * $Id: WriteFileNameArtifactsMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.plugin.artifact;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.DependencyManagement;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * Dump the name of the given Artifacts into a file.
 * @goal resolve
 * @phase compile
 * @author Florent Benoit
 */
public class WriteFileNameArtifactsMojo extends AbstractMojo {

    /**
     * Artifact Factory used to create Artifacts.
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * Artifact Resolver used to resolve artifacts.
     * @component
     */
    private ArtifactResolver artifactResolver;

    /**
     * Local Maven repository used by the Artifact Resolver.
     * @parameter expression="${localRepository}"
     */
    private ArtifactRepository localRepository;

    /**
     * List of Maven repositories (remote) used by the Artifact Resolver.
     * @parameter expression="${project.remoteArtifactRepositories}"
     */
    private List<?> repositories;

    /**
     * Path of the build directory.
     * @parameter expression="${project.build.directory}"
     */
    private File buildDirectory;

    /**
     * Name of the file where to print the list of the files.
     * @parameter
     */
    private File outputFile;

    /**
     * List of Modules that needs to be printed in a file.
     * @parameter
     */
    private Module[] modules;

    /**
     * The maven project.
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Execute the Maven plugin.
     * @throws MojoExecutionException if the file is not generated.
     */
    @SuppressWarnings("unchecked")
    public void execute() throws MojoExecutionException {

        // No parameter, use default
        if (this.outputFile == null) {
            this.outputFile = new File(this.buildDirectory, "list-of-files.txt");
        }

        // Create directory if not existing
        this.outputFile.getParentFile().mkdirs();

        // Build artifacts from modules
        if (this.modules == null) {
            throw new MojoExecutionException("No modules element has been set");
        }

        // Create list
        List<Artifact> artifacts = new ArrayList<Artifact>();

        // for each module, build artifact.
        for (Module module : this.modules) {
            String groupId = module.getGroupId();
            String artifactId = module.getArtifactId();
            String type = module.getType();
            String version = module.getVersion();
            String classifier = module.getClassifier();

            if (groupId == null) {
                throw new MojoExecutionException("No groupdId set for module '" + module + "'");
            }
            if (artifactId == null) {
                throw new MojoExecutionException("No artifactId set for module '" + module + "'");
            }

            if (type == null) {
                getLog().debug("No type set for module '" + module + "', assume it is jar type");
                type = "jar";
                module.setType(type);
            }

            if (version == null) {
                // try to find version with dependency management
                DependencyManagement dependencyManagement = this.project.getDependencyManagement();

                if (dependencyManagement != null) {
                    // try to find a version for this component
                    List<Dependency> dependencies = dependencyManagement.getDependencies();
                    if (dependencies != null) {
                        for (Dependency dependency : dependencies) {
                            getLog().debug("Dependency = " + dependency);
                            if (groupId.equals(dependency.getGroupId()) && artifactId.equals(dependency.getArtifactId())
                                    && type.equals(dependency.getType())) {
                                getLog().debug("Using version of dependency management" + dependency.getVersion());
                                version = dependency.getVersion();
                                break;
                            }
                        }
                    }
                }
                if (version == null) {
                    throw new MojoExecutionException("No version set for module '" + module + "'");
                }
            }
            Artifact artifact = null;
            if (classifier != null) {
                artifact = this.artifactFactory.createArtifactWithClassifier(groupId, artifactId, version, type, classifier);
            } else {
                artifact = this.artifactFactory.createBuildArtifact(groupId, artifactId, version, type);
            }
            getLog().debug("Built Artifact = " + artifact);

            // now resolve this artifact
            try {
                this.artifactResolver.resolve(artifact, this.repositories, this.localRepository);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Cannot resolve artifact '" + artifactId + "'.", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException("Artifact '" + artifactId + "' not found.", e);
            }

            // Add artifact
            artifacts.add(artifact);

        }

        FileWriter fileWriter = null;
        try {
            // Create file writer
            try {
                fileWriter = new FileWriter(this.outputFile);
            } catch (IOException e) {
                throw new MojoExecutionException("Cannot create a file on '" + this.outputFile + "'", e);
            }

            // Write the name of the files
            for (Artifact artifact : artifacts) {
                try {
                    getLog().debug("Writing " + artifact.getFile().getName());
                    fileWriter.write(artifact.getFile().getName());
                    fileWriter.write("\n");
                } catch (IOException e) {
                    throw new MojoExecutionException("Cannot write the line for Artifact '" + artifact + "' in the file '"
                            + this.outputFile + "'", e);
                }
            }
        } finally {
            // close file writer
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    getLog().debug("Cannot close the file writer", e);
                }
            }
        }
    }

    /**
     * @return the artifactFactory
     */
    public ArtifactFactory getArtifactFactory() {
        return this.artifactFactory;
    }

    /**
     * @param artifactFactory the artifactFactory to set
     */
    public void setArtifactFactory(final ArtifactFactory artifactFactory) {
        this.artifactFactory = artifactFactory;
    }

    /**
     * @return the artifactResolver
     */
    public ArtifactResolver getArtifactResolver() {
        return this.artifactResolver;
    }

    /**
     * @param artifactResolver the artifactResolver to set
     */
    public void setArtifactResolver(final ArtifactResolver artifactResolver) {
        this.artifactResolver = artifactResolver;
    }

    /**
     * @return the localRepository
     */
    public ArtifactRepository getLocalRepository() {
        return this.localRepository;
    }

    /**
     * @param localRepository the localRepository to set
     */
    public void setLocalRepository(final ArtifactRepository localRepository) {
        this.localRepository = localRepository;
    }

    /**
     * @return the repositories
     */
    public List<?> getRepositories() {
        return this.repositories;
    }

    /**
     * @param repositories the repositories to set
     */
    public void setRepositories(final List<?> repositories) {
        this.repositories = repositories;
    }

    /**
     * @return the buildDirectory
     */
    public File getBuildDirectory() {
        return this.buildDirectory;
    }

    /**
     * @param buildDirectory the buildDirectory to set
     */
    public void setBuildDirectory(final File buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    /**
     * @param modules the modules to set
     */
    public void setModules(final Module[] modules) {
        if (modules != null) {
            this.modules = modules.clone();
        }
    }

    /**
     * @return the project
     */
    public MavenProject getProject() {
        return this.project;
    }

    /**
     * @param project the project to set
     */
    public void setProject(final MavenProject project) {
        this.project = project;
    }
}
