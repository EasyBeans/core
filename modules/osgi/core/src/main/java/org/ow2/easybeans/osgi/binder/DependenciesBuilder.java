/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: DependenciesBuilder.java 3493 2008-06-13 22:08:22Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.osgi.binder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import org.osgi.framework.BundleContext;
import org.ow2.easybeans.osgi.annotation.BindingMode;
import org.ow2.easybeans.osgi.annotation.Multiplicity;
import org.ow2.easybeans.osgi.annotation.OSGiResource;
import org.ow2.easybeans.osgi.binder.desc.DependencyDescription;
import org.ow2.easybeans.osgi.binder.desc.FieldDependencyDescription;
import org.ow2.easybeans.osgi.binder.desc.MethodsDependencyDescription;
import org.ow2.easybeans.osgi.binder.util.ReflectionHelper;

/**
 * Builds a list of dependencies from an instance.
 * @author Guillaume Sauthier
 */
public class DependenciesBuilder {

    /**
     * Configured instance, only used for initial injection.
     */
    private final Object instance;

    /**
     * The {@link BundleContext} to be injected if required by the bean.
     */
    private final BundleContext context;

    /**
     * Extracted dependencies.
     */
    private final List<DependencyDescription> dependencies;

    /**
     * @param instance instance to be analyzed.
     * @param context {@link BundleContext} to be injected if required.
     */
    public DependenciesBuilder(final Object instance,
                               final BundleContext context) {
        super();
        this.instance = instance;
        this.context = context;
        dependencies = new ArrayList<DependencyDescription>();
    }

    /**
     * Extract the list of dependencies.
     * @return the list of dependencies.
     */
    public List<DependencyDescription> extractDependencies() {

        Class<?> type = instance.getClass();

        // Process fields
        Class<?> processed = type;
        while (!Object.class.equals(processed)) {
            Field[] fields = processed.getDeclaredFields();
            for (int i = 0; i < fields.length; i++) {
                Field field = fields[i];
                if (field.isAnnotationPresent(OSGiResource.class)) {
                    processAnnotatedField(field);
                }
            }
            // Now, process parent class
            processed = processed.getSuperclass();
        }

        // Process methods
        processed = type;
        while (!Object.class.equals(processed)) {
            Method[] methods = processed.getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                Method method = methods[i];

                if (method.isAnnotationPresent(OSGiResource.class)) {
                    processAnnotatedMethod(method);
                }
            }
            // Now, process parent class
            processed = processed.getSuperclass();
        }

