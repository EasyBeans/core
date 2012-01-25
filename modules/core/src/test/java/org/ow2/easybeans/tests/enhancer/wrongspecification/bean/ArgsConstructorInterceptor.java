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
 * $Id: ArgsConstructorInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.tests.enhancer.wrongspecification.bean;


/**
 * This class has only an args constructor to an interceptor.
 * It isn't in agreement with the JSR 220 - EJB 3.0 Spec.
 * @author Eduardo Studzinski Estima de Castro
 * @author Gisele Pinheiro Souza
 */
public class ArgsConstructorInterceptor {

    /**
     * This constructor has args and interceptors shouldn't have arg constructors.
     * An interceptor must have no-args constructor.
     * @param arg1 value
     * @param arg2 value
     */
    public ArgsConstructorInterceptor(final Integer arg1, final Integer arg2){
        throw new IllegalStateException("This constructor has args and this isn't valid to the JSR 220 - EJB 3.0 Spec.");
    }
}
