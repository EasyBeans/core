/**
 * EasyBeans
 * Copyright (C) 2006 Bull S.A.S.
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
 * $Id: EasyBeansHandle.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.svc;

import javax.ejb.EJBObject;
import javax.ejb.Handle;

/**
 * Handle object of an EJB. This object allows to get the EJB Object. It uses
 * the HandleDelegate APi for managing its own serialization.
 * @author Florent Benoit.
 */
public class EasyBeansHandle implements Handle {


    /**
     * UId used for the serialization.
     */
    private static final long serialVersionUID = -5201378510750050833L;

    /**
     * EJB Object proxy (that is serializable).
     */
    private transient EJBObject ejbObject = null;

    /**
     * Build an handle for an EJBObject.
     * @param ejbObject the given EJBObject proxy.
     */
    public EasyBeansHandle(final EJBObject ejbObject) {
        this.ejbObject = ejbObject;
    }

    /**
     * Obtain the EJB object reference represented by this handle.
     * @return the EJB object reference represented by this handle.
     */
    public EJBObject getEJBObject() {
        return this.ejbObject;
    }

    /**
     * Specific implementation of serialization. Must call
     * HandleDelegate.writeEJBObject, as specified in 19.5.5.1 of spec EJB 2.1
     * @param out The output stream used to write object
     * @throws IOException error when writing object.
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        // Get the handle delegate Object.
        getHandleDelegate().writeEJBObject(getEJBObject(), out);
    }
     */

    /**
     * Specific implementation of deserialization. Must call
     * HandleDelegate.readEJBObject, as specified in 19.5.5.1 of spec EJB 2.1
     * @param in The input Stream from where is read the object.
     * @throws IOException error when reading object.
     * @throws ClassNotFoundException -
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        EJBObject obj = getHandleDelegate().readEJBObject(in);
    }
     */

}
