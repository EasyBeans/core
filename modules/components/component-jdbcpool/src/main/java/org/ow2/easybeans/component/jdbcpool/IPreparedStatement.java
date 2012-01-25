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
 * $Id: IPreparedStatement.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Extends the SQL PreparedStatement interface with other methods.
 * @author Florent BENOIT
 */
public interface IPreparedStatement extends PreparedStatement {

    /**
     * @return true if this statement has been closed, else false.
     */
    boolean isClosed();

    /**
     * Reuses this statement so reset properties.
     * @throws SQLException if reset fails
     */
    void reuse() throws SQLException;

    /**
     * Physically close this Statement.
     * @throws SQLException
     */
    void forget();

    /**
     * Force a close on the Prepared Statement. Usually, it's the caller that did
     * not close it explicitly
     * @return true if it was open
     */
    boolean forceClose();

}
