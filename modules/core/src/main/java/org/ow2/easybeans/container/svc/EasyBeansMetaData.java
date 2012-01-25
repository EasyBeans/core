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
 * $Id: EasyBeansMetaData.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.svc;

import javax.ejb.EJBException;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;

/**
 * Metadata that are provided to the EJB 2.1 view clients.
 * @author Florent Benoit
 */
public class EasyBeansMetaData implements EJBMetaData {

    /**
     * Boolean used to indicate if the bean is a stateless bean or not.
     */
    private boolean stateless = false;

    /**
     * EJB Home object.
     */
    private EJBHome ejbHome = null;

    /**
     * Home interface class object.
     */
    private Class<?> homeInterfaceClass = null;

    /**
     * Remote Home interface class object.
     */
    private Class<?> remoteInterfaceClass = null;


    /**
     * Build a new metadata object with the given arguments.
     * @param ejbHome the given ejb home object
     * @param homeInterfaceClass the given interface used for the Home.
     * @param remoteInterfaceClass the given interface used for the remote.
     * @param stateless if true, it means that it is a stateless object.
     */
    public EasyBeansMetaData(final EJBHome ejbHome, final Class<?> homeInterfaceClass, final Class<?> remoteInterfaceClass,
            final boolean stateless) {
        this.ejbHome = ejbHome;
        this.homeInterfaceClass = homeInterfaceClass;
        this.remoteInterfaceClass = remoteInterfaceClass;
        this.stateless = stateless;
    }


    /**
     * Obtain the remote home interface of the enterprise Bean.
     * @return the remote home interface of the enterprise Bean.
     */
    public EJBHome getEJBHome() {
        return this.ejbHome;
    }

    /**
     * Obtain the Class object for the enterprise Bean's remote home interface.
     * @return the Class object for the enterprise Bean's remote home interface.
     */
    public Class<?> getHomeInterfaceClass() {
        return this.homeInterfaceClass;
    }

    /**
     * Obtain the Class object for the enterprise Bean's remote interface.
     * @return the Class object for the enterprise Bean's remote interface.
     */
    public Class<?> getRemoteInterfaceClass() {
        return this.remoteInterfaceClass;
    }

    /**
     * Obtain the Class object for the enterprise Bean's primary key class.
     * @return the Class object for the enterprise Bean's primary key class.
     */
    public Class<?> getPrimaryKeyClass() {
        // session bean don't have primary key class
        throw new EJBException("getPrimaryKeyClass() not allowed for session beans.");
    }

    /**
     * Test if the enterprise Bean's type is "session".
     * @return True if the type of the enterprise Bean is session bean.
     */
    public boolean isSession() {
        // always true as entity 2.1 are not supported
        return true;
    }

    /**
     * Test if the enterprise Bean's type is "stateless session".
     * @return True if the type of the enterprise Bean is stateless session.
     */
    public boolean isStatelessSession() {
        return this.stateless;
    }

}
