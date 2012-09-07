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
 * $Id: BeanInfo.java 5488 2010-05-04 16:06:31Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import static javax.ejb.TransactionManagementType.CONTAINER;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionManagementType;

import org.ow2.easybeans.api.bean.info.IApplicationExceptionInfo;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IMethodInfo;
import org.ow2.easybeans.api.bean.info.ISecurityInfo;
import org.ow2.easybeans.api.bean.info.ITimerInfo;
import org.ow2.easybeans.api.bean.info.IWebServiceInfo;

/**
 * This class contains description for a bean.
 * It is used at the runtime.
 * @author Florent Benoit
 */
public class BeanInfo implements IBeanInfo {

    /**
     * Bean's name.
     */
    private String name;

    /**
     * Security info.
     */
    private ISecurityInfo securityInfo = null;

    /**
     * Management type for the bean.
     */
    private TransactionManagementType transactionManagementType = CONTAINER;

    /**
     * List of application exceptions used on this ejb-jar.
     */
    private Map<String, IApplicationExceptionInfo> applicationExceptions = null;

    /**
     * Cluster configuration.
     */
    private Object cluster;

    /**
     * Local interfaces.
     */
    private List<String> localInterfaces = null;

    /**
     * Remote interfaces.
     */
    private List<String> remoteInterfaces = null;


    /**
     * no inteface view interface (if any).
     */
    private String noInterfaceViewInterface = null;

    /**
     * Web Services infos.
     */
    private IWebServiceInfo webserviceInfo;

    /**
     * Business methods.
     */
    private List<IMethodInfo> businessMethodsInfo = null;

    /**
     * Session synchronization methods.
     */
    private List<IMethodInfo> sessionSynchronizationMethodsInfo = null;

    /**
     * List of timers.
     */
    private List<ITimerInfo> timersInfo = null;

    /**
     * Depends On ?
     */
    private List<String> dependsOn = null;

    /**
     * Startup flag (for singleton).
     */
    private boolean startup = false;

    /**
     * Default constructor.
     */
    public BeanInfo() {
        this.localInterfaces = new ArrayList<String>();
        this.remoteInterfaces = new ArrayList<String>();
        this.businessMethodsInfo = new ArrayList<IMethodInfo>();
        this.sessionSynchronizationMethodsInfo = new ArrayList<IMethodInfo>();
        this.dependsOn = new ArrayList<String>();
    }


    /**
     * Gets the list of application exceptions defined on this ejb jar metadata.
     * @return the list of application exceptions defined on this ejb jar metadata.
     */
    public Map<String, IApplicationExceptionInfo> getApplicationExceptions() {
        return this.applicationExceptions;
    }

    /**
     * Sets the list of application exceptions defined on this ejb jar metadata.
     * @param applicationExceptions the list of application exceptions defined on this ejb jar metadata.
     */
    public void setApplicationExceptions(final Map<String, IApplicationExceptionInfo> applicationExceptions) {
        this.applicationExceptions = applicationExceptions;
    }

    /**
     * Gets the type of transaction for the given bean.
     * @return transaction management type.
     */
    public TransactionManagementType getTransactionManagementType() {
        return this.transactionManagementType;
    }

    /**
     * Sets the type of transaction for the given bean.
     * @param transactionManagementType transaction management type.
     */
    public void setTransactionManagementType(final TransactionManagementType transactionManagementType) {
        this.transactionManagementType = transactionManagementType;
    }

    /**
     * Sets the security info.
     * @param securityInfo security info.
     */
    public void setSecurityInfo(final ISecurityInfo securityInfo) {
        this.securityInfo = securityInfo;
    }


    /**
     * Gets the security info.
     * @return security info.
     */
    public ISecurityInfo getSecurityInfo() {
        return this.securityInfo;
    }

    /**
     * Gets the business methods info.
     * @return list of business methods
     */
    public List<IMethodInfo> getBusinessMethodsInfo() {
        return this.businessMethodsInfo;
    }

    /**
     * Sets the list of business methods.
     * @param businessMethodsInfo the list of business methods
     */
    public void setBusinessMethodsInfo(final List<IMethodInfo> businessMethodsInfo) {
        this.businessMethodsInfo = businessMethodsInfo;
    }

    /**
     * Gets the name of the bean.
     * @return the name of the bean.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the bean.
     * @param name the bean's name.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets configuration of the cluster, if the bean is clustered.
     * @return the cluster
     */
    public Object getCluster() {
        return this.cluster;
    }

    /**
     * Sets configuration of the cluster, if the bean is clustered.
     * @param cluster the cluster to set
     */
    public void setCluster(final Object cluster) {
        this.cluster = cluster;
    }

    /**
     * @return list of local interfaces (if any).
     */
    public List<String> getLocalInterfaces() {
        return this.localInterfaces;
    }

    /**
     * @param localInterfaces list of local interfaces.
     */
    public void setLocalInterfaces(final List<String> localInterfaces) {
        // ensure that the names are using ".'
        this.localInterfaces = fix(localInterfaces);
    }

