/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: SimpleAuthenticator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * This class specifies the {@link Authenticator} to use in
 * <code>Sesssion.getInstance()</code> method.
 * @author Florent Benoit
 */
public class SimpleAuthenticator extends Authenticator {

    /**
     * Username.
     */
    private String username = null;

    /**
     * Password.
     */
    private String password = null;

    /**
     * Constructor for build a new {@link Authenticator}.
     * @param username the name of the user
     * @param password the password of the user
     */
    public SimpleAuthenticator(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Called when password authentication is needed.
     * @return a password authentication
     */
    @Override
    public PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(username, password);
    }

}
