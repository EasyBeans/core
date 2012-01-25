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
 * $Id: EasyBeansInvocationContextGenerator.java 5997 2011-10-13 15:12:47Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer.interceptors;

import static org.ow2.util.ee.metadata.ejbjar.api.InterceptorType.AROUND_INVOKE;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.ow2.easybeans.api.EasyBeansInvocationContext;
import org.ow2.easybeans.asm.Label;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Type;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.CommonClassGenerator;
import org.ow2.easybeans.enhancer.EasyBeansClassWriter;
import org.ow2.easybeans.enhancer.lib.MethodRenamer;
import org.ow2.util.ee.metadata.ejbjar.api.IJClassInterceptor;
import org.ow2.util.ee.metadata.ejbjar.api.InterceptorType;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.scan.api.metadata.structures.JMethod;

/**
 * Generates the implementation of
 * {@link org.ow2.easybeans.api.EasyBeansInvocationContext} interface for a
 * given business method.
 * @author Florent Benoit
 */
public class EasyBeansInvocationContextGenerator extends CommonClassGenerator {

    /**
     * Prefix used as package name for generated classes
     * (EasyBeansInvocationContext* impl).
     */
    public static final String PACKAGE_NAME_PREFIX = "org.ow2.easybeans.gen.invocationcontext.";

    /**
     * Name of the attributes will start with this name with an index as suffix,
     * ie : arg0, arg1, arg2,...
     */
    public static final String ARG = "arg";

    /**
     * Name of the interceptor attributes will start with this name with an index as suffix,
     * ie : interceptor0, interceptor1, interceptor2,...
     */
    public static final String INTERCEPTOR = "interceptor";

    /**
     * Suffix for generated classes EasyBeansInvocationContextImpl.
     */
    public static final String SUFFIX_CLASS = "EasyBeansInvocationContextImpl";

    /**
     * Interface of this invocation context.
     */
    public static final String[] INTERFACES = new String[] {"org/ow2/easybeans/api/EasyBeansInvocationContext"};

    /**
     * Exceptions of the proceed method.
     */
    public static final String[] PROCEED_EXCEPTIONS = new String[] {Type.getInternalName(Exception.class)};

    /**
     * EasyBeansInvocationContext interface.
     */
    public static final String EASYBEANS_INVOCATION_CONTEXT =  Type.getDescriptor(EasyBeansInvocationContext.class);

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(EasyBeansInvocationContextGenerator.class);

    /**
     * Metadata available for a class (extracted from method metadat object.
     * (parent))
     */
    private EasyBeansEjbJarClassMetadata classAnnotationMetadata = null;

    /**
     * Package name which is used for generating class.
     */
    private String packageName = null;

    /**
     * Full class name of the generated class (prefixed by packageName).
     */
    private String generatedClassName = null;

    /**
     * JMethod object which correspond to the current method metadata which is
     * used.
     */
    private JMethod jMethod = null;

    /**
     * Metadata available for a method (given as constructor arg).
     */
    private EasyBeansEjbJarMethodMetadata methodAnnotationMetadata;

    /**
     * Bean class descriptor.
     */
    private String beanClassDesc = null;

    /**
     * Bean class name.
     */
    private String beanClassName = null;

    /**
     * Bean class Type (ASM).
     */
    private Type beanClassType = null;

    /**
     * ASM descriptor of the generated constructor.
     */
    private String constructorDesc = null;

    /**
     * ASM Type arguments of the method.
     */
    private Type[] methodArgsType = null;

    /**
     * List of interceptors.
     */
    private List<IJClassInterceptor> allInterceptors = null;

    /**
     * Type of the interceptor (AroundInvoke, PostConstruct, etc).
     */
    private InterceptorType interceptorType = null;

    /**
     * Name of the interceptor manager class.
     */
    private String interceptorManagerClassName = null;

    /**
     * Suffix for InterceptorManager.
     */
    public static final String SUFFIX_INTERCEPTOR_MANAGER = "InterceptorManager";

    /**
     * Classloader used to load classes.
     */
    private ClassLoader readLoader = null;

