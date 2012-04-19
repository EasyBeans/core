/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.embeddable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;


/**
 * This bean exposes its lifecycle and interceptor methods as business methods.
 * Check that calling these methods is OK.
 * @author Florent Benoit
 */
@Singleton
public class LifeCycleAsBusinessMethodsBean extends SuperLifeCycleAsBusinessMethods {

    @Override
    @PostConstruct
    public void myPostConstruct() {
        super.myPostConstruct();
    }


    @Override
    @PreDestroy
    public void myPreDestroy() {

    }


    @Override
    @AroundInvoke
    public Object myInterceptor(final InvocationContext invocationContext) throws Exception {
        return super.myInterceptor(invocationContext);
    }


    public int dummyMethod(final int a) {
        return a;
    }



}
