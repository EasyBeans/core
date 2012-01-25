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
 * $Id: AskingClassLoader.java 5567 2010-09-14 13:33:05Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.component.smartclient.client;

import static org.ow2.easybeans.component.smartclient.api.ProtocolConstants.PROTOCOL_VERSION;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ow2.easybeans.component.smartclient.api.Message;
import org.ow2.easybeans.component.smartclient.api.ProtocolConstants;
import org.ow2.easybeans.component.smartclient.message.ClassAnswer;
import org.ow2.easybeans.component.smartclient.message.ClassNotFound;
import org.ow2.easybeans.component.smartclient.message.ClassRequest;
import org.ow2.easybeans.component.smartclient.message.ProviderURLAnswer;
import org.ow2.easybeans.component.smartclient.message.ProviderURLRequest;
import org.ow2.easybeans.component.smartclient.message.ResourceAnswer;
import org.ow2.easybeans.component.smartclient.message.ResourceRequest;

/**
 * ClassLoader that is used and that ask the EasyBeans remote server.
 * @author Florent Benoit
 */
public class AskingClassLoader extends URLClassLoader {

    /**
     * Use the JDK logger (to avoid any dependency).
     */
    private static Logger logger = Logger.getLogger(AskingClassLoader.class.getName());

    /**
     * Default buffer size.
     */
    private static final int DEFAULT_BUFFER_SIZE = 1024;

    /**
     * Socket adress used to connect.
     */
    private InetSocketAddress socketAddress = null;

    /**
     * Number of classes downloaded.
     */
    private static int nbClasses = 0;

    /**
     * Number of resources downloaded.
     */
    private static int nbResources = 0;

    /**
     * Number of bytes downloaded.
     */
    private static long nbBytes = 0;

    /**
     * All the time it took to ask server.
     */
    private static long timeToDownload = 0;

    /**
     * Directory used to store temporary resources that are downloaded.
     */
    private File tmpDirectory = null;


    /**
     * Creates a new classloader by using an empty URL.
     * @param host the remote host to connect.
     * @param portNumber the port number for the protocol.
     */
    public AskingClassLoader(final String host, final int portNumber) {
        this(host, portNumber, Thread.currentThread().getContextClassLoader(), new URL[0]);
    }


    /**
     * Creates a new classloader by using an empty URL.
     * @param host the remote host to connect.
     * @param portNumber the port number for the protocol.
     * @param urls the given URLs for this classloader
     */
    public AskingClassLoader(final String host, final int portNumber, final URL[] urls) {
        this(host, portNumber, Thread.currentThread().getContextClassLoader(), urls);
    }

