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
 * $Id: HSQLDBComponent.java 5752 2011-03-01 12:48:22Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.hsqldb;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;

import org.hsqldb.DatabaseManager;
import org.hsqldb.Server;
import org.hsqldb.ServerConstants;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EmbeddedDBComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Allows to start an embedded HSQLDB server.
 * @author Florent Benoit
 */
public class HSQLDBComponent implements EmbeddedDBComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(HSQLDBComponent.class);

    /**
     * List of users.
     */
    private List<User> users = null;

    /**
     * Name of database.
     */
    private String databaseName = null;

    /**
     * Default port number.
     */
    private static final String DEFAULT_PORT = "9001";

    /**
     * Default port number.
     */
    private static final String DEFAULT_HOST = "localhost";

    /**
     * Sleep value.
     */
    private static final int SLEEP_VALUE = 100;

    /**
     * Max retry number.
     */
    private static final int MAX_RETRY_NB = 50;

    /**
     * port number used.
     */
    private String portNumber = null;

    /**
     * Hostname to use.
     */
    private String hostname = null;

    /**
     * HsqlDB server.
     */
    private Server server = null;

    /**
     * String path.
     */
    private String path = null;

    /**
     * Default constructor.<br>
     * Use default port number + hostname.
     */
    public HSQLDBComponent() {
        this.portNumber = DEFAULT_PORT;
        this.hostname = DEFAULT_HOST;
    }

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

        this.server = new Server();
        // Remove all traces if level != DEBUG
        if (!logger.isDebugEnabled()) {
            this.server.setLogWriter(null);
            this.server.setErrWriter(null);
            this.server.setSilent(true);
            this.server.setTrace(false);
            this.server.setLogWriter(null);
        } else {
            // Enable all traces : verbose mode (as user needs DEBUG)
            this.server.setSilent(false);
            this.server.setTrace(true);
        }

        // Use a specified path or go in temp directory ?
        String baseDir = null;
        if (this.path != null) {
            baseDir = this.path + File.separator + this.databaseName;
        } else {
            baseDir = System.getProperty("java.io.tmpdir") + File.separator + "easybeans" + File.separator + "hsqldb"
            + File.separator + this.databaseName;
        }

        String pString = "";
        if (this.portNumber != null) {
            pString = ";port=" + this.portNumber;
        }
        String serverProps = "database.0=" + baseDir + ";dbname.0=" + this.databaseName + pString;
        logger.debug("Server properties = {0}", serverProps);
        this.server.putPropertiesFromString(serverProps);

        // Specify hostname to use
        this.server.setAddress(this.hostname);

        try {
            Class.forName("org.hsqldb.jdbcDriver");
        } catch (ClassNotFoundException e) {
            throw new EZBComponentException("Cannot access to HSQL Driver 'org.hsqldb.jdbcDriver'.", e);
        }

    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    @SuppressWarnings("boxing")
    public void start() throws EZBComponentException {

        logger.info("Starting ''{0}'' ''{1}'' on port ''{2}''", this.server.getProductName(), this.server.getProductVersion(),
                this.portNumber);

        this.server.start();

        // Wait the start
        int retryNb = 0;
        while (this.server.getState() != ServerConstants.SERVER_STATE_ONLINE) {
            try {
                Thread.sleep(SLEEP_VALUE);
            } catch (InterruptedException ie) {
                logger.error("Cannot wait that the service is online", ie);
            }
            // Error if server state is "SHUTDOWN" during a long period
            // Maybe strange but 'SHUTDOWN' state seems to be an intermediate
            // state during startup
            retryNb++;
            if (this.server.getState() == ServerConstants.SERVER_STATE_SHUTDOWN && retryNb >= MAX_RETRY_NB) {
                Throwable t = this.server.getServerError();
                throw new EZBComponentException("Cannot start the server. The server has not started and is shutdown.", t);
            }
            logger.debug("retry= {0}, serverState= {1}", retryNb, this.server.getState());
        }

        String connURL = "jdbc:hsqldb:hsql://" + this.hostname + ":" + this.portNumber + "/" + this.databaseName;
        logger.info("{0} started with URL {1}", this.server.getProductName(), connURL);

        Connection conn = null;
        Statement st = null;

        try {
            conn = DriverManager.getConnection(connURL, "sa", "");
        } catch (SQLException e) {
            throw new EZBComponentException("Cannot access to HSQL", e);
        }

        try {
            st = conn.createStatement();
        } catch (SQLException e) {
            try {
                conn.close();
            } catch (SQLException connEx) {
                logger.error("Error while closing connection object", connEx);
            }
            throw new EZBComponentException("Cannot access to HSQL", e);
        }



        // Drop users before recreating it
        User user = null;
        String userName = null;
        String password = null;
        ResultSet rs = null;
        for (Iterator<User> it = this.users.iterator(); it.hasNext();) {
            user = it.next();
            try {
                password = user.getPassword();
                userName = user.getUserName();
                logger.debug("Dropping and adding user {0} with password {1}.", userName, password);
                try {
                    rs = st.executeQuery("DROP USER " + userName);
                } catch (SQLException e) {
                    logger.debug("User {0} doesn't exists", userName, e);
                }
                rs = st.executeQuery("Create USER " + userName + " PASSWORD " + password + " ADMIN");
                rs.close();
            } catch (SQLException e) {
                logger.error("Error while creating/adding user", e);
            }

        }

        try {
            st.close();
            conn.close();
        } catch (SQLException e) {
            logger.error("Error while closing statement object", e);
        }

    }

    /**
     * Gets the list of users.
     * @return the list of users.
     */
    public List<User> getUsers() {
        return this.users;
    }

    /**
     * Set the list of users.
     * @param users the list of users.
     */
    public void setUsers(final List<User> users) {
        this.users = users;
    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        this.server.shutdown();
        DatabaseManager.getTimer().shutDown();
    }

    /**
     * Sets the port number.
     * @param portNumber the port number to use.
     */
    public void setPortNumber(final String portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Sets the hostname.
     * @param hostname the hostname to use.
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * Sets the database name.
     * @param databaseName the name of the database.
     */
    public void setDatabaseName(final String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Allows to change the path of the database files.
     * @param path the path of the files for storing the database.
     */
    public void setPath(final String path) {
        this.path = path;
    }

}
