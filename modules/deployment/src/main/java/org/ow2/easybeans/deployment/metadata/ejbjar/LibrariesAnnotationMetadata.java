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
 * $Id: LibrariesAnnotationMetadata.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar;

import java.util.List;

import org.ow2.easybeans.deployment.metadata.ejbjar.view.EasyBeansEjbJarView;


/**
 * Metadata of libraries that can be packaged within an EAR and used by the EJBs.
 * @author Florent BENOIT
 */
public class LibrariesAnnotationMetadata  {

    /**
     * List of Metadata for each Library.
     */
    private List<EasyBeansEjbJarView> ejbJarAnnotationMetadataList = null;


    /**
     * @return list of metadata for the given jar.
     */
    public List<EasyBeansEjbJarView> getEjbJarAnnotationMetadataList() {
        return ejbJarAnnotationMetadataList;
    }

    /**
     * Sets the list of jar metadata.
     * @param ejbJarAnnotationMetadataList the list of jar metadata
     */
    public void setEjbJarAnnotationMetadataList(final List<EasyBeansEjbJarView> ejbJarAnnotationMetadataList) {
        this.ejbJarAnnotationMetadataList = ejbJarAnnotationMetadataList;
    }




}
