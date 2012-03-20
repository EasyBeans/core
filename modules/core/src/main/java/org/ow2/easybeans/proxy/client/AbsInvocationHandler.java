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
 * $Id: AbsInvocationHandler.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.client;

import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.rmi.Remote;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.EJBException;

import org.ow2.easybeans.rpc.api.RPCException;

/**
 * Abstract class used by remote or local invocation handler.
 * @author Florent Benoit
 */
public abstract class AbsInvocationHandler implements InvocationHandler, Serializable {

    /**
     * Serial Version UID.
     */
    private static final long serialVersionUID = -8177576962615463120L;

    /**
     * Bean has been removed.
     */
    private boolean removed = false;

    /**
     * Container id.
     */
    private String containerId = null;

    /**
     * Factory name.
     */
    private String factoryName = null;

    /**
     * Bean id.
     */
    private Long beanId = null;

    /**
     * Map between method and its hash.
     */
    private transient Map<Method, Long> hashedMethods = null;

    /**
     * Interface used by this handler.
     */
    private String interfaceClassName = null;

    /**
     * Boolean used to know if this class extends java.rmi.Remote class.
     */
    private boolean isItfExtendingRmiRemote = false;

    /**
     * useID true if all instance build with this ref are unique (stateful), false if it references the same object (stateless).
     */
    private boolean useID = false;

    /**
     * This handler is used for getBusinessObject() method ?
     */
    private boolean businessObjectMode = false;

    /**
     * Build a new Invocation handler.
     * @param containerId the id of the container that will be called on the
     *        remote side.
     * @param factoryName the name of the remote factory.
     * @param useID true if all instance build with this ref are unique
     *        (stateful), false if it references the same object (stateless)
     */
    public AbsInvocationHandler(final String containerId, final String factoryName, final boolean useID) {
        this.containerId = containerId;
        this.factoryName = factoryName;
        this.useID = useID;
        this.hashedMethods = new HashMap<Method, Long>();
    }

    /**
     * Manages all methods of java.lang.Object class.
     * @param proxy the proxy instance receiving the call
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance. The declaring
     *        class of the <code>Method</code> object will be the interface
     *        that the method was declared in, which may be a superinterface of
     *        the proxy interface that the proxy class inherits the method
     *        through.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance
     * @return the value of the called method.
     */
    protected Object handleObjectMethods(final Object proxy, final Method method, final Object[] args) {
        String methodName = method.getName();

        if (methodName.equals("equals")) {
            if (args != null && args.length > 0) {
                if (args[0] == null) {
                    return Boolean.FALSE;
                }
                // Needs to compute toString() on both objects
                String localValue = proxy.toString();
                String otherValue = args[0].toString();
                return Boolean.valueOf(localValue.equals(otherValue));
            }
            return Boolean.FALSE;
        } else if (methodName.equals("toString")) {
            return toString();
        } else if (methodName.equals("hashCode")) {
            return Integer.valueOf(toString().hashCode());
        } else {
            throw new IllegalStateException("Method '" + methodName + "' is not present on Object.class.");
        }
    }

    /**
     * Handle the given throwable and throw the correct exception to the client.
     * @param originalThrowable the exception that has been thrown on the server side.
     * @param isApplicationException true if it is an application exception, else false.
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance.
     * @param rpcException the RPC exception if any
     * @throws Exception the exception that has been wrapped or not for the client.
     */
    protected void handleThrowable(final Throwable originalThrowable, final boolean isApplicationException,
            final Method method, final RPCException rpcException) throws Exception {

        // Handle the exception
        // Checked application exception --> throw exception without wrapping it.

        Class<?>[] exceptions = method.getExceptionTypes();
        if (exceptions != null) {
            for (Class<?> clazz : exceptions) {
                // Is an Exception but not a runtime exception
                if (clazz.isInstance(originalThrowable)
                        && originalThrowable instanceof Exception
                        && !(originalThrowable instanceof RuntimeException)) {
                    throw (Exception) originalThrowable;
                }
            }
        }

        // EJBException --> throw exception without wrapping it.
        if (originalThrowable instanceof EJBException) {
            throw (EJBException) originalThrowable;
        }

        // Runtime Exception :
        //  1/ It's an application exception : throw exception without wrapping it.
        //  2/ It's not an application exception : Wrap exception in an EJBException.
        if (originalThrowable instanceof RuntimeException) {
            if (isApplicationException) {
                throw (RuntimeException) originalThrowable;
            }
            // not an application exception
            throw new EJBException((RuntimeException) originalThrowable);
        }

        // Exception but not checked (wrap it in an EJBException)
        if (originalThrowable instanceof Exception) {
            throw new EJBException((Exception) originalThrowable);
        }

        // Other case (throwable): Wrap throwable in an Exception and then in an EJBException
        // First wrap is required as EJBException accept only Exception, not throwable
        if (rpcException != null) {
            throw new EJBException(rpcException);
        }
        // Else use the throwable
        throw new EJBException(new Exception("Unexpected Exception", originalThrowable));

    }

