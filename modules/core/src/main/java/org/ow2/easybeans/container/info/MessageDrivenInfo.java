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
 * $Id: MessageDrivenInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info;

import java.util.ArrayList;
import java.util.List;

import javax.ejb.ActivationConfigProperty;

/**
 * This class contains runtime information about Message driven beans.
 * @author Florent Benoit
 */
public class MessageDrivenInfo extends BeanInfo {

    /**
     * List of ActivationConfigProperty.
     */
    private List<ActivationConfigProperty> activationConfigProperties = null;

    /**
     * Message listener Interface.
     */
    private String messageListenerInterface = null;

    /**
     * The message destination link.
     */
    private String messageDestinationLink = null;

    /**
     * Default constructor for MessageDriven Bean info.
     */
    public MessageDrivenInfo() {
        this.activationConfigProperties = new ArrayList<ActivationConfigProperty>();
    }

    /**
     * Gets the activation config properties.
     * @return the list of activation config properties
     */
    public List<ActivationConfigProperty> getActivationConfigProperties() {
        return this.activationConfigProperties;
    }

    /**
     * Sets the activation config properties.
     * @param activationConfigProperties the list of activation config
     *        properties
     */
    public void setActivationConfigProperties(final List<ActivationConfigProperty> activationConfigProperties) {
        this.activationConfigProperties = activationConfigProperties;
    }

    /**
     * @return message listener interface.
     */
    public String getMessageListenerInterface() {
        return this.messageListenerInterface;
    }

    /**
     * Sets the message listener interface.
     * @param messageListenerInterface the given interface.
     */
    public void setMessageListenerInterface(final String messageListenerInterface) {
        this.messageListenerInterface = messageListenerInterface;
    }

    /**
     * Set the message destination link.
     * @param messageDestinationLink message destination link.
     */
    public void setMessageDestinationLink(final String messageDestinationLink) {
        this.messageDestinationLink = messageDestinationLink;
    }

    /**
     * Gets the message destination link.
     * @return the message destination link
     */
    public String getMessageDestinationLink() {
        return this.messageDestinationLink;
    }

}
