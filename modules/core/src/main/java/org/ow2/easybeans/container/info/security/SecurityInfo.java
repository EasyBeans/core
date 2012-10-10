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
 * $Id: SecurityInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info.security;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.Subject;

import org.ow2.easybeans.api.bean.info.IMethodSecurityInfo;
import org.ow2.easybeans.api.bean.info.ISecurityInfo;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.RunAs;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.SecurityRoleMapping;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.Session;
import org.ow2.easybeans.security.struct.JGroup;
import org.ow2.easybeans.security.struct.JPrincipal;
import org.ow2.util.ee.metadata.common.api.xml.struct.ISecurityRoleRef;

/**
 * Runtime info about security.
 * @author Florent Benoit
 */
public class SecurityInfo implements ISecurityInfo {

    /**
     * List of roles.
     */
    private List<String> declaredRoles = null;

    /**
     * List of methods.
     */
    private List<IMethodSecurityInfo> methodSecurityInfos = null;

    /**
     * Name of the run-as role.
     */
    private String runAsRole = null;

    /**
     * Subject for run-as role.
     */
    private Subject runAsSubject = null;

    /**
     * The metadata of the current bean.
     */
    private EasyBeansEjbJarClassMetadata bean = null;

    /**
     * Default constructor.
     * @param bean the metadata of the current bean.
     */
    public SecurityInfo(final EasyBeansEjbJarClassMetadata bean) {
        this.bean = bean;
        this.methodSecurityInfos = new ArrayList<IMethodSecurityInfo>();
    }

    /**
     * Adds a method containing security.
     * @param methodSecurityInfo the info about security.
     */
    public void addMethodSecurityInfo(final IMethodSecurityInfo methodSecurityInfo) {
        this.methodSecurityInfos.add(methodSecurityInfo);
    }

    /**
     * @return list of security infos on all methods.
     */
    public List<IMethodSecurityInfo> getMethodSecurityInfos() {
        return this.methodSecurityInfos;
    }

    /**
     * Sets the name of the run-as security role.
     * @param runAsRole the name of the role.
     */
    public void setRunAsRole(final String runAsRole) {
        this.runAsRole = runAsRole;
        this.runAsSubject = new Subject();

        // Structure associating a principal with its roles
        Map<Principal, List<Principal>> principals = getRunAsPrincipals();

        // Add principal name
        Principal principal = principals.keySet().iterator().next();
        this.runAsSubject.getPrincipals().add(principal);

        // Add roles for this principal
        Group roles = new JGroup("roles");
        roles.addMember(new JPrincipal(runAsRole));

        // Add list roles for this role
        for (Principal member : principals.get(principal)) {
            roles.addMember(member);
        }

        this.runAsSubject.getPrincipals().add(roles);

    }

    /**
     * Gets run-as name.
     * @return the name of the security role for the run-as.
     */
    public String getRunAsRole() {
        return this.runAsRole;
    }

    /**
     * Gets run-as role subject.
     * @return a subject with run-as role as role.
     */
    public Subject getRunAsSubject() {
        return this.runAsSubject;
    }

    /**
     * Adds a role for this bean (for isCallerInRole).
     * @param roleName the name of a role.
     */
    public void addDeclaredRole(final String roleName) {
        this.declaredRoles.add(roleName);
    }

    /**
     * @return list of roles declared for this bean.
     */
    public List<String> getDeclaredRoles() {
        return this.declaredRoles;
    }

    /**
     * Sets the list of declared roles.
     * @param declaredRoles list of declared roles.
     */
    public void setDeclaredRole(final List<String> declaredRoles) {
        this.declaredRoles = declaredRoles;
    }

    /**
     * Read security role mappings from metadata and return the list of
     * principals for the current run-as role.
     * @return The list of principals for the current run-as role
     */
    private Map<Principal, List<Principal>> getRunAsPrincipals() {
        Map<Principal, List<Principal>> principalMap = new HashMap<Principal, List<Principal>>();
        // List of principals to return
        List<Principal> principals = new ArrayList<Principal>();

        // By default, build the principal with the run-as role value
        Principal principal = new JPrincipal(this.runAsRole);

        // Get the EasyBeans DD
        EasyBeansDD easyBeansDD = this.bean.getEjbJarDeployableMetadata().getEasyBeansDD();
        if (easyBeansDD != null) {
            // Get the current bean name
            String beanName;
            if (this.bean.isSession() || this.bean.isMdb()) {
                beanName = this.bean.getJCommonBean().getName();
            } else {
                // ManagedBean
                beanName = this.bean.getManagedBeanName();
            }

            // For each declared session bean
            List<Session> sessions = easyBeansDD.getEJB().getSessions();
            for (Session session : sessions) {
                String ejbName = session.getEjbName();

                // Found the bean to analyze
                if (beanName.equals(ejbName)) {
                    RunAs runAs = session.getRunAs();
                    if (runAs != null) {
                        // Found the principal name to use with the current bean
                        String beanPincipalName = runAs.getPrincipalName();
                        principal = new JPrincipal(beanPincipalName);

                        // Read security role mappings to get all roles for the principal name identity
                        for (SecurityRoleMapping securityRoleMapping : easyBeansDD.getSecurityRoleMappings()) {
                            for (String pincipalName : securityRoleMapping.getPrincipalNames()) {
                                if (beanPincipalName.equals(pincipalName)) {
                                    principals.add(new JPrincipal(securityRoleMapping.getRoleName()));
                                }
                            }
                        }
                    }
                }
            }
        }

        // Fill the result map
        principalMap.put(principal, principals);

        return principalMap;
    }

    /**
     * Gets the list of &lt;security-role-ref&gt; elements.
     * @return list of &lt;security-role-ref&gt; elements.
     */
    public List<ISecurityRoleRef> getSecurityRoleRefList() {
        return this.bean.getSecurityRoleRefList();
    }

}
