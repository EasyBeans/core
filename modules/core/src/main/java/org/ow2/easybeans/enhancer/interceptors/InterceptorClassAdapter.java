/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: InterceptorClassAdapter.java 5997 2011-10-13 15:12:47Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.bean.lifecycle.EasyBeansMDBLifeCycle;
import org.ow2.easybeans.api.bean.lifecycle.EasyBeansManagedBeanLifeCycle;
import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSFSBLifeCycle;
import org.ow2.easybeans.api.bean.lifecycle.EasyBeansSLSBLifeCycle;
import org.ow2.easybeans.asm.ClassAdapter;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.Label;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.annotations.exceptions.ResolverException;
import org.ow2.easybeans.deployment.annotations.helper.bean.BusinessMethodResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceInterfacesHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.InheritanceMethodResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.InterfaceAnnotatedHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.SessionBeanHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.TransactionResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.managedbean.ManagedBeanBusinessMethodResolver;
import org.ow2.easybeans.deployment.annotations.helper.bean.mdb.MDBBeanHelper;
import org.ow2.easybeans.deployment.annotations.helper.bean.mdb.MDBListenerBusinessMethodResolver;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.DefinedClass;
import org.ow2.easybeans.enhancer.bean.BeanClassAdapter;
import org.ow2.easybeans.enhancer.injection.InjectionClassAdapter;
import org.ow2.easybeans.enhancer.lib.AnnotationRecorder;
import org.ow2.easybeans.enhancer.lib.MethodAdapterWithAnnotationRecorder;
import org.ow2.easybeans.enhancer.lib.MethodRenamer;
import org.ow2.easybeans.enhancer.lib.ParameterAnnotationRecorder;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IJEjbSchedule;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.AROUND_INVOKE;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.DEP_INJECT;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.POST_ACTIVATE;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.POST_CONSTRUCT;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.PRE_DESTROY;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.PRE_PASSIVATE;
import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.TIMED_OBJECT;

/**
 * This class delegates the creation of an implementation of a
 * EasyBeansInvocationContext interface and intercepts all business methods() of a
 * Bean.
 * @author Florent Benoit
 */
public class InterceptorClassAdapter extends ClassAdapter implements Opcodes {

    /**
     * Logger.
     */
    private Log LOGGER = LogFactory.getLog(InterceptorClassAdapter.class);

    /**
     * If this flag is enabled, it allows to share the bean class with other frameworks/tools that load the enhanced class.
     */
    public static final String EASYBEANS_SHARED_CLASS_FLAG = "easybeans.sharedclass";

    /**
     * Metadata available by this adapter for a class.
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata;

    /**
     * List of methods which have been renamed.
     */
    private List<JMethod> renamedMethods = null;

    /**
     * Mappping between className and the bytecode.
     */
    private List<DefinedClass> definedClasses = null;

    /**
     * List of generated classes for each interceptor type.
     */
    private List<InterceptorType> generatedTypes = null;

    /**
     * List of interceptors classes used by the bean.
     */
    private List<String> beanInterceptors = null;

    /**
     * If it is true, interfaces of interceptor lifecycle will be added.
     */
    private boolean addInterface = true;

    /**
     * Map between desc of a method and the list of annotation on the method.
     */
    private Map<String, List<AnnotationRecorder>> annotationsOfMethod = null;

    /**
     * Map between desc of a method and the list of annotation of parameters of the method.
     */
    private Map<String, List<ParameterAnnotationRecorder>> parametersAnnotationsOfMethod = null;

