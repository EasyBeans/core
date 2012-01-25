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
 * $Id: RemoteJNDIResolverComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.remotejndiresolver;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.server.ServerConfig;
import org.ow2.easybeans.api.EZBServer;
import org.ow2.easybeans.api.EmbeddedManager;
import org.ow2.easybeans.resolver.api.EZBRemoteJNDIResolver;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

/**
 * Component providing JNDI Resolver to clients.
 * @author  Florent Benoit
 */
public class RemoteJNDIResolverComponent implements EZBComponent {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(RemoteJNDIResolverComponent.class);

    /**
     * Default JNDI Name for the Remote JNDI Resolver.
     */
    private static final String DEFAULT_JNDI_NAME = "EZB_Remote_JNDIResolver";

    /**
     * JNDI Resolver instance
     */
    private EZBRemoteJNDIResolver jndiResolver = null;

   /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
    */
    public void init() throws EZBComponentException {

    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {
        // get server from the config

        // TODO: Change this to an event
        EZBServer server = EmbeddedManager.getEmbedded(0);

        // Build Remote Object for this server.
        try {
            jndiResolver = new JNDIResolverRemoteImpl(server);
        } catch (RemoteException e) {
            throw new EZBComponentException("Cannot create the JNDI Resolver.", e);
        }

        // Bind
        try {
            new InitialContext().bind(DEFAULT_JNDI_NAME, jndiResolver);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot bind the JNDI Resolver", e);
        }

    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Unbind JNDI Resolver
        try {
            new InitialContext().unbind(DEFAULT_JNDI_NAME);
        } catch (NamingException e) {
            logger.error("Cannot unbind the JNDI Resolver", e);
        }

        // Unexport
        try {
            PortableRemoteObject.unexportObject(jndiResolver);
        } catch (NoSuchObjectException e) {
            logger.error("Cannot unexport the JNDI Resolver", e);
        }


    }

}
