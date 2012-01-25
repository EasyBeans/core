/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: BytecodeDescriptorHelper.java 5214 2009-10-29 13:32:27Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.util;

import org.ow2.easybeans.asm.Type;

/**
 * This class helps to extract classnames from the method/class descriptors.
 * @author Guillaume Sauthier
 */
public final class BytecodeDescriptorHelper {

    /**
     * Private default empty constructor for utility class.
     */
    private BytecodeDescriptorHelper() { }

    /**
     * Get the classname from a descriptor.
     * @param descriptor class descriptor
     * @return the classname from a descriptor
     */
    public static String getClassname(final String descriptor) {
        Type type = Type.getType(descriptor);
        return type.getClassName();
    }

    /**
     * Get the classname of the n'th parameter from a method descriptor.
     * @param descriptor method descriptor
     * @param index index of the parameter's type to extract
     * @return the classname of the n'th parameter
     */
    public static String getMethodParamClassname(final String descriptor, final int index) {
        Type[] types = Type.getArgumentTypes(descriptor);

        if (index > types.length) {
            throw new IllegalArgumentException("The given method descriptor does not have enough parameters");
        }

        return types[index].getClassName();
    }
}
