/**
 * EasyBeans
 * Copyright (C) 2009-2010 Bull S.A.S.
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
 * $Id: ContainerManagedEntityManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.persistence;

import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.FlushModeType;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.OptimisticLockException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;
import javax.persistence.Query;
import javax.persistence.TransactionRequiredException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.metamodel.Metamodel;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.ow2.easybeans.transaction.JTransactionManager;

/**
 * Wrap an entity manager and check that operations are authorized in Container
 * mode.
 * @author Florent Benoit
 */
public class ContainerManagedEntityManager implements EntityManager {

    /**
     * Wrapped Entity Manager.
     */
    private EntityManager wrappedEntityManager = null;

    /**
     * Wrap an entity manager.
     * @param entityManager the wrapped entity manager
     */
    public ContainerManagedEntityManager(final EntityManager entityManager) {
        this.wrappedEntityManager = entityManager;
    }

    /**
     * Make an instance managed and persistent.
     * @param entity entity bean.
     * @throws IllegalArgumentException if not an entity or entity is detached
     * @throws TransactionRequiredException if there is no transaction and the
     *         persistence context is of type PersistenceContextType.TRANSACTION
     */
    public void persist(final Object entity) throws IllegalArgumentException, TransactionRequiredException {
        checkTransaction();
        this.wrappedEntityManager.persist(entity);
    }

    /**
     * Merge the state of the given entity into the current persistence context.
     * @param entity entity bean
     * @param <T> entity object's class.
     * @return the instance that the state was merged to
     * @throws IllegalArgumentException if instance is not an entity or is a
     *         removed entity
     * @throws TransactionRequiredException if there is no transaction and the
     *         persistence context is of type PersistenceContextType.TRANSACTION
     */
    public <T> T merge(final T entity) throws IllegalArgumentException, TransactionRequiredException {
        checkTransaction();
        return this.wrappedEntityManager.merge(entity);
    }

    /**
     * Remove the entity instance.
     * @param entity entity bean
     * @throws IllegalArgumentException if not an entity or if a detached entity
     * @throws TransactionRequiredException if there is no transaction and the
     *         persistence context is of type PersistenceContextType.TRANSACTION
     */
    public void remove(final Object entity) throws IllegalArgumentException, TransactionRequiredException {
        checkTransaction();
        this.wrappedEntityManager.remove(entity);
    }

    /**
     * Find by primary key.
     * @param <T> entity object's class.
     * @param entityClass the class of the entity
     * @param primaryKey the primary key
     * @return the found entity instance or null if the entity does not exist
     * @throws IllegalArgumentException if the first argument does not denote an
     *         entity type or the second argument is not a valid type for that
     *         entity?s primary key
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey) throws IllegalArgumentException {
        return this.wrappedEntityManager.find(entityClass, primaryKey);
    }

    /**
     * Get an instance, whose state may be lazily fetched. If the requested
     * instance does not exist in the database, throws EntityNotFoundException
     * when the instance state is first accessed. (The persistence provider
     * runtime is permitted to throw the EntityNotFoundException when
     * getReference is called.) The application should not expect that the
     * instance state will be available upon detachment, unless it was accessed
     * by the application while the entity manager was open.
     * @param <T> entity object's class.
     * @param entityClass the class of the entity
     * @param primaryKey the primary key
     * @return the found entity instance
     * @throws IllegalArgumentException if the first argument does not denote an
     *         entity type or the second argument is not a valid type for that
     *         entity?s primary key
     * @throws EntityNotFoundException if the entity state cannot be accessed
     */
    public <T> T getReference(final Class<T> entityClass, final Object primaryKey) throws IllegalArgumentException,
            EntityNotFoundException {
        return this.wrappedEntityManager.getReference(entityClass, primaryKey);
    }

    /**
     * Synchronize the persistence context to the underlying database.
     * @throws TransactionRequiredException if there is no transaction
     * @throws PersistenceException if the flush fails
     */
    public void flush() throws TransactionRequiredException, PersistenceException {
        this.wrappedEntityManager.flush();
    }

