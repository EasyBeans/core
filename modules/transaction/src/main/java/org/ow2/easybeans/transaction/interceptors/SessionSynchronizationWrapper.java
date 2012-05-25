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

package org.ow2.easybeans.transaction.interceptors;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.List;

import javax.ejb.EJBException;
import javax.ejb.SessionSynchronization;

import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Wrap a bean around a Session Synchronization.
 * @author Florent Benoit
 */
public class SessionSynchronizationWrapper implements SessionSynchronization {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(SessionSynchronizationWrapper.class);

    /**
     * Bean wrapped into a session synchronization object.
     */
    private Object wrappedBean = null;

    /**
     * afterBegin method to call on the wrapped object (if any).
     */
    private Method afterBeginMethod = null;

    /**
     * beforeCompletion method to call on the wrapped object (if any).
     */
    private Method beforeCompletionMethod = null;

    /**
     * afterCompletion method to call on the wrapped object (if any).
     */

    private Method afterCompletionMethod = null;


    /**
     * Build a new Wrapper for the given bean and the given specified methods
     * (can be at more 3 callbacks as we can only implement some of them).
     * @param wrappedBean the bean being wrapped
     * @param synchroMethodsInfoList the list of the methods
     *        (afterBegin/beforeCompletion/afterCompletion)
     */
    public SessionSynchronizationWrapper(final Object wrappedBean, final List<IMethodInfo> synchroMethodsInfoList) {
        this.wrappedBean = wrappedBean;

        // afterBegin ?
        for (IMethodInfo methodInfo : synchroMethodsInfoList) {
            if (methodInfo.isAfterBegin()) {
                // get the method
                this.afterBeginMethod = getMethod(wrappedBean, methodInfo);
            }

            if (methodInfo.isBeforeCompletion()) {
                // get the method
                this.beforeCompletionMethod = getMethod(wrappedBean, methodInfo);
            }

            if (methodInfo.isAfterCompletion()) {
                // get the method
                this.afterCompletionMethod = getMethod(wrappedBean, methodInfo);
            }
        }
    }


    /**
     * Helper method used to get a given method for the specified parameters.
     * @param wrappedBean the wrapped bean
     * @param methodInfo the data on the given method
     * @return the found method if any
     */
    private Method getMethod(final Object wrappedBean, final IMethodInfo methodInfo) {
        Class<?>[] params = null;
        if (methodInfo.isAfterCompletion()) {
            params = new Class[] {boolean.class};
        }

        Method m = null;

        try {
            m = wrappedBean.getClass().getDeclaredMethod(methodInfo.getName(), params);
        } catch (SecurityException e) {
            this.logger.error("Unable to get callback method '" + methodInfo.getName() + "' in the class '"
                    + wrappedBean.getClass() + "'.", e);
        } catch (NoSuchMethodException e) {

            // Try with getMethod
            try {
                m = wrappedBean.getClass().getMethod(methodInfo.getName(), params);
            } catch (SecurityException e1) {
                this.logger.error("Unable to get callback method '" + methodInfo.getName() + "' in the class '"
                        + wrappedBean.getClass() + "'.", e);
            } catch (NoSuchMethodException e1) {
                this.logger.debug("Unable to get callback method '" + methodInfo.getName() + "' in the class '"
                        + wrappedBean.getClass() + "'.", e);
            }
        }
        return m;
    }

    /**
     * Helper used to invoke the given method with the given parameters.
     * @param m the method to invoke
     * @param params the parameters of the method
     */
    private void invoke(final Method m, final Object...params) {
        if (m == null) {
            return;
        }

        boolean accessible = m.isAccessible();

        try {
            m.setAccessible(true);
            m.invoke(this.wrappedBean, params);
        } catch (IllegalArgumentException e) {
            throw new EJBException("Exception on a SessionSynchronization callback", e);
        } catch (IllegalAccessException e) {
            throw new EJBException("Exception on a SessionSynchronization callback", e);
        } catch (InvocationTargetException e) {
            // Got exception in the callback
            Throwable t = e.getCause();
            if (t instanceof Exception) {
                throw new EJBException("Exception on a SessionSynchronization callback", (Exception) t);
            }
            throw new EJBException("Exception on a SessionSynchronization callback", new Exception(t));

        } finally {
            m.setAccessible(accessible);
        }

    }


    /**
     * The afterBegin method notifies a session Bean instance that a new
     * transaction has started, and that the subsequent business methods on the
     * instance will be invoked in the context of the transaction. The instance
     * can use this method, for example, to read data from a database and cache
     * the data in the instance fields. This method executes in the proper
     * transaction context.
     * @throws EJBException Thrown by the method to indicate a failure caused by
     *         a system-level error.
     * @throws RemoteException This exception is defined in the method signature
     *         to provide backward compatibility for enterprise beans written
     *         for the EJB 1.0 specification. Enterprise beans written for the
     *         EJB 1.1 and higher specifications should throw the
     *         javax.ejb.EJBException instead of this exception. Enterprise
     *         beans written for the EJB 2.0 and higher specifications must not
     *         throw the java.rmi.RemoteException.
     */
    public void afterBegin() throws EJBException, RemoteException {
        invoke(this.afterBeginMethod);
    }

    /**
     * The beforeCompletion method notifies a session Bean instance that a
     * transaction is about to be committed. The instance can use this method,
     * for example, to write any cached data to a database. This method executes
     * in the proper transaction context. Note: The instance may still cause the
     * container to rollback the transaction by invoking the setRollbackOnly()
     * method on the instance context, or by throwing an exception.
     * @throws EJBException Thrown by the method to indicate a failure caused by
     *         a system-level error.
     * @throws RemoteException This exception is defined in the method signature
     *         to provide backward compatibility for enterprise beans written
     *         for the EJB 1.0 specification. Enterprise beans written for the
     *         EJB 1.1 and higher specification should throw the
     *         javax.ejb.EJBException instead of this exception. Enterprise
     *         beans written for the EJB 2.0 and higher specifications must not
     *         throw the java.rmi.RemoteException.
     */
    public void beforeCompletion() throws EJBException, RemoteException {
        invoke(this.beforeCompletionMethod);
    }

    /**
     * The afterCompletion method notifies a session Bean instance that a
     * transaction commit protocol has completed, and tells the instance whether
     * the transaction has been committed or rolled back. This method executes
     * with no transaction context. This method executes with no transaction
     * context.
     * @param committed True if the transaction has been committed, false if is
     *        has been rolled back.
     * @throws EJBException Thrown by the method to indicate a failure caused by
     *         a system-level error.
     * @throws RemoteException This exception is defined in the method signature
     *         to provide backward compatibility for enterprise beans written
     *         for the EJB 1.0 specification. Enterprise beans written for the
     *         EJB 1.1 and higher specification should throw the
     *         javax.ejb.EJBException instead of this exception. Enterprise
     *         beans written for the EJB 2.0 and higher specifications must not
     *         throw the java.rmi.RemoteException.
     */
    public void afterCompletion(final boolean committed) throws EJBException, RemoteException {
        invoke(this.afterCompletionMethod, Boolean.valueOf(committed));
    }

}
