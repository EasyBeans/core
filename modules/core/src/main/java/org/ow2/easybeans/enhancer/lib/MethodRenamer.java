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
 * $Id: MethodRenamer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

/**
 * Change the name of a method.
 * @author Florent Benoit
 */
public final class MethodRenamer {

    /**
     * Prefix for renamed method.
     */
    public static final String PREFIX = "original$EasyBeans$";

    /**
     * Utility class, no constructor.
     */
    private MethodRenamer() {

    }

    /**
     * Encodes a name by prefixing it with the prefix.
     * @param name method name to rename/encode
     * @return the encoded name
     */
    public static String encode(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Cannot encode a null name");
        }
        return PREFIX + name;
    }

    /**
     * Decodes a method by removing the prefix.
     * @param name encoded method name to decode.
     * @return the decoded name.
     */
    public static String decode(final String name) {
        if (name == null || !name.startsWith(PREFIX)) {
            throw new IllegalArgumentException("Given name '" + name + "' don't start with prefix " + PREFIX);
        }
        return name.substring(PREFIX.length());
    }


    /**
     * Decodes a method by removing the prefix.
     * @param name encoded method name to decode.
     * @return the decoded name.
     */
    public static String tryDecode(final String name) {
        if (name == null || !name.startsWith(PREFIX)) {
           return name;
        }
        return name.substring(PREFIX.length());
    }

}
