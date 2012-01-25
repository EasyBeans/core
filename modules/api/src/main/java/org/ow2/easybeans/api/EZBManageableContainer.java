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
 * $Id: EZBManageableContainer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.deploy.api.deployable.IDeployable;

/**
 * Manageable interface for {@link EZBContainer}.
 * @author Guillaume Sauthier
 */
public interface EZBManageableContainer {

    /**
     * Gets the id of this container.
     * @return string id.
     */
    String getId();

    /**
     * Gets the name of this container.
     * @return the name.
     */
    String getName();

    /**
     * Resolve the metadata and analyze deployment descriptors. May be called
     * before the start method. If not already called, it will be called inside
     * the start method.
     * @throws EZBContainerException if resolve step has failed
     */
    void resolve() throws EZBContainerException;

    /**
     * Start this container.
     * @throws EZBContainerException if the start fails.
     */
    void start() throws EZBContainerException;

    /**
     * Stop this container.
     */
    void stop();
    
    /**
     * Gets the deployable used by this container.
     * @return the deployable.
     */
    IDeployable getDeployable();

    /**
     * Gets the archive used by this container. It can be a .jar file or a directory.
     * @return the archive.
     */
    IArchive getArchive();

    /**
     * Check if the container is available or not.
     * @return true if the container is available.
     */
    boolean isAvailable();

}
