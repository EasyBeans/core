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
 * $Id: ArchiveInMemory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.tests.enhancer;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ow2.util.archive.api.IArchive;
import org.ow2.util.archive.impl.AbsArchiveImpl;

/**
 * Emulate an archive in memory.
 * @author Gael Lalire
 */
public class ArchiveInMemory extends AbsArchiveImpl implements IArchive {

    /**
     * Map which associate an url to a class name.
     */
    private Map<String, URL> urlMap;

    /**
     * Construct an archive in memory.
     * @param classLoader this classloader is use to load class by their name
     * @param classNames the name of classes in archive
     */
    public ArchiveInMemory(final ClassLoader classLoader, final List<String> classNames) {
        this.urlMap = new HashMap<String, URL>();
        for (String className : classNames) {
            this.urlMap.put(className, classLoader.getResource(className));
        }
    }

    /**
     * Close the archive.
     * @return true
     */
    public boolean close() {
        return true;
    }

    /**
     * @return ArchiveInMemory
     */
    public String getName() {
        return "ArchiveInMemory";
    }

    /**
     * @param clazz the class name
     * @return the url of a class.
     */
    public URL getResource(final String clazz) {
        return this.urlMap.get(clazz);
    }

    /**
     * @return all classes url
     */
    public Iterator<URL> getResources() {
        return this.urlMap.values().iterator();
    }

    /**
     * @param clazz the class name
     * @return a singleton iterator with url of the class
     */
    public Iterator<URL> getResources(final String clazz) {
        LinkedList<URL> linkedList = new LinkedList<URL>();
        linkedList.add(this.urlMap.get(clazz));
        return linkedList.iterator();
    }

    /**
     * The memory archive has no url.
     * @return null
     */
    public URL getURL() {
        return null;
    }

    /**
     * @return entries
     */
    public Iterator<String> getEntries() {
        return this.urlMap.keySet().iterator();
    }

}
