/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 *
 * --------------------------------------------------------------------------
 * $Id: EasyBeansEjbJarMethodMetadata.java 5643 2010-10-18 15:17:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.view;

import java.util.List;

import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.view.IEJBMethodView;
import org.ow2.util.ee.metadata.ejbjar.impl.view.EJBView;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IMetadata;
import org.ow2.util.scan.api.metadata.IMethodMetadata;
import org.ow2.util.scan.api.metadata.structures.IMethod;

/**
 * This class represents the annotation metadata of a method.
 * @author Florent Benoit
 */
public class EasyBeansMethodView extends EJBView implements IEJBMethodView {

    /**
     * Wrapped metadata
     */
    private final IMetadata methodMetadata;

    /**
     * Constructor.
     * @param methodMetadata the metadata.
     */
    public EasyBeansMethodView(final IMetadata methodMetadata) {
        super(methodMetadata);
        this.methodMetadata = methodMetadata;
    }

    /**
     * @return true if this method is inherited from a super class
     */
    public boolean isInherited() {
        return methodMetadata.getBoolean("inherited");
    }

    /**
     * @return true if this method is generated for a super method call
     */
    public boolean isPrivateSuperCallGenerated() {
        return methodMetadata.getBoolean("privateSuperCallGenerated");
    }

    /**
     * Sets the inheritance of this method.
     * @param privateSuperCallGenerated true if a method needs to be generated for a super private call method.
     * @param originalClassMetadata the metadata of the original class (not
     *        inherited)
     */
    public void setPrivateSuperCallGenerated(final boolean privateSuperCallGenerated, final IClassMetadata originalClassMetadata, final int inheritanceLevel) {
        // disable inheritance
        this.methodMetadata.setProperty("inherited", false);
        this.methodMetadata.setProperty("privateSuperCallGenerated", privateSuperCallGenerated);
        this.methodMetadata.setProperty("originalClassMetadata",  originalClassMetadata);
        this.methodMetadata.setProperty("inheritanceLevel",  inheritanceLevel);
    }

    /**
     * Sets the inheritance of this method.
     * @param inherited true if method is from a super class
     * @param originalClassMetadata the metadata of the original class (not
     *        inherited)
     */
    public void setInherited(final boolean inherited, final IClassMetadata originalClassMetadata) {
        this.methodMetadata.setProperty("inherited", inherited);
        this.methodMetadata.setProperty("originalClassMetadata",  originalClassMetadata);
    }

    /**
     * @return true if this method should be ignored from bytecode processing
     */
    public boolean isIgnored() {
        return this.methodMetadata.getBoolean("ignored");
    }

    /**
     * Specify if the method will be ignored or not.
     * @param ignored true/false
     */
    public void setIgnored(final boolean ignored) {
        this.methodMetadata.setProperty("ignored", ignored);
    }

    /**
     * @return original parent metadata (class) if inherited.
     */
    public IClassMetadata getOriginalClassMetadata() {
        return this.methodMetadata.getProperty("originalClassMetadata");
    }

    /**
     * @return list of interceptors that enhancer will use. (ie :
     *         security/transaction)
     */
    public List<? extends IJClassInterceptor> getInterceptors() {
        return this.methodMetadata.getProperty("interceptors");
    }

    /**
     * Sets the list of interceptors(tx, security, etc) that enhancers will use.<br>
     * These interceptors are defined per methods.
     * @param interceptors list of interceptors that enhancer will use.
     */
    public void setInterceptors(final List<? extends IJClassInterceptor> interceptors) {
        this.methodMetadata.setProperty("interceptors", interceptors);
    }


    /**
     * @return true if this method is transacted
     */
    public boolean isTransacted() {
        return this.methodMetadata.getBoolean("transacted");
    }

    /**
     * Sets the transacted mode.
     * @param transacted true if this method is transacted
     */
    public void setTransacted(final boolean transacted) {
        this.methodMetadata.setProperty("transacted", transacted);
    }

    /**
     * @return true if this method is a session synchronization interface.
     */
    public boolean isSessionSynchronization() {
        IEJBMethodView methodView = this.methodMetadata.as(IEJBMethodView.class);
        return methodView.isAfterBegin() || methodView.isAfterCompletion() || methodView.isBeforeCompletion();
    }

    /**
     * Inheritance Level.
     * @return inheritance level
     */
    public int getInheritanceLevel() {
        // FIXME: test
        return this.methodMetadata.getProperty("inheritanceLevel");
    }

    public String getSuperPrivateMethodName() {
        return this.methodMetadata.getProperty("superPrivateMethodName");
    }

    public void setSuperPrivateMethodName(final String superPrivateMethodName) {
        this.methodMetadata.setProperty("superPrivateMethodName", superPrivateMethodName);
    }

    public IMethodMetadata getMethodMetadata() {
        return (IMethodMetadata) methodMetadata;
    }

    public IMethod getJMethod() {
        return getMethodMetadata().getJMethod();
    }

    public String getMethodName() {
        return getJMethod().getName();
    }

}
