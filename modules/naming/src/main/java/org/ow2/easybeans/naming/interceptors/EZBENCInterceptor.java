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
 * $Id: EZBENCInterceptor.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.interceptors;

import javax.naming.Context;
import javax.naming.NamingException;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.naming.NamingInterceptor;
import org.ow2.easybeans.naming.NamingManager;

/**
 * This interceptor sets the ENC context before calling method.
 * @author Florent Benoit
 */
public class EZBENCInterceptor extends AbsENCInterceptor implements NamingInterceptor {

    /**
     * Reference on the naming manager.
     */
    private static NamingManager namingManager = null;

    /**
     * Default constructor.
     * Gets a reference on the naming manager.
     */
    public EZBENCInterceptor() {
        if (namingManager == null) {
            try {
                namingManager = NamingManager.getInstance();
            } catch (NamingException e) {
                throw new IllegalStateException("Cannot get the naming manager", e);
            }
        }
    }

    /**
     * Sets ENC context.
     * @param invocationContext context with useful attributes on the current
     *        invocation.
     * @return result of the next invocation (to chain interceptors).
     * @throws Exception needs for signature of interceptor.
     */
    @Override
    public Object intercept(final EasyBeansInvocationContext invocationContext) throws Exception {
        Context oldContext = namingManager.setComponentContext(invocationContext.getFactory().getJavaContext());
        try {
            return invocationContext.proceed();
        } finally {
            namingManager.resetComponentContext(oldContext);
        }
    }
}
