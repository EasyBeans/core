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
 * $Id: EasyBeansStopOSGiMojo.java 6143 2012-01-25 14:15:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.maven.plugin.osgi;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.ow2.util.maven.osgi.launcher.api.LauncherException;

/**
 * Allows to start EasyBeans OSGi.
 *
 * @author Florent Benoit
 * @author Loic Albertin (Maven 3 plugin migration)
 */
@Mojo(name = "stop", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class EasyBeansStopOSGiMojo extends AbsEasyBeansOSGiMojo {

    /**
     * Stop the OSGi framework.
     *
     * @throws MojoExecutionException if there is a failure.
     */
    @Override
    protected void doExecute() throws MojoExecutionException {

        // Stop the framework with the given configuration
        try {
            getLauncher().stop();
        } catch (LauncherException e) {
            throw new MojoExecutionException("Cannot launch the framework", e);
        }
    }


}
