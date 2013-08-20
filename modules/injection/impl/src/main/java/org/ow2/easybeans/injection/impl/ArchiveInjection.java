/**
 * EasyBeans
 * Copyright (C) 2008-2009 Bull S.A.S.
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
 * $Id: ArchiveInjection.java 6088 2012-01-16 14:01:51Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.injection.impl;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.injection.api.ArchiveInjectionException;
import org.ow2.easybeans.resolver.api.EZBJNDIBeanData;
import org.ow2.easybeans.resolver.api.EZBJNDIData;
import org.ow2.easybeans.resolver.api.EZBRemoteJNDIResolver;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.CARDeployable;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;
import org.ow2.util.ee.deploy.api.deployable.metadata.DeployableMetadataException;
import org.ow2.util.ee.deploy.api.helper.DeployableHelperException;
import org.ow2.util.ee.deploy.impl.helper.DeployableHelper;
import org.ow2.util.ee.metadata.car.api.ICarMetadata;
import org.ow2.util.ee.metadata.common.api.ICommonClassMetadata;
import org.ow2.util.ee.metadata.common.api.ICommonMethodMetadata;
import org.ow2.util.ee.metadata.common.api.struct.IJAnnotationResource;
import org.ow2.util.ee.metadata.common.api.struct.IJEjbEJB;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxPersistenceUnit;
import org.ow2.util.ee.metadata.common.api.struct.IJaxwsWebServiceRef;
import org.ow2.util.ee.metadata.common.impl.helper.MethodHelper;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.impl.metadata.JClass;
import org.ow2.util.scan.impl.metadata.JField;
import org.ow2.util.scan.impl.metadata.JMethod;

/**
 * This class manages the injection.
 * @author Florent Benoit
 */
public class ArchiveInjection {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(ArchiveInjection.class);

    /**
     * Defines java.lang.Object class.
     */
    public static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Size of the getter prefix.
     */
    private static final int GETTER_LENGTH = "get".length();

    /**
     * Archive that will be used to analyze the classes (may be null).
     */
    private IArchive archive = null;

    /**
     * Analyze of the archive has been done ?
     */
    private boolean analyzed = false;

    /**
     * Remote JNDI Resolver (if present).
     */
    private EZBRemoteJNDIResolver jndiResolver = null;

    /**
     * Collection of metadata that have been analyzed.
     */
    private Collection<IClassMetadata> metadataCollection = null;

    /**
     * Default constructor.
     * @param archive the given archive on which injection is based.
     */
    public ArchiveInjection(final IArchive archive) {
        this.archive = archive;
    }

    /**
     * Constructor for users that already have the metadatas.
     * @param carMetadata the metadatas to use.
     */
    public ArchiveInjection(final ICarMetadata carMetadata) {
        this.metadataCollection = carMetadata.getClassMetadataCollection();
        this.analyzed = true;
    }

    /**
     * Analyze the archive.
     * @param classLoader classloader used for the analyze
     * @throws ArchiveInjectionException if analyze fails
     */
    public void analyze(final ClassLoader classLoader) throws ArchiveInjectionException {

        // Get Deployable
        IDeployable<?> deployable = null;
        try {
            deployable = DeployableHelper.getDeployable(this.archive);
        } catch (DeployableHelperException e) {
            throw new ArchiveInjectionException("Unable to get a deployable on archive '" + this.archive + "'.", e);
        }

        // Client Archive ?
        if (CARDeployable.class.isInstance(deployable)) {
            // Create metadata
            ICarDeployableMetadata carDeployableMetadata = null;
            try {
                carDeployableMetadata = new CarDeployableMetadataFactory().createDeployableMetadata(CARDeployable.class
                        .cast(deployable), classLoader);
            } catch (DeployableMetadataException e) {
                throw new ArchiveInjectionException("Unable to get metadata on archive '" + this.archive + "'.", e);
            }

            // Use only common part
            this.metadataCollection = carDeployableMetadata.getCarClassMetadataCollection();
        } else {
            // TODO : adapt for other archives
            throw new UnsupportedOperationException("Can only manage injection for Client Deployable. Deployable found is '"
                    + deployable + "'.");
        }

        // This archive has been analyzed
        this.analyzed = true;
    }

