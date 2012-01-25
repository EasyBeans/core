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
 * $Id: SmartClientEndPointComponent.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.server;

import static org.ow2.easybeans.component.smartclient.api.ProtocolConstants.PROTOCOL_VERSION;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.easybeans.component.api.EZBComponentException;
import org.ow2.easybeans.component.itf.EZBSmartComponent;
import org.ow2.easybeans.component.itf.RegistryComponent;
import org.ow2.easybeans.component.smartclient.api.Message;
import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;
import org.ow2.easybeans.component.smartclient.message.ChannelAttachment;
import org.ow2.easybeans.component.smartclient.message.ClassAnswer;
import org.ow2.easybeans.component.smartclient.message.ClassNotFound;
import org.ow2.easybeans.component.smartclient.message.ClassRequest;
import org.ow2.easybeans.component.smartclient.message.ProviderURLAnswer;
import org.ow2.easybeans.component.smartclient.message.ResourceAnswer;
import org.ow2.easybeans.component.smartclient.message.ResourceNotFound;
import org.ow2.easybeans.component.smartclient.message.ResourceRequest;

/**
 * This endpoint receives the request from clients, handle them and send an
 * answer.<br>
 * For example, it send the bytecode for a given class.
 * @author Florent Benoit
 */
public class SmartClientEndPointComponent implements EZBSmartComponent, Runnable {

    /**
     * Use the JDK logger (to avoid any dependency).
     */
    private static Logger logger = Logger.getLogger(SmartClientEndPointComponent.class.getName());


    /**
     * Maximum length of messages that we accept.
     */
    private static final int MAX_LENGTH_INCOMING_MSG = 500;

    /**
     * Default port number.
     */
    private static final int DEFAULT_PORT_NUMBER = 2503;

    /**
     * Buffer length.
     */
    private static final int BUF_APPEND = 1000;

    /**
     * Listening port number.
     */
    private int portNumber = DEFAULT_PORT_NUMBER;

    /**
     * Nio Selector.
     */
    private Selector selector = null;

    /**
     * The selection key of the server (accepting clients).
     */
    private SelectionKey serverkey = null;

    /**
     * Server socket channel (listening).
     */
    private ServerSocketChannel server = null;

    /**
     * Waiting ?
     */
    private boolean waitingSelector = true;

    /**
     * Link to the RMI component used to get the provider URL.
     */
    private RegistryComponent registryComponent = null;

    /**
     * The class loader to use for the resource lookup. If null, Thread's
     * context class loader is used.
     */
    private ClassLoader classLoader = null;

    /**
     * Init method.<br/> This method is called before the start method.
     * @throws EZBComponentException if the initialization has failed.
     */
    public void init() throws EZBComponentException {

        // Creates a new selector
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            throw new EZBComponentException("Cannot open a new selector.", e);
        }
        // Server socket
        try {
            this.server = ServerSocketChannel.open();
        } catch (IOException e) {
            throw new EZBComponentException("Cannot open a new server socket channel.", e);
        }

