/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id:CarolComponent.java 1477 2007-06-16 16:50:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.carol;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServer;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.objectweb.jotm.jta.rmi.JTAInterceptorInitializer;
import org.ow2.carol.jndi.ns.NameServiceManager;
import org.ow2.carol.util.configuration.ConfigurationRepository;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.RegistryComponent;
import org.ow2.easybeans.component.util.Property;
import org.ow2.easybeans.jmx.JMXRemoteException;
import org.ow2.easybeans.jmx.MBeanServerHelper;
import org.ow2.easybeans.security.propagation.rmi.jrmp.interceptors.SecurityInitializer;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This component allows to start a registry.
 * @author Florent Benoit
 */
public class CarolComponent implements RegistryComponent {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(CarolComponent.class);

    /**
     * Default prefix protocol.
     */
    private static final String DEFAULT_PREFIX_PROTOCOL = "rmi";

    /**
     * List of protocols.
     */
    private List<Protocol> protocols = null;

    /**
     * Global Initial Context Factory.
     */
    private String initialContextFactory = null;

    /**
     * Additional properties for Carol.
     */
    private List<Property> carolProperties = null;

    /**
     * Unbind objects when stopping Carol.
     */
    private boolean unbindOnStop = false;

    /**
     * Keep running Carol (it is started once and calls to stop() start() won't do anything).
     */
    private boolean keepRunning = false;

    /**
     * Component has been initialized/started once.
     */
    private boolean initAlreadyDone = false;


    /**
     * Creates a new Carol component.
     */
    public CarolComponent() {
        this.protocols = new ArrayList<Protocol>();
    }

    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    @SuppressWarnings("deprecation")
    public void init() throws EZBComponentException {
        // Do do nothing if the component is restarted with keepRunning mode
        if (this.initAlreadyDone && this.keepRunning) {
            return;
        }

        // check
        if (this.protocols == null || this.protocols.isEmpty()) {
            this.logger.debug("No protocols, use the existing carol configuration");
            return;
        }

        // Get Value of the Initial Factory
        this.initialContextFactory = System.getProperty(Context.INITIAL_CONTEXT_FACTORY);

        // List of protocols
        String lstProtocol = null;

        // Create configuration
        Properties carolConf = new Properties();

        // Set carol mode in server mode (will use fixed port if set)
        System.setProperty("carol.server.mode", "true");

        // Use a local access to registry for optimization
        carolConf.setProperty("carol.jvm.rmi.local.registry", "true");

        for (Protocol protocol : this.protocols) {

            String protocolName = protocol.getName();

            // Build list of protocols
            if (lstProtocol != null) {
                lstProtocol += ",";
                lstProtocol += protocolName;
            } else {
                lstProtocol = protocolName;
            }

            //Define URL
            String url;
            if (protocol.getUrl() != null) {
                // set defined URL
                url = protocol.getUrl();
            } else {
                // compute URL
                String host = protocol.getHostname();
                int portNumber = protocol.getPortNumber();
                url = DEFAULT_PREFIX_PROTOCOL + "://" + host + ":" + portNumber;
            }
            carolConf.setProperty("carol." + protocolName + ".url", url);
        }

        carolConf.setProperty("carol.protocols", lstProtocol);

        // Get additional properties (override the previous, should this happen)
        if (this.carolProperties != null) {
            for (Property property : this.carolProperties) {
                carolConf.setProperty(property.getName(), property.getValue());
            }
        }

        this.logger.debug("carolProps: {0}", carolConf);

        String domainName = null;
        try {
            MBeanServer mBeanServer = MBeanServerHelper.getMBeanServerServer();
            if (mBeanServer != null) {
                domainName = mBeanServer.getDefaultDomain();
            }
        } catch (JMXRemoteException e) {
            this.logger.debug("No domain name for JMX", e);
        }

        try {
            ConfigurationRepository.init(carolConf, domainName, "EasyBeans");
        } catch (Exception e) {
            throw new EZBComponentException("Cannot initialize registry", e);
        }

        try {
            ConfigurationRepository.addInterceptors("jrmp", JTAInterceptorInitializer.class);
        } catch (Exception e) {
            throw new EZBComponentException("Cannot add JOTM interceptors", e);
        }

        try {
            ConfigurationRepository.addInterceptors("iiop", "org.objectweb.jotm.ots.OTSORBInitializer");
        } catch (Exception e) {
            throw new EZBComponentException("Cannot add JOTM interceptors", e);
        }

        try {
            ConfigurationRepository.addInterceptors("jrmp", SecurityInitializer.class);
        } catch (Exception e) {
            throw new EZBComponentException("Cannot add Security interceptors", e);
        }

    }


