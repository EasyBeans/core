/**
 * EasyBeans
 * Copyright (C) 2006-2010 Bull S.A.S.
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
 * $Id:JoramComponent.java 1022 2006-08-04 11:02:28Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.joram;

import java.lang.reflect.Field;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.QueueConnectionFactory;
import javax.jms.TopicConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.XAException;

import org.objectweb.joram.client.connector.ActivationSpecImpl;
import org.objectweb.joram.client.connector.JoramAdapter;
import org.objectweb.joram.client.jms.ConnectionMetaData;
import org.objectweb.joram.client.jms.admin.AdminException;
import org.objectweb.joram.client.jms.admin.AdminModule;
import org.objectweb.joram.client.jms.tcp.QueueTcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TcpConnectionFactory;
import org.objectweb.joram.client.jms.tcp.TopicTcpConnectionFactory;
import org.objectweb.joram.mom.proxies.ConnectionManager;
import org.objectweb.joram.mom.proxies.tcp.TcpProxyService;
import org.objectweb.util.monolog.Monolog;
import org.objectweb.util.monolog.api.LoggerFactory;
import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBWorkManagerComponent;
import org.ow2.easybeans.component.itf.JMSComponent;
import org.ow2.easybeans.component.itf.TMComponent;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

import fr.dyade.aaa.agent.AgentServer;
import fr.dyade.aaa.agent.ServerConfigHelper;
import fr.dyade.aaa.util.NullTransaction;

/**
 * Class that start/stop a simple collocated JORAM server.
 * It also creates some initial Topics/Queues.
 * @author Florent Benoit
 */
