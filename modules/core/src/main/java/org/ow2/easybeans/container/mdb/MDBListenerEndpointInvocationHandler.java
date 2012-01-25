/**
 * EasyBeans
 * Copyright (C) 2010 Bull S.A.S.
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
 * $Id: MDBListenerEndpointInvocationHandler.java 5929 2011-07-25 15:24:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.ejb.Timer;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;

import org.ow2.easybeans.api.bean.EasyBeansMDB;
import org.ow2.easybeans.api.event.bean.EZBEventBeanInvocation;
import org.ow2.easybeans.event.bean.EventBeanInvocationEnd;
import org.ow2.easybeans.event.bean.EventBeanInvocationError;
import org.ow2.easybeans.naming.J2EEManagedObjectNamingHelper;
import org.ow2.util.auditreport.api.IAuditID;
import org.ow2.util.auditreport.api.ICurrentInvocationID;
import org.ow2.util.auditreport.impl.AuditIDImpl;
import org.ow2.util.event.api.IEventDispatcher;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Invocation handler used to register the EndPointListener.
 * @author Florent Benoit
 */
public class MDBListenerEndpointInvocationHandler extends MDBMessageEndPoint implements InvocationHandler {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MDBListenerEndpointInvocationHandler.class);


    /**
     * Event dispatcher.
     */
    private IEventDispatcher eventDispatcher = null;

    /**
     * Listener interface.
     */
    private Class<?> listenerInterface = null;

    /**
     * Initialize the handler with the given factory and the given instance of the MDB.
     * @param mdbMessageEndPointFactory the MDB factory
     * @param easyBeansMDB the bean instance
     * @param listenerInterface the listener interface
     */
    public MDBListenerEndpointInvocationHandler(final MDBMessageEndPointFactory mdbMessageEndPointFactory,
            final EasyBeansMDB easyBeansMDB, final Class<?> listenerInterface) {
        super(mdbMessageEndPointFactory, easyBeansMDB);
        this.listenerInterface = listenerInterface;
        this.eventDispatcher = ((MDBMessageEndPointFactory) getEasyBeansFactory()).getEventDispatcher();
    }

    /**
     * Processes a method invocation on a proxy instance and returns the result.
     * This method will be invoked on an invocation handler when a method is
     * invoked on a proxy instance that it is associated with.
     * @param proxy the proxy instance that the method was invoked on
     * @param method the <code>Method</code> instance corresponding to the
     *        interface method invoked on the proxy instance. The declaring
     *        class of the <code>Method</code> object will be the interface
     *        that the method was declared in, which may be a superinterface of
     *        the proxy interface that the proxy class inherits the method
     *        through.
     * @param args an array of objects containing the values of the arguments
     *        passed in the method invocation on the proxy instance, or
     *        <code>null</code> if interface method takes no arguments.
     *        Arguments of primitive types are wrapped in instances of the
     *        appropriate primitive wrapper class, such as
     *        <code>java.lang.Integer</code> or <code>java.lang.Boolean</code>.
     * @return the value to return from the method invocation on the proxy
     *         instance.
     * @throws Throwable the exception to throw from the method invocation on
     *         the proxy instance.
     */
    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
        String methodName = method.getName();

        if ("getEasyBeansFactory".equals(methodName)) {
            return getEasyBeansFactory();
        } else if ("getMDBMessageEndPointFactory".equals(methodName)) {
            return getMDBMessageEndPointFactory();
        } else if ("getEasyBeansMDB".equals(methodName)) {
            return getEasyBeansMDB();
        } else if ("notifyTimeout".equals(methodName)) {
            notifyTimeout((Timer) args[0]);
            return null;
        } else if ("release".equals(methodName)) {
            release();
            return null;
        } else if ("beforeDelivery".equals(methodName)) {
            beforeDelivery((Method) args[0]);
            return null;
        } else if ("afterDelivery".equals(methodName)) {
            afterDelivery();
            return null;
        }
        return invokeMethodOnMDB(method, args);
    }

    /**
     * Invoke the given method on the MDB.
     * @param method the method that needs to be called on the Message Driven Bean.
     * @param args the arguments of the method
     * @return the response of the call (or null if there is no answer)
     */
    protected Object invokeMethodOnMDB(final Method method, final Object[] args) {
        // set classloader to EJB classloader
        ClassLoader oldCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(getEasyBeansFactory().getContainer().getClassLoader());

        // Do some stuff on ID only if object is there, which means that audit
        // component is enabled and that this is an MDB JMS
        ICurrentInvocationID currentInvocationID = getMDBMessageEndPointFactory().getCurrentInvocationID();

        if (currentInvocationID != null && MessageListener.class.equals(this.listenerInterface)) {
            // Check if there is an ID in the message
            String auditIDString = null;
            try {
                auditIDString = ((Message) args[0]).getStringProperty(IAuditID.class.getName());
            } catch (JMSException e) {
                logger.error("Unable to get the ID in the JMS message", e);
            }
            if (auditIDString != null) {
                // Get the ID stored in this message
                AuditIDImpl callerID = new AuditIDImpl(auditIDString);

                // Add a new call
                AuditIDImpl newID = new AuditIDImpl();
                newID.generate();
                newID.setParentID(callerID.getLocalID());
                currentInvocationID.setAuditID(newID);

            } else {
                // New invocation is starting there
                currentInvocationID.init(null);
            }
        }

        // Create the bean invocation begin event.
        String methodEventProviderId = getMDBMessageEndPointFactory().getJ2EEManagedObjectId() + "/"
                + J2EEManagedObjectNamingHelper.getMethodSignature(method);
        EZBEventBeanInvocation event = getMDBMessageEndPointFactory().getInvocationEventBegin(methodEventProviderId, args);
        long number = event.getInvocationNumber();
        this.eventDispatcher.dispatch(event);
        EZBEventBeanInvocation endEvent = null;

        Method mdbMethod = null;
        // Bean is implementing the interface, keep the given method
        if (this.listenerInterface.isInstance(getEasyBeansMDB())) {
            mdbMethod = method;
        } else {
            // search the method with the same signature on the bean
            try {
                mdbMethod = getEasyBeansMDB().getClass().getMethod(method.getName(), method.getParameterTypes());
            } catch (SecurityException e) {
                throw new IllegalStateException("Cannot deliver the message", e);
            } catch (NoSuchMethodException e) {
                throw new IllegalStateException("Cannot deliver the message", e);
            }
        }



        // invoke by using reflection
        Object result = null;
        try {
            result = mdbMethod.invoke(getEasyBeansMDB(), args);
            endEvent = new EventBeanInvocationEnd(methodEventProviderId, number, null);
        } catch (IllegalArgumentException e) {
            logger.error("Cannot deliver the message", e);
            endEvent = new EventBeanInvocationError(methodEventProviderId, number, e);
            throw new IllegalStateException("Cannot deliver the message", e);
        } catch (IllegalAccessException e) {
            logger.error("Cannot deliver the message", e);
            endEvent = new EventBeanInvocationError(methodEventProviderId, number, e);
            throw new IllegalStateException("Cannot deliver the message", e);
        } catch (InvocationTargetException e) {
            endEvent = new EventBeanInvocationError(methodEventProviderId, number, e);
            logger.error("Cannot deliver the message", e.getTargetException());
            throw new IllegalStateException("Cannot deliver the message", e.getTargetException());
        } finally {
            // Reset classloader
            Thread.currentThread().setContextClassLoader(oldCL);
            if (currentInvocationID != null) {
                currentInvocationID.setAuditID(null);
            }
            if (endEvent != null) {
                this.eventDispatcher.dispatch(endEvent);
            }
        }
        return result;
    }

}