    /**
     * Start method.<br/>
     * This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {
        // Do do nothing if the component is restarted with keepRunning mode
        if (this.initAlreadyDone && this.keepRunning) {
            return;
        }

        // Start registry only if there are protocols
        if (this.protocols != null && !this.protocols.isEmpty()) {
            try {
                NameServiceManager.getNameServiceManager().startNS();
            } catch (Exception e) {
                throw new EZBComponentException("Cannot start registry", e);
            }
        }

        if (this.keepRunning) {
            this.logger.info("KeepRunning mode enabled");
        }

        // Component has been setup
        this.initAlreadyDone = true;
    }


    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Do do nothing if the component is stopped with keepRunning mode
        if (this.keepRunning) {
            return;
        }

        // Stop registry only if there are protocols
        if (this.protocols != null && !this.protocols.isEmpty()) {
            try {
                NameServiceManager.getNameServiceManager().stopNS();
            } catch (Exception e) {
                throw new EZBComponentException("Cannot stop the registry", e);
            }


            // Unbind all objects that are still in the registry
            NamingEnumeration<NameClassPair> namingEnumeration = null;
            try {
                namingEnumeration = new InitialContext().list("");
            } catch (NamingException e) {
                throw new EZBComponentException("Unable to unbind remaining objects in the registry", e);
            }

            // Loop on all objects
            List<String> names = new ArrayList<String>();
            while (namingEnumeration.hasMoreElements()) {
                NameClassPair ncp = namingEnumeration.nextElement();

                String txt = "[Name='" + ncp.getName() + "', class='" + ncp.getClassName() + "']";
                names.add(txt);

                if (this.unbindOnStop) {
                    try {
                        new InitialContext().unbind(ncp.getName());
                        this.logger.info("Unbind of " + txt + " done !");
                    } catch (NamingException e) {
                        this.logger.error("Unable to unbind the name '" + ncp.getName() + "'", e);
                    }
                }
            }

            // Display all objects that were still bound
            if (names.size() > 0) {
                this.logger.warn("JNDI Names '" + names + "' are still bound in the RMI registry");
            }

            // Restore previous value of the Initial Context Factory
            if (this.initialContextFactory != null) {
                System.setProperty(Context.INITIAL_CONTEXT_FACTORY, this.initialContextFactory);
            } else {
                // Remove property
                System.getProperties().remove(Context.INITIAL_CONTEXT_FACTORY);
            }

        }
        this.logger.info("Carol Component Stopped");
    }

    /**
     * Gets the protocols defined for the start.
     * @return the list of protocols.
     */
    public List<Protocol> getProtocols() {
        return this.protocols;
    }

    /**
     * Sets the list of protocols.
     * @param protocols the list of protocols configured for this server.
     */
    public void setProtocols(final List<Protocol> protocols) {
        this.protocols = protocols;
    }

    /**
     * Gets the default Provider URL.
     * Note: The old API needs to be used here in order to be compliant with products using Carol v2.x
     * @return the provider URL that is used by default.
     */
    public String getProviderURL() {
        return org.objectweb.carol.util.configuration.ConfigurationRepository.getCurrentConfiguration().getProviderURL();
    }

    /**
     * Gets the list of properties.
     * @return the list of properties.
     */
    public List<Property> getProperties() {
        return this.carolProperties;
    }

    /**
     * Set the list of properties.
     * @param carolProperties the list of properties.
     */
    public void setProperties(final List<Property> carolProperties) {
        this.carolProperties = carolProperties;
    }

    /**
     * @return true if unbind needs to be done on stop
     */
    public boolean isUnbindOnStop() {
        return this.unbindOnStop;
    }

    /**
     * If true, the names will be unbound when the component will be stopped.
     * @param unbindOnStop true or false
     */
    public void setUnbindOnStop(final boolean unbindOnStop) {
        this.unbindOnStop = unbindOnStop;
    }

    /**
     * @return true if we should do nothing on stop/restart
     */
    public boolean isKeepRunning() {
        return this.keepRunning;
    }

    /**
     * Stop and restart calls on this component won't do anything with this mode enabled.
     * @param keepRunning true/false
     */
    public void setKeepRunning(final boolean keepRunning) {
        this.keepRunning = keepRunning;
    }

}
