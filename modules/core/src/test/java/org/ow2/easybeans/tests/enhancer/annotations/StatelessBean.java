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
 * $Id: StatelessBean.java 5930 2011-07-26 16:19:54Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.enhancer.annotations;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ejb.Local;
import javax.ejb.Stateless;
import javax.interceptor.AroundInvoke;
import javax.interceptor.InvocationContext;
import javax.jws.WebParam;

import junit.framework.Assert;

/**
 * Stateless Bean with multiple user-annotations.
 * @author Florent Benoit
 */
@Stateless(name = "AnnotationBean")
@Local(BusinessInterface.class)
public class StatelessBean {

    /**
     * Method with annotation.
     * @return true if check of annotation has been OK
     */
    @ComplexAnnotation(simpleName = "simpleName", dummyAnnotation = @DummyAnnotation(dummyItf = List.class))
    public boolean testMethod() {
        return false;
    }

    /**
     * A method with complex annotation on the method and on the parameter.
     * @param a the first parameter
     * @param b the second parameter
     * @param c the third parameter
     * @return true if check of annotation has been OK
     */
    @Deprecated
    @ClassAnnotation
    @ComplexAnnotation(itf = Properties.class,
                       value = MyEnumType.SECOND_VALUE,
                       classes = {URL.class, URI.class},
                       properties = {@AnnotationProperty(propertyName = "prop1Name", propertyValue = "prop1Value"),
                                     @AnnotationProperty(propertyName = "prop2Name", propertyValue = "prop2Value")},
                       dummyAnnotation = @DummyAnnotation(dummyEnum = MyEnumType.FIRST_VALUE,
                           dummyName = "dummyNameOnMethod",
                           dummyArray = {@AnnotationProperty(propertyName = "dummyProp1Name", propertyValue = "dummyProp1Value"),
                                         @AnnotationProperty(propertyName = "dummyProp2Name", propertyValue = "dummyProp2Value")},
                           dummyItf = URL.class),
                      simpleName = "complexAnnotationOnMethod")
    public boolean complexAnnotationMethod(
            @WebParam(targetNamespace = "a1", partName = "b1")
            @DummyAnnotation(dummyName = "testParameter")
            final int a,
            @WebParam(targetNamespace = "a2", partName = "b2") final int b,
            @ComplexAnnotation(simpleName = "test",
                               itf = Properties.class,
                               value = MyEnumType.SECOND_VALUE,
                               properties = {@AnnotationProperty(propertyName = "propName", propertyValue = "propValue")},
                               dummyAnnotation = @DummyAnnotation(dummyEnum = MyEnumType.FIRST_VALUE,
                                       dummyName = "dummyName",
                                       dummyArray = {@AnnotationProperty(propertyName = "dummyPropName",
                                                                         propertyValue = "dummyPropValue")},
                                       dummyItf = URL.class)) final int c) {
        return false;
    }

