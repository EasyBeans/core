/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.ejbinwar;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.AbsArchiveImpl;

/**
 * Wrap a War archive into a EJB Archive.
 * @author Florent Benoit
 */
public class EJBInWarArchive extends AbsArchiveImpl implements IArchive {

    /**
     * WEB-INF entry.
     */
    private static final String WEB_INF = "WEB-INF/";

    /**
     * WEB-INF/ejb-jar.xml.
     */
    private static final String WEB_INF_EJBJAR_XML = WEB_INF.concat("ejb-jar.xml");

    /**
     * META-INF/ejb-jar.xml.
     */
    public static final String META_INF_EJBJAR_XML = "META-INF/ejb-jar.xml";

    /**
     * WEB-INF/classes folder.
     */
    private static final String WEB_INF_CLASSES = WEB_INF.concat("classes");

    /**
     * Wrapped Archive.
     */
    private IArchive wrappedWarArchive = null;

    /**
     * Resource names of the entries.
     */
    private Map<String, String> nameEntries = null;

    /**
     * URL entries of the EJB3 (based on the war).
     */
    private List<URL> urlEntries = null;

    /**
     * Build a new EJB archive based on the given WAR archive.
     * @param wrappedWarArchive the archive to wrap
     * @throws ArchiveException if archive can't be transformed
     */
    public EJBInWarArchive(final IArchive wrappedWarArchive) throws ArchiveException {
        super();
        this.wrappedWarArchive = wrappedWarArchive;
        this.nameEntries = new HashMap<String, String>();
        this.urlEntries = new ArrayList<URL>();
        init();
    }

    /**
     * Initialize the archive.
     * @throws ArchiveException if entries can't be read
     */
    public void init() throws ArchiveException {

        // Init entries
        Iterator<String> itEntries = this.wrappedWarArchive.getEntries();
        while (itEntries.hasNext()) {
            String wrappedEntry = itEntries.next();
            // Handle special case of ejb-jar.xml
            if (WEB_INF_EJBJAR_XML.equals(wrappedEntry)) {
                // add it on our own entry
                addEntry(META_INF_EJBJAR_XML, wrappedEntry);
            }

            // Filter WEB-INF/ resources
            if (wrappedEntry.startsWith(WEB_INF)) {

                // remove WEB-INF/classes
                if (wrappedEntry.startsWith(WEB_INF_CLASSES)) {
                    addEntry(wrappedEntry.substring(WEB_INF_CLASSES.length() + 1), wrappedEntry);
                }
            }
        }

    }

    /**
     * Add the given resource name on this archive which is acting as the delegating entry.
     * @param name the resource name
     * @param wrappedEntry the entry to wrap
     * @throws ArchiveException if entry can't be added
     */
    protected void addEntry(final String name, final String wrappedEntry) throws ArchiveException {
        // Add the resource name
        this.nameEntries.put(name, wrappedEntry);

        // Add URL entry
        this.urlEntries.add(this.wrappedWarArchive.getResource(wrappedEntry));
    }

    /**
     * Close the underlying Resource.
     * @return Returns <code>true</code> if the close was succesful,
     *         <code>false</code> otherwise.
     */
    public boolean close() {
        return this.wrappedWarArchive.close();
    }

    /**
     * @return Returns all resources name in the archive.
     * @throws ArchiveException if method fails.
     */
    public Iterator<String> getEntries() throws ArchiveException {
        // Return our own filtered entries
        return this.nameEntries.keySet().iterator();
    }

    /**
     * @return a description of this archive. This name could be used in logger
     *         info.
     */
    public String getName() {
        // Prefix the name
        return "EJBInWar[".concat(this.wrappedWarArchive.getName()).concat("]");
    }

    /**
     * @param resourceName The resource name to be looked up.
     * @return Returns the resource URL if the resource has been found. null
     *         otherwise.
     * @throws ArchiveException if method fails.
     */
    public URL getResource(final String resourceName) throws ArchiveException {
        String wrappedEntry = this.nameEntries.get(resourceName);
        if (wrappedEntry != null) {
            return this.wrappedWarArchive.getResource(wrappedEntry);
        }
        // Not found
        return null;
    }

    /**
     * @return Returns an Iterator of Resource's URL.
     * @throws ArchiveException if method fails.
     */
    public Iterator<URL> getResources() throws ArchiveException {
        return this.urlEntries.iterator();
    }

    /**
     * @param resourceName The resource name to be looked up.
     * @return Returns an Iterator of matching resources.
     * @throws ArchiveException if method fails.
     */
    public Iterator<URL> getResources(final String resourceName) throws ArchiveException {
        return this.wrappedWarArchive.getResources(this.nameEntries.get(resourceName));
    }

    /**
     * Returns an URL that can be used to access the resource described by this
     * IArchive. This URL MUST be canonicalized (at least when this is a file)
     * because it is often used as a key.
     * @return Returns the resource URL.
     * @throws ArchiveException if method fails.
     */
    public URL getURL() throws ArchiveException {
        return this.wrappedWarArchive.getURL();
    }

}
