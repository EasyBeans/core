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
 * $Id: EasyBeansTestOSGiMojo.java 6143 2012-01-25 14:15:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.maven.plugin.osgi;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.ow2.util.maven.osgi.launcher.api.LauncherException;
import org.ow2.util.maven.osgi.launcher.core.Configuration;

/**
 * Allows to start EasyBeans OSGi.
 *
 * @author Florent Benoit
 * @author Loic Albertin (Maven 3 plugin migration)
 */
@Mojo(name = "test", defaultPhase = LifecyclePhase.TEST)
public class EasyBeansTestOSGiMojo extends EasyBeansStartOSGiMojo {

    /**
     * Start the OSGi framework.
     *
     * @throws MojoExecutionException if there is a failure.
     */
    @Override
    protected void doExecute() throws MojoExecutionException {

        // Launch the framework with the given configuration
        try {
            getLauncher().start();
        } catch (LauncherException e) {
            throw new MojoExecutionException("Cannot launch the framework", e);
        }

    }


    /**
     * Gets the configuration used by the launcher/manager.
     *
     * @return a configuration object
     *
     * @throws MojoExecutionException if the configuration can't be built.
     */
    @Override
    protected Configuration getConfiguration() throws MojoExecutionException {
        Configuration configuration = super.getConfiguration();

        // Once all has been runned, stop the framework
        configuration.setShutdownAfterStart(true);

        // No wait (it waits the end of the framework)
        configuration.setWaitAfterStart(0L);

        // Needs to wait the end of the process as we don't have pre-post
        // integration phase in this case
        configuration.setWaitEndProcess(true);

        //configuration.
        return configuration;
    }


}
