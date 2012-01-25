/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: MethodAdapterWithAnnotationRecorder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.easybeans.asm.MethodAdapter;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.enhancer.CommonClassGenerator;

/**
 * Adapter that is storing annotation metadata.
 * @author Florent Benoit
 */
public class MethodAdapterWithAnnotationRecorder extends MethodAdapter {

    /**
     * List of annotation recorders (one for each annotation).
     */
    private List<AnnotationRecorder> annotationRecorders = null;

    /**
     * Map between parameter key and the List of annotation recorders (one for each annotation).
     */
    private List<ParameterAnnotationRecorder> parameterAnnotationRecorders = null;


    /**
     * Build a new method adapter around the given method visitor.
     * @param methodVisitor the given method visitor
     */
    public MethodAdapterWithAnnotationRecorder(final MethodVisitor methodVisitor) {
        super(methodVisitor);
        this.annotationRecorders = new LinkedList<AnnotationRecorder>();
        this.parameterAnnotationRecorders = new LinkedList<ParameterAnnotationRecorder>();
    }

    /**
     * Visits an annotation of this method by storing the metadata.
     *
     * @param desc the class descriptor of the annotation class.
     * @param visible <tt>true</tt> if the annotation is visible at runtime.
     * @return a visitor to visit the annotation values, or <tt>null</tt> if
     *         this visitor is not interested in visiting this annotation.
     */
    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
        AnnotationRecorder annotationRecorder = new AnnotationRecorder(desc, visible);
        // Add it to the list
        this.annotationRecorders.add(annotationRecorder);
        return annotationRecorder;
    }

    /**
     * Starts the visit of the method's code, if any (i.e. non abstract method).
     */
    @Override
    public void visitCode() {
        // Before visiting the code, add stuff to exclude on generated methods
        CommonClassGenerator.addAnnotationsOnGeneratedMethod(this.mv);

        super.visitCode();
    }

    /**
     * Visits an annotation of a parameter this method.
     * @param parameter the parameter index.
     * @param desc the class descriptor of the annotation class.
     * @param visible <tt>true</tt> if the annotation is visible at runtime.
     * @return a visitor to visit the annotation values, or <tt>null</tt> if
     *         this visitor is not interested in visiting this annotation.
     */
    @Override
    public AnnotationVisitor visitParameterAnnotation(final int parameter, final String desc, final boolean visible) {
        // Record the annotations
        ParameterAnnotationRecorder parameterAnnotationRecorder = new ParameterAnnotationRecorder(parameter, desc, visible);

        // Add recorder
        this.parameterAnnotationRecorders.add(parameterAnnotationRecorder);

        // return it
        return parameterAnnotationRecorder;
    }

    /**
     * @return list of annotation recorders.
     */
    public List<AnnotationRecorder> getAnnotationRecorders() {
        return this.annotationRecorders;
    }

    /**
     * @return list of recorders for each parameter.
     */
    public List<ParameterAnnotationRecorder> getParameterAnnotationRecorders() {
        return this.parameterAnnotationRecorders;
    }

}
