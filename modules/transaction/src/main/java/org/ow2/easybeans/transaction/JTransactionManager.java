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
 * $Id: JTransactionManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.ow2.easybeans.component.itf.TMComponent;

/**
 * Allows to get a transaction manager.
 * @author Florent Benoit
 */
public final class JTransactionManager {

    /**
     * Utility class, no public constructor.
     */
    private JTransactionManager() {

    }

    /**
     * Internal tm.
     */
    private static TransactionManager tm = null;

    /**
     * Allow to get a transaction manager.
     * @return a transaction manager.
     */
    public static TransactionManager getTransactionManager() {
        if (tm != null) {
            return tm;
        }

        // Needs to be initialized
        init();

        return tm;
    }

    /**
     * Init the Transaction manager.
     */
    public static void init() {
        // get it.
        try {
            tm = (TransactionManager) new InitialContext().lookup(TMComponent.JNDI_NAME);
        } catch (NamingException e) {
            throw new IllegalStateException("Cannot get transaction manager", e);
        }
    }
}
