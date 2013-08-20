/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@ow2.org
 * Copyright 2013 Peergreen S.A.S.
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
 * $Id:EjbJarArchiveMetadata.java 2372 2008-02-08 18:18:37Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.view;

import java.util.List;
import java.util.Map;

import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.scan.api.metadata.IMetadata;

/**
 * This class represents the annotation metadata of all classes of an EjbJar
 * file. From this class, we can get metadata of all beans.
 * @author Florent Benoit
 */
public class EasyBeansEjbJarView {

    /**
     * Wrapped.
     */
    private final IMetadata ejbJarMetadata;

    /**
     * Constructor.
     * @param ejb3Deployable the given EJB3 deployable
     */
    public EasyBeansEjbJarView(final IMetadata ejbJarMetadata) {
        this.ejbJarMetadata = ejbJarMetadata;
    }


    /**
     * @return the specific deployment descriptor object.
     */
    public EasyBeansDD getEasyBeansDD() {
        return this.ejbJarMetadata.get(EasyBeansDD.class);
    }

    /**
     * Sets the easybeans specific DD deployment descriptor object.
     * @param easybeansDD the specific deployment descriptor object.
     */
    public void setEasyBeansDD(final EasyBeansDD easybeansDD) {
        this.ejbJarMetadata.set(EasyBeansDD.class, easybeansDD);
    }

    /**
     * @return Map&lt;interceptor type &lt;--&gt; List of methods/class corresponding to the interceptor&gt; (interceptor classes)
     * of default interceptors that enhancer will use.
     */
    public Map<InterceptorType, List<? extends IJClassInterceptor>> getDefaultInterceptors() {
        return this.ejbJarMetadata.getProperty("default-interceptors");
    }

    /**
     * Sets the list of default interceptors that enhancers will use.<br>
     * These interceptors are defined by XML DD.
     * @param defaultInterceptors list of interceptors that enhancer will use.
     */
    public void setDefaultInterceptors(final Map<InterceptorType, List<? extends IJClassInterceptor>> defaultInterceptors) {
        this.ejbJarMetadata.setProperty("default-interceptors", defaultInterceptors);
    }

}
