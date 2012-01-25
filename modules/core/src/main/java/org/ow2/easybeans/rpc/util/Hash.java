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
 * $Id: Hash.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.rpc.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

import org.ow2.easybeans.asm.Type;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Utility class providing some hashing methods on java.lang.reflect.Method or
 * java.lang.Class.
 * @author Florent Benoit
 */
public final class Hash {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(Hash.class);

    /**
     * Length of hash of a method (eight byte sequence).
     */
    private static final int BYTES_LENGTH = 8;

    /**
     * Mask for hashing algorithm.
     */
    private static final int BYTE_MASK = 0xFF;


    /**
     * Utility class, no public constructor.
     */
    private Hash() {

    }


    /**
     * Computes the hash for a given method.<br>
     * The method hash to be used for the opnum parameter is a 64-bit (long)
     * integer computed from the first two 32-bit values of the message digest
     * of a particular byte stream using the National Institute of Standards and
     * Technology (NIST) Secure Hash Algorithm (SHA-1). This byte stream
     * contains a string as if it was written using the
     * java.io.DataOutput.writeUTF method, consisting of the remote method's
     * name followed by its method descriptor (see section 4.3.3 of The Java
     * Virtual Machine Specification (JVMS) for a description of method
     * descriptors). The 64-bit hash value is the little-endian composition of
     * an eight byte sequence where the first four bytes are the first 32-bit
     * value of the message digest in big-endian byte order and the last four
     * bytes are the second 32-bit value of the message digest in big-endian
     * byte order.
     * @param methodName the given method. name
     * @param methodDescriptor the method descriptor
     * @return the computed hash.
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method
     *      hashing of RMI</a>
     */
    public static long hashMethod(final String methodName, final String methodDescriptor) {

        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algorithm SHA-1 is not available", e);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DigestOutputStream dos = new DigestOutputStream(baos, md);
        DataOutputStream das = new DataOutputStream(dos);

        StringBuilder sb = new StringBuilder();
        sb.append(methodName);
        sb.append(methodDescriptor);
        try {
            das.writeUTF(sb.toString());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot write data for method '" + methodName + "'.", e);
        }
        try {
            das.flush();
        } catch (IOException e) {
            logger.warn("Cannot flush the stream", e);
        }
        try {
            das.close();
        } catch (IOException e) {
            logger.warn("Cannot flush the stream", e);
        }

        byte[] digest = md.digest();
        long hash = 0;
        int size = Math.min(digest.length, BYTES_LENGTH);
        for (int i = 0; i < size; i++) {
            hash += (long) (digest[i] & BYTE_MASK) << (BYTES_LENGTH * i);
        }
        return hash;
    }


    /**
     * Computes the hash for a given method.<br>
     * The method hash to be used for the opnum parameter is a 64-bit (long)
     * integer computed from the first two 32-bit values of the message digest
     * of a particular byte stream using the National Institute of Standards and
     * Technology (NIST) Secure Hash Algorithm (SHA-1). This byte stream
     * contains a string as if it was written using the
     * java.io.DataOutput.writeUTF method, consisting of the remote method's
     * name followed by its method descriptor (see section 4.3.3 of The Java
     * Virtual Machine Specification (JVMS) for a description of method
     * descriptors). The 64-bit hash value is the little-endian composition of
     * an eight byte sequence where the first four bytes are the first 32-bit
     * value of the message digest in big-endian byte order and the last four
     * bytes are the second 32-bit value of the message digest in big-endian
     * byte order.
     * @param method the given method.
     * @return the computed hash.
     * @see <a
     *      href="http://java.sun.com/j2se/1.5.0/docs/guide/rmi/spec/rmi-stubs24.html">Method
     *      hashing of RMI</a>
     */
    public static long hashMethod(final Method method) {
        return hashMethod(method.getName(), Type.getMethodDescriptor(method));
    }


    /**
     * Gets a map between an hash and its associated method.
     * @param clz the class to analyze.
     * @return a map with an hash and its associated method.
     */
    public static Map<Long, Method> hashClass(final Class<?> clz) {
        Map<Long, Method> map = new HashMap<Long, Method>();
        Method[] methods = clz.getMethods();
        for (Method m : methods) {
            map.put(Long.valueOf(hashMethod(m)), m);
        }
        return map;
    }

}
