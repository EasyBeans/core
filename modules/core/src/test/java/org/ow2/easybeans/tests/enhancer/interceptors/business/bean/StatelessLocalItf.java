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
 * $Id: StatelessLocalItf.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business.bean;


/**
 * Local interface.
 * @author Florent Benoit
 */
public interface StatelessLocalItf {

    // =====================
    //        boolean
    // =====================

    /**
     * Test method on boolean.
     * @param b value to return
     * @return given value
     */
    boolean getBoolean(final boolean b);

    /**
     * Test method on boolean.
     * @param booleans array to return
     * @return given value
     */
    boolean[] getBooleans(final boolean[] booleans);

    // =====================
    //        byte
    // =====================

    /**
     * Test method on byte.
     * @param i value to return
     * @return given value
     */
    byte getByte(final byte i);

    /**
     * Test method on byte.
     * @param bytes array to return
     * @return given value
     */
    byte[] getBytes(final byte[] bytes);

    // =====================
    //        char
    // =====================


    /**
     * Test method on char.
     * @param c value to return
     * @return given value
     */
    char getChar(final char c);

    /**
     * Test method on char.
     * @param chars array to return
     * @return given value
     */
    char[] getChars(final char[] chars);

    // =====================
    //        double
    // =====================


    /**
     * Test method on double.
     * @param d value to return
     * @return given value
     */
    double getDouble(final double d);

    /**
     * Test method on double.
     * @param doubles array to return
     * @return given value
     */
    double[] getDoubles(final double[] doubles);

    // =====================
    //        float
    // =====================


    /**
     * Test method on float.
     * @param f value to return
     * @return given value
     */
    float getFloat(final float f);

    /**
     * Test method on float.
     * @param floats array to return
     * @return given value
     */
    float[] getFloats(final float[] floats);

    // =====================
    //        int
    // =====================


    /**
     * Test method on int.
     * @param i value to return
     * @return given value
     */
    int getInt(final int i);

    /**
     * Adds two int.
     * @param i first value
     * @param j second value
     * @return given value
     */
    int addInt(final int i, final int j);

    /**
     * Test method on int.
     * @param ints array to return
     * @return given value
     */
    int[] getInts(final int[] ints);


    // =====================
    //        long
    // =====================

    /**
     * Test method on long.
     * @param l value to return
     * @return given value
     */
    long getLong(final long l);

    /**
     * Test method on long.
     * @param longs array to return
     * @return given value
     */
    long[] getLongs(final long[] longs);

    // =====================
    //        short
    // =====================


    /**
     * Test method on short.
     * @param s value to return
     * @return given value
     */
    short getShort(final short s);

    /**
     * Test method on short.
     * @param shorts array to return
     * @return given value
     */
    short[] getShorts(final short[] shorts);


    // =====================
    //   Mix of primitives
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
    Object[] getPrimitive(final boolean flag, final byte b, final char c, final double d, final float f, final int i,
            final long l, final Object o);


    // =====================
    //        Exceptions
    // =====================


    /**
     * Throws a user defined exception.
     * @throws TestException an user defined exception
     */
    void someCustomizedExceptions() throws TestException;

    /**
     * Throws user defined exceptions.
     * @param value depending of the value, throw different exceptions.
     * @throws TestException an user defined exception
     * @throws TestException2 another user defined exception
     */
    void someCustomizedExceptions2(final int value) throws TestException, TestException2;

    /**
     * Throws user defined exceptions.
     * @param value depending of the value, throw different exceptions
     * @throws Exception another exception
     */
    void someCustomizedExceptions3(final int value) throws Exception;

    /**
     * Method do nothing but the interceptor will throw an exception.
     */
    void throwExceptionByInterceptor();


    /**
     * Change the return value by the interceptor.
     * @param i value to be add twice.
     * @return a value (mult * 2) of the given value
     */
    int valueDoubledByInterceptor(int i);

    /**
     * Test interceptor which is applied only on a single method.
     */
    void singleMethodIntercepted();

    /**
     * Test that no interceptors are called on this method.
     */
    void excludedInterceptorsMethod();

    /**
     * Two methods with the same name but different parameters.
     * First method.
     * @param i dummy parameter.
     */
    void sameMethodName(int i);

    /**
     * Two methods with the same name but different parameters.
     * Second method.
     * @param d dummy parameter.
     */
    void sameMethodName(double d);


}