public class JoramComponent implements JMSComponent {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JoramComponent.class);

    /**
     * Queue connection factory (for external components).
     */
    private static final String QUEUE_CONN_FACT_NAME = "JQCF";

    /**
     * Topic connection factory (for external components).
     */
    private static final String TOPIC_CONN_FACT_NAME = "JTCF";

    /**
     * Connection factory (for external components).
     */
    private static final String CONN_FACT_NAME = "JCF";

    /**
     * Default port number.
     */
    private static final int DEFAULT_PORT_NUMBER = 16010;

    /**
     * Port number.
     */
    private int port = DEFAULT_PORT_NUMBER;

    /**
     * Default hostname.
     */
    private static final String DEFAULT_HOST_NAME = "localhost";

    /**
     * Host.
     */
    private String host = DEFAULT_HOST_NAME;

    /**
     * ID of the JORAM server.
     */
    private static final short ID = 0;

    /**
     * Transaction property (Set to remove persistence settings : transient). It
     * avoids the creation of a directory.
     */
    private static final String TRANSACTION_PROPERTY = "Transaction";

    /**
     * Default name of the persistence directory (won't be used).
     */
    private static final String DEFAULT_PERSISTENCE_DIRECTORY = "joram-persistence-s" + ID;

    /**
     * Server is started ?
     */
    private boolean started = false;

    /**
     * Instance of the resource adapter.
     */
    private JoramAdapter joramAdapter = null;

    /**
     * List of topics to create.
     */
    private List<String> topics = null;

    /**
     * List of queues to create.
     */
    private List<String> queues = null;

    /**
     * WorkManager used by this component.
     */
    private EZBWorkManagerComponent workManager = null;

    /**
     * Transaction component.
     */
    private TMComponent transactionComponent = null;

    /**
     * Initial Context.
     */
    private InitialContext ictx = null;

    /**
     * Default constructor.
     */
    public JoramComponent() {
        this.topics = new ArrayList<String>();
        this.queues = new ArrayList<String>();
    }


    /**
     * Init method.<br/>
     * This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

    }

    /**
     * Starts a Joram Server without persistence.
     * @throws EZBComponentException if start fails
     */
    public void start() throws EZBComponentException {
        // Initialize Monolog
        Properties properties = new Properties();
        properties.put(Monolog.MONOLOG_CLASS_NAME, "org.objectweb.util.monolog.wrapper.javaLog.LoggerFactory");
        properties.put("logger.com.scalagent.level", "ERROR");
        properties.put("logger.fr.dyade.aaa.level", "ERROR");
        properties.put("logger.org.objectweb.joram.level", "ERROR");
        // Avoid errors on CNFE (that are handled later) and a class that is missing in new joram version
        properties.put("logger.fr.dyade.aaa.agent.Service.level", "FATAL");
        properties.put("logger.org.objectweb.joram.mom.Destination.level", "FATAL");
        properties.put("logger.org.objectweb.joram.mom.Proxy.level", "FATAL");
        LoggerFactory factory = Monolog.init(properties);

        // Sets the JORAM monolog factory.
        Field field;
        try {
            field = fr.dyade.aaa.common.Debug.class.getDeclaredField("factory");
            field.setAccessible(true);
            field.set(null, factory);
        } catch (Exception e) {
            logger.error("Unable to setup logger of Joram", e);
        }

        // Build initial context
        try {
            this.ictx = new InitialContext();
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot create an initial context.", e);
        }

        // Check if the transaction component is present
        if (this.transactionComponent == null) {
            // Missing injection
            throw new EZBComponentException("Transaction component was not injected. Reason: Missing tm=\"#tm\""
                    + " in the easybeans.xml configuration file ?");
        }

        // Check if the work manager is present
        if (this.workManager == null) {
            // Missing injection
            throw new EZBComponentException("Work manager was not injected. Reason: Missing workmanager=\"#workmanager\""
                    + " in the easybeans.xml configuration file ?");
        }

        // Create BootstrapContext
        XATerminator xaTerminator = null;
        try {
            xaTerminator = this.transactionComponent.getXATerminator();
        } catch (XAException e) {
            throw new EZBComponentException("Cannot get the XA terminator", e);
        }

        BootstrapContext bootstrapContext = new JoramBootstrapContext(this.workManager, xaTerminator);

        // Create JORAM adapter
        this.joramAdapter = new JoramAdapter();

        // Set host/port
        this.joramAdapter.setHostName(this.host);
        this.joramAdapter.setServerPort(Integer.valueOf(this.port));

        // configure it (in fact the server will be collocated but started by this service and not by the resource adapter).
        this.joramAdapter.setCollocatedServer(Boolean.FALSE);

        // Set properties (transient)
        System.setProperty(TRANSACTION_PROPERTY, NullTransaction.class.getName());

        // Init
        try {
            AgentServer.init(ID, DEFAULT_PERSISTENCE_DIRECTORY, null);
        } catch (Exception e) {
            throw new EZBComponentException("Cannot initialize a new collocated Joram server", e);
        }

        // start Agent
       try {
           AgentServer.start();
        } catch (Exception e) {
            throw new EZBComponentException("Cannot start collocated Joram server", e);
        }

        // Add services
        try {
            new ServerConfigHelper(false).addService(ID, ConnectionManager.class.getName(), "root root");
        } catch (Exception e) {
            throw new EZBComponentException("Cannot add connection manager service", e);
        }
        // To enable TCP listener
        try {
            new ServerConfigHelper(false).addService(ID, TcpProxyService.class.getName(), String.valueOf(this.port));
        } catch (Exception e) {
            throw new EZBComponentException("Cannot add TcpProxy service", e);
        }


        // connect to the collocated server
        try {
            connectToCollocated();
        } catch (JoramException e) {
            throw new EZBComponentException("Cannot connect to the collocated server", e);
        }


        // Create anonymous user
        try {
            this.joramAdapter.createUser("anonymous", "anonymous");
        } catch (AdminException e) {
            throw new EZBComponentException("Cannot create anonymous user", e);
        } catch (ConnectException e) {
            throw new EZBComponentException("Cannot create anonymous user", e);
        }


        // start resource adapter
        try {
            this.joramAdapter.start(bootstrapContext);
        } catch (ResourceAdapterInternalException e) {
            throw new EZBComponentException("Cannot start the resource adapter of JORAM", e);
        }

        // create factories
        try {
            createConnectionFactories();
        } catch (JoramException e) {
            throw new EZBComponentException("Cannot create connection factories", e);
        }


        // initial topics and queues
        try {
            createInitialTopics();
        } catch (JoramException e) {
            throw new EZBComponentException("Cannot create initial topics", e);
        }

        try {
            createInitialQueues();
        } catch (JoramException e) {
            throw new EZBComponentException("Cannot create initial queues", e);
        }

        // Create and bind ActivationSpec
        ActivationSpec activationSpec = new ActivationSpecImpl();
        try {
            activationSpec.setResourceAdapter(this.joramAdapter);
        } catch (ResourceException e) {
            throw new EZBComponentException("Cannot set resource adapter on activation spec object", e);
        }
        try {
            this.ictx.rebind("joramActivationSpec", activationSpec);
        } catch (NamingException e) {
            throw new EZBComponentException("Cannot bind activation spec object", e);
        }

        logger.info("Joram version ''{0}'' started on {1}:{2}",
                ConnectionMetaData.providerVersion, this.host, String.valueOf(this.port));
        this.started = true;
    }

    /**
     * Stops the JORAM server (if started).
     * @throws EZBComponentException if stop is failing
     */
    public void stop() throws EZBComponentException {
        if (!this.started) {
            throw new IllegalStateException("Cannot stop a server as it was not started");
        }

        // Unbind Joram Activation Spec object
        try {
            this.ictx.unbind("joramActivationSpec");
        } catch (NamingException e) {
            logger.error("Cannot unbind activation spec object", e);
        }

        // Unbind factories
        try {
            this.ictx.unbind(CONN_FACT_NAME);
        } catch (NamingException e) {
            logger.error("Cannot unbind activation spec object", e);
        }
        try {
            this.ictx.unbind(QUEUE_CONN_FACT_NAME);
        } catch (NamingException e) {
            logger.error("Cannot unbind activation spec object", e);
        }
        try {
            this.ictx.unbind(TOPIC_CONN_FACT_NAME);
        } catch (NamingException e) {
            logger.error("Cannot unbind activation spec object", e);
        }

        // Stop adapter
        this.joramAdapter.stop();

        // disconnect
        disconnectFromCollocated();

        // stop
        AgentServer.stop();

        // Reset
        AgentServer.reset();



        // Info on stopping.
        logger.info("Joram Component Stopped");
    }

    /**
     * Connect to the collocated server to performg administration tasks. It
     * needs to be called before any admin task.
     * @throws JoramException if the connection to the collocated server fails
     */
    private void connectToCollocated() throws JoramException {
        try {
            AdminModule.collocatedConnect("root", "root");
        } catch (ConnectException e) {
            throw new JoramException("Cannot connect to the collocated server for the administration", e);
        } catch (AdminException e) {
            throw new JoramException("Cannot connect to the collocated server for the administration", e);
        }
    }

    /**
     * Disconnect from the collocated server. It needs to be called when
     * stopping to use admin task.
     * @throws AdminException
     * @throws ConnectException
     */
    private void disconnectFromCollocated() {
        AdminModule.disconnect();
    }

    /**
     * Create connection factories.
     * @throws JoramException if factories are not created.
     */
    private void createConnectionFactories() throws JoramException {
        // managed
        this.joramAdapter.createCF("CF");
        this.joramAdapter.createQueueCF("QCF");
        this.joramAdapter.createTopicCF("TCF");

        // Create connection factories  that will be used by a pure JMS Client
        ConnectionFactory jcf = null;
        TopicConnectionFactory jtcf = null;
        QueueConnectionFactory jqcf = null;

        String name = CONN_FACT_NAME;
        try {
            jcf = TcpConnectionFactory.create(this.host, this.port);
            this.ictx.rebind(name, jcf);
        } catch (NamingException e) {
            throw new JoramException("Cannot create a factory with the name '" + name + "'.", e);
        }

        name = QUEUE_CONN_FACT_NAME;
        try {
            jqcf = QueueTcpConnectionFactory.create(this.host, this.port);
            this.ictx.rebind(name, jqcf);
        } catch (NamingException e) {
            throw new JoramException("Cannot create a factory with the name '" + name + "'.", e);
        }

        name = TOPIC_CONN_FACT_NAME;
        try {
            jtcf = TopicTcpConnectionFactory.create(this.host, this.port);
            this.ictx.rebind(name, jtcf);
        } catch (NamingException e) {
            throw new JoramException("Cannot create a factory with the name '" + name + "'.", e);
        }
    }

    /**
     * Creates a topic with a given name.
     * @param name the topic's name.
     * @throws JoramException if the topic can't be created
     */
    private void createTopic(final String name) throws JoramException {
        try {
            this.joramAdapter.createTopic(name);
        } catch (AdminException e) {
            throw new JoramException("Cannot create a topic with the name '" + name + "'.", e);
        } catch (ConnectException e) {
            throw new JoramException("Cannot create a topic with the name '" + name + "'.", e);
        }
    }

    /**
     * Creates a queue with a given name.
     * @param name the topic's name.
     * @throws JoramException if the queue can't be created
     */
    private void createQueue(final String name) throws JoramException {
        try {
            this.joramAdapter.createQueue(name);
        } catch (AdminException e) {
            throw new JoramException("Cannot create a queue with the name '" + name + "'.", e);
        } catch (ConnectException e) {
            throw new JoramException("Cannot create a queue with the name '" + name + "'.", e);
        }
    }

    /**
     * Create the list of the defined topics.
     * @throws JoramException if topics can't be created
     */
    private void createInitialTopics() throws JoramException {
        for (String topic : this.topics) {
            createTopic(topic);
        }
    }

    /**
     * Create the list of the defined queues.
     * @throws JoramException if queues can't be created
     */
    private void createInitialQueues() throws JoramException {
        for (String queue : this.queues) {
            createQueue(queue);
        }
    }

    /**
     * Sets the initial queues of the Joram server.
     * @param queues the list of the name of the queues.
     */
    public void setQueues(final List<String> queues) {
        this.queues = queues;
    }

    /**
     * Gets the initial queues of the Joram server.
     * @return the initial queues of the Joram server.
     */
    public List<String> getQueues() {
        return this.queues;
    }

    /**
     * Sets the initial topics of the Joram server.
     * @param topics the list of the name of the topics.
     */
    public void setTopics(final List<String> topics) {
        this.topics = topics;
    }

    /**
     * Gets the initial topics of the Joram server.
     * @return the initial topics of the Joram server.
     */
    public List<String> getTopics() {
        return this.topics;
    }

    /**
     * Gets the resource adapter instance.
     * @return resource adapter instance.
     */
    public ResourceAdapter getResourceAdapter() {
        return this.joramAdapter;
    }


    /**
     * Sets the hostname to use.
     * @param host the host to use.
     */
    public void setHostname(final String host) {
        this.host = host;
    }


    /**
     * Sets the port number to use.
     * @param port the given port.
     */
    public void setPort(final int port) {
        this.port = port;
    }

    /**
     * @return the work manager used.
     */
    public EZBWorkManagerComponent getWorkManager() {
        return this.workManager;
    }

    /**
     * Sets the work manager instance.
     * @param workManager the given instance
     */
    public void setWorkManager(final EZBWorkManagerComponent workManager) {
        this.workManager = workManager;
    }

    /**
     * @return transaction component.
     */
    public TMComponent getTransactionComponent() {
        return this.transactionComponent;
    }

    /**
     * Sets the transaction component.
     * @param transactionComponent the given transaction component.
     */
    public void setTransactionComponent(final TMComponent transactionComponent) {
        this.transactionComponent = transactionComponent;
    }

    /**
     * Update the activation config properties if required.
     * @param activationConfigProperties the properties that will be set on the activation spec object
     */
    public void updateActivationConfigProperties(final Map<String, String> activationConfigProperties) {
        // For joram, nothing to update

    }

}
