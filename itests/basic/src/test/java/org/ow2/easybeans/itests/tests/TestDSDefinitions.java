/*
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: jonas-team@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * --------------------------------------------------------------------------
 *  $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.itests.tests;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.datasources.IDSDefinitionBean;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author Loic Albertin
 */
public class TestDSDefinitions {

    /**
     * Remote bean.
     */
    private IDSDefinitionBean simpleDSDefinitionBean = null;

    /**
     * Remote bean.
     */
    private IDSDefinitionBean multiDSDefinitionBean = null;

    /**
     * Remote bean.
     */
    private IDSDefinitionBean overrideDSDefinitionBean = null;

    /**
     * Initialize beans.
     *
     * @throws javax.naming.NamingException if cannot get the bean
     */
    @BeforeClass
    public void getBeans() throws NamingException {
        this.simpleDSDefinitionBean = (IDSDefinitionBean) new InitialContext().lookup("SimpleDSDefinitionEJB");
        this.multiDSDefinitionBean = (IDSDefinitionBean) new InitialContext().lookup("MultiDSDefinitionEJB");
        this.overrideDSDefinitionBean = (IDSDefinitionBean) new InitialContext().lookup("OverrideAnnotationDSBeanEJB");
    }

    @Test
    public void testSimpleDSDefBean() {
        this.simpleDSDefinitionBean.testDataSourceInjection();
    }

    @Test
    public void testMultiDSDefBean() {
        this.multiDSDefinitionBean.testDataSourceInjection();
    }

    @Test
    public void testOverrideDSDefBean() {
        this.overrideDSDefinitionBean.testDataSourceInjection();
    }
}
