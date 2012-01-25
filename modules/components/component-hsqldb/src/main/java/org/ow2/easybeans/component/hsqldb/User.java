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
 * $Id: User.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.hsqldb;

/**
 * Class for the representation of a user with username and password.
 * @author Florent Benoit
 */
public class User {

    /**
     * Username.
     */
    private String userName = null;

    /**
     * Password.
     */
    private String password = null;

    /**
     * Default constructor.
     */
    public User() {
    }

    /**
     * @return the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return the userName.
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Sets the password.
     * @param password the given password
     */
    public void setPassword(final String password) {
        this.password = password;
    }

    /**
     * Sets the username.
     * @param userName the given userName
     */
    public void setUserName(final String userName) {
        this.userName = userName;
    }
}
