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
 * $Id: EZBRemoteJNDIResolver.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.resolver.api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface used by Remote clients in order to get JNDI Name for a given set of attributes
 * like Interface name, Bean name and/or application name.
 * @author Florent Benoit
 */
public interface EZBRemoteJNDIResolver extends Remote {

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @throws RemoteException if RMI communication is failing.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName) throws RemoteException;

    /**
     * Allows to find EJB JNDI name.
     * @return a list of matching JNDI objects for the given interface and bean name.
     * @param interfaceName the name of the interface that EJBs are implementing.
     * @param beanName the name of the bean on which we need to find JNDI name.
     * @throws RemoteException if RMI communication is failing.
     */
    List<EZBJNDIBeanData> getEJBJNDINames(String interfaceName, String beanName) throws RemoteException;

    /**
     * Allows to find Message Destination JNDI name.
     * @return a list of matching JNDI objects for the given message destination name.
     * @param messageDestinationName the name of the message destination.
     * @throws RemoteException if RMI communication is failing.
     */
    List<EZBJNDIData> getMessageDestinationJNDINames(String messageDestinationName) throws RemoteException;

}
