/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EZBClusteredBeanEvent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.event.bean;

import java.util.List;

import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.api.event.lifecycle.EZBEventLifeCycle;

/**
 *
 * @author eyindanga.
 */
public interface EZBClusteredBeanEvent extends EZBEventLifeCycle {

    /**
     * Container is starting.
     */
    String STARTING = "STARTING";

    /**
     * Container is stopping.
     */
    String STOPPING = "STOPPING";

    /**
     * @return the state.
     */
    String getState();

    /**
     * Sets the state.
     * @param state the given state
     */
     void setState(final String state);

     /**
      * @return EasyBeans JNDI reference.
      */
    List<EZBRef> getReferences();

    /**
     * Set beans references.
     * @param references the given values.
     */
    void setReferences(final List<EZBRef> references);


}
