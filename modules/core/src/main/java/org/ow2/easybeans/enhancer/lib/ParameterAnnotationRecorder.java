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
 * $Id: ParameterAnnotationRecorder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.easybeans.asm.MethodVisitor;

/**
 * This class is used to record annotations of parameters.
 * @author Florent Benoit
 */
public class ParameterAnnotationRecorder extends AnnotationRecorder {

    /**
     * Index of the parameter.
     */
    private int index;

    /**
     * Build a recorder for the given index parameter with the given annotation name and the visible mode.
     * @param index the index of the parameter [0--> args.length]
     * @param name the name the annotation
     * @param visible true if retention is runtime
     */

    public ParameterAnnotationRecorder(final int index, final String name, final boolean visible) {
        super(name, visible);
        this.index = index;
    }

    /**
     * Replay the value stored on the given method visitor.
     * @param methodVisitor the given visitor on which annotation are replayed.
     */
    @Override
    public void replay(final MethodVisitor methodVisitor) {
        // Build a new annotation visitor for the given method
        AnnotationVisitor annotationVisitor = methodVisitor.visitParameterAnnotation(this.index, getName(), getVisible());
        getLogger().debug("AnnotationVisitor annotationVisitor = methodVisitor.visitParameterAnnotation({0}, {1}, {2});",
                Integer.valueOf(this.index), getName(), Boolean.valueOf(getVisible()));

        // Replay
        replayInner(annotationVisitor);

        // End of visit
        annotationVisitor.visitEnd();
        getLogger().debug("annotationVisitor.visitEnd();");
    }


}
