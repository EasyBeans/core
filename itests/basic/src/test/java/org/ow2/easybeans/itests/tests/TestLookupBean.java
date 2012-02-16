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

import org.ow2.easybeans.application.lookup.ILookup;
import org.ow2.easybeans.application.lookup.ISimple;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test @EJB(lookup) and ejb-jar.xml lookup-name entries.
 * @author Florent Benoit
 */
public class TestLookupBean {

    /**
     * Bean with annotation.
     */
    private ILookup lookupBeanAnnotation = null;

    /**
     * Bean with XML.
     */
    private ILookup lookupBeanXML = null;

    @BeforeClass
    public void getBean() throws NamingException {
        this.lookupBeanAnnotation = (ILookup) new InitialContext().lookup("AnnotationLookupBean");
        this.lookupBeanXML = (ILookup) new InitialContext().lookup("XMLLookupBean");
    }

    @Test
    public void testInjectedAnnotationBean() {
        check(this.lookupBeanAnnotation);
    }

    @Test
    public void testInjectedXMLBean() {
        check(this.lookupBeanXML);
    }



    protected void check(final ILookup bean) {
        ISimple simpleBean1 = bean.getBean1();
        Assert.assertNotNull(simpleBean1);
        ISimple simpleBean2 = bean.getBean2();
        Assert.assertNotNull(simpleBean2);

        Assert.assertEquals(simpleBean1.getValue(), "Singleton");
        Assert.assertEquals(simpleBean2.getValue(), "Stateless");

    }

}
