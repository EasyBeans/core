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

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContextType;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.container.EZBEJBContext;
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
    public static EntityManager getEntityManager(final EZBEJBContext ejbContext, final String unitName,
            final PersistenceContextType type) {
        // get bean's factory
        Factory factory = ejbContext.getFactory();
        EZBPersistenceUnitManager persistenceUnitManager = factory.getContainer().getPersistenceUnitManager();
        if (persistenceUnitManager != null) {
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
