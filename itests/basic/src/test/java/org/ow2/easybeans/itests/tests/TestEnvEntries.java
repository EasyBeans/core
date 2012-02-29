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
 * $Id: TestEnvEntries.java 5743 2011-02-28 16:02:42Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.itests.tests;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.ow2.easybeans.application.enventry.IEnvEntry;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the Env-Entries.
 * @author Florent Benoit
 */
public class TestEnvEntries {

    /**
     * Bean.
     */
    private IEnvEntry envEntryBean = null;

    /**
     * Bean without env-entry-type.
     */
    private IEnvEntry noTypeEnvEntryBean = null;

    /**
     * Bean with @Resource on env entries.
     */
    private IEnvEntry annotationResourceEntryBean = null;

    /**
     * Bean with @Resource on env entries but without value so we should keep the values.
     */
    private IEnvEntry defaultEntryBean = null;


    @BeforeClass
    public void getBeans() throws NamingException {
        this.envEntryBean = (IEnvEntry) new InitialContext().lookup("EnvEntryBean@Remote");
        this.noTypeEnvEntryBean = (IEnvEntry) new InitialContext().lookup("NoTypeEnvEntryBean@Remote");
        this.annotationResourceEntryBean = (IEnvEntry) new InitialContext().lookup("AnnotationResourceEnvEntryBean@Remote");
        this.defaultEntryBean = (IEnvEntry) new InitialContext().lookup("DefaultEnvEntryBean@Remote");


    }

    @Test
    public void testInjectedFieldsEntries() {
        this.envEntryBean.checkInjectedFields();
    }

    @Test
    public void checkCompNotEqualsModuleEntries() throws NamingException {
        this.envEntryBean.checkCompNotEqualsModule();
    }

    @Test
    public void testInjectedFieldsNoType() {
        this.noTypeEnvEntryBean.checkInjectedFields();
    }

    @Test
    public void checkCompNotEqualsModuleNoType() throws NamingException {
        this.noTypeEnvEntryBean.checkCompNotEqualsModule();
    }


    @Test
    public void testInjectedFieldsAnnotationResource() {
        this.annotationResourceEntryBean.checkInjectedFields();
    }

    @Test
    public void checkCompNotEqualsModuleAnnotationResource() throws NamingException {
        this.annotationResourceEntryBean.checkCompNotEqualsModule();
    }

    @Test
    public void testInjectedFieldsDefault() {
        this.defaultEntryBean.checkInjectedFields();
    }

    @Test
    public void checkCompNotEqualsModuleDefault() throws NamingException {
        this.defaultEntryBean.checkCompNotEqualsModule();
    }
}
