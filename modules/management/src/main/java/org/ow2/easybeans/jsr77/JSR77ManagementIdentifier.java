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
 * $Id: JSR77ManagementIdentifier.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.jsr77;

import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.ow2.easybeans.api.jmx.EZBManagementIdentifier;
import org.ow2.util.log.Log;
import org.ow2.util.log.LogFactory;

/**
 * Specialized {@link EZBManagementIdentifier} for JSR77 MBeans.
 * @author Guillaume Sauthier
 * @author Florent BENOIT
 * @param <T> Managed Type
 */
public abstract class JSR77ManagementIdentifier<T> implements EZBManagementIdentifier<T> {

    /**
     * Default domain name.
     */
    private static final String DEFAULT_DOMAIN_NAME = "";

    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(JSR77ManagementIdentifier.class);

    /**
     * Domain name.
     */
    private String domainName = null;

    /**
     * Server name.
     */
    private String serverName = null;

    /**
     * Empty default constructor.
     */
    protected JSR77ManagementIdentifier() {
    }

    /**
     * @param name base ObjectName
     * @return Returns a String that contains "inherited" properties from
     *         parent's ObjectName
     */
    @SuppressWarnings("unchecked")
    protected static String getInheritedPropertiesAsString(final ObjectName name) {
        Hashtable<String, Object> table = (Hashtable<String, Object>) name.getKeyPropertyList().clone();
        // we remove some attributes from the ObjectName
        table.remove("j2eeType");
        table.remove("type");
        table.remove("subtype");
        table.remove("name");
        StringBuffer sb = new StringBuffer();
        Set<Map.Entry<String, Object>>  entries = table.entrySet();
        for (Map.Entry<String, Object> entry : entries) {
            String key = entry.getKey();
            String value = entry.getValue().toString();
            sb.append(key + "=" + value + ",");
        }

        if (sb.length() > 1) {
            // remove the trailing comma
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    /**
     * @param parentObjectName Parent ObjectName.
     * @return Returns the couple j2eetype=name of the parent ObjectName.
     */
    protected static String getParentNameProperty(final String parentObjectName) {
        ObjectName on = null;
        try {
            on = ObjectName.getInstance(parentObjectName);
        } catch (MalformedObjectNameException e) {
            logger.error("Cannot get objectname on {0}", parentObjectName, e);
            return "";
        } catch (NullPointerException e) {
            logger.error("Cannot get objectname on {0}", parentObjectName, e);
            return "";
        }

        String type = on.getKeyProperty("j2eeType");
        String name = on.getKeyProperty("name");

        return type + "=" + name;
    }

    /**
     * @return the couple property=value with value which is the value stored in
     *         the objectName
     * @param objectName given ObjectName.
     * @param property the name of the property
     */
    protected static String getPropertyNameValue(final String objectName, final String property) {
        ObjectName on = null;
        try {
            on = ObjectName.getInstance(objectName);
        } catch (MalformedObjectNameException e) {
            logger.error("Cannot get objectname on {0}", objectName, e);
            return "";
        } catch (NullPointerException e) {
            logger.error("Cannot get objectname on {0}", objectName, e);
            return "";
        }

        String value = on.getKeyProperty(property);
        return property + "=" + value;
    }

    /**
     * @return Returns the JMX Domain name of the MBean.
     */
    public String getDomain() {
        if (this.domainName == null) {
            return DEFAULT_DOMAIN_NAME;
        }
        return this.domainName;
    }

    /**
     * Sets the domain for this identifier.
     * @param domainName the JMX Domain name of the MBean.
     */
    public void setDomain(final String domainName) {
        this.domainName = domainName;
    }

    /**
     * @return the JMX Server name of the MBean.
     */
    public String getServerName() {
        return this.serverName;
    }

    /**
     * Sets the Server name for this identifier.
     * @param serverName the JMX Server name of this MBean.
     */
    public void setServerName(final String serverName) {
        this.serverName = serverName;
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeName() {
        return "j2eeType";
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeProperty() {
        return getTypeName() + "=" + getTypeValue();
    }

    /**
     * @return the logger
     */
    public static final Log getLogger() {
        return logger;
    }

    /**
     * @return the String for J2EEServer.
     */
    protected String getJ2EEServerString() {
        return "J2EEServer=" + this.serverName;
    }

    /**
     * For a given URL, make a shorter name.
     * @param url the given URL
     * @return a short name for the given URL
     */
    public String shorterName(final URL url) {

        String urlExternalForm = url.toExternalForm();

        // if it ends by a /, remove this last one
        if (urlExternalForm.charAt(urlExternalForm.length() - 1) == '/') {
            urlExternalForm = urlExternalForm.substring(0, urlExternalForm.length() - 1);
        }

        // get string after the last / (all URL have this, no need to test if
        // there is a / character)
        int slashPos = urlExternalForm.lastIndexOf('/');
        String shortName = urlExternalForm.substring(slashPos + 1, urlExternalForm.length());
        int dotPos = shortName.lastIndexOf('.');
        if (dotPos != -1) {
            shortName = shortName.substring(0, dotPos);
        }
        return shortName;
    }
}