    /**
     * Constructor It will generate a class for the given method metadata.
     * @param methodAnnotationMetadata method meta data
     * @param interceptorType the type of invocationContext to generate (AroundInvoke, PostConstruct, etc)
     * @param readLoader the classloader used to load classes.
     */
    public EasyBeansInvocationContextGenerator(final EasyBeansEjbJarMethodMetadata methodAnnotationMetadata,
            final InterceptorType interceptorType, final ClassLoader readLoader) {
        super(new EasyBeansClassWriter(readLoader));
        this.methodAnnotationMetadata = methodAnnotationMetadata;
        this.classAnnotationMetadata = methodAnnotationMetadata.getClassMetadata();
        this.jMethod = methodAnnotationMetadata.getJMethod();

        // package name is prefixed
        this.packageName = PACKAGE_NAME_PREFIX + this.classAnnotationMetadata.getClassName();

        // Type of the generated interceptor
        this.interceptorType = interceptorType;

        this.interceptorManagerClassName = this.classAnnotationMetadata.getClassName() + SUFFIX_INTERCEPTOR_MANAGER;


        // Name of the class that is generated
        this.generatedClassName = this.packageName.replace(".", "/") + "/" + SUFFIX_CLASS;
        this.generatedClassName += methodAnnotationMetadata.getJMethod().getName() + interceptorType.name().replace("_", "");
        // Also, as two methods with the same name but different parameters will produce the same class name,
        // add the hashcode of the ASM descriptor.
        this.generatedClassName += Math.abs(methodAnnotationMetadata.getJMethod().getDescriptor().hashCode());

        // useful constants
        this.beanClassDesc = encodeClassDesc(this.classAnnotationMetadata.getClassName());
        this.beanClassName = this.classAnnotationMetadata.getClassName();
        this.beanClassType = Type.getType(this.beanClassDesc);

        // type arguments of the method
        this.methodArgsType = Type.getArgumentTypes(this.jMethod.getDescriptor());

        // Get interceptors
        this.allInterceptors = new MethodInterceptorsBuilder(methodAnnotationMetadata, interceptorType).getAllInterceptors();

    }

    /**
     * Generates the class. It call sub methods for being more clear for read
     * the code
     */
    public void generate() {
        if (logger.isDebugEnabled()) {
            logger.debug("Generating InvocationContext for Method " + this.jMethod + " of class " + this.beanClassName);
        }

        addClassDeclaration();
        addAttributes();
        addConstructor();
        addStaticClassInitialization();
        addMethods();
        endClass();


        if (logger.isDebugEnabled()) {
            String fName = System.getProperty("java.io.tmpdir") + File.separator
                + this.generatedClassName.replace("/", ".") + ".class";
            logger.debug("Writing Invocation context of method " + this.methodAnnotationMetadata.getMethodName() + " to "
                    + fName);
            try {
                FileOutputStream fos = new FileOutputStream(fName);
                fos.write(getCW().toByteArray());
                fos.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return the bytecode of the generated class.
     */
    public byte[] getBytes() {
        return getCW().toByteArray();
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
     * Create the constructor which should look like :
     * <ul>
     * <li> First arg = bean instance</li>
     * <li> Last args are arguments of the method (if any)</li>
     * </ul>
     * <br>
     *
     * <pre>
     *  public CtxImpl(Bean bean, int i, Long k, ...) {
     *      this.bean = bean;
     *      this.factory = bean.getEasyBeansFactory();
     *      this.interceptorManager = bean.getEasyBeansInterceptorManager();
     *      this.i = i;
     *      this.k = k;
     *      this... = ...
     *      this.interceptor0 = interceptorManager.getXXXInterceptor();
     *      this.interceptor1 = interceptorManager....();
     *  }
     * </pre>
     */
    private void addConstructor() {

        // First, get the desc of the intercepted method
        String argsMethodDesc = "";
        for (Type t : this.methodArgsType) {
            argsMethodDesc += t.getDescriptor();
        }


        // Add the bean class type before arguments
        // public CtxImpl(Bean bean, <args of the method>)
        // it is a void type for return type
        this.constructorDesc = "(" + this.beanClassDesc + argsMethodDesc + ")V";

        // Generate constructor
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "<init>", this.constructorDesc, null, null);
        mv.visitCode();

        // Call super constructor
        int arg = 1;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");

        // Now, set the attributes of the class
        // this.bean = bean
        int argBean = arg++;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, argBean);
        mv
                .visitFieldInsn(PUTFIELD, this.generatedClassName, "bean", encodeClassDesc(this.classAnnotationMetadata
                        .getClassName()));


        // this.factory = bean.getEasyBeansFactory();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, argBean);
        mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansFactory",
                "()Lorg/ow2/easybeans/api/Factory;");
        mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "factory", "Lorg/ow2/easybeans/api/Factory;");


        // this.interceptorManager = bean.getEasyBeansInterceptorManager();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, argBean);
        mv.visitMethodInsn(INVOKEVIRTUAL, this.classAnnotationMetadata.getClassName(), "getEasyBeansInterceptorManager",
                "()" + encodeClassDesc(this.interceptorManagerClassName));
        mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "interceptorManager",
                encodeClassDesc(this.interceptorManagerClassName));

