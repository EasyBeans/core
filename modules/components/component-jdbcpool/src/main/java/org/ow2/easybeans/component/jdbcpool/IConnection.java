/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: IConnection.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Extends the SQL Connection interface with a method to get the physical SQL connection.
 * @author Florent BENOIT
 */
public interface IConnection extends Connection {

    /**
     * Gets the physical connection to the database.
     * @return physical connection to the database
     */
    Connection getConnection();

    /**
     * @return true if the connection to the database is closed or not.
     * @throws SQLException if a database access error occurs
     */
    boolean isPhysicallyClosed() throws SQLException;
}
