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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
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

    private final static Logger      log     = Logger.getLogger(NetworkBuilder.class.getCanonicalName());
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
        Node parent = new Node(filter.getSourcePattern(),
                               filter.getSourceQuery());
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
                log.log(Level.SEVERE,
                        String.format("Unable to register new parent: %s",
                                      nodeName), e);
            }
        }

        try {
            groo.addParent(parent);
        } catch (IOException e) {
            log.log(Level.INFO,
                    String.format(String.format("Error adding parent: %s",
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
            log.log(Level.SEVERE,
                    String.format("error in creating parent node name from: %s.  parent properties: %s",
                                  sourceName, parentProperties), e);
            return null;
        }
    }
}
