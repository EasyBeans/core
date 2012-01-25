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
 * $Id: JCheckClassAdapter.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer;

import java.util.List;

import org.ow2.easybeans.asm.ClassReader;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.asm.tree.ClassNode;
import org.ow2.easybeans.asm.tree.MethodNode;
import org.ow2.easybeans.asm.tree.analysis.Analyzer;
import org.ow2.easybeans.asm.tree.analysis.AnalyzerException;
import org.ow2.easybeans.asm.tree.analysis.Frame;
import org.ow2.easybeans.asm.tree.analysis.SimpleVerifier;
import org.ow2.easybeans.asm.util.CheckClassAdapter;
import org.ow2.easybeans.asm.util.TraceMethodVisitor;

/**
 * Checks a class.
 * @author Florent Benoit
 */
public class JCheckClassAdapter extends CheckClassAdapter {

    /**
     * Start of int (debug traces).
     */
    private static final int START_INT = 100000;

    /**
     * Constructs a new JCheckClassAdapter.
     * @param cv the class visitor to which this adapter must delegate calls.
     */
    public JCheckClassAdapter(final ClassVisitor cv) {
        super(cv);
    }

    /**
     * Check a set of bytes.
     * @param bytes representing a class.
     * @throws AnalyzerException if class is incorrect.
     */
    public static void checkClass(final byte[] bytes) throws AnalyzerException {
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(new CheckClassAdapter(cn), ClassReader.SKIP_DEBUG);
        List methods = cn.methods;
        for (int i = 0; i < methods.size(); ++i) {
            MethodNode method = (MethodNode) methods.get(i);
            if (method.instructions.size() > 0) {
                Analyzer a = new Analyzer(new SimpleVerifier(Type.getType("L" + cn.name + ";"), Type.getType("L" + cn.superName
                        + ";"), (cn.access & Opcodes.ACC_INTERFACE) != 0));
                AnalyzerException throwE = null;
                try {
                    a.analyze(cn.name, method);
                    continue;
                } catch (AnalyzerException e) {
                    throwE = e;
                }
                final Frame[] frames = a.getFrames();

                if (throwE != null) {
                    System.out.println(method.name + method.desc);

                    TraceMethodVisitor mv = new TraceMethodVisitor() {

                        @Override
                        public void visitMaxs(final int maxStack, final int maxLocals) {
                            for (int i = 0; i < text.size(); ++i) {
                                String s;
                                if (frames[i] == null) {
                                        s = "null";
                                } else {
                                    s = frames[i].toString();
                                }
                                while (s.length() < maxStack + maxLocals + 1) {
                                    s += " ";
                                }
                                System.out.print(Integer.toString(i + START_INT).substring(1));
                                System.out.print(" " + s + " : " + text.get(i));
                            }
                            System.out.println();
                        }
                    };
                    for (int j = 0; j < method.instructions.size(); ++j) {
                        method.instructions.get(j).accept(mv);
                    }
                    mv.visitMaxs(method.maxStack, method.maxLocals);
                    throw throwE;
                }
            }
        }
    }

}
