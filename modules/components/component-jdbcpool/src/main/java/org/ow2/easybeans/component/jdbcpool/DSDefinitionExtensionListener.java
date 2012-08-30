/*
 * JOnAS: Java(TM) Open Application Server
 * Copyright (C) 2012 Bull S.A.S.
 * Contact: jonas-team@ow2.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA
 *
 * --------------------------------------------------------------------------
 *  $Id$
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.jdbcpool;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;

import org.ow2.easybeans.api.bean.info.IBeanInfo;
import org.ow2.easybeans.api.event.naming.EZBJavaContextNamingEvent;
import org.ow2.util.ee.metadata.common.api.struct.IJAnnotationSqlDataSourceDefinition;
import org.ow2.util.event.api.EventPriority;
import org.ow2.util.event.api.IEvent;
import org.ow2.util.event.api.IEventListener;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * @author Loic Albertin
 */
public class DSDefinitionExtensionListener implements IEventListener {

    private static Log logger = LogFactory.getLog(DSDefinitionExtensionListener.class);

    private List<String> globalDataSourcesNames = new ArrayList<String>();

    private Context globalContext;

    public DSDefinitionExtensionListener() {
        try {
            globalContext = new InitialContext();
        } catch (NamingException e) {
            logger.error("Unable to retrieve global JNDI context", e);
        }
    }

    public void handle(IEvent event) {
        EZBJavaContextNamingEvent javaContextNamingEvent = (EZBJavaContextNamingEvent) event;
        IBeanInfo beanInfo = javaContextNamingEvent.getFactory().getBeanInfo();
        Context context = javaContextNamingEvent.getJavaContext();


        for (IJAnnotationSqlDataSourceDefinition dataSourceDefinition : beanInfo.getDataSourceDefinitions()) {
            String normalizedDataSourceName = dataSourceDefinition.getName();
            if (normalizedDataSourceName.startsWith("java:")) {
                normalizedDataSourceName = normalizedDataSourceName.replaceFirst("java:", "");
            } else {
                normalizedDataSourceName = "comp/env/" + normalizedDataSourceName;
            }

            final String globalJndiName = javaContextNamingEvent.getFactory().getId() + "_" + normalizedDataSourceName;
            ConnectionManager connectionManager = ConnectionManager.getConnectionManager(globalJndiName);
            if (connectionManager != null) {
                //Already registered
                continue;
            }

            try {
                connectionManager = new ConnectionManager();
                connectionManager.setDSName(globalJndiName);
                connectionManager.setClassName(dataSourceDefinition.getClassName());
                connectionManager.setUrl(dataSourceDefinition.getUrl());
                connectionManager.setUserName(dataSourceDefinition.getUser());
                connectionManager.setPassword(dataSourceDefinition.getPassword());

                if (dataSourceDefinition.getIsolationLevel() == Connection.TRANSACTION_SERIALIZABLE) {
                    connectionManager.setTransactionIsolation("serializable");
                } else if (dataSourceDefinition.getIsolationLevel() == Connection.TRANSACTION_NONE) {
                    connectionManager.setTransactionIsolation("none");
                } else if (dataSourceDefinition.getIsolationLevel() == Connection.TRANSACTION_READ_COMMITTED) {
                    connectionManager.setTransactionIsolation("read_committed");
                } else if (dataSourceDefinition.getIsolationLevel() == Connection.TRANSACTION_READ_UNCOMMITTED) {
                    connectionManager.setTransactionIsolation("read_uncommitted");
                } else if (dataSourceDefinition.getIsolationLevel() == Connection.TRANSACTION_REPEATABLE_READ) {
                    connectionManager.setTransactionIsolation("repeatable_read");
                } else {
                    connectionManager.setTransactionIsolation("default");
                }
                connectionManager.setMaxWaitTime(dataSourceDefinition.getMaxIdleTime());
                connectionManager.setPoolMax(dataSourceDefinition.getMaxPoolSize());
                connectionManager.setPoolMin(dataSourceDefinition.getMinPoolSize());
                connectionManager.setPstmtMax(dataSourceDefinition.getMaxStatements());

                //Register connection manager globally
                globalContext.rebind(globalJndiName, connectionManager);
                globalDataSourcesNames.add(globalJndiName);
                context.rebind(normalizedDataSourceName, new LinkRef(globalJndiName));
            } catch (Exception e) {
                logger.error("Unable to define data source {0}", dataSourceDefinition.getName(), e);
            }
        }
    }

    public boolean accept(IEvent event) {
        if (event instanceof EZBJavaContextNamingEvent) {
            EZBJavaContextNamingEvent javaContextNamingEvent = (EZBJavaContextNamingEvent) event;

            // source/event-provider-id attribute is used to filter the destination
            if ("java:".equals(javaContextNamingEvent.getEventProviderId())) {
                return true;
            }
        }
        return false;
    }

    public EventPriority getPriority() {
        return EventPriority.SYNC_NORM;
    }

    public void clearAllGlobalDataSources() {
        for (String globalDataSourcesName : globalDataSourcesNames) {
            try {
                globalContext.unbind(globalDataSourcesName);
            } catch (NamingException e) {
                logger.error("Unable to unbind global data-source: " + globalDataSourcesName, e);
            }
        }
        globalDataSourcesNames.clear();
    }
}
