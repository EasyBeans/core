/**
 * EasyBeans
 * Copyright (C) 2012 Bull S.A.S.
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
 * $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.itests.tests;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.statefulsync.IRemoteStatefulSyncBean;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the synchronization methods of stateful session beans.
 * @author Florent Benoit
 */
public class TestStatefulSync {


    /**
     * Remote bean.
     */
    private IRemoteStatefulSyncBean remoteTesterBean = null;


    /**
     * Initialize beans.
     * @throws NamingException if cannot get the bean
     */
    @BeforeClass
    public void getBeans() throws NamingException {
        this.remoteTesterBean = (IRemoteStatefulSyncBean) new InitialContext().lookup("RemoteTesterStatefulSyncBean");
    }


    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulAfterBeginBean() {
        this.remoteTesterBean.checkAnnotatedStatefulAfterBeginBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulAfterCompletionBean() {
        this.remoteTesterBean.checkAnnotatedStatefulAfterCompletionBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulAfterCompletionBeanRollback() {
        this.remoteTesterBean.checkAnnotatedStatefulAfterCompletionBeanRollback();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulBeforeCompletionBean() {
        this.remoteTesterBean.checkAnnotatedStatefulBeforeCompletionBean();
    }


    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulSyncBean() {
        this.remoteTesterBean.checkAnnotatedStatefulSyncBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testAnnotatedStatefulSyncBeanRollback() {
        this.remoteTesterBean.checkAnnotatedStatefulSyncBeanRollback();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testXmlStatefulPartialSyncBean() {
        this.remoteTesterBean.checkXmlStatefulPartialSyncBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testXmlStatefulPartialSyncBeanRollback() {
        this.remoteTesterBean.checkXmlStatefulPartialSyncBeanRollback();
    }


    /**
     * Check remote bean.
     */
    @Test
    public void testXmlStatefulSyncBean() {
        this.remoteTesterBean.checkXmlStatefulSyncBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testXmlStatefulSyncBeanRollback() {
        this.remoteTesterBean.checkXmlStatefulSyncBeanRollback();
    }


    /**
     * Check remote bean.
     */
    @Test
    public void testSessionSynchronizationBean() {
        this.remoteTesterBean.checkSessionSynchronizationBean();
    }

    /**
     * Check remote bean.
     */
    @Test
    public void testSessionSynchronizationBeanRollback() {
        this.remoteTesterBean.checkSessionSynchronizationBeanRollback();
    }




}
