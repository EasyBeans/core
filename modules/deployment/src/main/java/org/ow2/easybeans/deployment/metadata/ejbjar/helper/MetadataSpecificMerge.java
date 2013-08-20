/**
 * EasyBeans
 * Copyright (C) 2008-2009 Bull S.A.S.
 * Copyright 2013 Peergreen S.A.S.
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
 * $Id: MetadataSpecificMerge.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.deployment.metadata.ejbjar.helper;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ow2.easybeans.deployment.metadata.ejbjar.EasyBeansEjbJarClassMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.EjbJarArchiveMetadata;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.AbsSpecificBean;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EJB;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.EasyBeansDD;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.MessageDrivenBean;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.PortComponentRef;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.ServiceRef;
import org.ow2.easybeans.deployment.metadata.ejbjar.xml.Session;
import org.ow2.util.ee.metadata.common.api.struct.IJaxwsWebServiceRef;
import org.ow2.util.ee.metadata.common.api.view.ICommonView;
import org.ow2.util.ee.metadata.common.api.xml.struct.IPortComponentRef;
import org.ow2.util.ee.metadata.ejbjar.api.IEjbJarMetadata;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;
import org.ow2.util.pool.api.IPoolConfiguration;
import org.ow2.util.scan.api.metadata.IClassMetadata;
import org.ow2.util.scan.api.metadata.IFieldMetadata;
import org.ow2.util.scan.api.metadata.IMethodMetadata;

/**
 * Merge specific XML data into the metadata model.
 * @author Florent BENOIT
 */
public final class MetadataSpecificMerge {

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(MetadataSpecificMerge.class);

    /**
     * ejb metadata.
     */
    private final IEjbJarMetadata ejbMetadata;

    /**
     * Helper class, no public constructor.
     * @param ejbMetadata the metadata corresponding to an EJB-JAR file.
     */
    private MetadataSpecificMerge(final IEjbJarMetadata ejbMetadata) {
        this.ejbMetadata = ejbMetadata;
    }

    /**
     * Takes struct of metadata and adds/set information on class/methods
     * metadata.
     * @param ejbMetadata the metadata corresponding to an EJB-JAR file.
     */
    public static void merge(final IEjbJarMetadata ejbMetadata) {
        new MetadataSpecificMerge(ejbMetadata).resolve();
    }

    /**
     * Do all merging operations.
     */
    private void resolve() {
        EjbJarArchiveMetadata ejbJarView = this.ejbMetadata.as(EjbJarArchiveMetadata.class);
        EasyBeansDD easybeansDD = ejbJarView.getEasyBeansDD();
        if (easybeansDD != null) {
            logger.debug("There is a specific Deployment Descriptor object, performing the merge of the metadata.");
            // Analyze EJBs
            EJB ejb = easybeansDD.getEJB();
            if (ejb != null) {

                // get session beans
                List<Session> sessionList = ejb.getSessions();
                if (sessionList != null) {
                    for (Session session : sessionList) {
                        // get ejb-name
                        String ejbName = session.getEjbName();
                        IClassMetadata classAnnotationMetadata = this.ejbMetadata.getEjbJarClassMetadataForEjbName(ejbName);
                        EasyBeansEjbJarClassMetadata easyBeansClassView = classAnnotationMetadata.as(EasyBeansEjbJarClassMetadata.class);
                        // class is found, then apply all customization.
                        applySessionBean(session, easyBeansClassView);
                    }
                }

                // get MDB
                List<MessageDrivenBean> mdbList = ejb.getMessageDrivenBeans();
                if (mdbList != null) {
                    for (MessageDrivenBean mdb : mdbList) {
                        // get ejb-name
                        String ejbName = mdb.getEjbName();
                        IClassMetadata classAnnotationMetadata = this.ejbMetadata.getEjbJarClassMetadataForEjbName(ejbName);
                        EasyBeansEjbJarClassMetadata easyBeansClassView = classAnnotationMetadata.as(EasyBeansEjbJarClassMetadata.class);

                        // class is found, then apply all customization.
                        applyMessageDrivenBean(mdb, easyBeansClassView);
                    }
                }
            }
        }
    }

    /**
     * Apply rules for a session bean.
     * @param sessionbean the struct (XML DD)
     * @param classAnnotationMetadata (Annotation metadata)
     */
    private void applySessionBean(final Session sessionbean, final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        applyCommonBean(sessionbean, classAnnotationMetadata);

        // apply context-root
        classAnnotationMetadata.setWebServiceContextRoot(sessionbean.getContextRoot());

        // apply endpoint-address
        classAnnotationMetadata.setWebServiceEndpointAddress(sessionbean.getEndpointAddress());

        // apply realm-name
        classAnnotationMetadata.setWebServiceRealmName(sessionbean.getRealmName());

        // apply auth-method
        classAnnotationMetadata.setWebServiceAuthMethod(sessionbean.getAuthMethod());

        // apply transport-guarantee
        classAnnotationMetadata.setWebServiceTransportGuarantee(sessionbean.getTransportGuarantee());

        // apply http-methods
        classAnnotationMetadata.setWebServiceHttpMethods(sessionbean.getHttpMethods());
    }

    /**
     * Apply rules for an MDB.
     * @param mdb the struct (XML DD)
     * @param classAnnotationMetadata (Annotation metadata)
     */
    private void applyMessageDrivenBean(final MessageDrivenBean mdb, final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        applyCommonBean(mdb, classAnnotationMetadata);
    }

