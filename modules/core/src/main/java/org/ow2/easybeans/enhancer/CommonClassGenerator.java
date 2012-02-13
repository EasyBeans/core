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
 * $Id: CommonClassGenerator.java 5505 2010-05-26 14:01:09Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.enhancer;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.asm.AnnotationVisitor;
import org.ow2.easybeans.asm.ClassVisitor;
import org.ow2.easybeans.asm.ClassWriter;
import org.ow2.easybeans.asm.FieldVisitor;
import org.ow2.easybeans.asm.MethodVisitor;
import org.ow2.easybeans.asm.Opcodes;
import org.ow2.easybeans.asm.Type;

/**
 * Class with useful routines for writing a class.
 * @author Florent Benoit
 */
public abstract class CommonClassGenerator implements Opcodes {

    /**
     * Define an array of objects.
     */
    public static final String ARRAY_OBJECTS = "[Ljava/lang/Object;";

    /**
     * Defines java.lang.Object class.
     */
    public static final String JAVA_LANG_OBJECT = "Ljava/lang/Object;";

    /**
     * Defines a void method with JAVA_LANG_OBJECT as parameter.
     */
    public static final String VOID_METHOD_JAVA_LANG_OBJECT = "(Ljava/lang/Object;)V";

    /**
     * Defines java.lang.Exception class.
     */
    public static final String JAVA_LANG_EXCEPTION = "Ljava/lang/Exception;";

    /**
     * Define java.lang.reflect.Method.
     */
    public static final String JAVA_LANG_REFLECT_METHOD = "Ljava/lang/reflect/Method;";

    /**
     * Factory class (used to make the bean factory available).
     */
    public static final String EASYBEANS_FACTORY = Type.getDescriptor(Factory.class);

    /**
     * Version used for generated class.
     */
    public static final int GENERATED_CLASS_VERSION = V1_5;

    /**
     * The {@link org.ow2.easybeans.asm.ClassWriter} to which this adapter delegates
     * calls.
     */
    private ClassWriter cw;

    /**
     * Field visitor.
     */
    private FieldVisitor fv = null;

    /**
     * Creates a default class with useful routines for writing a class.
     * @param cw the class writer which will generate the class
     */
    public CommonClassGenerator(final ClassWriter cw) {
        this.cw = cw;
    }

    /**
     * Adds an attribute in the current classwriter.
     * @param access the field's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the field is synthetic and/or
     *        deprecated.
     * @param name the field's name.
     * @param desc the field's descriptor (see {@link Type Type}).
     */
    protected void addAttribute(final int access, final String name, final String desc) {
        addAttribute(access, name, desc, null);
    }

    /**
     * Adds an attribute in the current classwriter.
     * @param access the field's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the field is synthetic and/or
     *        deprecated.
     * @param name the field's name.
     * @param desc the field's descriptor (see {@link Type Type}).
     * @param value the field's initial value. This parameter, which may be
     *        <tt>null</tt> if the field does not have an initial value, must
     *        be an {@link Integer}, a {@link Float}, a {@link Long}, a
     *        {@link Double} or a {@link String} (for <tt>int</tt>,
     *        <tt>float</tt>, <tt>long</tt> or <tt>String</tt> fields
     *        respectively). <i>This parameter is only used for static fields</i>.
     *        Its value is ignored for non static fields, which must be
     *        initialized through bytecode instructions in constructors or
     *        methods.
     */
    protected void addAttribute(final int access, final String name, final String desc, final Object value) {
        this.fv = this.cw.visitField(access, name, desc, null, value);
        this.fv.visitEnd();
    }

    /**
     * Encodes an internal classname into a descriptor.
     * @param className internal class name
     * @return desc for the given className
     */
    public static String encodeClassDesc(final String className) {
        return "L" + className + ";";
    }

    /**
     * Encodes an internal classname into an array descriptor.
     * @param className internal class name
     * @return desc for the given className (array mode)
     */
    public static String encodeArrayClassDesc(final String className) {
        return "[L" + className + ";";
    }

    /**
     * @return the class writer used by this generator
     */
    public ClassWriter getCW() {
        return this.cw;
    }

