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
 * $Id: JoramBootstrapContext.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.joram;

import java.util.Timer;

import javax.resource.spi.BootstrapContext;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.work.WorkManager;
import javax.resource.spi.XATerminator;

/**
 * This class implements the BootstrapContext interface of the Connector
 * Architecture 1.5 specification.
 */
public class JoramBootstrapContext implements BootstrapContext {

    /**
     * Work Manager.
     */
    private WorkManager workManager = null;

    /**
     * XATerminator object.
     */
    private XATerminator xaTerm = null;

    /**
     * Constructor for the Resource Adapter context.
     * @param workManager the WorkManager
     * @param xa XATerminator
     */
    public JoramBootstrapContext(final WorkManager workManager, final XATerminator xa) {
        this.workManager = workManager;
        xaTerm = xa;
    }

    /**
     * Creates a timer for use by the Resource Adapter.
     * @return Timer object
     * @throws UnavailableException if a Timer instance is unavailable
     */
    public Timer createTimer() throws UnavailableException {
        return new Timer(true);
    }

    /**
     * Return the associated WorkManager.
     * @return WorkManger object
     */
    public WorkManager getWorkManager() {
        return workManager;
    }

    /**
     * Return an XATerminator.
     * @return XATerminator object
     */
    public XATerminator getXATerminator() {
        return xaTerm;
    }
}
