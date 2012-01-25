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
 * $Id: RemoteEasyBeansPluginServer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.io.IOException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.deployer.IRemoteDeployer;
import org.ow2.easybeans.deployer.management.RemoteDeployerIdentifier;
import org.ow2.easybeans.server.management.EmbeddedIdentifier;



/**
 * Implementation of IRemotePluginServer interface.
 * Provide access of remote EasyBeans instance and operations.
 * @author Vincent Michaud
 * @author Alexandre Deneux
 */
public class RemoteEasyBeansPluginServer extends AbstractEasyBeansPluginServer {

    /**
     * Prefix for the JMX service URL.
     */
    private static final String PREFIX_JMX_URL = "service:jmx:rmi:///jndi/rmi://";

    /**
     * Suffix for the JMX service URL.
     */
    private static final String SUFFIX_JMX_URL = "/EasyBeansConnector";

    /**
     * Prefix name for JMX component.
     */
    private static final String PREFIX_NAME = "EasyBeans:";

    /**
     * Name of the EasyBeans server instance.
     */
    private static final String SERVER_NAME = "EasyBeans_0";

    /**
     * The remote deployer.
     */
    private IRemoteDeployer rd = null;

    /**
     * The connection with the server.
     */
    private MBeanServerConnection mbsc;

    /**
     * The name of the MBean server.
     */
    private ObjectName serverName;

    /**
     * Get an instance of launched remote EasyBeans server.
     * @param hostname Domaine name used to contact EasyBeans.
     * @param numPort Port number used to contact EasyBeans.
     * @throws IOException When not any EasyBeans server instance found
     */
    public RemoteEasyBeansPluginServer(final String hostname, final int numPort) throws IOException {        
        if (!localServerFound()) {
            try {
                // If no server was found, check for a remote server.
                JMXServiceURL url = new JMXServiceURL(PREFIX_JMX_URL + hostname + ":" + numPort + SUFFIX_JMX_URL);
                serverName = newJMXServerObjectName();
                JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
                mbsc = jmxc.getMBeanServerConnection();
                setServer((EZBServer) MBeanServerInvocationHandler.newProxyInstance(this.mbsc, this.serverName,
                                                                                    EZBServer.class, false));
                ObjectName deployerName = newJMXDeployerObjectName();
                this.rd = (IRemoteDeployer) MBeanServerInvocationHandler.newProxyInstance(this.mbsc, deployerName,
                                                                                          IRemoteDeployer.class, false);
            } catch (MalformedObjectNameException ex) {
                getLog().error(ex);
            } catch (NullPointerException ex) {
                getLog().error(ex);
            }
        }
    }

    /**
     * Deploy an EJB or an EAR.
     * @param filename Name of deployable file
     */
    @Override
    public synchronized void deployArchive(final String filename) {
        if (localServerFound()) {
            super.deployArchive(filename);
        } else {
            this.rd.deploy(filename);
        }
    }

    /**
     * Undeploy a deployed EJB or EAR.
     * @param filename A deployable deployed on EasyBeans
     */
    @Override
    public synchronized void undeployArchive(final String filename) {
        if (localServerFound()) {
            super.undeployArchive(filename);
        } else {
            this.rd.undeploy(filename);
        }
    }

    /**
     * Determine if the server is a remote instance.
     * @return True if server is a remote instance.
     */
    public boolean isRemoteInstance() {
        return !localServerFound();
    }

    /**
     * Get the version of EasyBeans server.
     * @return The version
     */
    public String getVersion() {
        try {
            String version = (String) mbsc.getAttribute(serverName, "serverVersion");
            return Version.removeRevision(version);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * Get the JMX ObjectName of the EasyBeans server component.
     * @return The ObjectName
     * @exception MalformedObjectNameException If the name is malformed
     */
    private ObjectName newJMXServerObjectName() throws MalformedObjectNameException {
        StringBuffer buffer = new StringBuffer();
        EmbeddedIdentifier ei = new EmbeddedIdentifier();
        buffer.append(PREFIX_NAME);
        buffer.append(ei.getTypeProperty());
        buffer.append(",name=");
        buffer.append(SERVER_NAME);
        return new ObjectName(buffer.toString());
    }

    /**
     * Get the JMX ObjectName of the EasyBeans deployer component.
     * @return The ObjectName
     * @exception MalformedObjectNameException If the name is malformed
     */
    private ObjectName newJMXDeployerObjectName() throws MalformedObjectNameException {
        StringBuffer buffer = new StringBuffer();
        RemoteDeployerIdentifier rdi = new RemoteDeployerIdentifier();
        buffer.append(PREFIX_NAME);
        buffer.append(rdi.getTypeProperty());
        buffer.append(",name=");
        buffer.append(rdi.getNamePropertyValue(null));
        return new ObjectName(buffer.toString());
    }
}