    /**
     * Sends the OpCode used in an constructor to set the field.
     * @param sortCode type of attribute to set
     * @return op code
     */
    public static int putFieldLoadOpCode(final int sortCode) {
        switch (sortCode) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                return ILOAD;
            case Type.FLOAT:
                return FLOAD;
            case Type.LONG:
                return LLOAD;
            case Type.DOUBLE:
                return DLOAD;
            // case ARRAY:
            // case OBJECT:
            default:
                return ALOAD;
        }
    }




    public void putConstNumber(final int val, final MethodVisitor mv) {
        switch (val) {
            case 0:
                mv.visitInsn(ICONST_0);
                break;
            case 1:
                mv.visitInsn(ICONST_1);
                break;
            case 2:
                mv.visitInsn(ICONST_2);
                break;
            case 3:
                mv.visitInsn(ICONST_3);
                break;
            case 4:
                mv.visitInsn(ICONST_4);
                break;
            case 5:
                mv.visitInsn(ICONST_5);
                break;
            default:
                mv.visitIntInsn(BIPUSH, val);
                break;
        }
    }



    /**
     * If a type is one of the primitive object : boolean, int, long, etc, adds an instruction to transform type into an object.
     * ie : from int to Integer by calling Integer.valueOf(i);
     * @param type the object that need to be processed
     * @param mv the method visitor on which there is a need to adds an instruction
     */
    public static void transformPrimitiveIntoObject(final Type type, final MethodVisitor mv) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;");
                break;
            case Type.BYTE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;");
                break;
            case Type.CHAR:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;");
                break;
            case Type.SHORT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;");
                break;
            case Type.INT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;");
                break;
            case Type.FLOAT:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;");
                break;
            case Type.LONG:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;");
                break;
            case Type.DOUBLE:
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;");
                break;
            // case ARRAY:
            // case OBJECT:
            default:
                // nothing as this is already objects !
                break;
        }
    }

    /**
     * If a type is one of the object type with something which could be linked to a primitive type, cast object into primitive.
     * ie : from Integer to int by calling ((Integer) object).intValue();
     * @param type the object that need to be processed
     * @param mv the method visitor on which there is a need to adds an instruction
     */
    public static void transformObjectIntoPrimitive(final Type type, final MethodVisitor mv) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                break;
            case Type.BYTE:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B");
                break;
            case Type.CHAR:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C");
                break;
            case Type.SHORT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S");
                break;
            case Type.INT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I");
                break;
            case Type.FLOAT:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F");
                break;
            case Type.LONG:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J");
                break;
            case Type.DOUBLE:
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D");
                break;
            case Type.VOID:
                mv.visitInsn(POP);
                break;
             case Type.ARRAY:
                 mv.visitTypeInsn(CHECKCAST, type.getDescriptor());
                 break;
            case Type.OBJECT:
                mv.visitTypeInsn(CHECKCAST, type.getInternalName());
                break;
            default:
                // nothing as this is already objects !
                break;
        }
    }


    /**
     * Adds a return entry depending of the type value.
     * ie, for int : mv.visitInsn(IRETURN);
     * for void : mv.visitInsn(RETURN);
     * @param type the object that need to be processed
     * @param mv the method visitor on which there is a need to adds an instruction
     */
    public static void addReturnType(final Type type, final MethodVisitor mv) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.BYTE:
            case Type.CHAR:
            case Type.SHORT:
            case Type.INT:
                mv.visitInsn(IRETURN);
                break;
            case Type.FLOAT:
                mv.visitInsn(FRETURN);
                break;
            case Type.LONG:
                mv.visitInsn(LRETURN);
                break;
            case Type.DOUBLE:
                mv.visitInsn(DRETURN);
                break;
            case Type.VOID:
                mv.visitInsn(RETURN);
                break;
             case Type.ARRAY:
             case Type.OBJECT:
                 mv.visitInsn(ARETURN);
                 break;
            default:
                // nothing as this is already objects !
                break;
        }
    }





    /**
     * Allow to access to a class.<br>
     * ie, for int.class it will access to Integer.TYPE.<br>
     * For objects, it only calls the Object.
     * @param type the type of the object from which we want the class
     * @param mv object on which we add the instruction
     */
    public static void visitClassType(final Type type, final MethodVisitor mv) {
        switch (type.getSort()) {
            case Type.BOOLEAN:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Boolean", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.BYTE:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Byte", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.CHAR:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Character", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.SHORT:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Short", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.INT:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Integer", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.FLOAT:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Float", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.LONG:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Long", "TYPE", "Ljava/lang/Class;");
                break;
            case Type.DOUBLE:
                mv.visitFieldInsn(GETSTATIC, "java/lang/Double", "TYPE", "Ljava/lang/Class;");
                break;
            // case ARRAY:
            // case OBJECT:
            default:
                mv.visitLdcInsn(type);
                break;
        }
    }

    /**
     * Returns the object by casting it or return null if method is of void type.
     * @param returnType the kind of return for this method.
     * @param mv object on which we add the instruction.
     */
    public static void returnsObject(final Type returnType, final MethodVisitor mv) {
        if (returnType.equals(Type.VOID_TYPE)) {
            mv.visitInsn(ACONST_NULL);
        } else {
            transformPrimitiveIntoObject(returnType, mv);
        }
        mv.visitInsn(ARETURN);
    }

    /**
     * Adds a field with its getters/setters.
     * @param cv the classvisitor to add field/methods
     * @param beanClassName the name of the bean's class
     * @param fieldName the name of the attribute
     * @param clazz the class of the attribute.
     */
    public static void addFieldGettersSetters(final ClassVisitor cv, final String beanClassName,
            final String fieldName, final Class<?> clazz) {
        String className = Type.getDescriptor(clazz);
        addFieldGettersSetters(cv, beanClassName, fieldName, className);
    }

    /**
     * Adds a field with its getters/setters.
     * @param cv the classvisitor to add field/methods
     * @param beanClassName the name of the bean's class
     * @param fieldName the name of the attribute
     * @param className the className of the attribute.
     */
    public static void addFieldGettersSetters(final ClassVisitor cv, final String beanClassName,
            final String fieldName, final String className) {


        // Get type of the class
        Type type = Type.getType(className);

        // Add the fieldName attribute
        // private CLASSNAME fieldName = null;
        FieldVisitor fv = cv.visitField(ACC_PRIVATE, fieldName, className, null, null);
        fv.visitEnd();

        // build getterName
        String appendName = fieldName.toUpperCase().charAt(0) + fieldName.substring(1);
        String getterName = "get" + appendName;

        // Add its getter :
        // public CLASSNAME getterName() {
        //    return this.fieldName;
        // }
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, getterName, "()" + className, null, null);
        // Add some flags on the generated method
        addAnnotationsOnGeneratedMethod(mv);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, beanClassName, fieldName, className);
        // return type is depending of the type
        addReturnType(type, mv);
        mv.visitMaxs(0, 0);
        mv.visitEnd();

        // Add the setter
        // public void setterName(final CLASSNAME setterName) {
        //    this.fieldName = fieldName;
        // }
        String setterName = "set" + appendName;
        mv = cv.visitMethod(ACC_PUBLIC, setterName, "(" + className + ")V", null, null);
        // Add some flags on the generated method
        addAnnotationsOnGeneratedMethod(mv);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        // Depends of the type
        int opCode = putFieldLoadOpCode(type.getSort());
        mv.visitVarInsn(opCode, 1);
        mv.visitFieldInsn(PUTFIELD, beanClassName, fieldName, className);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }


    /**
     * Sets the given field to null.
     * @param mv the method visitor to add field/methods
     * @param beanClassName the name of the bean's class
     * @param fieldName the name of the attribute
     * @param clazz the class of the attribute.
     */
    public static void nullifyField(final MethodVisitor mv, final String beanClassName,
            final String fieldName, final Class<?> clazz) {
        String className = Type.getDescriptor(clazz);
        nullifyField(mv, beanClassName, fieldName, className);
    }

    /**
     * Sets the given field to null.
     * @param mv the method visitor to add field/methods
     * @param beanClassName the name of the bean's class
     * @param fieldName the name of the attribute
     * @param className the className of the attribute.
     */
    public static void nullifyField(final MethodVisitor mv, final String beanClassName,
            final String fieldName, final String className) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ACONST_NULL);
        mv.visitFieldInsn(PUTFIELD, beanClassName, fieldName, className);
    }


    /**
     * Adds a getter for a given fieldName. If the fieldName is null, it's return null.
     * @param cv the classvisitor to add the method
     * @param getterName the name of the method
     * @param clazz the class of the returned object.
     */
    public static void addNullGetter(final ClassVisitor cv, final String getterName, final Class<?> clazz) {
        String returnedClassName = Type.getDescriptor(clazz);
        // Add its getter :
        // public returnedClassName getterName() {
        //    return null;
        // }
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, getterName, "()" + returnedClassName, null, null);
        // Add some flags on the generated method
        addAnnotationsOnGeneratedMethod(mv);

        mv.visitCode();
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);

        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }



    /**
     * All EasyBeans generated methods should add some annotations.
     * @param methodVisitor the given method visitor
     */
    public static void addAnnotationsOnGeneratedMethod(final MethodVisitor methodVisitor) {
        // This method shouldn't be included as a JAX-WS WebMethod.
        AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation("Ljavax/jws/WebMethod;", true);
        annotationVisitor.visit("exclude", Boolean.TRUE);
        annotationVisitor.visitEnd();
    }


}