    /**
     * @return list of remote interfaces (if any).
     */
    public List<String> getRemoteInterfaces() {
        return this.remoteInterfaces;
    }

    /**
     * @param remoteInterfaces list of remote interfaces.
     */
    public void setRemoteInterfaces(final List<String> remoteInterfaces) {
        // ensure that the names are using ".'
        this.remoteInterfaces = fix(remoteInterfaces);
    }


    /**
     * @return no inteface view interface (if any).
     */
    public String getNoInterfaceViewInterface() {
        return this.noInterfaceViewInterface;
    }

    /**
     * @param noInterfaceViewInterface no inteface view interface (if any)..
     */
    public void setNoInterfaceViewInterface(final String noInterfaceViewInterface) {
        // ensure that the names are using ".'
        this.noInterfaceViewInterface = fix(noInterfaceViewInterface);
    }

    /**
     * @return the webservices info related to this bean (if any)
     */
    public IWebServiceInfo getWebServiceInfo() {
        return this.webserviceInfo;
    }

    /**
     * @param info web services related runtime information.
     */
    public void setWebServiceInfo(final IWebServiceInfo info) {
        this.webserviceInfo = info;
    }

    /**
     * Convert all / char into dot chars in the elements of the list.
     * @param list the parameter list
     * @return an updated list.
     */
    protected List<String> fix(final List<String> list) {
        if (list == null) {
            return null;
        }
        List<String> newList = new ArrayList<String>();
        for (String item : list) {
            newList.add(fix(item));
        }
        return newList;
    }

    /**
     * Convert all / char into dot chars in the elements of the list.
     * @param name the parameter
     * @return an updated name.
     */
    protected String fix(final String name) {
        if (name == null) {
            return null;
        }
        return name.replace('/', '.');
    }


    /**
     * Gets a default checked exception attribute with rollback = false for checkedException.
     * @return an application exception
     */
    public IApplicationExceptionInfo getDefaultCheckedException() {
        return new ApplicationExceptionInfo(false, true);
    }


    /**
     * Try to see if we've an application exception object for the given exception.
     * @param throwable the given throwable
     * @return the object if found
     */
    public IApplicationExceptionInfo getApplicationException(final Throwable throwable) {

        // Do we have a direct match for the given throwable
        IApplicationExceptionInfo appException = this.applicationExceptions.get(throwable.getClass().getName());
        if (appException != null) {
            return appException;
        }

        // No matching exception, needs to check if there is inheritance

        // Get all super classes of the given exception
        List<String> superClasses = getSuperClass(throwable.getClass());

        // Now, search if we've a matching application exceptions in these super classes
        int i = 0;
        while (appException == null && i < superClasses.size()) {
            String superClass = superClasses.get(i);
            appException = this.applicationExceptions.get(superClass);
            // If we've found a inherited exception but this one is not
            // inherited, the exception is not considered as an application
            // exception
            if (appException != null && !appException.inherited()) {
                return null;
            }
            i++;
        }
        return appException;
    }



    /**
     * @param clazz the class instance
     * @return list of super class names for the given class
     */
    protected List<String> getSuperClass(final Class<?> clazz) {
        Class<?> superClass = clazz.getSuperclass();
        List<String> list = new ArrayList<String>();

        // no more super classes.
        if (Throwable.class.equals(superClass) || Object.class.equals(superClass)) {
            return list;
        }
        list.add(superClass.getName());

        // Else, add the super classes
        list.addAll(getSuperClass(superClass));

        return list;
    }

    /**
     * Defines the list of dependencies of this bean.
     * @param dependsOn the given list
     */
    public void setDependsOn(final List<String> dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * @return the list of dependencies of this bean.
     */
    public List<String> getDependsOn() {
        return this.dependsOn;
    }

    /**
     * Singleton startup ?
     * @return true if the singleton is a startup singleton
     */
    public boolean isStartup() {
        return this.startup;
    }

    /**
     * Sets the startup mode for the singleton.
     * @param startup true/false
     */
    public void setStartup(final boolean startup) {
        this.startup = startup;
    }

    /**
     * Gets the Session Synchronization methods.
     * @return the list of Session Synchronization methods
     */
    public List<IMethodInfo> getSessionSynchronizationMethodsInfo() {
        return this.sessionSynchronizationMethodsInfo;
    }

    /**
     * Sets the list of Session Synchronization methods.
     * @param sessionSynchronizationMethodsInfo the list of Session Synchronization methods
     */
    public void setSessionSynchronizationMethodsInfo(final List<IMethodInfo> sessionSynchronizationMethodsInfo) {
        this.sessionSynchronizationMethodsInfo = sessionSynchronizationMethodsInfo;
    }


    /**
     * @return list of timers that needs to be applied on this bean.
     */
    public List<ITimerInfo> getTimersInfo() {
        return this.timersInfo;
    }

    /**
     * Sets the timers info.
     * @param timersInfo the  list of timers that needs to be applied on this bean.
     */
    public void setTimersInfo(final List<ITimerInfo> timersInfo) {
        this.timersInfo = timersInfo;
    }
}
