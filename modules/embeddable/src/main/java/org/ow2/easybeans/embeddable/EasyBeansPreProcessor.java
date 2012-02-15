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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.embeddable;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJBException;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.server.Embedded;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;

/**
 * This class allows to initialize classes for EJBs found in the current classpath by enhancing all EJB classes.
 * @author Florent Benoit
 */
public final class EasyBeansPreProcessor {

    /**
     * Utility class.
     */
    private EasyBeansPreProcessor() {

    }

    /**
     * Calls of the main method.
     * @param args the given arguments
     * @throws Exception on failures
     */
    public static void main(final String[] args) throws Exception {

        if (args == null || args.length < 1) {
            throw new IllegalArgumentException("This class needs to be called with a classname as argument");
        }

        // Needs to analyze the CLASSPATH of the JVM
        String classpath = System.getProperty("java.class.path");
        String[] classpathElements = classpath.split(File.pathSeparator);

        // Embedded Instance
        Embedded embedded = new Embedded();

        // Build a list of EJB3 container if some are found
        List<EZBContainer> containers = new ArrayList<EZBContainer>();

        if (classpathElements != null) {
            for (String classpathElement : classpathElements) {

                // Get archive (file, directory) for the given element
                IArchive archive = ArchiveManager.getInstance().getArchive(new File(classpathElement));

                // Scan the archive
                IDeployable<?> deployable;
                try {
                    deployable = DeployableHelper.getDeployable(archive);
                } catch (DeployableHelperException e) {
                    throw new EJBException("Cannot get a deployable for the archive '" + archive + "'", e);
                }

                if (deployable instanceof EJB3Deployable) {
                    containers.add(embedded.createContainer(deployable));
                }
            }
        }

        // Enhance all the containers
        for (EZBContainer container : containers) {
            container.setClassLoader(Thread.currentThread().getContextClassLoader());
            container.resolve();
            container.enhance(false);
        }


        // Call the wrapper class
        String className = args[0];
        Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(className);
        Method mainMethod = c.getMethod("main", String[].class);

        List<String> remainingArgs = new ArrayList<String>();
        for (int i = 1; i < args.length; i++) {
            remainingArgs.add(args[i]);
        }

        mainMethod.invoke(null, (Object) remainingArgs.toArray(new String[remainingArgs.size()]));

    }

}
