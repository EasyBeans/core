/**
 * EasyBeans
 * Copyright (C) 2006-2008 Bull S.A.S.
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
 * $Id: EZBContainerCallbackInfo.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import org.ow2.util.archive.api.IArchive;

import java.util.Map;

/**
 * Information send by containers to listeners when the callback is called.
 * @author Florent Benoit
 */
public class EZBContainerCallbackInfo {

    /**
     * Archive managed by the container.
     */
    private IArchive archive;

    /**
     * Map of Factories.
     */
    private Map<String, Factory<?, ?>> factories;

    /**
     * The EZBContainer associated to this callback object.
     */
    private EZBContainer container;

    /**
     * Gets the archive (directory/file) managed by this container.
     * @return the archive (directory/file) managed by this container.
     */
    public IArchive getArchive() {
        return this.archive;
    }

    /**
     * Sets the archive (directory/file) managed by this container.
     * @param archive the archive (directory/file) managed by this container.
     */
    public void setArchive(final IArchive archive) {
        this.archive = archive;
    }

    /**
     * Sets the Map of factories managed by this container.
     * @param factories Map of factories managed by this container.
     */
    public void setFactories(final Map<String, Factory<?, ?>> factories) {
        this.factories = factories;
    }

    /**
     * Gets the Map of factories managed by this container.
     * @return Returns the Map of factories managed by this container.
     */
    public Map<String, Factory<?, ?>> getFactories() {
        return this.factories;
    }

    /**
     * @return the EjbJar container
     */
    public EZBContainer getContainer() {
        return container;
    }

    /**
     * Set the EZBContainer.
     * @param container the EjbJar container
     */
    public void setContainer(final EZBContainer container) {
        this.container = container;
    }
}
