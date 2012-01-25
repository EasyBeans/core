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
 * $Id: EZBComponentServiceFactory.java 5221 2009-11-04 15:09:31Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.component;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.Context;

import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedServiceFactory;
import org.osgi.service.component.ComponentConstants;
import org.osgi.service.component.ComponentContext;
import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.osgi.configuration.XMLConfigurationExtractor;
import org.ow2.easybeans.osgi.configuration.XMLConfigurationInjector;
import org.ow2.util.execution.ExecutionResult;
import org.ow2.util.execution.IExecution;
import org.ow2.util.execution.helper.RunnableHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.xmlconfig.XMLConfigurationException;

/**
 * Factory of EasyBeans components.
 * @author David Alves
 * @version $Revision$
 */
public class EZBComponentServiceFactory implements ManagedServiceFactory {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EZBComponentServiceFactory.class);

    private String componentName;

    private Map<String, EZBComponent> pidsToEasybeansComponentsMap;

    private Map<String, ServiceRegistration> pidsToServiceRegistrationsMap;

    @SuppressWarnings("unchecked")
    private Map<String, Dictionary> pidsToConfigurationsMap;

    private ComponentContext componentContext;

    /**
     * Helper to run protected block code.
     */
    private RunnableHelper<EZBComponent> runner = null;

    static {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY,
                "org.objectweb.carol.jndi.spi.MultiOrbInitialContextFactory");
    }

    @SuppressWarnings("unchecked")
    public EZBComponentServiceFactory() {
        pidsToEasybeansComponentsMap = new HashMap<String, EZBComponent>();
        pidsToServiceRegistrationsMap = new HashMap<String, ServiceRegistration>();
        pidsToConfigurationsMap = new HashMap<String, Dictionary>();

        runner = new RunnableHelper<EZBComponent>();
    }

    /**
     * Remove a factory instance.
     * @param pid the PID of the service to be removed
     */
    public synchronized void deleted(final String pid) {
        final EZBComponent ezbComponent = pidsToEasybeansComponentsMap.get(pid);

        // Execute the code under the Bundle ClassLoader
        ExecutionResult<EZBComponent> result = runner.execute(ezbComponent.getClass().getClassLoader(),
                                                      new IExecution<EZBComponent>() {
            public EZBComponent execute() throws Exception {
                ezbComponent.stop();
                return ezbComponent;
            }
        });

        if (result.getException() != null) {
            logger.error("Cannot stop component {0}", ezbComponent.getClass().getName(), result.getException());
        }

        // Continue component's stop
        ServiceRegistration serviceRegistration = pidsToServiceRegistrationsMap.get(pid);
        serviceRegistration.unregister();
        pidsToConfigurationsMap.remove(pid);
        pidsToEasybeansComponentsMap.remove(pid);
        pidsToServiceRegistrationsMap.remove(pid);

        // Log
        logger.debug("Component with pid {0} deleted", pid);
    }

    /**
     * Return a descriptive name of this factory.
     *
     * @return the name for the factory, which might be localized
     */
    public synchronized String getName() {
        return this.componentName;
    }

    /**
     * Create a new instance, or update the configuration of an existing
     * instance.
     * @param pid The PID for this configuration.
     * @param properties A copy of the configuration properties. This argument
     *        must not contain the service.bundleLocation" property. The value
     *        of this property may be obtained from the
     *        <code>Configuration.getBundleLocation</code> method.
     * @throws ConfigurationException when the configuration properties are
     *         invalid.
     */
    @SuppressWarnings("unchecked")
    public synchronized void updated(final String pid, final Dictionary properties) throws ConfigurationException {
        if (pidsToConfigurationsMap.get(pid) != null) {
            // TODO: Handle EZBComponent Configuration Update
            logger.debug("Update of component with pid {0} not yet supported", pid);
            return;
        }
        logger.debug("Creating Component with pid {0}", pid);
        // Insert immediatly to handle updates
        pidsToConfigurationsMap.put(pid, properties);
        Class<EZBComponent> componentClass = null;
        String componentClassName = (String) properties.get(XMLConfigurationExtractor.CLASS_NAME_CONFIG_PROPERTY);

        try {
            componentClass = componentContext.getBundleContext()
                                             .getBundle()
                                             .loadClass(componentClassName)
                                             .asSubclass(EZBComponent.class);
        } catch (Exception e) {
            throw new ConfigurationException(XMLConfigurationExtractor.CLASS_NAME_CONFIG_PROPERTY,
                    "Cannot find EasyBeans component class: " + componentClassName, e);
        } catch (Error e) {
            e.printStackTrace();
            throw new ConfigurationException(XMLConfigurationExtractor.CLASS_NAME_CONFIG_PROPERTY,
                    "Cannot find EasyBeans component class: " + componentClassName, e);
        }


        // Only final variables can be accessed in anonymous inner classes
        final Class<? extends EZBComponent> clazz = componentClass;

        // Execute the code under the Bundle ClassLoader
        ExecutionResult<EZBComponent> result = runner.execute(componentClass.getClassLoader(),
                                                              new IExecution<EZBComponent>() {
            public EZBComponent execute() throws Exception {

                // Get a new configured EZBComponent instance
                EZBComponent ezbComponent = getConfiguredComponent(clazz, properties);

                // Start component Lifecycle
                ezbComponent.init();
                ezbComponent.start();

                // Post Startup hook
                postComponentStart(ezbComponent);
                return ezbComponent;
            }
        });

        if (result.getException() != null) {
            throw new ConfigurationException("", "Error while starting component (class: "
                    + componentClass.getName() + ").", result.getException());
        }

        updateMapsAndRegisterService(pid, properties, result.getResult());

        // Log
        logger.debug("Component with pid {0} and dictionary {1} updated", pid, properties);
        logger.debug("Component with pid {0} created", pid);

    }

    /**
     * Callback after {@link EZBComponent} activation.
     * @param ezbComponent The Started {@link EZBComponent}.
     */
    protected void postComponentStart(final EZBComponent ezbComponent) {
        // By default, do nothing
        // To be subclassed
    }

    @SuppressWarnings("unchecked")
    protected synchronized void updateMapsAndRegisterService(final String pid,
                                                final Dictionary properties,
                                                final EZBComponent ezbComponent) {
        ServiceRegistration serviceRegistration = componentContext.getBundleContext().registerService(
                // Maybe here is missing the business interface of the EZBComponent ?
                new String[] {EZBComponent.class.getName(),
                              ezbComponent.getClass().getName()},
                ezbComponent,
                properties);
        pidsToEasybeansComponentsMap.put(pid, ezbComponent);
        pidsToServiceRegistrationsMap.put(pid, serviceRegistration);
        pidsToConfigurationsMap.put(pid, properties);
    }

    @SuppressWarnings("unchecked")
    public void activate(final ComponentContext componentContext) {
        this.componentContext = componentContext;
        Dictionary properties = componentContext.getProperties();
        this.componentName = (String) properties.get(ComponentConstants.COMPONENT_NAME);
        logger.debug("Activate component {0} with factory.pid = {1}", componentName, properties.get(Constants.SERVICE_PID));
    }

    /**
     * De-activate this DS Component. This will unregister all created services.
     * @param componentContext DS {@link ComponentContext}
     */
    public void deactivate(final ComponentContext componentContext) {
        // Unregister all created instances

        // Need to copy all the PIDs in another List to
        // avoid ConcurrentModificationException
        List<String> pids = new ArrayList<String>();
        pids.addAll(this.pidsToServiceRegistrationsMap.keySet());

        for(String pid : pids) {
            logger.debug("Deleting component with pid {0}", pid);
            deleted(pid);
        }
        logger.debug("Deactivated component {0}", componentName);
    }

    @SuppressWarnings("unchecked")
    protected EZBComponent getConfiguredComponent(final Class<? extends EZBComponent> componentClass,
                                                  final Dictionary configuration)
            throws ConfigurationException {
        EZBComponent ezbComponent;
        String xmlConfiguration = (String) configuration.get(XMLConfigurationExtractor.XML_CONFIG_PROPERTY);
        if (xmlConfiguration == null) {
            throw new ConfigurationException(XMLConfigurationExtractor.XML_CONFIG_PROPERTY,
                    "Could not find configuration for component: " + componentClass.getName());
        }
        XMLConfigurationInjector xmlConfigurationInjector;
        try {
            xmlConfigurationInjector = new XMLConfigurationInjector(componentClass, xmlConfiguration);
        } catch (XMLConfigurationException e) {
            throw new ConfigurationException("", "Error while configuring component (class: "
                    + componentClass.getName() + ").", e);
        }
        ezbComponent = xmlConfigurationInjector.getConfiguredComponent();
        return ezbComponent;
    }
}
