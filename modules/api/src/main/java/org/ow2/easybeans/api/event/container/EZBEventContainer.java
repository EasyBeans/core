/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
 * Contact: easybeans@objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 * USA
 *
 * --------------------------------------------------------------------------
 * $Id: EZBEventContainer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.event.container;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.event.EZBEvent;
import org.ow2.util.archive.api.IArchive;

/**
 * Interface for all container events.
 * @author Vincent Michaud
 */
public interface EZBEventContainer extends EZBEvent {

    /**
     * Get the archive.
     * @return The archive.
     */
    IArchive getArchive();

    /**
     * Get the configuration of the container.
     * @return The configuration of the container.
     */
    EZBContainerConfig getContainerConfiguration();
}
