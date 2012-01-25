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
 * $Id: AbsSessionBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;


/**
 * Simple common methods for a session bean.
 * @author Florent Benoit
 */

public class AbsSessionBean implements SessionBeanItf {

    /**
     * postConstruct method has been called ?
     */
    private boolean postConstructCalled = false;


    /**
     * preDestroy method has been called ?
     */
    private boolean preDestroyCalled = false;

    /**
     * Counter for the super lifecycle method.
     */
    private int superLifeCycleCounter = 0;


    /**
     * Sets that method marked by &#64;{@link javax.ejb.PostConstruct}  has been called.
     */
    public void calledPostConstruct() {
        this.postConstructCalled = true;
    }

    /**
     * @return true if method marked by &#64;{@link javax.ejb.PostConstruct}  has been called.
     */
    public boolean isPostConstructCalled() {
        return postConstructCalled;
    }


    /**
     * Sets that method marked by &#64;{@link javax.ejb.PreDestroy}  has been called.
     */
    public void calledPreDestroy() {
        this.preDestroyCalled = true;
    }


    /**
     * @return true if method marked by &#64;{@link javax.ejb.PreDestroy}  has been called.
     */
    public boolean isPreDestroyCalled() {
        return preDestroyCalled;
    }


    /**
     * LifeCycle method defined in a super class. Increments a counter.
     */
    @PostConstruct
    @PostActivate
    public void superLifeCycleMethodIncrement() {
        superLifeCycleCounter++;
    }

    /**
     * LifeCycle method defined in a super class. Decrements a counter.
     */
    @PreDestroy
    @PrePassivate
    public void superLifeCycleMethodDecrement() {
        superLifeCycleCounter--;
    }

    /**
     * @return value of the counter of the super lifecycle method
     */
    public int getSuperLifeCycleCounter() {
        return superLifeCycleCounter;
    }


}
