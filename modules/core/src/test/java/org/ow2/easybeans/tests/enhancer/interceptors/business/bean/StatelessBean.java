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
 * $Id: StatelessBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business.bean;

import static javax.ejb.TransactionAttributeType.NEVER;

import javax.ejb.Local;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.interceptor.AroundInvoke;
import javax.interceptor.ExcludeClassInterceptors;
import javax.interceptor.Interceptors;
import javax.interceptor.InvocationContext;

/**
 * Simple class for testing interceptors.
 * @author Florent Benoit
 */
@Stateless(name = "HelloWorldbean")
@Remote(StatelessRemoteItf.class)
@Local({StatelessLocalItf.class, StatelessLocalBisItf.class})
@Interceptors({Interceptor.class})
@TransactionAttribute(NEVER)
public class StatelessBean implements StatelessRemoteItf, StatelessLocalItf, StatelessLocalBisItf {

    /**
     * Counter of intercepted methods.
     */
    private int counter;

    /**
     * Counter of intercepted methods by an external interceptor.
     */
    private int otherInterceptorCounter = 0;

    /**
     * Counter of intercepted calls on singleMethodIntercepted.
     */
    private int singleMethodInterceptedCounter = 0;

    // =====================
    // boolean
    // =====================

    /**
     * Test method on boolean.
     * @param b value to return
     * @return given value
     */
    public boolean getBoolean(final boolean b) {
        return b;
    }

    /**
     * Test method on boolean.
     * @param booleans array to return
     * @return given value
     */
    public boolean[] getBooleans(final boolean[] booleans) {
        return booleans;
    }

    // =====================
    // byte
    // =====================

    /**
     * Test method on byte.
     * @param i value to return
     * @return given value
     */
    public byte getByte(final byte i) {
        return i;
    }

    /**
     * Test method on byte.
     * @param bytes array to return
     * @return given value
     */
    public byte[] getBytes(final byte[] bytes) {
        return bytes;
    }

    // =====================
    // char
    // =====================

    /**
     * Test method on char.
     * @param c value to return
     * @return given value
     */
    public char getChar(final char c) {
        return c;
    }

    /**
     * Test method on char.
     * @param chars array to return
     * @return given value
     */
    public char[] getChars(final char[] chars) {
        return chars;
    }

    // =====================
    // double
    // =====================

    /**
     * Test method on double.
     * @param d value to return
     * @return given value
     */
    public double getDouble(final double d) {
        return d;
    }

    /**
     * Test method on double.
     * @param doubles array to return
     * @return given value
     */
    public double[] getDoubles(final double[] doubles) {
        return doubles;
    }

    // =====================
    // float
    // =====================

    /**
     * Test method on float.
     * @param f value to return
     * @return given value
     */
    public float getFloat(final float f) {
        return f;
    }

    /**
     * Test method on float.
     * @param floats array to return
     * @return given value
     */
    public float[] getFloats(final float[] floats) {
        return floats;
    }

    // =====================
    // int
    // =====================

    /**
     * Test method on int.
     * @param i value to return
     * @return given value
     */
    public int getInt(final int i) {
        return i;
    }

    /**
     * Adds two int.
     * @param i first value
     * @param j second value
     * @return given value
     */
    public int addInt(final int i, final int j) {
        return i + j;
    }

    /**
     * Test method on int.
     * @param ints array to return
     * @return given value
     */
    public int[] getInts(final int[] ints) {
        return ints;
    }

    // =====================
    // long
    // =====================

    /**
     * Test method on long.
     * @param l value to return
     * @return given value
     */
    public long getLong(final long l) {
        return l;
    }

    /**
     * Test method on long.
     * @param longs array to return
     * @return given value
     */
    public long[] getLongs(final long[] longs) {
        return longs;
    }

    // =====================
    // short
    // =====================

    /**
     * Test method on short.
     * @param s value to return
     * @return given value
     */
    public short getShort(final short s) {
        return s;
    }

    /**
     * Test method on short.
     * @param shorts array to return
     * @return given value
     */
    public short[] getShorts(final short[] shorts) {
        return shorts;
    }

    // =====================
    // Mix of primitives
    // =====================

