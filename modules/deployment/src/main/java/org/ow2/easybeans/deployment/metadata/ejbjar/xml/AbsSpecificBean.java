/**
 * EasyBeans
 * Copyright (C) 2008-2009 Bull S.A.S.
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
 * $Id: AbsSpecificBean.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.ArrayList;
import java.util.List;

import org.ow2.util.pool.api.IPoolConfiguration;

/**
 * Common stuff for the specific part of EJB.
 * @author Florent BENOIT
 */
public class AbsSpecificBean {

    /**
     * Name of the bean.
     */
    private String ejbName = null;

    /**
     * Pool's configuration.
     */
    private IPoolConfiguration poolConfiguration = null;

    /**
     * Cluster configuration.
     */
    private Object cluster = null;

    /**
     * List of service-ref info that can be used to override @WebServiceRef values.
     */
    private List<ServiceRef> serviceReferences;

    /**
     * RunAs of the bean.
     */
    private RunAs runAs = null;

    /**
     * Default constructor.
     */
    public AbsSpecificBean() {
        this.serviceReferences = new ArrayList<ServiceRef>();
    }

    /**
     * @return the name of the EJB
     */
    public String getEjbName() {
        return this.ejbName;
    }

    /**
     * Sets the name of the EJB.
     * @param ejbName the given name
     */
    public void setEjbName(final String ejbName) {
        this.ejbName = ejbName;
    }

    /**
     * @return the configuration of the pool.
     */
    public IPoolConfiguration getPoolConfiguration() {
        return this.poolConfiguration;
    }

    /**
     * Sets the pool's configuration.
     * @param poolConfiguration the given configuration
     */
    public void setPoolConfiguration(final IPoolConfiguration poolConfiguration) {
        this.poolConfiguration = poolConfiguration;
    }

    /**
     * Gets the cluster configuration.
     * @return the cluster configuration
     */
    public Object getCluster() {
        return this.cluster;
    }

    /**
     * Sets the cluster configuration.
     * @param cluster the cluster configuration to set
     */
    public void setCluster(final Object cluster) {
        this.cluster = cluster;
    }

    /**
     * @return the list of web service references.
     */
    public List<ServiceRef> getServiceRefs() {
        return this.serviceReferences;
    }

    /**
     * Set the list of references.
     * @param references web services references
     */
    public void setServiceRefs(final List<ServiceRef> references) {
        this.serviceReferences = references;
    }

    /**
     * @return the run-as of the bean.
     */
    public RunAs getRunAs() {
        return this.runAs;
    }

    /**
     * Set the run-as of the bean.
     * @param runAs run-as of the bean
     */
    public void setRunAs(final RunAs runAs) {
        this.runAs = runAs;
    }

}
