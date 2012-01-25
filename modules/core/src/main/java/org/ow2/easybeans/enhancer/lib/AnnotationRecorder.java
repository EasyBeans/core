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
 * $Id: AnnotationRecorder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import java.util.LinkedList;
import java.util.List;

import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class is storing annotation information and is able to replay the data collected on a given method.
 * @author Florent Benoit
 */
public class AnnotationRecorder implements AnnotationVisitor {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(AnnotationRecorder.class);

    /**
     * Name of this annotation.
     */
    private String name = null;

    /**
     * Desc of this annotation.
     */
    private String desc = null;

    /**
     * Annotation visible or not ?
     */
    private boolean visible = false;

    /**
     * List of primitive values.
     */
    private List<AnnotationPrimitiveValue> primitiveValues = null;

    /**
     * List of enumeration values.
     */
    private List<AnnotationEnumerationValue> enumerationValues = null;

    /**
     * List of nested annotations.
     */
    private List<AnnotationRecorder> nestedValues = null;

    /**
     * List of array of annotations.
     */
    private List<ArrayAnnotationRecorder> arrayValues = null;


    /**
     * Constructor for a given name/visible.
     * @param name the name of the annotation
     * @param visible true if annotation is visible
     */
    public AnnotationRecorder(final String name, final boolean visible) {
        this(name);
        this.visible = visible;
    }

    /**
     * Constructor for a given name/desc.
     * @param name the name of the annotation
     * @param desc the ASM desc value
     */
    public AnnotationRecorder(final String name, final String desc) {
        this(name);
        this.desc = desc;
    }


    /**
     * Default constructor.
     * @param name the given name
     */
    public AnnotationRecorder(final String name) {
        this.name = name;
        this.primitiveValues = new LinkedList<AnnotationPrimitiveValue>();
        this.enumerationValues = new LinkedList<AnnotationEnumerationValue>();
        this.nestedValues = new LinkedList<AnnotationRecorder>();
        this.arrayValues = new LinkedList<ArrayAnnotationRecorder>();
    }


    /**
     * Visits a primitive value of the annotation.
     *
     * @param name the value name.
     * @param value the actual value, whose type must be {@link Byte},
     *        {@link Boolean}, {@link Character}, {@link Short},
     *        {@link Integer}, {@link Long}, {@link Float}, {@link Double},
     *        {@link String} or {@link org.ow2.easybeans.asm.Type}. This value can also be an array
     *        of byte, boolean, short, char, int, long, float or double values
     *        (this is equivalent to using {@link #visitArray visitArray} and
     *        visiting each array element in turn, but is more convenient).
     */
    public void visit(final String name, final Object value) {
        this.primitiveValues.add(new AnnotationPrimitiveValue(name, value));
    }

    /**
     * Visits an enumeration value of the annotation.
     *
     * @param name the value name.
     * @param desc the class descriptor of the enumeration class.
     * @param value the actual enumeration value.
     */
    public void visitEnum(final String name, final String desc, final String value) {
        this.enumerationValues.add(new AnnotationEnumerationValue(name, desc, value));
    }

    /**
     * Visits a nested annotation value of the annotation.
     *
     * @param name the value name.
     * @param desc the class descriptor of the nested annotation class.
     * @return a visitor to visit the actual nested annotation value, or
     *         <tt>null</tt> if this visitor is not interested in visiting
     *         this nested annotation. <i>The nested annotation value must be
     *         fully visited before calling other methods on this annotation
     *         visitor</i>.
     */
    public AnnotationVisitor visitAnnotation(final String name, final String desc) {
        // Add a new recorder for this annotation
        AnnotationRecorder annotationRecorder = new AnnotationRecorder(name, desc);
        this.nestedValues.add(annotationRecorder);
        return annotationRecorder;
    }

    /**
     * Visits an array value of the annotation. Note that arrays of primitive
     * types (such as byte, boolean, short, char, int, long, float or double)
     * can be passed as value to {@link #visit visit}. This is what
     * {@link org.ow2.easybeans.asm.ClassReader} does.
     *
     * @param name the value name.
     * @return a visitor to visit the actual array value elements, or
     *         <tt>null</tt> if this visitor is not interested in visiting
     *         these values. The 'name' parameters passed to the methods of this
     *         visitor are ignored. <i>All the array values must be visited
     *         before calling other methods on this annotation visitor</i>.
     */
    public AnnotationVisitor visitArray(final String name) {
        ArrayAnnotationRecorder arrayAnnotationRecorder = new ArrayAnnotationRecorder(name);
        this.arrayValues.add(arrayAnnotationRecorder);
        return arrayAnnotationRecorder;
    }

    /**
     * Visits the end of the annotation.
     */
    public void visitEnd() {
        // do nothing
    }

    /**
     * Replay the value stored on the given method visitor.
     * @param methodVisitor the given visitor on which annotation are replayed.
     */
    public void replay(final MethodVisitor methodVisitor) {
        // Build a new annotation visitor for the given method
        AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation(this.name, this.visible);
        this.logger.debug("AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation({0}, {1});", this.name,
                Boolean.valueOf(this.visible));

        // Replay
        replayInner(annotationVisitor);

        // End of visit
        annotationVisitor.visitEnd();
        this.logger.debug("annotationVisitor.visitEnd();");
    }

    /**
     * Replay the value stored on the given annotation visitor.
     * @param annotationVisitor the given visitor on which annotation are replayed.
     */
    public void replay(final AnnotationVisitor annotationVisitor) {
        // Build a new inner annotation visitor
        AnnotationVisitor av = annotationVisitor.visitAnnotation(this.name, this.desc);
        this.logger.debug("AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation({0}, {1});", this.name, this.desc);

        // Replay
        replayInner(av);

        // End of visit
        av.visitEnd();
        this.logger.debug("annotationVisitor.visitEnd()");
    }

    /**
     * Common stuff shared by annotationvisitor/methodvisitor replay code.
     * @param annotationVisitor the given visited annotation visitor.
     */
    protected void replayInner(final AnnotationVisitor annotationVisitor) {
        // primitive values
        for (AnnotationPrimitiveValue primitiveValue : this.primitiveValues) {
            primitiveValue.visit(annotationVisitor);
        }

        // Enum values
        for (AnnotationEnumerationValue enumerationValue : this.enumerationValues) {
            enumerationValue.visit(annotationVisitor);
        }

        // Nested values
        for (AnnotationRecorder nestedAnnotation : this.nestedValues) {
            nestedAnnotation.replay(annotationVisitor);
        }

        // Array values
        for (ArrayAnnotationRecorder arrayAnnotationRecorder : this.arrayValues) {
            arrayAnnotationRecorder.replay(annotationVisitor);
        }
    }

    /**
     * @return the given name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return the visible status
     */
    public boolean getVisible() {
        return this.visible;
    }

    /**
     * @return logger.
     */
    public Log getLogger() {
        return this.logger;
    }
}
