/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.bean;

import org.ow2.easybeans.api.event.bean.EZBMessageDrivenInfo;
import org.ow2.easybeans.container.info.MessageDrivenInfo;
import org.ow2.easybeans.event.AbstractEvent;
import org.ow2.util.ee.metadata.ejbjar.api.struct.IActivationConfigProperty;

import java.util.List;

/**
 * Message driven info events.
 * @author Mohammed Boukada
 */
public class EventMessageDrivenInfo extends AbstractEvent implements EZBMessageDrivenInfo {

    /**
     * Message driven info
     */
    private MessageDrivenInfo messageDrivenInfo;

    /**
     * The default constructor.
     *
     * @param source The event source.
     */
    public EventMessageDrivenInfo(final String source, MessageDrivenInfo messageDrivenInfo) {
        super(source);
        this.messageDrivenInfo = messageDrivenInfo;
    }

    /**
     * Get Activation config properties
     * @return
     */
    public List<IActivationConfigProperty> getActivationConfigProperties() {
        return messageDrivenInfo.getActivationConfigProperties();
    }

    /**
     * Set Activation config properties
     * @param activationConfigProperties
     */
    public void setActivationConfigProperties(List<IActivationConfigProperty> activationConfigProperties) {
        this.messageDrivenInfo.setActivationConfigProperties(activationConfigProperties);
    }
}
