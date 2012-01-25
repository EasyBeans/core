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
 * $Id: ProDelegate.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.spi;

import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.rmi.CORBA.PortableRemoteObjectDelegate;

/**
 * Implementation of the prodelegate class that will redirect to the carol class
 * by using the correct classloader mechanism.
 * @author Florent Benoit
 */
public class ProDelegate implements PortableRemoteObjectDelegate {

    /**
     * Use the JDK logger (to avoid any dependency).
     */
    private static Logger logger = Logger.getLogger(ProDelegate.class.getName());

    /**
     * Carol ProDelegate class.
     */
    private static final String CAROL_PRO_DELEGATE = "org.objectweb.carol.rmi.multi.MultiPRODelegate";

    /**
     * Reference to the classloader to use. This classloader will be set by the
     * EasyBeans smartclient factory.
     */
    private static ClassLoader classLoader;

    /**
     * Wrapped reference of the carol PortableRemoteObjectDelegate class.
     */
    private PortableRemoteObjectDelegate wrapped = null;

    /**
     * Gets the Carol wrapping class.
     */
    public ProDelegate() {
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            Class<?> clazz = null;
            try {
                clazz = classLoader.loadClass(CAROL_PRO_DELEGATE);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("Cannot find the '" + CAROL_PRO_DELEGATE + "' class.", e);
            }
            try {
                wrapped = (PortableRemoteObjectDelegate) clazz.newInstance();
            } catch (InstantiationException e) {
                throw new IllegalStateException("Cannot build an instance of the '" + CAROL_PRO_DELEGATE + "' class.", e);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Cannot build an instance of the '" + CAROL_PRO_DELEGATE + "' class.", e);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Check if there is a classloader.
     */
    private void check() {
        if (classLoader == null) {
            throw new IllegalStateException("No classloader was set previously. Invalid call.");
        }

    }

    /**
     * Makes a Remote object ready for remote communication. This normally
     * happens implicitly when the object is sent or received as an argument on
     * a remote method call, but in some circumstances it is useful to perform
     * this action by making an explicit call.
     * @param target the object to connect.
     * @param source a previously connected object.
     * @throws RemoteException if <code>source</code> is not connected or if
     *         <code>target</code> is already connected to a different ORB
     *         than <code>source</code>.
     */
    public void connect(final Remote target, final Remote source) throws RemoteException {
        check();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "connect '" + target + "' with '" + source + "'.");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.connect(target, source);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Makes a server object ready to receive remote calls. Note that subclasses
     * of PortableRemoteObject do not need to call this method, as it is called
     * by the constructor.
     * @param obj the server object to export.
     * @exception RemoteException if export fails.
     */
    public void exportObject(final Remote obj) throws RemoteException {
        check();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "exportObject '" + obj + "'.");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.exportObject(obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }

    }

    /**
     * Checks to ensure that an object of a remote or abstract interface type
     * can be cast to a desired type.
     * @param narrowFrom the object to check.
     * @param narrowTo the desired type.
     * @return an object which can be cast to the desired type.
     * @throws ClassCastException if narrowFrom cannot be cast to narrowTo.
     */
    @SuppressWarnings("unchecked")
    public Object narrow(final Object narrowFrom, final Class narrowTo) throws ClassCastException {
        check();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Narrowing '" + narrowFrom + "' to '" + narrowTo + "'.");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.narrow(narrowFrom, narrowTo);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Returns a stub for the given server object.
     * @param obj the server object for which a stub is required. Must either be
     *        a subclass of PortableRemoteObject or have been previously the
     *        target of a call to {@link #exportObject}.
     * @return the most derived stub for the object.
     * @exception NoSuchObjectException if a stub cannot be located for the
     *            given server object.
     */
    public Remote toStub(final Remote obj) throws NoSuchObjectException {
        check();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "toStub '" + obj + "'.");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            return wrapped.toStub(obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Deregisters a server object from the runtime, allowing the object to
     * become available for garbage collection.
     * @param obj the object to unexport.
     * @exception NoSuchObjectException if the remote object is not currently
     *            exported.
     */
    public void unexportObject(final Remote obj) throws NoSuchObjectException {
        check();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "unexportObject '" + obj + "'.");
        }
        ClassLoader old = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);
        try {
            wrapped.unexportObject(obj);
        } finally {
            Thread.currentThread().setContextClassLoader(old);
        }
    }

    /**
     * Sets the ClassLoader to use.
     * @param cl the ClassLoader to use.
     */
    public static void setClassLoader(final ClassLoader cl) {
        classLoader = cl;
    }

}
