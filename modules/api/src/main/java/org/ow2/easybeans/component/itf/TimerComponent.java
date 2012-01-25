/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: TimerComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.itf;

import javax.ejb.TimerService;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.component.api.EZBComponent;

/**
 * Interface for the component that provides the EJB timer service.
 * @author Florent Benoit
 */
public interface TimerComponent extends EZBComponent {


    /**
     * Gets an EJB timer service through this component.
     * @param factory an EasyBeans factory providing timeout notification.
     * @return an EJB timer service
     */
    TimerService getTimerService(final Factory factory);

}