    /**
     * Test method on primitive.
     * @param flag value to return
     * @param b value to return
     * @param c value to return
     * @param d value to return
     * @param f value to return
     * @param i value to return
     * @param l value to return
     * @param o value to return
     * @return given values
     */
    public Object[] getPrimitive(final boolean flag, final byte b, final char c, final double d, final float f, final int i,
            final long l, final Object o) {
        // don't use autoboxing
        return new Object[]{Boolean.valueOf(flag), Byte.valueOf(b), Character.valueOf(c), Double.valueOf(d), Float.valueOf(f),
                Integer.valueOf(i), Long.valueOf(l), o};
    }

    /**
     * -----------------------------------------. - Method from another
     * interface --- -----------------------------------------
     */
    public void methodNotInAllInterface() {

    }

    // =====================
    // Exceptions
    // =====================

    /**
     * Throws a user defined exception.
     * @throws TestException an user defined exception
     */
    public void someCustomizedExceptions() throws TestException {
        throw new TestException("someCustomizedExceptions");
    }

    /**
     * Throws user defined exceptions.
     * @param value depending of the value, throw different exceptions.
     * @throws TestException an user defined exception
     * @throws TestException2 another user defined exception
     */
    public void someCustomizedExceptions2(final int value) throws TestException, TestException2 {
        switch (value) {
        case 1:
            throw new TestException("someCustomizedExceptions2.TestException");
        case 2:
            throw new TestException2("someCustomizedExceptions2.TestException2");
        default:
            break;
        }
    }

    /**
     * Throws user defined exceptions.
     * @param value depending of the value, throw different exceptions
     * @throws Exception another exception
     */
    public void someCustomizedExceptions3(final int value) throws Exception {
        final int one = 1;
        final int two = 2;
        final int three = 3;
        switch (value) {
        case one:
            throw new TestException("someCustomizedExceptions3.TestException");
        case two:
            throw new TestException2("someCustomizedExceptions3.TestException2");
        case three:
            throw new Exception("someCustomizedExceptions3.Exception");
        default:
            throw new RuntimeException("someCustomizedExceptions3.RuntimeException");
        }
    }

    /**
     * Method do nothing but the interceptor will throw an exception.
     */
    public void throwExceptionByInterceptor() {

    }

    /**
     * Change the return value by the interceptor.
     * @param i value to be add twice.
     * @return a value (mult * 2) of the given value
     */
    public int valueDoubledByInterceptor(final int i) {
        return i;
    }

    /**
     * Do some stuff while intercepting methods.
     * @param invocationContext contains attributes of invocation
     * @return method's invocation result
     * @throws Exception if invocation fails
     */
    @AroundInvoke
    public Object intercepted(final InvocationContext invocationContext) throws Exception {
        // first, increase the counter
        this.counter++;

        // Check the method name
        if (invocationContext.getMethod().getName().equals("throwExceptionByInterceptor")) {
            throw new Exception("Throw an exception on throwExceptionByInterceptor");
        }

        if (invocationContext.getMethod().getName().equals("valueDoubledByInterceptor")) {
            Object value = null;
            try {
                value = invocationContext.proceed();
            } finally {
                if (value instanceof Integer) {
                    return Integer.valueOf(((Integer) value).intValue() * 2);
                }
            }
        }

        return invocationContext.proceed();
    }

    /**
     * @return the counter (should be incremented by interceptor)
     */
    public int getCounter() {
        return this.counter;
    }

    /**
     * @return a counter used by other interceptors.
     */
    public int getOtherInterceptorCounter() {
        return this.otherInterceptorCounter;
    }

    /**
     * Increment the value of the counter used by other interceptors.
     */
    public void incrementOtherInterceptorCounter() {
        this.otherInterceptorCounter++;
    }

    /**
     * Test interceptor which is applied only on a single method.
     */
    @Interceptors({SingleMethodInterceptor.class})
    public void singleMethodIntercepted() {

    }

    /**
     * Increment the value of the counter used by single method interceptor.
     */
    public void incrementSingleMethodInterceptedCounter() {
        this.singleMethodInterceptedCounter++;
    }

    /**
     * @return the value of the counter used by single method interceptor.
     */
    public int getIncrementSingleMethodInterceptedCounter() {
        return this.singleMethodInterceptedCounter;
    }

    /**
     * Test that no interceptors are called on this method.
     */
    @ExcludeClassInterceptors
    public void excludedInterceptorsMethod() {

    }

    /**
     * Two methods with the same name but different parameters. First method.
     * @param i dummy parameter.
     */
    public void sameMethodName(final int i) {

    }

    /**
     * Two methods with the same name but different parameters. Second method.
     * @param d dummy parameter.
     */
    public void sameMethodName(final double d) {

    }
}
