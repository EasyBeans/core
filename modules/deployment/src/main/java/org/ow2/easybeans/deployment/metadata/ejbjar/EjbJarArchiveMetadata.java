/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id:EjbJarArchiveMetadata.java 2372 2008-02-08 18:18:37Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;

import java.util.List;
import java.util.Map;

import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.util.ee.deploy.api.deployable.EJB3Deployable;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.ee.metadata.ejbjar.impl.EjbJarDeployableMetadata;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class represents the annotation metadata of all classes of an EjbJar
 * file. From this class, we can get metadata of all beans.
 * @author Florent Benoit
 */
public class EjbJarArchiveMetadata
        extends
        EjbJarDeployableMetadata<EJB3Deployable, EjbJarArchiveMetadata, EasyBeansEjbJarClassMetadata, EasyBeansEjbJarMethodMetadata, EasyBeansEjbJarFieldMetadata> {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = -3149359097049247372L;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EjbJarArchiveMetadata.class);

    /**
     * Link to the Specific Deployment Descriptor object.
     */
    private EasyBeansDD easybeansDD = null;

    /**
     * List of default interceptors by type (business or lifecycle).
     */
    private Map<InterceptorType, List<? extends IJClassInterceptor>> defaultInterceptors = null;


    /**
     * Constructor.
     * @param ejb3Deployable the given EJB3 deployable
     */
    public EjbJarArchiveMetadata(final EJB3Deployable ejb3Deployable) {
        super(ejb3Deployable);
    }

    /**
     * Gets the class metadata for the given ejb-name.
     * @return class metadata or null if not found
     * @param ejbName the name of the EJB.
     */
    @Override
    public EasyBeansEjbJarClassMetadata getEjbJarClassMetadataForEjbName(final String ejbName) {
        return super.getEjbJarClassMetadataForEjbName(ejbName);
    }

    /**
     * @return the specific deployment descriptor object.
     */
    public EasyBeansDD getEasyBeansDD() {
        return this.easybeansDD;
    }

    /**
     * Sets the easybeans specific DD deployment descriptor object.
     * @param easybeansDD the specific deployment descriptor object.
     */
    public void setEasyBeansDD(final EasyBeansDD easybeansDD) {
        this.easybeansDD = easybeansDD;
    }

    /**
     * @return Map&lt;interceptor type &lt;--&gt; List of methods/class corresponding to the interceptor&gt; (interceptor classes)
     * of default interceptors that enhancer will use.
     */
    public Map<InterceptorType, List<? extends IJClassInterceptor>> getDefaultInterceptors() {
        return this.defaultInterceptors;
    }

    /**
     * Sets the list of default interceptors that enhancers will use.<br>
     * These interceptors are defined by XML DD.
     * @param defaultInterceptors list of interceptors that enhancer will use.
     */
    public void setDefaultInterceptors(final Map<InterceptorType, List<? extends IJClassInterceptor>> defaultInterceptors) {
        this.defaultInterceptors = defaultInterceptors;
    }

}
