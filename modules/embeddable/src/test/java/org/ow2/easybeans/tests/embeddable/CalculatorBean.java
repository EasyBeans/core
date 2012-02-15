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

package org.ow2.easybeans.tests.embeddable;

import javax.ejb.Stateless;

/**
 * Dummy no interface view bean.
 * @author Florent Benoit
 */
@Stateless
public class CalculatorBean {

    /**
     * Computes a + b.
     * @param a the first parameter
     * @param b the next parameter
     * @return the sum
     */
    public int add(final int a, final int b) {
        return a + b;
    }


    /**
     * Protected method should throw EJBException on no interface view calls.
     * @return constant
     */
    protected int methodShouldntBeCalled() {
        return 2503;
    }
}
