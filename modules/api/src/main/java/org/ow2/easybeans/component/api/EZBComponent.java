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
 * $Id: EZBComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.api;

/**
 * Defines the interface that a component needs to implement.
 * @author Florent Benoit
 */
public interface EZBComponent {

    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    void init() throws EZBComponentException;


    /**
     * Start method.<br/>
     * This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    void start() throws EZBComponentException;


    /**
     * Stop method.<br/>
     * This method is called when component needs to be stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    void stop() throws EZBComponentException;

}