    /**
     * Set the flush mode that applies to all objects contained in the
     * persistence context.
     * @param flushMode the mode of flushing
     */
    public void setFlushMode(final FlushModeType flushMode) {
        this.wrappedEntityManager.setFlushMode(flushMode);

    }

    /**
     * Get the flush mode that applies to all objects contained in the
     * persistence context.
     * @return flushMode
     */
    public FlushModeType getFlushMode() {
        return this.wrappedEntityManager.getFlushMode();
    }

    /**
     * Set the lock mode for an entity object contained in the persistence
     * context.
     * @param entity entity bean
     * @param lockMode mode for locking
     * @throws PersistenceException if an unsupported lock call is made
     * @throws IllegalArgumentException if the instance is not an entity or is a
     *         detached entity
     * @throws TransactionRequiredException if there is no transaction
     */
    public void lock(final Object entity, final LockModeType lockMode) throws PersistenceException, IllegalArgumentException,
            TransactionRequiredException {
        this.wrappedEntityManager.lock(entity, lockMode);
    }

    /**
     * Refresh the state of the instance from the database, overwriting changes
     * made to the entity, if any.
     * @param entity entity bean
     * @throws IllegalArgumentException if not an entity or entity is not
     *         managed
     * @throws TransactionRequiredException if there is no transaction and the
     *         persistence context is of type PersistenceContextType.TRANSACTION
     * @throws EntityNotFoundException if the entity no longer exists in the
     *         database
     */
    public void refresh(final Object entity) throws IllegalArgumentException, TransactionRequiredException,
            EntityNotFoundException {
        checkTransaction();
        this.wrappedEntityManager.refresh(entity);
    }

    /**
     * Clear the persistence context, causing all managed entities to become
     * detached. Changes made to entities that have not been flushed to the
     * database will not be persisted.
     */
    public void clear() {
        this.wrappedEntityManager.clear();
    }

    /**
     * Check if the instance belongs to the current persistence context.
     * @param entity the entity bean
     * @return true/false
     * @throws IllegalArgumentException if not an entity
     */
    public boolean contains(final Object entity) throws IllegalArgumentException {
        return this.wrappedEntityManager.contains(entity);
    }

    /**
     * Create an instance of Query for executing an EJB QL statement.
     * @param ejbqlString an EJB QL query string
     * @return the new query instance
     * @throws IllegalArgumentException if query string is not valid
     */
    public Query createQuery(final String ejbqlString) throws IllegalArgumentException {
        return this.wrappedEntityManager.createQuery(ejbqlString);
    }

    /**
     * Create an instance of Query for executing a named query (in EJB QL or
     * native SQL).
     * @param name the name of a query defined in metadata
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been defined with the
     *         given name
     */
    public Query createNamedQuery(final String name) throws IllegalArgumentException {
        return this.wrappedEntityManager.createNamedQuery(name);
    }

    /**
     * Create an instance of Query for executing a native SQL statement, e.g.,
     * for update or delete.
     * @param sqlString a native SQL query string
     * @return the new query instance
     */
    public Query createNativeQuery(final String sqlString) {
        return this.wrappedEntityManager.createNativeQuery(sqlString);
    }

    /**
     * Create an instance of Query for executing a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultClass the class of the resulting instance(s)
     * @return the new query instance
     */
    public Query createNativeQuery(final String sqlString, final Class resultClass) {
        return this.wrappedEntityManager.createNativeQuery(sqlString, resultClass);
    }

    /**
     * Create an instance of Query for executing a native SQL query.
     * @param sqlString a native SQL query string
     * @param resultSetMapping the name of the result set mapping
     * @return the new query instance
     */
    public Query createNativeQuery(final String sqlString, final String resultSetMapping) {
        return this.wrappedEntityManager.createNativeQuery(sqlString, resultSetMapping);
    }

    /**
     * Indicate to the EntityManager that a JTA transaction is active. This
     * method should be called on a JTA application managed EntityManager that
     * was created outside the scope of the active transaction to associate it
     * with the current JTA transaction.
     * @throws IllegalStateException if this EntityManager has been closed.
     * @throws TransactionRequiredException if there is no transaction.
     */
    public void joinTransaction() throws IllegalStateException, TransactionRequiredException {
        this.wrappedEntityManager.joinTransaction();
    }

