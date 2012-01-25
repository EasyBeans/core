/**
 * EasyBeans
 * Copyright (C) 2006-2007 Bull S.A.S.
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
 * $Id: IRemoteDeployer.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployer;

/**
 * Interface allowing to deploy some files. This interface will be exposed
 * through JMX.
 * @author Florent Benoit
 */
public interface IRemoteDeployer {

    /**
     * Dump the given bytes to a local file and then load this file by using a
     * local deployer.
     * @param fileName the name of the file to deploy
     * @param fileContent the content of the given file
     */
    void deployFile(String fileName, byte[] fileContent);

    /**
     * Dump the given bytes to a local file and then return the path to this
     * file.
     * @param fileName the name of the file to deploy
     * @param fileContent the content of the given file
     * @return the path of the deployed file
     */
    String dumpFile(String fileName, byte[] fileContent);

    /**
     * Deploy a file to a local deployer.
     * @param fileName the name of the file to deploy
     */
    void deploy(final String fileName);

    /**
     * Undeploy a file by using a local deployer.
     * @param fileName the name of the file to undeploy
     */
    void undeploy(final String fileName);

    /**
     * Checks if the given file is deployed or not.
     * @param fileName test if a given file is already deployed.
     * @return true if the given deployable is deployed.
     */
    boolean isDeployed(final String fileName);
}
