/**
 * EasyBeans
 * Copyright (C) 2008 Bull S.A.S.
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
 * $Id: BeanNamingInfo.java 5733 2011-02-21 12:54:34Z benoitf $
 * --------------------------------------------------------------------------
 */

package org.ow2.easybeans.naming;

import org.ow2.easybeans.api.bean.info.EZBBeanNamingInfo;

/**
 * Naming information for a bean.
 * @author S. Ali Tokmen
 */
public class BeanNamingInfo implements EZBBeanNamingInfo {

    /**
     * The bean name.
     */
    private String name;

    /**
     * The module name.
     */
    private String moduleName;

    /**
     * Name of the bean class.
     */
    private String beanClassName;

    /**
     * Name of the interface.
     */
    private String interfaceName;

    /**
     * Local, Remote or null.
     */
    private String mode;

    /**
     * The MappedName attribute, may be null.
     */
    private String mappedName;

    /**
     * Java EE application name (if bean is in an EAR).
     */
    private String javaEEApplicationName;

    /**
     * True if bean has only one interface view.
     */
    private boolean isSingleInterface = true;

    /**
     * Default Constructor.
     */
    public BeanNamingInfo() {
    }

    /**
     * @param s String to check.
     * @return null if s is null or "", s otherwise.
     */
    private String checkEmpty(final String s) {
        if (s == null || "".equals(s)) {
            return null;
        }
        return s;
    }

    /**
     * @return Java EE application name (if bean is in an EAR).
     */
    public String getJavaEEApplicationName() {
        return this.javaEEApplicationName;
    }

    /**
     * @return Name of the bean class.
     */
    public String getBeanClassName() {
        return this.beanClassName;
    }

    /**
     * @return Name of the interface.
     */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /**
     * @return Local, Remote or null.
     */
    public String getMode() {
        return this.mode;
    }

    /**
     * @return The MappedName attribute, may be null.
     */
    public String getMappedName() {
        return this.mappedName;
    }

    /**
     * @return the name of the bean.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the bean name.
     * @param name the given name
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * @return the name of the module.
     */
    public String getModuleName() {
        return this.moduleName;
    }

    /**
     * Sets the module name.
     * @param moduleName the given module name
     */
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }


    /**
     * Sets the bean class name.
     * @param beanClassName the classname of the bean
     */
    public void setBeanClassName(final String beanClassName) {
        this.beanClassName =  checkEmpty(beanClassName);
        if (beanClassName != null) {
            this.beanClassName = beanClassName.replace('/', '.');
        }
    }

    /**
     * Sets the name of the interface.
     * @param interfaceName the given interface name
     */
    public void setInterfaceName(final String interfaceName) {
        this.interfaceName = checkEmpty(interfaceName);
        if (interfaceName != null) {
            this.interfaceName = interfaceName.replace('/', '.');
        }
    }

    /**
     * Sets the mode of this bean.
     * @param mode the given mode
     */
    public void setMode(final String mode) {
        this.mode = checkEmpty(mode);
    }

    /**
     * Sets the mapped name used by this bean.
     * @param mappedName the given mappedName
     */
    public void setMappedName(final String mappedName) {
        this.mappedName = checkEmpty(mappedName);
    }

    /**
     * Sets the java EE application name of this bean.
     * @param javaEEApplicationName the given EE application name
     */
    public void setJavaEEApplicationName(final String javaEEApplicationName) {
        this.javaEEApplicationName = checkEmpty(javaEEApplicationName);
    }

    /**
     * @return true if there is only one single interface for the bean
     */
    public boolean isSingleInterface() {
        return this.isSingleInterface;
    }

    /**
     * Defines single interface mode.
     * @param isSingleInterface true if there is only one single interface for the bean
     */
    public void setSingleInterface(final boolean isSingleInterface) {
        this.isSingleInterface = isSingleInterface;
    }
}