    /**
     * Return the underlying provider object for the EntityManager, if
     * available. The result of this method is implementation specific.
     * @throws IllegalStateException if this EntityManager has been closed.
     * @return The underlying provider object for the EntityManager.
     */
    public Object getDelegate() throws IllegalStateException {
        return this.wrappedEntityManager.getDelegate();
    }

    /**
     * Close an application-managed EntityManager. After an EntityManager has
     * been closed, all methods on the EntityManager instance will throw the
     * IllegalStateException except for isOpen, which will return false. This
     * method can only be called when the EntityManager is not associated with
     * an active transaction.
     * @throws IllegalStateException if the EntityManager is associated with an
     *         active transaction or if the EntityManager is container-managed.
     */
    public void close() throws IllegalStateException {
        throw new IllegalStateException("This entity manager is a container managed entity manager. This operation is denied.");
    }

    /**
     * Determine whether the EntityManager is open.
     * @return true until the EntityManager has been closed.
     */
    public boolean isOpen() {
        return this.wrappedEntityManager.isOpen();
    }

    /**
     * Return the resource-level transaction object. The EntityTransaction
     * instance may be used serially to begin and commit multiple transactions.
     * @return EntityTransaction instance
     * @throws IllegalStateException if invoked on a JTA EntityManager or an
     *         EntityManager that has been closed.
     */
    public EntityTransaction getTransaction() throws IllegalStateException {
        return this.wrappedEntityManager.getTransaction();
    }

    /**
     * Create an instance of TypedQuery for executing a Java Persistence query
     * language named query. The select list of the query must contain only a
     * single item, which must be assignable to the type specified by the
     * resultClass argument.[27]
     * @param name the name of a query defined in metadata
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if a query has not been defined with the
     *         given name or if the query string is found to be invalid or if
     *         the query result is found to not be assignable to the specified
     *         type
     */
    public <T> TypedQuery<T> createNamedQuery(final String name, final Class<T> resultClass) throws IllegalArgumentException {
        return this.wrappedEntityManager.createNamedQuery(name, resultClass);
    }

    /**
     * Create an instance of TypedQuery for executing a criteria query.
     * @param criteriaQuery a criteria query object
     * @return the new query instance
     * @throws IllegalArgumentException if the criteria query is found to be
     *         invalid
     */
    public <T> TypedQuery<T> createQuery(final CriteriaQuery<T> criteriaQuery) throws IllegalArgumentException {
        return this.wrappedEntityManager.createQuery(criteriaQuery);
    }

    /**
     * Create an instance of TypedQuery for executing a Java Persistence query
     * language statement. The select list of the query must contain only a
     * single item, which must be assignable to the type specified by the
     * resultClass argument.[26]
     * @param qlString a Java Persistence query string
     * @param resultClass the type of the query result
     * @return the new query instance
     * @throws IllegalArgumentException if the query string is found to be
     *         invalid or if the query result is found to not be assignable to
     *         the specified type
     */
    public <T> TypedQuery<T> createQuery(final String qlString, final Class<T> resultClass) throws IllegalArgumentException {
        return this.wrappedEntityManager.createQuery(qlString, resultClass);
    }

    /**
     * Remove the given entity from the persistence context, causing a managed
     * entity to become detached. Unflushed changes made to the entity if any
     * (including removal of the entity), will not be synchronized to the
     * database. Entities which previously referenced the detached entity will
     * continue to reference it.
     * @param entity
     * @throws IllegalArgumentException if the instance is not an entity
     */
    public void detach(final Object entity) throws IllegalArgumentException {
        this.wrappedEntityManager.detach(entity);
    }

