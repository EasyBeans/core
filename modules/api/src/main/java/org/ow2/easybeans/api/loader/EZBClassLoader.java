/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: EZBClassLoader.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.loader;

import javax.persistence.spi.ClassTransformer;

/**
 * Interface of an EasyBeans classloader which allow to add class transformers and class definitions.
 * @author Florent Benoit
 */
public interface EZBClassLoader {

    /**
     * Adds the bytecode for a given class. It will be used when class will be
     * loaded.
     * @param className the name of the class.
     * @param bytecode the bytes of the given class.
     */
    void addClassDefinition(final String className, final byte[] bytecode);

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
    void addTransformer(final ClassTransformer transformer);


    /**
     * Creates and returns a copy of this object.
     * It is used for example when Persistence Provider needs a new Temp classloader to load some temporary classes.
     * @return a copy of this object
     */
    ClassLoader duplicate();


}
