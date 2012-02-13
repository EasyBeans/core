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

package org.ow2.easybeans.enhancer.bean;

import java.lang.reflect.InvocationHandler;
import java.util.Collection;

import org.ow2.easybeans.api.bean.proxy.EasyBeansNoInterfaceProxyBean;
import org.ow2.easybeans.asm.Label;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.EasyBeansClassWriter;
import org.ow2.easybeans.enhancer.lib.ProxyClassEncoder;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Adapter used to generate a new class based on a super class.
 * @author Florent Benoit
 */
public class NoInterfaceViewClassGenerator extends CommonClassGenerator {

    /**
     * InvocationHelper class.
     */
    private static final String INVOCATION_HELPER_CLASSNAME = Type.getInternalName(InvocationHelper.class);

    /**
     * The classmetadata.
     */
    private EasyBeansEjbJarClassMetadata classMetadata = null;

    /**
     * Name of the class to generate.
     */
    private String generatedClassName = null;

    /**
     * Default constructor for the given classmetadata.
     * @param classMetadata the given classmetadata
     * @param readLoader the classloader used to load classes.
     */
    public NoInterfaceViewClassGenerator(final EasyBeansEjbJarClassMetadata classMetadata, final ClassLoader readLoader) {
        super(new EasyBeansClassWriter(readLoader));
        this.classMetadata = classMetadata;

        this.generatedClassName = ProxyClassEncoder.getProxyClassName(classMetadata.getClassName());
    }

    /**
     * Generates the class. It call sub methods for being more clear for read
     * the code
     */
    public void generate() {
        addClassDeclaration();
        addAttributes();
        addConstructor();
        addMethods();
        endClass();
    }

    /**
     * Add attributes of the class in two steps.
     * <ul>
     * <li>InvocationHandler interface</li>
     * </ul>
     */
    private void addAttributes() {
        addInvocationHandlerAttributes();
    }

    /**
     * Add attribute InvocationHandler + getter/setter.
     */
    private void addInvocationHandlerAttributes() {
        CommonClassGenerator.addFieldGettersSetters(getCW(), this.generatedClassName, "invocationHandler",
                InvocationHandler.class);
    }

    /**
     * Creates the declaration of the class with the given interfaces.
     */
    private void addClassDeclaration() {

        // Add an interface
        String interfaceName = Type.getInternalName(EasyBeansNoInterfaceProxyBean.class);

        // create class
        getCW().visit(V1_5, ACC_PUBLIC + ACC_SUPER, this.generatedClassName, null, this.classMetadata.getClassName(),
                new String[] {interfaceName});
    }

    /**
     * Creates the constructor which should look like.
     *
     * <pre>
     * public XXXBean() {
     *     super();
     * }
     * </pre>
     */
    private void addConstructor() {
        // Generate constructor
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        // Call super constructor
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, this.classMetadata.getClassName(), "<init>", "()V");

        // need to add return instruction
        mv.visitInsn(RETURN);

        // visit max compute automatically
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Generate two kinds of methods : Call for public methods Call for not
     * public methods.
     */
    private void addMethods() {

        Collection<EasyBeansEjbJarMethodMetadata> methodsMetadata = this.classMetadata.getMethodMetadataCollection();
        if (methodsMetadata != null) {
            for (EasyBeansEjbJarMethodMetadata methodMetadata : methodsMetadata) {
                JMethod jMethod = methodMetadata.getJMethod();

                // Skip special methods
                if ("<init>".equals(jMethod.getName())) {
                    continue;
                }

                // Method is not public and not private
                if ((jMethod.getAccess() & ACC_PUBLIC) != ACC_PUBLIC && (jMethod.getAccess() & ACC_PRIVATE) != ACC_PRIVATE) {
                    addThrowsExceptionMethod(jMethod);
                } else if ((jMethod.getAccess() & ACC_PUBLIC) == ACC_PUBLIC) {
                    // Method is public, generate a method which calls the invocation handler
                    // the same method name but with a different content
                    addTransformedMethod(methodMetadata);
                }

            }
        }
    }

    /**
     * Each public method should call the invocation handler.
     * <pre>
     * public void methodWithException() throws MyException {
     *     Method m = Helper.getMethod(superClass.class, methodName, new Class[] {});
     *     try {
     *         Helper.invoke(this, m, handler, new Object[] {});
     *     } catch (Throwable originalThrowable) {
     *         if (originalThrowable instanceof MyException) {
     *             throw (MyException) originalThrowable;
     *         } else {
     *             if (originalThrowable instanceof RuntimeException) {
                       throw (RuntimeException) originalThrowable;
                   }
     *             if (originalThrowable instanceof Exception) {
     *                 throw new EJBException("illegal", (Exception) originalThrowable);
     *             }
     *             throw new EJBException("error", new RuntimeException(originalThrowable));
     *         }
     *     }
     * }
     * </pre>
     * @param methodMetadata the metadata used to generate the call
     */
    private void addTransformedMethod(final EasyBeansEjbJarMethodMetadata methodMetadata) {
        JMethod jMethod = methodMetadata.getJMethod();
        MethodVisitor mv = getCW().visitMethod(jMethod.getAccess(), jMethod.getName(), jMethod.getDescriptor(),
                jMethod.getSignature(), jMethod.getExceptions());
        mv.visitCode();

        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Throwable");

        // Get method of the super class
        mv.visitLdcInsn(Type.getType("L".concat(methodMetadata.getClassMetadata().getClassName()).concat(";")));
        mv.visitLdcInsn(jMethod.getName());


        // Arguments of the method
        // parameters = new Class[] {arg0, arg1, arg...};
        // put size of the array
        Type[] args = Type.getArgumentTypes(jMethod.getDescriptor());
        int methodArg = 1;

        putConstNumber(args.length, mv);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");

        // for each argument of the methods, load the class parameter
        int argCount = 0;
        for (Type type : args) {
            mv.visitInsn(DUP);
            putConstNumber(argCount, mv);
            visitClassType(type, mv);
            mv.visitInsn(AASTORE);

            int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
            // Double and Long are special parameters
            if (opCode == LLOAD || opCode == DLOAD) {
                methodArg++;
            }
            methodArg++;
            argCount++;
        }

        mv.visitMethodInsn(INVOKESTATIC, INVOCATION_HELPER_CLASSNAME, "getMethod",
                "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");
        mv.visitVarInsn(ASTORE, methodArg);

        // Begin try
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, this.generatedClassName, "getInvocationHandler",
                "()Ljava/lang/reflect/InvocationHandler;");