    /**
     * Find by primary key, using the specified properties. Search for an entity
     * of the specified class and primary key. If the entity instance is
     * contained in the persistence context it is returned from there. If a
     * vendor-specific property or hint is not recognized, it is silently
     * ignored.
     * @param entityClass
     * @param primaryKey
     * @param properties standard and vendor-specific properties and hints
     * @return the found entity instance or null if the entity does not exist
     * @throws IllegalArgumentException if the first argument does not denote an
     *         entity type or the second argument is is not a valid type for
     *         that entity’s primary key or is null
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final Map<String, Object> properties)
            throws IllegalArgumentException {
        return this.wrappedEntityManager.find(entityClass, primaryKey, properties);
    }

    /**
     * Find by primary key and lock. Search for an entity of the specified class
     * and primary key and lock it with respect to the specified lock type. If
     * the entity instance is contained in the persistence context it is
     * returned from there, and the effect of this method is the same as if the
     * lock method had been called on the entity. If the entity is found within
     * the persistence context and the lock mode type is pessimistic and the
     * entity has a version attribute, the persistence provider must perform
     * optimistic version checks when obtaining the database lock. If these
     * checks fail, the OptimisticLockException will be thrown. If the lock mode
     * type is pessimistic and the entity instance is found but cannot be
     * locked: - the PessimisticLockException will be thrown if the database
     * locking failure causes transaction-level rollback - the
     * LockTimeoutException will be thrown if the database locking failure
     * causes only statement-level rollback
     * @param entityClass
     * @param primaryKey
     * @param lockMode
     * @return the found entity instance or null if the entity does not exist
     * @throws IllegalArgumentException if the first argument does not denote an
     *         entity type or the second argument is not a valid type for that
     *         entity's primary key or is null
     * @throws TransactionRequiredException if there is no transaction and a
     *         lock mode other than NONE is specified
     * @throws OptimisticLockException if the optimistic version check fails
     * @throws PessimisticLockException if pessimistic locking fails and the
     *         transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only the
     *         statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode)
            throws IllegalArgumentException, TransactionRequiredException, OptimisticLockException, PessimisticLockException,
            LockTimeoutException, PersistenceException {
        return this.wrappedEntityManager.find(entityClass, primaryKey, lockMode);
    }

    /**
     * Find by primary key and lock, using the specified properties. Search for
     * an entity of the specified class and primary key and lock it with respect
     * to the specified lock type. If the entity instance is contained in the
     * persistence context it is returned from there. If the entity is found
     * within the persistence context and the lock mode type is pessimistic and
     * the entity has a version attribute, the persistence provider must perform
     * optimistic version checks when obtaining the database lock. If these
     * checks fail, the OptimisticLockException will be thrown. If the lock mode
     * type is pessimistic and the entity instance is found but cannot be
     * locked: - the PessimisticLockException will be thrown if the database
     * locking failure causes transaction-level rollback - the
     * LockTimeoutException will be thrown if the database locking failure
     * causes only statement-level rollback If a vendor-specific property or
     * hint is not recognized, it is silently ignored. Portable applications
     * should not rely on the standard timeout hint. Depending on the database
     * in use and the locking mechanisms used by the provider, the hint may or
     * may not be observed.
     * @param entityClass
     * @param primaryKey
     * @param lockMode
     * @param properties standard and vendor-specific properties and hints
     * @return the found entity instance or null if the entity does not exist
     * @throws IllegalArgumentException if the first argument does not denote an
     *         entity type or the second argument is not a valid type for that
     *         entity's primary key or is null
     * @throws TransactionRequiredException if there is no transaction and a
     *         lock mode other than NONE is specified
     * @throws OptimisticLockException if the optimistic version check fails
     * @throws PessimisticLockException if pessimistic locking fails and the
     *         transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only the
     *         statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    public <T> T find(final Class<T> entityClass, final Object primaryKey, final LockModeType lockMode,
            final Map<String, Object> properties) throws IllegalArgumentException, TransactionRequiredException,
            OptimisticLockException, PessimisticLockException, LockTimeoutException, PersistenceException {
        return this.wrappedEntityManager.find(entityClass, primaryKey, lockMode, properties);
    }

    /**
     * Return an instance of CriteriaBuilder for the creation of CriteriaQuery
     * objects.
     * @return CriteriaBuilder instance
     * @throws IllegalStateException if the entity manager has been closed
     */
    public CriteriaBuilder getCriteriaBuilder() throws IllegalStateException {
        return this.wrappedEntityManager.getCriteriaBuilder();
    }

