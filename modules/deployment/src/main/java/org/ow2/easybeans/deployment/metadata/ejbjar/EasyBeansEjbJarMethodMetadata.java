/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: EasyBeansEjbJarMethodMetadata.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;

import java.util.List;

import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * This class represents the annotation metadata of a method.
 * @author Florent Benoit
 */
public class EasyBeansEjbJarMethodMetadata
        extends
        EjbJarMethodMetadata<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata>
        implements Cloneable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -1491050227163436797L;

    /**
     * This method is a method from a super class ?<br>
     */
    private boolean inherited = false;

    /**
     * This method is a method that should be ignored.
     */
    private boolean ignored = false;

    /**
     * Transacted ?
     */
    private boolean transacted = false;

    /**
     * Original parent metadata (if method is inherited).
     */
    private EasyBeansEjbJarClassMetadata originalClassMetadata = null;

    /**
     * EasyBeans method interceptors. These interceptors correspond to a list of
     * Interceptors like security or transaction.
     */
    private List<? extends IJClassInterceptor> interceptors = null;

    /**
     * Constructor.
     * @param jMethod the method on which we will set/add metadata
     * @param classAnnotationMetadata the parent metadata.
     */
    public EasyBeansEjbJarMethodMetadata(final JMethod jMethod, final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        super(jMethod, classAnnotationMetadata);
    }

    /**
     * @return true if this method is inherited from a super class
     */
    public boolean isInherited() {
        return this.inherited;
    }

    /**
     * Sets the inheritance of this method.
     * @param inherited true if method is from a super class
     * @param originalClassMetadata the metadata of the original class (not
     *        inherited)
     */
    public void setInherited(final boolean inherited, final EasyBeansEjbJarClassMetadata originalClassMetadata) {
        this.inherited = inherited;
        this.originalClassMetadata = originalClassMetadata;
    }

    /**
     * @return true if this method should be ignored from bytecode processing
     */
    public boolean isIgnored() {
        return this.ignored;
    }

    /**
     * Specify if the method will be ignored or not.
     * @param ignored true/false
     */
    public void setIgnored(final boolean ignored) {
        this.ignored = ignored;
    }

    /**
     * @return original parent metadata (class) if inherited.
     */
    public EasyBeansEjbJarClassMetadata getOriginalClassMetadata() {
        return this.originalClassMetadata;
    }

    /**
     * Override because
     * {@link org.ow2.easybeans.enhancer.interceptors.InterceptorClassAdapter}
     * use it.
     * @return the method name
     */
    @Override
    public String getMethodName() {
        return getJMethod().getName();
    }

    /**
     * @return list of interceptors that enhancer will use. (ie :
     *         security/transaction)
     */
    public List<? extends IJClassInterceptor> getInterceptors() {
        return this.interceptors;
    }

    /**
     * Sets the list of interceptors(tx, security, etc) that enhancers will use.<br>
     * These interceptors are defined per methods.
     * @param interceptors list of interceptors that enhancer will use.
     */
    public void setInterceptors(final List<? extends IJClassInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    /**
     * @return a clone of this metadata.
     */
    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * @return true if this method is transacted
     */
    public boolean isTransacted() {
        return this.transacted;
    }

    /**
     * Sets the transacted mode.
     * @param transacted true if this method is transacted
     */
    public void setTransacted(final boolean transacted) {
        this.transacted = transacted;
    }

}
