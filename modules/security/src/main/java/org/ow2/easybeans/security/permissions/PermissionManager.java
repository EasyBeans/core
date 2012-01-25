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
 * $Id: PermissionManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.security.permissions;

import java.net.URL;
import java.security.CodeSource;
import java.security.Principal;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.List;

import javax.security.jacc.EJBMethodPermission;
import javax.security.jacc.EJBRoleRefPermission;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;

import org.ow2.easybeans.api.EZBPermissionManager;
import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.api.PermissionManagerException;
import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.bean.info.IEJBJarInfo;
import org.ow2.easybeans.api.bean.info.IMethodSecurityInfo;
import org.ow2.easybeans.api.bean.info.ISecurityInfo;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;
import org.ow2.util.ee.metadata.common.api.xml.struct.ISecurityRoleRef;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Permission manager for EJB.
 * @author Florent Benoit
 */
public class PermissionManager extends AbsPermissionManager implements EZBPermissionManager {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(PermissionManager.class);

    /**
     * CodeSource.
     */
    private CodeSource codeSource = null;

    /**
     * EJB-jar Info.
     */
    private IEJBJarInfo ejbJarInfo;

    /**
     * Default Constructor.
     * @param contextIdURL context ID used for PolicyContext
     * @param ejbJarInfo the metadata on all the beans (runtime info)
     * @throws PermissionManagerException if permissions can't be set
     */
    public PermissionManager(final URL contextIdURL, final IEJBJarInfo ejbJarInfo) throws PermissionManagerException {
        super(contextIdURL);
        this.ejbJarInfo = ejbJarInfo;
        this.codeSource = new CodeSource(contextIdURL, (Certificate[]) null);

    }

    /**
     * 3.1.5 Translating EJB Deployment Descriptors<br>
     * A reference to a PolicyConfiguration object must be obtained by calling
     * the getPolicyConfiguration method on the PolicyConfigurationFactory
     * implementation class of the provider configured into the container. The
     * policy context identifier used in the call to getPolicyConfiguration must
     * be a String that satisfies the requirements described in Section 3.1.4,
     * EJB Policy Context Identifiers, on page 28. The value true must be passed
     * as the second parameter in the call to getPolicyConfiguration to ensure
     * that any and all policy statements are removed from the policy context
     * associated with the returned PolicyConfiguration. The method-permission,
     * exclude-list, and security-role-ref elements appearing in the deployment
     * descriptor must be translated into permissions and added to the
     * PolicyConfiguration object to yield an equivalent translation as that
     * defined in the following sections and such that every EJB method for
     * which the container performs pre-dispatch access decisions is implied by
     * at least one permission resulting from the translation.
     * @throws PermissionManagerException if permissions can't be set
     */
    public void translateMetadata() throws PermissionManagerException {
        List<IBeanInfo> beansInfo = this.ejbJarInfo.getBeanInfos();
        if (beansInfo != null) {
            for (IBeanInfo beanInfo : beansInfo) {
                ISecurityInfo securityInfo = beanInfo.getSecurityInfo();
                translateEjbMethodPermission(securityInfo);
                translateEjbExcludeList(securityInfo);
                translateEjbSecurityRoleRef(beanInfo, securityInfo);
            }
        }
    }

