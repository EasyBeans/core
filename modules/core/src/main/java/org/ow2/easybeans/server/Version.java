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
 * $Id: Version.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.server;

/**
 * Sets the EasyBeans version.
 * @author Florent Benoit
 */
public final class Version {

    /**
     * The Version number is read from the package.
     * Default Version number of EasyBeans if package version not available.
     */
    private static final String DEFAULT_VERSION_NUMBER = "1.2.x";

    /**
     * Version number.
     */
    private static String versionNumber = null;

    /**
     * No public constructor. (utility class)
     */
    private Version() {

    }

    /**
     * @return Returns the EasyBeans Version Number.
     */
    public static String getVersion() {
        if (versionNumber == null) {
            // Read version from the package
            Package pkg = Version.class.getPackage();
            if (pkg != null) {
                String implVersion = pkg.getImplementationVersion();
                if (implVersion != null) {
                    versionNumber = implVersion;
                }
            }
            // not found, return default value
            if (versionNumber == null || versionNumber.length() == 0) {
                versionNumber = DEFAULT_VERSION_NUMBER;
            }
        }

        return versionNumber;
    }

    /**
     * Display the current EasyBeans version.
     * @param args the given arguments
     */
    public static void main(final String[] args) {
       System.out.println("Version is : " + getVersion());

    }

}
