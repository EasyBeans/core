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
 * $Id: AbstractExtensionListener.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.helper.listener;

import org.ow2.easybeans.event.naming.JavaContextNamingEvent;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.event.api.IEventListener;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * The AbstractExtensionListener provides default behavior for IEventListener methods.
 * The ExtensionListeners sub classes will be synchronous and only accepting {@link JavaContextNamingEvent} .
 * @author Guillaume Sauthier
 */
public abstract class AbstractExtensionListener implements IEventListener {
    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(AbstractExtensionListener.class);

    /**
     * Only accepts the event if it's a CompNamingEvent.
     * @param event tested event
     * @return <code>true</code> if the proposed event is of the expected type
     */
    public boolean accept(final IEvent event) {
        if (event instanceof JavaContextNamingEvent) {
            JavaContextNamingEvent namingEvent = (JavaContextNamingEvent) event;

            // source/event-provider-id attribute is used to filter the destination
            if ("java:".equals(namingEvent.getEventProviderId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return Normal synchrone priority
     */
    public EventPriority getPriority() {
        return EventPriority.SYNC_NORM;
    }

    /**
     * Utility method to associate an Exception in an IEvent.
     * @param event event to be used as storage place for the Exception.
     * @param throwable The exception to store.
     */
    protected void throwException(final JavaContextNamingEvent event, final Throwable throwable) {

        // Log a message
        logger.error("Errors during Listeners processing.", throwable);

        // Append the exception in the event
        event.addThrowable(throwable);

        // Rethrow the exception to break the execution flow
        if (throwable instanceof RuntimeException) {
            throw (RuntimeException) throwable;
        }
        throw new RuntimeException("Wrapping cause", throwable);
    }
}