    /**
     * Classloader used to load classes.
     */
    private ClassLoader readLoader = null;

    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param readLoader the classloader used to load classes.
     */
    public InterceptorClassAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final ClassVisitor cv, final ClassLoader readLoader) {
        this(classAnnotationMetadata, cv, false);
        this.readLoader = readLoader;
        this.beanInterceptors = new ArrayList<String>();
        this.annotationsOfMethod = new HashMap<String, List<AnnotationRecorder>>();
        this.parametersAnnotationsOfMethod = new HashMap<String, List<ParameterAnnotationRecorder>>();
    }

    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param addInterface adds lifecycle interface for a given bean.
     */
    public InterceptorClassAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata,
            final ClassVisitor cv, final boolean addInterface) {
        super(cv);
        this.classAnnotationMetadata = classAnnotationMetadata;
        this.renamedMethods = new ArrayList<JMethod>();
        this.definedClasses = new ArrayList<DefinedClass>();
        this.addInterface = addInterface;
        this.generatedTypes = new ArrayList<InterceptorType>();
    }


    /**
     * Visits the header of the class.
     * @param version the class version.
     * @param access the class's access flags (see
     *        {@link org.ow2.easybeans.asm.Opcodes}). This parameter also indicates
     *        if the class is deprecated.
     * @param name the internal name of the class (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     * @param signature the signature of this class. May be <tt>null</tt> if
     *        the class is not a generic one, and does not extend or implement
     *        generic classes or interfaces.
     * @param superName the internal of name of the super class (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     *        For interfaces, the super class is {@link Object}. May be
     *        <tt>null</tt>, but only for the {@link Object} class.
     * @param interfaces the internal names of the class's interfaces (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     *        May be <tt>null</tt>.
     */
    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName,
            final String[] interfaces) {

        String[] newInterfaces = null;

        // Add new interface for lifecycle (if asked)
        if (this.classAnnotationMetadata.isBean() && this.addInterface) {
            // copy old interfaces in the new array
            newInterfaces = new String[interfaces.length + 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);

            int indexElement = newInterfaces.length - 1;

            // Add the right interface (SLSB, SFSB, MDB)
            if (this.classAnnotationMetadata.isStateless()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansSLSBLifeCycle.class);
            } else if (this.classAnnotationMetadata.isStateful()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansSFSBLifeCycle.class);
            } else if (this.classAnnotationMetadata.isMdb()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansMDBLifeCycle.class);
            } else if (this.classAnnotationMetadata.isManagedBean()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansManagedBeanLifeCycle.class);
            } else {
                throw new IllegalStateException("Bean '" + this.classAnnotationMetadata.getClassName() + "' not SLSB, SFSB, MDB or MB");
            }
        } else {
            newInterfaces = interfaces;
        }

        super.visit(version, access, name, signature, superName, newInterfaces);

    }

    /**
     * Visits information about an inner class. This inner class is not
     * necessarily a member of the class being visited.
     * @param name the internal name of an inner class (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     * @param outerName the internal name of the class to which the inner class
     *        belongs (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     *        May be <tt>null</tt>.
     * @param innerName the (simple) name of the inner class inside its
     *        enclosing class. May be <tt>null</tt> for anonymous inner
     *        classes.
     * @param access the access flags of the inner class as originally declared
     *        in the enclosing class.
     */
    @Override
    public void visitInnerClass(final String name, final String outerName, final String innerName, final int access) {
        super.visitInnerClass(name, outerName, innerName, access);
    }

    /**
     * Visits a method of the class. T
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link org.ow2.easybeans.asm.Type}).
     * @param signature the method's signature. May be <tt>null</tt> if the
     *        method parameters, return type and exceptions do not use generic
     *        types.
     * @param exceptions the internal names of the method's exception classes
     *        (see
     *        {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}).
     *        May be <tt>null</tt>.
     * @return an object to visit the byte code of the method, or <tt>null</tt>
     *         if this class visitor is not interested in visiting the code of
     *         this method.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {
        JMethod jMethod = new JMethod(access, name, desc, signature, exceptions);
        String newName = name;
        int newAccess = access;

        if (isScheduleMethod(jMethod)) {
            // Ensure that the access is public for all timeout methods
            newAccess = Opcodes.ACC_PUBLIC;
        }


        // Intercepted method : need to change the method name for Beans
        if (isInterceptedMethod(jMethod) && this.classAnnotationMetadata.isBean()) {
            // Add the method as renamed
            this.renamedMethods.add(jMethod);

            // Ensure that the access is public for all intercepted methods
            newAccess = Opcodes.ACC_PUBLIC;

            // Rename the method name
            newName = MethodRenamer.encode(name);
        }

        // Interceptor method : need to change access to public.
        if (!isDependencyInjectionMethod(jMethod) && !isInjectedMethod(jMethod) && isInterceptorMethod(jMethod)) {
            // Change modifier to public
            newAccess = Opcodes.ACC_PUBLIC;
        }

        // Needs to keep annotation for intercepted method
        if (this.classAnnotationMetadata.isBean()) {
            if (isInterceptedMethod(jMethod) || isInterceptorMethod(jMethod)) {
                MethodAdapterWithAnnotationRecorder methodAdapter = new MethodAdapterWithAnnotationRecorder(super.visitMethod(
                        newAccess, newName, desc, signature, exceptions));
                // keep annotations
                this.annotationsOfMethod.put(name + desc, methodAdapter.getAnnotationRecorders());
                this.parametersAnnotationsOfMethod.put(name + desc, methodAdapter.getParameterAnnotationRecorders());

                return methodAdapter;
            }
        }
        // Else only call super method
        return super.visitMethod(newAccess, newName, desc, signature, exceptions);
    }

    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        super.visitEnd();


        // If we have methods to generate, generate them
        for (EasyBeansEjbJarMethodMetadata m : this.classAnnotationMetadata.getMethodMetadataCollection()) {
            if (m.isPrivateSuperCallGenerated()) {
                // Generate super call
                String originalMethodName = m.getSuperPrivateMethodName();
                generateCallSuperEncodedMethod(m, m.getMethodName(), originalMethodName, m.getOriginalClassMetadata().getClassName());
            }
        }


        // For Bean only
        if (this.classAnnotationMetadata.isBean()) {
            // Add default lifecycle methods. These methods will call defined
            // lifecycle callback method and super methods or will do nothing.
            EasyBeansEjbJarMethodMetadata posConsMetaData = generateBeanLifeCycleMethod(this.classAnnotationMetadata, POST_CONSTRUCT);
            EasyBeansEjbJarMethodMetadata preDesMetaData = generateBeanLifeCycleMethod(this.classAnnotationMetadata, PRE_DESTROY);
            EasyBeansEjbJarMethodMetadata postActMetaData = generateBeanLifeCycleMethod(this.classAnnotationMetadata, POST_ACTIVATE);
            EasyBeansEjbJarMethodMetadata prePassMetaData = generateBeanLifeCycleMethod(this.classAnnotationMetadata, PRE_PASSIVATE);

            // Generate class for dependency injection
            generateClass(
                    new EasyBeansEjbJarMethodMetadata(InjectionClassAdapter.INJECTED_JMETHOD, this.classAnnotationMetadata),
                    DEP_INJECT);

            // Generate class for timer
            // Create the method by cloning the existing timer method
            EasyBeansEjbJarMethodMetadata timerMethodAnnotationMetadata = null;
            for (EasyBeansEjbJarMethodMetadata m : this.classAnnotationMetadata.getMethodMetadataCollection()) {
                // Found the timer method ?
                if (m.isTimeout()) {
                    // clone this method (to get the correct interceptors, etc)
                    timerMethodAnnotationMetadata = (EasyBeansEjbJarMethodMetadata) m.clone();
                    // Change the method name to the generated method
                    timerMethodAnnotationMetadata.setJMethod(BeanClassAdapter.TIMER_JMETHOD);

                    // set the class
                    timerMethodAnnotationMetadata.setClassMetadata(this.classAnnotationMetadata);
                    // It is not inherited as it's build on this class level
                    timerMethodAnnotationMetadata.setInherited(false, null);
                    break;
                }

            }
            // build an empty one if not built just before
            if (timerMethodAnnotationMetadata == null) {
                timerMethodAnnotationMetadata = new EasyBeansEjbJarMethodMetadata(BeanClassAdapter.TIMER_JMETHOD,
                        this.classAnnotationMetadata);
            }
            // Generate the class
            generateClass(timerMethodAnnotationMetadata, TIMED_OBJECT);


            // Need to generate the implementation of EasyBeansInvocationContext Impl on intercepted methods
            for (EasyBeansEjbJarMethodMetadata method : this.classAnnotationMetadata.getMethodMetadataCollection()) {

                // No else if, need to generate an invocationcontext for each case
                if (method.isBusinessMethod() && !method.isIgnored()) {
                    generateClass(method, AROUND_INVOKE);

                    // method was not renamed (it is inherited), need to generate a method calling super method().
                    if (!this.renamedMethods.contains(method.getJMethod())) {
                        generateCallSuperEncodedMethod(method);
                    }
                }
            }


            // First method is the method that has been generated by the call to generateBeanLifeCycleMethod
            // This is method which needs to be intercepted. (there is always
            // one method as we added default method, so no need to check
            // null list)
            generateClass(posConsMetaData, POST_CONSTRUCT);
            generateClass(preDesMetaData, PRE_DESTROY);
            generateClass(prePassMetaData, PRE_PASSIVATE);
            generateClass(postActMetaData, POST_ACTIVATE);

            // Then generate the interceptorManager
            String generatedClName = this.classAnnotationMetadata.getClassName()
                    + EasyBeansInvocationContextGenerator.SUFFIX_INTERCEPTOR_MANAGER;
            InterceptorManagerGenerator interceptorManagerGenerator = new InterceptorManagerGenerator(
                    this.classAnnotationMetadata.getEjbJarDeployableMetadata(), generatedClName, this.beanInterceptors,
                    this.readLoader);
            interceptorManagerGenerator.generate();
            DefinedClass dc = new DefinedClass(generatedClName.replace("/", "."), interceptorManagerGenerator.getBytes());
            // this class will be defined later on the classloader
            this.definedClasses.add(dc);

        }
    }

    /**
     * Generates the call to InvocationContext impl proceed method after
     * building a new object. ie :
     *
     * <pre>
     * public int generatedMethodName(int a, int b) throws MyException {
     *     try {
     *         return ((Integer) new MethodAddInvocationContextImpl(this, a, b).proceed()).intValue();
     *     } catch (MyException e) {
     *         throw e;
     *     } catch (Exception e) {
     *         if (e instanceof RuntimeException) {
     *             throw (RuntimeException) e;
     *         } else {
     *             throw new RuntimeException(e);
     *         }
     *     }
     * }
     * </pre>
     *
     * @param method the annotation metadata of the method
     * @param genInvCtx the generator of the EasyBeansInvocationContext impl class.
     * @param interceptorType the type of method which is intercepted
     */
    private void generateCallToInvocationContext(final EasyBeansEjbJarMethodMetadata method,
            final EasyBeansInvocationContextGenerator genInvCtx, final InterceptorType interceptorType) {

        /**
         * Method name, two cases :
         *      - AroundInvoke : method = original name
         *      - LifeCycle    : method = postConstructEasyBeansLifeCycle, preDestroyEasyBeansLifeCycle
         */
        String generatedMethodName = null;
        switch (interceptorType) {
            case AROUND_INVOKE:
                generatedMethodName = method.getMethodName();
                break;
            case DEP_INJECT:
            case TIMED_OBJECT:
                generatedMethodName = MethodRenamer.decode(method.getMethodName());
                break;
            case POST_CONSTRUCT:
                generatedMethodName = "postConstructEasyBeansLifeCycle";
                break;
            case PRE_DESTROY:
                generatedMethodName = "preDestroyEasyBeansLifeCycle";
                break;
            case PRE_PASSIVATE:
                generatedMethodName = "prePassivateEasyBeansLifeCycle";
                break;
            case POST_ACTIVATE:
                generatedMethodName = "postActivateEasyBeansLifeCycle";
                break;
                default:
                    throw new RuntimeException("No generated method name found for method '" + method.getMethodName() + "'");
        }

        if (generatedMethodName == null) {
            throw new RuntimeException("No generated method name found for method '" + method.getMethodName() + "'");
        }

        // Adds a method which will call the invocationcontext impl
        MethodVisitor mv = this.cv.visitMethod(ACC_PUBLIC, generatedMethodName, method.getJMethod().getDescriptor(),
                null, method.getJMethod().getExceptions());


        if (this.renamedMethods.contains(method.getJMethod())) {
            // Processing a renamed method,
            // There is a chance that we need to transfer recorded annotations

            // replay annotations
            String nameDesc =  MethodRenamer.tryDecode(generatedMethodName) + method.getJMethod().getDescriptor();
            List<AnnotationRecorder> annotationRecorders = this.annotationsOfMethod.get(nameDesc);
            if (annotationRecorders != null) {
                for (AnnotationRecorder annotationRecorder : annotationRecorders) {
                    annotationRecorder.replay(mv);
                }
            }

            // parameters annotations
            List<ParameterAnnotationRecorder> parameterAnnotationRecorders = this.parametersAnnotationsOfMethod.get(nameDesc);
            if (parameterAnnotationRecorders != null) {
                for (ParameterAnnotationRecorder parameterAnnotationRecorder : parameterAnnotationRecorders) {
                    parameterAnnotationRecorder.replay(mv);
                }
            }
        } else {
            // Completely new generated method so some Annotations should be added (for example to exclude this method for JAX-WS)
            CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);
        }

        mv.visitCode();

        if (Boolean.getBoolean(EASYBEANS_SHARED_CLASS_FLAG)) {
            // if the class is used by other framework, interceptors shouldn't be invoked, so call the renamed method directly.
            // if (getEasyBeansFactory() == null) {
            //     original$MethodHelloWorld();
            // }


            // if the factory is not null, skip the code and go the easyBeansFactoryNotNullLabel label
            Label easyBeansFactoryNotNullLabel = new Label();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansFactory", "()Lorg/ow2/easybeans/api/Factory;");
            mv.visitJumpInsn(IFNONNULL, easyBeansFactoryNotNullLabel);

            // factory is null, needs to call the original/renamed method
            Type[] args = Type.getArgumentTypes(method.getJMethod().getDescriptor());
            mv.visitVarInsn(ALOAD, 0);

            // for each argument of the methods :
            int methodArg = 1;
            for (Type type : args) {
                int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
                mv.visitVarInsn(opCode, methodArg);
                // Double and Long are special parameters
                if (opCode == LLOAD || opCode == DLOAD) {
                    methodArg++;
                }
                methodArg++;
            }

            String encodedMethodName = MethodRenamer.encode(method.getMethodName());
            if (method.getJMethod().equals(InjectionClassAdapter.INJECTED_JMETHOD)) {
                encodedMethodName = InjectionClassAdapter.INJECTED_JMETHOD.getName();
            }


            // Call the original method
            mv.visitMethodInsn(INVOKEVIRTUAL,  this.classAnnotationMetadata.getClassName(), encodedMethodName, method.getJMethod().getDescriptor());

            // Cast and return value
            Type returnType = Type.getReturnType(method.getJMethod().getDescriptor());
            CommonClassGenerator.addReturnType(returnType, mv);
            // if the factory is not null, skip the previous code
            mv.visitLabel(easyBeansFactoryNotNullLabel);
        }




        if (interceptorType == AROUND_INVOKE || interceptorType  == POST_CONSTRUCT || interceptorType == PRE_DESTROY) {
            //        if (getEasyBeansInvocationContextFactory() != null) {
            //            try {
            //                return ((Integer) getEasyBeansInvocationContextFactory().getContext(this, getEasyBeansDynamicInterceptorManager(), interceptorType.toString(), "addMethodSignature" , a, b)
            //                        .proceed()).intValue();
            //            } catch (Exception e) {
            //                throw new RuntimeException(e);
            //            }
            //        }
            Label tryLabelStart = new Label();
            Label l1 = new Label();
            Label l2 = new Label();
            mv.visitTryCatchBlock(tryLabelStart, l1, l2, "java/lang/Exception");
            // Add bean (as first argument)
            mv.visitVarInsn(ALOAD, 0);
            // Test if invocation context factory is null
            // If there is no invocation context factory, jump to the end
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansInvocationContextFactory", "()Lorg/ow2/easybeans/api/interceptor/EZBInvocationContextFactory;");
            Label labelNoInvocationContextFactory = new Label();
            mv.visitJumpInsn(IFNULL, labelNoInvocationContextFactory);

            // Begin of the try block
            mv.visitLabel(tryLabelStart);
            mv.visitVarInsn(ALOAD, 0);

            // There is an invocation context factory, get it
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansInvocationContextFactory", "()Lorg/ow2/easybeans/api/interceptor/EZBInvocationContextFactory;");
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 0);
            // Get the interceptor manager
            mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansDynamicInterceptorManager", "()Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;");

            // Add the interceptor type
            mv.visitFieldInsn(GETSTATIC, Type.getInternalName(InterceptorType.class), interceptorType.toString(), "Lorg/ow2/util/ee/metadata/ejbjar/api/InterceptorType;");
            mv.visitMethodInsn(INVOKEVIRTUAL, Type.getInternalName(InterceptorType.class), "toString", "()Ljava/lang/String;");

            // Signature of the method
            mv.visitLdcInsn(MethodHelper.getSignature(method));

            // Arguments of the method
            // parameters = new Object[] {arg0, arg1, arg...};
            // put size of the array
            Type[] args = Type.getArgumentTypes(method.getJMethod().getDescriptor());
            int methodArg = 1;

            mv.visitIntInsn(BIPUSH, args.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            // for each argument of the methods :
            int argCount = 0;
            for (Type type : args) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, argCount);

                int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
                mv.visitVarInsn(opCode, methodArg);
                // Double and Long are special parameters
                if (opCode == LLOAD || opCode == DLOAD) {
                    methodArg++;
                }
                methodArg++;

                // if type is not object type, need to convert it
                // for example : Integer.valueOf(i);
                CommonClassGenerator.transformPrimitiveIntoObject(type, mv);
                mv.visitInsn(AASTORE);
                argCount++;
            }

            Type returnType = Type.getReturnType(method.getJMethod().getDescriptor());

            // Call getContext method
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/interceptor/EZBInvocationContextFactory", "getContext", "(Lorg/ow2/easybeans/api/bean/EasyBeansBean;Lorg/ow2/easybeans/api/interceptor/EZBInterceptorManager;Ljava/lang/String;Ljava/lang/String;[Ljava/lang/Object;)Lorg/ow2/easybeans/api/EasyBeansInvocationContext;");
            // Call of proceed method
            mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/EasyBeansInvocationContext", "proceed", "()Ljava/lang/Object;");

            // Cast and return value
            CommonClassGenerator.transformObjectIntoPrimitive(returnType, mv);
            mv.visitLabel(l1);
            CommonClassGenerator.addReturnType(returnType, mv);
            mv.visitLabel(l2);




            boolean methodAlreadyThrowJavaLangException = false;

            // Catch blocks
            String[] methodExceptions = method.getJMethod().getExceptions();
            // catch label = exceptions thrown by method + 1
            Label[] catchsLabel = null;
            if (methodExceptions != null) {
                // if the java.lang.Exception is present, don't need two catchs
                // blocks
                // for java/lang/Exception
                if (Arrays.asList(methodExceptions).contains("java/lang/Exception")) {
                    methodAlreadyThrowJavaLangException = true;
                    catchsLabel = new Label[methodExceptions.length];
                } else {
                    // else, add a catch for java.lang.Exception
                    catchsLabel = new Label[methodExceptions.length + 1];
                }
            } else {
                catchsLabel = new Label[1];
            }

            // init labels
            for (int i = 0; i < catchsLabel.length; i++) {
                catchsLabel[i] = new Label();
            }

            // First, do method exceptions (just rethrow the given exception)
            int lastCatchBlockLabel = 0;
            if (methodAlreadyThrowJavaLangException) {
                lastCatchBlockLabel = catchsLabel.length;
            } else {
                lastCatchBlockLabel = catchsLabel.length - 1;
            }

            for (int block = 0; block < lastCatchBlockLabel; block++) {
                mv.visitLabel(catchsLabel[block]);
                mv.visitVarInsn(ASTORE, methodArg);
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitInsn(ATHROW);
            }
            // Now, do the wrapped of Exception into a RuntimeException
            if (!methodAlreadyThrowJavaLangException) {
                // start label
                mv.visitLabel(catchsLabel[lastCatchBlockLabel]);
                mv.visitVarInsn(ASTORE, methodArg);

                // instanceof RuntimeException
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
                Label notInstanceOfRuntimeExceptionLabel = new Label();
                mv.visitJumpInsn(IFEQ, notInstanceOfRuntimeExceptionLabel);

                // throw existing runtime exception (by casting it)
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
                mv.visitInsn(ATHROW);

                // build Runtime exception with given exception
                mv.visitLabel(notInstanceOfRuntimeExceptionLabel);
                mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
                mv.visitInsn(ATHROW);

            }

            // Perform try/catch blocks with ASM
            int block = 0;
            // method exception
            if (methodExceptions != null) {
                for (String exception : methodExceptions) {
                    mv.visitTryCatchBlock(tryLabelStart, catchsLabel[0], catchsLabel[block], exception);
                    block++;
                }
            }
            // Exception thrown by proceed() call
            if (!methodAlreadyThrowJavaLangException) {
                mv.visitTryCatchBlock(tryLabelStart, catchsLabel[0], catchsLabel[lastCatchBlockLabel], "java/lang/Exception");
            }

            // No invocation context factory end
            mv.visitLabel(labelNoInvocationContextFactory);
        }


        // Start of the Try label of Try/Catch
        Label tryLabel = new Label();
        mv.visitLabel(tryLabel);

        // build new object by calling the constructor
        mv.visitTypeInsn(NEW, genInvCtx.getGeneratedClassName());
        mv.visitInsn(DUP);

        // Add bean (as first argument)
        mv.visitVarInsn(ALOAD, 0);

        // for each argument
        Type[] args = Type.getArgumentTypes(method.getJMethod().getDescriptor());
        int methodArg = 1;
        for (Type type : args) {
            int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
            mv.visitVarInsn(opCode, methodArg);
            // Double and Long are special parameters
            if (opCode == LLOAD || opCode == DLOAD) {
                methodArg++;
            }
            methodArg++;
        }
        Type returnType = Type.getReturnType(method.getJMethod().getDescriptor());

        String constructorDesc = genInvCtx.getConstructorDesc();
        mv.visitMethodInsn(INVOKESPECIAL, genInvCtx.getGeneratedClassName(), "<init>", constructorDesc);
        mv.visitMethodInsn(INVOKEVIRTUAL, genInvCtx.getGeneratedClassName(), "proceed", "()Ljava/lang/Object;");

        CommonClassGenerator.transformObjectIntoPrimitive(returnType, mv);
        CommonClassGenerator.addReturnType(returnType, mv);

        boolean methodAlreadyThrowJavaLangException = false;

        // Catch blocks
        String[] methodExceptions = method.getJMethod().getExceptions();
        // catch label = exceptions thrown by method + 1
        Label[] catchsLabel = null;
        if (methodExceptions != null) {
            // if the java.lang.Exception is present, don't need two catchs
            // blocks
            // for java/lang/Exception
            if (Arrays.asList(methodExceptions).contains("java/lang/Exception")) {
                methodAlreadyThrowJavaLangException = true;
                catchsLabel = new Label[methodExceptions.length];
            } else {
                // else, add a catch for java.lang.Exception
                catchsLabel = new Label[methodExceptions.length + 1];
            }
        } else {
            catchsLabel = new Label[1];
        }

        // init labels
        for (int i = 0; i < catchsLabel.length; i++) {
            catchsLabel[i] = new Label();
        }

        // First, do method exceptions (just rethrow the given exception)
        int lastCatchBlockLabel = 0;
        if (methodAlreadyThrowJavaLangException) {
            lastCatchBlockLabel = catchsLabel.length;
        } else {
            lastCatchBlockLabel = catchsLabel.length - 1;
        }

        for (int block = 0; block < lastCatchBlockLabel; block++) {
            mv.visitLabel(catchsLabel[block]);
            mv.visitVarInsn(ASTORE, methodArg);
            mv.visitVarInsn(ALOAD, methodArg);
            mv.visitInsn(ATHROW);
        }
        // Now, do the wrapped of Exception into a RuntimeException
        if (!methodAlreadyThrowJavaLangException) {
            // start label
            mv.visitLabel(catchsLabel[lastCatchBlockLabel]);
            mv.visitVarInsn(ASTORE, methodArg);

            // instanceof RuntimeException
            mv.visitVarInsn(ALOAD, methodArg);
            mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
            Label notInstanceOfRuntimeExceptionLabel = new Label();
            mv.visitJumpInsn(IFEQ, notInstanceOfRuntimeExceptionLabel);

            // throw existing runtime exception (by casting it)
            mv.visitVarInsn(ALOAD, methodArg);
            mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
            mv.visitInsn(ATHROW);

            // build Runtime exception with given exception
            mv.visitLabel(notInstanceOfRuntimeExceptionLabel);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ALOAD, methodArg);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);

        }

        // Perform try/catch blocks with ASM
        int block = 0;
        // method exception
        if (methodExceptions != null) {
            for (String exception : methodExceptions) {
                mv.visitTryCatchBlock(tryLabel, catchsLabel[0], catchsLabel[block], exception);
                block++;
            }
        }
        // Exception thrown by proceed() call
        if (!methodAlreadyThrowJavaLangException) {
            mv.visitTryCatchBlock(tryLabel, catchsLabel[0], catchsLabel[lastCatchBlockLabel], "java/lang/Exception");
        }

        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * Generate an invocation context object.
     * @param method intercepted method
     * @param interceptorType the type of method which is intercepted
     */
    private void generateClass(final EasyBeansEjbJarMethodMetadata method, final InterceptorType interceptorType) {
        EasyBeansInvocationContextGenerator genInvCtx = new EasyBeansInvocationContextGenerator(method, interceptorType, this.readLoader);
        genInvCtx.generate();

        // Get all interceptors used and that are not defined in the bean
        for (IJClassInterceptor interceptor : genInvCtx.getAllInterceptors()) {
            String interceptorClassName = interceptor.getClassName();
            if (!interceptorClassName.equals(this.classAnnotationMetadata.getClassName())) {
                if (!this.beanInterceptors.contains(interceptorClassName)) {
                    this.beanInterceptors.add(interceptorClassName);
                }
            }
        }
        DefinedClass dc = new DefinedClass(genInvCtx.getGeneratedClassName().replace("/", "."), genInvCtx.getBytes());
        // this class will be defined later on the classloader
        this.definedClasses.add(dc);
        this.generatedTypes.add(interceptorType);
        // generate method calling generated EasyBeansInvocationContext impl
        generateCallToInvocationContext(method, genInvCtx, interceptorType);

    }


    /**
     * Generates a call to the method defined in the super class.
     * public int original$add(int i, int j) {
     *     return super.add(i, j);
     * }
     * @param method the annotation metadata of the method
     */
    private void generateCallSuperEncodedMethod(final EasyBeansEjbJarMethodMetadata method, final String generatedMethodName, final String superMethodName, final String superClassName) {

        JMethod jMethod = method.getJMethod();
        MethodVisitor mv = this.cv.visitMethod(jMethod.getAccess(), generatedMethodName,
                jMethod.getDescriptor(), jMethod.getSignature(), jMethod.getExceptions());

        // Add some flags on the generated method
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);

        mv.visitCode();

        // Add bean (as first argument)
        mv.visitVarInsn(ALOAD, 0);

        // for each argument
        Type[] args = Type.getArgumentTypes(jMethod.getDescriptor());
        int methodArg = 1;
        for (Type type : args) {
            int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
            mv.visitVarInsn(opCode, methodArg);
            // Double and Long are special parameters
            if (opCode == LLOAD || opCode == DLOAD) {
                methodArg++;
            }
            methodArg++;
        }

        // call super class method()
        mv.visitMethodInsn(INVOKESPECIAL, superClassName,
                superMethodName, jMethod.getDescriptor());

        Type returnType = Type.getReturnType(jMethod.getDescriptor());
        CommonClassGenerator.addReturnType(returnType, mv);


        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Generates a call to the method defined in the super class.
     * public int original$add(int i, int j) {
     *     return super.add(i, j);
     * }
     * @param method the annotation metadata of the method
     */
    private void generateCallSuperEncodedMethod(final EasyBeansEjbJarMethodMetadata method) {

        // If the super class is a bean, then it means that we should call the super original$add method
        EasyBeansEjbJarClassMetadata classMetadata = method.getClassMetadata();
        String superClassName = classMetadata.getSuperName();

        // we've metadata of the super class ?
        EasyBeansEjbJarClassMetadata superClassMetadata = classMetadata.getLinkedClassMetadata(superClassName);

        String superMethodName = method.getMethodName();
        if (superClassMetadata.isBean()) {
            // Resolve the bean in order to check for business methods on this class
            try {
                InheritanceInterfacesHelper.resolve(superClassMetadata);

                InterfaceAnnotatedHelper.resolve(superClassMetadata);
                InheritanceMethodResolver.resolve(superClassMetadata);

                // Find business method
                if (superClassMetadata.isSession()) {
                    BusinessMethodResolver.resolve(superClassMetadata);
                } else if (superClassMetadata.isMdb()) {
                    MDBListenerBusinessMethodResolver.resolve(superClassMetadata);
                } else if (superClassMetadata.isManagedBean()) {
                    ManagedBeanBusinessMethodResolver.resolve(superClassMetadata);
                }

                // for each bean, call sub helper
                if (superClassMetadata.isSession()) {
                    SessionBeanHelper.resolve(superClassMetadata);
                } else if (superClassMetadata.isMdb()) {
                    MDBBeanHelper.resolve(superClassMetadata);
                }

            } catch (ResolverException e) {
                this.LOGGER.error("Unable to resolve some methods for a super class which is also a Bean", e);
            }


            EasyBeansEjbJarMethodMetadata superClassMethod = superClassMetadata.getMethodMetadata(method.getJMethod());
            if (superClassMethod.isBusinessMethod()) {
                // Needs to call the super method not changed
                superMethodName = MethodRenamer.encode(method.getMethodName());
            }

        }


        generateCallSuperEncodedMethod(method, MethodRenamer.encode(method.getMethodName()), superMethodName, method.getClassMetadata().getSuperName());
    }





    /**
     * Generates a default method for lifecycle method events.
     * It will call the methods in the super classes of the defined method.
     * @param classMetaData the metadata used to generate method metadata
     * @param interceptorType the type of intercepted method
     * @return the generated method metadata
     */
    private EasyBeansEjbJarMethodMetadata generateBeanLifeCycleMethod(final EasyBeansEjbJarClassMetadata classMetaData,
            final InterceptorType interceptorType) {
        String generatedMethodName = null;
        List<EasyBeansEjbJarMethodMetadata> existingLifecycleMethods = null;
        switch (interceptorType) {
            case AROUND_INVOKE:
            case DEP_INJECT:
            case TIMED_OBJECT:
                //Nothing to generate
                return null;
            case POST_CONSTRUCT:
                generatedMethodName = "beanPostConstruct$generated";
                existingLifecycleMethods = classMetaData.getPostConstructMethodsMetadata();
                break;
            case PRE_DESTROY:
                generatedMethodName = "beanPreDestroy$generated";
                existingLifecycleMethods = classMetaData.getPreDestroyMethodsMetadata();
                break;
            case PRE_PASSIVATE:
                generatedMethodName = "beanPrePassivate$generated";
                existingLifecycleMethods = classMetaData.getPrePassivateMethodsMetadata();
                break;
            case POST_ACTIVATE:
                generatedMethodName = "beanPostActivate$generated";
                existingLifecycleMethods = classMetaData.getPostActivateMethodsMetadata();
                break;
                default:
                    throw new RuntimeException("No generated method name found for interceptorType '" + interceptorType + "'");
        }


        // Generates the body of this method.
        MethodVisitor mv = this.cv.visitMethod(ACC_PUBLIC, generatedMethodName, "()V", null, null);

        // Add some flags on the generated method
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);

        mv.visitCode();
        // Call methods in their order (if any)
        if (existingLifecycleMethods != null) {
            for (EasyBeansEjbJarMethodMetadata method : existingLifecycleMethods) {
                // Inherited or not ?
                String clName = method.getClassMetadata().getClassName();
                mv.visitVarInsn(ALOAD, 0);
                int opcode = INVOKEVIRTUAL;
                if (method.isInherited()) {
                    clName = method.getOriginalClassMetadata().getClassName();
                    opcode = INVOKESPECIAL;
                }
                mv.visitMethodInsn(opcode, clName, method.getMethodName(), method.getJMethod().getDescriptor());
            }
        }


        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // add method in the class metadata
        JMethod method = new JMethod(ACC_PUBLIC, generatedMethodName, "()V", null, null);
        EasyBeansEjbJarMethodMetadata generatedMetadata = new EasyBeansEjbJarMethodMetadata(method, classMetaData);

        // Set value
        switch (interceptorType) {
            case POST_CONSTRUCT:
                generatedMetadata.setPostConstruct(true);
                break;
            case PRE_DESTROY:
                generatedMetadata.setPreDestroy(true);
                break;
            case PRE_PASSIVATE:
                generatedMetadata.setPrePassivate(true);
                break;
            case POST_ACTIVATE:
                generatedMetadata.setPostActivate(true);
                break;
            default:
                    throw new RuntimeException("No generated method name found for interceptorType '" + interceptorType + "'");
        }

        if (classMetaData.isSingleton()) {
            /**
             * The PostConstruct lifecycle callback interceptor methods for
             * singleton beans execute in a transaction context determined by
             * the bean's transaction management type and any applicable
             * transaction attribute.
             */
            /**
             * The PreDestroy lifecycle callback interceptor methods for
             * singleton beans execute in a transaction context determined by
             * the bean's transaction management type and any applicable
             * transaction attribute.
             */

            /**
             * PostConstruct and PreDestroy methods of Singletons with
             * container-managed transactions are transactional. From the bean
             * developer's view there is no client of a PostConstruct or
             * PreDestroy method. A PostConstruct or PreDestroy method of a
             * Singleton with container-managed transactions has transaction
             * attribute REQUIRED, REQUIRES_NEW, or NOT_SUPPORTED (Required ,
             * RequiresNew, or NotSupported if the deployment descriptor is used
             * to specify the transaction attribute). Note that the container
             * must start a new transaction if the REQUIRED (Required)
             * transaction attribute is used. This guarantees, for example, that
             * the transactional behavior of the PostConstruct method is the
             * same regardless of whether it is initialized eagerly at container
             * startup time or as a side effect of a first client invocation on
             * the Singleton. The REQUIRED transaction attribute value is
             * allowed so that specification of a transaction attribute for the
             * Singleton PostConstruct/PreDestroy methods can be defaulted.
             */

            if (POST_CONSTRUCT == interceptorType || PRE_DESTROY == interceptorType) {
                // Is there already some interceptors ?
                if (classMetaData.getPostConstructMethodsMetadata().size() > 0) {
                    generatedMetadata.setInterceptors(classMetaData.getPostConstructMethodsMetadata().getFirst().getInterceptors());
                } else {
                    // Add default transaction on this method
                    TransactionResolver.resolveMethod(classMetaData, generatedMetadata);
                }

            }
        }

        classMetaData.addStandardMethodMetadata(generatedMetadata);
        return generatedMetadata;
    }

    /**
     * Check if this method is the injected method used for dependency injection.
     * @param jMethod object to check
     * @return true if the given method is the injected method used for dependency
     *         injection
     */
    private boolean isDependencyInjectionMethod(final JMethod jMethod) {
        return InjectionClassAdapter.INJECTED_METHOD.equals(jMethod.getName());
    }

    /**
     * Check if this method is injected or not by injection class adapter : No need to add interceptors on these methods.
     * @param jMethod object to check
     * @return true if the given method is injected by injection class adapter.
     */
    private boolean isInjectedMethod(final JMethod jMethod) {
        for (String method : InjectionClassAdapter.INJECTED_METHODS) {
            if (method.equals(jMethod.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param jMethod object to check
     * @return true if the given method is an intercepted method
     * (and business method) because lifecycle methods are not renamed
     */
    private boolean isInterceptedMethod(final JMethod jMethod) {
        // needs to be intercepted
        if (isDependencyInjectionMethod(jMethod)) {
            return this.classAnnotationMetadata.isBean();
        }

        // other injected methods are helper methods (and not business --> no need to intercept them)
        if (isInjectedMethod(jMethod)) {
            return false;
        }

        // get method metadata
        EasyBeansEjbJarMethodMetadata method = this.classAnnotationMetadata.getMethodMetadata(jMethod);
        if (method == null) {
            throw new IllegalStateException("Cannot find a method " + jMethod + " in class "
                    + this.classAnnotationMetadata.getClassName());
        }
        return method.isBusinessMethod();
    }

    /**
     * @param jMethod object to check
     * @return true if the given method has schedule annotation
     */
    private boolean isScheduleMethod(final JMethod jMethod) {

        // get method metadata
        EasyBeansEjbJarMethodMetadata method = this.classAnnotationMetadata.getMethodMetadata(jMethod);
        if (method == null) {
            return false;
        }

        if (method.isTimeout()) {
            return true;
        }

        List<IJEjbSchedule> schedules = method.getJavaxEjbSchedules();
        if (schedules != null) {
            return schedules.size() > 0;
        }
        return false;
    }

    /**
     * @param jMethod object to check
     * @return true if the given method is an interceptor method (ie AroundInvoke, PostConstruct, etc).
     */
    private boolean isInterceptorMethod(final JMethod jMethod) {

        if (isInjectedMethod(jMethod)) {
            return false;
        }
        // get method metadata
        EasyBeansEjbJarMethodMetadata method = this.classAnnotationMetadata.getMethodMetadata(jMethod);
        if (method == null) {
            throw new IllegalStateException("Cannot find a method " + jMethod + " in class "
                    + this.classAnnotationMetadata.getClassName());
        }
        return (method.isAroundInvoke() || method.isLifeCycleMethod());
    }


    /**
     * @return list of classes generated and that need to be defined in a
     *         classloader
     */
    public List<DefinedClass> getDefinedClasses() {
        return this.definedClasses;
    }
}
