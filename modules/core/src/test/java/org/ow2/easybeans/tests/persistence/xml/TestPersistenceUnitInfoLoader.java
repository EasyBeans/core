/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
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
 * $Id: TestPersistenceUnitInfoLoader.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.tests.persistence.xml;

import java.net.URL;
import java.util.Properties;

import javax.persistence.spi.PersistenceUnitTransactionType;

import org.ow2.easybeans.persistence.xml.JPersistenceUnitInfo;
import org.ow2.easybeans.persistence.xml.JPersistenceUnitInfoLoader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Check that the XML parsing/analyzing is working.
 * @author Florent BENOIT
 */
public class TestPersistenceUnitInfoLoader {

    /**
     * Path to persistence XML file.
     */
    private static final String PERSISTENCE_XML_PATH = "org/ow2/easybeans/tests/persistence/xml/persistence1.xml";

    /**
     * Instance of persistence unit info object.
     */
    private JPersistenceUnitInfo persistenceUnitInfo = null;

    /**
     * Load persistence unit info object.
     * @throws Exception if XML is not parsed.
     */
    @BeforeClass
    public void loadPersistenceUnitInfo() throws Exception {
        // get URL
        URL persistenceXMl = this.getClass().getClassLoader().getResource(PERSISTENCE_XML_PATH);
        if (persistenceXMl == null) {
            throw new Exception("Unable to find '" + PERSISTENCE_XML_PATH + "'.");
        }

        // Load object
        JPersistenceUnitInfo[] persistenceUnitInfos = JPersistenceUnitInfoLoader.loadPersistenceUnitInfoImpl(persistenceXMl);
        if (persistenceUnitInfos == null || persistenceUnitInfos.length != 1) {
            throw new Exception("Invalid size of persistence unit infos");
        }

        // Keep object
        persistenceUnitInfo = persistenceUnitInfos[0];

    }


    /**
     * Test the persistence unit name.
     */
    @Test
    public void testPersistenceUnitName() {
        assertEquals(persistenceUnitInfo.getPersistenceUnitName(), "my-persistence-unit");
    }

    /**
     * Test the persistence unit transaction type.
     */
    @Test
    public void testPersistenceUnitTransactionType() {
        assertEquals(persistenceUnitInfo.getTransactionType(), PersistenceUnitTransactionType.JTA);
    }

    /**
     * Test the persistence provider.
     */
    @Test
    public void testPersistenceProvider() {
        assertEquals(persistenceUnitInfo.getPersistenceProviderClassName(), "my-persistence-provider");
    }

    /**
     * Test the persistence provider JTA datasource.
     */
    @Test
    public void testPersistenceJTADataSource() {
        assertEquals(persistenceUnitInfo.getJtaDataSourceName(), "my-jta-datasource");
    }

    /**
     * Test the persistence provider no JTA datasource.
     */
    @Test
    public void testPersistenceNoJTADataSource() {
        assertEquals(persistenceUnitInfo.getNonJtaDataSourceName(), "my-nojta-datasource");
    }

    /**
     * Test the persistence provider properties.
     */
    @Test
    public void testPersistenceProperties() {
        Properties p = persistenceUnitInfo.getProperties();
        assertNotNull(p);
        assertEquals(p.getProperty("myproperty"), "myvalue");
    }


}