    /**
     * Return the entity manager factory for the entity manager.
     * @return EntityManagerFactory instance
     * @throws IllegalStateException if the entity manager has been closed
     */
    public EntityManagerFactory getEntityManagerFactory() throws IllegalStateException {
        return this.wrappedEntityManager.getEntityManagerFactory();
    }

    /**
     * Get the current lock mode for the entity instance.
     * @param entity
     * @return lock mode
     * @throws TransactionRequiredException if there is no transaction
     * @throws IllegalArgumentException if the instance is not a managed entity
     *         and a transaction is active
     */
    public LockModeType getLockMode(final Object entity) throws TransactionRequiredException, IllegalArgumentException {
        return this.wrappedEntityManager.getLockMode(entity);
    }

    /**
     * Return an instance of Metamodel interface for access to the metamodel of
     * the persistence unit.
     * @return Metamodel instance
     * @throws IllegalStateException if the entity manager has been closed
     */
    public Metamodel getMetamodel() throws IllegalStateException {
        return this.wrappedEntityManager.getMetamodel();
    }

    /**
     * Get the properties and hints and associated values that are in effect for
     * the entity manager. Changing the contents of the map does not change the
     * configuration in effect.
     * @return map of properties and hints in effect
     */
    public Map<String, Object> getProperties() {
        return this.wrappedEntityManager.getProperties();
    }

    /**
     * Lock an entity instance that is contained in the persistence context with
     * the specified lock mode type and with specified properties. If a
     * pessimistic lock mode type is specified and the entity contains a version
     * attribute, the persistence provider must also perform optimistic version
     * checks when obtaining the database lock. If these checks fail, the
     * OptimisticLockException will be thrown. If the lock mode type is
     * pessimistic and the entity instance is found but cannot be locked: - the
     * PessimisticLockException will be thrown if the database locking failure
     * causes transaction-level rollback - the LockTimeoutException will be
     * thrown if the database locking failure causes only statement-level
     * rollback If a vendor-specific property or hint is not recognized, it is
     * silently ignored. Portable applications should not rely on the standard
     * timeout hint. Depending on the database in use and the locking mechanisms
     * used by the provider, the hint may or may not be observed.
     * @param entity
     * @param lockMode
     * @param properties standard and vendor-specific properties and hints
     * @throws IllegalArgumentException if the instance is not an entity or is a
     *         detached entity
     * @throws TransactionRequiredException if there is no transaction
     * @throws EntityNotFoundException if the entity does not exist in the
     *         database when pessimistic locking is performed
     * @throws OptimisticLockException if the optimistic version check fails
     * @throws PessimisticLockException if pessimistic locking fails and the
     *         transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only the
     *         statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    public void lock(final Object entity, final LockModeType lockMode, final Map<String, Object> properties)
            throws IllegalArgumentException, TransactionRequiredException, EntityNotFoundException, OptimisticLockException,
            PessimisticLockException, LockTimeoutException, PersistenceException {
        this.wrappedEntityManager.lock(entity, lockMode, properties);

    }

    /**
     * Refresh the state of the instance from the database, using the specified
     * properties, and overwriting changes made to the entity, if any. If a
     * vendor-specific property or hint is not recognized, it is silently
     * ignored.
     * @param entity
     * @param properties standard and vendor-specific properties and hints
     * @throws IllegalArgumentException if the instance is not an entity or the
     *         entity is not managed
     * @throws TransactionRequiredException if invoked on a container-managed
     *         entity manager of type PersistenceContextType.TRANSACTION and
     *         there is no transaction
     * @throws EntityNotFoundException if the entity no longer exists in the
     *         database
     */
    public void refresh(final Object entity, final Map<String, Object> properties) throws IllegalArgumentException,
            TransactionRequiredException, EntityNotFoundException {
        this.wrappedEntityManager.refresh(entity, properties);

    }

