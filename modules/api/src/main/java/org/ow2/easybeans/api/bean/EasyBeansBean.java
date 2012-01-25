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
 * $Id: EasyBeansBean.java 5505 2010-05-26 14:01:09Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.lifecycle.EasyBeansLifeCycle;
import org.ow2.easybeans.api.bean.timer.EasyBeansTimedObject;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.api.injection.EasyBeansInjection;
import org.ow2.easybeans.api.interceptor.EZBInvocationContextFactory;

/**
 * Defines the interface of a bean.
 * @author Florent Benoit
 */
public interface EasyBeansBean extends EasyBeansLifeCycle, EasyBeansInjection, EasyBeansTimedObject {

    /**
     * Clean some reference of this instance.
     */
    void easyBeansCleanup();

    /**
     * Gets the factory associated to this bean.
     * @return bean's factory
     */
    Factory getEasyBeansFactory();

    /**
     * Defines the factory associated to this bean.
     * @param easyBeansFactory the EasyBeans factory that manages this bean.
     */
    void setEasyBeansFactory(Factory easyBeansFactory);

    /**
     * Gets the EJB context associated to this bean.
     * @return EJB context
     */
    EZBEJBContext getEasyBeansContext();

    /**
     * Defines the EJB context associated to this bean.
     * @param beanContext the session context associated to this bean.
     */
    void setEasyBeansContext(EZBEJBContext beanContext);

    /**
     * Sets the invocation context factory for this bean.
     * @param ezbinvocationcontextfactory the invocation context factory
     */
    void setEasyBeansInvocationContextFactory(EZBInvocationContextFactory ezbinvocationcontextfactory);


}
