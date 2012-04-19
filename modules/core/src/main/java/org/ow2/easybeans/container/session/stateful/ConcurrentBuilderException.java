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

package org.ow2.easybeans.container.session.stateful;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.ejb.EJBException;

/**
 * Allows to build exception based on the spec level available on the platform.
 * @author Florent Benoit
 */
public class ConcurrentBuilderException {

    /**
     * Unique instance of this builder.
     */
    private static ConcurrentBuilderException unique = null;

    /**
     * Class used to instantiate javax.ejb.ConcurrentAccessTimeoutException exceptions.
     */
    private Class<?> concurrentTimeoutExceptionClass = null;

    /**
     * Constructor used to instantiate javax.ejb.ConcurrentAccessTimeoutException exceptions.
     */
    private Constructor<?> concurrentTimeoutExceptionConstructor = null;

    /**
     * Class used to instantiate javax.ejb.ConcurrentAccessException exceptions.
     */
    private Class<?> concurrentExceptionClass = null;

    /**
     * Constructor used to instantiate javax.ejb.ConcurrentAccessTimeoutException exceptions.
     */
    private Constructor<?> concurrentExceptionConstructor = null;

    /**
     * Default constructor.
     */
    public ConcurrentBuilderException() {

        // try to load the expecting class
        try {
            this.concurrentExceptionClass = ConcurrentBuilderException.class.getClassLoader().loadClass(
                    "javax.ejb.ConcurrentAccessException");
        } catch (ClassNotFoundException e) {
     // Not available
            //go back on EJBException
            this.concurrentExceptionClass = EJBException.class;
        }
        try {
            this.concurrentExceptionConstructor = this.concurrentExceptionClass.getConstructor(String.class);
        } catch (SecurityException e) {
            throw new EJBException("Unable to get constructor", e);
        } catch (NoSuchMethodException e) {
            throw new EJBException("Unable to get constructor", e);
        }

        // try to load the expecting class
        try {
            this.concurrentTimeoutExceptionClass = ConcurrentBuilderException.class.getClassLoader().loadClass(
                    "javax.ejb.ConcurrentAccessTimeoutException");
        } catch (ClassNotFoundException e) {
            // Not available
            // go back on EJBException
            this.concurrentTimeoutExceptionClass = EJBException.class;
        }

        try {
            this.concurrentTimeoutExceptionConstructor = this.concurrentTimeoutExceptionClass.getConstructor(String.class);
        } catch (SecurityException e) {
            throw new EJBException("Unable to get constructor", e);
        } catch (NoSuchMethodException e) {
            throw new EJBException("Unable to get constructor", e);
        }

    }


    /**
     * Build a new concurrent timeout exception.
     * @param message the given message to use
     * @return the exception built.
     */
    public EJBException concurrentTimeoutException(final String message) {
        try {
            return (EJBException) this.concurrentTimeoutExceptionConstructor.newInstance(message);
        } catch (InstantiationException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (IllegalAccessException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (IllegalArgumentException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (InvocationTargetException e) {
            throw new EJBException("Unable to build instance", e);
        }
    }

    /**
     * Build a new concurrent exception.
     * @param message the given message to use
     * @return the exception built.
     */
    public EJBException concurrentException(final String message) {
        try {
            return (EJBException) this.concurrentExceptionConstructor.newInstance(message);
        } catch (InstantiationException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (IllegalAccessException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (IllegalArgumentException e) {
            throw new EJBException("Unable to build instance", e);
        } catch (InvocationTargetException e) {
            throw new EJBException("Unable to build instance", e);
        }
    }

    /**
     * Build a new concurrent timeout exception.
     * @param message the given message to use
     * @return the exception built.
     */
    public static EJBException buildConcurrentTimeoutException(final String message) {
        return getInstance().concurrentTimeoutException(message);
    }

    /**
     * Build a new concurrent exception.
     * @param message the given message to use
     * @return the exception built.
     */
    public static EJBException buildConcurrentException(final String message) {
        return getInstance().concurrentException(message);
    }

    /**
     * @return unique instance
     */
    protected static ConcurrentBuilderException getInstance() {
        if (unique == null) {
            unique = new ConcurrentBuilderException();
        }
        return unique;
    }
}
