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
 * $Id: BeanClassAdapter.java 5645 2010-10-26 07:04:30Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.bean;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.xa.XAResource;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.bean.EasyBeansBean;
import org.ow2.easybeans.api.bean.EasyBeansMDB;
import org.ow2.easybeans.api.bean.EasyBeansManagedBean;
import org.ow2.easybeans.api.bean.EasyBeansSFSB;
import org.ow2.easybeans.api.bean.EasyBeansSLSB;
import org.ow2.easybeans.api.bean.EasyBeansSingletonSB;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.api.interceptor.EZBInterceptorManager;
import org.ow2.easybeans.api.interceptor.EZBInvocationContextFactory;
import org.ow2.easybeans.asm.ClassAdapter;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.MethodAdapter;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.DefinedClass;
import org.ow2.easybeans.enhancer.interceptors.EasyBeansInvocationContextGenerator;
import org.ow2.easybeans.enhancer.lib.MethodRenamer;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class adds a bean interface to the parsed object.<br>
 * It also adds getEasyBeansFactory method defined in the EasyBeansBean interface.<br>
 * Stateless bean will have EasyBeansStatelessSessionBean interface, etc.
 * @author Florent Benoit
 */
public class BeanClassAdapter extends ClassAdapter implements Opcodes {

    /**
     * Logger.
     */
    private static final Log LOGGER = LogFactory.getLog(BeanClassAdapter.class);

    /**
     * Metadata available by this adapter for a class.
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata = null;

    /**
     * Timer method name.
     */
    public static final String TIMER_METHOD = "timeoutCallByEasyBeans";

    /**
     * cleanup method.
     */
    public static final String CLEANUP_METHOD = "easyBeansCleanup";

    /**
     * JMethod object for timeoutCallByEasyBeans.
     */
    public static final JMethod TIMER_JMETHOD = new JMethod(ACC_PUBLIC, MethodRenamer.encode(TIMER_METHOD),
            "(Ljavax/ejb/Timer;)V", null, null);

    /**
     * JMethod object for timeoutCallByEasyBeans.
     */
    public static final JMethod TIMER_JMETHOD_NOARG = new JMethod(ACC_PUBLIC, MethodRenamer.encode(TIMER_METHOD),
            "()V", null, null);

    /**
     * Mappping between className and the bytecode.
     */
    private List<DefinedClass> definedClasses = null;

