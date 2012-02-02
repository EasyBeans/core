/**
 * EasyBeans
 * Copyright (C) 2007-2012 Bull S.A.S.
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
 * $Id: JNDIBindingFactory.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.proxy.binding;

import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;

import org.ow2.easybeans.api.binding.BindingException;
import org.ow2.easybeans.api.binding.EZBBindingFactory;
import org.ow2.easybeans.api.binding.EZBRef;
import org.ow2.easybeans.api.naming.EZBJNDINamingInfo;

/**
 * Binding of the reference in the JNDI/Registry.
 * @author Florent BENOIT
 *         Contributors:
 *             S. Ali Tokmen (JNDI naming strategy)
 */
public class JNDIBindingFactory implements EZBBindingFactory {

    /**
     * Binds a reference with the given JNDI name and the given referenceable
     * object.
     * @param ref the EasyBeans reference object
     * @throws BindingException if the bind fails.
     */
    public void bind(final EZBRef ref) throws BindingException {
        // Bind all JNDI names and create linkref for aliases
        List<EZBJNDINamingInfo> jndiInfos = ref.getJNDINamingInfos();
        if (jndiInfos != null) {
            for (EZBJNDINamingInfo jndiNamingInfo : jndiInfos) {

                Context javaContext = ref.getFactory().getJavaContext();
                String jndiName = jndiNamingInfo.jndiName();
                List<String> aliases = jndiNamingInfo.aliases();
                bind(jndiName, javaContext, ref);

                if (aliases != null) {
                    for (String alias : aliases) {
                        bind(alias, javaContext, new LinkRef(jndiName));
                    }
                }
            }
        }
    }

    /**
     * Unbinds an object with the given JNDI name.
     * @param ref the EasyBeans reference object.
     * @throws BindingException if any unbind fails.
     */
    public void unbind(final EZBRef ref) throws BindingException {
        // Unbind all JNDI names and the aliases
        List<EZBJNDINamingInfo> jndiInfos = ref.getJNDINamingInfos();
        if (jndiInfos != null) {
            for (EZBJNDINamingInfo jndiNamingInfo : jndiInfos) {

                String jndiName = jndiNamingInfo.jndiName();
                List<String> aliases = jndiNamingInfo.aliases();

                unbind(jndiName, ref.getFactory().getJavaContext());

                if (aliases != null) {
                    for (String alias : aliases) {
                        unbind(alias, ref.getFactory().getJavaContext());
                    }
                }

            }
        }

    }


    /**
     * Unbind the given JNDI name.
     * @param jndiName the name to unbind
     * @param javaContext the javaContext if parameters have to be added
     * @param toBind the object to bind
     * @throws BindingException if unbind fails
     */
    protected void bind(final String jndiName, final Context javaContext, final Object toBind) throws BindingException {

        try {
            //java prefix ?
            if (jndiName.startsWith("java:")) {
                javaContext.rebind(jndiName.substring("java:".length()), toBind);
            } else {
                new InitialContext().rebind(jndiName, toBind);
            }
        } catch (NamingException e) {
            throw new BindingException("Cannot bind the object with JNDI name '" + jndiName + "'", e);
        }

    }



    /**
     * Unbind the given JNDI name.
     * @param jndiName the name to unbind
     * @param javaContext the java: context
     * @throws BindingException if unbind fails
     */
    protected void unbind(final String jndiName, final Context javaContext) throws BindingException {
        try {
            if (jndiName.startsWith("java:")) {
                javaContext.unbind(jndiName.substring("java:".length()));
            } else {
                try {
                    new InitialContext().lookupLink(jndiName);
                } catch (NameNotFoundException e) {
                    // NA, do not unbind
                    return;
                }
                new InitialContext().unbind(jndiName);
            }
        } catch (NamingException e) {
            throw new BindingException("Cannot unbind the object with JNDI name '" + jndiName + "'.", e);
        }
    }
}
