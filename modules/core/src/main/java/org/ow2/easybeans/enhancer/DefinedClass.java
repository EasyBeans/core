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
 * $Id: DefinedClass.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.enhancer;

/**
 * This class is used to define class in classloader.
 * @author Florent Benoit
 */
public class DefinedClass {

    /**
     * Name of the class to define.
     */
    private String className;

    /**
     * Bytecode of the class.
     */
    private byte[] bytes;

    /**
     * Build a new object with a given class name and the bytecode in an
     * array of bytes.
     * @param className name of the class
     * @param bytes bytecode
     */
    public DefinedClass(final String className, final byte[] bytes) {
        this.className = className;
        this.bytes = bytes;
    }

    /**
     * @return bytecode of the class
     */
    public byte[] getBytes() {
        return bytes;
    }

    /**
     * @return the name of the class
     */
    public String getClassName() {
        return className;
    }

    /**
     * Equals.
     * @param o object to compare
     * @return true if given object is the same
     */
    @Override
    public boolean equals(final Object o) {
        if (o instanceof DefinedClass) {
            DefinedClass other = (DefinedClass) o;
            return className.equals(other.getClassName());
        }
        return false;
    }

    /**
     * @return hashcode
     */
    @Override
    public int hashCode() {
        return className.hashCode();
    }

}
