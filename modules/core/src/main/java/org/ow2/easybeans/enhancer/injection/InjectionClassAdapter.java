/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: InjectionClassAdapter.java 5854 2011-04-06 09:29:15Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.injection;

import static org.ow2.easybeans.deployment.helper.JavaContextHelper.getJndiName;
import static org.ow2.easybeans.injection.JNDILookupHelper.JndiType.JAVA;
import static org.ow2.easybeans.injection.JNDILookupHelper.JndiType.JAVA_COMP;
import static org.ow2.easybeans.injection.JNDILookupHelper.JndiType.JAVA_COMP_ENV;
import static org.ow2.easybeans.injection.JNDILookupHelper.JndiType.REGISTRY;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ejb.EJBContext;
import javax.ejb.MessageDrivenContext;
import javax.ejb.SessionContext;
import javax.ejb.TimerService;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.transaction.UserTransaction;

import org.omg.CORBA.ORB;
import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.api.container.EZBMDBContext;
import org.ow2.easybeans.api.container.EZBSessionContext;
import org.ow2.easybeans.asm.ClassAdapter;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.Label;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.asm.commons.JSRInlinerAdapter;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.interceptors.EasyBeansInvocationContextGenerator;
import org.ow2.easybeans.enhancer.lib.MethodRenamer;
import org.ow2.easybeans.injection.JNDILookupHelper.JndiType;
import org.ow2.easybeans.resolver.api.EZBContainerJNDIResolver;
import org.ow2.easybeans.resolver.api.EZBJNDIResolverException;
import org.ow2.util.ee.metadata.common.api.struct.IEnvEntry;
import org.ow2.util.ee.metadata.common.api.struct.IJAnnotationResource;
import org.ow2.util.ee.metadata.common.api.struct.IJEjbEJB;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxPersistenceContext;
import org.ow2.util.ee.metadata.common.api.struct.IJavaxPersistenceUnit;
import org.ow2.util.ee.metadata.common.api.struct.IJaxwsWebServiceRef;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class adds methods which will inject resources in the bean class.
 * @author Florent Benoit
 */
public class InjectionClassAdapter extends ClassAdapter implements Opcodes {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(InjectionClassAdapter.class);

    /**
     * Metadata available by this adapter for a class.
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata;

    /**
     * Metadata available by this adapter for a child class (the bean).
     */
    private EasyBeansEjbJarClassMetadata beanChildClassAnnotationMetadata;

    /**
     * Map containing informations for enhancers.
     */
    private Map<String, Object> map = null;

    /**
     * Is that generated method is static (client case).
     */
    private boolean staticMode = false;

    /**
     * javax.ejb.EJBContext interface.
     */
    private static final String EJBCONTEXT = EJBContext.class.getName();

    /**
     * javax.ejb.SessionContext interface.
     */
    private static final String SESSION_CONTEXT = SessionContext.class.getName();

    /**
     * javax.ejb.MessageDrivenContext interface.
     */
    private static final String MESSAGEDRIVEN_CONTEXT = MessageDrivenContext.class.getName();

    /**
     * org.omg.CORBA.ORB interface.
     */
    private static final String ORB_ITF = ORB.class.getName();

    /**
     * javax.transaction.UserTransaction interface.
     */
    private static final String USERTRANSACTION_ITF = UserTransaction.class.getName();

    /**
     * java.net.URL interface.
     */
    private static final String URL_ITF = URL.class.getName();

    /**
     * Entity Manager interface.
     */
    private static final String ENTITYMANAGER_ITF = EntityManager.class.getName();

    /**
     * Entity Manager Factory interface.
     */
    private static final String ENTITYMANAGERFACTORY_ITF = EntityManagerFactory.class.getName();

    /**
     * javax.ejb.TimerService interface.
     */
    private static final String TIMERSERVICE_ITF = TimerService.class.getName();

    /**
     * EZBEJBContext type descriptor.
     */
    private static final String EZB_EJBCONTEXT_DESC  = Type.getDescriptor(EZBEJBContext.class);

    /**
     * Defines java.lang.Object class.
     */
    public static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Injected method name (used to be called by the Bean class).
     */
    public static final String INJECTED_METHOD = "injectedByEasyBeans";

    /**
     * Internal injected method name (used to  be called between classes/super classes).
     */
    public static final String INTERNAL_INJECTED_METHOD = "internalInjectedByEasyBeans";

    /**
     * JMethod object for injectedByEasyBeans.
     */
    public static final JMethod INJECTED_JMETHOD = new JMethod(ACC_PUBLIC, MethodRenamer.encode(INJECTED_METHOD), "()V", null,
            new String[] {"org/ow2/easybeans/api/injection/EasyBeansInjectionException"});

    /**
     * JMethod object for internalInjectedByEasyBeans.
     */
    public static final JMethod INTERNAL_INJECTED_JMETHOD = new JMethod(ACC_PUBLIC, MethodRenamer.encode(INTERNAL_INJECTED_METHOD), "()V", null,
            new String[] {"org/ow2/easybeans/api/injection/EasyBeansInjectionException"});

    /**
     * List of injected methods.
     */
    public static final String[] INJECTED_METHODS = new String[] {"getEasyBeansContext", "setEasyBeansContext",
            "getEasyBeansFactory", "setEasyBeansFactory", INTERNAL_INJECTED_METHOD};

    /**
     * Replace length to create default JNDI names.
     */
    private static final int LENGTH = 3;

    /**
     * JNDI Resolver.
     */
    private EZBContainerJNDIResolver containerJNDIResolver = null;

    /**
     * Defered methods lookup.
     */
    private List<EasyBeansEjbJarMethodMetadata> deferedMethods = null;

    /**
     * Defered fields lookup.
     */
    private List<EasyBeansEjbJarFieldMetadata> deferedFields = null;

