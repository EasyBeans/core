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
 * $Id: JavaCompExtensionListener.java 5749 2011-02-28 17:15:08Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.helper.listener;

import static javax.ejb.TransactionManagementType.CONTAINER;

import javax.naming.Context;
import javax.naming.NamingException;

import org.ow2.easybeans.api.Factory;
import org.ow2.easybeans.container.EasyBeansEJBContext;
import org.ow2.easybeans.container.mdb.EasyBeansMessageDrivenContext;
import org.ow2.easybeans.container.mdb.MDBFactory;
import org.ow2.easybeans.container.session.EasyBeansSessionContext;
import org.ow2.easybeans.container.session.singleton.SingletonSessionFactory;
import org.ow2.easybeans.container.session.stateful.StatefulSessionFactory;
import org.ow2.easybeans.container.session.stateless.StatelessSessionFactory;
import org.ow2.easybeans.event.naming.JavaContextNamingEvent;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * This ExtensionListener is dedicated to adapt the <code>java:comp</code> Context.
 * It removes the <code>UserTransaction</code> object if required, add the
 * <code>EJBContext</code> entry and the <code>TimerService</code> entry.
 * @author Guillaume Sauthier
 */
public class JavaCompExtensionListener extends AbstractExtensionListener {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JavaCompExtensionListener.class);

    /**
     * Process the event.
     * @param event the given event
     */
    public void handle(final IEvent event) {

        JavaContextNamingEvent cne = (JavaContextNamingEvent) event;
        Factory<?, ?> easyBeansFactory = cne.getFactory();
        Context javaCtx = cne.getJavaContext();
        Context compCtx = null;
        try {
            compCtx = (Context) javaCtx.lookup("comp");
        } catch (NamingException e) {
            throwException(cne, new IllegalStateException("Cannot get java:comp object", e));
        }

        // Unbind UserTransaction context if it is not a BMT bean
        // as specified in chapter 16.12 of EJB 3 spec.
        if (easyBeansFactory.getBeanInfo().getTransactionManagementType() == CONTAINER) {
            logger.debug("Bean is container managed so remove availability of java:comp/UserTransaction object");
            try {
                compCtx.unbind("UserTransaction");
            } catch (NamingException e) {
                throwException(cne, new IllegalStateException("Cannot remove java:comp/UserTransaction object", e));
            }
        }

        // bind EJBContext
        EasyBeansEJBContext<?> context = null;
        if (easyBeansFactory instanceof StatelessSessionFactory) {
            context = new EasyBeansSessionContext<StatelessSessionFactory>((StatelessSessionFactory) easyBeansFactory);
        } else if (easyBeansFactory instanceof StatefulSessionFactory) {
            context = new EasyBeansSessionContext<StatefulSessionFactory>((StatefulSessionFactory) easyBeansFactory);
        } else if (easyBeansFactory instanceof SingletonSessionFactory) {
                context = new EasyBeansSessionContext<SingletonSessionFactory>((SingletonSessionFactory) easyBeansFactory);
        } else if (easyBeansFactory instanceof MDBFactory) {
            context = new EasyBeansMessageDrivenContext((MDBFactory) easyBeansFactory);
        } else {
            throwException(cne, new IllegalStateException("Unable to detect factory type '" + easyBeansFactory + "'"));
        }


        try {
            compCtx.bind("EJBContext", context);
        } catch (NamingException e) {
            throwException(cne, new IllegalStateException("Cannot bind EJBContext", e));
        }

        // bind TimerService
        try {
            compCtx.bind("TimerService", context.getTimerService());
        } catch (NamingException e) {
            throwException(cne, new IllegalStateException("Cannot bind TimerService", e));
        }
    }
}
