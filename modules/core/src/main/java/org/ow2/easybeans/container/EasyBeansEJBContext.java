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
 * $Id: EasyBeansEJBContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container;


import static javax.ejb.TransactionManagementType.BEAN;
import static javax.ejb.TransactionManagementType.CONTAINER;
import static org.ow2.easybeans.api.OperationState.AFTER_COMPLETION;
import static org.ow2.easybeans.api.OperationState.DEPENDENCY_INJECTION;
import static org.ow2.easybeans.api.OperationState.LIFECYCLE_CALLBACK_INTERCEPTOR;

import java.io.Serializable;
import java.security.Identity;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TransactionManagementType;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.OperationState;
import org.ow2.easybeans.api.bean.info.ISecurityInfo;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.container.session.stateless.StatelessSessionFactory;
import org.ow2.easybeans.security.propagation.context.SecurityCurrent;
import org.ow2.easybeans.transaction.JTransactionManager;
import org.ow2.easybeans.transaction.interceptors.CMTSupportsTransactionInterceptor;
import org.ow2.util.ee.metadata.common.api.xml.struct.ISecurityRoleRef;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Class implementing the EJBContext interface.
 * It is extended for Session context or MessageDriven Context
 * @param <FactoryType> The type of bean managed.
 * @author Florent Benoit
  */
public class EasyBeansEJBContext<FactoryType extends Factory<?, ?>> implements EZBEJBContext<FactoryType>, EJBContext {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(EasyBeansEJBContext.class);

    /**
     * java:comp/env prefix.
     */
    private static final String JAVA_COMP_ENV = "java:comp/env/";

    /**
     * Reference to the transaction manager.
     */
    private TransactionManager transactionManager = null;

    /**
     * Type of transaction.
     */
    private TransactionManagementType transactionManagementType = null;

    /**
     * Bean is using run-as ?
     */
    private boolean runAsBean = false;

    /**
     * Link to the factory.
     */
    private FactoryType easyBeansFactory = null;


    /**
     * Timer service.
     */
    private TimerService timerService = null;

    /**
     * Builds a default EJB Context implementation.
     * @param easyBeansFactory used to get the transaction management type.
     */
    public EasyBeansEJBContext(final FactoryType easyBeansFactory) {
        this.easyBeansFactory = easyBeansFactory;
        this.transactionManagementType = easyBeansFactory.getBeanInfo().getTransactionManagementType();
        this.runAsBean = easyBeansFactory.getBeanInfo().getSecurityInfo().getRunAsRole() != null;

        // Get Transaction manager
        this.transactionManager = JTransactionManager.getTransactionManager();

        // Get timer service
        this.timerService = easyBeansFactory.getTimerService();

        // If the component is not here, use a dummy implementation
        if (this.timerService == null) {
            this.timerService = new MissingTimerService();
        }
    }

    /**
     * Obtain the enterprise bean's remote home interface.
     * @return The enterprise bean's remote home interface.
     * @throws IllegalStateException if the enterprise bean does not have a
     *         remote home interface.
     */
    public EJBHome getEJBHome() throws IllegalStateException {
        throw new IllegalStateException("No Home");
    }

    /**
     * Obtain the enterprise bean's local home interface.
     * @return The enterprise bean's local home interface.
     * @throws IllegalStateException - if the enterprise bean does not have a
     *         local home interface.
     */
    public EJBLocalHome getEJBLocalHome() throws IllegalStateException {
        throw new IllegalStateException("No Local Home");
    }

    /**
     * Use the JNDI naming context java:comp/env to access enterprise bean's
     * environment. Obtain the enterprise bean's environment properties. Note:
     * If the enterprise bean has no environment properties this method returns
     * an empty java.util.Properties object. This method never returns null.
     * @return The environment properties for the enterprise bean.
     */
    @Deprecated
    public Properties getEnvironment() {
        throw new UnsupportedOperationException();
    }

    /**
     * Use Principal getCallerPrincipal() instead. Obtain the
     * java.security.Identity of the caller. This method is deprecated in EJB
     * 1.1. The Container is allowed to return alway null from this method. The
     * enterprise bean should use the getCallerPrincipal method instead.
     * @return The Identity object that identifies the caller.
     */
    @Deprecated
    public Identity getCallerIdentity() {
        throw new UnsupportedOperationException();
    }

