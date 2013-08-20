/**
 * EasyBeans
 * Copyright 2013 Peergreen S.A.S.
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
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.configurator.visitor.clazz;

import static java.lang.annotation.ElementType.TYPE;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.pool.impl.PoolConfiguration;
import org.ow2.util.scan.api.IAnnotationVisitorContext;
import org.ow2.util.scan.api.IType;
import org.ow2.util.scan.api.annotation.VisitorTarget;
import org.ow2.util.scan.api.annotation.VisitorType;
import org.ow2.util.scan.api.visitor.DefaultAnnotationVisitor;

/**
 * This class manages the handling of &#64;{@link org.ow2.util.pool.annotation.Pool}
 * annotation.
 * @author Florent Benoit
 */
@VisitorTarget(TYPE)
@VisitorType("org.ow2.util.pool.annotation.Pool")
public class PoolVisitor extends DefaultAnnotationVisitor<IPoolConfiguration> {
    /**
     * Min attribute of the annotation.
     */
    private static final String MIN = "min";

    /**
     * Spare attribute of the annotation.
     */
    private static final String SPARE = "spare";

    /**
     * Max attribute of the annotation.
     */
    private static final String MAX = "max";

    /**
     * Timeout attribute of the annotation.
     */
    private static final String TIMEOUT = "timeout";

    /**
     * MaxWaiters attribute of the annotation.
     */
    private static final String MAX_WAITERS = "maxwaiters";

    /**
     * Visits a primitive value of the annotation.<br>
     * @param name the value name.
     * @param value the actual value, whose type must be {@link Byte},
     *        {@link Boolean}, {@link Character}, {@link Short},
     *        {@link Integer}, {@link Long}, {@link Float}, {@link Double},
     *        {@link String} or {@link IType}.
     */
    @Override
    public void visit(final String name, final Object value, IAnnotationVisitorContext<IPoolConfiguration> annotationVisitorContext) {
        IPoolConfiguration poolConfiguration = annotationVisitorContext.getLocal();
        if (MAX.equals(name)) {
            poolConfiguration.setMax(((Integer) value).intValue());
        } else if (MIN.equals(name)) {
            poolConfiguration.setMin(((Integer) value).intValue());
        } else if (SPARE.equals(name)) {
            poolConfiguration.setSpare(((Integer) value).intValue());
        } else if (TIMEOUT.equals(name)) {
            poolConfiguration.setTimeout(((Long) value).longValue());
        } else if (MAX_WAITERS.equals(name)) {
            poolConfiguration.setMaxWaiters(((Integer) value).intValue());
        }
    }

    /**
     * Visits the end of the annotation. <br> Creates the object and store it.
     */
    @Override
    public void visitEnd(IAnnotationVisitorContext<IPoolConfiguration> annotationVisitorContext) {
        EasyBeansEjbJarClassMetadata view = annotationVisitorContext.getView(EasyBeansEjbJarClassMetadata.class);
        view.setPoolConfiguration(annotationVisitorContext.getLocal());
    }

    @Override
    public IPoolConfiguration buildInstance() {
        return new PoolConfiguration();
    }

}
