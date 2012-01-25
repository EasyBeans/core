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
 * $Id: AnnotationTestCase.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.annotations;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.tests.enhancer.ClassesEnhancer;
import org.ow2.easybeans.tests.enhancer.annotations.StatelessBean;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Allow to test if annotations are correctly moved.
 * @author Florent Benoit
 */
public class AnnotationTestCase {

    /**
     * Bean (stateless) tested.
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
            String packageName = "org.ow2.easybeans.tests.enhancer.annotations.".replace(".", "/");
            String suffixClass = ".class";

            List<String> list = new ArrayList<String>();
            list.add(packageName + "StatelessBean" + suffixClass);
            list.add(packageName + "BusinessInterface" + suffixClass);
            ClassesEnhancer.enhance(list, ClassesEnhancer.TYPE.INTERCEPTOR);
            enhancingDone = true;
        }
        this.statelessBean = new StatelessBean();
    }

    /**
     * Test if annotation has been moved.
     */
    @Test
    public void testComplexAnnotation() {
        Assert.assertTrue(this.statelessBean.testMethod(), "Annotation check");
    }

    /**
     * Test if annotation has been moved.
     */
    @Test
    public void testComplexAnnotation2() {
        Assert.assertTrue(this.statelessBean.complexAnnotationMethod(1, 2, 1), "Annotation check");
    }

}