        return dependencies;
    }

    /**
     * Process a method
     * @param method to be processed
     */
    private void processAnnotatedMethod(final Method method) {

        OSGiResource osgi = method.getAnnotation(OSGiResource.class);

        // find type & multiplicity
        Class<?> type = null;
        Multiplicity multiplicity = null;
        if (Object.class.equals(osgi.type())) {
            // extract from method
            type = ReflectionHelper.findServiceInterface(method);
        } else {
            // read from annotation
            type = osgi.type();
            // needs multiplicity
            multiplicity = osgi.multiplicity();
        }

        if (BundleContext.class.equals(type)) {
            if (Multiplicity.MULTIPLE.equals(osgi.multiplicity())) {
                // Log a warning, and do like if it was a SINGLE
                // TODO
            }

            // Inject the BundleContext
            ReflectionHelper.invokeMethod(method, instance, context);
        } else {

            // get the name
            String name = null;
            if (!"".equals(osgi.name())) {
                // We have a name
                name = osgi.name();
            } else {
                // compute the name
                name = ReflectionHelper.getNameFromMethod(method);
            }

            // Now we have a name, sure :)
            // Try to get a dependency from the name
            DependencyDescription desc = findDependency(dependencies, name);
            if (desc == null) {
                // No description found, create a new one
                desc = new MethodsDependencyDescription(name);
                dependencies.add(desc);
            }

            if (!(desc instanceof MethodsDependencyDescription)) {
                throw new IllegalStateException("Cannot mix Fields and Method dependencies");
            }

            MethodsDependencyDescription description = (MethodsDependencyDescription) desc;

            // then process it

            // bind/unbind?
            switch (osgi.mode()) {
            case BIND:
                description.setBindMethod(method);
                break;
            case UNBIND:
                description.setUnbindMethod(method);
                break;
            case BIND_UNBIND:
                description.setBindMethod(method);
                description.setUnbindMethod(method);
                break;
            }

            description.setMultiplicity(multiplicity);
            description.setServiceInterface(type);

            if (!"".equals(osgi.filter())) {
                description.setFilter(osgi.filter());
            }
        }
    }

    /**
     * Process a field.
     * @param field to be processed
     */
    @SuppressWarnings("unchecked")
    private void processAnnotatedField(final Field field) {

        OSGiResource osgi = field.getAnnotation(OSGiResource.class);

        // Validate first
        if (!BindingMode.BIND_UNBIND.equals(osgi.mode())) {
            throw new IllegalStateException("Invalid BindingMode (" + osgi.mode()
                                            + ") on field (" + field.getName() + ")."
                                            + " Only BIND_UNBIND is allowed.");
        }

        // BundleContext injection ?
        // Do it now ...
        if (BundleContext.class.equals(field.getType())) {
            if (Multiplicity.MULTIPLE.equals(osgi.multiplicity())) {
                // Log a warning, and do like if it was a SINGLE
                // TODO
            }

            // Inject the BundleContext
            ReflectionHelper.setFieldValue(instance, field, context);
        } else {
            // Other type
            // Continue processing ...

            Multiplicity multiplicity = null;
            Class<?> type = null;

            // name it
            String name = field.getName();

            // find the type & multiplicity
            Class<?> fieldType = field.getType();
            if (fieldType.isArray()) {
                // Force MULTIPLE
                multiplicity = Multiplicity.MULTIPLE;
                if (fieldType.getComponentType().isArray()) {
                    // At least 2 dimension array, not supported
                    throw new IllegalStateException("Illegal dependency (" + name + ") on multiple dimension array ("
                                                    + fieldType + ")");
                }
                type = fieldType.getComponentType();
            } else if (Collection.class.isAssignableFrom(fieldType)) {
                // Got a collection

                multiplicity = Multiplicity.MULTIPLE;

                // is it initialized ?
                Object value = ReflectionHelper.getFieldValue(instance, field, Object.class);
                if (value == null) {

                    // Hmmm, we will initialize the value ourselves
                    if (List.class.isAssignableFrom(fieldType)) {
                        value = new ArrayList();
                    } else if (Set.class.isAssignableFrom(fieldType)) {
                        value = new HashSet();
                    } else if (Queue.class.isAssignableFrom(fieldType)) {
                        value = new LinkedList();
                    } else {
                        throw new IllegalStateException("Unmanaged Collection type (" + fieldType
                                                        + "). Prefer intialization ins constructor.");
                    }

                    // Inject it back
                    ReflectionHelper.setFieldValue(instance, field, value);
                }

                // find inner type
                // from annotation
                if (!Object.class.equals(osgi.type())) {
                    type = osgi.type();
                }
                // TODO infer from generics
            } else {
                // default case: the type is directly what we expect
                type = fieldType;
                multiplicity = Multiplicity.SINGLE;
            }

            // Describe the dependency

            FieldDependencyDescription description = new FieldDependencyDescription(name, field);
            description.setServiceInterface(type);
            description.setMultiplicity(multiplicity);

            if (!"".equals(osgi.filter())) {
                description.setFilter(osgi.filter());
            }

            dependencies.add(description);
        }
    }

    /**
     * Find a named dependency in the given dependencies list
     * @param dependencies list of dependencies
     * @param name searched dependency name
     * @return the named dependency or null if not found
     */
    private DependencyDescription findDependency(final List<DependencyDescription> dependencies,
                                                 final String name) {
        for (DependencyDescription description : dependencies) {
            if (description.getName().equals(name)) {
                return description;
            }
        }
        return null;
    }

}
