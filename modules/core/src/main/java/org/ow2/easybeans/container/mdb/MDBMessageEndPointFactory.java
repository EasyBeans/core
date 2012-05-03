/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: MDBMessageEndPointFactory.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.mdb;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.ow2.easybeans.api.EZBContainer;
import org.ow2.easybeans.api.FactoryException;
import org.ow2.easybeans.api.bean.EasyBeansMDB;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.component.itf.JMSComponent;
import org.ow2.easybeans.container.info.MessageDrivenInfo;
import org.ow2.easybeans.resolver.api.EZBJNDIResolverException;
import org.ow2.easybeans.rpc.util.Hash;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IActivationConfigProperty;
import org.ow2.util.ee.metadata.ejbjar.impl.struct.JActivationConfigProperty;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.PoolException;

/**
 * Defines a class that will manage the message end point factory for the MDB.
 * The super class will manage the pool of message end point.
 * @author Florent Benoit
 */
public class MDBMessageEndPointFactory extends MDBFactory implements MessageEndpointFactory {

    /**
     * Default name of the activation spec (JORAM).
     */
    public static final String DEFAULT_ACTIVATION_SPEC_NAME = "joramActivationSpec";

    /**
     * Default name of the destination type.
     */
    public static final String DEFAULT_DESTINATION_TYPE = "javax.jms.Queue";

    /**
     * Activation Config Property for the destination type.
     */
    public static final String DESTINATION_TYPE_PROPERTY = "destinationType";