    /**
     * Classloader used to load classes.
     */
    private ClassLoader readLoader = null;

    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     * @param readLoader the classloader used to read classes
     */
    public BeanClassAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final ClassVisitor cv,
            final ClassLoader readLoader) {
        super(cv);
        this.classAnnotationMetadata = classAnnotationMetadata;
        this.readLoader = readLoader;
        this.definedClasses = new ArrayList<DefinedClass>();
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
    public void visit(final int version, final int access, final String name, final String signature,
            final String superName, final String[] interfaces) {

        String[] newInterfaces = null;

        // Add new interface with bean
        if (this.classAnnotationMetadata.isBean()) {
            // copy old interfaces in the new array
            newInterfaces = new String[interfaces.length + 1];
            System.arraycopy(interfaces, 0, newInterfaces, 0, interfaces.length);

            int indexElement = newInterfaces.length - 1;

            // Add the right interface (SLSB, SFSB, MDB)
            if (this.classAnnotationMetadata.isStateless()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansSLSB.class);
            } else if (this.classAnnotationMetadata.isStateful()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansSFSB.class);
            } else if (this.classAnnotationMetadata.isMdb()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansMDB.class);
            } else if (this.classAnnotationMetadata.isSingleton()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansSingletonSB.class);
            } else if (this.classAnnotationMetadata.isManagedBean()) {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansManagedBean.class);
            } else {
                newInterfaces[indexElement] = Type.getInternalName(EasyBeansBean.class);
            }
        } else {
            newInterfaces = interfaces;
        }

        super.visit(version, access, name, signature, superName, newInterfaces);

    }
    /**
     * Visits a method of the class. This method <i>must</i> return a new
     * {@link MethodVisitor} instance (or <tt>null</tt>) each time it is
     * called, i.e., it should not return a previously returned visitor.
     *
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type Type}).
     * @param signature the method's signature. May be <tt>null</tt> if the
     *        method parameters, return type and exceptions do not use generic
     *        types.
     * @param exceptions the internal names of the method's exception classes
     *        (see {@link Type#getInternalName() getInternalName}). May be
     *        <tt>null</tt>.
     * @return an object to visit the byte code of the method, or <tt>null</tt>
     *         if this class visitor is not interested in visiting the code of
     *         this method.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {

        // if default constructor, then needs to add a call to create a new InterceptorManager
        if ("<init>".equals(name) && "()V".equals(desc)) {
            MethodVisitor mv =  super.visitMethod(access, name, desc, signature, exceptions);
            return new AddMethodConstructorAdapter(mv);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);

    }


    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        super.visitEnd();

        // Adds the factory attribute and its getter/setter.
        CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                "easyBeansFactory", Factory.class);


        if (this.classAnnotationMetadata.isBean()) {
            // Adds interceptor manager
            CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                    "easyBeansInterceptorManager", "L" + this.classAnnotationMetadata.getClassName()
                            + EasyBeansInvocationContextGenerator.SUFFIX_INTERCEPTOR_MANAGER + ";");

            // Adds invocationcontext factory
            CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                    "easyBeansInvocationContextFactory", EZBInvocationContextFactory.class);

            // Adds dynamic interceptor manager
            CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                    "easyBeansDynamicInterceptorManager", EZBInterceptorManager.class);

            // Adds the context attribute and its getter/setter.
            CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                    "easyBeansContext", EZBEJBContext.class);

            // Add the removed attribute (if bean has been removed)
            if (this.classAnnotationMetadata.isSession()) {
                CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                        "easyBeansRemoved", Boolean.TYPE);
            }

            // Add id field for stateful
            if (this.classAnnotationMetadata.isStateful()) {
                CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                        "easyBeansStatefulID", Long.class);
                CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                        "inTransaction", Boolean.class);
                CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                        "statefulTimeout", Long.class);
            }

            // Add XAResource field for MDB
            if (this.classAnnotationMetadata.isMdb()) {
                CommonClassGenerator.addFieldGettersSetters(this.cv, this.classAnnotationMetadata.getClassName(),
                        "xaResource", XAResource.class);
            }

            // Add the timer method
            addTimerMethod(this.cv);

            // Add the cleanup method
            addCleanupMethod(this.cv);

            // Needs to add class for Proxy on the beans
            NoInterfaceViewClassGenerator noInterfaceViewClassGenerator = new NoInterfaceViewClassGenerator(
                    this.classAnnotationMetadata, this.readLoader);
            noInterfaceViewClassGenerator.generate();

            DefinedClass dc = new DefinedClass(noInterfaceViewClassGenerator.getGeneratedClassName().replace("/", "."),
                    noInterfaceViewClassGenerator.getBytes());
            // this class will be defined later on the classloader
            this.definedClasses.add(dc);

        }

    }

    /**
     * This method will remove any reference of this bean to the factories/manager/etc sets by EasyBeans. <br />
     * @param cv the class visitor.
     */
    private void addCleanupMethod(final ClassVisitor cv) {

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, CLEANUP_METHOD, "()V", null, null);
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);
        mv.visitCode();

        // Reset the factory attribute
        CommonClassGenerator.nullifyField(mv, this.classAnnotationMetadata.getClassName(), "easyBeansFactory", Factory.class);

        // Reset interceptor manager
        CommonClassGenerator.nullifyField(mv, this.classAnnotationMetadata.getClassName(), "easyBeansInterceptorManager", "L"
                + this.classAnnotationMetadata.getClassName() + EasyBeansInvocationContextGenerator.SUFFIX_INTERCEPTOR_MANAGER
                + ";");

        // Reset invocationcontext factory
        CommonClassGenerator.nullifyField(mv, this.classAnnotationMetadata.getClassName(), "easyBeansInvocationContextFactory",
                EZBInvocationContextFactory.class);

        // Reset dynamic interceptor manager
        CommonClassGenerator.nullifyField(mv, this.classAnnotationMetadata.getClassName(),
                "easyBeansDynamicInterceptorManager", EZBInterceptorManager.class);

        // Reset the context attribute and its getter/setter.
        CommonClassGenerator.nullifyField(mv, this.classAnnotationMetadata.getClassName(), "easyBeansContext",
                EZBEJBContext.class);

        // End of the method
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * This method will call the timeout method (if any). <br />
     * The timeout method may be defined on the super class.
     * If there is no timeout method defined on the bean, throw an exception
     * @param cv the class visitor.
     */
    private void addTimerMethod(final ClassVisitor cv) {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, MethodRenamer.encode(TIMER_METHOD), "(Ljavax/ejb/Timer;)V", null, null);
        // Add some flags on the generated method
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(mv);
        mv.visitCode();

        // Found a timer method ?
        boolean found = false;

        // get timer method if any and do a call on this timer method
        for (EasyBeansEjbJarMethodMetadata method : this.classAnnotationMetadata.getMethodMetadataCollection()) {
            if (method.isTimeout()) {
                // Write a call to this method
                mv.visitVarInsn(ALOAD, 0);

                // Timer argument (not a void method)
                if (!"()V".equals(method.getJMethod().getDescriptor())) {
                    mv.visitVarInsn(ALOAD, 1);
                }

                // The name of the class where the method is defined (can be a super class)
                String className = this.classAnnotationMetadata.getClassName();
                if (method.isInherited()) {
                    className = method.getOriginalClassMetadata().getClassName();
                }

                mv.visitMethodInsn(INVOKESPECIAL, className, method.getMethodName(), method.getJMethod().getDescriptor());
                found = true;
                break;
            }
        }

        // No timeout method, then needs to throw an exception
        // throw new EJBException("No timeout method has been defined on this bean");
        if (!found) {
            mv.visitTypeInsn(NEW, "javax/ejb/EJBException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("No timeout method has been defined on this bean");
            mv.visitMethodInsn(INVOKESPECIAL, "javax/ejb/EJBException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
        }

        // else, throw an exception

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Adds an entry in the constructor of the bean.
     * It will initialize the interceptorManager.
     * @author Florent Benoit
     */
    public class AddMethodConstructorAdapter extends MethodAdapter {

        /**
         * Constructs a new AddMethodConstructorAdapter object.
         * @param mv the code visitor to which this adapter must delegate calls.
         */
        public AddMethodConstructorAdapter(final MethodVisitor mv) {
            super(mv);
        }

        /**
         * Adds instruction just after the start of the method code.
         * TODO: Analyze when call to super() constructor is done and add instruction after.
         */
        @Override
        public void visitCode() {
            super.visitCode();
            String clManager = BeanClassAdapter.this.classAnnotationMetadata.getClassName()
            + EasyBeansInvocationContextGenerator.SUFFIX_INTERCEPTOR_MANAGER;
            this.mv.visitVarInsn(ALOAD, 0);
            this.mv.visitTypeInsn(NEW, clManager);
            this.mv.visitInsn(DUP);
            this.mv.visitMethodInsn(INVOKESPECIAL, clManager, "<init>", "()V");
            this.mv.visitFieldInsn(PUTFIELD, BeanClassAdapter.this.classAnnotationMetadata.getClassName(),
                    "easyBeansInterceptorManager", "L" + clManager + ";");

        }

    }

    /**
     * @return list of classes generated and that need to be defined in a
     *         classloader
     */
    public List<DefinedClass> getDefinedClasses() {
        return this.definedClasses;
    }
}

