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
 * $Id: ClientLifeCycleAdapter.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.client;

import org.ow2.easybeans.asm.ClassAdapter;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;

/**
 * This class manages the lifecycle of the client. It injects a method managing
 * the postConstruct annotation.
 * @author Florent Benoit
 */
public class ClientLifeCycleAdapter extends ClassAdapter implements Opcodes {

    /**
     * Metadata available by this adapter for a class.
     */
    private final EasyBeansEjbJarClassMetadata classAnnotationMetadata;

    /**
     * Defines java.lang.Object class.
     */
    public static final String JAVA_LANG_OBJECT = "java/lang/Object";

    /**
     * Constructor.
     * @param classAnnotationMetadata object containing all attributes of the
     *        class
     * @param cv the class visitor to which this adapter must delegate calls.
     */
    public ClientLifeCycleAdapter(final EasyBeansEjbJarClassMetadata classAnnotationMetadata, final ClassVisitor cv) {
        super(cv);
        this.classAnnotationMetadata = classAnnotationMetadata;
    }

    /**
     * Visits the end of the class. This method, which is the last one to be
     * called, is used to inform the visitor that all the fields and methods of
     * the class have been visited.
     */
    @Override
    public void visitEnd() {
        super.visitEnd();

        // Inject call to all postConstructs annotations
        MethodVisitor mv = this.cv
                .visitMethod(ACC_PUBLIC + ACC_STATIC, "easyBeansLifeCyclePostConstruct", "()V", null, null);
        mv.visitCode();

        // call super method (if any)
        String superNameClass = this.classAnnotationMetadata.getSuperName();
        if (superNameClass != null && !superNameClass.equals(JAVA_LANG_OBJECT)) {
            EasyBeansEjbJarClassMetadata superMetadata = this.classAnnotationMetadata.getEasyBeansLinkedClassMetadata(superNameClass);
            if (superMetadata != null) {
                mv.visitMethodInsn(INVOKESTATIC, superMetadata.getClassName(), "easyBeansLifeCyclePostConstruct", "()V");
            }
        }

        // call each method annotated
        for (EasyBeansEjbJarMethodMetadata method : this.classAnnotationMetadata.getPostConstructMethodsMetadata()) {
            String clName = method.getClassMetadata().getClassName();
            mv.visitMethodInsn(INVOKESTATIC, clName, method.getMethodName(), method.getJMethod().getDescriptor());
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

}
