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
 * $Id: AnnotationEnumerationValue.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.lib;

import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class store an enum value for an annotation.
 * @author Florent Benoit
 */
public class AnnotationEnumerationValue {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(AnnotationEnumerationValue.class);

    /**
     * Name of the enum.
     */
    private String name;

    /**
     * Desc of the enum.
     */
    private String desc;

    /**
     * Value of the enum.
     */
    private String value;

    /**
     * Build an annotation enum value with given parameters.
     * @param name the given name
     * @param desc the given descriptor
     * @param value the value of the enum
     */
    public AnnotationEnumerationValue(final String name, final String desc, final String value) {
        this.name = name;
        this.desc = desc;
        this.value = value;
    }

    /**
     * Add the enum on the given annotation visitor.
     * @param annotationVisitor the visitor on which generate enum.
     */
    public void visit(final AnnotationVisitor annotationVisitor) {
        annotationVisitor.visitEnum(this.name, this.desc, this.value);
        this.logger.debug("annotationVisitor.visitEnum({0}, {1}, {2});", this.name, this.desc, this.value);
    }

}
