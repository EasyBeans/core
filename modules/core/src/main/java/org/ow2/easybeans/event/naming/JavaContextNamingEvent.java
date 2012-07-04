/**
 * EasyBeans
 * Copyright (C) 2008-2011 Bull S.A.S.
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
 * $Id: JavaContextNamingEvent.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.naming;

import java.util.LinkedList;
import java.util.List;

import javax.naming.Context;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.event.naming.EZBJavaContextNamingEvent;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.event.AbstractEvent;

/**
 * A JavaContextNamingEvent is an IEvent fired/dispatched during bean's context building phase.
 * @author Guillaume Sauthier
 */
public class JavaContextNamingEvent extends AbstractEvent implements EZBJavaContextNamingEvent {

    /**
     * The EJB factory for which the context will be filled.
     */
    private Factory<?, ?> factory;

    /**
     * The target java context.
     */
    private Context javaContext;

    /**
     * The list of Throwables that may be thrown by the listeners.
     */
    private List<Throwable> throwables;

    /**
     * Additional metadatas.
     */
    private EasyBeansEjbJarClassMetadata beanMetadata = null;

    /**
     * The default constructor.
     *
     * @param source The event source.
     * @param javaContext the target Context
     * @param factory the EJB factory
     * @param beanMetadata bean related metadata
     */
    public JavaContextNamingEvent(final String source,
                           final Context javaContext,
                           final Factory<?, ?> factory,
                           final EasyBeansEjbJarClassMetadata beanMetadata) {
        super(source);
        this.throwables = new LinkedList<Throwable>();
        this.factory = factory;
        this.javaContext = javaContext;
        this.beanMetadata = beanMetadata;
    }



    /**
     * @return the EasyBeans EJB factory.
     */
    public Factory<?, ?> getFactory() {
        return this.factory;
    }

    /**
     * @return the java: Context.
     */
    public Context getJavaContext() {
        return this.javaContext;
    }

    /**
     * @return the list of throwables thrown by listeners.
     */
    public List<Throwable> getThrowables() {
        return this.throwables;
    }

    /**
     * Append a new Throwable in the list of throwables thrown by listeners.
     * @param throwable Added exception
     */
    public void addThrowable(final Throwable throwable) {
        this.throwables.add(throwable);
    }


    /**
     * @return the EJB associated metadatas.
     */
    public EasyBeansEjbJarClassMetadata getBeanMetadata() {
        return this.beanMetadata;
    }
}