    /**
     * Apply rules for a common bean.
     * @param bean the struct (XML DD)
     * @param classAnnotationMetadata (Annotation metadata)
     */
    private void applyCommonBean(final AbsSpecificBean bean, final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {
        // Pool configured ? apply it
        IPoolConfiguration poolConfiguration = bean.getPoolConfiguration();
        Object clusterConfiguration = bean.getCluster();
        if (poolConfiguration != null) {
            classAnnotationMetadata.setPoolConfiguration(poolConfiguration);
        }
        if (clusterConfiguration != null) {
            classAnnotationMetadata.setCluster(clusterConfiguration);
        }

        // service-ref
        List<ServiceRef> references = bean.getServiceRefs();
        if ((references != null) && (!references.isEmpty())) {
            applyServiceRefs(classAnnotationMetadata, references);
        }
    }

    /**
     * Apply rules for merging service-ref.
     * @param classAnnotationMetadata Annotation metadatas
     * @param references list of web services references
     */
    private void applyServiceRefs(final EasyBeansEjbJarClassMetadata classAnnotationMetadata,
                                  final List<ServiceRef> references) {

        Map<String, IJaxwsWebServiceRef> namedReferences;
        namedReferences = createMappedAnnotationsReferences(classAnnotationMetadata);

        // Iterates on each reference
        for (ServiceRef reference : references) {
            String name = reference.getName();

            IJaxwsWebServiceRef ref = namedReferences.get(name);

            // Found a matching reference name
            if (ref != null) {

                // Update WSDL location
                ref.setWsdlLocation(reference.getWsdlLocation());

                // Update port component refs
                List<PortComponentRef> easyBeansPortCompentRefs = reference.getPortComponentRefs();
                if (easyBeansPortCompentRefs != null) {
                    for (PortComponentRef easyBeansPortComponentRef : easyBeansPortCompentRefs) {
                        String serviceEndpointInterface = easyBeansPortComponentRef.getServiceEndpointInterface();

                        // matching port component ref ?
                        IPortComponentRef portComponentRef = ref.getPortComponentRef(serviceEndpointInterface);
                        if (portComponentRef == null) {
                            // no matching found, create a new one
                            portComponentRef = new org.ow2.util.ee.metadata.common.impl.xml.struct.PortComponentRef();
                            portComponentRef.setServiceEndpointInterface(serviceEndpointInterface);
                            List<IPortComponentRef> existingList = ref.getPortComponentRefs();
                            // add
                            existingList.add(portComponentRef);
                            // set the new list
                            ref.setPortComponentRefs(existingList);
                        }
                        // Set the properties
                        portComponentRef.setStubProperties(easyBeansPortComponentRef.getProperties());
                    }
                }
            } else {
                logger.info("@WebServiceRef({0}) was not found in the class '{1}'"
                            + ", unused overriding information.",
                            name,
                            classAnnotationMetadata.getClassMetadata().getClassName());
            }

        }
    }

    /**
     * Build a Map of all IJaxwsWebServiceRef contained in this Class.
     * @param classAnnotationMetadata the metadata to traverse
     * @return A Map of all the WebServiceRef available in this class
     *         (class/fields/methods)
     */
    private Map<String, IJaxwsWebServiceRef> createMappedAnnotationsReferences(
            final EasyBeansEjbJarClassMetadata classAnnotationMetadata) {

        // Init a default map
        Map<String, IJaxwsWebServiceRef> references;
        references = new HashMap<String, IJaxwsWebServiceRef>();

        // Class level annotations (must have a name)
        ICommonView classView = classAnnotationMetadata.getClassMetadata().as(ICommonView.class);

        IJaxwsWebServiceRef ref = classView.getJaxwsWebServiceRef();
        if (ref != null) {
            references.put(ref.getName(), ref);
        }

        List<IJaxwsWebServiceRef> refs = classView.getJaxwsWebServiceRefs();
        if ((refs != null) && (!refs.isEmpty())) {
            for (IJaxwsWebServiceRef classLevelReference : refs) {
                references.put(classLevelReference.getName(), classLevelReference);
            }
        }

        // Field level annotations (may have a name, if not set, use field name)
        Collection<IFieldMetadata> fields = classAnnotationMetadata.getClassMetadata().getFieldMetadataCollection();
        if ((fields != null) && (!fields.isEmpty())) {
            for (IFieldMetadata field : fields) {
                ICommonView fieldView = field.as(ICommonView.class);
                IJaxwsWebServiceRef fieldReference = fieldView.getJaxwsWebServiceRef();
                if (fieldReference != null) {
                    String refName = fieldReference.getName();
                    // Is there a usable name ?
                    if (refName == null) {
                        // If not, use the field name
                        refName = field.getJField().getName();
                    }
                    references.put(refName, fieldReference);
                }
            }
        }

        // Method level annotations (may have a name, if not set, use property method name)
        Collection<IMethodMetadata> methods = classAnnotationMetadata.getClassMetadata().getMethodMetadataCollection();
        if ((methods != null) && (!methods.isEmpty())) {
            for (IMethodMetadata methodMetadata : methods) {
                ICommonView methodView = methodMetadata.as(ICommonView.class);
                IJaxwsWebServiceRef methodReference = methodView.getJaxwsWebServiceRef();
                if (methodReference != null) {
                    String refName = methodReference.getName();
                    // Is there a usable name ?
                    if (refName == null) {
                        // If not, use the method's property name
                        String methodName = methodMetadata.getJMethod().getName();
                        methodName = methodName.substring("set".length());
                        String first = methodName.substring(0, 1).toUpperCase();
                        refName = first.concat(methodName.substring(1));
                    }
                    references.put(refName, methodReference);
                }
            }
        }

        // Return the filled map
        return references;
    }

}
