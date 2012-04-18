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
 * $Id: TestWrongSpecification.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.tests.enhancer.wrongspecification;

import static org.ow2.easybeans.tests.enhancer.ClassesEnhancer.EXT_CLASS;
import static org.ow2.easybeans.tests.enhancer.ClassesEnhancer.enhanceNewClassLoader;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.easybeans.tests.enhancer.ClassesEnhancer;
import org.ow2.easybeans.tests.enhancer.ClassesEnhancer.TYPE;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.ArgsConstructorInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.ISuperBeanFinal;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.ItfOneMethod;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.ItfOneMethod00;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.ItfWithInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBFinalFieldEntry;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBFinalInternalInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBItfWithInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBStaticEntry;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBStaticExternalInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBStaticInternalInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBStaticMethodEntry;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBTwoArInvokeExternalInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBTwoAroundInvokeError;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBVoidExternalInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBWithArgsInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SLSBWithSuperClassFinalMethod;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.StaticMethodInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.SuperClassBeanWithFinal;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.TwoAroundInvokeInterceptor;
import org.ow2.easybeans.tests.enhancer.wrongspecification.bean.VoidInterceptor;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Verifies if the Enhancer detects violation of the specification.
 * @reference JSR 220 FINAL
 * @author Eduardo Studzinski Estima de Castro
 * @author Gisele Pinheiro Souza
 */
public class TestWrongSpecification {

    /**
     * This test should cause an enhancer exception. The &#64;Resource is annoted in a final field, it is denied by the
     *           specification.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = IllegalStateException.class)
    public void testWrongSpec00() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(ItfOneMethod.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBStaticMethodEntry.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.ALL);
    }

    /**
     * This test should cause an enhancer exception. The &#64;Resource is annoted in a static field, it is denied by the
     *           specification.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = IllegalStateException.class)
    public void testWrongSpec01() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(ItfOneMethod.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBStaticEntry.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.ALL);
    }

    /**
     * This test should cause an enhancer exception. The &#64;Resource is annoted in a final field, it is denied by the
     *           specification.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = IllegalStateException.class)
    public void testWrongSpec02() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(ItfOneMethod.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBFinalFieldEntry.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.ALL);
    }



    /**
     * This test should cause an enhancer exception. Verifies if an interceptor
     * class with two &#64;AroundInvoke doesn't compile.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec03() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBTwoArInvokeExternalInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(TwoAroundInvokeInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * Verifies if an interceptor class with args doesn't compile.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec04() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBWithArgsInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ArgsConstructorInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * This test should cause an enhancer exception. Verifies if a bean class
     * with two &#64;AroundInvoke doesn't compile.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec05() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBTwoAroundInvokeError.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * This test should cause an enhancer exception. Verifies if an interceptor
     * that the return type is void doesn't compile.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec06() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBVoidExternalInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(VoidInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);

        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);


    }

    /**
     * Verifies if an interceptor class with a static method modifier compile.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
      public void testWrongSpec07() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBStaticExternalInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(StaticMethodInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * This method should cause an exception. Verifies if an
     * &#64;AroundInvoke declared into an interface compiles.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec09() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(ItfWithInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBItfWithInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * Verifies if a bean class with a static interceptor method compiles.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec10() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBStaticInternalInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }

    /**
     * Verifies if a bean class with a final interceptor method compiles.
     * @input -
     * @output -
     * @throws Exception if there is an enhancer exception.
     */
    @Test(groups = {"withWrongSpecification"}, expectedExceptions = InterceptorsValidationException.class)
    public void testWrongSpec11() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SLSBFinalInternalInterceptor.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod00.class.getName().replace(".", File.separator) + EXT_CLASS);
        enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
    }


    /**
     * Verifies if a bean class with a super class with a final method is working.
     * EasyBeans should ignore this method
     * @throws Exception no expected exceptions !
     */
    @SuppressWarnings("boxing")
    //@Test(groups = {"withWrongSpecification"}, expectedExceptions = {})
    public void testWrongSpecFinalMethod() throws Exception {
        List<String> lstFiles = new ArrayList<String>();

        lstFiles.add(SuperClassBeanWithFinal.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ItfOneMethod.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(SLSBWithSuperClassFinalMethod.class.getName().replace(".", File.separator) + EXT_CLASS);
        lstFiles.add(ISuperBeanFinal.class.getName().replace(".", File.separator) + EXT_CLASS);

        ClassLoader definedClassLoader = null;
        try {
            definedClassLoader = ClassesEnhancer.enhanceNewClassLoader(lstFiles, TYPE.INTERCEPTOR);
        } catch (Exception e) {
            Assert.fail("Unable to define the class", e);
        }
        Class<?> beanClass = definedClassLoader.loadClass(SLSBWithSuperClassFinalMethod.class.getName());
        Object bean = beanClass.newInstance();

        Method getBoolMethod = beanClass.getMethod("getBool");
        Method dummyMethod = beanClass.getMethod("dummyFinalMethod", int.class);


        // Expect that the interceptor is called (it means class has been enhanced)
        Assert.assertFalse((Boolean) getBoolMethod.invoke(bean));

        // Expect that the method is not intercepted (default beahvior)
        Assert.assertEquals(dummyMethod.invoke(bean, 2), 2);
    }
}
