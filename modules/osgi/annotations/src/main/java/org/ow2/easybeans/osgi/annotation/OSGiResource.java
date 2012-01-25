/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: OSGiResource.java 5371 2010-02-24 15:02:00Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Allows to inject OSGi resources.
 * @author Florent BENOIT
 */
@Target({METHOD, FIELD})
@Retention(RUNTIME)
public @interface OSGiResource {

    /**
     * This attribute denote the name of the dependency.<br/>
     * It is only useful when the annotation is placed on a method.
     * Without this attribute, it is discovered from the method name.
     * Example:
     * <tt>bind[DependencyName]</tt> gives <tt>dependencyName</tt>
     *
     * Recognised patterns:
     * <ul>
     *   <li>bind[DependencyName]</li>
     *   <li>unbind[DependencyName]</li>
     *   <li>set[DependencyName]</li>
     *   <li>reset[DependencyName]</li>
     *   <li>unset[DependencyName]</li>
     *   <li>add[DependencyName]</li>
     *   <li>remove[DependencyName]</li>
     * </ul>
     */
    String name() default "";

    /**
     * When this annotation is used to declare a service dependency,
     * it may be useful to specify a fine dependency selection, by
     * using an LDAP filter.<br/>
     * for a field:
     * <code>
     *  @OSGiResource(filter="(property=value)")
     *  private &lt;service-interface> field;
     * </code>
     *
     * for a method:
     * <code>
     *  @OSGiResource(filter="(property=value)")
     *  private void &lt;method-name>(&lt;service-interface> p) {
     *    // ...
     *  }
     * </code>

     */
    String filter() default "";

    /**
     * When the cardinality of the dependency is multiple, sometime,
     * using a generic List (with no type information), the required
     * type is missing.<br/>
     * For a field:
     * <code>
     *  @OSGiResource(type=&lt;service-interface>.class)
     *  private List field;
     * </code>
     *
     * For a method:
     * <code>
     *  @OSGiResource(type=&lt;service-interface>.class)
     *  private void &lt;method-name>(List p) {
     *    // ...
     *  }
     * </code>

     */
    Class<?> type() default Object.class;

    /**
     * When @OSGiResource is used on a field, the mode has to be set
     * to BIND_UNBIND (otherwise, this is an error), nevertheless, this
     * is the default value, so by default it will works.<br/>
     *
     * When used on a method, it will determine for what this method will be used:
     * <ul>
     *  <li>BIND: The method will be used for service injection</li>
     *  <li>UNBIND: The method will be used when the dependencies is going down (the
     *  requester have to release the resource)</li>
     *  <li>BIND_UNBIND: The method is used for both BIND and UNBIND operations, when
     *  unbindig, the service reference is <tt>null</tt></li>
     * </ul>
     *
     */
    BindingMode mode() default BindingMode.BIND_UNBIND;

    /**
     * Set the dependency multiplicity:
     * <ul>
     *   <li>SINGLE: Only one dependency is needed</li>
     *   <li>MULTIPLE: Multiple dependencies (List or array)</li>
     * </ul>
     * Having MULTIPLE for <tt>BundleContext</tt> injection is FORBIDDEN.
     */
    Multiplicity multiplicity() default Multiplicity.SINGLE;
}
