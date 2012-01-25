/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: JNDIResolverRemoteImpl.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.remotejndiresolver;

import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.resolver.api.EZBJNDIData;
import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;
import org.ow2.easybeans.resolver.api.EZBRemoteJNDIResolver;

import javax.rmi.PortableRemoteObject;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Remote object that is used by client to ask JNDI names on server.
 * @author  Florent Benoit
 */
public class JNDIResolverRemoteImpl extends PortableRemoteObject implements EZBRemoteJNDIResolver {

    /**
     * Server used for the calls.
     */
    private EZBServer easyBeansServer;

    /**
     * This remote object will delegate operations to a given server.
     * @param easyBeansServer the server on which send calls.
     * @throws RemoteException if RMI call fails
     */
    public JNDIResolverRemoteImpl(final EZBServer easyBeansServer) throws RemoteException {
        super();
        this.easyBeansServer = easyBeansServer;
    }


    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @throws java.rmi.RemoteException if RMI communication is failing.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName) throws RemoteException {
        // Delegate call
        return easyBeansServer.getJNDIResolver().getEJBJNDINames(interfaceName);
    }

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @throws RemoteException if RMI communication is failing.
     */
    public List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName, String beanName) throws RemoteException {
        // Delegate call
        return easyBeansServer.getJNDIResolver().getEJBJNDINames(interfaceName, beanName);
    }

    /**
     * Adds a new JNDI name for the given message destination name.
     * @param messageDestinationName the given message destination name.
     * @param jndiData data for the JNDI Name entry
     */
    public List<EZBJNDIData> getMessageDestinationJNDINames(final String messageDestinationName) throws RemoteException {
        // Delegate call
        return easyBeansServer.getJNDIResolver().getMessageDestinationJNDINames(messageDestinationName);
    }
     

}
