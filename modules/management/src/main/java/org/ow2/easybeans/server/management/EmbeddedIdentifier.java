/**
 *
 */
package org.ow2.easybeans.server.management;

import org.ow2.easybeans.jsr77.JSR77ManagementIdentifier;
import org.ow2.easybeans.server.Embedded;

/**
 * Generates an ObjectName for the {@link Embedded} component.
 * @author Guillaume Sauthier
 */
public class EmbeddedIdentifier extends JSR77ManagementIdentifier<Embedded> {

    /**
     * JMX MBean Type.
     */
    private static final String TYPE = "J2EEServer";

    /**
     * {@inheritDoc}
     */
    public String getAdditionnalProperties(final Embedded instance) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String getNamePropertyValue(final Embedded instance) {
        // If ServerName is already set, return this value
        if (getServerName() != null) {
            return getServerName();
        }
        return "EasyBeans_" + instance.getID();
    }

    /**
     * {@inheritDoc}
     */
    public String getTypeValue() {
        return TYPE;
    }

}