    /**
     * Sets the id of the bean.
     * @param beanId the new ID.
     */
    public void setBeanId(final Long beanId) {
        this.beanId = beanId;
    }

    /**
     * Sets the hashed methods.
     * @param hashedMethods the hash for each method.
     */
    protected void setHashedMethods(final Map<Method, Long> hashedMethods) {
        this.hashedMethods = hashedMethods;
    }

    /**
     * Gets the bean id.
     * @return the bean id.
     */
    public Long getBeanId() {
        return this.beanId;
    }

    /**
     * @return the container id.
     */
    protected String getContainerId() {
        return this.containerId;
    }

    /**
     * @return the name of the factory.
     */
    public String getFactoryName() {
        return this.factoryName;
    }

    /**
     * @return the hashes for each method.
     */
    protected Map<Method, Long> getHashedMethods() {
        return this.hashedMethods;
    }

    /**
     * Sets the container ID.
     * @param containerId the identifier of the container.
     */
    protected void setContainerId(final String containerId) {
        this.containerId = containerId;
    }

    /**
     * Sets the factory's name.
     * @param factoryName the name of the factory.
     */
    protected void setFactoryName(final String factoryName) {
        this.factoryName = factoryName;
    }

    /**
     * @return the name of the interface that represents this handler.
     */
    public String getInterfaceClassName() {
        return this.interfaceClassName;
    }

    /**
     * Sets the name of the interface that represents this handler.
     * @param interfaceClassName the name of the interface.
     */
    protected void setInterfaceClassName(final String interfaceClassName) {
        this.interfaceClassName = interfaceClassName;
    }

    /**
     * Sets the interface that represents this handler.
     * @param clz the instance of the interface.
     */
    public void setInterfaceClass(final Class<?> clz) {
        if (Remote.class.isAssignableFrom(clz)) {
            this.isItfExtendingRmiRemote = true;
        }
        setInterfaceClassName(clz.getName());
    }


    /**
     * Sets the flag if interface is extending java.rmi.Remote.
     * @param isItfExtendingRmiRemote true if it extending, else false.
     */
    public void setExtendingRmiRemote(final boolean isItfExtendingRmiRemote) {
        this.isItfExtendingRmiRemote = isItfExtendingRmiRemote;
    }

    /**
     * @return true if the interface used by this handler is extending java.rmi.Remote.
     */
    public boolean isExtendingRmiRemote() {
        return this.isItfExtendingRmiRemote;
    }

    /**
     * Sets the flag  if all instance build with this ref are unique or not.
     * @param useID true : (stateful), false if it references the same object (stateless).
     */
    public void setUseID(final boolean useID) {
        this.useID = useID;
    }

    /**
     * @return true : (stateful), false if it references the same object (stateless).
     */
    public boolean isUsingID() {
        return this.useID;
    }

    /**
     * Gets a string representation for this handler.
     * @return a string representation for this handler.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.factoryName);
        sb.append("_");
        sb.append(this.interfaceClassName);
        sb.append("/");
        sb.append(this.containerId);
        if (this.useID) {
            sb.append("@");
            if (this.beanId != null) {
                sb.append(this.beanId);
            } else {
                sb.append(System.identityHashCode(this));
            }
        }
        return sb.toString();
    }

    /**
     * @return true if the bean has been removed
     */
    public boolean isRemoved() {
        return this.removed;
    }

    /**
     * Sets the removed flag.
     * @param removed if bean has been removed.
     */
    public void setRemoved(final boolean removed) {
        this.removed = removed;
    }

    /**
     * Sets this handler as business object.
     * @param businessObjectMode the boolean value
     */
    public void setBusinessObjectMode(final boolean businessObjectMode) {
        this.businessObjectMode = businessObjectMode;
    }

    /**
     * @return true if this handler is used for getBusiness object method
     */
    public boolean isBusinessObjectMode() {
        return this.businessObjectMode;
    }
}
