/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: GenerateClass.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.test;

import org.ow2.easybeans.asm.ClassWriter;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;

/**
 * Allow to generate dynamically a class.
 * @author Florent BENOIT
 */
public final class GenerateClass implements Opcodes {

    /**
     * Utility class, no public constructor.
     */
    private GenerateClass() {

    }

    /**
     * Gets the bytecode for the given generated classname and the content of
     * the hello() method.
     * @param className the name of the class to generate
     * @param helloWorldString the content that hello() method will return
     * @return the bytecode
     * @throws Exception if bytecode can't be built
     */
    public static byte[] getByteForClass(final String className, final String helloWorldString) throws Exception {

        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object", null);

        // Constructor
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        // hello method
        mv = cw.visitMethod(ACC_PUBLIC, "hello", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(helloWorldString);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

}
