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
 * $Id: JavaMailMimePartDataSourceRef.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import javax.mail.Address;
import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;

/**
 * Defines a referenceable object for a MimePartDatasource Mail object.
 * @author Florent BENOIT
 */
public class JavaMailMimePartDataSourceRef extends AbsJavaMailRef {

    /**
     * List of recipients for TO field.
     */
    private Address[] toRecipients = null;

    /**
     * List of recipients for CC field.
     */
    private Address[] ccRecipients = null;

    /**
     * List of recipients for BCC field.
     */
    private Address[] bccRecipients = null;

    /**
     * Pre-defined subject.
     */
    private String subject = null;

    /**
     * Build a new javax.mail.internet.MimePartDataSource reference.
     */
    public JavaMailMimePartDataSourceRef() {
        super();
    }

    /**
     * Return the type of the factory.
     * @return the type of the mail factory
     */
    @Override
    public String getType() {
        return JavaMailMimePartDataSourceFactory.FACTORY_TYPE;
    }

    /**
     * Retrieves the Reference of this object.
     * @return The non-null Reference of this object.
     * @exception NamingException If a naming exception was encountered while
     *            retrieving the reference.
     */
    @Override
    public Reference getReference() throws NamingException {

        // Build the reference for the JavaMailSession factory
        Reference reference = new Reference(getType(), JavaMailMimePartDataSourceFactory.class.getName(), null);

        // Add the recipients
        // TO
        putObject(reference, JavaMailMimePartDataSourceFactory.TO_RECIPIENTS, this.toRecipients);

        // CC
        putObject(reference, JavaMailMimePartDataSourceFactory.CC_RECIPIENTS, this.ccRecipients);

        // BCC
        putObject(reference, JavaMailMimePartDataSourceFactory.BCC_RECIPIENTS, this.bccRecipients);

        // Add subject
        reference.add(new StringRefAddr(JavaMailMimePartDataSourceFactory.SUBJECT, this.subject));

        // Update the reference
        updateRefAddr(reference);

        return reference;
    }

    /**
     * @return array of TO recipients.
     */
    public Address[] getToRecipients() {
        if (this.toRecipients != null) {
            return this.toRecipients.clone();
        }
        return null;
    }

    /**
     * Sets the TO recipients.
     * @param toRecipients the given recipients
     */
    public void setToRecipients(final Address[] toRecipients) {
        if (toRecipients != null) {
            this.toRecipients = toRecipients.clone();
        } else {
            this.toRecipients = null;
        }
    }

    /**
     * @return array of CC recipients.
     */
    public Address[] getCcRecipients() {
        if (this.ccRecipients != null) {
            return this.ccRecipients.clone();
        }
        return null;
    }

    /**
     * Sets the CC recipients.
     * @param ccRecipients the given recipients
     */
    public void setCcRecipients(final Address[] ccRecipients) {
        if (ccRecipients != null) {
            this.ccRecipients = ccRecipients.clone();
        } else {
            this.ccRecipients = null;
        }
    }

    /**
     * @return array of BCC recipients.
     */
    public Address[] getBccRecipients() {
        if (this.bccRecipients != null) {
            return this.bccRecipients.clone();
        }
        return null;
    }

    /**
     * Sets the BCC recipients.
     * @param bccRecipients the given recipients
     */
    public void setBccRecipients(final Address[] bccRecipients) {
        if (bccRecipients != null) {
            this.bccRecipients = bccRecipients.clone();
        } else {
            this.bccRecipients = null;
        }
    }

    /**
     * @return the subject that can be used.
     */
    public String getSubject() {
        return this.subject;
    }

    /**
     * Sets the subject of the mail.
     * @param subject the given subject
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

}
