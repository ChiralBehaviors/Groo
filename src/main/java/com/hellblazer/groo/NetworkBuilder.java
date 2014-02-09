/** 
 * (C) Copyright 2013 Hal Hildebrand, All Rights Reserved
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *     
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.hellblazer.groo;

import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 * @author hhildebrand
 * 
 */
public class NetworkBuilder {

    private final static Logger                   log     = Logger.getLogger(NetworkBuilder.class.getCanonicalName());
    private final Map<String, String>             childSearchProperties;
    private final QueryExp                        childSearchQuery;
    private final RegistrationFilter              filter;
    private Groo                                  groo;
    private final ConcurrentMap<ObjectName, Node> managed = new ConcurrentHashMap<ObjectName, Node>();
    private final List<String>                    parentProperties;

    /**
     * @param groo
     * @param networkPattern
     * @param networkQuery
     * @param childProperty
     */
    public NetworkBuilder(ObjectName networkPattern, QueryExp networkQuery,
                          List<String> parentProperties,
                          Map<String, String> childSearchProperties,
                          QueryExp childSearchQuery) {
        filter = new RegistrationFilter(networkPattern, childSearchQuery);
        this.parentProperties = parentProperties;
        this.childSearchProperties = childSearchProperties;
        this.childSearchQuery = childSearchQuery;
    }

    /**
     * @param sourceName
     */
    public void addParent(final ObjectName sourceName) {
        ObjectName nodeName = getParentName(sourceName);
        if (nodeName == null) {
            return;
        }

        ObjectName searchName = getSearchName(sourceName);
        if (searchName == null) {
            return;
        }
        Node parent = new Node(searchName, childSearchQuery);
        if (managed.putIfAbsent(nodeName, parent) != null) {
            return;
        }
        try {
            groo.addParent(parent);
        } catch (IOException e) {
            log.log(Level.INFO,
                    String.format(String.format("Error adding parent: %s",
                                                nodeName)), e);
            return;
        }

        MBeanServer mbs = groo.getMbs();
        if (mbs != null) {
            try {
                mbs.registerMBean(parent, nodeName);
            } catch (InstanceAlreadyExistsException
                    | MBeanRegistrationException | NotCompliantMBeanException e) {
                log.log(Level.SEVERE,
                        String.format("Unable to register new parent: %s",
                                      nodeName), e);
            }
        }
    }

    /**
     * @return the filter
     */
    public RegistrationFilter getFilter() {
        return filter;
    }

    /**
     * @param groo
     *            the groo to set
     */
    public void setGroo(Groo groo) {
        this.groo = groo;
    }

    private ObjectName getParentName(ObjectName sourceName) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        for (String property : parentProperties) {
            properties.put(property, sourceName.getKeyProperty(property));
        }
        try {
            return new ObjectName(sourceName.getDomain(), properties);
        } catch (MalformedObjectNameException e) {
            log.log(Level.SEVERE,
                    String.format("error in creating parent node name from: %s.  parent properties: %s",
                                  sourceName, parentProperties), e);
            return null;
        }
    }

    private ObjectName getSearchName(ObjectName sourceName) {
        Hashtable<String, String> properties = new Hashtable<String, String>(
                                                                             sourceName.getKeyPropertyList());
        for (Map.Entry<String, String> entry : childSearchProperties.entrySet()) {
            properties.put(entry.getKey(), entry.getValue());
        }
        try {
            return new ObjectName(sourceName.getDomain(), properties);
        } catch (MalformedObjectNameException e) {
            log.log(Level.SEVERE,
                    String.format(String.format("error in creating search name from: %s.  Child search properties: %s",
                                                sourceName,
                                                childSearchProperties)), e);
            return null;
        }
    }
}