    /**
     * Defered Class lookup.
     */
    private List<LookupEncEntry> deferedEntries = null;

    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param map a map allowing to give some objects to the adapter.
     * @param beanChildClassAnnotationMetadata the classmetadata of the bean if we're enhancing a super class of a bean
     * @param staticMode - Is that generated method is static (client case).
     */
    public InjectionClassAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final ClassVisitor cv,
            final Map<String, Object> map, final EasyBeansEjbJarClassMetadata beanChildClassAnnotationMetadata, final boolean staticMode) {
        super(cv);
        this.classAnnotationMetadata = classAnnotationMetadata;
        this.map = map;
        this.beanChildClassAnnotationMetadata = beanChildClassAnnotationMetadata;
        this.staticMode = staticMode;
        this.containerJNDIResolver = (EZBContainerJNDIResolver) this.map.get(EZBContainerJNDIResolver.class.getName());
        this.deferedMethods = new ArrayList<EasyBeansEjbJarMethodMetadata>();
        this.deferedFields = new ArrayList<EasyBeansEjbJarFieldMetadata>();
        this.deferedEntries = new ArrayList<LookupEncEntry>();

        // Ensure it's there
        if (this.containerJNDIResolver == null) {
            throw new IllegalStateException("No JNDI Resolver found under the key '" + EZBContainerJNDIResolver.class.getName()
                    + "'.");
        }
    }

    /**
     * We need to remove JSR/RET instructions if they're present.
     * @param access access mode
     * @param name the name of the method
     * @param desc the desc of the method
     * @param signature the signature of the method
     * @param exceptions the given exceptions of the method
     * @return a method visitor
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc,
            final String signature, final String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);
    }


    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        super.visitEnd();

        // now, adds the injected method for beans (that will be intercepted)
        if (this.classAnnotationMetadata.isBean()) {
            addInjectedMethod();
        }

        // Adds the internal injected method
        addInternalInjectedMethod();

        // Adds methods if it's not a bean (as it should have been already added by the bean class adapter).
        // If it's a super class of a bean, as the class that will be instantiated
        // is the bean's class, the methods won't be used.
        // It it's an interceptor class, the interceptor manager will call the setters methods
        if (!this.classAnnotationMetadata.isBean()) {
            addDefaultMethods();
        }
    }

    /**
     * Generated methods allowing to set a context and a factory.
     * This allows to set on injectors the bean's session context and its factory.
     */
    private void addDefaultMethods() {
        // Adds the factory attribute and its getter/setter.
        CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(), "easyBeansFactory",
                Factory.class);

        // Adds the easyBeansContext attribute and its getter/setter.
        Class<?> contextClass = null;
        if (this.classAnnotationMetadata.isSession()) {
            contextClass = EZBSessionContext.class;
        } else if (this.classAnnotationMetadata.isMdb()) {
            contextClass = EZBMDBContext.class;
        } else {
            contextClass = EZBEJBContext.class;
        }
        CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                "easyBeansContext", contextClass);
    }


    /**
     * Generates the injectedByEasyBeans() method on the current class.
     */
    private void addInjectedMethod() {

        int access = ACC_PUBLIC;
        if (this.staticMode) {
            access = access + ACC_STATIC;
        }

        MethodVisitor mv = this.cv.visitMethod(access, INJECTED_METHOD, "()V", null,
                new String[] {"org/ow2/easybeans/api/injection/EasyBeansInjectionException"});
        // Add some flags on the generated method
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);

        mv.visitCode();


        // Init the dynamic interceptor manager if there is an invocation
        // context factory
        //        if (getEasyBeansInvocationContextFactory() != null) {
        //            this.easyBeansDynamicInterceptorManager = getEasyBeansInvocationContextFactory().getInterceptorManagerFactory().getInterceptorManager();
        //            this.easyBeansDynamicInterceptorManager.setEasyBeansContext(getEasyBeansContext());
        //            this.easyBeansDynamicInterceptorManager.injectedByEasyBeans();
        //        }
        if (this.classAnnotationMetadata.isBean()) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansInvocationContextFactory",
            "()Lorg/ow2/easybeans/api/interceptor/EZBInvocationContextFactory;");
            Label l1 = new Label();
            mv.visitJumpInsn(IFNULL, l1);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansInvocationContextFactory", "()Lorg/ow2/easybeans/api/interceptor/EZBInvocationContextFactory;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/interceptor/EZBInvocationContextFactory", "getInterceptorManagerFactory", "()Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManagerFactory;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/interceptor/EZBInterceptorManagerFactory", "getInterceptorManager", "()Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;");
            mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansDynamicInterceptorManager", "Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;");

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansDynamicInterceptorManager", "Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansContext", "()Lorg/ow2/easybeans/api/container/EZBEJBContext;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/interceptor/EZBInterceptorManager", "setEasyBeansContext", "(Lorg/ow2/easybeans/api/container/EZBEJBContext;)V");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansDynamicInterceptorManager", "Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;");
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/interceptor/EZBInterceptorManager", "injectedByEasyBeans", "()V");
            mv.visitLabel(l1);
        }


        // Now, calls our internal injected method
        if (!this.staticMode) {
            // generate call to INTERNAL_INJECTED_METHOD();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKESPECIAL, this.classAnnotationMetadata.getClassName(), INTERNAL_INJECTED_METHOD, "()V");
        } else {
            mv.visitMethodInsn(INVOKESTATIC, this.classAnnotationMetadata.getClassName(), INTERNAL_INJECTED_METHOD, "()V");
        }


        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }




    /**
     * Generates the internalInjectedByEasyBeans() method on the current class.
     */
    private void addInternalInjectedMethod() {
        int access = ACC_PUBLIC;
        if (this.staticMode) {
            access = access + ACC_STATIC;
        }

        MethodVisitor mv = this.cv.visitMethod(access, INTERNAL_INJECTED_METHOD, "()V", null,
                new String[] {"org/ow2/easybeans/api/injection/EasyBeansInjectionException"});
        // Add some flags on the generated method
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);

        mv.visitCode();


        // First, call the super class method (if the super class has been
        // analyzed) and if there is one
        String superNameClass = this.classAnnotationMetadata.getSuperName();
        if (superNameClass != null && !superNameClass.equals(JAVA_LANG_OBJECT)) {
            EasyBeansEjbJarClassMetadata superMetadata = this.classAnnotationMetadata.getLinkedClassMetadata(superNameClass);
            if (superMetadata != null) {
                if (!this.staticMode) {
                    // generate call to super method : super.INTERNAL_INJECTED_METHOD();
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitMethodInsn(INVOKESPECIAL, superMetadata.getClassName(), INTERNAL_INJECTED_METHOD, "()V");
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, superMetadata.getClassName(), INTERNAL_INJECTED_METHOD, "()V");
                }
            }
        }



        // If it is a bean, call the interceptorManager and the attributes (like context and factory)
        if (this.classAnnotationMetadata.isBean()) {
            String clNameManager = this.classAnnotationMetadata.getClassName()
            + EasyBeansInvocationContextGenerator.SUFFIX_INTERCEPTOR_MANAGER;

            // this.interceptorManager.setEasyBeansContext(easyBeansContext);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansInterceptorManager", "L"
                    + clNameManager + ";");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansContext", EZB_EJBCONTEXT_DESC);
            mv.visitMethodInsn(INVOKEVIRTUAL, clNameManager, "setEasyBeansContext", "(" + EZB_EJBCONTEXT_DESC + ")V");


            // this.interceptorManager.injectedByEasyBeans();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.classAnnotationMetadata.getClassName(), "easyBeansInterceptorManager", "L"
                    + clNameManager + ";");
            mv.visitMethodInsn(INVOKEVIRTUAL, clNameManager, "injectedByEasyBeans", "()V");

        }

        generateBodyInjectedMethod(mv);

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Generates the body of the injectedByEasyBeans() method if any.<br> Else,
     * do nothing.
     * @param mv the method visitor object used to add some code.
     */
    private void generateBodyInjectedMethod(final MethodVisitor mv) {

        // generates injection for annotations on the class itself
        generateClassInjection(mv);

        // Generates injection for setters methods
        generateSettersInjection(mv);

        // Generates injection for attributes
        generateAttributesInjection(mv);

        // Generate all lookup stuff at the end as it may reference previous entries
        generateDeferedInjection(mv);

    }

    /**
     * Generates the calls to populate ENC environment by using annotations on the class itself.
     * @param mv the method visitor used to inject bytecode.
     */
    private void generateClassInjection(final MethodVisitor mv) {
        // Get annotations on the class

        // &#64;PersistenceContexts annotation
        List<IJavaxPersistenceContext> javaxPersistencePersistenceContexts = this.classAnnotationMetadata
                .getJavaxPersistencePersistenceContexts();
        if (javaxPersistencePersistenceContexts != null && javaxPersistencePersistenceContexts.size() > 0) {
            // For each javaxPersistenceContext
            for (IJavaxPersistenceContext javaxPersistenceContext : javaxPersistencePersistenceContexts) {
                bindClassPersistenceContext(javaxPersistenceContext, mv);
            }
        }
        // &#64;PersistenceContext annotation
        if (this.classAnnotationMetadata.isPersistenceContext()) {
            bindClassPersistenceContext(this.classAnnotationMetadata.getJavaxPersistenceContext(), mv);
        }

        // &#64;PersistenceUnits annotation
        List<IJavaxPersistenceUnit> javaxPersistencePersistenceUnits = this.classAnnotationMetadata
                .getJavaxPersistencePersistenceUnits();
        if (javaxPersistencePersistenceUnits != null && javaxPersistencePersistenceUnits.size() > 0) {
            // For each javaxPersistenceUnit
            for (IJavaxPersistenceUnit javaxPersistenceUnit : javaxPersistencePersistenceUnits) {
                bindClassPersistenceUnit(javaxPersistenceUnit, mv);
            }
        }
        // &#64;PersistenceUnit annotation
        if (this.classAnnotationMetadata.isPersistenceUnit()) {
            bindClassPersistenceUnit(this.classAnnotationMetadata.getJavaxPersistenceUnit(), mv);
        }

        // &#64;EJBs annotation
        List<IJEjbEJB> jEjbs = this.classAnnotationMetadata.getJEjbEJBs();
        if (jEjbs != null && jEjbs.size() > 0) {
            // For each jEJB
            for (IJEjbEJB jEJB : jEjbs) {
                bindClassEJB(jEJB, mv);
            }
        }
        // &#64;EJB annotation
        IJEjbEJB jEJB = this.classAnnotationMetadata.getJEjbEJB();
        if (jEJB != null) {
            // For each ejb, do :
            bindClassEJB(jEJB, mv);
        }


        // &#64;Resources annotation
        List<IJAnnotationResource> jAnnotationResources = this.classAnnotationMetadata.getJAnnotationResources();
        if (jAnnotationResources != null && jAnnotationResources.size() > 0) {
            // For each jAnnotationResource
            for (IJAnnotationResource jAnnotationResource : jAnnotationResources) {
                bindResource(jAnnotationResource, mv);
            }
        }
        // &#64;Resource annotation
        IJAnnotationResource jAnnotationResource = this.classAnnotationMetadata.getJAnnotationResource();
        if (jAnnotationResource != null) {
            bindResource(jAnnotationResource, mv);
        }

    }


    /**
     * Generates the calls to methods that will set the attributes value.
     * @param mv the method visitor used to inject bytecode.
     */
    private void generateAttributesInjection(final MethodVisitor mv) {

        for (EasyBeansEjbJarFieldMetadata fieldMetaData : this.classAnnotationMetadata.getStandardFieldMetadataCollection()) {

            // Get type of interface
            Type typeInterface = Type.getType(fieldMetaData.getJField().getDescriptor());
            String itfName = typeInterface.getClassName();

            // &#64;PersistenceContext annotation
            if (fieldMetaData.isPersistenceContext()) {
                // validate
                validateAccessFieldAnnotation(fieldMetaData);

                // Check that attribute is EntityManager
                if (!ENTITYMANAGER_ITF.equals(itfName)) {
                    throw new IllegalStateException(
                            "Trying to applied @PersistenceContext on an invalid field in the class '"
                                    + this.classAnnotationMetadata.getClassName() + "', field = " + fieldMetaData);
                }

                IJavaxPersistenceContext javaxPersistenceContext = fieldMetaData.getJavaxPersistenceContext();

                logger.debug("Add injection for PersistenceContext on attribute {0} of class {1}", fieldMetaData
                        .getFieldName(), this.classAnnotationMetadata.getClassName());
                // add this.em =
                // EntityManagerHelper.getEntityManager(getEasyBeansContext(),
                // "myUnitName", PersistenceContextType.EXTENDED);
                mv.visitVarInsn(ALOAD, 0);


                // call em helper
                addCallEntityManagerHelper(javaxPersistenceContext, mv);

                // Set result in the field
                mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                        "Ljavax/persistence/EntityManager;");

                // Bind value in JNDI
                javaxPersistenceContext.setName(getJndiName(javaxPersistenceContext.getName(), fieldMetaData));
                bindClassPersistenceContext(javaxPersistenceContext, mv);

            }

            // &#64;PersistenceUnit annotation
            if (fieldMetaData.isPersistenceUnit()) {
                // validate
                validateAccessFieldAnnotation(fieldMetaData);

                // Check that attribute is EntityManager
                if (!ENTITYMANAGERFACTORY_ITF.equals(itfName)) {
                    throw new IllegalStateException(
                            "Trying to applied @PersistenceUnit on an invalid field in the class '"
                                    + this.classAnnotationMetadata.getClassName() + "', field = " + fieldMetaData);
                }
                logger.debug("Add injection for PersistenceUnit on attribute {0} of class {1}", fieldMetaData
                        .getFieldName(), this.classAnnotationMetadata.getClassName());


                IJavaxPersistenceUnit javaxPersistenceUnit = fieldMetaData.getJavaxPersistenceUnit();
                // add this.emf = EntityManagerHelper.getEntityManagerFactory(getEasyBeansContext(), "myUnitName");

                mv.visitVarInsn(ALOAD, 0);
                // get EMF
                addCallEntityManagerFactoryHelper(javaxPersistenceUnit, mv);
                // set attribute
                mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                        "Ljavax/persistence/EntityManagerFactory;");

                // Bind value in JNDI
                javaxPersistenceUnit.setName(getJndiName(javaxPersistenceUnit.getName(), fieldMetaData));
                bindClassPersistenceUnit(javaxPersistenceUnit, mv);
            }

            // &#64;EJB annotation
            IJEjbEJB jEjb = fieldMetaData.getJEjbEJB();
            if (jEjb != null) {
                // validate
                validateAccessFieldAnnotation(fieldMetaData);

                String lookup = jEjb.getLookup();
                if (lookup != null && !lookup.equals("")) {
                    // Not done right now
                    this.deferedFields.add(fieldMetaData);
                    continue;
                }

                // Update interface name ?
                String beanInterface = jEjb.getBeanInterface();
                if (beanInterface != null && !"java/lang/Object".equals(beanInterface)) {
                    itfName = beanInterface;
                }

                logger.debug("Add injection for EJB on attribute {0} of class {1}", fieldMetaData.getFieldName(),
                        this.classAnnotationMetadata.getClassName());

                // Gets the JNDI Resolver
                EZBContainerJNDIResolver containerJNDIResolver = (EZBContainerJNDIResolver) this.map
                        .get(EZBContainerJNDIResolver.class.getName());

                // ejbName ?
                String beanName = jEjb.getBeanName();

                // JNDI name
                String jndiName = null;

                // Mapped Name ? if not null, use it as JNDI name
                String mappedName = jEjb.getMappedName();
                if (mappedName != null && !mappedName.equals("")) {
                    jndiName = mappedName;
                }



                // JNDI name still null, ask the JNDI resolver
                if (jndiName == null) {
                    try {
                        jndiName = containerJNDIResolver.getEJBJNDIUniqueName(itfName, beanName);
                    } catch (EZBJNDIResolverException e) {
                        logger.error("No jndi name found on class {0} for interface {1} and beanName {2}",
                                this.classAnnotationMetadata.getClassName(), itfName, beanName);
                    }
                }

                // JNDI name not null
                if (jndiName != null) {
                    logger.debug("Result of Asking jndi name on class {0} for interface {1} and beanName {2}. Result = {3}",
                            this.classAnnotationMetadata.getClassName(), itfName, beanName, jndiName);
                    callAttributeJndi(jndiName, typeInterface, mv, fieldMetaData, this.classAnnotationMetadata
                            .getClassName(), REGISTRY);
                    callBindAttributeJndi(jEjb.getName(), jndiName, mv, fieldMetaData);
                }


            }

            // &#64;Resource annotation
            IJAnnotationResource jAnnotationResource = fieldMetaData.getJAnnotationResource();
            if (jAnnotationResource != null) {

                // Set default name if not present.
                jAnnotationResource.setName(getJndiName(jAnnotationResource.getName(), fieldMetaData));


                // Update annotation value with data set on the class
                updateAnnotationResource(jAnnotationResource);

                // Get Mapped Name / lookup Name
                String mappedName = jAnnotationResource.getMappedName();
                String lookupName = jAnnotationResource.getLookup();

                // Use MessageDestinationLink if present !
                String messageDestinationLink = jAnnotationResource.getMessageDestinationLink();
                if (messageDestinationLink != null) {
                    try {
                        mappedName = this.containerJNDIResolver.getMessageDestinationJNDIUniqueName(messageDestinationLink);
                    } catch (EZBJNDIResolverException e) {
                        throw new IllegalStateException("No JNDI name found when analyzing @Resource annotation '"
                                + jAnnotationResource + "' for the class '" + this.classAnnotationMetadata.getClassName()
                                + "'.", e);
                    }
                }

                // validate
                validateAccessFieldAnnotation(fieldMetaData);

                if (SESSION_CONTEXT.equals(itfName)) {
                    logger.debug("Add injection for @Resource on attribute {0} of class {1} for the type {2}",
                            fieldMetaData.getFieldName(), this.classAnnotationMetadata.getClassName(), itfName);

                    // this.attribute = getEasyBeansContext();
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/SessionContext");
                    mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                            "Ljavax/ejb/SessionContext;");
                    // Define the type (if missing)
                    jAnnotationResource.setType(SESSION_CONTEXT);

                    bindResource(jAnnotationResource, mv);
                } else if (MESSAGEDRIVEN_CONTEXT.equals(itfName)) {
                    logger.debug("Add injection for @Resource on attribute {0} of class {1} for the type {2}",
                            fieldMetaData.getFieldName(), this.classAnnotationMetadata.getClassName(), itfName);

                    // this.attribute = getEasyBeansContext();
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/MessageDrivenContext");
                    mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                            "Ljavax/ejb/MessageDrivenContext;");
                    // Define the type (if missing)
                    jAnnotationResource.setType(MESSAGEDRIVEN_CONTEXT);

                    bindResource(jAnnotationResource, mv);

                } else if (EJBCONTEXT.equals(itfName)) {
                    logger.debug("Add injection for @Resource on attribute {0} of class {1} for the type {2}",
                            fieldMetaData.getFieldName(), this.classAnnotationMetadata.getClassName(), itfName);

                    // this.attribute = getEasyBeansContext();
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/EJBContext");
                    mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                            "Ljavax/ejb/EJBContext;");
                    // Define the type (if missing)
                    jAnnotationResource.setType(EJBCONTEXT);

                    bindResource(jAnnotationResource, mv);

                } else if (isEnvEntry(jAnnotationResource.getName(), typeInterface)) { // Env-Entry
                    JndiType type = JAVA_COMP_ENV;
                    // Lookup name exists ?
                    if (lookupName == null) {
                        lookupName = jAnnotationResource.getName();
                    }
                    if (lookupName.startsWith("java:")) {
                        type = JAVA;
                    }

                    if (!this.staticMode) {
                        callAttributeNotNullJndi(lookupName, typeInterface, mv, fieldMetaData,
                                this.classAnnotationMetadata.getClassName(), type);
                    } else {
                        callAttributeJndi(lookupName, typeInterface, mv, fieldMetaData,
                                this.classAnnotationMetadata.getClassName(), type);
                    }
                } else if (USERTRANSACTION_ITF.equals(itfName)) {
                    callAttributeJndi("UserTransaction", typeInterface, mv, fieldMetaData,
                            this.classAnnotationMetadata.getClassName(), JAVA_COMP);
                    callBindAttributeJndi(jAnnotationResource.getName(), "java:comp/UserTransaction", mv, fieldMetaData);
                } else if (URL_ITF.equals(itfName)) {
                    // Bind object in java:comp/env
                    callBindLookupURLRef(jAnnotationResource.getName(), mappedName, mv);

                    // Set attribute
                    callAttributeJndi(jAnnotationResource.getName(), typeInterface, mv, fieldMetaData,
                            this.classAnnotationMetadata.getClassName(), JAVA_COMP_ENV);
                } else if (TIMERSERVICE_ITF.equals(itfName)) {
                    // Needs to get timerservice with the bean's context.
                    //this.fieldtimerService = getEasyBeansContext().getInternalTimerService();
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, null);
                    mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(EZBEJBContext.class), "getInternalTimerService",
                    "()Ljavax/ejb/TimerService;");
                    mv.visitFieldInsn(PUTFIELD, this.classAnnotationMetadata.getClassName(), fieldMetaData.getFieldName(),
                            "Ljavax/ejb/TimerService;");
                    callBindAttributeJndi(jAnnotationResource.getName(), "java:comp/TimerService", mv, fieldMetaData);
                } else if (ORB_ITF.equals(itfName)) {
                    callAttributeJndi("ORB", typeInterface, mv, fieldMetaData,
                            this.classAnnotationMetadata.getClassName(), JAVA_COMP);
                    callBindAttributeJndi(jAnnotationResource.getName(), "java:comp/ORB", mv, fieldMetaData);
                } else if (lookupName != null && !lookupName.equals("")) {
                    JndiType type = REGISTRY;
                    // Lookup name ?
                    if (lookupName.startsWith("java:")) {
                        type = JAVA;
                    }
                    callAttributeJndi(lookupName, typeInterface, mv, fieldMetaData,
                            this.classAnnotationMetadata.getClassName(), type);
                } else if (mappedName != null && !mappedName.equals("")) {
                        callAttributeJndi(mappedName, typeInterface, mv, fieldMetaData,
                                this.classAnnotationMetadata.getClassName(), REGISTRY);
                        callBindAttributeJndi(jAnnotationResource.getName(), mappedName, mv, fieldMetaData);
                    }

            }

            // &#64;WebServiceRef annotation
            IJaxwsWebServiceRef jWebServiceRef = fieldMetaData.getJaxwsWebServiceRef();
            if (jWebServiceRef != null) {

                // Validate
                validateAccessFieldAnnotation(fieldMetaData);

                // Perform a java:comp/env lookup
                callAttributeJndi(jWebServiceRef.getName(), typeInterface, mv, fieldMetaData,
                        this.classAnnotationMetadata.getClassName(), JAVA_COMP_ENV);
            }
        }
    }


    /**
     * Generates the calls to methods that will lookup entries.
     * @param mv the method visitor used to inject bytecode.
     */
    protected void generateDeferedInjection(final MethodVisitor mv) {

        // Generate class calls.
        bindLookupClass(mv);

        // Generate setters calls.
        bindLookupMethods(mv);

        // Generate fields calls.
        bindLookupFields(mv);

    }


    /**
     * Update the given resource with given encname with data set on the class.
     * @param jAnnotationResource the resource to update
     */
    private void updateAnnotationResource(final IJAnnotationResource jAnnotationResource) {
        // Search if no resource was defined on the class.
        List<IJAnnotationResource> classResources = null;
        IJAnnotationResource resClass = this.classAnnotationMetadata.getJAnnotationResource();
        if (resClass != null) {
            classResources = new ArrayList<IJAnnotationResource>();
            classResources.add(resClass);
        } else {
            classResources = this.classAnnotationMetadata.getJAnnotationResources();
        }
        // if resources are existing on the class, search matching key.
        if (classResources != null) {
            for (IJAnnotationResource annotationResource : classResources) {
                // Matching value
                if (jAnnotationResource.getName().equals(annotationResource.getName())) {
                    // Update the value if not set
                    jAnnotationResource.setMappedName(annotationResource.getMappedName());
                    if (jAnnotationResource.getMessageDestinationLink() == null) {
                        jAnnotationResource.setMessageDestinationLink(annotationResource.getMessageDestinationLink());
                    }
                }
            }
        }

        // On the bean class (if not already done)
        if (this.beanChildClassAnnotationMetadata != null && !this.beanChildClassAnnotationMetadata.equals(this.classAnnotationMetadata)) {
            resClass = this.beanChildClassAnnotationMetadata.getJAnnotationResource();
            if (resClass != null) {
                classResources = new ArrayList<IJAnnotationResource>();
                classResources.add(resClass);
            } else {
                classResources = this.beanChildClassAnnotationMetadata.getJAnnotationResources();
            }
            // if resources are existing on the class, search matching key.
            if (classResources != null) {
                for (IJAnnotationResource annotationResource : classResources) {
                    // Matching value
                    if (jAnnotationResource.getName().equals(annotationResource.getName())) {
                        // Update the value if not set
                        jAnnotationResource.setMappedName(annotationResource.getMappedName());
                        if (jAnnotationResource.getMessageDestinationLink() == null) {
                            jAnnotationResource.setMessageDestinationLink(annotationResource.getMessageDestinationLink());
                        }
                    }
                }
            }
        }





    }

    /**
     * Generates the calls to methods that will call the setters methods.
     * @param mv the method visitor used to inject bytecode.
     */
    private void generateSettersInjection(final MethodVisitor mv) {

        for (EasyBeansEjbJarMethodMetadata methodMetaData : this.classAnnotationMetadata.getMethodMetadataCollection()) {
            // Ignore inherited methods (managed by super class)
            if (methodMetaData.isInherited()) {
                continue;
            }

            IJAnnotationResource jAnnotationResource = methodMetaData.getJAnnotationResource();
            // &#64;Resource annotation
            if (jAnnotationResource != null) {
                Type typeInterface = validateSetterMethod(methodMetaData);
                String itfName = typeInterface.getClassName();

                // Set default name if not present.
                jAnnotationResource.setName(getJndiName(jAnnotationResource.getName(), methodMetaData));

                // Update annotation value with data set on the class
                updateAnnotationResource(jAnnotationResource);

                // Get lookup / Mapped Name
                String lookupName = jAnnotationResource.getLookup();
                String mappedName = jAnnotationResource.getMappedName();

                // Use MessageDestinationLink if present !
                String messageDestinationLink = jAnnotationResource.getMessageDestinationLink();
                if (messageDestinationLink != null) {
                    try {
                        mappedName = this.containerJNDIResolver.getMessageDestinationJNDIUniqueName(messageDestinationLink);
                    } catch (EZBJNDIResolverException e) {
                        throw new IllegalStateException("No JNDI name found when analyzing @Resource annotation '"
                                + jAnnotationResource + "' for the class '" + this.classAnnotationMetadata.getClassName()
                                + "'.", e);
                    }
                }

                // Env-Entry
                if (isEnvEntry(jAnnotationResource.getName(), typeInterface)) {

                    JndiType type = JAVA_COMP_ENV;
                    // Lookup name exists ?
                    if (lookupName == null) {
                        lookupName = jAnnotationResource.getName();
                    }
                    if (lookupName.startsWith("java:")) {
                        type = JAVA;
                    }

                    if (!this.staticMode) {
                        callMethodJndiEnvNotNull(lookupName, typeInterface, mv, methodMetaData, this.classAnnotationMetadata
                                .getClassName(), type);
                    } else {
                        callMethodJndiEnv(lookupName, typeInterface, mv, methodMetaData, this.classAnnotationMetadata
                                .getClassName(), type);
                    }
                } else if (USERTRANSACTION_ITF.equals(itfName)) {
                    callMethodJndiEnv("UserTransaction", typeInterface, mv, methodMetaData,
                            this.classAnnotationMetadata.getClassName(), JAVA_COMP);
                    callBindLookupJndiRef(jAnnotationResource.getName(), "java:comp/UserTransaction", mv);
                } else if (TIMERSERVICE_ITF.equals(itfName)) {
                    // add call to : setterMethod(getEasyBeansContext().getInternalTimerService());
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, null);
                    mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/container/EZBEJBContext",
                            "getInternalTimerService", "()Ljavax/ejb/TimerService;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData.getMethodName(),
                            "(Ljavax/ejb/TimerService;)V");
                    callBindLookupJndiRef(jAnnotationResource.getName(), "java:comp/TimerService", mv);
                } else if (SESSION_CONTEXT.equals(itfName)) {
                    // add call to : setterMethod(getEasyBeansContext());
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/SessionContext");
                    mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData.getMethodName(),
                            "(Ljavax/ejb/SessionContext;)V");

                    // Define the type (if missing)
                    jAnnotationResource.setType(SESSION_CONTEXT);
                    bindResource(jAnnotationResource, mv);
                } else if (MESSAGEDRIVEN_CONTEXT.equals(itfName)) {
                    // add call to : setterMethod(getEasyBeansContext());
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/MessageDrivenContext");
                    mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData.getMethodName(),
                            "(Ljavax/ejb/MessageDrivenContext;)V");

                    // Define the type (if missing)
                    jAnnotationResource.setType(MESSAGEDRIVEN_CONTEXT);
                    bindResource(jAnnotationResource, mv);
                } else if (EJBCONTEXT.equals(itfName)) {
                    // add call to : setterMethod(getEasyBeansContext());
                    mv.visitVarInsn(ALOAD, 0);
                    addCallGetEasyBeansContext(mv, "javax/ejb/EJBContext");
                    mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData.getMethodName(),
                            "(Ljavax/ejb/EJBContext;)V");

                    // Define the type (if missing)
                    jAnnotationResource.setType(EJBCONTEXT);
                    bindResource(jAnnotationResource, mv);
                } else if (ORB_ITF.equals(itfName)) {
                    callMethodJndiEnv("ORB", typeInterface, mv, methodMetaData,
                            this.classAnnotationMetadata.getClassName(), JAVA_COMP);
                    callBindLookupJndiRef(jAnnotationResource.getName(), "java:comp/ORB", mv);
                } else if (URL_ITF.equals(itfName)) {
                    // Bind object in java:comp/env
                    callBindLookupURLRef(jAnnotationResource.getName(), mappedName, mv);

                    // Get JNDI value from registry and call setter method
                    callMethodJndiEnv(jAnnotationResource.getName(), typeInterface, mv, methodMetaData,
                        this.classAnnotationMetadata.getClassName(), JAVA_COMP_ENV);
                } else if (lookupName != null && !lookupName.equals("")) {
                    JndiType type = REGISTRY;
                    // Lookup name ?
                    if (lookupName.startsWith("java:")) {
                        type = JAVA;
                    }
                    callMethodJndiEnv(lookupName, typeInterface, mv, methodMetaData,
                            this.classAnnotationMetadata.getClassName(), type);
                } else if (mappedName != null && !mappedName.equals("")) {
                    // Get JNDI value from registry and call setter method
                    callMethodJndiEnv(mappedName, typeInterface, mv, methodMetaData,
                            this.classAnnotationMetadata.getClassName(), REGISTRY);
                    // Then bind attribute in ENC
                    callBindLookupJndiRef(jAnnotationResource.getName(), mappedName, mv);
                }
            }

            // &#64;EJB annotation
            IJEjbEJB jEjb = methodMetaData.getJEjbEJB();
            if (jEjb != null) {
                logger.debug("Add injection for EJB on method {0} of class {1}", methodMetaData.getMethodName(),
                        this.classAnnotationMetadata.getClassName());

                Type typeInterface = validateSetterMethod(methodMetaData);
                String itfName = typeInterface.getClassName();

                // Update interface name ?
                String beanInterface = jEjb.getBeanInterface();
                if (beanInterface != null && !"java/lang/Object".equals(beanInterface)) {
                    itfName = beanInterface;
                }


                String lookup = jEjb.getLookup();
                if (lookup != null && !lookup.equals("")) {
                    // Not done right now
                    this.deferedMethods.add(methodMetaData);
                    continue;
                }

                // ejbName ?
                String beanName = jEjb.getBeanName();

                // JNDI name
                String jndiName = null;

                // Mapped Name ? if not null, use it as JNDI name
                String mappedName = jEjb.getMappedName();
                if (mappedName != null && !mappedName.equals("")) {
                    jndiName = mappedName;
                }


                // JNDI name still null, ask the JNDI resolver
                if (jndiName == null) {
                    try {
                        jndiName = this.containerJNDIResolver.getEJBJNDIUniqueName(itfName, beanName);
                    } catch (EZBJNDIResolverException e) {
                        logger.error("Cannot find JNDI Name on class {0} for interface {1} and beanName {2}",
                            this.classAnnotationMetadata.getClassName(), itfName, beanName);
                    }
                }

                // Not null, bind it
                if (jndiName != null) {
                    logger.debug("Asking jndi name on class {0} for interface {1} and beanName {2}. Result = {3}",
                            this.classAnnotationMetadata.getClassName(), itfName, beanName, jndiName);

                    callMethodJndiEnv(jndiName, typeInterface, mv, methodMetaData, this.classAnnotationMetadata
                            .getClassName(), REGISTRY);

                    // get enc name (or the default name) and bind result
                    String encName = getJndiName(jEjb.getName(), methodMetaData);
                    callBindLookupJndiRef(encName, jndiName, mv);
                }
            }

            // &#64;PersistenceContext annotation
            if (methodMetaData.isPersistenceContext()) {
                Type typeInterface = validateSetterMethod(methodMetaData);
                String itfName = typeInterface.getClassName();

                // Check that arg of the method is EntityManager
                if (!ENTITYMANAGER_ITF.equals(itfName)) {
                    throw new IllegalStateException(
                            "Trying to applied @PersistenceContext on an invalid method in the class '"
                                    + this.classAnnotationMetadata.getClassName() + "', method = " + methodMetaData);
                }
                logger.debug("Add injection for PersistenceContext on method {0} of class {1}", methodMetaData
                        .getMethodName(), this.classAnnotationMetadata.getClassName());

                IJavaxPersistenceContext javaxPersistenceContext = methodMetaData.getJavaxPersistenceContext();
                // add
                // setterName(EntityManagerHelper.getEntityManager(getEasyBeansContext(),
                // "myUnitName", PersistenceContextType.EXTENDED);

                mv.visitVarInsn(ALOAD, 0);

                // call em helper
                addCallEntityManagerHelper(javaxPersistenceContext, mv);

                // call setter method
                mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData
                        .getMethodName(), "(Ljavax/persistence/EntityManager;)V");


                // bind value in ENC environment
                javaxPersistenceContext.setName(getJndiName(javaxPersistenceContext.getName(), methodMetaData));
                bindClassPersistenceContext(javaxPersistenceContext, mv);


            }

            // &#64;PersistenceUnit annotation
            if (methodMetaData.isPersistenceUnit()) {
                Type typeInterface = validateSetterMethod(methodMetaData);
                String itfName = typeInterface.getClassName();
                // Check that attribute is EntityManager
                if (!ENTITYMANAGERFACTORY_ITF.equals(itfName)) {
                    throw new IllegalStateException(
                            "Trying to applied @PersistenceUnit on an invalid method in the class '"
                                    + this.classAnnotationMetadata.getClassName() + "', method = " + methodMetaData);
                }
                logger.debug("Add injection for PersistenceUnit on on method {0} of class {1}", methodMetaData
                        .getMethodName(), this.classAnnotationMetadata.getClassName());

                IJavaxPersistenceUnit javaxPersistenceUnit = methodMetaData.getJavaxPersistenceUnit();

                // add
                // setterName(EntityManagerHelper.getEntityManagerFactory(getEasyBeansContext(),
                // "myUnitName"));

                mv.visitVarInsn(ALOAD, 0);
                // get EMF
                addCallEntityManagerFactoryHelper(javaxPersistenceUnit, mv);
                // call setter method
                mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), methodMetaData
                        .getMethodName(), "(Ljavax/persistence/EntityManagerFactory;)V");

                // Bind value in JNDI
                javaxPersistenceUnit.setName(getJndiName(javaxPersistenceUnit.getName(), methodMetaData));
                bindClassPersistenceUnit(javaxPersistenceUnit, mv);
            }

            // &#64;WebServiceRef annotation
            IJaxwsWebServiceRef jWebServiceRef = methodMetaData.getJaxwsWebServiceRef();
            if (jWebServiceRef != null) {

                Type typeInterface = validateSetterMethod(methodMetaData);

                // Validate
                validateAccessMethodAnnotation(methodMetaData);

                // Perform a java:comp/env lookup
                callMethodJndiEnv(jWebServiceRef.getName(),
                                  typeInterface,
                                  mv,
                                  methodMetaData,
                                  this.classAnnotationMetadata.getClassName(),
                                  JAVA_COMP_ENV);
            }

        }
    }


    /**
     * Generate all lookup calls.
     * @param mv the method visitors
     * @param methodMetadatas the metadata to use
     */
    protected void bindLookupClass(final MethodVisitor mv) {
        for (LookupEncEntry lookupEncEntry : this.deferedEntries) {
            callBindLookupJndiRef(lookupEncEntry.getEncName(), lookupEncEntry.getLookupName(), mv);
        }

    }

    /**
     * Generate all lookup calls.
     * @param mv the method visitors
     * @param methodMetadatas the metadata to use
     */
    protected void bindLookupMethods(final MethodVisitor mv) {
        for (EasyBeansEjbJarMethodMetadata methodMetaData : this.deferedMethods) {
            // &#64;EJB annotation
            IJEjbEJB jEjb = methodMetaData.getJEjbEJB();
            if (jEjb != null) {
                logger.debug("Add injection for EJB on method {0} of class {1}", methodMetaData.getMethodName(),
                        this.classAnnotationMetadata.getClassName());

                Type typeInterface = validateSetterMethod(methodMetaData);

                // Lookup is not null else we're not here
                String lookup = jEjb.getLookup();
                logger.debug("Asking lookup name on class {0} with lookupName {1}",
                            this.classAnnotationMetadata.getClassName(), lookup);

                    callMethodJndiEnv(lookup, typeInterface, mv, methodMetaData, this.classAnnotationMetadata
                            .getClassName(), REGISTRY);

                    // get enc name (or the default name) and bind result
                    String encName = getJndiName(jEjb.getName(), methodMetaData);
                    callBindLookupJndiRef(encName, lookup, mv);
            }
        }
    }

    /**
     * Generate all lookup calls.
     * @param mv the method visitors
     * @param fieldMetadatas the metadata to use
     */
    protected void bindLookupFields(final MethodVisitor mv) {
        for (EasyBeansEjbJarFieldMetadata fieldMetaData : this.deferedFields) {

            Type typeInterface = Type.getType(fieldMetaData.getJField().getDescriptor());

            // &#64;EJB annotation
            IJEjbEJB jEjb = fieldMetaData.getJEjbEJB();
            if (jEjb != null) {
                // validate
                validateAccessFieldAnnotation(fieldMetaData);

                // Lookup is not null else we're not here
                String lookup = jEjb.getLookup();
                logger.debug("Asking lookup name on class {0} with lookupName {1}",
                        this.classAnnotationMetadata.getClassName(), lookup);
                callAttributeJndi(lookup, typeInterface, mv, fieldMetaData, this.classAnnotationMetadata
                        .getClassName(), REGISTRY);
                callBindAttributeJndi(jEjb.getName(), lookup, mv, fieldMetaData);
            }


        }
    }



    /**
     * Ensure that this method is a valid setter method and return ASM type of the first arg of the method.
     * @param methodMetaData the metadata to check
     * @return ASM type of the first arg of the method.
     */
    private Type validateSetterMethod(final EasyBeansEjbJarMethodMetadata methodMetaData) {
        // validate access
        validateAccessMethodAnnotation(methodMetaData);

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
     * Return true if the given type is a type used in env-entry.
     * @param name the name of the env-entry
     * @param type an ASM type.
     * @return true if this entry is used in env-entry.
     */
    private boolean isEnvEntry(final String name, final Type type) {
        // Is the name in the env-entries ?
        Collection<? extends IEnvEntry> envEntries = null;

        // Part of a bean ?
        if (this.beanChildClassAnnotationMetadata != null) {
            envEntries = this.beanChildClassAnnotationMetadata.getEnvEntryCollection();
        } else {
            envEntries = this.classAnnotationMetadata.getEnvEntryCollection();
        }
        if (envEntries != null && name != null) {
            // search name
            for (IEnvEntry envEntry : envEntries) {
                // Found env-entry
                if (name.equals(envEntry.getName()) || "java:comp/env/".concat(name).equals(envEntry.getName())) {
                    // Inject only if there is a value that override
                    if (envEntry.getValue() != null) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Add a call to getEasyBeansContext() method in the given method visitor.
     * @param mv the method visitor on which instructions are added
     * @param castDesc the cast to do.
     */
    private void addCallGetEasyBeansContext(final MethodVisitor mv, final String castDesc) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(),
                "getEasyBeansContext", "()" + EZB_EJBCONTEXT_DESC);
        if (castDesc != null) {
            mv.visitTypeInsn(CHECKCAST, castDesc);
        }
    }

    /**
     * Add a call to EntityManagerHelper class (PersistenceContext) :
     * EntityManagerHelper.getEntityManager(getEasyBeansContext(),
     * unitName, type of persistence).
     * @param javaxPersistenceContext informations on the persistence context
     * @param mv the method visitor on which instructions are added
     */
    private void addCallEntityManagerHelper(final IJavaxPersistenceContext javaxPersistenceContext, final MethodVisitor mv) {

        // get EasyBeansContext
        addCallGetEasyBeansContext(mv, null);

        // Persistence-unit name
        mv.visitLdcInsn(javaxPersistenceContext.getUnitName());

        // Transaction Type
        mv.visitFieldInsn(GETSTATIC, "javax/persistence/PersistenceContextType", javaxPersistenceContext.getType().toString(),
                "Ljavax/persistence/PersistenceContextType;");

        // this
        mv.visitVarInsn(ALOAD, 0);


        // Call EntityManagerHelper
        mv
        .visitMethodInsn(
                INVOKESTATIC,
                "org/ow2/easybeans/injection/EntityManagerHelper",
                "getEntityManager",
                "(" + EZB_EJBCONTEXT_DESC
                + "Ljava/lang/String;Ljavax/persistence/PersistenceContextType;"
                + "Lorg/ow2/easybeans/api/bean/EasyBeansBean;)"
                + "Ljavax/persistence/EntityManager;");
    }

    /**
     * Add a call to EntityManagerHelper class (PersistenceUnit):
     * EntityManagerHelper.getEntityManagerFactory(getEasyBeansContext(), unitName).
     * @param javaxPersistenceUnit informations on the persistence unit
     * @param mv the method visitor on which instructions are added
     */
    private void addCallEntityManagerFactoryHelper(final IJavaxPersistenceUnit javaxPersistenceUnit, final MethodVisitor mv) {
        // get EasyBeansContext
        addCallGetEasyBeansContext(mv, null);

        // Persistence-unit name
        mv.visitLdcInsn(javaxPersistenceUnit.getUnitName());

        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/EntityManagerHelper",
                "getEntityManagerFactory",
                "(" + EZB_EJBCONTEXT_DESC
                + "Ljava/lang/String;)Ljavax/persistence/EntityManagerFactory;");
    }


    /**
     * Generates a call to JNDILookupHelper class to get the java:comp/env name
     * requested.
     * @param jndiName the name to lookup.
     * @param type the ASM type
     * @param mv the method visitor to write code.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void callJndi(final String jndiName, final Type type, final MethodVisitor mv,
            final String className, final JndiType jndiType) {
        if (!this.staticMode) {
            mv.visitVarInsn(ALOAD, 0);
        }
        generateCallJndi(jndiName, type, mv, className, jndiType);
        CommonClassGenerator.transformObjectIntoPrimitive(type, mv);
    }

    /**
     * Generates a call to JNDI helper.
     * @param jndiName the name to lookup.
     * @param type the ASM type
     * @param mv the method visitor to write code.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void generateCallJndi(final String jndiName, final Type type, final MethodVisitor mv,
            final String className, final JndiType jndiType) {
        mv.visitLdcInsn(jndiName);
        String mName = "";
        switch (jndiType) {
            case JAVA_COMP:
                mName = "getCompJndiName";
                break;
            case JAVA_COMP_ENV:
                mName = "getEnvJndiName";
                break;
            case REGISTRY:
                mName = "getJndiName";
                break;
            case JAVA:
                mName = "getJavaJndiName";
                break;
            default:
                throw new IllegalStateException("invalid type");
        }
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDILookupHelper", mName,
                "(Ljava/lang/String;)Ljava/lang/Object;");
    }


    /**
     * Generates a call to JNDILookupHelper class to get the java:comp/env name
     * requested.
     * @param jndiName the name to lookup.
     * @param type the ASM type.
     * @param mv the method visitor to write code.
     * @param fieldMetaData the metadata of the attribute.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void callAttributeJndi(final String jndiName, final Type type, final MethodVisitor mv,
            final EasyBeansEjbJarFieldMetadata fieldMetaData, final String className, final JndiType jndiType) {
        logger.debug("Add injection for @Resource on attribute {0} of class {1} for the type {2}", fieldMetaData
                .getFieldName(), className, type.getClassName());

        String formattedJndiName = getJndiName(jndiName, fieldMetaData);
        callJndi(formattedJndiName, type, mv, className, jndiType);
        setField(mv, className, fieldMetaData, type);
    }

    /**
     * Generates a call to JNDILookupHelper class to get the java:comp/env name
     * requested.
     * @param jndiName the name to lookup.
     * @param type the ASM type.
     * @param mv the method visitor to write code.
     * @param fieldMetaData the metadata of the attribute.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void callAttributeNotNullJndi(final String jndiName, final Type type, final MethodVisitor mv,
            final EasyBeansEjbJarFieldMetadata fieldMetaData, final String className, final JndiType jndiType) {
        logger.debug("Add injection for @Resource on attribute {0} of class {1} for the type {2}", fieldMetaData
                .getFieldName(), className, type.getClassName());

        /***
         * this.entryFloat = JNDILookupHelper.getEnvJndiName("resource/entry-float", Float.valueOf(this.entryFloat));
         */
        String formattedJndiName = getJndiName(jndiName, fieldMetaData);

        generateCallJndi(formattedJndiName, type, mv, className, jndiType);
        // store result
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label ifNullLabel = new Label();
        mv.visitJumpInsn(IFNULL, ifNullLabel);

        // Read value
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        CommonClassGenerator.transformObjectIntoPrimitive(type, mv);
        setField(mv, className, fieldMetaData, type);

        // if null label
        mv.visitLabel(ifNullLabel);
    }

    /**
     * Sets the value of a field.
     * @param mv the method visitor to use
     * @param className the class which contains the attribute.
     * @param fieldMetaData the metadata corresponding to the attribute
     * @param type the ASM type of the attribute
     */
    private void setField(final MethodVisitor mv, final String className,
            final EasyBeansEjbJarFieldMetadata fieldMetaData, final Type type) {
        mv.visitFieldInsn(setField(), className, fieldMetaData.getFieldName(), type.getDescriptor());
    }

    /**
     * Sets the value of a field.
     * @param mv the method visitor to use
     * @param className the class which contains the method.
     * @param methodMetaData the metadata corresponding to the method *
     * @param type the ASM type of the method
     */
    private void callSetterMethod(final MethodVisitor mv, final String className,
            final EasyBeansEjbJarMethodMetadata methodMetaData, final Type type) {
        mv.visitMethodInsn(INVOKEVIRTUAL, className, methodMetaData.getMethodName(), methodMetaData.getJMethod()
                .getDescriptor());
    }


    /**
     * Generates a call to JNDILookupHelper class to get the java:comp/env name
     * requested.
     * @param jndiName the name to lookup.
     * @param type the ASM type.
     * @param mv the method visitor to write code.
     * @param methodMetaData the metadata of the method.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void callMethodJndiEnv(final String jndiName, final Type type, final MethodVisitor mv,
            final EasyBeansEjbJarMethodMetadata methodMetaData, final String className, final JndiType jndiType) {
        logger.debug("Add injection for @Resource on method {0} of class {1} for the type {2}", methodMetaData
                .getMethodName(), className, type.getClassName());

        String checkedJndiName = getJndiName(jndiName, methodMetaData);
        callJndi(checkedJndiName, type, mv, className, jndiType);
        callSetterMethod(mv, className, methodMetaData, type);
    }


    /**
     * Generates a call to JNDILookupHelper class to get the java:comp/env name
     * requested.
     * @param jndiName the name to lookup.
     * @param type the ASM type.
     * @param mv the method visitor to write code.
     * @param methodMetaData the metadata of the method.
     * @param className the name of the generated class.
     * @param jndiType the type of access (registry, java:comp/env, etc)
     */
    private void callMethodJndiEnvNotNull(final String jndiName, final Type type, final MethodVisitor mv,
            final EasyBeansEjbJarMethodMetadata methodMetaData, final String className, final JndiType jndiType) {
        logger.debug("Add injection for @Resource on method {0} of class {1} for the type {2}", methodMetaData
                .getMethodName(), className, type.getClassName());

        String checkedJndiName = getJndiName(jndiName, methodMetaData);

        /**
         * Object value = JNDILookupHelper.getEnvJndiName("resource/entry-float");
         * if (value != null) {
         *    setFloat(((Float) value).floatValue());
         * }
         */
        generateCallJndi(checkedJndiName, type, mv, className, jndiType);
        // store result
        mv.visitVarInsn(ASTORE, 1);
        mv.visitVarInsn(ALOAD, 1);
        Label ifNullLabel = new Label();
        mv.visitJumpInsn(IFNULL, ifNullLabel);

        // Read value
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        CommonClassGenerator.transformObjectIntoPrimitive(type, mv);
        callSetterMethod(mv, className, methodMetaData, type);
        // if null label
        mv.visitLabel(ifNullLabel);
    }


    /**
     * Generates a call to JNDIBinderHelper class to bind an object into the
     * java:comp/env context.
     * @param encName the name to bind
     * @param jndiName the name to link to (LinkRef)
     * @param mv the method visitor to write code.
     * @param fieldMetaData the metadata of the attribute (that will be bound)
     */
    private void callBindAttributeJndi(final String encName, final String jndiName, final MethodVisitor mv,
            final EasyBeansEjbJarFieldMetadata fieldMetaData) {


        // call : JNDIBinderHelper.bindLinkRefEnvJndiName("localName", "jndiName");
        mv.visitLdcInsn(getJndiName(encName, fieldMetaData));
        mv.visitLdcInsn(jndiName);
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDIBinderHelper",
                "bindLinkRefEnvJndiName", "(Ljava/lang/String;Ljava/lang/String;)V");
    }


    /**
     * Generates a call to JNDIBinderHelper class to link an URL object into the
     * java:comp/env context.
     * @param encName the name to bind
     * @param url the URL that need to be built
     * @param mv the method visitor to write code.
     */
    private void callBindLookupURLRef(final String encName, final String url, final MethodVisitor mv) {
        // use a LinkRef between JNDI name and ENC name
        // call : JNDIBinderHelper.bindLinkRefEnvURL("localName", "url");
        if (url == null) {
            logger.warn("No URL injection for enc Name '" + encName + "' as the specified mapped-name is null");
            return;
        }
        mv.visitLdcInsn(encName);
        mv.visitLdcInsn(url);
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDIBinderHelper",
                "bindLinkRefEnvURL", "(Ljava/lang/String;Ljava/lang/String;)V");

        logger.debug("Linking Object with URL '" + url + "' to ENC name '" + encName + "' for the class '"
                + this.classAnnotationMetadata.getClassName() + "'.");
    }

    /**
     * Generates a call to JNDIBinderHelper class to link an object into the
     * java:comp/env context.
     * @param encName the name to bind
     * @param jndiName the jndi name to use for the link
     * @param mv the method visitor to write code.
     */
    private void callBindLookupJndiRef(final String encName, final String jndiName, final MethodVisitor mv) {
        // use a LinkRef between JNDI name and ENC name
        // call : JNDIBinderHelper.bindLinkRefEnvJndiName("localName", "jndiName");

        mv.visitLdcInsn(encName);
        mv.visitLdcInsn(jndiName);
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDIBinderHelper",
                "bindLinkRefEnvJndiName", "(Ljava/lang/String;Ljava/lang/String;)V");

        logger.debug("Linking Object with JNDI name '" + jndiName + "' to ENC name '" + encName + "' for the class '"
                + this.classAnnotationMetadata.getClassName() + "'.");
    }


    /**
     * Bind a ref for an EJB in ENC environment.
     * @param jEJB annotation to analyze
     * @param mv the visitor on which write the bytecode.
     */
    private void bindClassEJB(final IJEjbEJB jEJB, final MethodVisitor mv) {

        // JNDIBinderHelper.bindEnvJndiName("encName",
        // JNDILookupHelper.getEnvJndiName("jndiName"));


        // name attribute is mandatory
        String encName = jEJB.getName();
        if (encName == null || "".equals(encName)) {
            throw new IllegalStateException("Error when analyzing @EJB annotation '" + jEJB
                    + "' for the class '" + this.classAnnotationMetadata.getClassName() + "' : No name !");
        }

        // ejbName ?
        String beanName = jEJB.getBeanName();
        String jndiName = null;

        String lookupName = jEJB.getLookup();
        if (lookupName != null) {
            this.deferedEntries.add(new LookupEncEntry(encName, lookupName));
            return;
        }


        if (jEJB.getMappedName() != null && jEJB.getMappedName().length() > 0) {
            jndiName = jEJB.getMappedName();
        } else {
            try {
                jndiName = this.containerJNDIResolver.getEJBJNDIUniqueName(jEJB.getBeanInterface(), beanName);
            } catch (EZBJNDIResolverException e) {
                throw new IllegalStateException("No JNDI name found when analyzing @EJB annotation '" + jEJB
                        + "' for the class '" + this.classAnnotationMetadata.getClassName() + "'.", e);
            }
        }

        // inject code
        callBindLookupJndiRef(encName, jndiName, mv);
    }

    /**
     * Bind a ref for a Resource in ENC environment.
     * @param jAnnotationResource annotation to analyze
     * @param mv the visitor on which write the bytecode.
     */
    private void bindResource(final IJAnnotationResource jAnnotationResource, final MethodVisitor mv) {
        if (jAnnotationResource.getMappedName() != null
                || SESSION_CONTEXT.equals(jAnnotationResource.getType())
                || MESSAGEDRIVEN_CONTEXT.equals(jAnnotationResource.getType())
                || EJBCONTEXT.equals(jAnnotationResource.getType())
                || USERTRANSACTION_ITF.equals(jAnnotationResource.getType())
                || URL_ITF.equals(jAnnotationResource.getType())
                || TIMERSERVICE_ITF.equals(jAnnotationResource.getType())
                || ORB_ITF.equals(jAnnotationResource.getType())
        ) {
            // get name
            String encName = jAnnotationResource.getName();
            if (encName == null || "".equals(encName)) {
                logger.error("No encName for Annotation resource {0}.", jAnnotationResource);
                return;
            }
            String jndiName = null;
            if (SESSION_CONTEXT.equals(jAnnotationResource.getType())
                    || EJBCONTEXT.equals(jAnnotationResource.getType())
                    || MESSAGEDRIVEN_CONTEXT.equals(jAnnotationResource.getType())) {
                // specify JNDI name
                jndiName = "java:comp/EJBContext";
            } else if (USERTRANSACTION_ITF.equals(jAnnotationResource.getType())) {
                // specify JNDI name
                jndiName = "java:comp/UserTransaction";
            } else if (TIMERSERVICE_ITF.equals(jAnnotationResource.getType())) {
                // specify JNDI name
                jndiName = "java:comp/TimerService";
            } else if (ORB_ITF.equals(jAnnotationResource.getType())) {
                // specify JNDI name
                jndiName = "java:comp/ORB";
            } else {
                jndiName = jAnnotationResource.getMappedName();
            }

            // Use MessageDestinationLink if present !
            String messageDestinationLink = jAnnotationResource.getMessageDestinationLink();
            if (messageDestinationLink != null) {
                try {
                    jndiName = this.containerJNDIResolver.getMessageDestinationJNDIUniqueName(messageDestinationLink);
                } catch (EZBJNDIResolverException e) {
                    throw new IllegalStateException("No JNDI name found when analyzing @Resource annotation '"
                            + jAnnotationResource + "' for the class '" + this.classAnnotationMetadata.getClassName() + "'.", e);
                }
            }

            if (jndiName == null) {
                logger.error("MappedName for resource annotation {0} is null, no binding to ENC name {1}",
                        jAnnotationResource, encName);
            } else if (URL_ITF.equals(jAnnotationResource.getType())) {
                // Binds a reference in context in order to build a new URL object
                // The JNDI name is the URL
                callBindLookupURLRef(encName, jndiName, mv);
            } else {
                // inject code for JNDI ref
                callBindLookupJndiRef(encName, jndiName, mv);
            }
        }
    }

    /**
     * Bind a ref for a PersistenceContext in ENC environment.
     * @param javaxPersistenceContext annotation to analyze
     * @param mv the visitor on which write the bytecode.
     */
    private void bindClassPersistenceContext(final IJavaxPersistenceContext javaxPersistenceContext, final MethodVisitor mv) {

        // Add :
        // JNDIBinderHelper.bindEnvJndiName("myName", EntityManagerHelper.getEnt....);

        // if name is not valid, do nothing
        String name = javaxPersistenceContext.getName();
        if (name == null || "".equals(name)) {
            logger.warn("PersistenceContext '" + javaxPersistenceContext + "' has an empty or null name, cannot bind it in ENC.");
            return;
        }

        // name for ENC
        mv.visitLdcInsn(name);

        // call em helper
        addCallEntityManagerHelper(javaxPersistenceContext, mv);

        // bind in ENC
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDIBinderHelper", "bindEnvJndiName",
        "(Ljava/lang/String;Ljava/lang/Object;)V");
    }


    /**
     * Bind a ref for a PersistenceUnit in ENC environment.
     * @param javaxPersistenceUnit annotation to analyze
     * @param mv the visitor on which write the bytecode.
     */
    private void bindClassPersistenceUnit(final IJavaxPersistenceUnit javaxPersistenceUnit, final MethodVisitor mv) {
        // Add :
        // JNDIBinderHelper.bindEnvJndiName("myName", EntityManagerFactory.getEnt....);

        // if name is not valid, do nothing
        String name = javaxPersistenceUnit.getName();
        if (name == null || "".equals(name)) {
            logger.warn("PersistenceUnit '" + javaxPersistenceUnit + "' has an empty or null name, cannot bind it in ENC.");
            return;
        }

        // name for ENC
        mv.visitLdcInsn(name);

        // get EMF
        addCallEntityManagerFactoryHelper(javaxPersistenceUnit, mv);

        // bind in ENC
        mv.visitMethodInsn(INVOKESTATIC, "org/ow2/easybeans/injection/JNDIBinderHelper", "bindEnvJndiName",
        "(Ljava/lang/String;Ljava/lang/Object;)V");
    }


    /**
     * @return the opCode used for non-static or static mode.
     */
    private int setField() {
        int opCode = PUTFIELD;
        if (this.staticMode) {
            opCode = PUTSTATIC;
        }
        return opCode;
    }

    /**
     * Check that the annotation is not used on a final field or static field
     * (except client mode).
     * @param field the attribute to check.
     */
    private void validateAccessFieldAnnotation(final EasyBeansEjbJarFieldMetadata field) {
        if (accessTest(field.getJField().getAccess(), ACC_FINAL)) {
            throw new IllegalStateException("The '" + field
                    + "' attribute is a final attribute which is not compliant for dependency injection.");
        }
        if (!this.staticMode && accessTest(field.getJField().getAccess(), ACC_STATIC)) {
            throw new IllegalStateException("The '" + field
                    + "' attribute is a static attribute which is not compliant for dependency injection.");
        }
    }

    /**
     * Check that the annotation is not used on a final field or static field
     * (except client mode).
     * @param methodData the method to check.
     */
    private void validateAccessMethodAnnotation(final EasyBeansEjbJarMethodMetadata methodData) {
        if (!this.staticMode && accessTest(methodData.getJMethod().getAccess(), ACC_STATIC)) {
            throw new IllegalStateException("The '" + methodData
                    + "' method is a static attribute which is not compliant for dependency injection.");
        }
    }


    /**
     * @param access the full access modified to check
     * @param checkedAccess the access to check
     * @return true if it is ok, else false.
     */
    private boolean accessTest(final int access, final int checkedAccess) {
        return (access & checkedAccess) == checkedAccess;
    }

}
