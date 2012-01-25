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
 * $Id: BusinessInterceptorsClassesEnhancer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.interceptors.business;

import java.util.ArrayList;
import java.util.List;

import org.ow2.easybeans.tests.enhancer.ClassesEnhancer;


/**
 * Checks that interceptor enhancing of classses is ok.
 * @author Florent Benoit
 */
public final class BusinessInterceptorsClassesEnhancer {

    /**
     * Utility class, no constructor.
     */
    private BusinessInterceptorsClassesEnhancer() {

    }

    /**
     * Enhance the classes simulating a Bean.
     * @throws Exception if it fails
     */
    public static void enhance() throws Exception {
        String packageName = "org.ow2.easybeans.tests.enhancer.interceptors.business.bean.".replace(".", "/");
        String suffixClass = ".class";

        List<String> list = new ArrayList<String>();
        list.add(packageName + "Interceptor" + suffixClass);
        list.add(packageName + "SingleMethodInterceptor" + suffixClass);
        list.add(packageName + "StatelessBean" + suffixClass);
        list.add(packageName + "StatelessLocalBisItf" + suffixClass);
        list.add(packageName + "StatelessLocalItf" + suffixClass);
        list.add(packageName + "StatelessRemoteItf" + suffixClass);

        ClassesEnhancer.enhance(list, ClassesEnhancer.TYPE.INTERCEPTOR);
    }

}