    /**
     * Refresh the state of the instance from the database, overwriting changes
     * made to the entity, if any, and lock it with respect to given lock mode
     * type. If the lock mode type is pessimistic and the entity instance is
     * found but cannot be locked: - the PessimisticLockException will be thrown
     * if the database locking failure causes transaction-level rollback - the
     * LockTimeoutException will be thrown if the database locking failure
     * causes only statement-level rollback.
     * @param entity
     * @param lockMode
     * @throws IllegalArgumentException if the instance is not an entity or the
     *         entity is not managed
     * @throws TransactionRequiredException if there is no transaction and if
     *         invoked on a container-managed EntityManager instance with
     *         PersistenceContextType.TRANSACTION or with a lock mode other than
     *         NONE
     * @throws EntityNotFoundException if the entity no longer exists in the
     *         database
     * @throws PessimisticLockException if pessimistic locking fails and the
     *         transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only the
     *         statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    public void refresh(final Object entity, final LockModeType lockMode) throws IllegalArgumentException,
            TransactionRequiredException, EntityNotFoundException, PessimisticLockException, LockTimeoutException,
            PersistenceException {
        this.wrappedEntityManager.refresh(entity, lockMode);
    }

    /**
     * Refresh the state of the instance from the database, overwriting changes
     * made to the entity, if any, and lock it with respect to given lock mode
     * type and with specified properties. If the lock mode type is pessimistic
     * and the entity instance is found but cannot be locked: - the
     * PessimisticLockException will be thrown if the database locking failure
     * causes transaction-level rollback - the LockTimeoutException will be
     * thrown if the database locking failure causes only statement-level
     * rollback If a vendor-specific property or hint is not recognized, it is
     * silently ignored. Portable applications should not rely on the standard
     * timeout hint. Depending on the database in use and the locking mechanisms
     * used by the provider, the hint may or may not be observed.
     * @param entity
     * @param lockMode
     * @param properties standard and vendor-specific properties and hints
     * @throws IllegalArgumentException if the instance is not an entity or the
     *         entity is not managed
     * @throws TransactionRequiredException if there is no transaction and if
     *         invoked on a container-managed EntityManager instance with
     *         PersistenceContextType.TRANSACTION or with a lock mode other than
     *         NONE
     * @throws EntityNotFoundException if the entity no longer exists in the
     *         database
     * @throws PessimisticLockException if pessimistic locking fails and the
     *         transaction is rolled back
     * @throws LockTimeoutException if pessimistic locking fails and only the
     *         statement is rolled back
     * @throws PersistenceException if an unsupported lock call is made
     */
    public void refresh(final Object entity, final LockModeType lockMode, final Map<String, Object> properties)
            throws IllegalArgumentException, TransactionRequiredException, EntityNotFoundException, PessimisticLockException,
            LockTimeoutException, PersistenceException {
        this.wrappedEntityManager.refresh(entity, lockMode, properties);
    }

    /**
     * Set an entity manager property or hint. If a vendor-specific property or
     * hint is not recognized, it is silently ignored.
     * @param propertyName name of property or hint
     * @param value
     * @throws IllegalArgumentException if the second argument is not valid for
     *         the implementation
     */
    public void setProperty(final String propertyName, final Object value) throws IllegalArgumentException {
        this.wrappedEntityManager.setProperty(propertyName, value);
    }

    /**
     * Return an object of the specified type to allow access to the
     * provider-specific API. If the provider's EntityManager implementation
     * does not support the specified class, the PersistenceException is thrown.
     * @param cls the class of the object to be returned. This is normally
     *        either the underlying EntityManager implementation class or an
     *        interface that it implements.
     * @return an instance of the specified class
     * @throws PersistenceException if the provider does not support the call
     */
    public <T> T unwrap(final Class<T> cls) throws PersistenceException {
        return this.wrappedEntityManager.unwrap(cls);
    }

    /**
     * Chapter 5.9.1 <br>
     * The container must throw the TransactionRequiredException if a
     * transaction-scoped persistence context is used, and the EntityManager
     * persist, remove, merge, or refresh method is invoked when no transaction
     * is active.
     * @throws TransactionRequiredException if there is no transaction
     */
    protected void checkTransaction() throws TransactionRequiredException {
        // Get current transaction (if any)
        Transaction currentTx = null;
        try {
            currentTx = JTransactionManager.getTransactionManager().getTransaction();
        } catch (SystemException e) {
            throw new IllegalStateException("Cannot get current transaction", e);
        }

        if (currentTx == null) {
            throw new TransactionRequiredException("The operation needs to be executed within a transaction");
        }

    }

}
