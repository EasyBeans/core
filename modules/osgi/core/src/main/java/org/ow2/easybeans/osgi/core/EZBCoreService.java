/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EZBCoreService.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.core;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.osgi.archive.BundleArchiveFactory;
import org.ow2.easybeans.osgi.extension.EasyBeansOSGiExtension;
import org.ow2.easybeans.osgi.extension.OSGiBindingFactory;
import org.ow2.easybeans.proxy.binding.BindingManager;
import org.ow2.easybeans.server.Embedded;
import org.ow2.easybeans.server.ServerConfig;
import org.ow2.util.archive.api.IArchiveFactory;
import org.ow2.util.archive.impl.ArchiveManager;
import org.ow2.util.execution.ExecutionResult;
import org.ow2.util.execution.IExecution;
import org.ow2.util.execution.IRunner;
import org.ow2.util.execution.helper.RunnableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Class used for managing the Embedded instance and its associated components.
 * @author David Alves
 * @version $Revision$
 */
public class EZBCoreService {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EZBCoreService.class);

    /**
     * EasyBeans embeddable server.
     */
    private Embedded embedded = null;

    /**
     * ServiceRegistration for EZB.
     */
    private ServiceRegistration sr = null;

    /**
     * OSGi archive factory.
     */
    private IArchiveFactory<BundleContext> factory = null;

    /**
     * Containers obtained from OSGi ServiceRegistry.
     */
    private List<EZBContainer> containers = new ArrayList<EZBContainer>();

    /**
     * Containers obtained from OSGi ServiceRegistry.
     */
    private List<EZBComponent> components = new ArrayList<EZBComponent>();

    /**
     * This bundle's BundleContext.
     */
    private ComponentContext componentContext = null;

    /**
     * The {@link org.ow2.easybeans.api.binding.EZBBindingFactory} specialized for OSGi.
     */
    private OSGiBindingFactory bindingFactory = null;

    /**
     * Default Constructor.
     */
    public EZBCoreService() {
        this.embedded = new Embedded();
    }

    public void start() throws Exception {

        // create the OSGi BF
        this.bindingFactory = new OSGiBindingFactory(componentContext.getBundleContext());

        IRunner<Embedded> runner = new RunnableHelper<Embedded>();
        ExecutionResult<Embedded> result = runner.execute(this.getClass().getClassLoader(), new IExecution<Embedded>() {

            public Embedded execute() throws Exception {
                // register the BundleArchiveFactory
                ArchiveManager am = ArchiveManager.getInstance();
                factory = new BundleArchiveFactory();
                am.addFactory(factory);

                BindingManager.getInstance().registerFactory(bindingFactory);

                // Create the configuration
                ServerConfig sc = new ServerConfig();
                sc.setShouldWait(false);
                sc.setDirectoryScanningEnabled(false);
                sc.setAddEmbeddedComponents(false);
                sc.setAutoConfigureComponents(false);
                sc.setStopComponentsDuringShutdown(false);

                // Add extension factory
                EasyBeansOSGiExtension extension = new EasyBeansOSGiExtension();
                // Store the default BC (for EjbJars not deployed as bundle)
                extension.setBundleContext(componentContext.getBundleContext());
                sc.addExtensionFactory(extension);

                // Create the Server
                // TODO : configure server with xml file
                embedded.setServerConfig(sc);

                // Start it
                embedded.start();

                return embedded;
            }

        });

        if (result.getException() != null) {
            throw result.getException();
        }

        // Register after startup
        sr = componentContext.getBundleContext().registerService(Embedded.class.getName(), embedded, null);

    }

    public void stop() throws Exception {

        IRunner<Embedded> runner = new RunnableHelper<Embedded>();
        ExecutionResult<Embedded> result = runner.execute(this.getClass().getClassLoader(), new IExecution<Embedded>() {

            public Embedded execute() throws Exception {
                // unregister the BundleArchiveFactory
                if (factory != null) {
                    ArchiveManager am = ArchiveManager.getInstance();
                    am.removeFactory(factory);
                }
                // unregister the service
                if (sr != null) {
                    sr.unregister();
                }
                // stop the server
                if (embedded != null) {
                    // Remove the Containers
                    removeContainers();
                    // Stop the instance
                    embedded.stop();
                    // Remove the Components
                    removeComponents();
                }

                // Do not forget to unregister the OSGi BF
                BindingManager.getInstance().unregisterFactory(bindingFactory);

                return embedded;
            }

        });

        if (result.getException() != null) {
            throw result.getException();
        }

        if (result.getResult() != null) {
            // Build a new one
            embedded = new Embedded();
        }

    }

    protected void activate(final ComponentContext componentContext) {
        this.componentContext = componentContext;
        try {
            logger.info("Activating EasyBeans/OSGi Core ");
            start();
        } catch (Exception e) {
            logger.error("Cannot start the EasyBeans core", e);
        }
    }

    protected void deactivate(final ComponentContext componentContext) {
        this.componentContext = componentContext;
        try {
            logger.info("Deactivating EasyBeans/OSGi Core ");
            stop();
        } catch (Exception e) {
            logger.error("Cannot stop the EasyBeans core", e);
        }
    }

    /**
     * Remove a given EZBContainer.
     * @param container the EZBContainer to remove.
     */
    public void removeContainer(final EZBContainer container) {
        // Remove only if container is present, else it has already been
        // removed.
        if (containers.contains(container)) {
            containers.remove(container);
            embedded.removeContainer(container);
        }
    }

    /**
     * Add a given EZBContainer.
     * @param container The EZBContainer to add.
     */
    public void addContainer(final EZBContainer container) {
        containers.add(container);
        embedded.addContainer(container);
    }

    /**
     * Add a given EZBComponent.
     * @param component The EZBComponent to add.
     * @exception EZBComponentException Cannot add the component.
     */
    public void addComponent(final EZBComponent component) throws EZBComponentException {
        // Add component into Embedded instance
        embedded.getComponentManager().addComponent(component);
        components.add(component);

    }

    /**
     * Remove a given EZBComponent.
     * @param component the EZBComponent to remove.
     */
    public void removeComponent(final EZBComponent component) {
        // Only remove if the component is still there
        if (components.contains(component)) {
            components.remove(component);
            try {
                embedded.getComponentManager().removeComponent(component);
            } catch (EZBComponentException e) {
                logger.error("Cannot remove the component {0}", component, e);
            }
        }
    }

    /**
     * Remove all EZBContainers obtained with OSGi.
     */
    private void removeContainers() {
        while (!containers.isEmpty()) {
            EZBContainer container = containers.get(0);
            removeContainer(container);
        }
    }

    /**
     * Remove all EZBContainers obtained with OSGi.
     * @exception EZBComponentException Cannot remove a component.
     */
    private void removeComponents() throws EZBComponentException {
        while (!components.isEmpty()) {
            EZBComponent component = components.get(0);
            removeComponent(component);
        }
    }

}
