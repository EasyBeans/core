/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: Enhancer.java 6002 2011-10-14 14:48:04Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer;

import static org.ow2.easybeans.enhancer.injection.InjectionClassAdapter.JAVA_LANG_OBJECT;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.api.loader.EZBClassLoader;
import org.ow2.easybeans.asm.ClassReader;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.ClassWriter;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.enhancer.bean.BeanClassAdapter;
import org.ow2.easybeans.enhancer.bean.Migration21ClassAdapter;
import org.ow2.easybeans.enhancer.injection.InjectionClassAdapter;
import org.ow2.easybeans.enhancer.interceptors.InterceptorClassAdapter;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This class is used for enhancing a set of classes (Beans like Stateless,
 * Stateful, MDB, etc).
 * @author Florent Benoit
 */
public class Enhancer {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(Enhancer.class);

    /**
     * Metadata of the classes of a given jar file.
     */
    private EjbJarArchiveMetadata ejbJarAnnotationMetadata = null;

    /**
     * Classloader used to define classes.
     */
    private ClassLoader writeLoader = null;

    /**
     * Classloader used to load classes.
     */
    private ClassLoader readLoader = null;

    /**
     * Map containing informations for enhancers.
     */
    private Map<String, Object> map = null;

    /**
     * Creates an new enhancer.
     * @param writeLoader classloader where to define enhanced classes.
     * @param ejbJarAnnotationMetadata object with references to the metadata.
     * @param map a map allowing to give some objects to the enhancer.
     */
    public Enhancer(final ClassLoader writeLoader, final EjbJarArchiveMetadata ejbJarAnnotationMetadata,
            final Map<String, Object> map) {
        this.writeLoader = writeLoader;
        this.ejbJarAnnotationMetadata = ejbJarAnnotationMetadata;
        this.map = map;
        if (writeLoader instanceof EZBClassLoader) {
            this.readLoader = ((EZBClassLoader) writeLoader).duplicate();
        } else {
            // Unable to duplicate classloader
            this.readLoader = writeLoader;
        }


    }

    /**
     * Enhance all classes which match beans, etc.
     * @throws EnhancerException if enhancing fails
     */
    public void enhance() throws EnhancerException {

        // Define all interceptors first.
        for (EasyBeansEjbJarClassMetadata classAnnotationMetadata : this.ejbJarAnnotationMetadata
                .getEjbJarClassMetadataCollection()) {
            if (classAnnotationMetadata.isInterceptor() && !classAnnotationMetadata.isBean()
                    && !classAnnotationMetadata.wasModified()) {
                logger.debug("ClassAdapter on interceptor : {0}", classAnnotationMetadata.getClassName());

                // enhance all super classes of the interceptor. (if any)
                // And do this only one time.
                enhanceSuperClass(classAnnotationMetadata, null);

                // Create ClassReader/Writer
                ClassReader cr = getClassReader(classAnnotationMetadata);
                ClassWriter cw = new EasyBeansClassWriter(this.readLoader);
                InterceptorClassAdapter cv = new InterceptorClassAdapter(classAnnotationMetadata, cw, this.readLoader);
                InjectionClassAdapter cv2 = new InjectionClassAdapter(classAnnotationMetadata, cv, this.map, null, false);
                cr.accept(cv2, 0);
                classAnnotationMetadata.setModified();
                defineClass(this.writeLoader, classAnnotationMetadata.getClassName().replace("/", "."), cw.toByteArray());
            }
        }
        // search all beans
        logger.info("Beans found are {0}", this.ejbJarAnnotationMetadata.getBeanNames());


        List<String> beanNames = this.ejbJarAnnotationMetadata.getBeanNames();
        for (String beanName : beanNames) {
            for (EasyBeansEjbJarClassMetadata classAnnotationMetadata : this.ejbJarAnnotationMetadata
                    .getClassesForBean(beanName)) {
                if (classAnnotationMetadata.isBean()) {

                    // First, enhance all super classes of the bean. (if any)
                    // And do this only one time.
                    enhanceSuperClass(classAnnotationMetadata, classAnnotationMetadata);
                    //logger.info("Enhancement of {0} done !", classAnnotationMetadata);

                    // Create ClassReader/Writer
                    ClassReader cr = getClassReader(classAnnotationMetadata);
                    ClassWriter cw = new EasyBeansClassWriter(this.readLoader);
                    BeanClassAdapter cv = new BeanClassAdapter(classAnnotationMetadata, cw);
                    InterceptorClassAdapter itcpClassAdapter = new InterceptorClassAdapter(classAnnotationMetadata, cv, this.readLoader);
                    InjectionClassAdapter cv2 = new InjectionClassAdapter(classAnnotationMetadata, itcpClassAdapter, this.map,
                            classAnnotationMetadata, false);

                    ClassVisitor beanVisitor = cv2;
                    // EJb 2.1 view ?
                    if (classAnnotationMetadata.getRemoteHome() != null || classAnnotationMetadata.getLocalHome() != null) {
                        Migration21ClassAdapter ejb21Adapter = new Migration21ClassAdapter(classAnnotationMetadata, cv2);
                        beanVisitor = ejb21Adapter;
                    }


                    cr.accept(beanVisitor, 0);

                    // define subclasses if interceptor enabled
                    loadDefinedClasses(this.writeLoader, itcpClassAdapter.getDefinedClasses());

                    defineClass(this.writeLoader, classAnnotationMetadata.getClassName().replace("/", "."), cw.toByteArray());

                }
            }
        }
    }