    /**
     * Interception method in order to check that annotations have been moved.
     * @param invocationContext the context in order to get method/parameters
     * @return the value of the invocation
     * @throws Exception if there is a failure when checking methods
     */
    @AroundInvoke
    protected Object intercept(final InvocationContext invocationContext) throws Exception {

        // Get method
        Method method = invocationContext.getMethod();

        // Get annotations
        Annotation[] annotations = method.getAnnotations();


        // Check that annotations are still present with the right values
        if ("testMethod".equals(method.getName())) {
            // Annotation present ?
            if (annotations == null) {
                throw new IllegalStateException("Annotation is missing on the method " + method);
            }

            // only 1
            if (1 != annotations.length) {
                throw new IllegalStateException("Only one annotation should be present");
            }

            ComplexAnnotation complexAnnotation = method.getAnnotation(ComplexAnnotation.class);
            Assert.assertNotNull(complexAnnotation);

            // Verify parameters
            Assert.assertEquals("simpleName", complexAnnotation.simpleName());
            DummyAnnotation dummyAnnotation = complexAnnotation.dummyAnnotation();
            Assert.assertNotNull(dummyAnnotation);
            Assert.assertEquals(List.class, dummyAnnotation.dummyItf());


            // It's ok, then return true !
            return Boolean.TRUE;

        }


     // Check that annotations are still present with the right values
        if ("complexAnnotationMethod".equals(method.getName())) {
            // Annotation present ?
            if (annotations == null) {
                throw new IllegalStateException("Annotation is missing on the method " + method);
            }

            // only 2 (@ClassAnnotation being hidden at runtime)
            if (1 + 1  != annotations.length) {
                throw new IllegalStateException("Exactly two annotations should be present");
            }

            ComplexAnnotation complexAnnotation = method.getAnnotation(ComplexAnnotation.class);
            Assert.assertNotNull(complexAnnotation);

            // Verify parameters of annotation
            Assert.assertEquals("complexAnnotationOnMethod", complexAnnotation.simpleName());
            Assert.assertEquals(Properties.class, complexAnnotation.itf());
            Assert.assertEquals(MyEnumType.SECOND_VALUE, complexAnnotation.value());
            Assert.assertTrue(Arrays.equals(new Class[] {URL.class, URI.class}, complexAnnotation.classes()));
            AnnotationProperty[] annotationProperties = complexAnnotation.properties();
            Assert.assertNotNull(annotationProperties);
            Assert.assertEquals("prop1Name", annotationProperties[0].propertyName());
            Assert.assertEquals("prop1Value", annotationProperties[0].propertyValue());
            Assert.assertEquals("prop2Name", annotationProperties[1].propertyName());
            Assert.assertEquals("prop2Value", annotationProperties[1].propertyValue());
            DummyAnnotation dummyAnnotation = complexAnnotation.dummyAnnotation();
            Assert.assertNotNull(dummyAnnotation);
            Assert.assertEquals("dummyNameOnMethod", dummyAnnotation.dummyName());
            Assert.assertEquals(URL.class, dummyAnnotation.dummyItf());
            AnnotationProperty[] dummyArrayProperties = dummyAnnotation.dummyArray();
            Assert.assertNotNull(dummyArrayProperties);
            Assert.assertEquals("dummyProp1Name", dummyArrayProperties[0].propertyName());
            Assert.assertEquals("dummyProp1Value", dummyArrayProperties[0].propertyValue());
            Assert.assertEquals("dummyProp2Name", dummyArrayProperties[1].propertyName());
            Assert.assertEquals("dummyProp2Value", dummyArrayProperties[1].propertyValue());

            // check parameters of the method
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            Assert.assertNotNull(parameterAnnotations);
            Assert.assertEquals(1 + 1 + 1, parameterAnnotations.length);

            // check annotations on the first parameter
            Annotation[] parameter1Annotations = parameterAnnotations[0];
            Assert.assertNotNull(parameter1Annotations);
            Assert.assertEquals(2, parameter1Annotations.length);

            WebParam webParam =  (WebParam) parameter1Annotations[0];
            Assert.assertEquals("a1", webParam.targetNamespace());
            Assert.assertEquals("b1", webParam.partName());

            DummyAnnotation dummyAnnotation2 = (DummyAnnotation) parameter1Annotations[1];
            Assert.assertEquals("testParameter", dummyAnnotation2.dummyName());

            // check annotation on the 2nd parameter
            Annotation[] parameter2Annotations = parameterAnnotations[1];
            Assert.assertNotNull(parameter2Annotations);
            Assert.assertEquals(1, parameter2Annotations.length);

            WebParam webParam2 =  (WebParam) parameter2Annotations[0];
            Assert.assertEquals("a2", webParam2.targetNamespace());
            Assert.assertEquals("b2", webParam2.partName());

            // check annotation on the 3rd parameter
            Annotation[] parameter3Annotations = parameterAnnotations[2];
            Assert.assertNotNull(parameter3Annotations);
            Assert.assertEquals(1, parameter3Annotations.length);

            ComplexAnnotation complexAnnotation3 =  (ComplexAnnotation) parameter3Annotations[0];
            Assert.assertEquals("test", complexAnnotation3.simpleName());
            Assert.assertEquals(Properties.class, complexAnnotation3.itf());
            Assert.assertEquals(MyEnumType.SECOND_VALUE, complexAnnotation3.value());
            AnnotationProperty[] annotationProperties3 = complexAnnotation3.properties();
            Assert.assertNotNull(annotationProperties3);
            Assert.assertEquals("propName", annotationProperties3[0].propertyName());
            Assert.assertEquals("propValue", annotationProperties3[0].propertyValue());

            DummyAnnotation dummyAnnotation3 = complexAnnotation3.dummyAnnotation();
            Assert.assertEquals("dummyName", dummyAnnotation3.dummyName());
            Assert.assertEquals(URL.class, dummyAnnotation3.dummyItf());
            Assert.assertEquals(MyEnumType.FIRST_VALUE, dummyAnnotation3.dummyEnum());
            AnnotationProperty[] annotationProperties3b = dummyAnnotation3.dummyArray();
            Assert.assertNotNull(annotationProperties3b);
            Assert.assertEquals("dummyPropName", annotationProperties3b[0].propertyName());
            Assert.assertEquals("dummyPropValue", annotationProperties3b[0].propertyValue());

            // It's ok, then return true !
            return Boolean.TRUE;

        }

        // Call the method
        return invocationContext.proceed();

    }

}
