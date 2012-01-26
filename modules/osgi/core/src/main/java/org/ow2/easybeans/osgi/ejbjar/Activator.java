/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: Activator.java 6096 2012-01-16 16:50:54Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.ejbjar;

import java.net.URL;
import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.ow2.easybeans.api.EZBConfigurationExtension;
import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.EZBContainerException;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.container.JContainer3;
import org.ow2.easybeans.container.JContainerConfig;
import org.ow2.easybeans.osgi.util.LDAPFilter;
import org.ow2.easybeans.util.osgi.BCMapper;
import org.ow2.easybeans.server.Embedded;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This should be a Generic EJB3.0 ActivatorWithBackedClassLoader.
 */
public class Activator implements BundleActivator {

    /**
     * Root of the bundle.
     */
    private static final String ROOT = "/";

    /**
     * Logger.
     */
    private final Log logger = LogFactory.getLog(Activator.class);

    /**
     * The local JContainer instance (ie the EjbJar).
     */
    private EZBContainer container = null;

    /**
     * BundleContext.
     */
    private BundleContext bc = null;

    /**
     * The Embedded listener.
     */
    private ServiceListener listener;

    /**
     * ServiceRegistration object.
     */
    private ServiceRegistration serviceReg = null;

    /**
     * Register ServiceListener and start the COntainer if an Embedded instance is available. {@inheritDoc}
     *
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    public void start(final BundleContext bc) throws Exception {
        this.bc = bc;
        this.listener = new EmbeddedServiceListener(this);

        String filter = LDAPFilter.createLDAPFilter(Embedded.class.getName());
        bc.addServiceListener(this.listener, filter);

        // first init, try to get the Embedded reference
        ServiceReference sr = bc.getServiceReference(Embedded.class.getName());
        if (sr != null) {
            // and use it only if the service is available
            startContainer(sr);
        }
    }

    /**
     * Unregister ServiceListener and stop the Container. {@inheritDoc}
     *
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    public void stop(final BundleContext bc) throws Exception {
        bc.removeServiceListener(this.listener);
        // Stop the container if any
        stopContainer();
    }

    /**
     * Start the EZBContainer and register the instance as an OSGi Service.
     *
     * @param sr
     *            The Embedded ServiceReference.
     */
    public void startContainer(final ServiceReference sr) {
        if (this.container == null) {
            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try {
                // Create the EZBArchive relying on OSGi
                ArchiveManager am = ArchiveManager.getInstance();
                IArchive archive = am.getArchive(this.bc);

                BCMapper.getInstance().put(this.bc.getBundle().getEntry(ROOT), this.bc);

                BackedClassLoader backedClassLoader = new BackedClassLoader(new URL[] {archive.getURL()}, this.getClass()
                        .getClassLoader(), this.bc);

                Thread.currentThread().setContextClassLoader(backedClassLoader);

                this.logger.info("Creating Container from the Bundle Archive ''{0}''", archive.getURL());

                IDeployable<?> deployable = DeployableHelper.getDeployable(archive);

                // Create and start the container
                EZBContainerConfig jConfig = new JContainerConfig(deployable);
                EZBServer server = (EZBServer) this.bc.getService(sr);
                jConfig.setEZBServer(server);
                this.container = new JContainer3(jConfig);

                for (EZBConfigurationExtension extension : server.getServerConfig().getExtensionFactories()) {
                    try {
                        extension.configure(jConfig);
                    } catch (Throwable t) {
                        // prevent malicious code to break everything ...
                        this.logger.info("Failed to configure JContainerConfig with {0}", extension.getClass().getName());
                    }
                }


                this.container.addExtension(BundleContext.class, this.bc);

                this.container.setClassLoader(backedClassLoader);

                this.container.start();

                // register the container as a Service
                Properties props = new Properties();
                props.setProperty("name", this.bc.getBundle().getSymbolicName());
                props.setProperty("last.modified", String.valueOf(this.bc.getBundle().getLastModified()));
                props.setProperty("url", String.valueOf(this.bc.getBundle().getBundleId()));

                this.serviceReg = this.bc.registerService(EZBContainer.class.getName(), this.container, props);

            } catch (EZBContainerException e) {
                if (this.container != null) {
                    this.container.stop();
                }
                BCMapper.getInstance().remove(this.bc.getBundle().getEntry(ROOT));
                throw new RuntimeException(e);
            } catch (ArchiveException e) {
                if (this.container != null) {
                    this.container.stop();
                }
                BCMapper.getInstance().remove(this.bc.getBundle().getEntry(ROOT));
                throw new RuntimeException(e);
            } catch (DeployableHelperException e) {
                throw new RuntimeException(e);
            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }
        }
    }

    /**
     * Stop the EZBContainer and unregister the exposed Service.
     */
    public void stopContainer() {
        // Remove the container
        if (this.container != null) {

            ClassLoader old = Thread.currentThread().getContextClassLoader();

            try {
                Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());

                // Stop
                this.container.stop();

                this.container.removeExtension(BundleContext.class);

                // unregister if started
                if (this.serviceReg != null) {
                    this.serviceReg.unregister();
                }

                BCMapper.getInstance().remove(this.bc.getBundle().getEntry(ROOT));

                this.container = null;

            } finally {
                Thread.currentThread().setContextClassLoader(old);
            }

        }
    }

}
