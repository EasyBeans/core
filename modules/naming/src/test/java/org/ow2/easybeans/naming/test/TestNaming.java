/**
 * EasyBeans
 * Copyright (C) 2011 Bull S.A.S.
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
 * $Id: TestNaming.java 5736 2011-02-22 08:27:11Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming.test;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameNotFoundException;
import javax.naming.NamingException;
import javax.transaction.UserTransaction;

import org.objectweb.jotm.Current;
import org.ow2.easybeans.naming.NamingManager;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the java: namespace.
 * @author Florent Benoit
 */
public class TestNaming {

    /**
     * Initial Context.
     */
    private Context initialContext = null;

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(TestNaming.class);

    /**
     * Naming Manager instance.
     */
    private NamingManager namingManager;

    /**
     * Setup the java: naming.
     * @throws NamingException if setup fails
     */
    @BeforeClass
    public void init() throws NamingException {
        this.namingManager = NamingManager.getInstance();

        this.initialContext = new InitialContext();
        this.logger.debug("InitialContext = ''{0}''.", this.initialContext);
        // fake bind
        this.initialContext.bind("javax.transaction.UserTransaction", new Current());

        Context context = this.namingManager.createEnvironmentContext("test");
        this.namingManager.setComponentContext(context);
    }

    /**
     * Test java:global.
     * @throws NamingException if lookup fails
     */
    @Test
    public void testGlobal() throws NamingException {
        Context javaContext = (Context) this.initialContext.lookup("java:");
        this.logger.debug("javaContext = ''{0}''.", javaContext);
        Context global = (Context) this.initialContext.lookup("java:global");
        this.logger.debug("globalContext = ''{0}''.", javaContext);
        String name = "name1";
        String value = "value1";
        global.bind(name, value);

        Assert.assertEquals(value, global.lookup(name));
    }

    /**
     * Test there is only java:global for all modules.
     * @throws NamingException if lookup fails
     */
    @Test
    public void testOnlyOneGlobal() throws NamingException {
        String key = "comp/env/myValue";

        // Value1 for the first component
        Context context1 = this.namingManager.createEnvironmentContext("component1");
        String value1 = "Value1";
        context1.bind(key, value1);

        // Value2 for other component
        Context context2 = this.namingManager.createEnvironmentContext("component1");
        String value2 = "Value2";
        context2.bind(key, value2);

        // global should be the same
        String globalKey = "global/sameKeyTest";
        String globalValue = "sameValueTest";
        context1.bind(globalKey, globalValue);

        Context old = this.namingManager.setComponentContext(context1);
        try {
            // do lookup within component1
            String globalValueComponent1 = (String) this.initialContext.lookup("java:" + globalKey);
            String moduleValueComponent1 = (String) this.initialContext.lookup("java:" + key);

            // do lookup within component2
            this.namingManager.setComponentContext(context2);
            String globalValueComponent2 = (String) this.initialContext.lookup("java:" + globalKey);
            String moduleValueComponent2 = (String) this.initialContext.lookup("java:" + key);

            // global should be the same
            Assert.assertEquals(globalValueComponent1, globalValue);
            Assert.assertEquals(globalValueComponent1, globalValueComponent2);

            // env value should be different for two components
            Assert.assertEquals(moduleValueComponent1, value1);
            Assert.assertEquals(moduleValueComponent2, value2);
            Assert.assertNotSame(moduleValueComponent1, moduleValueComponent2);

        } finally {
            // reset
            this.namingManager.setComponentContext(old);
        }

    }

    /**
     * Test Equals java:comp and java:module.
     * @throws NamingException if lookup fails
     */
    @Test
    public void testCompEqualsModule() throws NamingException {
        Context compContext = (Context) this.initialContext.lookup("java:comp");
        this.logger.debug("compContext = ''{0}''.", compContext);
        Context moduleContext = (Context) this.initialContext.lookup("java:module");
        this.logger.debug("moduleContext = ''{0}''.", moduleContext);

        Assert.assertEquals(compContext, moduleContext);
    }

    /**
     * Test Equals java:comp and java:module.
     * @throws NamingException if lookup fails
     */
    @Test(dependsOnMethods = "testCompEqualsModule")
    public void testCompModuleContent() throws NamingException {
        UserTransaction userTransactionComp = (UserTransaction) this.initialContext.lookup("java:comp/UserTransaction");
        UserTransaction userTransactionModule = (UserTransaction) this.initialContext.lookup("java:module/UserTransaction");

        Assert.assertEquals(userTransactionComp, userTransactionModule);
    }

    /**
     * Test java:app context.
     * @throws NamingException if lookup fails
     */
    @Test()
    public void testAppContext() throws NamingException {
        String key = "java:app/myKeyApp";
        String value = "myValueApp";

        this.initialContext.bind(key, value);
        String resultValue = (String) this.initialContext.lookup(key);
        Assert.assertEquals(resultValue, value);
    }

    /**
     * Test java:global context.
     * @throws NamingException if lookup fails
     */
    @Test()
    public void testBindUnbind() throws NamingException {
        String key = "java:global/testBindUnbind";
        String value = "myValueApp";

        String result = null;
        try {
            result = (String) this.initialContext.lookup(key);
            Assert.fail("Lookup should fail as it is not bound");
        } catch (NameNotFoundException e) {
            Assert.assertNotNull(e);
        }

        this.initialContext.bind(key, value);

        result = (String) this.initialContext.lookup(key);
        Assert.assertEquals(result, value);

        // Unbind
        this.initialContext.unbind(key);

        try {
            result = (String) this.initialContext.lookup(key);
            Assert.fail("Lookup should fail as it was unbind");
        } catch (NameNotFoundException e) {
            Assert.assertNotNull(e);
        }


    }

}
