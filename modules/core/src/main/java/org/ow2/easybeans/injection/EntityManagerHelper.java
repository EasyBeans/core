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
 * $Id: EntityManagerHelper.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.injection;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;

import org.ow2.easybeans.api.EZBStatefulSessionFactory;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.persistence.api.EZBExtendedEntityManager;
import org.ow2.easybeans.persistence.api.EZBPersistenceUnitManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Helper class for injecting EntityManager instance in the bean.
 * @author Florent Benoit
 */
public final class EntityManagerHelper {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EntityManagerHelper.class);

    /**
     * Utility class, no public constructor.
     */
    private EntityManagerHelper() {

    }

    /**
     * Gets an entity manager for the given session Context.
     * @param ejbContext on which we should provide an entity manager.
     * @param unitName name of the persistence unit.
     * @param type the persistence context type.
     * @return instance of an entity manager which will be used by the
     *         bean/interceptor.
     */
    @SuppressWarnings("unchecked")
    public static EntityManager getEntityManager(final EZBEJBContext ejbContext, final String unitName,
            final PersistenceContextType type, final EasyBeansBean bean) {
        // get bean's factory
        Factory factory = ejbContext.getFactory();
        EZBPersistenceUnitManager persistenceUnitManager = factory.getContainer().getPersistenceUnitManager();
        if (persistenceUnitManager != null) {

            // Check type
            if (PersistenceContextType.EXTENDED == type) {
                // Check that this is for StatefulSession Bean
                if (factory instanceof EZBStatefulSessionFactory) {

                    // Do we have an existing persistence contexts ?
                    Map<String, EZBExtendedEntityManager> extendedPersistenceContexts = factory.getContainer()
                            .getCurrentExtendedPersistenceContexts();

                    if (extendedPersistenceContexts == null) {
                        throw new IllegalStateException(
                                "Extended PersistenceContexts map shouldn't be empty for stateful on factory + "
                                        + factory.getClassName() + "'.");
                    }

                    // We have extended persistence contexts ?
                    EZBExtendedEntityManager extendedEntityManager = extendedPersistenceContexts.get(unitName);

                    if (extendedEntityManager != null) {

                        // close() will occur only at the last ejb.remove() call
                        // so needs to add a new usage on this extended
                        // persistence context
                        extendedEntityManager.addUsage();
                    } else {
                        // We didn't have found any previous Extended Entity
                        // Manager, create a new one
                        // Cast as EZBExtendedEntityManager as the type is
                        // extended
                        extendedEntityManager = (EZBExtendedEntityManager) persistenceUnitManager.getEntityManager(unitName,
                                type);
                        extendedPersistenceContexts.put(unitName, extendedEntityManager);
                    }

                    // Get the SFSB
                    EasyBeansSFSB statefulSessionBean = (EasyBeansSFSB) bean;

                    // Get the factory
                    EZBStatefulSessionFactory statefulFactory = (EZBStatefulSessionFactory) factory;

                    // Add extended Persistence context for the given bean.
                    statefulFactory.addExtendedPersistenceContext(statefulSessionBean, extendedEntityManager);
                    return extendedEntityManager;
                }
                logger.warn("Requested an EntityManager with an extended mode but factory {0} is not a stateful so no extended mode available.", factory);
            }
            return persistenceUnitManager.getEntityManager(unitName, type);
        }
        logger.warn("Requested an EntityManager object but there is no persistenceUnitManager associated"
                + " to this bean/interceptor : {0}", factory);
        return null;
    }

    /**
     * Gets an entity manager factory for the given session Context.
     * @param ejbContext on which we should provide an entity manager
     *        factory.
     * @param unitName name of the persistence unit.
     * @return instance of an entity manager factory which will be used by the
     *         bean/interceptor.
     */
    public static EntityManagerFactory getEntityManagerFactory(final EZBEJBContext ejbContext,
            final String unitName) {
        // get bean's factory
        Factory factory = ejbContext.getFactory();
        EZBPersistenceUnitManager persistenceUnitManager = factory.getContainer().getPersistenceUnitManager();
        if (persistenceUnitManager != null) {
            return persistenceUnitManager.getEntityManagerFactory(unitName);
        }
        logger.warn("Requested an EntityManagerFactory but there is no persistenceUnitManager associated"
                + " to this bean/interceptor : {0}", factory);
        return null;
    }

}