        // And now, the attributes corresponding to the arguments of the method
        // it will do : this.ARG0 = xxx;
        int methodArg = 0;
        for (Type type : this.methodArgsType) {
            mv.visitVarInsn(ALOAD, 0);
            int opCode = putFieldLoadOpCode(type.getSort());
            mv.visitVarInsn(opCode, arg++);
            mv.visitFieldInsn(PUTFIELD, this.generatedClassName, ARG + (methodArg++), type.getDescriptor());
            // Double and Long are special parameters
            if (opCode == LLOAD || opCode == DLOAD) {
                arg++;
            }
        }


        // this.interceptorXX = interceptorManager.getXXXInterceptor();
        int index = 0;
        for (IJClassInterceptor interceptor : this.allInterceptors) {
            // Only if interceptor is not in the bean class
            if (!interceptor.getClassName().equals(this.beanClassName)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, "interceptorManager",
                        encodeClassDesc(this.interceptorManagerClassName));
                String getterName = "get" + interceptor.getClassName().replace("/", "");
                mv.visitMethodInsn(INVOKEVIRTUAL, this.interceptorManagerClassName, getterName, "()"
                        + encodeClassDesc(interceptor.getClassName()));
                mv.visitFieldInsn(PUTFIELD, this.generatedClassName, INTERCEPTOR + (index++), encodeClassDesc(interceptor
                        .getClassName()));
            }
        }



        // need to add return instruction
        mv.visitInsn(RETURN);

        // visit max compute automatically
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
     * Add attributes of the class in two steps.
     * <ul>
     * <li>InvocationContext interface</li>
     * <li>EasyBeansInvocationContext interface</li>
     * </ul>
     */
    private void addAttributes() {
        addInvocationContextAttributes();
        addEasyBeansInvocationContextAttributes();

    }

    /**
     * Add methods of the class in two steps.
     * <ul>
     * <li>InvocationContext interface</li>
     * <li>EasyBeansInvocationContext interface</li>
     * <li>toString() method</li>
     * </ul>
     */
    private void addMethods() {
        addInvocationContextMethods();
        addEasyBeansInvocationContextMethods();
        addToString();

    }

    /**
     * Add methods for InvocationContext interface.
     */
    private void addInvocationContextMethods() {
        addInvocationContextGetParameters();
        addInvocationContextSetParameters();
        addInvocationContextGetMethod();
        addInvocationContextGetTarget();
        addInvocationContextProceed();
        addInvocationContextGetContextData();
    }

    /**
     * Add methods for EasyBeansInvocationContext interface.
     */
    private void addEasyBeansInvocationContextMethods() {

        addEasyBeansInvocationContextGetFactory();

    }


    /**
     * Adds the getTarget method of InvocationContext interface.<br>
     * It adds :
     *
     * <pre>
     * public Object getTarget() {
     *   return bean;
     * }
     * </pre>
     */
    private void addInvocationContextGetTarget() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "getTarget", "()Ljava/lang/Object;", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, this.generatedClassName, "bean", this.beanClassDesc);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Adds the getFactory method of EasyBeansInvocationContext interface.<br>
     * It adds :
     *
     * <pre>
     * public Factory getFactory() {
     *     return this.factory;
     * }
     * </pre>
     */
    private void addEasyBeansInvocationContextGetFactory() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "getFactory", "()" + EASYBEANS_FACTORY, null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, this.generatedClassName, "factory", EASYBEANS_FACTORY);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }


    /**
     * Adds the getMethod() method of InvocationContext interface.<br>
     * It adds :
     *
     * <pre>
     * public Method getMethod() {
     *   if (method == null) {
     *     try {
     *       method = MyEjb.class.getMethod("methodName", new Class[] {xxx, yyy, ...});
     *     } catch (SecurityException e) {
     *       throw new RuntimeException("Cannot...", e);
     *     } catch (NoSuchMethodException e) {
     *       throw new RuntimeException("Cannot...", e);
     *     }
     *   }
     *   return method;
     * }
     * </pre>
     */
    private void addInvocationContextGetMethod() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "getMethod", "()" + JAVA_LANG_REFLECT_METHOD, null, null);
        mv.visitCode();

        // only for around invoke type, lifecycle interceptor should return null
        if (this.interceptorType == AROUND_INVOKE) {

            // if (method == null) {
            mv.visitFieldInsn(GETSTATIC, this.generatedClassName, "method", JAVA_LANG_REFLECT_METHOD);
            // go to this label if not null
            Label notNullParametersLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, notNullParametersLabel);


            // Start of the try block
            Label tryLabel = new Label();
            mv.visitLabel(tryLabel);

            // call a method on the bean class
            mv.visitLdcInsn(this.beanClassType);
            // name of the method which is searched
            mv.visitLdcInsn(this.jMethod.getName());


            // build an array of java.lang.Class with the size of args of the method
            mv.visitIntInsn(BIPUSH, this.methodArgsType.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Class");
            int argCount = 0;
            for (Type type : this.methodArgsType) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, argCount);
                visitClassType(type, mv);
                mv.visitInsn(AASTORE);
                argCount++;
            }

            // signature of the getMethod() method on java.lang.Class class
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
                    "getMethod", "(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;");

            // set the result : method = ...
            mv.visitFieldInsn(PUTSTATIC, this.generatedClassName, "method", "Ljava/lang/reflect/Method;");


            // go to the return label
            mv.visitJumpInsn(GOTO, notNullParametersLabel);

            // start of the catch label which throw a runtime exception
            // } catch (SecurityException e) {
            //   throw new RuntimeException("Cannot...", e);
            // }
            Label firstCatchLabel = new Label();
            mv.visitLabel(firstCatchLabel);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Cannot find method due to a security exception");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>",
                    "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);


            // } catch (NoSuchMethodException e) {
            //   throw new RuntimeException("Cannot...", e);
            // }
            Label secondCatchLabel = new Label();
            mv.visitLabel(secondCatchLabel);
            mv.visitVarInsn(ASTORE, 1);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Cannot find the method");
            mv.visitVarInsn(ALOAD, 1);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>",
                    "(Ljava/lang/String;Ljava/lang/Throwable;)V");
            mv.visitInsn(ATHROW);


            // if method is not null, return it
            mv.visitLabel(notNullParametersLabel);
            mv.visitFieldInsn(GETSTATIC, this.generatedClassName, "method", JAVA_LANG_REFLECT_METHOD);
            mv.visitInsn(ARETURN);

            // add try/cacth
            mv.visitTryCatchBlock(tryLabel, firstCatchLabel, firstCatchLabel, "java/lang/SecurityException");
            mv.visitTryCatchBlock(tryLabel, firstCatchLabel, secondCatchLabel, "java/lang/NoSuchMethodException");
        } else {
            // for lifecycle method
            mv.visitInsn(ACONST_NULL);
            mv.visitInsn(ARETURN);
        }


        // finish
        mv.visitMaxs(0, 0);
        mv.visitEnd();


    }
    /**
     * Adds attributes of InvocationContext interface.
     *
     * <pre>
     *      private StatelessBean bean;
     *      private Object[] parameters;
     *      private static Method method;
     *      private int interceptor;
     *      private Map contextData;
     *
     *      // args of the method
     *      private TYPE_ARG_METHOD arg0 = xxx;
     *      private TYPE_ARG_METHOD arg1 = xxx;
     *      private TYPE_ARG_METHOD arg2 = xxx;
     *      private TYPE_ARG_METHOD.......;
     * </pre>
     */
    private void addInvocationContextAttributes() {

        // Add bean attribute
        // private StatelessBean bean;
        addAttribute(ACC_PRIVATE, "bean", this.beanClassDesc);

        // Add parameters attribute
        // private Object[] parameters;
        addAttribute(ACC_PRIVATE, "parameters", ARRAY_OBJECTS);

        // Add java.lang.reflect.Method attribute
        // private static Method method;
        addAttribute(ACC_PRIVATE + ACC_STATIC, "method", JAVA_LANG_REFLECT_METHOD);

        // Add the interceptor counter
        // private int interceptor;
        addAttribute(ACC_PRIVATE, "interceptor", "I", Integer.valueOf(0));

        // Now, add argument of the method as attributes
        int arg = 0;
        for (Type t : this.methodArgsType) {
            addAttribute(ACC_PRIVATE, ARG + (arg++), t.getDescriptor());
        }

        // Now, add interceptors objects
        int intercpt = 0;
        for (IJClassInterceptor interceptor : this.allInterceptors) {
            // Only if interceptor is not in the bean class
            if (!interceptor.getClassName().equals(this.beanClassName)) {
                addAttribute(ACC_PRIVATE , INTERCEPTOR + (intercpt++), encodeClassDesc(interceptor.getClassName()));
            }
        }

        // ContextData
        addAttribute(ACC_PRIVATE, "contextData", "Ljava/util/Map;");

    }

    /**
     * Adds the initialization of static attributes.
     * ie : private static Method method = null
     *      private static InterceptorClass interceptor0 = new MyInterceptor();
     *      private static InterceptorClass2 interceptor1 = ....
     */
    private void addStaticClassInitialization() {
        MethodVisitor mv = getCW().visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        // private static Method method = null
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTSTATIC, this.generatedClassName, "method", JAVA_LANG_REFLECT_METHOD);


        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }



    /**
     * Adds attributes of EasyBeansInvocationContext interface.
     *
     * <pre>
     * private Factory factory;
     * </pre>
     */
    private void addEasyBeansInvocationContextAttributes() {

        // Add factory attribute
        // private Factory factory;
        addAttribute(ACC_PRIVATE, "factory", EASYBEANS_FACTORY);


        // Add interceptor manager attribute
        // private interceptorManagerClassName interceptorManager;
        addAttribute(ACC_PRIVATE, "interceptorManager", encodeClassDesc(this.interceptorManagerClassName));

    }

    /**
     * Adds the proceed method.<br>
     * It adds :
     *
     * <pre>
     *  public Object proceed() throws Exception {
     *    interceptor++;
     *    switch (interceptor) {
     *      case 1 :
     *        return myInterceptor.intercept(this);
     *      case 2 :
     *        return otherInterceptor.intercept(this);
     *      case 3 :
     *           return bean.originalmethod(...);
     *       default:
     *           throw new IllegalStateException("Problem in interceptors");
     *    }
     *  }
     * </pre>
     */
    private void addInvocationContextProceed() {
        MethodVisitor mv = getCW()
                .visitMethod(ACC_PUBLIC, "proceed", "()" + JAVA_LANG_OBJECT, null, PROCEED_EXCEPTIONS);
        mv.visitCode();


        // interceptor++ or in fact : interceptor = interceptor + 1;
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, this.generatedClassName, "interceptor", "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD); // + 1
        mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "interceptor", "I");



        // load interceptor constant to do the switch
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, this.generatedClassName, "interceptor", "I");


        // Size
        int sizeInterceptors = this.allInterceptors.size();

        // need to add call to the original method
        int switchSize = sizeInterceptors + 1;

        // Build array of labels corresponding to swtich entries
        Label[] switchLabels = new Label[switchSize];
        for (int s = 0; s < switchSize; s++) {
            switchLabels[s] = new Label();
        }

        // default label
        Label defaultCaseLabel = new Label();

        // switch
        mv.visitTableSwitchInsn(1, switchSize, defaultCaseLabel, switchLabels);

        // add each interceptor switch entry with a return block at the end
        // ie : case 1 :
        //        return myInterceptor.intercept(this); // interceptor class
        // or case 1 :
        //        return bean.intercept(this) // bean class
        int index = 0;
        int interceptorIndex = 0;
        for (IJClassInterceptor interceptor : this.allInterceptors) {
            mv.visitLabel(switchLabels[index]);

            Type returnType = Type.getReturnType(interceptor.getJMethod().getDescriptor());

            // interceptor on the bean
            if (interceptor.getClassName().equals(this.beanClassName)) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, "bean", this.beanClassDesc);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, this.beanClassName,
                        interceptor.getJMethod().getName(), interceptor.getJMethod().getDescriptor());

                // return object or null if the return type is void
                returnsObject(returnType, mv);
            } else { // interceptor in another class
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, INTERCEPTOR + interceptorIndex ,
                        encodeClassDesc(interceptor.getClassName()));
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, interceptor.getClassName(),
                interceptor.getJMethod().getName(), interceptor.getJMethod().getDescriptor());
                // return object or null if the return type is void
                returnsObject(returnType, mv);
                interceptorIndex++;
            }
            index++;
        }

        // then, add call to original method, ie bean.businessMethod(i,j,...);
        mv.visitLabel(switchLabels[index++]);
        // get bean object
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, this.generatedClassName, "bean", this.beanClassDesc);

        // arguments of the method
        int indexArg = 0;
        for (Type argType : this.methodArgsType) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, ARG + (indexArg++), argType.getDescriptor());
        }

        // Call to the renamed method only for AroundInvoke
        // LifeCycle interceptors call the original method
        String interceptedMethod = null;
        if (this.interceptorType.equals(AROUND_INVOKE)) {
            interceptedMethod = MethodRenamer.encode(this.jMethod.getName());
        } else {
            interceptedMethod = this.jMethod.getName();
        }

        mv.visitMethodInsn(INVOKEVIRTUAL, this.beanClassName, interceptedMethod, this.jMethod.getDescriptor());
        Type returnType = Type.getReturnType(this.jMethod.getDescriptor());
        // return object or null if the return type is void
        returnsObject(returnType, mv);


        // default case
        mv.visitLabel(defaultCaseLabel);
        mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("Problem in interceptors. Shouldn't go in the default case.");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);

        // end
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }

    /**
     * Adds the getContextData() method.
     * <pre>
     * public Map getContextData() {
     *    if (contextData == null) {
     *       contextData = new HashMap();
     *    }
     *    return contextData;
     * }
     * </pre>
     *
     */
    public void addInvocationContextGetContextData() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "getContextData", "()Ljava/util/Map;", null, null);
            mv.visitCode();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, "contextData", "Ljava/util/Map;");

            Label elseLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, elseLabel);

            // if
            mv.visitVarInsn(ALOAD, 0);
            mv.visitTypeInsn(NEW, "java/util/HashMap");
            mv.visitInsn(DUP);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/HashMap", "<init>", "()V");
            mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "contextData", "Ljava/util/Map;");

            // else
            mv.visitLabel(elseLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, "contextData", "Ljava/util/Map;");

            // return
            mv.visitInsn(ARETURN);

            mv.visitMaxs(0, 0);
            mv.visitEnd();


    }

    /**
     * Adds the getParameters method of InvocationContext interface.<br>
     * It adds :
     *
     * <pre>
     * public Object[] getParameters() {
     *     if (parameters == null) {
     *         parameters = new Object[] {arg0, arg1, argxxx};
     *     }
     *     return parameters;
     * }
     * </pre>
     */
    private void addInvocationContextGetParameters() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "getParameters", "()" + ARRAY_OBJECTS, null, null);
        mv.visitCode();

        // only for around invoke type
        if (this.interceptorType == AROUND_INVOKE) {

            // if (parameters == null) {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, "parameters", ARRAY_OBJECTS);
            // go to this label if not null
            Label notNullParametersLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, notNullParametersLabel);

            // parameters = new Object[] {arg0, arg1, arg...};
            // put size of the array
            mv.visitVarInsn(ALOAD, 0);
            mv.visitIntInsn(BIPUSH, this.methodArgsType.length);
            mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            // for each argument of the methods :
            int argCount = 0;
            for (Type type : this.methodArgsType) {
                mv.visitInsn(DUP);
                mv.visitIntInsn(BIPUSH, argCount);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitFieldInsn(GETFIELD, this.generatedClassName, ARG + argCount, type.getDescriptor());
                // if type is not object type, need to convert it
                // for example : Integer.valueOf(i);
                transformPrimitiveIntoObject(type, mv);
                mv.visitInsn(AASTORE);
                argCount++;
            }

            // store field
            mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "parameters", ARRAY_OBJECTS);

            // not null label :
            // return parameters;
            mv.visitLabel(notNullParametersLabel);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, "parameters", ARRAY_OBJECTS);
        } else {
            // throw Exception
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Operation getParameters can only be applied on AroundInvoke interceptors");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
        }

        // return
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();


    }

    /**
     * Adds the setParameters method of InvocationContext interface.<br>
     * It adds :
     *
     * <pre>
     * public void setParameters(Object aobj[]) {
     *   if (aobj == null) {
     *     throw new IllegalStateException("Cannot set a null array.");
     *   }
     *   if (aobj.length != ...) {
     *     throw new IllegalStateException("Invalid size of the given array. The length should be '" + ... + "'.");
     *   }
     *   parameters = aobj;
     *
     *   arg0 = (Integer) aobj[0];
     *   arg1 = ((Integer) aobj[1]).intValue();
     *   arg2 = ((Double) aobj[2]).doubleValue();
     *   arg3 = ((Float) aobj[3]).floatValue();
     *   arg4 = (String) aobj[4];
     *   ...
     * }
     *
     * </pre>
     */
    private void addInvocationContextSetParameters() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "setParameters", "(" + ARRAY_OBJECTS + ")V", null, null);
        mv.visitCode();

        // only for aroundInvoke
        if (this.interceptorType == AROUND_INVOKE) {
            /**
             * if (aobj == null) { throw new IllegalStateException("Cannot set a
             * null array."); }
             */
            mv.visitVarInsn(ALOAD, 1);
            Label notNull = new Label();
            mv.visitJumpInsn(IFNONNULL, notNull);
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Cannot set a null array.");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(notNull);

            /**
             * if (aobj.length != ...) { throw new
             * IllegalStateException("Invalid size of the given array. The
             * length should be '" + ... + "'."); }
             */
            mv.visitVarInsn(ALOAD, 1);
            mv.visitInsn(ARRAYLENGTH);
            mv.visitIntInsn(BIPUSH, this.methodArgsType.length);
            Label sizeOk = new Label();
            mv.visitJumpInsn(IF_ICMPEQ, sizeOk);
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Invalid size of the given array. The length should be '" + this.methodArgsType.length + "'.");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
            mv.visitLabel(sizeOk);

            // this.parameters = parameters
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 1);
            mv.visitFieldInsn(PUTFIELD, this.generatedClassName, "parameters", ARRAY_OBJECTS);

            /**
             * arg0 = (Integer) aobj[0]; arg1 = ((Integer) aobj[1]).intValue();
             * arg2 = ((Double) aobj[2]).doubleValue(); arg3 = ((Float)
             * aobj[3]).floatValue(); arg4 = (String) aobj[4]; ...
             */
            int argCount = 0;
            for (Type type : this.methodArgsType) {
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitIntInsn(BIPUSH, argCount);
                mv.visitInsn(AALOAD);
                // Cast object Integer.valueOf(i);
                transformObjectIntoPrimitive(type, mv);
                // write result
                mv.visitFieldInsn(PUTFIELD, this.generatedClassName, ARG + argCount, type.getDescriptor());
                argCount++;
            }
        } else {
            // throw Exception
            mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Operation setParameters can only be applied on AroundInvoke interceptors");
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
            mv.visitInsn(ATHROW);
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Generated toString() method.
     * Generated code is in the comments of the method body.
     */
    private void addToString() {
        MethodVisitor mv = getCW().visitMethod(ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        mv.visitCode();

        // local vars
        // 1 = sb
        // 2 = classNames
        // 3 = className
        // 4 = indent2
        // 5 = indent4
        // 6 = i
        //
        int localVar = 1;
        final int varSB = localVar++;
        int varCLASSNAMES = localVar++;
        int varCLASSNAME = localVar++;
        int varINDENT2 = localVar++;
        int varINDENT4 = localVar++;
        int varI = localVar++;

        /*
         * StringBuilder sb = new StringBuilder();
         * String[] classNames = this.getClass().getName().split("\\.");
         * String className = classNames[classNames.length - 1];
         * // classname
         * sb.append(className);
         * sb.append("[\n");
         * String indent2 = "  ";
         * String indent4 = "    ";
         * sb.append(indent2);
         * sb.append("List of interceptors :\n");
         */

        mv.visitTypeInsn(NEW, "java/lang/StringBuilder");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/StringBuilder", "<init>", "()V");
        mv.visitVarInsn(ASTORE, varSB);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class", "getName", "()Ljava/lang/String;");
        mv.visitLdcInsn("\\.");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "split", "(Ljava/lang/String;)[Ljava/lang/String;");
        mv.visitVarInsn(ASTORE, varCLASSNAMES);
        mv.visitVarInsn(ALOAD, varCLASSNAMES);
        mv.visitVarInsn(ALOAD, varCLASSNAMES);
        mv.visitInsn(ARRAYLENGTH);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISUB);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, varCLASSNAME);
        mv.visitVarInsn(ALOAD, varSB);
        mv.visitVarInsn(ALOAD, varCLASSNAME);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, varSB);
        mv.visitLdcInsn("[\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);
        mv.visitLdcInsn("  ");
        mv.visitVarInsn(ASTORE, varINDENT2);
        mv.visitLdcInsn("    ");
        mv.visitVarInsn(ASTORE, varINDENT4);
        mv.visitVarInsn(ALOAD, varSB);
        mv.visitVarInsn(ALOAD, varINDENT2);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, varSB);
        mv.visitLdcInsn("List of interceptors :\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);

        /*
         * In the loop, print :
         * sb.append(indent4);
         * sb.append(i);
         * sb.append(") - ");
         * sb.append(interceptor.getClassName());
         * sb.append("[");
         * sb.append(interceptor.getJMethod().getName());
         * sb.append("]\n");
         */
        int i = 1;

        // int i = 1;
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ISTORE, varI);

        if (this.allInterceptors != null) {
            for (IJClassInterceptor interceptor : this.allInterceptors) {
                mv.visitVarInsn(ALOAD, varSB);
                mv.visitVarInsn(ALOAD, varINDENT4);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, varSB);
                mv.visitVarInsn(ILOAD, varI);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, varSB);
                mv.visitLdcInsn(") - ");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                // sb.append(interceptor.getClassName());
                mv.visitVarInsn(ALOAD, varSB);
                mv.visitLdcInsn(interceptor.getClassName());
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, varSB);
                mv.visitLdcInsn("[");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                // sb.append(interceptor.getJMethod().getName());
                mv.visitVarInsn(ALOAD, varSB);
                mv.visitLdcInsn(interceptor.getJMethod().getName());
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                mv.visitVarInsn(ALOAD, varSB);
                mv.visitLdcInsn("]\n");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                        "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
                mv.visitInsn(POP);

                i++;
                // i++
                mv.visitIincInsn(varI, 1);
            }
            /*
             * sb.append(indent2);
             * sb.append("Current interceptor : ");
             * sb.append(interceptor); sb.append("/");
             * sb.append(allInterceptors.size());
             */
            mv.visitVarInsn(ALOAD, varSB);
            mv.visitVarInsn(ALOAD, varINDENT2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, varSB);
            mv.visitLdcInsn("Current interceptor : ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, varSB);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, this.generatedClassName, "interceptor", "I");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, varSB);
            mv.visitLdcInsn("/");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, varSB);
            mv.visitLdcInsn(String.valueOf(this.allInterceptors.size()));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

        } else {
            /*
             * sb.append(indent2);
             * sb.append("No interceptors : ");
             */
            mv.visitVarInsn(ALOAD, varSB);
            mv.visitVarInsn(ALOAD, varINDENT2);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);

            mv.visitVarInsn(ALOAD, varSB);
            mv.visitLdcInsn("No interceptors : ");
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                    "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
            mv.visitInsn(POP);
        }

        /*
         * sb.append("\n");
         * sb.append("]");
         * return sb.toString();
         */
        mv.visitVarInsn(ALOAD, varSB);
        mv.visitLdcInsn("\n");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, varSB);
        mv.visitLdcInsn("]");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "append",
                "(Ljava/lang/String;)Ljava/lang/StringBuilder;");
        mv.visitInsn(POP);

        mv.visitVarInsn(ALOAD, varSB);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();

    }

    /**
     * @return method metadata used by this generator
     */
    public EasyBeansEjbJarMethodMetadata getMethodAnnotationMetadata() {
        return this.methodAnnotationMetadata;
    }

    /**
     * @return the name of the generated class name (with package name)
     */
    public String getGeneratedClassName() {
        return this.generatedClassName;
    }

    /**
     * @return the ASM descriptor of the generated constructor.
     */
    public String getConstructorDesc() {
        return this.constructorDesc;
    }

    /**
     * @return the interceptors used by this InvocationContext implementation object.
     */
    public List<IJClassInterceptor> getAllInterceptors() {
        return this.allInterceptors;
    }

}
