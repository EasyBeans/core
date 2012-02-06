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

package org.ow2.easybeans.ejbinwar.tests;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.ow2.easybeans.ejbinwar.EJBInWarArchive;
import org.ow2.easybeans.ejbinwar.tests.war1.MyEmbeddedStateless;
import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.impl.MemoryArchive;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Test the EJB wrapped in a war file.
 * @author Florent Benoit
 */
public class TestEJBInWar {

    private MemoryArchive warArchive = null;

    private EJBInWarArchive ejbInWarArchive = null;


    @BeforeClass
    public void init() {
        // Build War
        this.warArchive = new MemoryArchive();
        // Add WEB-INF/ejb-jar.xml
        this.warArchive.addResource("WEB-INF/ejb-jar.xml", TestEJBInWar.class.getResource("/WEB-INF/ejb-jar.xml"));

        addInWebInfClass(MyEmbeddedStateless.class);

    }

    protected void addInWebInfClass(final Class<?> c) {
        String path = "WEB-INF/classes/".concat(encode(c));
        URL url = TestEJBInWar.class.getClassLoader().getResource(encode(c));
        this.warArchive.addResource(path, url);
    }

    protected String encode(final Class<?> c) {
        return c.getName().replace(".", "/").concat(".class");
    }

    @Test
    public void testCreateEjbInWar() throws ArchiveException {
        // Create EJB3 from War
        this.ejbInWarArchive = new EJBInWarArchive(this.warArchive);
        this.ejbInWarArchive.init();

    }

    @Test(dependsOnMethods="testCreateEjbInWar")
    public void testMetaInf() throws ArchiveException {

        // we may found the ejb-jar.xml in META-INF but not in WEB-INF
        Assert.assertNull(this.ejbInWarArchive.getResource("WEB-INF/ejb-jar.xml"));
        Assert.assertNotNull(this.ejbInWarArchive.getResource("META-INF/ejb-jar.xml"));

    }


    @Test(dependsOnMethods="testCreateEjbInWar")
    public void testGetResource() throws ArchiveException {
        // The EJB class should be there at the root of the archive
        URL url = this.ejbInWarArchive.getResource(encode(MyEmbeddedStateless.class));
        Assert.assertNotNull(url);
    }


    @Test(dependsOnMethods="testCreateEjbInWar")
    public void testGetResources() throws ArchiveException {
        // Check name entries
        Iterator<String> itEntries = this.ejbInWarArchive.getEntries();

        List<String> entries = new ArrayList<String>();
        while (itEntries.hasNext()) {
            entries.add(itEntries.next());
        }

        // check content
        Assert.assertTrue(entries.contains("META-INF/ejb-jar.xml"));
        Assert.assertTrue(entries.contains(encode(MyEmbeddedStateless.class)));
    }

}
