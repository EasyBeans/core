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
 * $Id: AbstractEasyBeansBoundMojo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentBoundMojo;
import org.ow2.util.maven.plugin.deployment.api.IDeploymentCore;
import org.ow2.util.maven.plugin.deployment.core.BoundDeploymentCore;
import org.ow2.util.maven.plugin.deployment.core.Configuration;



/**
 * Class representing goal which must be bound to the Maven project lifecycle.
 * This mojo provide a simple deployment of the current project resource.
 * @author Vincent Michaud
 */
public abstract class AbstractEasyBeansBoundMojo extends AbstractEasyBeansMojo implements IDeploymentBoundMojo {

    /**
     * Define internal configuration of the goal.
     * @throws MojoExecutionException Execution error
     */
    public Configuration defineConfiguration() throws MojoExecutionException {
        String[] packaging = { "ejb", "ejb3", "ear" };
        String[] extension = { "jar", "ear" };
        return new Configuration(true, false, packaging, extension);
    }

    /**
     * Define the core of the mojo.
     * @return The core of the mojo
     * @throws MojoExecutionException Execution error
     */
    public IDeploymentCore defineMojoCore() throws MojoExecutionException {
        return new BoundDeploymentCore(this);
    }
}


