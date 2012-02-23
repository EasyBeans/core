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

import javax.ejb.EJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.exceptions.AnnotationRuntimeExceptionA;
import org.ow2.easybeans.application.exceptions.AnnotationRuntimeExceptionB;
import org.ow2.easybeans.application.exceptions.AnnotationRuntimeExceptionC;
import org.ow2.easybeans.application.exceptions.ICheckerException;
import org.ow2.easybeans.application.exceptions.IException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test exceptions on beans.
 * @author Florent Benoit
 */
public class TestExceptions {

    /**
     * Bean with annotation.
     */
    private IException exceptionBeanAnnotation = null;

    /**
     * Bean for checking tx status.
     */
    private ICheckerException checkedExceptionBean = null;


    @BeforeClass
    public void getBeans() throws NamingException {
        this.exceptionBeanAnnotation = (IException) new InitialContext().lookup("AnnotationExceptionsBean");
        this.checkedExceptionBean = (ICheckerException) new InitialContext().lookup("CheckerExceptionBean");
    }

    @Test(expectedExceptions = AnnotationRuntimeExceptionA.class)
    public void testAnnotationRuntimeExceptionA() {
        this.exceptionBeanAnnotation.methodA();
    }

    @Test(expectedExceptions = AnnotationRuntimeExceptionB.class)
    public void testAnnotationRuntimeExceptionB() {
        this.exceptionBeanAnnotation.methodB();
    }

    @Test(expectedExceptions = AnnotationRuntimeExceptionC.class)
    public void testAnnotationRuntimeExceptionC() {
        this.exceptionBeanAnnotation.methodC();
    }

    @Test(expectedExceptions = EJBException.class)
    public void testAnnotationRuntimeExceptionD() {
        // As AnnotationRuntimeExceptionD extends AnnotationRuntimeExceptionC
        // but with inherited = false, this exception shouldn't be declared as
        // an application exception
        this.exceptionBeanAnnotation.methodD();
    }

    @Test
    public void testCheckedException() {
        this.checkedExceptionBean.checkDefaultApplicationException();
    }

    @Test
    public void testCheckedRollbackException() {
        this.checkedExceptionBean.checkRollbackApplicationException();
    }


}
