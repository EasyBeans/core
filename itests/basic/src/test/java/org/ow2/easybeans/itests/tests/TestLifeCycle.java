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

package org.ow2.easybeans.itests.tests;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.lifecycle.ILifeCycle;
import org.ow2.easybeans.application.lifecycle.IXMLLifeCycle;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test lifecycle interceptors.
 * @author Florent Benoit
 */
public class TestLifeCycle {


    /**
     * Bean with Annotation.
     */
    private ILifeCycle lifeCycleAnnotationBean = null;

    /**
     * Bean with XML.
     */
    private IXMLLifeCycle lifeCycleXMLBean = null;


    @BeforeClass
    public void getBeans() throws NamingException {
        this.lifeCycleAnnotationBean = (ILifeCycle) new InitialContext().lookup("AnnotationLifeCycleBean");
        this.lifeCycleXMLBean = (IXMLLifeCycle) new InitialContext().lookup("XMLLifecycleBean");

    }



    @Test
    public void testCheckInterceptors() {
        //this.lifeCycleAnnotationBean.dummyCallWithMethodInterceptor(new ArrayList<String>());
    }






}
