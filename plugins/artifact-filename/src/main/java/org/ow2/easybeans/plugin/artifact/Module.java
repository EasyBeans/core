/**
 * EasyBeans
 * Copyright (C) 2007 Bull S.A.S.
 * Contact: easybeans@ow2.org
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
 * $Id: Module.java 5369 2010-02-24 14:58:19Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.plugin.artifact;

/**
 * Describe a Maven 2 artifact.
 * @author Florent Benoit
 */
public class Module {

    /**
     * Group Id of this module.
     */
    private String groupId;

    /**
     * Artifact id of this module.
     */
    private String artifactId;

    /**
     * Version of this module.
     * Version may be retrieved from the dependency management.
     */
    private String version;

    /**
     * Type of the archive.
     */
    private String type;

    /**
     * Classifier of the archive.
     */
    private String classifier;


    /**
     * @return group id
     */
    public String getGroupId() {
        return this.groupId;
    }


    /**
     * Sets group id.
     * @param groupId given value
     */
    public void setGroupId(final String groupId) {
        this.groupId = groupId;
    }


    /**
     * @return the artifact id
     */
    public String getArtifactId() {
        return this.artifactId;
    }


    /**
     * Sets artifact id.
     * @param artifactId given value
     */
    public void setArtifactId(final String artifactId) {
        this.artifactId = artifactId;
    }


    /**
     * @return the version.
     */

    public String getVersion() {
        return this.version;
    }


    /**
     * Sets the version.
     * @param version given value
     */
    public void setVersion(final String version) {
        this.version = version;
    }


    /**
     * @return the type
     */
    public String getType() {
        return this.type;
    }

    /**
     * Sets the type.
     * @param type the given value
     */
    public void setType(final String type) {
        this.type = type;
    }

    /**
     * @return the type
     */
    public String getClassifier() {
        return this.classifier;
    }

    /**
     * Sets the classifier.
     * @param classifier the given value
     */
    public void setClassifier(final String classifier) {
        this.classifier = classifier;
    }


    /**
     * @return the string representation of this module
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Module[groupId=");
        sb.append(this.groupId);
        sb.append(", artifactId=");
        sb.append(this.artifactId);
        sb.append(", type=");
        sb.append(this.type);
        sb.append(", version=");
        sb.append(this.version);
        sb.append("]");
        return sb.toString();
    }
}
