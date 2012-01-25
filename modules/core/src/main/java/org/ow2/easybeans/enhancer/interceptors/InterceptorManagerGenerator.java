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
 * $Id: InterceptorManagerGenerator.java 5997 2011-10-13 15:12:47Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import java.util.List;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.api.container.EZBEJBContext;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.EasyBeansClassWriter;

/**
 * This generates a class that manage the interceptor of a given bean. It
 * manages the lifecycle of the interceptors and allow to inject resources
 * (dependency injection).
 * @author Florent Benoit
 */
public class InterceptorManagerGenerator extends CommonClassGenerator {

    /**
     * Metadata to retrieve info on interceptors.
     */
    private EjbJarArchiveMetadata ejbJarAnnotationMetadata = null;

    /**
     * Name of the class to generate.
     */
    private String generatedClassName = null;

    /**
     * List of interceptors classes that are managed.
     */
    private List<String> allInterceptors = null;

    /**
     * Interface of this invocation context.
     */
    public static final String[] INTERFACES = new String[] {"org/ow2/easybeans/api/injection/EasyBeansInjection"};

    /**
     * EZBEJBContext type descriptor.
     */
    private static final String EZB_EJBCONTEXT_DESC  = Type.getDescriptor(EZBEJBContext.class);

    /**
     * Constructor.
     * @param ejbJarAnnotationMetadata the metadata to search interceptor class metadata.
     * @param generatedClassName the name of the class to generate.
     * @param allInterceptors interceptors that needs to be managed.
     * @param readLoader the classloader used to load classes.
     */
    public InterceptorManagerGenerator(final EjbJarArchiveMetadata ejbJarAnnotationMetadata,
            final String generatedClassName, final List<String> allInterceptors, final ClassLoader readLoader) {
        super(new EasyBeansClassWriter(readLoader));
        this.ejbJarAnnotationMetadata = ejbJarAnnotationMetadata;
        this.generatedClassName = generatedClassName;
        this.allInterceptors = allInterceptors;
    }

    /**
     * Generates the class. It call sub methods for being more clear for read
     * the code
     */
    public void generate() {
        addClassDeclaration();
        addAttributes();
        addConstructor();
        addMethods();
        endClass();
    }

    /**
     * Creates the declaration of the class with the given interfaces.
     */
    private void addClassDeclaration() {
        // create class
        getCW().visit(GENERATED_CLASS_VERSION, ACC_PUBLIC + ACC_SUPER, this.generatedClassName, null, "java/lang/Object",
                INTERFACES);
    }

    /**
     * Add attributes of the class. Attributes are interceptors names. ie :
     * private Interceptor interceptor;
     */
    private void addAttributes() {
        for (String interceptor : this.allInterceptors) {
            String fieldName = getField(interceptor);
            addAttribute(ACC_PRIVATE, fieldName, encodeClassDesc(interceptor));
        }
    }

    /**
     * Gets the field name for a given interceptor.
     * @param interceptorClass the given interceptor.
     * @return a field name.
     */
    private static String getField(final String interceptorClass) {
        return interceptorClass.replace("/", "");
    }

    /**
     * Gets the getter method name for a given interceptor.
     * @param interceptorClass the given interceptor.
     * @return a getter method name.
     */
    private static String getMethod(final String interceptorClass) {
        return "get" + interceptorClass.replace("/", "");
    }

    /**
     * Creates the constructor which should look like.
     *
     * <pre>
     * public XXXInterceptorManager() {
     *     this.interceptor = new Interceptor();
     *     this.interceptor2 = new Interceptor2();
     * }
     * </pre>
     */
    private void addConstructor() {
        // Generate constructor
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        // Call super constructor
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

        // Build instance of interceptors
        for (String interceptor : this.allInterceptors) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, interceptor);
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, interceptor, "<init>", "()V");
            mv.visitFieldInsn(PUTFIELD, this.generatedClassName, getField(interceptor), encodeClassDesc(interceptor));
        }

        // need to add return instruction
        mv.visitInsn(RETURN);

        // visit max compute automatically
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Add methods of the class.
     */
    private void addMethods() {
        addGetterMethods();
        addInjectedMethod();
        addDefaultMethods();
    }

    /**
     * Generated methods allowing to set a context and a factory. This allows to
     * set on injectors the bean's session context and its factory.
     */
    private void addDefaultMethods() {
        // Adds the factory attribute and its getter/setter.
        CommonClassGenerator.addFieldGettersSetters(getCW(), this.generatedClassName, "easyBeansFactory", Factory.class);

        // Adds the sessionContext attribute and its getter/setter.
        CommonClassGenerator.addFieldGettersSetters(getCW(), this.generatedClassName, "easyBeansContext",
                EZBEJBContext.class);
    }


    /**
     * Generates a getter method for each interceptor. ie :
     *
     * <pre>
     * public Interceptor getInterceptor() {
     *     return interceptor;
     * }
     * </pre>
     */
    private void addGetterMethods() {
        for (String interceptor : this.allInterceptors) {
            MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, getMethod(interceptor),
                    "()" + encodeClassDesc(interceptor), null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, getField(interceptor), encodeClassDesc(interceptor));
            mv.visitInsn(ARETURN);
            mv.visitMaxs(0, 0);
            mv.visitEnd();
        }
    }

    /**
     * For each interceptor, call injectedByEasyBeans on it (only if these
     * interceptors have been analyzed).
     *
     * <pre>
     * public void injectedByEasyBeans() {
     *     interceptorXX.setEasyBeansContext(easyBeansContext);
     *     interceptorXX.injectedByEasyBeans();
     *     interceptorXX.injectedByEasyBeans();
     * }
     * </pre>
     */
    private void addInjectedMethod() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "injectedByEasyBeans", "()V", null, null);
        mv.visitCode();

        for (String interceptor : this.allInterceptors) {
            // if interceptor has been analyzed, call injectedByEasyBeans method
            // Set also the bean's context and its factory.
            if (this.ejbJarAnnotationMetadata.getScannedClassMetadata(interceptor) != null) {
                String fieldName = getField(interceptor);

                // interceptor.setEasyBeansContext(easyBeansContext);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, fieldName, encodeClassDesc(interceptor));
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, "easyBeansContext",
                        EZB_EJBCONTEXT_DESC);
                mv.visitMethodInsn(INVOKEVIRTUAL, interceptor, "setEasyBeansContext",
                        "(" + EZB_EJBCONTEXT_DESC + ")V");

                // interceptorXX.injectedByEasyBeans();
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, fieldName, encodeClassDesc(interceptor));
                mv.visitMethodInsn(INVOKEVIRTUAL, interceptor, "injectedByEasyBeans", "()V");
            }
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Called when the generated class is done.
     */
    private void endClass() {
        getCW().visitEnd();
    }

    /**
     * @return the bytecode of the generated class.
     */
    public byte[] getBytes() {
        return getCW().toByteArray();
    }

}
