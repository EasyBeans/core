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
 * $Id: EmbeddedManager.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;


/**
 * This class manages the Embedded instance that have been created. The Embedded
 * object self register to this manager. Also, the list of the embedded server
 * is a weak hashmap. So when an object is deleted, reference can be removed.
 * @author Florent Benoit
 */
public final class EmbeddedManager {

    /**
     * Utility class, no public constructor.
     */
    private EmbeddedManager() {

    }

    /**
     * Map of embedded servers for some id.
     */
    private static Map<Integer, WeakReference<EZBServer>> servers = new WeakHashMap<Integer, WeakReference<EZBServer>>();


    /**
     * Gets the embedded server with the given id.
     * @param id the identifier of the embedded server.
     * @return the instance found or null.
     */
    public static EZBServer getEmbedded(final Integer id) {
        WeakReference<EZBServer> weakRef = servers.get(id);
        if (weakRef != null) {
            return weakRef.get();
        }
        // not found
        return null;
    }

    /**
     * Add a new embedded server to the managed list.
     * @param embedded a given server to add.
     */
    public static void addEmbedded(final EZBServer embedded) {
        // get ID
        Integer id = embedded.getID();

        // build reference (weak)
        WeakReference<EZBServer> weakRef = new WeakReference<EZBServer>(embedded);

        // add reference with given id
        servers.put(id, weakRef);
    }

}
