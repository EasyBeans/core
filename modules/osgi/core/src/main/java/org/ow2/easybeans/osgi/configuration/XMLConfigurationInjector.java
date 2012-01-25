/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
 * Contact: easybeans@objectweb.org
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
 * $Id: XMLConfigurationInjector.java 4313 2008-11-13 10:17:59Z sauthieg $
 * --------------------------------------------------------------------------
 */
package org.ow2.easybeans.osgi.configuration;

import org.ow2.easybeans.component.api.EZBComponent;
import org.ow2.easybeans.server.Embedded;
import org.ow2.util.xmlconfig.XMLConfiguration;
import org.ow2.util.xmlconfig.XMLConfigurationException;

/**
 * TODO: Class Description.
 *
 * @author David Alves
 * @version $Revision$
 */
public class XMLConfigurationInjector extends XMLConfiguration {

    private EZBComponent componentObject;

    private Embedded embedded;

    /**
     * Instantiates a new XML configuration injector.
     *
     * @param componentClass the component class
     * @param xmlConfiguration the xml configuration
     *
     * @throws XMLConfigurationException the XML configuration exception
     */
    public XMLConfigurationInjector(final Class<? extends EZBComponent> componentClass,
                                    final String xmlConfiguration) throws XMLConfigurationException {
        super("easybeans-mapping.xml");
        try {
            componentObject = componentClass.newInstance();
        } catch (Exception e) {
            throw new XMLConfigurationException("Could not instantiate component class: " + componentClass.getName(), e);
        }
        // TODO refactor so this doesn't need to configure the whole container
        embedded = new Embedded();
        this.addConfigurationSource(new StringConfigurationSource(xmlConfiguration));
        configure(embedded);

    }

    public EZBComponent getConfiguredComponent(){
        for (EZBComponent component : embedded.getComponents().getEZBComponents()) {
            if (component.getClass().equals(componentObject.getClass())){
                return component;
            }
        }
        return null;
    }

}
