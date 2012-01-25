/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id:JMXRemoteHelper.java 1537 2007-07-08 15:31:22Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jmx;

import static org.ow2.easybeans.jmx.MBeanServerHelper.getMBeanServerServer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistrationException;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

import org.ow2.easybeans.component.itf.RegistryComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This helper class allow to start a JMX remote connector allowing to connect remote applications.
 * This could be for example a JSR88 provider.
 * @author Florent Benoit
 */
public final class JMXRemoteHelper {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JMXRemoteHelper.class);

    /**
     * JMX connector (server side).
     */
    private static JMXConnectorServer jmxConnectorServer = null;

    /**
     * Prefix for the URL Service.
     */
    private static final String PREFIX_URL_SERVICE = "service:jmx:";

    /**
     * Suffix for the URL Service.
     */
    private static final String SUFFIX_URL_SERVICE = ":///jndi/";

    /**
     * Default RMI host.
     */
    private static final String DEFAULT_RMI = "rmi://localhost:1099";

    /**
     * Suffix for the URL.
     */
    private static final String SUFFIX_URL = "/EasyBeansConnector";

    /**
     * ObjectName for the connector.
     */
    private static final String DEFAULT_NAME_CONNECTOR = "connectors:name=JMXRemoteConnector";


    /**
     * Utility class, no public constructor.
     */
    private JMXRemoteHelper() {

    }


    /**
     * Build a new JMX Remote connector.
     * @param registryComponent to get the provider URL.
     * @throws JMXRemoteException if jmx connector can't be built.
     */
    private static void init(final RegistryComponent registryComponent) throws JMXRemoteException {

        // Create connector
        Map<String, String> environment = new HashMap<String, String>();
        JMXServiceURL jmxServiceURL = null;

        // Build URL
        StringBuilder sb = new StringBuilder(PREFIX_URL_SERVICE);
        URI providerURI = null;
        if (registryComponent != null) {
            try {
                providerURI = new URI(registryComponent.getProviderURL());
            } catch (URISyntaxException e) {
                throw new JMXRemoteException("Cannot get URL from registry component", e);
            }
        } else {
            try {
                providerURI = new URI(DEFAULT_RMI);
            } catch (URISyntaxException e) {
                throw new JMXRemoteException("Cannot get URL from '" + DEFAULT_RMI + "'", e);
            }
        }

        // Switch for the protocol
        String protocol = providerURI.getScheme();
        String protocolName = "rmi";
        if ("iiop".equals(protocol)) {
            protocolName = "iiop";
        }

        sb.append(protocolName);

        // Add suffix URL of the service
        sb.append(SUFFIX_URL_SERVICE);

        // Add rmi URL
        sb.append(protocolName);
        sb.append("://localhost:");
        sb.append(providerURI.getPort());

        // Connector
        sb.append(SUFFIX_URL);
        String url = sb.toString();

        try {
            jmxServiceURL = new JMXServiceURL(url);
        } catch (MalformedURLException e) {
            throw new JMXRemoteException("Cannot create jmxservice url with url '" + url + "'.", e);
        }
        environment.put("jmx.remote.jndi.rebind", "true");
        try {
            jmxConnectorServer = JMXConnectorServerFactory.newJMXConnectorServer(jmxServiceURL, environment,
                    getMBeanServerServer());
        } catch (IOException e) {
            throw new JMXRemoteException("Cannot create new JMX Connector", e);
        }
        logger.info("Creating JMXRemote connector with URL ''{0}''", url);

    }

    /**
     * Start a JMX connector (used to do remote administration).
     * @param registryComponent to get the provider URL.
     * @throws JMXRemoteException if the connector can't be started.
     */
    public static synchronized void startConnector(final RegistryComponent registryComponent) throws JMXRemoteException {
        // Create connector if null
        if (jmxConnectorServer == null) {
            init(registryComponent);
        }

        ObjectName connectorServerName = getConnectorObjectName();
        // register it
        try {
            getMBeanServerServer().registerMBean(jmxConnectorServer, connectorServerName);
        } catch (InstanceAlreadyExistsException e) {
            throw new JMXRemoteException("Cannot register Mbean with the name '" + connectorServerName + "'", e);
        } catch (MBeanRegistrationException e) {
            throw new JMXRemoteException("Cannot register Mbean with the name '" + connectorServerName + "'", e);
        } catch (NotCompliantMBeanException e) {
            throw new JMXRemoteException("Cannot register Mbean with the name '" + connectorServerName + "'", e);
        }

        // Start connector
        try {
            jmxConnectorServer.start();
        } catch (IOException e) {
            throw new JMXRemoteException("Cannot start the jmx connector", e);
        }
    }

    /**
     * Start a JMX connector (used to do remote administration).
     * @throws JMXRemoteException if the connector can't be started.
     */
    public static synchronized void stopConnector() throws JMXRemoteException {
        // Do nothing if there is no Connector
        if (jmxConnectorServer != null) {
            ObjectName connectorServerName = getConnectorObjectName();
            try {
                // Stop
                jmxConnectorServer.stop();
                // Unregister
                getMBeanServerServer().unregisterMBean(connectorServerName);
                // unget the ref
                jmxConnectorServer = null;
            } catch (InstanceNotFoundException e) {
                throw new JMXRemoteException("Cannot unregister JMX Connector with name '" + connectorServerName + "'", e);
            } catch (MBeanRegistrationException e) {
                throw new JMXRemoteException("Cannot unregister JMX Connector with name '" + connectorServerName + "'", e);
            } catch (IOException e) {
                throw new JMXRemoteException("Cannot Stop JMX Connector with name '" + connectorServerName + "'", e);
            }
        }
    }


    /**
     * @return Returns the Connector ObjectName.
     * @throws JMXRemoteException if unable to create the ObjectName.
     */
    private static ObjectName getConnectorObjectName() throws JMXRemoteException {
        ObjectName connectorServerName = null;
        String objName = null;
        try {
            objName = DEFAULT_NAME_CONNECTOR;
            connectorServerName = new ObjectName(objName);
        } catch (MalformedObjectNameException e) {
            throw new JMXRemoteException("Cannot create ObjectName with name '" + objName + "'", e);
        } catch (NullPointerException e) {
            throw new JMXRemoteException("Cannot create ObjectName with name '" + objName + "'", e);
        }
        return connectorServerName;
    }
}
