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
 * $Id: IBeanInfo.java 5488 2010-05-04 16:06:31Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;


import java.util.List;
import java.util.Map;

import javax.ejb.TransactionManagementType;

/**
 * This interface is used for containing a description for a bean.
 * It is used at the runtime.
 * @author Florent Benoit
 */
public interface IBeanInfo {

    /**
     * Gets the list of application exceptions defined on this ejb jar metadata.
     * @return the list of application exceptions defined on this ejb jar metadata.
     */
    Map<String, IApplicationExceptionInfo> getApplicationExceptions();

    /**
     * Sets the list of application exceptions defined on this ejb jar metadata.
     * @param applicationExceptions the list of application exceptions defined on this ejb jar metadata.
     */
    void setApplicationExceptions(final Map<String, IApplicationExceptionInfo> applicationExceptions);

    /**
     * Try to see if we've an application exception object for the given exception.
     * @param throwable the given throwable
     * @return the object if found
     */
    IApplicationExceptionInfo getApplicationException(final Throwable throwable);

    /**
     * Gets a default checked exception attribute with rollback = false for checkedException.
     * @return an application exception
     */
    IApplicationExceptionInfo getDefaultCheckedException();


    /**
     * Gets the type of transaction for the given bean.
     * @return transaction management type.
     */
    TransactionManagementType getTransactionManagementType();

    /**
     * Sets the type of transaction for the given bean.
     * @param transactionManagementType transaction management type.
     */
    void setTransactionManagementType(final TransactionManagementType transactionManagementType);

    /**
     * Gets the security info.
     * @return security info.
     */
    ISecurityInfo getSecurityInfo();

    /**
     * Sets the security info.
     * @param securityInfo security info.
     */
    void setSecurityInfo(final ISecurityInfo securityInfo);

    /**
     * Gets the business methods info.
     * @return list of business methods
     */
    List<IMethodInfo> getBusinessMethodsInfo();

    /**
     * Sets the list of business methods.
     * @param businessMethodsInfo the list of business methods
     */
    void setBusinessMethodsInfo(final List<IMethodInfo> businessMethodsInfo);

    /**
     * Gets the name of the bean.
     * @return the name of the bean.
     */
    String getName();

    /**
     * Sets the name of the bean.
     * @param name the bean's name.
     */
    void setName(final String name);

    /**
     * @return the cluster configuration.
     */
    Object getCluster();

    /**
     * Sets the cluster configuration.
     * @param cluster the cluster configuration to set.
     */
    void setCluster(final Object cluster);

    /**
     * @return list of local interfaces (if any).
     */
    List<String> getLocalInterfaces();

    /**
     * @param localInterfaces list of local interfaces.
     */
    void setLocalInterfaces(List<String> localInterfaces);

    /**
     * @return list of remote interfaces (if any).
     */
    List<String> getRemoteInterfaces();

    /**
     * @param remoteInterfaces list of remote interfaces.
     */
    void setRemoteInterfaces(List<String> remoteInterfaces);

    /**
     * @return no inteface view interface (if any).
     */
    String getNoInterfaceViewInterface();

    /**
     * @param noInterfaceViewInterface no inteface view interface (if any)..
     */
    void setNoInterfaceViewInterface(String noInterfaceViewInterface);

    /**
     * @return the webservices info related to this bean (if any)
     */
    IWebServiceInfo getWebServiceInfo();

    /**
     * @param info web services related runtime information.
     */
    void setWebServiceInfo(IWebServiceInfo info);

    /**
     * @return the list of dependencies of this bean.
     */
    List<String> getDependsOn();

    /**
     * Singleton startup ?
     * @return true if the singleton is a startup singleton
     */
    boolean isStartup();

    /**
     * Sets the startup mode for the singleton.
     * @param startup true/false
     */
    void setStartup(final boolean startup);
}