        // Give args for the call of the invoke method
        putConstNumber(args.length, mv);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

        // for each argument of the methods :
        argCount = 0;
        int localArg = 1;
        for (Type type : args) {
            mv.visitInsn(DUP);
            putConstNumber(argCount, mv);

            int opCode = CommonClassGenerator.putFieldLoadOpCode(type.getSort());
            mv.visitVarInsn(opCode, localArg);
            // Double and Long are special parameters
            if (opCode == LLOAD || opCode == DLOAD) {
                localArg++;
            }
            localArg++;

            // if type is not object type, need to convert it
            // for example : Integer.valueOf(i);
            CommonClassGenerator.transformPrimitiveIntoObject(type, mv);
            mv.visitInsn(AASTORE);
            argCount++;
        }

        // Invoke
        mv
                .visitMethodInsn(INVOKESTATIC, INVOCATION_HELPER_CLASSNAME, "invoke",
                        "(Ljava/lang/Object;Ljava/lang/reflect/Method;Ljava/lang/reflect/InvocationHandler;[Ljava/lang/Object;)Ljava/lang/Object;");

        Type returnType = Type.getReturnType(jMethod.getDescriptor());
        // Cast and return value
        CommonClassGenerator.transformObjectIntoPrimitive(returnType, mv);
        mv.visitLabel(l1);
        CommonClassGenerator.addReturnType(returnType, mv);
        mv.visitLabel(l2);

        methodArg++;

        mv.visitVarInsn(ASTORE, methodArg);

        // Exceptions to catch/rethrow ?
        String[] methodExceptions = jMethod.getExceptions();
        if (methodExceptions != null) {
            // ifLabels = exceptions thrown by method + 1
            Label[] ifLabels = new Label[methodExceptions.length + 1];

            // init labels
            for (int i = 0; i < ifLabels.length; i++) {
                ifLabels[i] = new Label();
            }

            for (int ifBlock = 0; ifBlock < methodExceptions.length; ifBlock++) {
                mv.visitLabel(ifLabels[ifBlock]);
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitTypeInsn(INSTANCEOF, methodExceptions[ifBlock]);
                mv.visitJumpInsn(IFEQ, ifLabels[ifBlock + 1]);
                mv.visitVarInsn(ALOAD, methodArg);
                mv.visitTypeInsn(CHECKCAST, methodExceptions[ifBlock]);
                mv.visitInsn(ATHROW);
            }
            mv.visitLabel(ifLabels[methodExceptions.length]);
        }

        // Check if throwable is exception or not
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/RuntimeException");
        Label notRuntimeException = new Label();
        mv.visitJumpInsn(IFEQ, notRuntimeException);
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitTypeInsn(CHECKCAST, "java/lang/RuntimeException");
        mv.visitInsn(ATHROW);
        mv.visitLabel(notRuntimeException);
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/Exception");
        Label notException = new Label();
        mv.visitJumpInsn(IFEQ, notException);
        mv.visitTypeInsn(NEW, "javax/ejb/EJBException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Unable to invoke method");
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitTypeInsn(CHECKCAST, "java/lang/Exception");
        mv.visitMethodInsn(INVOKESPECIAL, "javax/ejb/EJBException", "<init>", "(Ljava/lang/String;Ljava/lang/Exception;)V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(notException);
        mv.visitTypeInsn(NEW, "javax/ejb/EJBException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Unable to invoke method");
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, methodArg);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/Throwable;)V");
        mv.visitMethodInsn(INVOKESPECIAL, "javax/ejb/EJBException", "<init>", "(Ljava/lang/String;Ljava/lang/Exception;)V");
        mv.visitInsn(ATHROW);


        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Each method which is not public (and private) should throw a
     * javax.ejb.EJBException.
     *
     * <pre>
     * public void methodName() {
     *     throw new EJBException(&quot;This method is not a public method. Access is not allowed&quot;);
     * }
     * </pre>
     * @param jMethod the method used to add the throws
     */
    private void addThrowsExceptionMethod(final JMethod jMethod) {
        // Create the method which throw the exception with the same
        // signature
        MethodVisitor mv = getCW().visitMethod(jMethod.getAccess(), jMethod.getName(), jMethod.getDescriptor(),
                jMethod.getSignature(), jMethod.getExceptions());
        mv.visitCode();

        mv.visitTypeInsn(NEW, "javax/ejb/EJBException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("This method is not a public method. Access is not allowed");
        mv.visitMethodInsn(INVOKESPECIAL, "javax/ejb/EJBException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * Called when the generated class is done.
     */
    private void endClass() {
        getCW().visitEnd();
    }

    /**
     * @return the bytecode of the generated class.
     */
    public byte[] getBytes() {
        return getCW().toByteArray();
    }

    /**
     * @return the name of the generated class name (with package name)
     */
    public String getGeneratedClassName() {
        return this.generatedClassName;
    }

}
