/**
 * EasyBeans
 * Copyright (C) 2009 Bull S.A.S.
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
 * $Id: WebserviceSLSB.java 4832 2009-03-23 17:46:11Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.test;

import javax.ejb.Stateless;
import javax.jws.WebService;
import javax.jws.WebMethod;

/**
 * The WebserviceSLSB is ...
 *
 * @author Guillaume Sauthier
 */
@Stateless
@WebService
public class WebserviceSLSB {

    @WebMethod
    public String hello(String who) {
        return "Hello " + who;
    }
}