    /**
     * Creates a new classloader by using an empty URL.
     * @param host the remote host to connect.
     * @param portNumber the port number for the protocol.
     * @param parentClassLoader the parent classloader
     * @param urls the given URLs for this classloader
     */
    public AskingClassLoader(final String host, final int portNumber, final ClassLoader parentClassLoader, final URL[] urls) {
        super(urls, parentClassLoader);

        // Create directory
        this.tmpDirectory = new File(System.getProperty("java.io.tmpdir") + File.separator + "easybeans-smart-"
                + System.getProperty("user.name") + "-" + cleanup(host) + "_" + portNumber);
        if (!this.tmpDirectory.exists()) {
            this.tmpDirectory.mkdir();
        }

        // Should be deleted when we shuthdown the JVM
        this.tmpDirectory.deleteOnExit();

        // Setup socket address
        this.socketAddress = new InetSocketAddress(host, portNumber);

        // Add hook for shutdown
        try {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(tmpDirectory));
        } catch (IllegalStateException e) {
            logger.log(Level.FINE, "Cannot add a new hook", e);
        }

    }

    /**
     * Gets a channel to communicate with the server.
     * @return a socket channel.
     */
    private SocketChannel getChannel() {
        SocketChannel channel = null;

        // open
        try {
            channel = SocketChannel.open();
        } catch (IOException e) {
            cleanChannel(channel);
            throw new IllegalStateException("Cannot open a channel", e);
        }

        // Connect
        try {
            channel.connect(this.socketAddress);
        } catch (IOException e) {
            cleanChannel(channel);
            throw new IllegalStateException("Cannot connect the channel", e);
        }

        return channel;
    }

    /**
     * Cleanup the channel if there was a failure.
     * @param channel the channel to cleanup.
     */
    private void cleanChannel(final SocketChannel channel) {
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                logger.log(Level.FINE, "Cannot close the given channel", e);
            }
        }
    }

    /**
     * Sends the given message on the given channel.
     * @param message the message to send
     * @param channel the channel used to send the message.
     * @return the bytebuffer containing the answer (to analyze)
     */
    public ByteBuffer sendRequest(final Message message, final SocketChannel channel) {
        // Send request
        try {
            channel.write(message.getMessage());
        } catch (IOException e) {
            cleanChannel(channel);
            throw new IllegalStateException("Cannot send the given message '" + message + "'.", e);
        }

        // Read response
        ByteBuffer buffer = ByteBuffer.allocateDirect(DEFAULT_BUFFER_SIZE);

        ByteBuffer completeBuffer = null;
        try {
            int length = 0;
            boolean finished = false;
            while (!finished && (channel.read(buffer)) != -1) {
                // can read header
                if (buffer.position() >= Message.HEADER_SIZE) {
                    // Got length, create buffer
                    if (completeBuffer == null) {
                        length = buffer.getInt(2);
                        // Size + default buffer size so the copy from current
                        // buffer work all the time
                        completeBuffer = ByteBuffer.allocate(Message.HEADER_SIZE + length + DEFAULT_BUFFER_SIZE);

                    }
                }
                // Append all read data into completeBuffer
                buffer.flip();
                completeBuffer.put(buffer);

                // clear for next time
                buffer.clear();

                if (completeBuffer.position() >= Message.HEADER_SIZE + length) {
                    completeBuffer.limit(Message.HEADER_SIZE + length);
                    // Skip Header, got OpCode, now create function
                    completeBuffer.position(Message.HEADER_SIZE);
                    finished = true;
                    break;
                }
            }
        } catch (Exception e) {
            cleanChannel(channel);
            throw new IllegalStateException("Cannot read the answer from the server.", e);
        }

        return completeBuffer;

    }

    /**
     * Finds and loads the class with the specified name from the URL search
     * path.<br>
     * If the super classloader doesn't find the class, it ask the remote server
     * to download the class
     * @param name the name of the class
     * @return the resulting class
     * @exception ClassNotFoundException if the class could not be found
     */
    @Override
    protected synchronized Class<?> findClass(final String name) throws ClassNotFoundException {
        // search super classloader
        Class<?> clazz = null;

        try {
            return super.findClass(name);
        } catch (ClassNotFoundException cnfe) {
            SocketChannel channel = null;
            try {
                long tStart = System.currentTimeMillis();
                // Get channel
                channel = getChannel();
                ByteBuffer answerBuffer = sendRequest(new ClassRequest(name), channel);

                // Gets opCode
                byte opCode = getOpCode(answerBuffer, channel);

                // stats
                timeToDownload = timeToDownload + (System.currentTimeMillis() - tStart);

                // Switch :
                switch (opCode) {
                case ProtocolConstants.CLASS_ANSWER:
                    ClassAnswer classAnswer = new ClassAnswer(answerBuffer);
                    try {
                        clazz = loadClass(name, classAnswer.getByteCode());
                    } catch (IOException e) {
                        throw new ClassNotFoundException("Cannot find the class", e);
                    }
                    nbClasses++;
                    nbBytes = nbBytes + classAnswer.getByteCode().length;
                    // display statistics (use sysout)
                    if (Boolean.getBoolean("smart.debug.verbose")) {
                        System.out.println("Downloaded class '" + name + "'.");
                    }
                    break;
                case ProtocolConstants.CLASS_NOT_FOUND:
                    ClassNotFound classNotFound = new ClassNotFound(answerBuffer);
                    throw new ClassNotFoundException("The class '" + classNotFound.getName()
                            + "' was not found on the remote side");
                default:
                    throw new ClassNotFoundException("Invalid opCode '" + opCode + "' received");
                }
            } finally {
                // cleanup
                cleanChannel(channel);
            }
        }

        return clazz;

    }

    /**
     * Ask and return the remote PROVIDER_URL in order to connect with RMI.
     * @return a string with the PROVIDER_URL value.
     */
    public String getProviderURL() {
        String providerURL = null;
        SocketChannel channel = null;
        try {
            long tStart = System.currentTimeMillis();
            // Get channel
            channel = getChannel();
            ByteBuffer answerBuffer = sendRequest(new ProviderURLRequest(), channel);

            // Gets opCode
            byte opCode = getOpCode(answerBuffer, channel);

            // stats
            timeToDownload = timeToDownload + (System.currentTimeMillis() - tStart);

            // Switch :
            switch (opCode) {
            case ProtocolConstants.PROVIDER_URL_ANSWER:
                ProviderURLAnswer providerURLAnswer = new ProviderURLAnswer(answerBuffer);
                providerURL = providerURLAnswer.getProviderURL();
                break;
            default:
                throw new IllegalStateException("Invalid opCode '" + opCode + "' received");
            }
        } finally {
            // cleanup
            cleanChannel(channel);
        }
        return providerURL;
    }

    /**
     * Gets the operation code from the current buffer.
     * @param buffer the buffer to analyze.
     * @param channel the channel which is use for the exchange.
     * @return the operation code.
     */
    private byte getOpCode(final ByteBuffer buffer, final SocketChannel channel) {
        if (buffer == null) {
            throw new IllegalStateException("Empty buffer received");
        }

        // Check if it is a protocol that we manage
        byte version = buffer.get(0);
        if (version != PROTOCOL_VERSION) {
            cleanChannel(channel);
            throw new IllegalStateException("Invalid protocol version : waiting '" + PROTOCOL_VERSION + "', got '" + version
                    + "'.");
        }

        // Get operation asked by client
        byte opCode = buffer.get(1);
        // Length
        int length = buffer.getInt(2);
        if (length < 0) {
            cleanChannel(channel);
            throw new IllegalStateException("Invalid length for client '" + length + "'.");
        }
        return opCode;
    }

    /**
     * Finds the resource with the specified name on the URL search path. <br>
     * If resource is not found locally, search on the remote side.
     * @param name the name of the resource
     * @return a <code>URL</code> for the resource, or <code>null</code> if
     *         the resource could not be found.
     */
    @Override
    public synchronized URL findResource(final String name) {
        URL url = null;
        url = super.findResource(name);

        if (url != null) {
            return url;
        }

        if (name.startsWith("META-INF")) {
            return null;
        }

        SocketChannel channel = null;
        try {
            long tStart = System.currentTimeMillis();

            // Get channel
            channel = getChannel();
            ByteBuffer answerBuffer = sendRequest(new ResourceRequest(name), channel);

            // Gets opCode
            byte opCode = getOpCode(answerBuffer, channel);

            // stats
            timeToDownload = timeToDownload + (System.currentTimeMillis() - tStart);

            // Switch :
            switch (opCode) {
            case ProtocolConstants.RESOURCE_ANSWER:
                ResourceAnswer resourceAnswer = new ResourceAnswer(answerBuffer);
                String resourceName = resourceAnswer.getResourceName();
                byte[] bytes = resourceAnswer.getBytes();

                nbResources++;
                nbBytes = nbBytes + resourceAnswer.getBytes().length;

                // convert / into File.separator
                String[] tokens = resourceName.split("/");
                StringBuilder sb = new StringBuilder();
                for (String token : tokens) {
                    if (sb.length() > 0) {
                        sb.append(File.separator);
                    }
                    sb.append(token);
                }

                // Create parent dir if does not exist
                File urlFile = new File(this.tmpDirectory, sb.toString());
                if (!urlFile.getParentFile().exists()) {
                    urlFile.getParentFile().mkdir();
                }

                // dump stream
                FileOutputStream fos = new FileOutputStream(urlFile);
                fos.write(bytes);
                fos.close();
                url = urlFile.toURI().toURL();
                break;
            case ProtocolConstants.RESOURCE_NOT_FOUND:
                url = null;
                break;
            default:
                throw new IllegalStateException("Invalid opCode '" + opCode + "' received");
            }
        } catch (Throwable e) {
            logger.log(Level.SEVERE, "Cannot handle : findResource '" + name + "'", e);
        } finally {
            // cleanup
            cleanChannel(channel);
        }

        return url;
    }

    /**
     * Defines a class by loading the bytecode for the given class name.
     * @param className the name of the class to define
     * @param bytecode the bytecode of the class
     * @return the class that was defined
     * @throws IOException if the class cannot be defined.
     */

    private Class<?> loadClass(final String className, final byte[] bytecode) throws IOException {

        // override classDefine (as it is protected) and define the class.
        Class<?> clazz = null;
        try {
            ClassLoader loader = this;
            java.lang.reflect.Method method = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class,
                    int.class, int.class);

            // protected method invocaton
            method.setAccessible(true);
            try {
                clazz = (Class<?>) method.invoke(loader, className, bytecode, Integer.valueOf(0), Integer
                        .valueOf(bytecode.length));
            } finally {
                method.setAccessible(false);
            }
        } catch (Exception e) {
            IOException ioe = new IOException("Cannot define class with name '" + className + "'.");
            ioe.initCause(e);
            throw ioe;
        }
        return clazz;
    }

    /**
     * Remove any extra character from a hostname.
     * @param host the given string to cleanup
     * @return the string that has been cleanup
     */
    protected String cleanup(final String host) {
        return host.replace(":", "-").replace(".", "-");
    }


    /**
     * Hook that is called when process is going to shutdown.
     * @author Florent Benoit
     */
    static class ShutdownHook extends Thread {

        /**
         * Constructor for the given directory.
         * @param tmpDirectory the directory to remove at the end
         */
        public ShutdownHook(final File tmpDirectory) {
            this.tmpDirectory = tmpDirectory;
        }

        /**
         * Directory used to store temporary resources that are downloaded.
         */
        private File tmpDirectory = null;

        /**
         * Display stats.
         */
        @Override
        public void run() {
           delete(tmpDirectory);
            // display statistics (use sysout)
            if (Boolean.getBoolean("smart.debug")) {
                System.out.println("Downloaded '" + nbClasses + "' classes, '" + nbResources + "' resources for a total of '"
                        + nbBytes + "' bytes and it took '" + timeToDownload + "' ms.");
            }

        }

        /**
          * Delete the given directory.
          * @param path the path to remove
          */
        private static boolean delete(final File path) {
            if (path.exists() ) {
                File[] children = path.listFiles();
                if (children != null) {
                    for (File child : children) {
                        if (child.isDirectory()) {
                            delete(child);
                        } else {
                            child.delete();
                        }
                    }
                }
            }
            return path.delete();
        }
    }
}
