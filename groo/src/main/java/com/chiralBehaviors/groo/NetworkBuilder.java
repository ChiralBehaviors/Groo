/** 
 * (C) Copyright 2014 Chiral Behaviors, All Rights Reserved
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

package com.chiralBehaviors.groo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.management.QueryExp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhildebrand
 * 
 */
public class NetworkBuilder {

    private final static Logger      log     = LoggerFactory.getLogger(NetworkBuilder.class);
    private final RegistrationFilter filter;
    private final Groo               groo;
    private final Set<ObjectName>    managed = new CopyOnWriteArraySet<>();
    private final String[]           parentProperties;

    /**
     * @param groo
     * @param networkPattern
     * @param networkQuery
     * @param childProperty
     */
    public NetworkBuilder(Groo groo, ObjectName networkPattern,
                          QueryExp networkQuery, String[] parentProperties) {
        this.groo = groo;
        filter = new RegistrationFilter(networkPattern, networkQuery);
        this.parentProperties = parentProperties;
    }

    /**
     * @param sourceName
     */
    public void addParent(final ObjectName sourceName) {
        ObjectName nodeName = getParentName(sourceName);
        if (nodeName == null) {
            return;
        }
        Node parent = new Node(getChildSearchName(sourceName), null);
        if (!managed.add(nodeName)) {
            log.info(String.format("Already tracked %s on: %s", sourceName,
                                   this));
            return;
        }
        log.info(String.format("Adding %s on: %s", sourceName, this));

        MBeanServer mbs = groo.getMbs();
        if (mbs != null) {
            try {
                mbs.registerMBean(parent, nodeName);
            } catch (InstanceAlreadyExistsException
                    | MBeanRegistrationException | NotCompliantMBeanException e) {
                log.error(String.format("Unable to register new parent: %s",
                                        nodeName), e);
            }
        } else {
            log.info(String.format("No MBeanServer for groo, unable to register: %s",
                                   nodeName));
        }

        try {
            groo.addParent(parent);
        } catch (IOException e) {
            log.info(String.format(String.format("Error adding parent: %s",
                                                 nodeName)), e);
            return;
        }
    }

    /**
     * @return the filter
     */
    public RegistrationFilter getFilter() {
        return filter;
    }

    /**
     * @return the managed
     */
    public Set<ObjectName> getManaged() {
        return managed;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "NetworkBuilder [" + filter + ","
               + Arrays.toString(parentProperties) + "]";
    }

    private ObjectName getParentName(ObjectName sourceName) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        for (String property : parentProperties) {
            properties.put(property, sourceName.getKeyProperty(property));
        }
        try {
            return new ObjectName(sourceName.getDomain(), properties);
        } catch (MalformedObjectNameException e) {
            log.error(String.format("error in creating parent node name from: %s.  parent properties: %s",
                                    sourceName, parentProperties), e);
            return null;
        }
    }

    private ObjectName getChildSearchName(ObjectName sourceName) {
        Hashtable<String, String> properties = new Hashtable<String, String>();
        for (String property : parentProperties) {
            properties.put(property, sourceName.getKeyProperty(property));
        }
        for (Map.Entry<String, String> entry : filter.getSourcePattern().getKeyPropertyList().entrySet()) {
            if (!properties.containsKey(entry.getKey())) {
                properties.put(entry.getKey(), entry.getValue());
            }
        }
        try {
            return new ObjectName(sourceName.getDomain(), properties);
        } catch (MalformedObjectNameException e) {
            log.error(String.format("error in creating parent node name from: %s.  parent properties: %s",
                                    sourceName, parentProperties), e);
            return null;
        }
    }
}
