/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: BusinessInterface.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.annotations;

/**
 * Business interface of the test bean.
 * @author Florent Benoit
 */
public interface BusinessInterface {

    /**
     * Method with annotation.
     * @return true if check of annotation has been OK
     */
    boolean testMethod();

    /**
     * A method with complex annotation on the method and on the parameter.
     * @param a the first parameter
     * @param b the second parameter
     * @param c the third parameter
     * @return true if check of annotation has been OK
     */
    boolean complexAnnotationMethod(int a, int b, int c);



}
