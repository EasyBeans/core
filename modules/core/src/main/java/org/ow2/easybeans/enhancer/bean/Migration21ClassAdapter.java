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
 * $Id: Migration21ClassAdapter.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.bean;

import org.ow2.easybeans.asm.ClassAdapter;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.Label;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;

/**
 * This adapter is used to add the EJB 2.1 methods that are required for the
 * EJBObject or EJBLocalObject interfaces.
 * @author Florent Benoit
 */
public class Migration21ClassAdapter extends ClassAdapter implements Opcodes {

    /**
     * Metadata available by this adapter for a class.
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata = null;

    /**
     * Remove method has been implemented ?
     */
    private boolean addRemoveMethod = true;

    /**
     * Constant = 1.
     */
    private static final int ONE = 1;

    /**
     * Constant = 2.
     */
    private static final int TWO = 2;

    /**
     * Constant = 3.
     */
    private static final int THREE = 3;

    /**
     * Constant = 4.
     */
    private static final int FOUR = 4;

    /**
     * Constant = 5.
     */
    private static final int FIVE = 5;



    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     */
    public Migration21ClassAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final ClassVisitor cv) {
        super(cv);
        this.classAnnotationMetadata = classAnnotationMetadata;
    }

    /**
     * Visits a method of the class. This method <i>must</i> return a new
     * {@link MethodVisitor} instance (or <tt>null</tt>) each time it is
     * called, i.e., it should not return a previously returned visitor.
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link org.ow2.easybeans.asm.Type Type}).
     * @param signature the method's signature. May be <tt>null</tt> if the
     *        method parameters, return type and exceptions do not use generic
     *        types.
     * @param exceptions the internal names of the method's exception classes
     *        (see {@link org.ow2.easybeans.asm.Type#getInternalName() getInternalName}). May be
     *        <tt>null</tt>.
     * @return an object to visit the byte code of the method, or <tt>null</tt>
     *         if this class visitor is not interested in visiting the code of
     *         this method.
     */
    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, final String signature,
            final String[] exceptions) {

        // Check if remove method is implemented by the user or not ?
        if ("remove".equals(name)) {
            addRemoveMethod = false;
        }

        // go to the default case
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        // generate methods that are required for EJBObject/EJBLocalObject.
        if (addRemoveMethod) {
            addRemoveMethod();
        }

        // Add identical method for both EJBObject and EJBLocalObject
        addIsIdentitalMethods();

        // add getHandle method (which can't be use internally)
        addGetHandleMethod();

        // add the get primary key method which throw exceptions
        addGetPrimaryKey();

        super.visitEnd();

    }

    /**
     * Add en empty remove method. This method has been flagged as a
     * remove/business method. Then, when this method is called, the bean is
     * destroyed.
     */
    private void addRemoveMethod() {
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "remove", "()V", null, new String[] {"javax/ejb/RemoveException" });
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Add the getPrimaryKey() method. It always throw exceptions as it is
     * intented to be used by session beans.
     */
    private void addGetPrimaryKey() {
        // Add the following:
        // public Object getPrimaryKey() {
        // throw new EJBException("No primary key on session beans");
        // }

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getPrimaryKey", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitTypeInsn(NEW, "javax/ejb/EJBException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("No primary key on session beans");
        mv.visitMethodInsn(INVOKESPECIAL, "javax/ejb/EJBException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Add the internal getHandle method. This method do nothing. The proxy
     * client is handling this method.
     */
    private void addGetHandleMethod() {
        // Add the following:
        // public Handle getHandle() throws RemoteException {
        // throw new RemoteException(
        // "This method should be called on the remote object and not locally.
        // It is only available as a client view.");
        // }

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "getHandle", "()Ljavax/ejb/Handle;", null,
                new String[] {"java/rmi/RemoteException"});
        mv.visitCode();
        mv.visitTypeInsn(NEW, "java/rmi/RemoteException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("This method should be called on the remote object and not locally."
                + "It is only available as a client view.");
        mv.visitMethodInsn(INVOKESPECIAL, "java/rmi/RemoteException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * Add the two methods (one fo EJBObject and the other for EJBLocalObject).
     * These methods handle the equality of beans.
     */
    private void addIsIdentitalMethods() {
        addIsIdentitalEJBObject();
        addIsIdentitalEJBLocalObject();
    }

    /**
     * Add the method for EJBLocalObject equality.
     */
    private void addIsIdentitalEJBLocalObject() {
        // add the following code:
        // public boolean isIdentical(EJBLocalObject obj) throws EJBException {
        // // false if other object is null
        // if (obj == null) {
        // return false;
        // }
        //
        // // Gets the handler
        // InvocationHandler handler = Proxy.getInvocationHandler(obj);
        // LocalCallInvocationHandler localHandler = null;
        // if (handler instanceof LocalCallInvocationHandler) {
        // localHandler = (LocalCallInvocationHandler) handler;
        // } else {
        // return false;
        // }
        //
        // // get the other factory name
        // String otherFactoryName = localHandler.getFactoryName();
        //
        // // for stateless, compare only the class
        // if (getEasyBeansFactory() instanceof StatelessSessionFactory) {
        // return getEasyBeansFactory().getClassName()
        // .equals(otherFactoryName);
        // } else if (getEasyBeansFactory() instanceof StatefulSessionFactory) {
        // // compare class and ID
        // Long otherId = localHandler.getBeanId();
        // return getEasyBeansFactory().getClassName()
        // .equals(otherFactoryName)
        // && getEasyBeansStatefulID().equals(otherId);
        // } else {
        // return false;
        // }
        // }


        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "isIdentical", "(Ljavax/ejb/EJBLocalObject;)Z", null,
                new String[] {"javax/ejb/EJBException"});
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, ONE);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, ONE);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Proxy", "getInvocationHandler",
                "(Ljava/lang/Object;)Ljava/lang/reflect/InvocationHandler;");
        mv.visitVarInsn(ASTORE, TWO);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, THREE);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, TWO);
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/rpc/LocalCallInvocationHandler");
        Label l5 = new Label();
        mv.visitJumpInsn(IFEQ, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitVarInsn(ALOAD, TWO);
        mv.visitTypeInsn(CHECKCAST, "org/ow2/easybeans/rpc/LocalCallInvocationHandler");
        mv.visitVarInsn(ASTORE, THREE);
        Label l7 = new Label();
        mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l5);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, THREE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/ow2/easybeans/rpc/LocalCallInvocationHandler", "getFactoryName",
                "()Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, FOUR);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/container/session/stateless/StatelessSessionFactory");
        Label l9 = new Label();
        mv.visitJumpInsn(IFEQ, l9);
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/Factory", "getClassName", "()Ljava/lang/String;");
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitVarInsn(ALOAD, FOUR);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l9);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/container/session/stateful/StatefulSessionFactory");
        Label l13 = new Label();
        mv.visitJumpInsn(IFEQ, l13);
        Label l14 = new Label();
        mv.visitLabel(l14);
        mv.visitVarInsn(ALOAD, THREE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/ow2/easybeans/rpc/LocalCallInvocationHandler", "getBeanId",
                "()Ljava/lang/Long;");
        mv.visitVarInsn(ASTORE, FIVE);
        Label l15 = new Label();
        mv.visitLabel(l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/Factory", "getClassName", "()Ljava/lang/String;");
        Label l16 = new Label();
        mv.visitLabel(l16);
        mv.visitVarInsn(ALOAD, FOUR);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        Label l17 = new Label();
        mv.visitJumpInsn(IFEQ, l17);
        Label l18 = new Label();
        mv.visitLabel(l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansStatefulID", "()Ljava/lang/Long;");
        mv.visitVarInsn(ALOAD, FIVE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "equals", "(Ljava/lang/Object;)Z");
        mv.visitJumpInsn(IFEQ, l17);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l17);
        mv.visitInsn(ICONST_0);
        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l13);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Add the method for EJBObject equality.
     */
    private void addIsIdentitalEJBObject() {
        // code for EJBObject
        // public boolean isIdentical(EJBObject obj) throws RemoteException {
        // // false if other object is null
        // if (obj == null) {
        // return false;
        // }
        //
        // // Gets the handler
        // InvocationHandler handler = Proxy.getInvocationHandler(obj);
        // ClientRPCInvocationHandler clientHandler = null;
        // if (handler instanceof ClientRPCInvocationHandler) {
        // clientHandler = (ClientRPCInvocationHandler) handler;
        // } else {
        // return false;
        // }
        //
        // // get the other factory name
        // String otherFactoryName = clientHandler.getFactoryName();
        //
        // // for stateless, compare only the class
        // if (getEasyBeansFactory() instanceof StatelessSessionFactory) {
        // return getEasyBeansFactory().getClassName()
        // .equals(otherFactoryName);
        // } else if (getEasyBeansFactory() instanceof StatefulSessionFactory) {
        // // compare class and ID
        // Long otherId = clientHandler.getBeanId();
        // return getEasyBeansFactory().getClassName()
        // .equals(otherFactoryName)
        // && getEasyBeansStatefulID().equals(otherId);
        // } else {
        // return false;
        // }
        // }

        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "isIdentical", "(Ljavax/ejb/EJBObject;)Z", null,
                new String[] {"java/rmi/RemoteException"});
        mv.visitCode();
        Label l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        Label l1 = new Label();
        mv.visitJumpInsn(IFNONNULL, l1);
        Label l2 = new Label();
        mv.visitLabel(l2);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/reflect/Proxy", "getInvocationHandler",
                "(Ljava/lang/Object;)Ljava/lang/reflect/InvocationHandler;");
        mv.visitVarInsn(ASTORE, 2);
        Label l3 = new Label();
        mv.visitLabel(l3);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, THREE);
        Label l4 = new Label();
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, TWO);
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/rpc/ClientRPCInvocationHandler");
        Label l5 = new Label();
        mv.visitJumpInsn(IFEQ, l5);
        Label l6 = new Label();
        mv.visitLabel(l6);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitTypeInsn(CHECKCAST, "org/ow2/easybeans/rpc/ClientRPCInvocationHandler");
        mv.visitVarInsn(ASTORE, THREE);
        Label l7 = new Label();
        mv.visitJumpInsn(GOTO, l7);
        mv.visitLabel(l5);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, THREE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/ow2/easybeans/rpc/ClientRPCInvocationHandler", "getFactoryName",
                "()Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, FOUR);
        Label l8 = new Label();
        mv.visitLabel(l8);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/container/session/stateless/StatelessSessionFactory");
        Label l9 = new Label();
        mv.visitJumpInsn(IFEQ, l9);
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/Factory", "getClassName", "()Ljava/lang/String;");
        Label l11 = new Label();
        mv.visitLabel(l11);
        mv.visitVarInsn(ALOAD, FOUR);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        Label l12 = new Label();
        mv.visitLabel(l12);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l9);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitTypeInsn(INSTANCEOF, "org/ow2/easybeans/container/session/stateful/StatefulSessionFactory");
        Label l13 = new Label();
        mv.visitJumpInsn(IFEQ, l13);
        Label l14 = new Label();
        mv.visitLabel(l14);
        mv.visitVarInsn(ALOAD, THREE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "org/ow2/easybeans/rpc/ClientRPCInvocationHandler", "getBeanId",
                "()Ljava/lang/Long;");
        mv.visitVarInsn(ASTORE, FIVE);
        Label l15 = new Label();
        mv.visitLabel(l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitMethodInsn(INVOKEINTERFACE, "org/ow2/easybeans/api/Factory", "getClassName", "()Ljava/lang/String;");
        Label l16 = new Label();
        mv.visitLabel(l16);
        mv.visitVarInsn(ALOAD, FOUR);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "equals", "(Ljava/lang/Object;)Z");
        Label l17 = new Label();
        mv.visitJumpInsn(IFEQ, l17);
        Label l18 = new Label();
        mv.visitLabel(l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, classAnnotationMetadata.getClassName(), "getEasyBeansStatefulID", "()Ljava/lang/Long;");
        mv.visitVarInsn(ALOAD, FIVE);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "equals", "(Ljava/lang/Object;)Z");
        mv.visitJumpInsn(IFEQ, l17);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l17);
        mv.visitInsn(ICONST_0);
        Label l19 = new Label();
        mv.visitLabel(l19);
        mv.visitInsn(IRETURN);
        mv.visitLabel(l13);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

}
