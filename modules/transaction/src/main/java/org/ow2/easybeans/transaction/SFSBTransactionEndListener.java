/*
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
 * $Id:$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.transaction;

import javax.transaction.Synchronization;

import org.ow2.easybeans.api.bean.EasyBeansSFSB;

/**
 * This Listener is notified of a Transaction completion and updates the StatefulBean accordingly.
 * @author Loic Albertin
 */
public class SFSBTransactionEndListener implements Synchronization {

    /**
     * Stateful session bean that is used to manage Session synchronization.
     */
    private EasyBeansSFSB statefulBean = null;


    public SFSBTransactionEndListener(EasyBeansSFSB statefulBean) {
        this.statefulBean = statefulBean;
    }

    public void beforeCompletion() {
        // Nothing to do
    }

    public void afterCompletion(int i) {
        if (statefulBean != null) {
            statefulBean.setInTransaction(false);
        }
    }
}
