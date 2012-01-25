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
 * $Id: StatefulBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.lifecycle.bean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.PostActivate;
import javax.ejb.PrePassivate;
import javax.ejb.Stateful;
import javax.interceptor.Interceptors;

/**
 * Simple class for testing callbacks.
 * @author Florent Benoit
 */
@Stateful(name = "HelloWorldbean3")
@Interceptors(MyLifeCycleInterceptorStateful.class)
public class StatefulBean extends AbsSessionBean implements SessionBeanItf {

    /**
     * PrePassivate method has been called ?
     */
    private boolean prePassivateCalled = false;


    /**
     * PostActivate method has been called ?
     */
    private boolean postActivateCalled = false;


    /**
     * Counter used for all callbacks.
     */
    private int counter;

    /**
     * Sets that method marked by &#64;{@link javax.ejb.PrePassivate}  has been called.
     */
    public void calledPrePassivate() {
        this.prePassivateCalled = true;
    }

    /**
     * @return true if method marked by &#64;{@link javax.ejb.PrePassivate}  has been called.
     */
    public boolean isprePassivateCalled() {
        return prePassivateCalled;
    }


    /**
     * Sets that method marked by &#64;{@link javax.ejb.PostActivate}  has been called.
     */
    public void calledPostActivate() {
        this.postActivateCalled = true;
    }


    /**
     * @return true if method marked by &#64;{@link javax.ejb.PostActivate}  has been called.
     */
    public boolean isPostActivateCalled() {
        return postActivateCalled;
    }



    /**
     * Increment the counter.
     */
    @PostConstruct
    @PreDestroy
    @PostActivate
    @PrePassivate
    public void increment() {
        counter++;
    }

    /**
     * @return the value of the internal counter.
     */
    public int getCounter() {
        return counter;
    }

}
