/**
 * EasyBeans
 * Copyright (C) 2007,2008 Bull S.A.S.
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
 * $Id: CmiComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.cmi;

import java.util.List;
import java.util.Properties;

import org.ow2.carol.util.configuration.ConfigurationRepository;
import org.ow2.cmi.config.CMIProperty;
import org.ow2.cmi.controller.common.ClusterViewManager;
import org.ow2.cmi.controller.factory.ClusterViewManagerFactory;
import org.ow2.cmi.controller.server.ServerClusterViewManager;
import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.event.EventComponent;
import org.ow2.easybeans.component.itf.ICmiComponent;
import org.ow2.easybeans.server.ServerConfig;
import org.ow2.util.component.api.Component;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Component providing a support of CMI.
 * @author WEI Zhouyue & ZHU Ning
 */
public class CmiComponent implements ICmiComponent {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(CmiComponent.class);

    /**
     * Configuration of the embedded instance.
     */
    private ServerConfig config;

    /**
     * Event component.
     */
    private EventComponent eventComponent;

    /**
     * True if this component manages the lifecycle of the cluster view manager.
     */
    private boolean lifecycleManaged = true;

    /**
     * The manager of cluster view.
     */
    private ServerClusterViewManager clusterViewManager;

    /**
     * Some default properties.
     */
    private Properties cmiProperties = new Properties();


    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {
        this.cmiProperties.setProperty(CMIProperty.REPLICATION_MANAGER_CLASS.getPropertyName(),
                "org.ow2.cmi.controller.server.impl.jgroups.JGroupsClusterViewManager");
        this.cmiProperties.setProperty(CMIProperty.CONF_FILENAME.getPropertyName(), "cmi-jgroups-config.xml");
    }


    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {
        if (this.lifecycleManaged) {
            // Enable bindings into the cluster
            try {
                ConfigurationRepository.getServerConfiguration().enableCMI(this.cmiProperties);
            } catch (Exception e) {
                this.logger.error("Cannot configure Carol to use CMI", e);
                throw new EZBComponentException("Cannot configure Carol to use CMI", e);
            }

            ClusterViewManagerFactory clusterViewManagerFactory = ClusterViewManagerFactory.getFactory();

            // Start the manager
            try {
                this.clusterViewManager = (ServerClusterViewManager) clusterViewManagerFactory.create();
            } catch (Exception e) {
                this.logger.error("Cannot retrieve the CMI Server", e);
                throw new EZBComponentException("Cannot retrieve the CMI Server", e);
            }
            if (this.clusterViewManager != null
                    && this.clusterViewManager.getState().equals(ClusterViewManager.State.STOPPED)) {
                if (this.eventComponent != null) {
                    List<Component> components =
                        clusterViewManagerFactory.getConfig().getComponents().getComponents();
                    if (components != null) {
                        for (Component cmiEventComponent : components) {
                            if (org.ow2.cmi.component.event.EventComponent.class.isAssignableFrom(cmiEventComponent.getClass())) {
                                ((org.ow2.cmi.component.event.EventComponent) cmiEventComponent).setEventService(
                                        this.eventComponent.getEventService());
                            }
                        }
                    }
                }
                try {
                    this.clusterViewManager.start();
                } catch (Exception e) {
                    this.logger.error("Cannot start the CMI Server", e);
                    throw new EZBComponentException("Cannot start the CMI Server", e);
                }
            }
        }
        // register the listener.
        EZBEventListener eventListener = new CmiEventListener();
        this.eventComponent.registerEventListener(eventListener);

        this.logger.debug("The CMI configuration extension has been added.");
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        if (this.lifecycleManaged) {
            try {
                ConfigurationRepository.getServerConfiguration().disableCMI();
            } catch (Exception e) {
                this.logger.error("Cannot disable CMI in Carol", e);
            }
            if (this.clusterViewManager != null) {
                try {
                    this.clusterViewManager.stop();
                } catch (Exception e) {
                    this.logger.error("Cannot stop the server-side manager", e);
                    throw new EZBComponentException("Cannot stop the server-side manager", e);
                }
            }
        }
        this.logger.info("CMI extension stopped.");

    }

    /**
     * Return the configuration of the embedded instance.
     * @return the configuration of the embedded instance
     */
    public ServerConfig getServerConfig() {
        return this.config;
    }

    /**
     * Set the configuration of the embedded instance.
     * @param config the configuration of the embedded instance
     */
    public void setServerConfig(final ServerConfig config) {
        this.config = config;
    }

    /**
     * @return the event component
     */
    public EventComponent getEventComponent() {
        return this.eventComponent;
    }

    /**
     * Set the event component.
     * @param eventComponent the event component
     */
    public void setEventComponent(final EventComponent eventComponent) {
        this.eventComponent = eventComponent;
    }

    /**
     * @return true if this component manages the lifecycle of the cluster view
     *         manager
     */
    public boolean isLifecycleManaged() {
        return this.lifecycleManaged;
    }

    /**
     * Set if this component manages the lifecycle of the cluster view manager.
     * @param lifecycleManaged true if this component manages the lifecycle of
     *        the cluster view manager
     */
    public void setLifecycleManaged(final boolean lifecycleManaged) {
        this.lifecycleManaged = lifecycleManaged;
    }

}
