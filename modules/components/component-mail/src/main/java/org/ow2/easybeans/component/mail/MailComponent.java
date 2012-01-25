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
 * $Id: MailComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.mail;

import java.util.ArrayList;
import java.util.List;

import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.JavaMailComponent;
import org.ow2.easybeans.component.mail.factory.AbsJavaMailRef;
import org.ow2.easybeans.component.mail.factory.JavaMailMimePartDataSourceRef;
import org.ow2.easybeans.component.mail.factory.JavaMailSessionRef;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Component for providing mail access to EE components.
 * @author Florent BENOIT
 */
public class MailComponent implements JavaMailComponent {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(MailComponent.class);

    /**
     * Default auth for factories associated to this component.
     */
    private Auth defaultAuth = null;

    /**
     * List of mail session factories.
     */
    private List<Session> sessionFactories = null;

    /**
     * List of mail mime part ds factories.
     */
    private List<MimePart> mimePartFactories = null;

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

        // Create and bind factories
        for (MailItf factory : getAllFactories()) {

            AbsJavaMailRef javaMailRef = null;
            String jndiName = factory.getJNDIName();

            // Create object and bind it
            if (factory instanceof Session) {
                JavaMailSessionRef javamailSession = new JavaMailSessionRef();
                javaMailRef = javamailSession;
            } else if (factory instanceof MimePart) {
                JavaMailMimePartDataSourceRef mimePartDataSourceRef = new JavaMailMimePartDataSourceRef();
                javaMailRef = mimePartDataSourceRef;

                // Set properties of this factory.
                MimePart mimePart = (MimePart) factory;

                // Set subject
                mimePartDataSourceRef.setSubject(mimePart.getSubject());

                // Define addresses
                List<Address> toAddresses = new ArrayList<Address>();
                List<Address> ccAddresses = new ArrayList<Address>();
                List<Address> bccAddresses = new ArrayList<Address>();

                // Get value
                for (MailAddress mail : mimePart.getMailAddresses()) {

                    // Build address object from the string
                    Address address = null;
                    try {
                        address = new InternetAddress(mail.getName());
                    } catch (AddressException e) {
                        throw new EZBComponentException("Cannot build an internet address with given value '" + mail.getName()
                                + "'.", e);
                    }

                    // Add address in the correct list
                    if ("CC".equalsIgnoreCase(mail.getType())) {
                        ccAddresses.add(address);
                    } else if ("BCC".equalsIgnoreCase(mail.getType())) {
                        bccAddresses.add(address);
                    } else {
                        // Default is TO
                        toAddresses.add(address);
                    }
                }

                // Set recipients
                mimePartDataSourceRef.setToRecipients(toAddresses.toArray(new Address[toAddresses.size()]));
                mimePartDataSourceRef.setCcRecipients(ccAddresses.toArray(new Address[ccAddresses.size()]));
                mimePartDataSourceRef.setBccRecipients(bccAddresses.toArray(new Address[bccAddresses.size()]));

            } else {
                throw new EZBComponentException("Unknown factory '" + factory + "'.");
            }

            // Set common values
            javaMailRef.setName(factory.getName());
            javaMailRef.setJNDIName(jndiName);
            javaMailRef.setProperties(factory.getMailSessionProperties());

            // Auth set for the factory ?
            Auth auth = factory.getAuth();

            // No, try to see for default auth ?
            if (auth == null) {
                auth = getAuth();
            }

            // There is authentication, set it
            if (auth != null) {
                javaMailRef.setAuthName(auth.getUsername());
                javaMailRef.setAuthName(auth.getPassword());
            }

            // Bind object
            try {
                new InitialContext().bind(jndiName, javaMailRef);
                logger.info("Binding {0} Mail factory with JNDI name {1}", javaMailRef.getType(), jndiName);
            } catch (NamingException e) {
                throw new EZBComponentException(
                        "Unable to bind the factory '" + factory + "' with JNDI name '" + jndiName + "'.", e);
            }

        }

    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // Unbind factories
        // Create and bind factories
        for (MailItf factory : getAllFactories()) {
            try {
                new InitialContext().unbind(factory.getJNDIName());
            } catch (NamingException e) {
                logger.error("Cannot unbind factory with name {0} and JNDI name {1}", factory.getName(), factory.getJNDIName());
            }
        }
    }

    /**
     * Set the auth.
     * @param auth the given auth
     */
    public void setAuth(final Auth auth) {
        this.defaultAuth = auth;
    }

    /**
     * @return the auth.
     */
    public Auth getAuth() {
        return this.defaultAuth;
    }

    /**
     * @return list of factories.
     */
    public List<MailItf> getAllFactories() {
        List<MailItf> factories = new ArrayList<MailItf>();
        if (sessionFactories != null) {
            factories.addAll(sessionFactories);
        }
        if (mimePartFactories != null) {
            factories.addAll(mimePartFactories);
        }
        return factories;
    }

    /**
     * Sets the list of mail session factories.
     * @param sessionFactories the list of factories
     */
    public void setSessions(final List<Session> sessionFactories) {
        this.sessionFactories = sessionFactories;
    }

    /**
     * Gets the list of mail session factories.
     * @return the list of mail session factories.
     */
    public List<Session> getSessions() {
        return this.sessionFactories;
    }

    /**
     * Sets the list of mail mimepart factories.
     * @param mimePartFactories the list of factories
     */
    public void setMimeParts(final List<MimePart> mimePartFactories) {
        this.mimePartFactories = mimePartFactories;
    }

    /**
     * Gets the list of mail mimepart factories.
     * @return the list of mail mimepart factories.
     */
    public List<MimePart> getMimeParts() {
        return this.mimePartFactories;
    }
}
