/**
 * EasyBeans
 * Copyright 2013 Peergreen S.A.S.
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
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.configurator;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarFieldMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarMethodMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansClassViewFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansEjbJarViewFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansFieldViewFactory;
import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansMethodViewFactory;
import org.ow2.util.archive.api.IArchive;
import org.ow2.util.ee.metadata.ejbjar.impl.configurator.EjbJarSessionConfigurator;
import org.ow2.util.scan.api.configurator.IAnnotationConfigurator;
import org.ow2.util.scan.api.configurator.IViewConfigurator;
import org.ow2.util.scan.api.configurator.basic.ViewConfigurator;

/**
 * Defines a session configurator
 * @author Florent Benoit
 */
public class EasyBeansSessionConfigurator extends EjbJarSessionConfigurator {

    public EasyBeansSessionConfigurator(IArchive archive) {
        super(archive);

        // add a new view
        IViewConfigurator viewConfigurator =  new ViewConfigurator();
        viewConfigurator.registerView(EasyBeansEjbJarMethodMetadata.class, new EasyBeansMethodViewFactory());
        viewConfigurator.registerView(EasyBeansEjbJarFieldMetadata.class, new EasyBeansFieldViewFactory());
        viewConfigurator.registerView(EasyBeansEjbJarClassMetadata.class, new EasyBeansClassViewFactory());
        viewConfigurator.registerView(EjbJarArchiveMetadata.class, new EasyBeansEjbJarViewFactory());
        getViewConfigurators().add(viewConfigurator);
        // add it to the ejbjar also
        getEjbJarMetadata().addViewConfigurator(viewConfigurator);

        IAnnotationConfigurator commonAnnotationConfigurator = new EasyBeansAnnotationConfigurator();
        getAnnotationConfigurators().add(commonAnnotationConfigurator);

    }

}
