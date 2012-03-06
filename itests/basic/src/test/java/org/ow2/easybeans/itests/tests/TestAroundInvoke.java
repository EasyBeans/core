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

import java.util.ArrayList;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.aroundinvoke.IAroundInvoke;
import org.ow2.easybeans.application.aroundinvoke.IXMLAroundInvoke;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test around invoke interceptors.
 * @author Florent Benoit
 */
public class TestAroundInvoke {


    /**
     * Bean with Annotation.
     */
    private IAroundInvoke aroundInvokeAnnotationBean = null;

    /**
     * Bean with XML.
     */
    private IXMLAroundInvoke aroundInvokeXMLBean = null;


    @BeforeClass
    public void getBeans() throws NamingException {
        this.aroundInvokeAnnotationBean = (IAroundInvoke) new InitialContext().lookup("AnnotationInterceptorBean");
        this.aroundInvokeXMLBean = (IXMLAroundInvoke) new InitialContext().lookup("XMLInterceptorBean");

    }



    @Test
    public void testCheckInterceptors() {
        this.aroundInvokeAnnotationBean.dummyCallWithMethodInterceptor(new ArrayList<String>());
    }



    @Test
    public void testCheckExcludedClassInterceptors() {
        this.aroundInvokeAnnotationBean.dummyCallWithExcludedClassInterceptors(new ArrayList<String>());
    }


    @Test
    public void testCheckExcludedDefaultInterceptors() {
        this.aroundInvokeAnnotationBean.dummyCallWithExcludedDefaultInterceptors(new ArrayList<String>());
    }

    @Test
    public void testCheckOveriddedInterceptors() {
        this.aroundInvokeXMLBean.dummyCallOverrided(new ArrayList<String>());
    }

    @Test
    public void testCheckOveriddedInInterceptors() {
        this.aroundInvokeAnnotationBean.dummyCallWithOverridedInterceptors(new ArrayList<String>());
    }




}
