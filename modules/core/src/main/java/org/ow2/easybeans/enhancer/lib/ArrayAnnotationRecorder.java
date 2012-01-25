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
 * $Id: ArrayAnnotationRecorder.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import org.ow2.easybeans.asm.AnnotationVisitor;

/**
 * Defines a new array of annotations.
 * @author Florent Benoit
 */
public class ArrayAnnotationRecorder extends AnnotationRecorder {

    /**
     * Build a new recorder for the given name.
     * @param name the given array name
     */
    public ArrayAnnotationRecorder(final String name) {
        super(name);
    }


    /**
     * Replay the value stored on the given annotation visitor.
     * @param annotationVisitor the given visitor on which annotation are replayed.
     */
    @Override
    public void replay(final AnnotationVisitor annotationVisitor) {
        // Build a new array annotation visitor
        AnnotationVisitor av = annotationVisitor.visitArray(getName());
        getLogger().debug("AnnotationVisitor av = methodVisitor.visitArray({0});", getName());

        // Replay
        replayInner(av);

        // End of visit
        av.visitEnd();
        getLogger().debug("av.visitEnd();");
    }
}
