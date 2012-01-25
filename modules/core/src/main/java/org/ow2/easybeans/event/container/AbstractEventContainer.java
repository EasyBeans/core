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
 * $Id: AbstractEventContainer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.event.container;

import org.ow2.easybeans.api.EZBContainerConfig;
import org.ow2.easybeans.api.event.container.EZBEventContainer;
import org.ow2.easybeans.event.AbstractEvent;
import org.ow2.util.archive.api.IArchive;

/**
 * Abstract parent class for all EasyBeans container events.
 * @author Vincent Michaud
 */
public abstract class AbstractEventContainer extends AbstractEvent implements EZBEventContainer {

    /**
     * The archive of the container.
     */
    private IArchive archive;

    /**
     * The configuration of the container.
     */
    private EZBContainerConfig containerConfig;

    /**
     * The default constructor.
     * @param source The event source.
     * @param archive The archive of the container
     * @param containerConfig The configuration of the container.
     */
    public AbstractEventContainer(final String source, final IArchive archive,
                                  final EZBContainerConfig containerConfig) {
        super(source);
        this.archive = archive;
        this.containerConfig = containerConfig;
    }

    /**
     * Get the archive of the container.
     * @return The archive
     */
    public IArchive getArchive() {
        return this.archive;
    }

    /**
     * Get the configuration of the container.
     * @return The configuration of the container.
     */
    public EZBContainerConfig getContainerConfiguration() {
        return this.containerConfig;
    }
}
