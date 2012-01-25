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
 * $Id: MBeanServerHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jmx;

import java.util.List;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;

/**
 * Allow to start an MBean server and get an MBeanServer.
 * @author Florent Benoit
 */
public final class MBeanServerHelper {

    /**
     * MBeanServer.
     */
    private static MBeanServer mbeanServer = null;

    /**
     * Id of the generated MBeanServer.
     */
    private static String idMbeanServer = null;

    /**
     * Default domain name.
     */
    private static final String DEFAULT_DOMAIN_NAME = "EasyBeans";

    /**
     * Utility class, no public constructor.
     */
    private MBeanServerHelper() {

    }

    /**
     * Gets first available MBean Server.
     * @return first available MBean server.
     * @throws JMXRemoteException if no server is available
     */
    public static MBeanServer getMBeanServerServer() throws JMXRemoteException {
        if (getInternalMBeanServer() != null) {
            return getInternalMBeanServer();
        }
        throw new JMXRemoteException("No running MBeanServer was found.");
    }

    /**
     * @return first MBean server found.
     */
    private static MBeanServer getInternalMBeanServer() {
        List mbeanServers = MBeanServerFactory.findMBeanServer(null);
        if (mbeanServers.size() > 0) {
            return (MBeanServer) mbeanServers.get(0);
        }
        return null;
    }

/**
 * Starts an MBeanServer if no MBeanServer is available.
 * @throws MBeanServerException if MBeanServer can't be started
 */
    public static synchronized  void startMBeanServer() throws MBeanServerException {
        mbeanServer = getInternalMBeanServer();
        // Need to create one
        if (mbeanServer == null) {
        // Need to create an MBean server
            mbeanServer = MBeanServerFactory.createMBeanServer(DEFAULT_DOMAIN_NAME);
        }

        ObjectName mbeanServerDelegate = null;
        try {
            mbeanServerDelegate = new ObjectName("JMImplementation:type=MBeanServerDelegate");
        } catch (MalformedObjectNameException e) {
            throw new MBeanServerException("Cannot build an objectName", e);
        } catch (NullPointerException e) {
            throw new MBeanServerException("Cannot build an objectName", e);
        }

        try {
            idMbeanServer = (String) mbeanServer.getAttribute(mbeanServerDelegate, "MBeanServerId");
        } catch (AttributeNotFoundException e) {
            throw new MBeanServerException("Cannot get an attribute on MBeanserver.", e);
        } catch (InstanceNotFoundException e) {
            throw new MBeanServerException("Cannot get an attribute on MBeanserver.", e);
        } catch (MBeanException e) {
            throw new MBeanServerException("Cannot get an attribute on MBeanserver.", e);
        } catch (ReflectionException e) {
            throw new MBeanServerException("Cannot get an attribute on MBeanserver.", e);
        }
    }

    /**
     * @return the id of the created MbeanServer.
     */
    protected static String getIdMbeanServer() {
        return idMbeanServer;
    }

}