    /**
     * 3.1.5.1 Translating EJB method-permission Elements<br>
     * For each method element of each method-permission element, an
     * EJBMethodPermission object translated from the method element must be
     * added to the policy statements of the PolicyConfiguration object. The
     * name of each such EJBMethodPermission object must be the ejb-name from
     * the corresponding method element, and the actions must be established by
     * translating the method element into a method specification according to
     * the methodSpec syntax defined in the documentation of the
     * EJBMethodPermission class. The actions translation must preserve the
     * degree of specificity with respect to method-name, method-intf, and
     * method-params inherent in the method element. If the method-permission
     * element contains the unchecked element, then the deployment tools must
     * call the addToUncheckedPolicy method to add the permissions resulting
     * from the translation to the PolicyConfiguration object. Alternatively, if
     * the method-permission element contains one or more role-name elements,
     * then the deployment tools must call the addToRole method to add the
     * permissions resulting from the translation to the corresponding roles of
     * the PolicyConfiguration object.
     * @param securityInfo the security info for a given bean.
     * @throws PermissionManagerException if permissions can't be set
     */
    protected void translateEjbMethodPermission(final ISecurityInfo securityInfo) throws PermissionManagerException {
        List<IMethodSecurityInfo> methodSecurityInfos = securityInfo.getMethodSecurityInfos();
        if (methodSecurityInfos != null) {
            for (IMethodSecurityInfo methodSecurityInfo : methodSecurityInfos) {
                if (methodSecurityInfo.isUnchecked()) {
                    try {
                        this.logger.debug("Adding unchecked permission {0}", methodSecurityInfo.getPermission());
                        getPolicyConfiguration().addToUncheckedPolicy(methodSecurityInfo.getPermission());
                    } catch (PolicyContextException e) {
                        throw new PermissionManagerException("Cannot add unchecked policy for method '" + methodSecurityInfo
                                + "'.", e);
                    }
                } else {
                    for (String roleName : methodSecurityInfo.getRoles()) {
                        try {
                            this.logger.debug("Adding permission {0} to role {1}", methodSecurityInfo.getPermission(), roleName);
                            getPolicyConfiguration().addToRole(roleName, methodSecurityInfo.getPermission());
                        } catch (PolicyContextException e) {
                            throw new PermissionManagerException("Cannot add rolebase policy for method '" + methodSecurityInfo
                                    + "' and for role '" + roleName + "'.", e);
                        }
                    }
                }
            }
        }
    }

    /**
     * 3.1.5.2 Translating the EJB exclude-list<br>
     * An EJBMethodPermission object must be created for each method element
     * occurring in the exclude-list element of the deployment descriptor. The
     * name and actions of each EJBMethodPermission must be established as
     * described in Section 3.1.5.1, Translating EJB method-permission Elements.
     * The deployment tools must use the addToExcludedPolicy method to add the
     * EJBMethodPermission objects resulting from the translation of the
     * exclude-list to the excluded policy statements of the PolicyConfiguration
     * object.
     * @param securityInfo the security info for a given bean.
     * @throws PermissionManagerException if permissions can't be set
     */
    protected void translateEjbExcludeList(final ISecurityInfo securityInfo) throws PermissionManagerException {
        List<IMethodSecurityInfo> methodSecurityInfos = securityInfo.getMethodSecurityInfos();
        if (methodSecurityInfos != null) {
            for (IMethodSecurityInfo methodSecurityInfo : methodSecurityInfos) {
                if (methodSecurityInfo.isExcluded()) {
                    try {
                        this.logger.debug("Adding excluded permission {0}", methodSecurityInfo.getPermission());
                        getPolicyConfiguration().addToExcludedPolicy(methodSecurityInfo.getPermission());
                    } catch (PolicyContextException e) {
                        throw new PermissionManagerException("Cannot add excluded policy for method '" + methodSecurityInfo
                                + "'.", e);
                    }
                }
            }
        }
    }

