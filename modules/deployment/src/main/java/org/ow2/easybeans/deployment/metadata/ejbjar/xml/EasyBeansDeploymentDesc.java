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
 * $Id: EasyBeansDeploymentDesc.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.xml;

import java.io.Serializable;
import java.net.URL;

import org.ow2.util.archive.api.ArchiveException;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.metadata.common.impl.xml.parsing.ParsingException;
import org.ow2.util.xmlconfig.XMLConfiguration;
import org.ow2.util.xmlconfig.XMLConfigurationException;

/**
 * Utility class to get the specific deployment descriptor.
 * @author Florent Benoit
 */
public final class EasyBeansDeploymentDesc {

    /**
     * Name of the entry for the specific DD.
     */
    private static final String EASYBEANS_DD_ENTRY = "META-INF/easybeans.xml";

    /**
     * Utility class, no public constructor.
     */
    private EasyBeansDeploymentDesc() {

    }

    /**
     * Gets EasyBeans specific Deployment Descriptor for a given archive.
     * @param archive file/directory.
     * @throws ParsingException if no xml is present.
     * @return EasyBeansDD instance or null.
     */
    public static EasyBeansDD getEasyBeansDD(final IArchive archive) throws ParsingException {

        // Get XML entry
        URL easybeansXmlURL = null;
        try {
            easybeansXmlURL = archive.getResource(EASYBEANS_DD_ENTRY);
        } catch (ArchiveException e) {
            throw new ParsingException("Cannot get resource '" + EASYBEANS_DD_ENTRY + "' on the archive '"
                    + archive.getName() + "'.");
        }

        EasyBeansDD easyBeansDD = null;
        if (easybeansXmlURL != null) {
            easyBeansDD = new EasyBeansDD();
            XMLConfiguration xmlConfiguration = new XMLConfiguration(easybeansXmlURL, "easybeans-mapping.xml");
            try {
                xmlConfiguration.configure(easyBeansDD);
            } catch (XMLConfigurationException e) {
                throw new ParsingException(
                        "Cannot create the object representing EasyBeans specific Deployment Descriptor", e);
            }
        }
        return easyBeansDD;
    }
}
