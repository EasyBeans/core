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
 * $Id: Version.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A class for accessing to the EasyBeans version of the local server.
 * @author Vincent Michaud
 */
public final class Version {

    /**
     * Constructor.
     */
    private Version() {
    }

    /**
     * Get the version of EasyBeans server of the plugin.
     * @return A String
     */
    public static String getServerVersion() {
        return removeRevision(org.ow2.easybeans.server.Version.getVersion());
    }

    /**
     * Remove the revision of the version.
     * @param version The version
     * @return The cleaned version
     */
    public static String removeRevision(final String version) {
        Pattern pattern = Pattern.compile(".+([.][0-9]+)$");
        Matcher matcher = pattern.matcher(version);
        if (matcher.matches()) {
            return version.substring(0, version.lastIndexOf("."));
        }
        return version;
    }
}
