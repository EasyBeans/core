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

    @BeforeClass
    public void getBean() throws NamingException {
        this.envEntryBean = (IEnvEntry) new InitialContext().lookup("EnvEntryBean@Remote");
    }

    @Test
    public void testInjectedFields() {
        this.envEntryBean.checkInjectedFields();
    }

    @Test
    public void checkCompEqualsModule() throws NamingException {
        this.envEntryBean.checkCompEqualsModule();
    }

    @Test
    public void checkAppNotEqualsModule() throws NamingException {
        this.envEntryBean.checkAppNotEqualsModule();
    }

}
