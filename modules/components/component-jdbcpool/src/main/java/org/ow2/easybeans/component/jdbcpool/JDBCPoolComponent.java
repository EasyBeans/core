/**
 * EasyBeans
 * Copyright (C) 2006-2009 Bull S.A.S.
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
 * $Id: JDBCPoolComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.TMComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Defines a component that creates a JDBC pool in order to use it in EasyBeans.
 * @author Florent Benoit
 */
public class JDBCPoolComponent implements EZBComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JDBCPoolComponent.class);

    /**
     * Default username.
     */
    private static final String DEFAULT_USER = "";

    /**
     * Default password.
     */
    private static final String DEFAULT_PASSWORD = "";

    /**
     * Default min pool.
     */
    private static final int DEFAULT_MIN_POOL = 10;

    /**
     * Default max pool.
     */
    private static final int DEFAULT_MAX_POOL = 30;

    /**
     * Default prepared statement.
     */
    private static final int DEFAULT_PSTMT = 10;

    /**
     * Default checked level.
     */
    private static final int DEFAULT_CHECK_LEVEL = 0;

    /**
     * Default test statement.
     */
    private static final String DEFAULT_TEST_STATEMENT_HSQLDB = "select 1 from INFORMATION_SCHEMA.SYSTEM_USERS";


    /**
     * Level of checking on connections when got from the pool. this avoids
     * reusing bad connections because too old, for example when database was
     * restarted... 0 = no checking 1 = check that still physically opened. 2 =
     * try a null statement.
     */
    private int checkLevel = DEFAULT_CHECK_LEVEL;

    /**
     * Connection manager object.
     */
    private ConnectionManager connectionManager = null;

    /**
     * JNDI name.
     */
    private String jndiName = null;

    /**
     * Username.
     */
    private String username = DEFAULT_USER;

    /**
     * Password.
     */
    private String password = DEFAULT_PASSWORD;

    /**
     * URL for accessing to the database.
     */
    private String url = null;

    /**
     * Name of the driver class to use.
     */
    private String driver = null;

    /**
     * Use transaction or not ?
     */
    private boolean useTM = true;

    /**
     * Pool min.
     */
    private int poolMin = DEFAULT_MIN_POOL;

    /**
     * Pool max.
     */
    private int poolMax = DEFAULT_MAX_POOL;

    /**
     * Max of prepared statement.
     */
    private int pstmtMax = DEFAULT_PSTMT;

    /**
     * Test statement.
     */
    private String testStatement = DEFAULT_TEST_STATEMENT_HSQLDB;

    /**
     * Transaction component.
     */
    private TMComponent transactionComponent = null;

    /**
     * Default constructor.
     */
    public JDBCPoolComponent() {
    }

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {
        this.connectionManager = new ConnectionManager();
        this.connectionManager.setTransactionIsolation("default");

        // Check that data are correct
        validate();

        this.connectionManager.setDatasourceName(this.jndiName);
        this.connectionManager.setDSName(this.jndiName);
        this.connectionManager.setUrl(this.url);
        try {
            this.connectionManager.setClassName(this.driver);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot load jdbc driver '" + this.driver + "'.", e);
        }
        this.connectionManager.setUserName(this.username);
        this.connectionManager.setPassword(this.password);
        this.connectionManager.setTransactionIsolation("default");
        this.connectionManager.setPstmtMax(this.pstmtMax);
        this.connectionManager.setCheckLevel(this.checkLevel);
        this.connectionManager.setTestStatement(this.testStatement);

    }

    /**
     * Validate current data.
     * @throws EZBComponentException if validation fails.
     */
    private void validate() throws EZBComponentException {
        // check that there is a JNDI name
        if (this.jndiName == null) {
            throw new EZBComponentException("No JNDI name set");
        }

        // check that there is an URL
        if (this.url == null) {
            throw new EZBComponentException("No URL set");
        }

        // Check that there is a driver classname
        if (this.driver == null) {
            throw new EZBComponentException("No driver set");
        }
    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {
        // set settings
        if (this.useTM) {
            if (this.transactionComponent == null) {
                // Missing injection
                throw new EZBComponentException("Transaction component was not injected. Reason: Missing tm=\"#tm\""
                        + " in the easybeans.xml configuration file ?");
            }
            this.connectionManager.setTm(this.transactionComponent.getTransactionManager());
        }
        this.connectionManager.setPoolMin(this.poolMin);
        this.connectionManager.setPoolMax(this.poolMax);

        // Something is there ?
        try {
            Object o = new InitialContext().lookup(this.jndiName);
            if (o != null) {
                logger.warn("Entry with JNDI name {0} already exist", this.jndiName);
            }
        } catch (NamingException e) {
            logger.debug("Nothing with JNDI name {0}", this.jndiName);
        }

        // Bind the resource.
        try {
            new InitialContext().rebind(this.jndiName, this.connectionManager);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot bind a JDBC Datasource with the jndi name '" + this.jndiName + "'.");
        }

        logger.info("DS ''{0}'', URL ''{1}'', Driver = ''{2}''.", this.jndiName, this.url, this.driver);
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Unbind the resource.
        try {
            new InitialContext().unbind(this.jndiName);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot unbind a JDBC Datasource with the jndi name '" + this.jndiName + "'.");
        }
    }

    /**
     * Sets the name of the JDBC driver.
     * @param driver the driver's name
     */
    public void setDriver(final String driver) {
        this.driver = driver;
    }

    /**
     * @return the name of the JDBC driver.
     */
    public String getDriver() {
        return this.driver;
    }

    /**
     * Sets the JNDI name.
     * @param jndiName the name to bind the datasource
     */
    public void setJndiName(final String jndiName) {
        this.jndiName = jndiName;
    }

    /**
     * @return the name that is bound in the datasource
     */
    public String getJndiName() {
        return this.jndiName;
    }

    /**
     * Sets the password to use.
     * @param password the password for the url connection.
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * The maximum size of the JDBC pool.
     * @param poolMax the value of the pool's max.
     */
    public void setPoolMax(final int poolMax) {
        this.poolMax = poolMax;
    }

    /**
     * @return the maximum size of the JDBC pool.
     */
    public int getPoolMax() {
        return this.poolMax;
    }

    /**
     * The minimum size of the JDBC pool.
     * @param poolMin the value of the pool's min.
     */
    public void setPoolMin(final int poolMin) {
        this.poolMin = poolMin;
    }

    /**
     * @return the minimum size of the JDBC pool.
     */
    public int getPoolMin() {
        return this.poolMax;
    }

    /**
     * Set the max cache of prepared statement.
     * @param pstmtMax the max value for prepare statement.
     */
    public void setPstmtMax(final int pstmtMax) {
        this.pstmtMax = pstmtMax;
    }

    /**
     * @return the minimum size of the JDBC pool.
     */
    public int getPstmtMax() {
        return this.poolMax;
    }

    /**
     * Sets the connection's URL.
     * @param url the URL used for the connection.
     */
    public void setUrl(final String url) {
        this.url = url;
    }

    /**
     * @return the URL used for the connection.
     */
    public String getUrl() {
        return this.url;
    }

    /**
     * Sets the username that is used to get a connection.
     * @param username the name of the user.
     */
    public void setUsername(final String username) {
        this.username = username;
    }

    /**
     * @return the username that is used to get a connection.
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * Is that the pool will use transaction or not.
     * @param useTM the true/false value.
     */
    public void setUseTM(final boolean useTM) {
        this.useTM = useTM;
    }

    /**
     * @return true if this pool will use transaction (else false).
     */
    public boolean isUseTM() {
        return this.useTM;
    }

    /**
     * @return connection checking level
     */
    public int getCheckLevel() {
        return this.checkLevel;
    }

    /**
     * Sets the JDBC check level.
     * @param checkLevel jdbc connection checking level.
     */
    public void setCheckLevel(final int checkLevel) {
        this.checkLevel = checkLevel;
    }

    /**
     * @return the test statement used with a checkedlevel.
     */
    public String getTestStatement() {
        return this.testStatement;
    }

    /**
     * Sets the test statement.
     * @param testStatement the statement to execute
     */
    public void setTestStatement(final String testStatement) {
        this.testStatement = testStatement;
    }


    /**
     * @return transaction component.
     */
    public TMComponent getTransactionComponent() {
        return this.transactionComponent;
    }

    /**
     * Sets the transaction component.
     * @param transactionComponent the given transaction component.
     */
    public void setTransactionComponent(final TMComponent transactionComponent) {
        this.transactionComponent = transactionComponent;
    }

}
