/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: BundleArchive.java 3603 2008-07-04 15:43:00Z gaellalire $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.archive;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.osgi.framework.BundleContext;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.AbsArchiveImpl;

/**
 * The <code>BundleArchive</code> class is specialized to serve EjbJars contained in OSGi Bundle.
 * @author Guillaume Sauthier
 */
public class BundleArchive extends AbsArchiveImpl implements IArchive {

    /**
     * BundleContext where resources can be found.
     */
    private BundleContext bc = null;

    /**
     * Build a BundleArchive from a BundleContext.
     * @param bc the BundleContext
     */
    public BundleArchive(final BundleContext bc) {
        super();
        this.bc = bc;

        // Init Metadata
        initMetadata();
    }

    /**
     * Release the BundleContext.
     * @return always true.
     */
    public boolean close() {
        return true;
    }

    /**
     * @return Returns the Bundle Symbolic Name.
     */
    public String getName() {
        return bc.getBundle().getSymbolicName();
    }

    /**
     * @param resourceName Resource name to be looked up.
     * @return Returns the URL pointing to the Resource (the Bundle ClassLoader is used for the search).
     * @throws ArchiveException if unable to load the resource.
     * @see org.ow2.util.archive.api.IArchive#getResource(java.lang.String)
     */
    public URL getResource(final String resourceName) throws ArchiveException {
        return bc.getBundle().getResource(resourceName);
    }

    /**
     * @return Returns The "List" of all resources of that Bundle.
     * @throws ArchiveException if unable to load a resource.
     */
    public Iterator<URL> getResources() throws ArchiveException {

        // TODO: There is a lot of work to do here (Refactor + Cleanup)...
        // For now, this is only an Iteration over the jar files, but it
        // should discover direct classes of the bundle too

        // return all availables resources URLs
        Enumeration<?> e = bc.getBundle().findEntries("", "*.jar", true);

        // create a list (have an Iterator attached)
        List<URL> list = new ArrayList<URL>();

        // Iterates over the contained jar files if any
        if (e != null) {

            // For each contained jar file
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                URL jarURL = null;
                // creates a jar: URL
                try {
                    jarURL = new URL("jar:" + url + "!/");
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                JarURLConnection connection = null;
                try {
                    connection = (JarURLConnection) jarURL.openConnection();
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }

                // creates a JarFile from the connection ...
                JarFile jf = null;
                try {
                    jf = connection.getJarFile();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // ... and iterates over the entries
                Enumeration<JarEntry> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    URL entryURL = null;
                    try {
                        entryURL = new URL(jarURL, entry.getName());
                    } catch (MalformedURLException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }

                    // Finally add the URL to the list
                    list.add(entryURL);
                }
                try {
                    jf.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }

        // 2. Returns direct resources too
        e = bc.getBundle().findEntries("", "*", true);

        // Iterates over the entries if any
        if (e != null) {

            // For each resource
            for (; e.hasMoreElements();) {
                URL url = (URL) e.nextElement();
                list.add(url);
            }
        }
        return list.iterator();
    }

    /**
     * In the OSGi case, that Iterator always contains only 1 item.
     * @param resourceName Resource name to be searched.
     * @return Returns an Iterator
     * @throws ArchiveException if unable to load the resource.
     * @see org.ow2.util.archive.api.IArchive#getResources(java.lang.String)
     */
    public Iterator<URL> getResources(final String resourceName)
            throws ArchiveException {
        List<URL> list = new ArrayList<URL>();
        list.add(getResource(resourceName));
        return list.iterator();
    }

    /**
     * @return Returns the URL that will be the {@link org.ow2.easybeans.loader.EasyBeansClassLoader} base.
     * @throws ArchiveException if unable to load the root URL of that Bundle.
     * @see org.ow2.util.archive.api.IArchive#getURL()
     */
    public URL getURL() throws ArchiveException {
        return bc.getBundle().getEntry("/");
    }

    /**
     * Init metadata by reading the headers of the bundle.
     */
    protected void initMetadata() {
        // Get headers
        Dictionary<?, ?> dictionary = bc.getBundle().getHeaders();
        Enumeration<?> keys = dictionary.keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            getMetadata().put(key.toString(), dictionary.get(key).toString());
        }
    }

    /**
     * @return entries of bundle.
     */
    public Iterator<String> getEntries() {

        // create a list (have an Iterator attached)
        List<String> list = new ArrayList<String>();

        String rootUrl = bc.getBundle().getEntry("/").toExternalForm();

        // return all availables resources URLs
        Enumeration<?> e = bc.getBundle().findEntries("", "*.jar", true);


        // Iterates over the contained jar files if any
        if (e != null) {

            // For each contained jar file
            while (e.hasMoreElements()) {
                URL url = (URL) e.nextElement();
                URL jarURL = null;
                // creates a jar: URL
                try {
                    jarURL = new URL("jar:" + url + "!/");
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                JarURLConnection connection = null;
                try {
                    connection = (JarURLConnection) jarURL.openConnection();
                } catch (IOException e2) {
                    // TODO Auto-generated catch block
                    e2.printStackTrace();
                }

                // creates a JarFile from the connection ...
                JarFile jf = null;
                try {
                    jf = connection.getJarFile();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

                // ... and iterates over the entries
                Enumeration<JarEntry> entries = jf.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    // Finally add the URL to the list
                    list.add(entry.getName());
                }
                try {
                    jf.close();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

            }
        }

        // 2. Returns direct resources too
        e = bc.getBundle().findEntries("", "*", true);

        // Iterates over the entries if any
        if (e != null) {

            // For each resource
            for (; e.hasMoreElements();) {
                URL url = (URL) e.nextElement();

                list.add(url.toExternalForm().substring(rootUrl.length()));
            }
        }
        return list.iterator();

    }

}
