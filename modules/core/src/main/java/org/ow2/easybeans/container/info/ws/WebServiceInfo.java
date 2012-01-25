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
 * $Id: WebServiceInfo.java 4832 2009-03-23 17:46:11Z sauthieg $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.container.info.ws;

import java.util.List;

import org.ow2.easybeans.api.bean.info.IWebServiceInfo;

import org.ow2.util.ee.metadata.common.api.xml.struct.IHandlerChains;

import javax.xml.namespace.QName;

/**
 * The WebServiceInfo is ...
 *
 * @author Guillaume Sauthier
 */
public class WebServiceInfo implements IWebServiceInfo {

    /**
     * Name of the port-component.
     */
    private String portComponentName;

    /**
     * Location of the WSDL document.
     */
    private String wsdlLocation;

    /**
     * Name of the service in the WSDL.
     */
    private QName serviceName;

    /**
     * Name of the port in the WSDL (may be null).
     */
    private QName portName;

    /**
     * FQN of the SEI interface (may be null for Provider).
     */
    private String serviceEndpointInterface;

    /**
     * Enable MTOM ?
     */
    private boolean mtomEnabled = false;

    /**
     * Protocol bunding URI to be used.
     */
    private String protocolBinding;

    /**
     * HandlerChains applicable to this port-component.
     */
    private IHandlerChains handlerChains;

    /**
     * WSDL publication directory.
     */
    private String wsdlPublicationDirectory;

    /**
     * Generated context-root name.
     */
    private String contextRoot;

    /**
     * Security realm name.
     */
    private String realmName;

    /**
     * Guarantee name to be used.
     */
    private String transportGuarantee;

    /**
     * authentication method to be used in the context.
     */
    private String authMethod;

    /**
     * List of secured HTTP methods.
     */
    private List<String> httpMethods;

    /**
     * Address for this endpoint.
     */
    private String endpointAddress;

    public void setPortComponentName(String portComponentName) {
        this.portComponentName = portComponentName;
    }

    public void setWsdlLocation(String wsdlLocation) {
        this.wsdlLocation = wsdlLocation;
    }

    public void setServiceName(QName serviceName) {
        this.serviceName = serviceName;
    }

    public void setPortName(QName portName) {
        this.portName = portName;
    }

    public void setServiceEndpointInterface(String serviceEndpointInterface) {
        this.serviceEndpointInterface = serviceEndpointInterface;
    }

    public void setMTOMEnabled(boolean mtomEnabled) {
        this.mtomEnabled = mtomEnabled;
    }

    public void setProtocolBinding(String protocolBinding) {
        this.protocolBinding = protocolBinding;
    }

    public void setHandlerChains(IHandlerChains handlerChains) {
        this.handlerChains = handlerChains;
    }

    public void setWsdlPublicationDirectory(String wsdlPublicationDirectory) {
        this.wsdlPublicationDirectory = wsdlPublicationDirectory;
    }

    /**
     * @return the WSDL location URL
     */
    public String getWsdlLocation() {
        return this.wsdlLocation;
    }

    /**
     * The port-component-name specifies a port component's
     * name.  This name is assigned by the module producer to name
     * the service implementation bean in the module's deployment
     * descriptor. The name must be unique among the port component
     * names defined in the same module.
     * <p/>
     * Mapped to @WebService.name (if not specified then its default
     * value as specified in the JSR-181), only if it is unique in the module.
     * If the above is not unique, then fully qualified name of the Bean
     * class is used to guarantee uniqueness.
     *
     * @return Port component's unique name
     */
    public String getPortComponentName() {
        return this.portComponentName;
    }

    /**
     * Defines the name space and local name part of the WSDL
     * service QName. This is required to be specified for
     * port components that are JAX-WS Provider implementations.
     *
     * @return <code>wsdl:service</code> QName
     */
    public QName getServiceName() {
        return this.serviceName;
    }

    /**
     * Defines the name space and local name part of the WSDL
     * port QName. This is not required to be specified for port
     * components that are JAX-WS Provider implementations.
     *
     * @return <code>wsdl:port</code> QName
     */
    public QName getPortName() {
        return this.portName;
    }

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
    public String getEndpointInterface() {
        return this.serviceEndpointInterface;
    }

    /**
     * Used to enable or disable SOAP MTOM/XOP mechanism for an
     * endpoint implementation.
     *
     * @return <code>true</code> if MTOM/XOP has to be enabled for this port-component
     */
    public boolean isMTOMEnabled() {
        return this.mtomEnabled;
    }

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
    public String getProtocolBinding() {
        return this.protocolBinding;
    }

    /**
     * Handlerchain can be defined such that the handlers in the
     * handlerchain operate,all ports of a service, on a specific
     * port or on a list of protocol-bindings. The choice of elements
     * service-name-pattern, port-name-pattern and protocol-bindings
     * are used to specify whether the handlers in handler-chain are
     * for a service, port or protocol binding. If none of these
     * choices are specified with the handler-chain element then the
     * handlers specified in the handler-chain will be applied on
     * everything.
     *
     * @return The handler chains descriptor (possibly null).
     */
    public IHandlerChains getHandlerChains() {
        return handlerChains;
    }

    /**
     * @return the publication directory
     */
    public String getWsdlPublicationDirectory() {
        return wsdlPublicationDirectory;
    }

    /**
     * @return the endpoint address
     */
    public String getEndpointAddress() {
        return endpointAddress;
    }

    /**
     * Set the endpoint address
     * @param endpointAddress part of the URL that is usable to access the endpoint.
     */
    public void setEndpointAddress(String endpointAddress) {
        this.endpointAddress = endpointAddress;
    }

    /**
     * @return the user specified context-root to be created for this EJB endpoint.
     */
    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    /**
     * @return the user specified real name to be used for the secured web context.
     */
    public String getRealmName() {
        return realmName;
    }

    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }

    /**
     * @return the user specified transport-guarantee to be used for the secured web context.
     */
    public String getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(String transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
    }

    /**
     * @return  the user specified auth-method to be used for the secured web context.
     */
    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    /**
     * @return the user specified list of http-methods to be used for the secured web context.
     */
    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public void setHttpMethods(List<String> httpMethods) {
        this.httpMethods = httpMethods;
    }
}
