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
 * $Id: BusinessInterceptorsTestCase.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;
import static org.testng.AssertJUnit.fail;

import java.util.Arrays;

import org.ow2.easybeans.tests.enhancer.interceptors.business.bean.StatelessBean;
import org.ow2.easybeans.tests.enhancer.interceptors.business.bean.TestException;
import org.ow2.easybeans.tests.enhancer.interceptors.business.bean.TestException2;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Call bean and see if interceptor has not broken methods calls.
 * @author Florent Benoit
 */
public class BusinessInterceptorsTestCase{

    /**
     * Bean tested.
     */
    private StatelessBean statelessBean = null;

    /**
     * Enhancing has been done ?
     */
    private static boolean enhancingDone = false;

    /**
     * Setup for test case.
     * @throws Exception if super method fails
     */
    @BeforeMethod
    protected void setUp() throws Exception {
        if (!enhancingDone) {
            BusinessInterceptorsClassesEnhancer.enhance();
            enhancingDone = true;
        }
        statelessBean = new StatelessBean();
    }


    /**
     * Tests the boolean.
     */
    @Test
    public void testBoolean() {
        assertEquals(true, statelessBean.getBoolean(true));
        assertEquals(false, statelessBean.getBoolean(false));

        boolean[] booleans = new boolean[] {false, true, false};
        assertTrue(Arrays.equals(booleans, statelessBean.getBooleans(booleans)));
    }


    /**
     * Tests the bytes.
     */
    @Test
    public void testByte() {
        byte b = 1;
        assertEquals(b, statelessBean.getByte(b));

        byte[] bytes = new byte[] {0, 1};
        assertTrue(Arrays.equals(bytes, statelessBean.getBytes(bytes)));
    }


    /**
     * Tests the chars.
     */
    @Test
    public void testChar() {
        char c = 'a';
        assertEquals(c, statelessBean.getChar(c));

        char[] chars = new char[] {'a', 'b', 'c'};
        assertTrue(Arrays.equals(chars, statelessBean.getChars(chars)));
    }



    /**
     * Tests the double.
     */
    @Test
    public void testDouble() {
        double d = 1;
        assertEquals(Double.valueOf(d), Double.valueOf(statelessBean.getDouble(d)));

        double[] doubles = new double[] {0, 1};
        assertEquals(doubles, statelessBean.getDoubles(doubles));
    }


    /**
     * Tests the float.
     */
    @Test
    public void testFloat() {
        float f = 1;
        assertEquals(Float.valueOf(f), Float.valueOf(statelessBean.getFloat(f)));

        float[] floats = new float[] {0, 1};
        assertEquals(floats, statelessBean.getFloats(floats));
    }



    /**
     * Tests the int.
     */
    @Test
    public void testInt() {
        assertEquals(1, statelessBean.getInt(1));
        assertEquals(1, statelessBean.addInt(0, 1));
        int[] ints = new int[] {0, 1};
        assertEquals(ints, statelessBean.getInts(ints));
    }


    /**
     * Tests the long.
     */
    @Test
    public void testLong() {
        long l = 1;
        assertEquals(Long.valueOf(l), Long.valueOf(statelessBean.getLong(l)));

        long[] longs = new long[] {0, 1};
        assertEquals(longs, statelessBean.getLongs(longs));
    }

    /**
     * Tests the short.
     */
    @Test
    public void testShort() {
        short s = 1;
        assertEquals(Short.valueOf(s), Short.valueOf(statelessBean.getShort(s)));

        short[] shorts = new short[] {0, 1};
        assertEquals(shorts, statelessBean.getShorts(shorts));
    }

    /**
     * Mix of primitive / object.
     */
    @Test
    @SuppressWarnings("boxing")
    public void testMix() {
        boolean flag = false;
        byte b = 0;
        char c = 'c';
        double d = 1;
        float f = 1;
        int i = 1;
        long l = 2;
        Object o = new Object();

        Object[] returnObject = new Object[] {flag, b, c, d, f, i, l, o};
        assertTrue(Arrays.equals(returnObject, statelessBean.getPrimitive(flag, b, c, d, f, i, l, o)));
    }