    /**
     * 3.1.5.3 Translating EJB security-role-ref Elements<br>
     * For each security-role-ref element appearing in the deployment
     * descriptor, a corresponding EJBRoleRefPermission must be created. The
     * name of each EJBRoleRefPermission must be obtained as described for
     * EJBMethodPermission objects. The actions used to construct the permission
     * must be the value of the role-name (that is the reference), appearing in
     * the security-role-ref. The deployment tools must call the addToRole
     * method on the PolicyConfiguration object to add a policy statement
     * corresponding to the EJBRoleRefPermission to the role identified in the
     * rolelink appearing in the security-role-ref.
     * @param beanInfo info about the bean.
     * @param securityInfo the security info for a given bean.
     * @throws PermissionManagerException if permissions can't be set
     */
    public void translateEjbSecurityRoleRef(final IBeanInfo beanInfo, final ISecurityInfo securityInfo)
            throws PermissionManagerException {

        // For each role that has been declared
        List<String> declaredRoles = securityInfo.getDeclaredRoles();
        if (declaredRoles != null) {
            for (String role : declaredRoles) {
                try {
                    getPolicyConfiguration().addToRole(role, new EJBRoleRefPermission(beanInfo.getName(), role));
                } catch (PolicyContextException e) {
                    throw new PermissionManagerException("Cannot add to role '" + role + "' an  EJBRoleRefPermission.", e);
                }
            }
        }

        // For each security-role-ref, add an entry
        List<ISecurityRoleRef> securityRoleRefs = securityInfo.getSecurityRoleRefList();
        if (securityRoleRefs != null) {
            for (ISecurityRoleRef securityRoleRef : securityRoleRefs) {
                try {
                    getPolicyConfiguration().addToRole(securityRoleRef.getRoleLink(), new EJBRoleRefPermission(beanInfo.getName(), securityRoleRef.getRoleName()));
                } catch (PolicyContextException e) {
                    throw new PermissionManagerException("Cannot add to role-link'" + securityRoleRef.getRoleLink() + "' the EJBRoleRefPermission build with role-name '" + securityRoleRef.getRoleName() + "'.", e);
                }
            }
        }


    }

    /**
     * Checks the security for the given invocation context.
     * @param invocationContext the context to check.
     * @param runAsBean if true, the bean is a run-as bean.
     * @return true if the access has been granted, else false.
     */
    public boolean checkSecurity(final EasyBeansInvocationContext invocationContext, final boolean runAsBean) {
        PolicyContext.setContextID(getContextId());

        // Build Protection Domain with a codesource and array of principal
        // Get roles.
        Principal[] principals = SecurityCurrent.getCurrent().getSecurityContext().getCallerRoles(runAsBean);
        ProtectionDomain protectionDomain = new ProtectionDomain(this.codeSource, null, null, principals);

        boolean accessOK = getPolicy().implies(protectionDomain, invocationContextToMethodPermission(invocationContext));
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Policy.implies result = {0} ", Boolean.valueOf(accessOK));
        }
        return accessOK;
    }

    /**
     * Gets a EJBMethodPermission from an invocation context.
     * @param invocationContext the context containing data on the current
     *        invocation.
     * @return a Method Permission for the current method.
     */
    private static EJBMethodPermission invocationContextToMethodPermission(final EasyBeansInvocationContext invocationContext) {
        // TODO : cache ejbName/methodSignature to avoid creation of a new
        // EJBMethodPermission each time
        // See JACC 4.12

        // TODO: Fix Remote/Local method-itf parameter. (set to "" for now)
        EJBMethodPermission ejbMethodPermission = new EJBMethodPermission(invocationContext.getFactory().getBeanInfo().getName(),
                "", invocationContext.getMethod());

        return ejbMethodPermission;
    }

    /**
     * Test if the caller has a given role. EJBRoleRefPermission object must be
     * created with ejbName and actions equal to roleName<br/>
     * See section 4.3.2 of JACC
     * @param ejbName The name of the EJB on wich look role
     * @param roleName The name of the security role. The role must be one of
     *        the security-role-ref that is defined in the deployment
     *        descriptor.
     * @param inRunAs bean calling this method is running in run-as mode or not ?
     * @return True if the caller has the specified role.
     */
    public boolean isCallerInRole(final String ejbName, final String roleName, final boolean inRunAs) {
        PolicyContext.setContextID(getContextId());
        this.logger.debug("roleName = {0}", roleName);

        // Build Protection Domain with a codesource and array of principals
        Principal[] principals = SecurityCurrent.getCurrent().getSecurityContext().getCallerRoles(inRunAs);
        ProtectionDomain protectionDomain = new ProtectionDomain(this.codeSource, null, null, principals);

        // TODO :add cache mechanism ?
        // See JACC 4.12
        EJBRoleRefPermission ejbRoleRefPermission = new EJBRoleRefPermission(ejbName, roleName);
        boolean isInRole = getPolicy().implies(protectionDomain, ejbRoleRefPermission);
        return isInRole;

    }

}