    /**
     * Activation Config Property for the destination.
     */
    public static final String DESTINATION_PROPERTY = "destination";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MDBMessageEndPointFactory.class);

    /**
     * ActivationSpec object linked to this factory (used to activate or
     * deactive an endpoint factory (us).
     */
    private ActivationSpec activationSpec = null;

    /**
     * Resource adapter that provides the activation spec implementation.
     */
    private ResourceAdapter resourceAdapter = null;

    /**
     * JMS Component (if any).
     */
    private JMSComponent jmsComponent = null;

    /**
     * Listener interface.
     */
    private Class<?> listenerInterface = null;

    /**
     * Transacted methods.
     */
    private Map<Method, Boolean> transactedMethods = null;

    /**
     * Default constructor (delegate to super class).
     * @param className name of this factory (name of class that is managed)
     * @param container the root component of this factory.
     * @param activationSpec the activation Spec object used for
     *        activating/deactivating.
     * @param resourceAdapter the resource adapter used to activate/deactivate
     *        ourself.
     * @param jmsComponent for getting value.
     * @throws FactoryException if super constructor fails
     */
    public MDBMessageEndPointFactory(final String className, final EZBContainer container, final ActivationSpec activationSpec,
            final ResourceAdapter resourceAdapter, final JMSComponent jmsComponent) throws FactoryException {
        super(className, container);
        this.activationSpec = activationSpec;
        this.resourceAdapter = resourceAdapter;
        this.jmsComponent = jmsComponent;
        this.transactedMethods = new HashMap<Method, Boolean>();
    }

    /**
     * Init the factory.
     * @throws FactoryException if the initialization fails.
     */
    @Override
    public void init() throws FactoryException {
        super.init();

        // Load the listener interface
        String listenerInterfaceName = getMessageDrivenInfo().getMessageListenerInterface();
        if (listenerInterfaceName == null) {
            throw new FactoryException("No MessageListener interface found for MDB '" + getClassName() + "' of container '"
                    + getContainer().getName() + "'");
        }
        try {
            this.listenerInterface = getContainer().getClassLoader().loadClass(listenerInterfaceName.replace("/", "."));
        } catch (ClassNotFoundException e) {
            throw new FactoryException("Cannot load MessageListener interface '" + listenerInterfaceName + "' found for MDB '"
                    + getClassName() + "' of container '" + getContainer().getName() + "'");
        }


        initActivationSpec();

        validateActivationSpec();

        activate();
    }


    /**
     * Call setters method on the activation spec object.
     * @throws FactoryException if activation spec object is not configured.
     */
    private void initActivationSpec() throws FactoryException {

        // Get activation properties
        List<IActivationConfigProperty> properties = getMessageDrivenInfo().getActivationConfigProperties();
        // Init if null
        if (properties == null) {
            properties = new ArrayList<IActivationConfigProperty>();
        }

        // Message Destination Link to resolve ?
        String messageDestinationLink = getMessageDrivenInfo().getMessageDestinationLink();
        if (messageDestinationLink != null) {
            String jndiName = null;
            try {
                jndiName = getContainer().getConfiguration().getContainerJNDIResolver().getMessageDestinationJNDIUniqueName(
                        messageDestinationLink);
            } catch (EZBJNDIResolverException e) {
                throw new FactoryException("Unable to resolve message destination link '" + messageDestinationLink
                        + "' for bean '" + getBeanInfo().getName() + "'.", e);
            }
            properties.add(new JActivationConfigProperty("destination", jndiName));
            logger.info("Message destination link ''{0}'' resolved to ''{1}'' for bean ''{2}''", messageDestinationLink,
                    jndiName, getBeanInfo().getName());
        }

        // Check that there is a destination-type, if not, add the default
        boolean destinationFound = false;
        boolean destinationTypeFound = false;
        for (IActivationConfigProperty property : properties) {
            if (DESTINATION_TYPE_PROPERTY.equals(property.propertyName())) {
                destinationTypeFound = true;
            }
            if (DESTINATION_PROPERTY.equals(property.propertyName())) {
                destinationFound = true;
            }
        }
        if (!destinationTypeFound && destinationFound) {
            IActivationConfigProperty jActivationConfigProperty = new JActivationConfigProperty(DESTINATION_TYPE_PROPERTY,
                    DEFAULT_DESTINATION_TYPE);
            properties.add(jActivationConfigProperty);
            logger.warn("No ''{0}'' property found in the activation config, adding default value ''{1}'' for bean ''{2}''",
                    DESTINATION_TYPE_PROPERTY, DEFAULT_DESTINATION_TYPE, getBeanInfo().getName());
        }

        // Create a map with the given activation spec properties
        Map<String, String> activationConfigProperties = new HashMap<String, String>();
        for (IActivationConfigProperty property : properties) {
            activationConfigProperties.put(property.propertyName(), property.propertyValue());
        }

        logger.debug("Activation config properties are ''{0}''", activationConfigProperties);

        // JMS Component may update activation config properties if wanted
        if (this.jmsComponent != null) {
            this.jmsComponent.updateActivationConfigProperties(activationConfigProperties);
            logger.debug(
                    "The JMS Component ''{0}'' may have updated the activation config properties. New values are: ''{1}''",
                    this.jmsComponent, activationConfigProperties);
        }

        Set<Entry<String, String>> entrySet = activationConfigProperties.entrySet();
        Iterator<Entry<String, String>> it = entrySet.iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            // Apply the property on the activation spec object
            applyActivationSpecProperty(entry.getKey(), entry.getValue());
        }

    }

    /**
     * Apply the property with its given value on the activation spec object.
     * @param key the property's key
     * @param value the value of the property
     * @throws FactoryException if the value cannot be set on the activation spec
     */
    protected void applyActivationSpecProperty(final String key, final String value) throws FactoryException {

        // define setter method name
        String methodName = "set" + key.substring(0, 1).toUpperCase() + key.substring(1);

        // get Method (reflection)
        Method m = null;
        try {
            m = this.activationSpec.getClass().getMethod(methodName, new Class[] {String.class});
        } catch (SecurityException e) {
            throw new FactoryException("Cannot get a method named '" + methodName
                    + "' on activation spec object '" + this.activationSpec + "'.", e);
        } catch (NoSuchMethodException e) {
            throw new FactoryException("Cannot get a method named '" + methodName
                    + "' on activation spec object '" + this.activationSpec + "'.", e);
        }

        // invoke method
        try {
            m.invoke(this.activationSpec, value);
        } catch (IllegalArgumentException e) {
            throw new FactoryException("Cannot invoke method named '" + methodName + "' with value '" + value
                    + "' on activation spec object '" + this.activationSpec + "'.", e);
        } catch (IllegalAccessException e) {
            throw new FactoryException("Cannot invoke method named '" + methodName + "' with value '" + value
                    + "' on activation spec object '" + this.activationSpec + "'.", e);
        } catch (InvocationTargetException e) {
            throw new FactoryException("Cannot invoke method named '" + methodName + "' with value '" + value
                    + "' on activation spec object '" + this.activationSpec + "'.", e);
        }


    }


    /**
     * Validate the configuration used p, tje activation spec object.
     * @throws FactoryException if the validation of the activation spec
     *         implementation object fails.
     */
    protected void validateActivationSpec() throws FactoryException {
        try {
            this.activationSpec.validate();
        } catch (InvalidPropertyException e) {
            throw new FactoryException(
                    "Cannot validate the validation spec object for bean '" + getBeanInfo().getName() + "'.", e);
        }
    }

    /**
     * Activate this endpoint factory on resource adapter with the activation
     * spec object.
     * @throws FactoryException if the activation fails.
     */
    protected void activate() throws FactoryException {
        try {
            this.resourceAdapter.endpointActivation(this, this.activationSpec);
        } catch (ResourceException e) {
            throw new FactoryException(
                    "Cannot activate the activationspec object and us (MessageEndPointFactory) on the resource adapter",
                    e);
        }
    }

    /**
     * This is used to create a message endpoint. The message endpoint is
     * expected to implement the correct message listener type.
     * @param xaResource an optional XAResource instance used to get transaction
     *        notifications when the message delivery is transacted.
     * @return a message endpoint instance.
     * @throws UnavailableException indicates a transient failure in creating a
     *         message endpoint. Subsequent attempts to create a message
     *         endpoint might succeed.
     */
    public MessageEndpoint createEndpoint(final XAResource xaResource) throws UnavailableException {
        // Use the internal method (which return a MDBMessageEndPoint object)
        return createInternalEndpoint(xaResource);
    }


    /**
     * This is used to create a message endpoint. The message endpoint is
     * expected to implement the correct message listener type.
     * @param xaResource an optional XAResource instance used to get transaction
     *        notifications when the message delivery is transacted.
     * @return a message endpoint instance.
     * @throws UnavailableException indicates a transient failure in creating a
     *         message endpoint. Subsequent attempts to create a message
     *         endpoint might succeed.
     */
    public EZBMessageEndPoint createInternalEndpoint(final XAResource xaResource) throws UnavailableException {
        // get an instance of MDB
        EasyBeansMDB easyBeansMDB = null;
        try {
            easyBeansMDB = getPool().get();
        } catch (PoolException e) {
            throw new UnavailableException("Cannot get instance in the pool", e);
        }

        // Build a wrapper around this mdb instance
        MDBListenerEndpointInvocationHandler handler = new MDBListenerEndpointInvocationHandler(this, easyBeansMDB,
                this.listenerInterface);
        EZBMessageEndPoint proxy = (EZBMessageEndPoint) Proxy.newProxyInstance(getContainer().getClassLoader(), new Class[] {
                this.listenerInterface, EZBMessageEndPoint.class}, handler);

        // Set XAResource of the message endpoint.
        easyBeansMDB.setXaResource(xaResource);

        return proxy;
    }


    /**
     * Release an endpoint created by this factory.
     * @param mdbMessageEndPoint the endpoint to release.
     */
    protected void releaseEndPoint(final EZBMessageEndPoint mdbMessageEndPoint) {
        // Release the wrapped message driven bean
        try {
            getPool().release(mdbMessageEndPoint.getEasyBeansMDB());
        } catch (PoolException e) {
            throw new IllegalStateException("Cannot release the given message end point", e);
        }
    }

    /**
     * This is used to find out whether message deliveries to a target method on
     * a message listener interface that is implemented by a message endpoint
     * will be transacted or not. The message endpoint may indicate its
     * transacted delivery preferences (at a per method level) through its
     * deployment descriptor. The message delivery preferences must not change
     * during the lifetime of a message endpoint.
     * @param method description of a target method. This information about the
     *        intended target method allows an application server to find out
     *        whether the target method call will be transacted or not.
     * @return boolean whether the specified method is transacted
     * @throws NoSuchMethodException exception to throw
     */
    public boolean isDeliveryTransacted(final Method method) throws NoSuchMethodException {

        // Check if already computed
        Boolean isTransacted = this.transactedMethods.get(method);

        // existing value, return it
        if (isTransacted != null) {
            return isTransacted.booleanValue();
        }

        // No value, needs to find the tx attribute for this method

        // first, get the info for this method
        MessageDrivenInfo messageDrivenInfo = (MessageDrivenInfo) getBeanInfo();
        List<IMethodInfo> methodInfos = messageDrivenInfo.getBusinessMethodsInfo();
        long methodHash = Hash.hashMethod(method);

        if (methodInfos != null) {
            for (IMethodInfo methodInfo : methodInfos) {
                // check if there is a match
                long hashTempMethod = Hash.hashMethod(methodInfo.getName(), methodInfo.getDescriptor());
                if (methodHash == hashTempMethod) {
                    // found a match !
                    boolean tmpBoolean = methodInfo.isTransacted();
                    this.transactedMethods.put(method, Boolean.valueOf(tmpBoolean));
                    return tmpBoolean;
                }

            }
            // no matching method found, add false
            this.transactedMethods.put(method, Boolean.FALSE);
        }

        // no info found, expect it is false
        return false;
    }

    /**
     * Stops the factory.
     */
    @Override
    public void stop() {
        // stop the pool.
        super.stop();

        // deactivate this factory
        this.resourceAdapter.endpointDeactivation(this, this.activationSpec);

    }




    /**
     * Notified when the timer service send a Timer object.
     * It has to call the Timed method.
     * @param timer the given timer object that will be given to the timer method.
     */
    public void notifyTimeout(final Timer timer) {
        // Get an EndPoint
        EZBMessageEndPoint mdbMessageEndPoint = null;
        try {
            mdbMessageEndPoint = createInternalEndpoint(null);
        } catch (UnavailableException e) {
            throw new EJBException("Cannot get an endpoint for notifying the timeout", e);
        }

        // Call the timeout method
        try {
            mdbMessageEndPoint.notifyTimeout(timer);
        } finally {
            // release the endpoint
            releaseEndPoint(mdbMessageEndPoint);
        }

    }

}

