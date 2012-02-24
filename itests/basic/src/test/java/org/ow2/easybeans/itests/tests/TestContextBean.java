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

import org.ow2.easybeans.application.context.IContext;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test injection and EJB Context methods.
 * @author Florent Benoit
 */
public class TestContextBean {


    /**
     * Bean with XML.
     */
    private IContext contextBeanXML = null;

    @BeforeClass
    public void getBean() throws NamingException {
        this.contextBeanXML = (IContext) new InitialContext().lookup("XMLContextBean");
    }


    @Test
    public void testExternalInterceptorInjection() {
        this.contextBeanXML.testExternalInterceptorInjection();
    }

    @Test
    public void testSuperInjection() {
        this.contextBeanXML.testSuperInjection();
    }

    @Test
    public void testInjection() {
        this.contextBeanXML.testInjection();
    }

    @Test
    public void testPostConstruct() {
        this.contextBeanXML.testPostConstruct();
    }

    @Test
    public void testSuperPostConstruct() {
        this.contextBeanXML.testSuperPostConstruct();
    }

    @Test
    public void testAroundInvoke() {
        this.contextBeanXML.testAroundInvoke();
    }

    @Test
    public void testSuperAroundInvoke() {
        this.contextBeanXML.testSuperAroundInvoke();
    }



}
