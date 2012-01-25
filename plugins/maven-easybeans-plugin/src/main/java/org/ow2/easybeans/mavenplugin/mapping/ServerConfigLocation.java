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
 * $Id: ServerConfigLocation.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.mavenplugin.mapping;

import java.util.Collections;
import java.util.List;


/**
 * Access to user defined configuration file of EasyBeans. This class is mapped
 * by Maven with the POM configuration file.
 * @author Vincent Michaud
 */
public class ServerConfigLocation {

    /**
     * Location of the EasyBeans configuration file.
     */
    private String file = null;

    /**
     * Location of additionnal and partial EasyBeans configuration files.
     */
    private List<String> extraLocations = Collections.emptyList();

    /**
     * Get location of the EasyBeans configuration file.
     * @return Location of the EasyBeans configuration file, or null if
     *         not specified
     */
    public String getConfigFileLocation() {
        if (file == null || file.length() == 0) {
            return null;
        }
        return file;
    }

    /**
     * Get location of the partial EasyBeans configuration files.
     * @return Location of the EasyBeans configuration files, or null if
     *         any specified file
     */
    public List<String> getPartialConfigFileLocations() {
        if (extraLocations == null || extraLocations.isEmpty()) {
            return null;
        }
        return extraLocations;
    }

}
