package org.ow2.easybeans.tests.enhancer.annotations;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation with primitive/enum/nested/array values.
 * @author Florent Benoit
 */
@Target({TYPE, METHOD, FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ComplexAnnotation {

    /**
     * Simple Name of this annotation.
     */
    String simpleName() default "";

    /**
     * Class example.
     */
    Class<?> itf() default Object.class;

    /**
     * Classes examples.
     */
    Class<?>[] classes() default {};

    /**
     * Properties.
     */
    AnnotationProperty[] properties() default {};

    /**
     * Enum type.
     */
    MyEnumType value() default MyEnumType.FIRST_VALUE;

    /**
     * Dummy annotation.
     */
    DummyAnnotation dummyAnnotation();

}
