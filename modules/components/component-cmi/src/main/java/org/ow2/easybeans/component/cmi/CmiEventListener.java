/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: CmiEventListener.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.cmi;

import java.util.Set;

import org.ow2.cmi.annotation.Cluster;
import org.ow2.cmi.info.CMIInfoExtractor;
import org.ow2.cmi.info.CMIInfoExtractorException;
import org.ow2.cmi.info.CMIInfoRepository;
import org.ow2.cmi.info.ClusteredObjectInfo;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.api.event.EZBEventListener;
import org.ow2.easybeans.api.event.bean.EZBClusteredBeanEvent;
import org.ow2.easybeans.container.session.SessionFactory;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.proxy.reference.RemoteCallRef;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Listener for clusteredBeanEvents.
 * @author eyindanga.
 */
public class CmiEventListener implements EZBEventListener {

    /**
     * This is the topic where naming events will be send. Interested
     * IEventListeners should register to this topic.
     */
    public static final String NAMING_EXTENSION_POINT = "/easybeans/container/factory/context";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(CmiEventListener.class);

    public String getEventProviderFilter() {
        return NAMING_EXTENSION_POINT;
    }

    public boolean accept(final IEvent arg0) {
        return arg0 instanceof EZBClusteredBeanEvent;
    }

    public EventPriority getPriority() {
        return EventPriority.SYNC_LOW;
    }

    public void handle(final IEvent arg0) {
        EZBClusteredBeanEvent clusterEvent = (EZBClusteredBeanEvent) arg0;
        if (EZBClusteredBeanEvent.STARTING.equals(clusterEvent.getState())
                || EZBClusteredBeanEvent.STOPPING.equals(clusterEvent.getState())) {
            for (EZBRef reference : clusterEvent.getReferences()) {
                if (reference instanceof RemoteCallRef) {
                    Factory<?, ?> factory = reference.getFactory();
                    if (factory instanceof SessionFactory) {
                        SessionFactory<?> fact = ((SessionFactory<?>) factory);
                        Object cluster = fact.getBeanInfo().getCluster();
                        Class<?> beanClass = fact.getBeanClass();
                        Class<?> itfClass = null;
                        if (cluster != null || beanClass.isAnnotationPresent(Cluster.class)) {
                            if (EZBClusteredBeanEvent.STARTING.equals(clusterEvent.getState())) {
                                String itfClassname = reference.getItfClassName();
                                logger.info("The bean with the jndi name {0} and the interface name {1} is clustered.",
                                        reference.getJNDIName(), itfClassname);
                                for (Class<?> klass : beanClass.getInterfaces()) {
                                    if (klass.getName().equals(itfClassname)) {
                                        itfClass = klass;
                                    }
                                }
                                if (itfClass == null) {
                                    logger.error("Cannot find the interface for name {0}", itfClassname);
                                    // throw new
                                    // LifeCycleCallbackException("Cannot find the interface for name "
                                    // + itfClassname);
                                }
                                Set<String> applicationExceptionNames = factory.getBeanInfo().getApplicationExceptions()
                                        .keySet();
                                // Extract the informations on clustering
                                ClusteredObjectInfo infos = null;
                                try {
                                    if (cluster != null) {
                                        infos = CMIInfoExtractor.extractClusteringInfoFromClusteredObject(cluster, itfClass,
                                                beanClass, factory instanceof StatefulSessionFactory, false,
                                                applicationExceptionNames);
                                    } else {
                                        infos = CMIInfoExtractor.extractClusteringInfoFromAnnotatedPOJO(
                                                reference.getJNDIName(), itfClass, beanClass,
                                                factory instanceof StatefulSessionFactory, applicationExceptionNames);

                                    }
                                    CMIInfoRepository.addClusteredObjectInfo(reference.getJNDIName(), infos);
                                } catch (CMIInfoExtractorException e) {
                                    logger.error("Cannot extract infos for the class with name {0}", beanClass.getName(), e);
                                    /*
                                     * throw newLifeCycleCallbackException(
                                     * "Cannot extract infos for the class with name "
                                     * + beanClass.getName(), e);
                                     */
                                }
                            } else {
                                // Unbind reference.
                                CMIInfoRepository.removeClusteredObjectInfo(reference.getJNDIName());
                            }

                        }

                    }
                }
            }

        }

    }

}
