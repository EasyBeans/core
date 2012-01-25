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
 * $Id: MimePart.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail;

import java.util.List;

/**
 * Represent an object for mail component.
 * @author Florent BENOIT
 */
public class MimePart extends AbsMail {

    /**
     * Subject.
     */
    private String subject = null;

    /**
     * Recipients.
     */
    private List<MailAddress> mailAddresses = null;

    /**
     * @return the subject used by this factory
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the given subject.
     * @param subject the given subject.
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * @return the given mail addresses.
     */
    public List<MailAddress> getMailAddresses() {
        return mailAddresses;
    }

    /**
     * Sets the list of mail addresses.
     * @param mailAddresses the given list
     */
    public void setMailAddresses(final List<MailAddress> mailAddresses) {
        this.mailAddresses = mailAddresses;
    }




}
