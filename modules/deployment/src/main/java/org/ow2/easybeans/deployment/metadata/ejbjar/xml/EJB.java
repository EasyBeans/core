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
 * $Id: EJB.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.util.List;

/**
 * Contains information specific on EJBs (Session, MDB,...).
 * @author Florent BENOIT
 */
public class EJB {

    /**
     * List of session beans.
     */
    private List<Session> sessions = null;

    /**
     * List of session beans.
     */
    private List<MessageDrivenBean> messageDrivenBeans = null;

    /**
     * @return list of configuration for session beans.
     */
    public List<Session> getSessions() {
        return sessions;
    }

    /**
     * Sets the specific configuration on session beans.
     * @param sessions the configuration on session beans
     */
    public void setSessions(final List<Session> sessions) {
        this.sessions = sessions;
    }

    /**
     * @return list of configuration for message driven beans.
     */
    public List<MessageDrivenBean> getMessageDrivenBeans() {
        return messageDrivenBeans;
    }

    /**
     * Sets the specific configuration on message driven beans.
     * @param messageDrivenBeans the configuration on message driven beans
     */
    public void setMessageDrivenBeans(final List<MessageDrivenBean> messageDrivenBeans) {
        this.messageDrivenBeans = messageDrivenBeans;
    }
}