    /**
     * Enhance all super classes that are available.
     * @param classAnnotationMetadata the class where to lookup super classes.
     * @param beanClassMetadata the original class (bean class for example)
     * @throws EnhancerException if class can't be analyzed.
     */
    protected void enhanceSuperClass(final EasyBeansEjbJarClassMetadata classAnnotationMetadata,
            final EasyBeansEjbJarClassMetadata beanClassMetadata) throws EnhancerException {
        // First, enhance all super classes of the bean. (if any)
        // And do this only one time.
        String superClass = classAnnotationMetadata.getSuperName();
        if (!superClass.equals(JAVA_LANG_OBJECT)) {
            EasyBeansEjbJarClassMetadata superMetaData = classAnnotationMetadata.getLinkedClassMetadata(superClass);
            if (superMetaData != null && !superMetaData.wasModified()) {
                ClassReader cr = getClassReader(superMetaData);
                ClassWriter cw = new EasyBeansClassWriter(this.readLoader);
                InterceptorClassAdapter itcpClassAdapter = new InterceptorClassAdapter(superMetaData, cw, this.readLoader);
                InjectionClassAdapter cv = new InjectionClassAdapter(superMetaData, itcpClassAdapter, this.map,
                        beanClassMetadata, false);
                cr.accept(cv, 0);
                superMetaData.setModified();
                defineClass(this.writeLoader, superMetaData.getClassName().replace("/", "."), cw.toByteArray());
                enhanceSuperClass(superMetaData, beanClassMetadata);
            }
        }

    }

    /**
     * Load defined classes in the list.
     * @param loader classloader to use.
     * @param lst a list of new generated classes.
     */
    protected void loadDefinedClasses(final ClassLoader loader, final List<DefinedClass> lst) {
        if (lst != null) {
            for (DefinedClass definedClass : lst) {
                defineClass(loader, definedClass.getClassName(), definedClass.getBytes());
            }
        }
    }


    /**
     * Gets a class reader for a given metadata.
     * @param classAnnotationMetadata given metadata
     * @return classreader associated to the given metadata
     * @throws EnhancerException if no classWriter can be returned
     */
    protected ClassReader getClassReader(final EasyBeansEjbJarClassMetadata classAnnotationMetadata)
            throws EnhancerException {
        String className = classAnnotationMetadata.getClassName() + ".class";
        InputStream is = this.readLoader.getResourceAsStream(className);
        if (is == null) {
            throw new EnhancerException("Cannot find input stream in classloader " + this.readLoader + " for class " + className);
        }
        ClassReader cr = null;
        try {
            cr = new ClassReader(is);
        } catch (IOException e) {
            throw new EnhancerException("Cannot load input stream for class '" + className + "' in classloader '"
                    + this.readLoader, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                throw new EnhancerException("Cannot close input stream for class '" + className + "' in classloader '"
                        + this.readLoader, e);
            }
        }
        return cr;
    }

    /**
     * Loads/defines a class in the current class loader.
     * @param loader classloader to use.
     * @param className the name of the class
     * @param b the bytecode of the class to define
     */
    protected void defineClass(final ClassLoader loader, final String className, final byte[] b) {

        if (loader instanceof EZBClassLoader) {
            ((EZBClassLoader) loader).addClassDefinition(className, b);
        } else {
            // use other way of loading class.
            // override classDefine (as it is protected) and define the class.
            try {
                ///ClassLoader loader = Thread.currentThread().getContextClassLoader();
                Class<?> cls = Class.forName("java.lang.ClassLoader");
                java.lang.reflect.Method method = cls.getDeclaredMethod("defineClass", new Class[] {String.class,
                        byte[].class, int.class, int.class});

                // protected method invocaton
                method.setAccessible(true);
                try {
                    Object[] args = new Object[] {className, b, Integer.valueOf(0), Integer.valueOf(b.length)};
                    method.invoke(loader, args);
                } finally {
                    method.setAccessible(false);
                }
            } catch (InvocationTargetException ite) {
                if (ite.getCause() instanceof LinkageError) {
                    logger.error("Unable to define the class ''{0}''", className, ite);
                } else {
                    logger.warn("Unable to define the class ''{0}''", className, ite);
                }

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


    }

    /**
     * @return the ejbjar annotation metadata
     */
    protected EjbJarArchiveMetadata getEjbJarAnnotationMetadata() {
        return this.ejbJarAnnotationMetadata;
    }

    /**
     * @return map containing informations for enhancers.
     */
    protected Map<String, Object> getMap() {
        return this.map;
    }

    /**
     * @return the classloader used by this enhancer.
     */
    protected ClassLoader getClassLoader() {
        return this.writeLoader;
    }

}
