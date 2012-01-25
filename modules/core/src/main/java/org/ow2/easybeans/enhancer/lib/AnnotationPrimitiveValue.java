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
 * $Id: AnnotationPrimitiveValue.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class represents a primitive value of the annotation.
 * @author Florent Benoit
 */
public class AnnotationPrimitiveValue {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(AnnotationPrimitiveValue.class);

    /**
     * Name of the primitive.
     */
    private String name;

    /**
     * Value of the primitive.
     */
    private Object value;


    /**
     * Build an annotation primitive value with given parameters.
     * @param name the given name
     * @param value the value of the primitive
     */
    public AnnotationPrimitiveValue(final String name, final Object value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Add the primitive on the given annotation visitor.
     * @param annotationVisitor the visitor on which generate primitive.
     */
    public void visit(final AnnotationVisitor annotationVisitor) {
        annotationVisitor.visit(this.name, this.value);
        this.logger.debug("annotationVisitor.visit({0}, {1});", this.name, this.value);
    }

}