        // no blocking
        try {
            this.server.configureBlocking(false);
        } catch (IOException e) {
            throw new EZBComponentException("Cannot configure the server socket with non-blocking mode.", e);
        }
    }

    /**
     * Start method.<br/> This method is called after the init method.
     * @throws EZBComponentException if the start has failed.
     */
    public void start() throws EZBComponentException {

        // port number listener
        try {
            this.server.socket().bind(new java.net.InetSocketAddress(this.portNumber));
        } catch (IOException e) {
            throw new EZBComponentException("Cannot listen on the port number '" + this.portNumber
                    + "', maybe the port is already used.", e);
        }

        // registering
        try {
            this.serverkey = this.server.register(this.selector, SelectionKey.OP_ACCEPT);
        } catch (ClosedChannelException e) {
            throw new EZBComponentException("Cannot register the current selector as an accepting selector waiting clients.", e);
        }

        // now wait clients
        this.waitingSelector = true;

        // infinite loop
        new Thread(this).start();

        logger.info("SmartClient Endpoint listening on port '" +  this.portNumber + "'.");

    }

    /**
     * Stop method.<br/> This method is called when component needs to be
     * stopped.
     * @throws EZBComponentException if the stop is failing.
     */
    public void stop() throws EZBComponentException {
        // break infinite loop
        this.waitingSelector = false;
        this.selector.wakeup();
        try {
            this.server.socket().close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Unable to close the socket.", e);
        }
    }

    /**
     * Infinite loop (until the end of the component) that handle the selectors.
     */
    public void handleSelectors() {

        // infinite loop
        while (this.waitingSelector) {

            // wait new stuff
            int updatedKeys = 0;
            try {
                updatedKeys = this.selector.select();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Selector has been closed, stopping listener", e);
                this.waitingSelector = false;
            }

            // No update, then go ahead
            if (updatedKeys == 0) {
                continue;
            }

            // Get selected keys
            Set<SelectionKey> selectedKeys = this.selector.selectedKeys();

            for (Iterator<SelectionKey> itSelectedKeys = selectedKeys.iterator(); itSelectedKeys.hasNext();) {
                SelectionKey selectionKey = itSelectedKeys.next();
                itSelectedKeys.remove(); // remove it has it was handled

                // Server key ?
                if (selectionKey == this.serverkey) {
                    // New client ?
                    if (selectionKey.isAcceptable()) {
                        try {
                            handleAccept();
                        } catch (Exception e) {
                            // all exception (including runtime)
                            logger.log(Level.SEVERE, "Unable to accept a new connection.", e);
                        }
                    }
                } else if (selectionKey.isReadable()) {
                    // get request from client
                    try {
                        handleRead(selectionKey);
                    } catch (Exception e) {
                        // all exception (including runtime)
                        logger.log(Level.SEVERE, "Unable to read data from the client.", e);
                    }
                } else if (selectionKey.isWritable()) {
                    // answer to the client
                    try {
                        handleWrite(selectionKey);
                    } catch (Exception e) {
                        // all exception (including runtime)
                        logger.log(Level.SEVERE, "Unable to write data to the client.", e);
                    }
                }
            }
        }
    }

    /**
     * Handle a new client that is being connected.
     * @throws IOException if cannot accept the client
     */
    private void handleAccept() throws IOException {
        // new incoming connection
        SocketChannel client = this.server.accept();

        // Non blocking client
        client.configureBlocking(false);

        // Register client (with an empty channel attachment)
        client.register(this.selector, SelectionKey.OP_READ, new ChannelAttachment());
    }

    /**
     * Handle all read operations on channels.
     * @param selectionKey the selected key.
     * @throws IOException if cannot read from the channel.
     */
    private void handleRead(final SelectionKey selectionKey) throws IOException {
        // Get the client channel that has data to read
        SocketChannel client = (SocketChannel) selectionKey.channel();

        // current bytecode read
        ChannelAttachment channAttachment = (ChannelAttachment) selectionKey.attachment();
        ByteBuffer channBuffer = channAttachment.getByteBuffer();

        // Read again
        int bytesread = client.read(channBuffer);
        if (bytesread == -1) {
            // close (as the client has been disconnected)
            selectionKey.cancel();
            client.close();
        }

        // Client send data, analyze data

        // Got header ?
        if (channBuffer.position() >= Message.HEADER_SIZE) {

            // Yes, got header
            // Check if it is a protocol that we manage
            byte version = channBuffer.get(0);
            if (version != PROTOCOL_VERSION) {
                selectionKey.cancel();
                client.close();
                throw new IllegalStateException("Invalid protocol version : waiting '" + PROTOCOL_VERSION + "', got '" + version
                        + "'.");
            }

            // Get operation asked by client
            byte opCode = channBuffer.get(1);

            // Length
            int length = channBuffer.getInt(2);
            if (length < 0) {
                selectionKey.cancel();
                client.close();
                throw new IllegalStateException("Invalid length for client '" + length + "'.");
            }

            if (length > MAX_LENGTH_INCOMING_MSG) {
                selectionKey.cancel();
                client.close();
                throw new IllegalStateException("Length too big, max length = '" + MAX_LENGTH_INCOMING_MSG + "', current = '"
                        + length + "'.");
            }

            // Correct header and correct length ?
            if (channBuffer.position() >= Message.HEADER_SIZE + length) {
                // set the limit (specified in the length), else we have a
                // default buffer limit
                channBuffer.limit(Message.HEADER_SIZE + length);

                // duplicate this buffer
                ByteBuffer dataBuffer = channBuffer.duplicate();

                // skip header (already analyzed)
                dataBuffer.position(Message.HEADER_SIZE);

                // Switch on operations :
                try {
                    switch (opCode) {
                    case ProtocolConstants.CLASS_REQUEST:
                        handleReadClassRequest(selectionKey, dataBuffer);
                        break;
                    case ProtocolConstants.RESOURCE_REQUEST:
                        handleReadResourceRequest(selectionKey, dataBuffer);
                        break;
                    case ProtocolConstants.PROVIDER_URL_REQUEST:
                        handleReadProviderURLRequest(selectionKey, dataBuffer);
                        break;
                    default:
                        // nothing to do
                    }
                } catch (Exception e) {
                    // clean
                    selectionKey.cancel();
                    client.close();
                    throw new IllegalStateException("Cannot handle request with opCode '" + opCode + "'.", e);
                }
            }
        }

    }

    /**
     * Handle the client's request asking for a class.
     * @param selectionKey key for exchanging with the client.
     * @param dataBuffer the buffer that contains request.
     * @throws IOException if operation fails
     */
    private void handleReadClassRequest(final SelectionKey selectionKey, final ByteBuffer dataBuffer) throws IOException {
        // Build object (from input)
        ClassRequest classRequest = new ClassRequest(dataBuffer);
        String className = classRequest.getClassName();

        // Answer to the client (go in write mode)
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        String encodedClassName = className.replaceAll("\\.", "/").concat(".class");

        // Find the resource from the classloader
        InputStream inputStream = findClassLoader().getResourceAsStream(encodedClassName);

        if (inputStream == null) {
            ClassNotFound classNotFound = new ClassNotFound(className);
            selectionKey.attach(classNotFound.getMessage());
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Class '" + className + "' not found");
            }
            return;
        }
        byte[] bytecode = null;
        try {
            // Get bytecode of the class
            bytecode = readClass(inputStream);

        } finally {
            inputStream.close();
        }

        // Create answer object
        ClassAnswer classAnswer = new ClassAnswer(className, bytecode);

        // Attach the answer on the key
        selectionKey.attach(classAnswer.getMessage());

    }

    /**
     * Handle the client's request asking for a resource.
     * @param selectionKey key for exchanging with the client.
     * @param dataBuffer the buffer that contains request.
     * @throws IOException if operation fails
     */
    private void handleReadResourceRequest(final SelectionKey selectionKey, final ByteBuffer dataBuffer) throws IOException {

        // Build object (from input)
        ResourceRequest resourceRequest = new ResourceRequest(dataBuffer);
        String resourceName = resourceRequest.getResourceName();

        // Answer to the client
        selectionKey.interestOps(SelectionKey.OP_WRITE);

        // Find the resource from the classloader
        URL url = findClassLoader().getResource(resourceName);
        if (url == null) {
            ResourceNotFound resourceNotFound = new ResourceNotFound(resourceName);
            selectionKey.attach(resourceNotFound.getMessage());
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "Resource '" + resourceName + "' not found");
            }
            return;
        }
        InputStream inputStream = url.openStream();

        byte[] bytes = null;
        try {
            // Get bytecode of the class
            bytes = readClass(inputStream);

        } finally {
            inputStream.close();
        }

        // Create answer object
        ResourceAnswer resourceAnswer = new ResourceAnswer(resourceName, bytes);

        // Attach the answer on the key
        selectionKey.attach(resourceAnswer.getMessage());

    }

    /**
     * Handle the client's request asking for the default provider URL.
     * @param selectionKey key for exchanging with the client.
     * @param dataBuffer the buffer that contains request.
     * @throws IOException if operation fails
     */
    private void handleReadProviderURLRequest(final SelectionKey selectionKey, final ByteBuffer dataBuffer) throws IOException {

        // Answer to the client (go in write mode)
        selectionKey.interestOps(SelectionKey.OP_WRITE);

        String providerURL = this.registryComponent.getProviderURL();

        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "Provider URL asked by client : '" + providerURL + "'.");
        }

        // Create answer object
        ProviderURLAnswer providerURLAnswer = new ProviderURLAnswer(providerURL);

        // Attach the answer on the key
        selectionKey.attach(providerURLAnswer.getMessage());

    }

    /**
     * Handle all write operations on channels.
     * @param selectionKey the selected key.
     * @throws IOException if cannot write to the channel.
     */
    private void handleWrite(final SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();

        // Write the data that was attached on the selection key
        ByteBuffer buffer = (ByteBuffer) selectionKey.attachment();
        if (buffer.hasRemaining()) {
            channel.write(buffer);
        } else {
            // finished to write, close
            channel.close();
        }
    }

    /**
     * Gets the bytes from the given input stream.
     * @param is given input stream.
     * @return the array of bytes for the given input stream.
     * @throws IOException if class cannot be read.
     */
    private static byte[] readClass(final InputStream is) throws IOException {
        if (is == null) {
            throw new IOException("Given input stream is null");
        }
        byte[] b = new byte[is.available()];
        int len = 0;
        while (true) {
            int n = is.read(b, len, b.length - len);
            if (n == -1) {
                if (len < b.length) {
                    byte[] c = new byte[len];
                    System.arraycopy(b, 0, c, 0, len);
                    b = c;
                }
                return b;
            }
            len += n;
            if (len == b.length) {
                byte[] c = new byte[b.length + BUF_APPEND];
                System.arraycopy(b, 0, c, 0, len);
                b = c;
            }
        }
    }

    /**
     * Launch the thread looking at the selectors.
     */
    public void run() {
        handleSelectors();
    }

    /**
     * Sets the port number of the smart endpoint.
     * @param portNumber the port for listening
     */
    public void setPortNumber(final int portNumber) {
        this.portNumber = portNumber;
    }

    /**
     * Sets the registry component.
     * @param registryComponent the given component.
     */
    public void setRegistryComponent(final RegistryComponent registryComponent) {
        this.registryComponent = registryComponent;
    }

    /**
     * Gets the port number of the smart endpoint.
     * @return the port number
     */
    public int getPortNumber() {
        return this.portNumber;
    }

    /**
     * Sets the class loader to use for the resource lookup.
     * @param classLoader the class loader to use for the resource lookup.
     */
    public void setClassLoader(final ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    /**
     * The class loader to use for the resource lookup. If null, Thread's
     * context class loader is used.
     * @return the class loader to use for the resource lookup.
     */
    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    /**
     * The class loader to use for the resource lookup. If the user didn't set
     * any class loader, the thread's context class loader is returned.
     * @return the class loader to use for the resource lookup.
     */
    private ClassLoader findClassLoader() {
        if (this.classLoader != null) {
            return this.classLoader;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }


}
