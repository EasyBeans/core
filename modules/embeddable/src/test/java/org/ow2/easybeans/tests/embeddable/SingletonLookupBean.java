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

import javax.ejb.EJB;
import javax.ejb.Singleton;

/**
 * Bean
 * @author Florent Benoit
 */
@Singleton
public class SingletonLookupBean {


    @EJB(name="myEntries/CalculatorBean")
    private CalculatorBean calculatorBean1;


    private CalculatorBean calculatorBean2;


    @EJB(lookup="java:comp/env/myEntries/CalculatorBean")
    private void setCalculatorBean2(final CalculatorBean calculatorBean) {
        this.calculatorBean2 = calculatorBean;
    }


    public int calcWithBean1(final int a, final int b) {
        return this.calculatorBean1.add(a, b);
    }

    public int calcWithBean2(final int a, final int b) {
        return this.calculatorBean2.add(a, b);
    }


    public CalculatorBean getCalculatorBean1() {
        return this.calculatorBean1;
    }


    public CalculatorBean getCalculatorBean2() {
        return this.calculatorBean2;
    }



}
