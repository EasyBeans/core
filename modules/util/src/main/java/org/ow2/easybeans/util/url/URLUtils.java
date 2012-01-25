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
 * $Id: URLUtils.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.util.url;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * This class is used to :<br>
 * <ul>
 * <li> - get a File from an URL</li>
 * <li> - get an URL from a file</li>
 * </ul>
 * In fact, when there are spaces in a directory name, file.toURL() doesn't
 * escape spaces into %20, this is why this class will help.<br>
 * This class has to be used both for encoding and decoding URL.<br>
 * There are methods with checked exceptions or unchecked exceptions.
 * @author Florent Benoit
 */
public final class URLUtils {

    /**
     * File protocol.
     */
    public static final String FILE_PROTOCOL = "file";

    /**
     * Utility class.
     */
    private URLUtils() {

    }

    /**
     * Gets an URL from a given file.(throws only runtime exception).
     * @param file the given file
     * @return the URL that has been built
     */
    public static URL fileToURL(final File file) {
        try {
            return fileToURL2(file);
        } catch (URLUtilsException e) {
            throw new IllegalArgumentException("Cannot get URL from the given file '" + file + "'.", e);
        }
    }

    /**
     * Gets an URL from a given file.
     * @param file the given file
     * @return the URL that has been built
     * @throws URLUtilsException if the URL cannot be get from file.
     */
    public static URL fileToURL2(final File file) throws URLUtilsException {
        // check
        if (file == null) {
            throw new URLUtilsException("Invalid File. It is null");
        }

        // transform
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new URLUtilsException("Cannot get URL from the given file '" + file + "'.", e);
        }
    }

    /**
     * Gets a File object for the given URL.
     * @param url the given url.
     * @return File object
     */
    public static File urlToFile(final URL url) {
        try {
            return urlToFile2(url);
        } catch (URLUtilsException e) {
            throw new IllegalArgumentException("Cannot get File from the given url '" + url + "'.", e);
        }
    }

    /**
     * Gets a File object for the given URL.
     * @param url the given url.
     * @return File object
     * @throws URLUtilsException if File cannot be get from URL
     */
    public static File urlToFile2(final URL url) throws URLUtilsException {
        // check
        if (url == null) {
            throw new URLUtilsException("Invalid URL. It is null");
        }

        // Validate protocol
        if (!url.getProtocol().equals(FILE_PROTOCOL)) {
            throw new URLUtilsException("Invalid protocol named '" + url.getProtocol() + "'. Protocol should be '"
                    + FILE_PROTOCOL + "'.");
        }

        try {
            return new File(new URI(url.toString()));
        } catch (URISyntaxException e) {
            throw new URLUtilsException("Cannot get File from the given url '" + url + "'.", e);
        }
    }
}