    /**
     * Test a user defined exception.
     */
    @Test
    public void testUserDefinedException() {
         try {
             statelessBean.someCustomizedExceptions();
             fail("Should throw an exception");
         } catch (TestException test) {
             if (!test.getMessage().equals("someCustomizedExceptions")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }
    }

    /**
     * Test a user defined exception.
     */
    @Test
    public void testUserDefinedException2() {
         try {
             statelessBean.someCustomizedExceptions2(1);
             fail("Should throw an exception");
         } catch (TestException test) {
             if (!test.getMessage().equals("someCustomizedExceptions2.TestException")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }

         try {
             statelessBean.someCustomizedExceptions2(2);
             fail("Should throw an exception");
         } catch (TestException2 test) {
             if (!test.getMessage().equals("someCustomizedExceptions2.TestException2")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }
    }

    /**
     * Test user defined exceptions and exceptions.
     */
    @Test
    public void testMultipleException3() {
        int i = 1;

         try {
             statelessBean.someCustomizedExceptions3(i++);
             fail("Should throw an exception");
         } catch (TestException test) {
             if (!test.getMessage().equals("someCustomizedExceptions3.TestException")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }

         try {
             statelessBean.someCustomizedExceptions3(i++);
             fail("Should throw an exception");
         } catch (TestException2 test) {
             if (!test.getMessage().equals("someCustomizedExceptions3.TestException2")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }

         try {
             statelessBean.someCustomizedExceptions3(i++);
             fail("Should throw an exception");
         } catch (Exception e) {
             if (!e.getMessage().equals("someCustomizedExceptions3.Exception")) {
                 fail("Exception doesn't contain the expected value");
             }
         }

         try {
             statelessBean.someCustomizedExceptions3(i++);
             fail("Should throw an exception");
         } catch (RuntimeException e) {
             if (!e.getMessage().equals("someCustomizedExceptions3.RuntimeException")) {
                 fail("Exception doesn't contain the expected value");
             }
         } catch (Exception e) {
             fail("Not the expected type of exception");
         }

    }


    /**
     * Test that aroundInvoke in the bean has increased a counter.
     */
    @Test
    public void testCounter() {
        int count = statelessBean.getCounter();
        assert count == 0;
        assertEquals(1, statelessBean.addInt(0, 1));
        assert statelessBean.getCounter() > 0;
    }

    /**
     * Test that interceptor has increased a counter.
     */
    @Test
    public void testOtherClassInterceptorCounter() {
        int count = statelessBean.getOtherInterceptorCounter();
        assert count == 0;
        assertEquals(1, statelessBean.addInt(0, 1));
        assert statelessBean.getOtherInterceptorCounter() > 0;
    }

    /**
     * Test that interceptor throw an exception.
     */
    @Test
    public void testInterceptorThrowException() {
        try {
            statelessBean.throwExceptionByInterceptor();
            fail("Should have thrown an exception by the interceptor.");
        } catch (RuntimeException e) {
            assertEquals(e.getCause().getMessage(), "Throw an exception on throwExceptionByInterceptor");
        }
    }


    /**
     * Test that value is increased by interceptor.
     */
    @Test
    public void testValueDoubledByInterceptor() {
        int i = 1;
        assertEquals(i * 2, statelessBean.valueDoubledByInterceptor(i));
    }

    /**
     * Test that the interceptor is only called on a stateless
     * bean and on singleMethodIntercepted method.
     */
    @Test
    public void testSingleMethodIntercepted() {
        int count = statelessBean.getIncrementSingleMethodInterceptedCounter();
        assertTrue(count == 0);
        // Counter will increment
        statelessBean.singleMethodIntercepted();
        assertEquals(1, statelessBean.getIncrementSingleMethodInterceptedCounter());
        // counter shouldn't increment with another method
        statelessBean.addInt(1, 2);
        assertEquals(1, statelessBean.getIncrementSingleMethodInterceptedCounter());
    }

    /**
     * Test that no interceptors are called on this method.
     */
    @Test
    public void testExcludedInterceptorsMethod() {
        // start : counter = 0, interceptor not called
        int count = statelessBean.getOtherInterceptorCounter();
        assert count == 0;

        // call the business method on the bean
        statelessBean.excludedInterceptorsMethod();

        // Check that counter = 0 <-- means that it was excluded
        assert statelessBean.getOtherInterceptorCounter() == 0;
    }

}
