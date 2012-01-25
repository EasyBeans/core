/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: TimerBeanValidator.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.annotations.helper.bean.checks;

import static org.ow2.easybeans.asm.Opcodes.ACC_FINAL;
import static org.ow2.easybeans.asm.Opcodes.ACC_STATIC;
import static org.ow2.easybeans.deployment.annotations.helper.bean.checks.AccessChecker.ensureNoAccess;

import org.ow2.easybeans.deployment.annotations.exceptions.InterceptorsValidationException;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.enhancer.bean.BeanClassAdapter;

/**
 * This class ensures that the bean has no more than one timer method and that
 * the signature of this method is valid.
 * @author Florent Benoit
 */
public final class TimerBeanValidator {


    /**
     * Helper class, no public constructor.
     */
    private TimerBeanValidator() {
    }


    /**
     * Validate a bean (check timer objects).
     * @param bean bean to validate.
     */
    public static void validate(final EasyBeansEjbJarClassMetadata bean) {

        // Existing timer method
        EasyBeansEjbJarMethodMetadata alreadyFoundTimerMethod = null;

        // Get timer method if any and do a call on this timer method
        for (EasyBeansEjbJarMethodMetadata method : bean.getMethodMetadataCollection()) {

            // Check if the timeout method is valid
            if (method.isTimeout()) {

                // There is already a timer method that have been found.
                if (alreadyFoundTimerMethod != null) {
                    throw new InterceptorsValidationException(
                            "A bean cannot have more than one timer method, previous timer method is '" + alreadyFoundTimerMethod
                                    + "' while the new one is '" + method + "'.");
                }

                // flag this method as already found
                alreadyFoundTimerMethod = method;

                // Check that the signature is valid
                // void and Timer parameter
                if (!BeanClassAdapter.TIMER_JMETHOD.getDescriptor().equals(method.getJMethod().getDescriptor())) {
                    throw new InterceptorsValidationException("The timeout method '" + method
                            + "' hasn't a valid signature. The valid signature should be '"
                            + BeanClassAdapter.TIMER_JMETHOD.getDescriptor() + "'.");
                }

                // No static or final
                ensureNoAccess(ACC_FINAL, method.getJMethod(), "Final", bean.getClassName());
                ensureNoAccess(ACC_STATIC, method.getJMethod(), "Static", bean.getClassName());




            }
        }


    }

}
