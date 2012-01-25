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
 * $Id: JavaMailMimePartDataSourceFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail.factory;

import java.util.Hashtable;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimePartDataSource;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * JNDI factory for session factory.
 * @author Florent BENOIT
 */
public class JavaMailMimePartDataSourceFactory extends AbsJavaMailFactory implements ObjectFactory {

    /**
     * Type of object created by this factory.
     */
    public static final String FACTORY_TYPE = "javax.mail.internet.MimePartDataSource";

    /**
     * TO recipients.
     */
    public static final String TO_RECIPIENTS = "to.recipients";

    /**
     * CC recipients.
     */
    public static final String CC_RECIPIENTS = "cc.recipients";

    /**
     * BCC recipients.
     */
    public static final String BCC_RECIPIENTS = "bcc.recipients";

    /**
     * Subject.
     */
    public static final String SUBJECT = "subject";

    /**
     * Creates an object using the location or reference information specified.
     * @param obj The possibly null object containing location or reference
     *        information that can be used in creating an object.
     * @param name The name of this object relative to <code>nameCtx</code>,
     *        or null if no name is specified.
     * @param nameCtx The context relative to which the <code>name</code>
     *        parameter is specified, or null if <code>name</code> is relative
     *        to the default initial context.
     * @param environment The possibly null environment that is used in creating
     *        the object.
     * @return The object created; null if an object cannot be created.
     * @exception Exception if this object factory encountered an exception
     */
    public Object getObjectInstance(final Object obj, final Name name, final Context nameCtx, final Hashtable<?, ?> environment)
            throws Exception {

        // Check if the reference classname is valid
        Reference reference = (Reference) obj;
        if (!FACTORY_TYPE.equals(reference.getClassName())) {
            return null;
        }

        // Build Mimemessage wrapping a session object
        MimeMessage mimeMessage = new MimeMessage(Session.getInstance(getSessionProperties(reference),
                getAuthenticator(reference)));

        // Field 'to'
        Address[] toRecipients = getObject(reference, TO_RECIPIENTS);
        if (toRecipients != null) {
            mimeMessage.setRecipients(Message.RecipientType.TO, toRecipients);
        }

        // Field 'cc'
        Address[] ccRecipients = getObject(reference, CC_RECIPIENTS);
        if (ccRecipients != null) {
            mimeMessage.setRecipients(Message.RecipientType.CC, ccRecipients);
        }

        // Field 'bcc'
        Address[] bccRecipients = getObject(reference, BCC_RECIPIENTS);
        if (bccRecipients != null) {
            mimeMessage.setRecipients(Message.RecipientType.BCC, bccRecipients);
        }

        // Field 'subject'
        String mailSubject = getString(reference, SUBJECT);
        if (mailSubject != null) {
            mimeMessage.setSubject(mailSubject);
        }

        // Build object and return it
        MimePartDataSource mimePartDS = new MimePartDataSource(mimeMessage);
        return mimePartDS;

    }

}