    /**
     * Initialize stuff in the given class.
     * @param clazz the class to initialize
     * @throws ArchiveInjectionException if injection fails
     */
    public void init(final Class<?> clazz) throws ArchiveInjectionException {
        // Analyze the metadata if not done
        if (!this.analyzed) {
            analyze(clazz.getClassLoader());
        }

        // Get list of classes in the reverse order of the inheritance
        LinkedList<Class<?>> listClasses = new LinkedList<Class<?>>();
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            listClasses.addFirst(currentClass);
            currentClass = currentClass.getSuperclass();
        }

        // Analyze classes and perform initialization
        for (Class<?> classToAnalyze : listClasses) {
            // Get the metadata of this class
            ICommonClassMetadata<? extends ICommonClassMetadata<?, ?, ?>,
                                 ? extends ICommonMethodMetadata<?, ?, ?>,
                                 ? extends ICommonFieldMetadata<?, ?, ?>> classMetadata;

            classMetadata = getClassMetadata(classToAnalyze);

            // Found metadata ?
            if (classMetadata != null) {

                // Perform injection
                inject(classMetadata, classToAnalyze, null);
            }
        }
    }

    /**
     * Inject the value in the given field/method/...
     * @param sharedMetadata the shared metadata
     * @param clazz the class on which injection is performed
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if injection fails
     */
    protected void inject(final ISharedMetadata sharedMetadata, final Class<?> clazz, final Object instance)
            throws ArchiveInjectionException {



        // Inject EJB
        IJEjbEJB jejb = sharedMetadata.getJEjbEJB();
        if (jejb != null) {
            // Update interface name
            jejb.setBeanInterface(getInterfaceName(sharedMetadata));

            // inject field
            injectEJB(jejb, sharedMetadata, clazz, instance);
        }

        // Inject Resources
        IJAnnotationResource jAnnotationResource = sharedMetadata.getJAnnotationResource();
        if (jAnnotationResource != null) {
            // Update interface name
            jAnnotationResource.setType(getInterfaceName(sharedMetadata));

            // inject field
            injectResource(jAnnotationResource, sharedMetadata, clazz, instance);
        }

        // Inject PersistenceUnit
        IJavaxPersistenceUnit jJavaxPersistenceUnit = sharedMetadata.getJavaxPersistenceUnit();
        if (jJavaxPersistenceUnit != null) {
            // inject field
            injectPersistenceUnit(jJavaxPersistenceUnit, sharedMetadata, clazz, instance);
        }

        // Inject WebServiceRef
        IJaxwsWebServiceRef jaxwsWSR = sharedMetadata.getJaxwsWebServiceRef();
        if (jaxwsWSR != null) {

            // Update type name
            jaxwsWSR.setType(getInterfaceName(sharedMetadata));

            // Inject field
            injectWebServiceRef(jaxwsWSR, sharedMetadata, clazz, instance);
        }
    }

    /**
     * Perform injection in the given instance (or if null on a static way on
     * the class) with the provided metadata.
     * @param commonMetadata the metadata that will be used for injection
     * @param clazz the class used for injection
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if injection fails
     */
    @SuppressWarnings("unchecked")
    public void inject(final ICommonClassMetadata commonMetadata, final Class<?> clazz, final Object instance)
            throws ArchiveInjectionException {

        // PostConstruct methods
        List<ICommonMethodMetadata<?, ?, ?>> postConstructMethods = new ArrayList<ICommonMethodMetadata<?, ?, ?>>();

        // PreDestroy methods
        List<ICommonMethodMetadata<?, ?, ?>> preDestroyMethods = new ArrayList<ICommonMethodMetadata<?, ?, ?>>();

        // Loop on the fields
        Iterator<? extends ICommonFieldMetadata<?, ?, ?>> itField = commonMetadata.getStandardFieldMetadataCollection()
                .iterator();
        while (itField.hasNext()) {
            inject(itField.next(), clazz, instance);
        }

        // Loop on the methods
        Iterator<? extends ICommonMethodMetadata<?, ?, ?>> itMethod = commonMetadata.getStandardMethodMetadataCollection()
                .iterator();
        while (itMethod.hasNext()) {
            ICommonMethodMetadata<?, ?, ?> methodMetadata = itMethod.next();

            // Init
            inject(methodMetadata, clazz, instance);

            // PostConstruct Method ?
            if (methodMetadata.isPostConstruct()) {
                postConstructMethods.add(methodMetadata);
            }

            // PreDestroy Method ?
            if (methodMetadata.isPreDestroy()) {
                preDestroyMethods.add(methodMetadata);
            }

        }

        // Call Post Construct methods
        invokePostConstructMethods(postConstructMethods, clazz, instance);
    }

    /**
     * Perform injection in the given instance (or if null on a static way on
     * the class) with the provided metadata.
     * @param jaxwsWSR the service-ref that needs to be injected
     * @param sharedMetadata the metadata that will be used for injection
     * @param clazz the class used for injection
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if injection fails
     */
    protected void injectWebServiceRef(final IJaxwsWebServiceRef jaxwsWSR, final ISharedMetadata sharedMetadata,
            final Class<?> clazz, final Object instance) throws ArchiveInjectionException {


        // Check if there is a value bound in the ENC
        String encName = buildENCName(jaxwsWSR.getName(), sharedMetadata);
        Object value = null;
        try {
            value = new InitialContext().lookup("java:comp/env/" + encName);
        } catch (NamingException e) {
            logger.error("There was no Web Service Ref bound with ENC name ''{0}'' in the registry. Cannot inject it on metadata ''{1}''.", encName, sharedMetadata, e);
            return;
        }

        // Inject the value
        setValue(sharedMetadata, clazz, instance, value);

    }

    /**
     * Call postconstruct methods.
     * @param postConstructMethods the methods to call
     * @param clazz the class to use for getting the methods
     * @param instance the given instance to call
     * @throws ArchiveInjectionException if invocation on the methods is not
     *         done
     */
    protected void invokePostConstructMethods(final List<ICommonMethodMetadata<?, ?, ?>> postConstructMethods,
            final Class<?> clazz, final Object instance) throws ArchiveInjectionException {
        // null list, return
        if (postConstructMethods == null) {
            return;
        }

        // Call each method
        for (ICommonMethodMetadata<?, ?, ?> postConstructMethodMetadata : postConstructMethods) {
            Method postConstructMethod = null;
            try {
                postConstructMethod = clazz.getDeclaredMethod(postConstructMethodMetadata.getJMethod().getName());
            } catch (NoSuchMethodException e) {
                throw new ArchiveInjectionException("Cannot invoke postconstruct method", e);
            }

            boolean accessible = postConstructMethod.isAccessible();
            try {
                postConstructMethod.setAccessible(true);
                postConstructMethod.invoke(instance);
            } catch (IllegalAccessException e) {
                throw new ArchiveInjectionException("Cannot invoke postconstruct method", e);
            } catch (InvocationTargetException e) {
                throw new ArchiveInjectionException("Cannot invoke postconstruct method", e);
            } finally {
                postConstructMethod.setAccessible(accessible);
            }
        }

    }

    /**
     * Perform injection in the given instance (or if null on a static way on
     * the class) with the provided metadata.
     * @param ejb the EJB-REF that needs to be injected
     * @param sharedMetadata the metadata that will be used for injection
     * @param clazz the class used for injection
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if injection fails
     */
    public void injectEJB(final IJEjbEJB ejb, final ISharedMetadata sharedMetadata, final Class<?> clazz, final Object instance)
            throws ArchiveInjectionException {
        // get data for the EJB
        String interfaceName = ejb.getBeanInterface();
        String beanName = ejb.getBeanName();
        String mappedName = ejb.getMappedName();

        // Check if there is a value bound in the ENC
        String encName = buildENCName(ejb.getName(), sharedMetadata);
        boolean needToBind = true;
        Object value = null;
        try {
            value = new InitialContext().lookup("java:comp/env/" + encName);
            needToBind = false;
        } catch (NamingException e) {
            logger.debug("Value for ENC '{0}' not bound in the registry", encName);
        }

        // Inject this EJB reference

        // JNDI Name to use if no value yet
        if (value == null) {
            String jndiName = null;

            // Mapped name defined ? use it as JNDI name
            if (mappedName != null) {
                jndiName = mappedName;
            } else {
                // needs to ask the remote side
                jndiName = getRemoteEJBJNDIName(interfaceName, beanName);
            }

            // OK, now that JNDI name is here, get value
            try {
                value = new InitialContext().lookup(jndiName);
            } catch (NamingException e) {
                throw new ArchiveInjectionException("Cannot get object with JNDI Name '" + jndiName + "' for ejb '" + ejb
                        + "'.", e);
            }
        }

        // Set value
        setValue(sharedMetadata, clazz, instance, value);

        // bind in ENC
        if (needToBind) {
            bindValue(encName, value);
        }

    }

    /**
     * Perform injection in the given instance (or if null on a static way on
     * the class) with the provided metadata.
     * @param jJavaxPersistenceUnit the persistence-unit-ref that needs to be
     *        injecte
     * @param sharedMetadata the metadata that will be used for injection
     * @param clazz the class to use
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if it fails
     */
    public void injectPersistenceUnit(final IJavaxPersistenceUnit jJavaxPersistenceUnit, final ISharedMetadata sharedMetadata,
            final Class<?> clazz, final Object instance) throws ArchiveInjectionException {
        String name = jJavaxPersistenceUnit.getName();
        Object value = null;
        // Check if there is a value bound in the ENC
        String encName = buildENCName(jJavaxPersistenceUnit.getName(), sharedMetadata);
        boolean needToBind = true;
        try {
            value = new InitialContext().lookup("java:comp/env/" + encName);
            needToBind = false;
        } catch (NamingException e) {
            logger.debug("Value for ENC '{0}' not bound in the registry", encName);
        }

        // No value, needs to find it
        if (value == null) {
            try {
                value = new InitialContext().lookup(name);
            } catch (NamingException e) {
                throw new ArchiveInjectionException("Cannot get object with JNDI Name '" + name + "' for Resource '"
                        + jJavaxPersistenceUnit + "'.", e);
            }
        }

        // Set value
        setValue(sharedMetadata, clazz, instance, value);

        // bind in ENC
        if (needToBind) {
            bindValue(encName, value);
        }
    }

    /**
     * Perform injection in the given instance (or if null on a static way on
     * the class) with the provided metadata.
     * @param jAnnotationResource the resource-ref that needs to be injected
     * @param sharedMetadata the metadata that will be used for injection
     * @param clazz the class used
     * @param instance the instance to use for the injection. May be null for
     *        static case.
     * @throws ArchiveInjectionException if injection fails
     */
    public void injectResource(final IJAnnotationResource jAnnotationResource, final ISharedMetadata sharedMetadata,
            final Class<?> clazz, final Object instance) throws ArchiveInjectionException {

        // For some special case, fix the JNDI name to use
        if ("javax.transaction.UserTransaction".equals(jAnnotationResource.getType())) {
            jAnnotationResource.setMappedName("java:comp/UserTransaction");
        } else if ("org.omg.CORBA.ORB".equals(jAnnotationResource.getType())) {
            jAnnotationResource.setMappedName("java:comp/ORB");
        }

        // get data for the Resource
        String mappedName = jAnnotationResource.getMappedName();

        // Value that will be injected
        Object value = null;

        // Check if there is a value bound in the ENC
        String encName = buildENCName(jAnnotationResource.getName(), sharedMetadata);
        boolean needToBind = true;
        try {
            value = new InitialContext().lookup("java:comp/env/" + encName);
            needToBind = false;
        } catch (NamingException e) {
            logger.debug("Value for ENC '{0}' not bound in the registry", encName);
        }

        // No value, needs to find it
        if (value == null) {
            if (mappedName == null) {
                String messageDestinationLink = jAnnotationResource.getMessageDestinationLink();
                if (messageDestinationLink == null) {
                    logger.warn("Injection of @Resource for {0} is not supported", jAnnotationResource);
                    return;
                }
                // Use MessageDestinationLink if presetn !
                mappedName = getRemoteMessageDestinationJNDIName(messageDestinationLink);
            }

            // OK, now that JNDI name is here, get value
            try {
                value = new InitialContext().lookup(mappedName);
            } catch (NamingException e) {
                throw new ArchiveInjectionException("Cannot get object with JNDI Name '" + mappedName + "' for Resource '"
                        + jAnnotationResource + "'.", e);
            }
        }

        // Set value
        setValue(sharedMetadata, clazz, instance, value);

        // bind in ENC
        if (needToBind) {
            bindValue(encName, value);
        }

    }

    /**
     * Bind the given value in the ENC.
     * @param encName the ENC name
     * @param value the ENC value
     */
    protected void bindValue(final String encName, final Object value) {
        // bind in ENC if not already done
        try {
            new InitialContext().bind("java:comp/env/" + encName, value);
        } catch (NamingException e) {
            logger.warn("Unable to bind the value for ENC '{0}'.", encName);
        }
    }

    /**
     * Sets the value on a given field/method by using given arguments.
     * @param sharedMetadata the metadata for the name of the field/method
     * @param clazz the class of the field
     * @param instance the instance of the class
     * @param value the value to set
     * @throws ArchiveInjectionException if the value cannot be set
     */
    protected void setValue(final ISharedMetadata sharedMetadata, final Class<?> clazz, final Object instance,
            final Object value) throws ArchiveInjectionException {

        if (sharedMetadata instanceof ICommonFieldMetadata) {
            setFieldValue((ICommonFieldMetadata<?, ?, ?>) sharedMetadata, clazz, instance, value);
        } else if (sharedMetadata instanceof ICommonMethodMetadata) {
            setMethodValue((ICommonMethodMetadata<?, ?, ?>) sharedMetadata, clazz, instance, value);
        }
    }

    /**
     * Sets the value on a given method by using given arguments.
     * @param methodMetadata the metadata for the name of the method
     * @param clazz the class of the method
     * @param instance the instance of the class
     * @param value the value to set
     * @throws ArchiveInjectionException if the value cannot be set
     */
    protected void setMethodValue(final ICommonMethodMetadata<?, ?, ?> methodMetadata, final Class<?> clazz,
            final Object instance, final Object value) throws ArchiveInjectionException {

        // Value is obtained, set it
        // Get Method from the class
        JMethod jMethod = methodMetadata.getJMethod();
        Method method = MethodHelper.getMethod(jMethod, clazz);

        boolean accessible = method.isAccessible();
        try {
            // needs to be allowed to change value of the method
            method.setAccessible(true);

            // Set value
            try {
                method.invoke(instance, value);
            } catch (IllegalAccessException e) {
                throw new ArchiveInjectionException("Cannot set given value on the method '" + jMethod + "'.", e);
            } catch (IllegalArgumentException e) {
                throw new ArchiveInjectionException("Cannot set given value on the method '" + jMethod + "'.", e);
            } catch (InvocationTargetException e) {
                throw new ArchiveInjectionException("Cannot set given value on the method '" + jMethod + "'.", e);
            }
        } finally {
            // set back accessible attribute
            method.setAccessible(accessible);
        }

    }

    /**
     * Sets the value on a given field by using given arguments.
     * @param fieldMetadata the metadata for the name of the field
     * @param clazz the class of the field
     * @param instance the instance of the class
     * @param value the value to set
     * @throws ArchiveInjectionException if the value cannot be set
     */
    protected void setFieldValue(final ICommonFieldMetadata<?, ?, ?> fieldMetadata, final Class<?> clazz,
            final Object instance, final Object value) throws ArchiveInjectionException {

        // Value is obtained, set it
        // Get Field from the class
        JField jField = fieldMetadata.getJField();
        Field field = null;
        try {
            field = clazz.getDeclaredField(jField.getName());
        } catch (NoSuchFieldException e) {
            throw new ArchiveInjectionException("Cannot get field '" + jField + "' on the class '" + clazz.getName() + "'", e);
        }

        // if instance is null, we're using static injection.
        // But check that in this case, the field is a static field !
        int modifier = field.getModifiers();
        if (!Modifier.isStatic(modifier)) {
            logger.error("The field ''{0}'' of the class ''{1}'' is not a static field and as the injection is being done in a static way, this field won't be defined !",
                            field, clazz);
            return;
        }


        boolean accessible = field.isAccessible();
        try {
            // needs to be allowed to change value of the field
            field.setAccessible(true);

            // Set value
            try {
                field.set(instance, value);
            } catch (IllegalAccessException e) {
                throw new ArchiveInjectionException("Cannot set given value on the field '" + jField + "'.", e);
            }
        } finally {
            // set back accessible attribute
            field.setAccessible(accessible);
        }

    }

    /**
     * Build an ENC name for a given field.
     * @param name the existing name of the ENC specified through annotation/xml
     * @param sharedMetadata the metadata of the field
     * @return the updated enc name if there was none
     */
    protected String buildENCName(final String name, final ISharedMetadata sharedMetadata) {
        String encName = name;
        if (encName == null || "".equals(encName)) {
            if (sharedMetadata instanceof ICommonFieldMetadata) {
                ICommonFieldMetadata<?, ?, ?> fieldMetadata = (ICommonFieldMetadata<?, ?, ?>) sharedMetadata;
                encName = fieldMetadata.getClassMetadata().getJClass().getName().replace("/", ".") + "/"
                        + fieldMetadata.getJField().getName();
            } else if (sharedMetadata instanceof ICommonMethodMetadata) {
                ICommonMethodMetadata<?, ?, ?> methodMetaData = (ICommonMethodMetadata<?, ?, ?>) sharedMetadata;
                StringBuilder propertyBuilder = new StringBuilder(methodMetaData.getJMethod().getName());
                propertyBuilder.delete(0, GETTER_LENGTH);
                propertyBuilder.setCharAt(0, Character.toLowerCase(propertyBuilder.charAt(0)));
                String descriptor = methodMetaData.getClassMetadata().getJClass().getName();
                propertyBuilder.insert(0, descriptor.replace("/", ".") + "/");
                encName = propertyBuilder.toString();
            }
        }
        return encName;
    }

    /**
     * Gets the JNDI resolver and initialize it if not already initialized.
     * @return JNDI Resolver
     * @throws ArchiveInjectionException if resolver is not available
     */
    protected EZBRemoteJNDIResolver getJNDIResolver() throws ArchiveInjectionException {
        if (this.jndiResolver == null) {
            // Get object
            Object o = null;
            try {
                o = new InitialContext().lookup("EZB_Remote_JNDIResolver");
            } catch (NamingException e) {
                // No Remote JNDI resolver, so throws exception
                throw new ArchiveInjectionException("No Remote EJB3 JNDI Resolver found", e);
            }

            // Object is here, so we can cast it
            this.jndiResolver = (EZBRemoteJNDIResolver) PortableRemoteObject.narrow(o, EZBRemoteJNDIResolver.class);
        }
        return this.jndiResolver;
    }

    /**
     * Gets a JNDI name from the server for a given message destination name.
     * @param messageDestinationName the name of the message destination
     * @return a JNDI name if value has been found, else an exception
     * @throws ArchiveInjectionException if JNDI Name is not found
     */
    protected String getRemoteMessageDestinationJNDIName(final String messageDestinationName) throws ArchiveInjectionException {

        String jndiName = null;

        // Ask the Resolver with interface name and bean name
        List<EZBJNDIData> jndiDataList = null;
        try {
            jndiDataList = getJNDIResolver().getMessageDestinationJNDINames(messageDestinationName);
        } catch (RemoteException re) {
            // No Remote JNDI resolver, so throw first exception
            throw new ArchiveInjectionException("Unable to get JNDI Name for '" + messageDestinationName + "'", re);
        }

        // Data is here, check if it is empty or not
        if (jndiDataList.size() == 0) {
            throw new ArchiveInjectionException("Unable to get JNDI Name for message destination '" + messageDestinationName
                    + "', no data was found on the remote side.");
        } else if (jndiDataList.size() > 1) {
            // too many entries
            logger.warn("There may be a problem for message destination '" + messageDestinationName + "', too many answers : '"
                    + jndiDataList + "'. Using the first entry");
        }

        // Only one item found, so get JNDI Name from this object
        EZBJNDIData jndiData = jndiDataList.get(0);
        // Update JNDI name
        jndiName = jndiData.getName();
        if (logger.isDebugEnabled()) {
            logger.debug("Found JNDI Name '" + jndiName + "' for message destination '" + messageDestinationName
                    + "', answers : '" + jndiDataList + "'.");
        }

        return jndiName;
    }

    /**
     * Gets a JNDI name from the server for a given bean/interface.
     * @param interfaceName the name of the interface
     * @param beanName the name of the bean
     * @return a JNDI name if value has been found, else an exception
     * @throws ArchiveInjectionException if JNDI Name is not found
     */
    protected String getRemoteEJBJNDIName(final String interfaceName, final String beanName) throws ArchiveInjectionException {

        String jndiName = null;

        // Ask the Resolver with interface name and bean name
        List<EZBJNDIBeanData> jndiDataList = null;
        try {
            jndiDataList = getJNDIResolver().getEJBJNDINames(interfaceName, beanName);
        } catch (RemoteException re) {
            // No Remote JNDI resolver, so throw first exception
            throw new ArchiveInjectionException("Unable to get JNDI Name for '" + interfaceName + "'/'" + beanName + "'", re);
        }

        // Data is here, check if it is empty or not
        if (jndiDataList.size() == 0) {
            throw new ArchiveInjectionException("Unable to get JNDI Name for '" + interfaceName + "'/'" + beanName
                    + "', no data was found on the remote side.");
        } else if (jndiDataList.size() > 1) {
            // too many entries
            logger.warn("There may be a problem for bean '" + interfaceName + "'/'" + beanName + "', too many answers : '"
                    + jndiDataList + "'. Using the first entry");
        }

        // Only one item found, so get JNDI Name from this object
        EZBJNDIBeanData jndiData = jndiDataList.get(0);
        // Update JNDI name
        jndiName = jndiData.getName();
        if (logger.isDebugEnabled()) {
            logger.debug("Found JNDI Name '" + jndiName + "' for '" + interfaceName + "'/'" + beanName + "', answers : '"
                    + jndiDataList + "'.");
        }

        return jndiName;
    }

    /**
     * Gets the metadata for a given class.
     * @param clazz the class on which we want to get metadata
     * @return the metadata if found else null
     */
    protected ICommonClassMetadata<
                ? extends ICommonClassMetadata<?, ?, ?>,
                ? extends ICommonMethodMetadata<?, ?, ?>,
                ? extends ICommonFieldMetadata<?, ?, ?>> getClassMetadata(
            final Class<?> clazz) {
        return getClassMetadata(clazz.getName());
    }

    /**
     * Gets the metadata for a given class.
     * @param requestedClassName the name of the class on which we want to get
     *        metadata
     * @return the metadata if found else null
     */
    protected ICommonClassMetadata<
            ? extends ICommonClassMetadata<?, ?, ?>,
            ? extends ICommonMethodMetadata<?, ?, ?>,
            ? extends ICommonFieldMetadata<?, ?, ?>> getClassMetadata(
            final String requestedClassName) {

        // Get an iterator
        Iterator<? extends ICommonClassMetadata<? extends ICommonClassMetadata<?, ?, ?>,
                                                ? extends ICommonMethodMetadata<?, ?, ?>,
                                                ? extends ICommonFieldMetadata<?, ?, ?>>> it = this.metadataCollection
                .iterator();

        // Search the class by iterating on the metadata collection
        while (it.hasNext()) {
            // Declare temporary var
            ICommonClassMetadata<? extends ICommonClassMetadata<?, ?, ?>,
                                 ? extends ICommonMethodMetadata<?, ?, ?>,
                                 ? extends ICommonFieldMetadata<?, ?, ?>> tmpMetadata;

            // Get current metadata
            tmpMetadata = it.next();

            // Metadata for the expected class ?
            JClass jClass = tmpMetadata.getJClass();
            if (jClass != null) {
                String className = jClass.getName();
                // compare name
                if (requestedClassName.equals(className.replace('/', '.'))) {
                    return tmpMetadata;
                }

            }
        }

        // Not found
        return null;
    }

    /**
     * Get method from the metadata.
     * @param methodMetaData the metadata to check
     * @return ASM type of the first arg of the method.
     */
    protected Type validateSetterMethod(final ICommonMethodMetadata<?, ?, ?> methodMetaData) {

            JMethod jMethod = methodMetaData.getJMethod();
            // Should be a setter
            if (!jMethod.getName().startsWith("set") || jMethod.getName().equalsIgnoreCase("set")) {
                throw new IllegalStateException("Method '" + jMethod
                        + "' is invalid. Should be in the setter form setXXX().");
            }

            // Get type of interface
            // Get arguments of the method.
            Type[] args = Type.getArgumentTypes(jMethod.getDescriptor());
            if (args.length != 1) {
                throw new IllegalStateException("Method args '" + Arrays.asList(args) + "' for method '" + jMethod
                        + "' are invalid. Length should be of 1.");
            }
            return args[0];
        }

    /**
     * @param sharedMetadata the given metadata
     * @return the interface name based on the given metadata
     */
    protected String getInterfaceName(final ISharedMetadata sharedMetadata) {
        // Set the interface name by using the descriptor of the field or method
        Type typeInterface = null;
        if (sharedMetadata instanceof ICommonFieldMetadata) {
            typeInterface = Type.getType(((ICommonFieldMetadata<?, ?, ?>) sharedMetadata).getJField().getDescriptor());
        } else if (sharedMetadata instanceof ICommonMethodMetadata<?, ?, ?>) {
            typeInterface = validateSetterMethod((ICommonMethodMetadata<?, ?, ?>) sharedMetadata);
        }

        if (typeInterface == null) {
            logger.warn("Unable to proceed to injection of metadata ''{0}'' as no interface has been found", sharedMetadata);
            throw new IllegalArgumentException("Unable to proceed to injection of metadata '" + sharedMetadata
                    + "' as no interface has been found");
        }

        // Get interface name
        return typeInterface.getClassName();
    }

}
