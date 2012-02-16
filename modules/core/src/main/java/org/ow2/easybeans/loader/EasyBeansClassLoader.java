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
 * $Id: EasyBeansClassLoader.java 5997 2011-10-13 15:12:47Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.spi.ClassTransformer;

import org.ow2.easybeans.api.loader.EZBClassLoader;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class defines the EasyBeans classloader. This classloader allows to set
 * the bytecode for a given class. Then, when the class will be loaded, it will
 * define the class by using the associated bytecode.
 * @author Florent Benoit
 */
public class EasyBeansClassLoader extends URLClassLoader implements EZBClassLoader {

    /**
     * Buffer length.
     */
    private static final int BUF_APPEND = 1000;

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EasyBeansClassLoader.class);

    /**
     * Need to recompute toString() value ? (urls have changed)
     * True by default (not done).
     * Then, compute is done only when required and if needed
     */
    private boolean recomputeToString = true;

    /**
     * String representation used by toString() method.
     */
    private String toStringValue = null;

    /**
     * Always transform class.
     */
    private boolean alwaysTransform = false;

    /**
     * Map between class name and the bytecode associated to the given
     * classname.
     */
    private Map<String, byte[]> mapDefined = new HashMap<String, byte[]>();

    /**
     * List of Class Transformers. Transformer is called when the Container
     * invokes at class-(re)definition time
     */
    private List<ClassTransformer> classTransformers = null;

    /**
     * Use the same constructors as parent class.
     * @param urls the URLs from which to load classes and resources
     * @param parent the parent class loader for delegation
     */
    public EasyBeansClassLoader(final URL[] urls, final ClassLoader parent) {
        super(urls, parent);
    }

    /**
     * Use the same constructors as parent class.
     * @param urls the URLs from which to load classes and resources
     */
    public EasyBeansClassLoader(final URL[] urls) {
        super(urls);
    }

    /**
     * Finds and loads the class with the specified name from the URL search
     * path. If this classloader has the bytecode for the associated class, it
     * defines the class.
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        Class<?> clazz = searchingDefinedClass(name);
        if (clazz != null) {
            return clazz;
        }

        return super.findClass(name);
    }

    /**
     * Defines the class by using the bytecode of the given classname.
     * @param className the name of the class.
     * @param bytecode the bytes of the given class.
     * @return the class which is now defined
     */
    private Class<?> defineInternalClass(final String className, final byte[] bytecode) {

       /* if (logger.isDebugEnabled()) {*/
            String fName = System.getProperty("java.io.tmpdir") + File.separator + className + ".class";
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(fName);
                fos.write(bytecode);
            } catch (IOException ioe) {
                throw new RuntimeException(ioe);
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        logger.debug("Cannot close stream for ''{0}''.", fName);
                    }
                }
            }
        /*}*/

        checkAndDefinePackage(className);
        try {
            return defineClass(className, bytecode, 0, bytecode.length);
        } catch (Error e) {
            if (e != null && e.getMessage().contains("duplicate class definition")) {
                logger.debug("Cannot invoke the defineClass method on the classloader", e);
            } else {
                logger.error("Cannot invoke the defineClass method on the classloader", e);
            }
            return null;
        }
    }

    /**
     * Adds the bytecode for a given class. It will be used when class will be
     * loaded.
     * @param className the name of the class.
     * @param bytecode the bytes of the given class.
     */
    public void addClassDefinition(final String className, final byte[] bytecode) {
        // check override ?
        if (this.mapDefined.get(className) != null) {
            logger.debug("There is already a bytecode defined for the class named '" + className
                    + "'. Not replacing. This could be due to a duplicated class in the given package.");
        }
        this.mapDefined.put(className, bytecode);
    }



    /**
     * When trying to load a class, look if this class needs to be defined.
     * @param name the class name to load.
     * @return the class if it was found.
     * @throws ClassNotFoundException if the class is not found.
     */
    @Override
    public Class<?> loadClass(final String name) throws ClassNotFoundException {
        searchingDefinedClass(name);

        // No transformers use the default mode
        if (this.classTransformers == null) {
            return super.loadClass(name, false);
        }

        // already loaded class, use the super method
        if (findLoadedClass(name) != null) {
            return super.loadClass(name, false);
        }

        // name of the resource to search
        StringBuilder sb = new StringBuilder(name.replace(".", "/"));
        sb.append(".class");
        String resourceName = sb.toString();

        // Check if the resource is in the given set of URLs.
        // If is not present, use the super method. (because the transformers will only apply on the given EJB3 which are URLs)
        URL resourceURL = findResource(resourceName);
        if (resourceURL == null) {
            return super.loadClass(name, false);
        }

        // Get the inputstream for the resource
        InputStream inputStream = getResourceAsStream(resourceName);

        // No resource found ? Throw exception
        if (inputStream == null) {
            throw new ClassNotFoundException("The resource '" + resourceName + "' was not found");
        }

        // Get Bytecode from the class
        byte[] bytes = null;
        try {
            bytes = readClass(inputStream);
        } catch (IOException e) {
            throw new ClassNotFoundException("Cannot read the class '" + "'.", e);
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
               logger.error("Cannot close the stream", e);
            }
        }

        // Need to provide the bytecode to the transformers
        boolean transformed = false;
        for (ClassTransformer classTransformer : this.classTransformers) {
            try {

                // Apply transformer
                byte[] updatedBytes = classTransformer.transform(this, name.replace(".", "/"), null, null, bytes);

                // Transformer has updated the bytes ? update the old one
                if (updatedBytes != null) {
                    bytes = updatedBytes;
                    transformed = true;
                }
            } catch (IllegalClassFormatException e) {
                throw new ClassNotFoundException("Cannot transform the resource '" + resourceName + "'", e);
            }
        }

        // Now that bytes has been changed (or not), define the class
        if (this.alwaysTransform || transformed) {
            return defineInternalClass(name, bytes);
        }

        // Unchanged, continue with default mechanism
        return super.loadClass(name, false);

    }

    /**
     * Checks if a Package object is defined for the given class.
     * If no package is defined, set a new one
     * @param className the given classname
     */
    protected void checkAndDefinePackage(final String className) {
        int i = className.lastIndexOf('.');
        // Found a package ?!
        if (i != -1) {
            String packageName = className.substring(0, i);
            // Package object already defined ?
            Package pkg = getPackage(packageName);
            if (pkg == null) {
                definePackage(packageName, null, null, null, null, null, null, null);
            }
        }
    }


    /**
     * Search a class in the local repository of classes to define.
     * @param className the name of the class to search and define if found.
     * @return the class if it was defined and loaded.
     */
    private Class<?> searchingDefinedClass(final String className) {
        // Defines the class if the bytecode is here.
        if (this.mapDefined != null) {
            byte[] defined = this.mapDefined.get(className);
            if (defined != null) {
                Class<?> clazz = defineInternalClass(className, defined);
                if (clazz != null) {
                    // remove defined class.
                    this.mapDefined.remove(className);
                    return clazz;
                }

            }
        }
        return null;
    }


    /**
     * Displays useful information.
     * @return information
     */
    @Override
    public String toString() {
        // urls have changed, need to build value
        if (this.recomputeToString) {
            computeToString();
        }
        return this.toStringValue;
    }

    /**
     * Compute a string representation used by toString() method.
     */
    private void computeToString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.getClass().getName());
        sb.append("[");
        sb.append("urls=");
        URL[] urls = getURLs();
        for (int u = 0; u < urls.length; u++) {
            sb.append(urls[u]);
            if (u != urls.length - 1) {
                sb.append(";");
            }
        }
        sb.append("]");
        this.toStringValue = sb.toString();

        // value is updated, no need to do it again.
        this.recomputeToString = false;
   }

    /**
     * Creates and returns a copy of this object.
     * It is used for example when Persistence Provider needs a new Temp classloader to load some temporary classes.
     * @return a copy of this object
     */
    public ClassLoader duplicate() {
        PrivilegedAction<EasyBeansClassLoader> privilegedAction = new PrivilegedAction<EasyBeansClassLoader>() {
            public EasyBeansClassLoader run() {
                return new EasyBeansClassLoader(getURLs(), getParent());
            }
        };
        return AccessController.doPrivileged(privilegedAction);
    }


    /**
     * Add a transformer supplied by the provider that will be called for every
     * new class definition or class redefinition that gets loaded by the loader
     * returned by the PersistenceInfo.getClassLoader method. The transformer
     * has no effect on the result returned by the
     * PersistenceInfo.getTempClassLoader method. Classes are only transformed
     * once within the same classloading scope, regardless of how many
     * persistence units they may be a part of.
     * @param transformer A provider-supplied transformer that the Container
     *        invokes at class-(re)definition time
     */
    public void addTransformer(final ClassTransformer transformer) {

        // init class transformers list if not set.
        if (this.classTransformers == null) {
            this.classTransformers = new ArrayList<ClassTransformer>();
        }
        this.classTransformers.add(transformer);
    }

    /**
     * Always transform class even if class transformer has not enhanced the class.
     * @param alwaysTransform the given parameter (false/true)
     */
    protected void setAlwaysTransform(final boolean alwaysTransform) {
        this.alwaysTransform = alwaysTransform;
    }


    /**
     * Gets the bytes from the given input stream.
     * @param is given input stream.
     * @return the array of bytes for the given input stream.
     * @throws IOException if class cannot be read.
     */
    private static byte[] readClass(final InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Given input stream is null");
        }
        byte[] b = new byte[is.available()];
        int len = 0;
        while (true) {
            int n = is.read(b, len, b.length - len);
            if (n == -1) {
                if (len < b.length) {
                    byte[] c = new byte[len];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
                return b;
            }
            len += n;
            if (len == b.length) {
                byte[] c = new byte[b.length + BUF_APPEND];
                System.arraycopy(b, 0, c, 0, len);
                b = c;
            }
        }
    }

}
