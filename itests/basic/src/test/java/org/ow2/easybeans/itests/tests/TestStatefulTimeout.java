/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.itests.tests;

import javax.ejb.NoSuchEJBException;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import org.ow2.easybeans.application.stateful.timeout.ITimeoutBean;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Loic Albertin
 */
public class TestStatefulTimeout {


    @Test
    public void testSimpleTimeout() throws InterruptedException, NamingException {
        /*
      Stateful test Bean.
     */
        ITimeoutBean simpleTimeoutBean = (ITimeoutBean) new InitialContext().lookup("simpleTimeoutBean");
        simpleTimeoutBean.ping();
        //Sleep for 50 ms
        Thread.sleep(50);
        simpleTimeoutBean.ping();
        //Sleep for 100 ms
        Thread.sleep(100);
        try {
            simpleTimeoutBean.ping();
        } catch (NoSuchEJBException e) {
            return;
        }
        Assert.fail("Timeout exceeded and call to the EJB doesn't throw a NoSuchEJBException");
    }

//    @Test
    public void testZeroTimeout() throws InterruptedException, NamingException {
        /*
         Stateful test Bean.
        */
        ITimeoutBean zeroTimeoutBean = (ITimeoutBean) new InitialContext().lookup("zero-timeout");
        // As the actual resolution is in ms, on some powerful machines we need to sleep the test a little bit to detect the timeout
        Thread.sleep(200);
        try {
            zeroTimeoutBean.ping();
        } catch (NoSuchEJBException e) {
            // Should return a  NoSuchEJBException immediately after the lookup
            return;
        }
        Assert.fail("Timeout exceeded and call to the EJB doesn't throw a NoSuchEJBException");
    }


    @Test
    public void testBMTInTxTimeout() throws InterruptedException, NamingException {
        /*
         Stateful test Bean.
        */
        ITimeoutBean inTxTimeoutBean = (ITimeoutBean) new InitialContext().lookup("BMTInTxBean");
        inTxTimeoutBean.init();
        Thread.sleep(200);
        // Initially it should  return a NoSuchEJBException immediately after the lookup but there is a running transaction
        inTxTimeoutBean.ping();
        // Now transaction is committed
        Thread.sleep(100);
        try {
            inTxTimeoutBean.ping();
        } catch (NoSuchEJBException e) {
            // Should return a  NoSuchEJBException immediately after the end of the transaction
            return;
        }
        Assert.fail("Timeout exceeded and call to the EJB doesn't throw a NoSuchEJBException");
    }

    @Test
    public void testCMTInTxTimeout()
            throws InterruptedException, NamingException, SystemException, NotSupportedException, RollbackException,
            HeuristicRollbackException, HeuristicMixedException {
        /*
         Stateful test Bean.
            */

        UserTransaction userTransaction = (UserTransaction) new InitialContext().lookup("javax.transaction.UserTransaction");
        ITimeoutBean simpleTimeoutBean = (ITimeoutBean) new InitialContext().lookup("simpleTimeoutBean");
        userTransaction.begin();
        simpleTimeoutBean.init();
        Thread.sleep(200);
        // Initially it should  return a NoSuchEJBException immediately after the lookup but there is a running transaction
        simpleTimeoutBean.ping();
        userTransaction.commit();
        // Now transaction is committed
        Thread.sleep(100);
        try {
            simpleTimeoutBean.ping();
        } catch (NoSuchEJBException e) {
            // Should return a  NoSuchEJBException immediately after the end of the transaction
            return;
        }
        Assert.fail("Timeout exceeded and call to the EJB doesn't throw a NoSuchEJBException");
    }


}
