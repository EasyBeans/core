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
 * $Id: EasyBeansSessionContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.session;

import java.util.List;

import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;
import javax.ejb.SessionContext;
import javax.transaction.Transaction;

import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.container.EZBSessionContext;
import org.ow2.easybeans.container.EasyBeansEJBContext;
import org.ow2.easybeans.proxy.helper.ProxyHelper;

/**
 * Defines the Session Context used by Stateless and Stateful beans.
 * @param <FactoryType> a factory.
 * @author Florent Benoit
 */
public class EasyBeansSessionContext<FactoryType extends SessionFactory<?>> extends EasyBeansEJBContext<FactoryType> implements
        EZBSessionContext<FactoryType>, SessionContext {

    /**
     * Transaction used by this bean. (Used by stateful bean).
     */
    private Transaction beanTransaction = null;

    /**
     * Gets the transaction used by this bean.
     * @return the bean transaction.
     */
    public Transaction getBeanTransaction() {
        return this.beanTransaction;
    }

    /**
     * Sets the transaction used by this bean.
     * @param beanTransaction the bean transaction.
     */
    public void setBeanTransaction(final Transaction beanTransaction) {
        this.beanTransaction = beanTransaction;
    }


    /**
     * Build a new Session context.
     * @param factory the factory on which we are linked.
     */
    public EasyBeansSessionContext(final FactoryType factory) {
        super(factory);
    }

    /**
     * Obtain a reference to the EJB local object that is associated with the
     * instance. An instance of a session enterprise Bean can call this method
     * at anytime between the ejbCreate() and ejbRemove() methods, including
     * from within the ejbCreate() and ejbRemove() methods. An instance can use
     * this method, for example, when it wants to pass a reference to itself in
     * a method argument or result.
     * @return The EJB local object currently associated with the instance.
     * @throws java.lang.IllegalStateException - Thrown if the instance invokes
     *         this method while the instance is in a state that does not allow
     *         the instance to invoke this method, or if the instance does not
     *         have a local interface.
     */
    public EJBLocalObject getEJBLocalObject() throws java.lang.IllegalStateException {
        throw new IllegalStateException("No getEJBLocalObject() method");
    }

    /**
     * Obtain a reference to the EJB object that is currently associated with
     * the instance. An instance of a session enterprise Bean can call this
     * method at anytime between the ejbCreate() and ejbRemove() methods,
     * including from within the ejbCreate() and ejbRemove() methods. An
     * instance can use this method, for example, when it wants to pass a
     * reference to itself in a method argument or result.
     * @return The EJB object currently associated with the instance.
     * @throws java.lang.IllegalStateException - Thrown if the instance invokes
     *         this method while the instance is in a state that does not allow
     *         the instance to invoke this method, or if the instance does not
     *         have a remote interface.
     */
    public EJBObject getEJBObject() throws java.lang.IllegalStateException {
        throw new IllegalStateException("No getEJBObject() method");
    }

    /**
     * Obtain a reference to the JAX-RPC MessageContext. An instance of a
     * stateless session bean can call this method from any business method
     * invoked through its web service endpoint interface.
     * @return The MessageContext for this web service invocation.
     * @throws java.lang.IllegalStateException - Thrown if this method is
     *         invoked while the instance is in a state that does not allow
     *         access to this method.
     */
    public javax.xml.rpc.handler.MessageContext getMessageContext() throws java.lang.IllegalStateException {
        throw new IllegalStateException("No getMessageContext() method");
    }

    /**
     * Obtain an object that can be used to invoke the current bean through the
     * given business interface.
     * @param <T> the interface of the bean
     * @param businessInterface One of the local business interfaces or remote
     *        business interfaces for this session bean.
     * @return The business object corresponding to the given business
     *         interface.
     * @throws IllegalStateException - Thrown if this method is invoked with an
     *         invalid business interface for the current bean.
     */
    public <T> T getBusinessObject(final Class<T> businessInterface) throws IllegalStateException {
        if (businessInterface == null) {
            throw new IllegalStateException("Invalid business interface '" + businessInterface + "'.");
        }

        String businessInterfaceClassname = businessInterface.getName();

        // Check if the given interface is a correct interfaces
        IBeanInfo beanInfo = getFactory().getBeanInfo();
        List<String> localInterfaces = beanInfo.getLocalInterfaces();
        List<String> remoteInterfaces = beanInfo.getRemoteInterfaces();

        // Not a business interfaces
        if (!localInterfaces.contains(businessInterfaceClassname) && !remoteInterfaces.contains(businessInterfaceClassname)) {
            throw new IllegalStateException("The interface '" + businessInterface
                    + "' is not a valid interface for this bean '" + beanInfo.getName() + "'. Valid Local Interfaces are '"
                    + localInterfaces + "' and remote interfaces '" + remoteInterfaces + "'.");
        }

        // Now build a local or remote proxy
        boolean localInterface = false;
        if (localInterfaces.contains(businessInterfaceClassname)) {
            localInterface = true;
        }

        // Return a new proxy
        return ProxyHelper.getProxy(getFactory(), businessInterface, localInterface);




    }

    /**
     * Obtain the business interface through which the current business method
     * invocation was made.
     * @return the business interface through which the current business method
     *         invocation was made.
     * @throws IllegalStateException - Thrown if this method is called and the
     *         bean has not been invoked through a business interface.
     */
    public Class<?> getInvokedBusinessInterface() throws IllegalStateException {

        String invokedBusinessInterfaceName = getFactory().getInvokedBusinessInterfaceNameThreadLocal().get();
        if (invokedBusinessInterfaceName == null) {
            throw new IllegalStateException("This method has not be called through a business interface");
        }

        // load the class
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(invokedBusinessInterfaceName);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Unable to load the business interface class '" + invokedBusinessInterfaceName
                    + "'.", e);
        }
    }


    /**
     * Check whether a client invoked the cancel() method on the client Future object corresponding to the currently executing
     * asynchronous business method.
     * @return true if the client has invoked Future.cancel with a value of true for the mayInterruptIfRunning parameter.
     * @throws IllegalStateException - Thrown if not invoked from within an asynchronous business method invocation with return
     * type Future.
     */
    public boolean wasCancelCalled() throws IllegalStateException {
        //TODO: implement !
        return false;
    }

}