    /**
     * Obtain the java.security.Principal that identifies the caller.
     * @return The Principal object that identifies the caller. This method
     *         never returns null.
     */
    public Principal getCallerPrincipal() {
        // Disallowed from dependency injection and from stateless lifecycle
        // callbacks
        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState
                || (LIFECYCLE_CALLBACK_INTERCEPTOR == operationState && getFactory() instanceof StatelessSessionFactory)) {
            throw new IllegalStateException("The getCallerPrincipal() method cannot be called within the operation state '"
                    + operationState + "'.");
        }
        return SecurityCurrent.getCurrent().getSecurityContext().getCallerPrincipal(this.runAsBean);
    }

    /**
     * Use boolean isCallerInRole(String roleName) instead. Test if the caller
     * has a given role. This method is deprecated in EJB 1.1. The enterprise
     * bean should use the isCallerInRole(String roleName) method instead.
     * @param role The java.security.Identity of the role to be tested.
     * @return True if the caller has the specified role.
     */
    @Deprecated
    public boolean isCallerInRole(final Identity role) {
        throw new UnsupportedOperationException();
    }

    /**
     * Test if the caller has a given security role.
     * @param roleName The name of the security role. The role must be one of
     *        the security roles that is defined in the deployment descriptor.
     * @return True if the caller has the specified role.
     */
    public boolean isCallerInRole(final String roleName) {
        // Disallowed from dependency injection and from stateless lifecycle
        // callbacks
        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState
                || (LIFECYCLE_CALLBACK_INTERCEPTOR == operationState && getFactory() instanceof StatelessSessionFactory)) {
            throw new IllegalStateException("The isCallerInRole() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        // Get list of declared roles for this bean.
        ISecurityInfo securityInfo = this.easyBeansFactory.getBeanInfo().getSecurityInfo();
        List<String> declaredRoles = securityInfo.getDeclaredRoles();
        if (declaredRoles == null) {
            declaredRoles = new ArrayList<String>();
        }
        // Add also all security roles declared as security-role-ref
        List<ISecurityRoleRef> securityRoleRefList = securityInfo.getSecurityRoleRefList();
        if (securityRoleRefList != null) {
            for (ISecurityRoleRef securityRoleRef : securityRoleRefList) {
                if (!declaredRoles.contains(securityRoleRef.getRoleName())) {
                    declaredRoles.add(securityRoleRef.getRoleName());
                }
            }
        }

        // Not contained ?
        if (!declaredRoles.contains(roleName)) {
            this.logger.debug("No security-role with role name ''{0}'' was declared for the bean ''{1}''", roleName,
                    this.easyBeansFactory.getBeanInfo().getName());
            return false;
        }

        // Check with JACC manager
        boolean inRole = this.easyBeansFactory.getContainer().getPermissionManager().isCallerInRole(this.easyBeansFactory
                .getBeanInfo().getName(), roleName, this.runAsBean);

        return inRole;
    }

    /**
     * Obtain the transaction demarcation interface. Only enterprise beans with
     * bean-managed transactions are allowed to to use the UserTransaction
     * interface. As entity beans must always use container-managed
     * transactions, only session beans with bean-managed transactions are
     * allowed to invoke this method.
     * @return The UserTransaction interface that the enterprise bean instance
     *         can use for transaction demarcation.
     * @throws java.lang.IllegalStateException - The Container throws the
     *         exception if the instance is not allowed to use the
     *         UserTransaction interface (i.e. the instance is of a bean with
     *         container-managed transactions).
     */
    public UserTransaction getUserTransaction() throws IllegalStateException {
        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState) {
            throw new IllegalStateException("The getUserTransaction() method cannot be called within the operation state '"
                    + operationState + "'.");
        }

        if (this.transactionManagementType == CONTAINER) {
            throw new IllegalStateException("This bean is not allowed to use getUserTransaction() "
                    + " method as it is in ContainerManagedTransaction");
        }
        return (UserTransaction) this.transactionManager;
    }

    /**
     * Mark the current transaction for rollback. The transaction will become
     * permanently marked for rollback. A transaction marked for rollback can
     * never commit. Only enterprise beans with container-managed transactions
     * are allowed to use this method.
     * @throws java.lang.IllegalStateException - The Container throws the
     *         exception if the instance is not allowed to use this method (i.e.
     *         the instance is of a bean with bean-managed transactions).
     */
    public void setRollbackOnly() throws IllegalStateException {
        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The setRollbackOnly() method cannot be called within the operation state '"
                    + operationState + "'.");
        }
        if (this.transactionManagementType == BEAN) {
            throw new IllegalStateException("This bean is not allowed to use setRollbackOnly() "
                    + " method as it is in BeanManagedTransaction");
        }

        Object value = getContextData().get(CMTSupportsTransactionInterceptor.class.getName());
        if (value != null && ((Boolean) value).booleanValue()) {
            throw new IllegalStateException("This bean is not allowed to use getRollbackOnly() "
                    + " method as it is in a SUPPORTS call");
        }

        // Check if there is a transaction, as it is mandatory
        try {
            if (this.transactionManager.getTransaction() == null) {
                throw new IllegalStateException("Cannot use setRollbackOnly() outside transaction");
            }
        } catch (SystemException e) {
            throw new IllegalStateException("Cannot get transaction on transaction manager", e);
        }


        try {
            this.transactionManager.setRollbackOnly();
        } catch (SystemException e) {
            throw new RuntimeException("setRollbackOnly() raised an unexpected exception:", e);
        }
    }

    /**
     * Test if the transaction has been marked for rollback only. An enterprise
     * bean instance can use this operation, for example, to test after an
     * exception has been caught, whether it is fruitless to continue
     * computation on behalf of the current transaction. Only enterprise beans
     * with container-managed transactions are allowed to use this method.
     * @return True if the current transaction is marked for rollback, false
     *         otherwise.
     * @throws java.lang.IllegalStateException - The Container throws the
     *         exception if the instance is not allowed to use this method (i.e.
     *         the instance is of a bean with bean-managed transactions).
     */
    public boolean getRollbackOnly() throws IllegalStateException {
        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState || LIFECYCLE_CALLBACK_INTERCEPTOR == operationState
                || AFTER_COMPLETION == operationState) {
            throw new IllegalStateException("The getRollbackOnly() method cannot be called within the operation state '"
                    + operationState + "'.");
        }
        if (this.transactionManagementType == BEAN) {
            throw new IllegalStateException("This bean is not allowed to use getRollbackOnly() "
                    + " method as it is in BeanManagedTransaction");
        }

        Object value = getContextData().get(CMTSupportsTransactionInterceptor.class.getName());
        if (value != null && ((Boolean) value).booleanValue()) {
            throw new IllegalStateException("This bean is not allowed to use getRollbackOnly() "
                    + " method as it is in a SUPPORTS call");
        }


        try {
            switch (this.transactionManager.getStatus()) {
                case Status.STATUS_MARKED_ROLLBACK:
                case Status.STATUS_ROLLING_BACK:
                    return true;
                case Status.STATUS_ACTIVE:
                case Status.STATUS_COMMITTING:
                case Status.STATUS_PREPARED:
                case Status.STATUS_PREPARING:
                    return false;
                case Status.STATUS_ROLLEDBACK:
                    throw new IllegalStateException("Transaction already rolled back");
                case Status.STATUS_COMMITTED:
                    throw new IllegalStateException("Transaction already committed");
                case Status.STATUS_NO_TRANSACTION:
                case Status.STATUS_UNKNOWN:
                    throw new IllegalStateException("Cannot getRollbackOnly outside transaction");
                default:
                    throw new IllegalStateException("Invalid status");
            }
        } catch (SystemException e) {
            throw new IllegalStateException("Cannot get transaction status", e);
        }
    }

    /**
     * Get access to the EJB Timer Service.
     * @return Timer service.
     * @throws java.lang.IllegalStateException The Container throws the
     *         exception if the instance is not allowed to use this method (e.g.
     *         if the bean is a stateful session bean)
     */
    public TimerService getTimerService() throws IllegalStateException {
        // Disallowed from stateful
        if (getFactory() instanceof StatefulSessionFactory) {
            throw new IllegalStateException("The getTimerService() method cannot be called from a stateful session bean.");
        }

        OperationState operationState = getFactory().getOperationState();
        if (DEPENDENCY_INJECTION == operationState) {
            throw new IllegalStateException("The getTimerService() method cannot be called within the operation state '"
                    + operationState + "'.");
        }
        return getInternalTimerService();
    }

    /**
     * Get access to the EJB Timer Service.
     * @return Timer service.
     */
    public TimerService getInternalTimerService() {
        return this.timerService;
    }

    /**
     * @return string representation.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        // classname
        sb.append(this.getClass().getName().substring(this.getClass().getPackage().getName().length() + 1));
        return sb.toString();
    }

    /**
     * Lookup object with given name.
     * @param name given name
     * @return result of the lookup
     */
    public Object lookup(final String name) {
        if (name == null) {
            throw new IllegalArgumentException("Invalid resource name used for lookup '" + name + "'.");
        }


        // Search in java:comp/env first
        try {
            return new InitialContext().lookup(JAVA_COMP_ENV + name);
        } catch (NamingException ne) {
            // try in registry
            try {
                return new InitialContext().lookup(name);
            } catch (NamingException e) {
                throw new IllegalArgumentException("Lookup on '" + name + "' was not found");
            }
        }
    }


    /**
     * Gets the factory of this context.
     * @return factory used by this context.
     */
    public FactoryType getFactory() {
        return this.easyBeansFactory;
    }

    /**
     * Enables a business method, lifecycle callback method, or timeout method to retrieve any interceptor/webservices context
     * associated with its invocation.
     * @return the shared context
     * @since EJB 3.1 version.
     */
    public Map<String, Object> getContextData() {
        return this.easyBeansFactory.getContextDataThreadLocal().get();
    }

    /**
     * Implementation of Timer Service that throws exception as
     * it means that the timer component is missing.
     * @author Florent Benoit
     */
    class MissingTimerService implements TimerService {

        /**
         * Create an interval timer whose first expiration occurs after a
         * specified duration, and whose subsequent expirations occur after a
         * specified interval.
         * @param initialDuration - The number of milliseconds that must elapse
         *        before the first timer expiration notification.
         * @param intervalDuration - The number of milliseconds that must elapse
         *        between timer expiration notifications. Expiration
         *        notifications are scheduled relative to the time of the first
         *        expiration. If expiration is delayed(e.g. due to the
         *        interleaving of other method calls on the bean) two or more
         *        expiration notifications may occur in close succession to
         *        "catch up".
         * @param info - Application information to be delivered along with the
         *        timer expiration. This can be null.
         * @return The newly created Timer.
         * @throws IllegalArgumentException - If initialDuration is negative, or
         *         intervalDuration is negative.
         * @throws IllegalStateException - If this method is invoked while the
         *         instance is in a state that does not allow access to this
         *         method.
         * @throws EJBException - If this method could not complete due to a
         *         system-level failure.
         */
        public Timer createTimer(final Date initialDuration, final long intervalDuration, final Serializable info)
                throws IllegalArgumentException, IllegalStateException, EJBException {
            throw new IllegalStateException("No timer component was found in the EasyBeans components");
        }

        /**
         * Create a single-action timer that expires at a given point in time.
         * @param expiration - The point in time at which the timer must expire.
         * @param info - Application information to be delivered along with the
         *        timer expiration notification. This can be null.
         * @return The newly created Timer.
         * @throws IllegalArgumentException - If expiration is null, or
         *         expiration.getTime() is negative.
         * @throws IllegalStateException - If this method is invoked while the
         *         instance is in a state that does not allow access to this
         *         method.
         * @throws EJBException - If this method could not complete due to a
         *         system-level failure.
         */
        public Timer createTimer(final Date expiration, final Serializable info) throws IllegalArgumentException,
                IllegalStateException, EJBException {
            throw new IllegalStateException("No timer component was found in the EasyBeans components");
        }

        /**
         * Create an interval timer whose first expiration occurs after a
         * specified duration, and whose subsequent expirations occur after a
         * specified interval.
         * @param initialDuration - The number of milliseconds that must elapse
         *        before the first timer expiration notification.
         * @param intervalDuration - The number of milliseconds that must elapse
         *        between timer expiration notifications. Expiration
         *        notifications are scheduled relative to the time of the first
         *        expiration. If expiration is delayed(e.g. due to the
         *        interleaving of other method calls on the bean) two or more
         *        expiration notifications may occur in close succession to
         *        "catch up".
         * @param info - Application information to be delivered along with the
         *        timer expiration. This can be null.
         * @return The newly created Timer.
         * @throws IllegalArgumentException - If initialDuration is negative, or
         *         intervalDuration is negative.
         * @throws IllegalStateException - If this method is invoked while the
         *         instance is in a state that does not allow access to this
         *         method.
         * @throws EJBException - If this method could not complete due to a
         *         system-level failure.
         */
        public Timer createTimer(final long initialDuration, final long intervalDuration, final Serializable info)
                throws IllegalArgumentException, IllegalStateException, EJBException {
            throw new IllegalStateException("No timer component was found in the EasyBeans components");
        }

        /**
         * Create a single-action timer that expires after a specified duration.
         * @param duration - The number of milliseconds that must elapse before
         *        the timer expires.
         * @param info - Application information to be delivered along with the
         *        timer expiration notification. This can be null.
         * @return The newly created Timer.
         * @throws IllegalArgumentException - If duration is negative
         * @throws IllegalStateException - If this method is invoked while the
         *         instance is in a state that does not allow access to this
         *         method.
         * @throws EJBException - If this method fails due to a system-level
         *         failure.
         */
        public Timer createTimer(final long duration, final Serializable info) throws IllegalArgumentException,
                IllegalStateException, EJBException {
            throw new IllegalStateException("No timer component was found in the EasyBeans components");
        }

        /**
         * Get all the active timers associated with this bean.
         * @return A collection of javax.ejb.Timer objects.
         * @throws IllegalStateException - If this method is invoked while the
         *         instance is in a state that does not allow access to this
         *         method.
         * @throws EJBException - If this method could not complete due to a
         *         system-level failure.
         */
        public Collection<Timer> getTimers() throws IllegalStateException, EJBException {
            return Collections.emptyList();
        }

    }
}
