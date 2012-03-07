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
 * $Id: DynamicInvocationContextImpl.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.interceptor.EZBInterceptorInvoker;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Manages an invocation context implementation that is provided to
 * interceptors/beans.
 * @author Florent benoit
 */
public class DynamicInvocationContextImpl implements EasyBeansInvocationContext {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(DynamicInvocationContextImpl.class);

    /**
     * Map shared between all interceptors.
     */
    private Map<String, Object> contextData;

    /**
     * Parameters of the method invocation.
     */
    private Object[] parameters;

    /**
     * Instance of the bean (the target of the interceptor).
     */
    private EasyBeansBean instance;

    /**
     * Timer object.
     */
    private Object timer;

    /**
     * List of invokers for this invocation context.
     */
    private List<EZBInterceptorInvoker> interceptorInvokerList;

    /**
     * Intercepted method.
     */
    private Method method = null;

    /**
     * Index of the invokers. It is increased after each call on the proceed
     * method.
     */
    private int index;

    /**
     * Manager of interceptors (used to get interceptor instances).
     */
    private EZBInterceptorManager interceptorManager;

    /**
     * Context used in a lifecycle interceptor.
     */
    private boolean isLifeCycleMode = false;

    /**
     * Build a new invocation context for the given method.
     * @param instance the bean's instance that will act as the target object.
     * @param interceptorInvokerList the list of the invokers to call
     * @param interceptorManager the manager of interceptor lifecycle
     * @param isLifeCycleMode true if interceptor chain is for lifecycle mode
     * @param method the intercepted method (may be null)
     * @param parameters the parameters of the method (may be null)
     */
    public DynamicInvocationContextImpl(final EasyBeansBean instance, final List<EZBInterceptorInvoker> interceptorInvokerList,
            final EZBInterceptorManager interceptorManager, final boolean isLifeCycleMode, final Method method, final Object... parameters) {
        LOGGER
                .debug(
                        "Creating DynamicInvocationContextImpl for instance ''{0}'', interceptor Invoker list ''{1}'', interceptor manager ''{2}'', method ''{3}'', parameters ''{4}''",
                        instance, interceptorInvokerList, interceptorManager, method, Arrays.asList(parameters));
        this.index = 0;
        this.instance = instance;
        this.interceptorInvokerList = interceptorInvokerList;
        this.interceptorManager = interceptorManager;
        this.isLifeCycleMode = isLifeCycleMode;
        this.method = method;
        this.parameters = parameters;
    }

    /**
     * Calling the next interceptor in the chain.
     * @throws Exception if there is a failure in the call
     * @return the invocation result
     */
    public Object proceed() throws Exception {
        // index is too high
        if (this.index == this.interceptorInvokerList.size()) {
            this.index = this.interceptorInvokerList.size() - 1;
        }

        try {
            if (getFactory() != null) {
                getFactory().getContextDataThreadLocal().set(getContextData());
            }
            return this.interceptorInvokerList.get(this.index++).invoke(this, this.interceptorManager);
        } finally {
            if (getFactory() != null) {
                getFactory().getContextDataThreadLocal().set(null);
            }
        }
    }

    /**
     * @return the factory of the invocation context.
     */
    public Factory<?, ?> getFactory() {
        return this.instance.getEasyBeansFactory();
    }

    /**
     * Build a new empty map if there is none.
     * @return the shared map.
     */
    public Map<String, Object> getContextData() {
        if (this.contextData == null) {
            this.contextData = new HashMap<String, Object>();
        }
        return this.contextData;
    }

    /**
     * Gets the intercepted method of the bean.
     * @return intercepted method
     */
    public Method getMethod() {
        if (this.isLifeCycleMode) {
            return null;
        }

        return this.method;
    }

    /**
     * @return the parameters of the method call.
     */
    public Object[] getParameters() {
        if (this.isLifeCycleMode) {
            throw new IllegalStateException("getParameters method shouldn't be called on lifecycle interceptors");
        }
        return getInternalParameters();
    }

    /**
     * For internal use, avoid to get an exception for invoking lifecycle method by using the parameters.
     * @return the parameters of the method call.
     */
    protected Object[] getInternalParameters() {
        return this.parameters;
    }


    /**
     * @return the bean instance.
     */
    public Object getTarget() {
        return this.instance;
    }

    /**
     * @return the bean instance.
     */
    public Object getTimer() {
        return this.timer;
    }


    /**
     * Change the parameters of the current invocation by replacing the previous
     * parameters.
     * @param parameters the new parameters
     */
    public void setParameters(final Object[] parameters) {
        if (this.isLifeCycleMode) {
            throw new IllegalStateException("setParameters method shouldn't be called on lifecycle interceptors");
        }

        Class<?>[] types = this.method.getParameterTypes();
        if (types.length > 0 && parameters == null) {
            throw new IllegalArgumentException("Invalid null argument. Expecting '" + types + "' and got '"
                    + parameters + "'.");
        }

        // Check if parameters length are ok
        if (types.length != parameters.length) {
            throw new IllegalArgumentException("Invalid size of the parameters. Expecting '" + types + "' and got '"
                    + parameters + "'.");
        }

        // Now check the types
        for (int i = 0; i < types.length; i++) {
            Class<?> type = convertType(types[i]);
            if (parameters[i] != null) {
                // Check if type is a super type
                if (!type.isAssignableFrom((parameters[i].getClass()))) {
                    throw new IllegalArgumentException("Expecting '" + types + "' and got '" + parameters + "'.");
                }
            }
        }


        this.parameters = parameters;
    }

    /**
     * Convert primitive type to object class.
     * @param clazz the given class to use
     * @return the updated class
     */
    private Class<?> convertType(final Class<?> clazz) {
        if (Boolean.TYPE.equals(clazz)) {
            return Boolean.class;
        } else if (Byte.TYPE.equals(clazz)) {
            return Byte.class;
        } else  if (Character.TYPE.equals(clazz)) {
            return Character.class;
        } else  if (Short.TYPE.equals(clazz)) {
            return Short.class;
        } else  if (Float.TYPE.equals(clazz)) {
            return Float.class;
        } else  if (Long.TYPE.equals(clazz)) {
            return Long.class;
        } else  if (Double.TYPE.equals(clazz)) {
            return Double.class;
        }
        return clazz;
    }

    /**
     * Description of this object.
     * @return string value of this object
     */
    @Override
    public String toString() {
        // create a string representation of the invocation context
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append("[");
        int j = 1;
        for (EZBInterceptorInvoker invoker : this.interceptorInvokerList) {
            sb.append("\n  [").append(j).append("/").append(this.interceptorInvokerList.size()).append("] = ").append(invoker);
            if (j == this.index) {
                sb.append("  <-- current");
            }
            j++;
        }
        sb.append("]");
        return sb.toString();
    }


}
