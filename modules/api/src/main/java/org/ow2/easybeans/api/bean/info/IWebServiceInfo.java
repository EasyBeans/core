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
 * $Id: IWebServiceInfo.java 4832 2009-03-23 17:46:11Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.api.bean.info;

import java.util.List;

import javax.xml.namespace.QName;
import org.ow2.util.ee.metadata.common.api.xml.struct.IHandlerChains;

/**
 * The IWebServiceInfo contains descriptions information about the web
 * service exposure of an EJB.
 * This information is available at runtime.
 *
 * @author Guillaume Sauthier
 */
public interface IWebServiceInfo {

    /**
     * @return the WSDL location URL
     */
    String getWsdlLocation();

    /**
     * The port-component-name specifies a port component's
     * name.  This name is assigned by the module producer to name
     * the service implementation bean in the module's deployment
     * descriptor. The name must be unique among the port component
     * names defined in the same module.
     *
     * Mapped to @WebService.name (if not specified then its default
     * value as specified in the JSR-181), only if it is unique in the module.
     * If the above is not unique, then fully qualified name of the Bean
     * class is used to guarantee uniqueness.
     *
     * @return Port component's unique name
     */
    String getPortComponentName();

    /**
     * Defines the name space and local name part of the WSDL
     * service QName. This is required to be specified for
     * port components that are JAX-WS Provider implementations.

     * @return <code>wsdl:service</code> QName
     */
    QName getServiceName();

    /**
     * Defines the name space and local name part of the WSDL
     * port QName. This is not required to be specified for port
     * components that are JAX-WS Provider implementations.

     * @return <code>wsdl:port</code> QName
     */
    QName getPortName();

    /**
     * The service-endpoint-interface element contains the
     * fully-qualified name of the port component's Service Endpoint
     * Interface.
     * <br />
     * This may not be specified in case there is no Service
     * Enpoint Interface as is the case with directly using an
     * implementation class with the @WebService annotation.
     * <br />
     * When the port component is a {@link javax.xml.ws.Provider} implementation
     * this is not specified.
     *
     * @return the FQN of the service endpoint interface
     */
    String getEndpointInterface();

    /**
     * Used to enable or disable SOAP MTOM/XOP mechanism for an
     * endpoint implementation.

     * @return <code>true</code> if MTOM/XOP has to be enabled for this port-component
     */
    boolean isMTOMEnabled();

    /**
     * Used to specify the protocol binding used by the port-component.
     * If this element is not specified, then the default binding is
     * used (SOAP 1.1 over HTTP).
     * <br />
     * Unrecognized alias (starting with <code>##</code> may be returned).
     * Recognized aliases (<code>##SOAP11_HTTP</code>, <code>##SOAP11_HTTP_MTOM</code>,
     * <code>##SOAP12_HTTP</code>, <code>##SOAP12_HTTP_MTOM</code>,
     * <code>##XML_HTTP</code>) have been resolved.
     *
     * @return The URI representing the protocol binding to be used by this port-component
     */
    String getProtocolBinding();

    /**
     * HandlerChain can be defined such that the handlers in the
     * handlerchain operate,all ports of a service, on a specific
     * port or on a list of protocol-bindings. The choice of elements
     * service-name-pattern, port-name-pattern and protocol-bindings
     * are used to specify whether the handlers in handler-chain are
     * for a service, port or protocol binding. If none of these
     * choices are specified with the handler-chain element then the
     * handlers specified in the handler-chain will be applied on
     * everything.

     * @return The parsed HandlerChains from descriptor (possibliy null).
     */
    IHandlerChains getHandlerChains();

    /**
     * This optional attribute is used to store where the user wants the WSDL
     * of its endpoint published.
     * @return an absolute path to a directory (may not exists)
     */
    String getWsdlPublicationDirectory();

    /**
     * @return the endpoint address
     */
    String getEndpointAddress();

    /**
     * @return the user specified context-root to be created for this EJB endpoint.
     */
    String getContextRoot();

    /**
     * @return the user specified real name to be used for the secured web context.
     */
    String getRealmName();

    /**
     * @return the user specified transport-guarantee to be used for the secured web context.
     */
    String getTransportGuarantee();

    /**
     * @return  the user specified auth-method to be used for the secured web context.
     */
    String getAuthMethod();

    /**
     * @return the user specified list of http-methods to be used for the secured web context.
     */
    List<String> getHttpMethods();
}
