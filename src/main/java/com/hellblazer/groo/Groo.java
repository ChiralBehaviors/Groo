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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectionNotification;

/**
 * @author hhildebrand
 * 
 */
public class Groo implements GrooMBean {

    private static final Logger                               log      = Logger.getLogger(Groo.class.getCanonicalName());

    private final ConcurrentMap<MbscFactory, List<NodeMBean>> children = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, Node>                   filters  = new ConcurrentHashMap<>();
    private final String                                      description;
    private final List<Node>                                  parents  = new CopyOnWriteArrayList<>();
    private final AtomicBoolean                               active   = new AtomicBoolean(
                                                                                           true);

    public Groo(String description) {
        this.description = description;
    }

    public void addConnection(MbscFactory factory)
                                                  throws InstanceNotFoundException,
                                                  IOException {
        children.putIfAbsent(factory, new CopyOnWriteArrayList<NodeMBean>());
        factory.registerListeners();
        for (Node parent : parents) {
            register(parent, factory);
            RegistrationFilter filter = parent.getFilter();
            for (ObjectName name : factory.getMBeanServerConnection().queryNames(filter.getSourcePattern(),
                                                                                 filter.getSourceQuery())) {
                addChild(parent, name, factory);
            }
        }
    }

    public void addParent(Node parent) throws IOException {
        parents.add(parent);
        for (MbscFactory factory : children.keySet()) {
            register(parent, factory);
        }
        RegistrationFilter filter = parent.getFilter();
        for (MbscFactory factory : children.keySet()) {
            for (ObjectName name : factory.getMBeanServerConnection().queryNames(filter.getSourcePattern(),
                                                                                 filter.getSourceQuery())) {
                addChild(parent, name, factory);
            }
        }
    }

    private void register(Node parent, MbscFactory factory) {
        UUID handback = UUID.randomUUID();
        filters.put(handback, parent);
        factory.register(parent.getFilter(), handback);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#stop()
     */
    @Override
    public void stop() throws IOException {
        if (!active.compareAndSet(true, false)) {
            return;
        }
        try {
            for (MbscFactory factory : children.keySet()) {
                cleanup(factory);
            }
            children.clear();
        } catch (Exception x) {
            log.fine("cleanup: Unexpected exception while handling " + null
                     + ": " + x);
            log.log(Level.FINEST, "cleanup", x);
        }
    }

    private void addChild(Node node, ObjectName sourceName, MbscFactory factory) {
        if (!active.get()) {
            return;
        }
        MbscNodeWrapper child = new MbscNodeWrapper(factory, sourceName);
        node.addChild(child);
    }

    private void cleanup(MbscFactory factory) {
        factory.deregisterListeners();
        for (Node parent : parents) {
            for (NodeMBean child : children.remove(factory)) {
                parent.removeChild(child);
            }
        }

    }

    private void removeChild(MbscFactory factory, ObjectName sourceName) {
        for (NodeMBean child : children.get(factory)) {
            if (sourceName.equals(child.getName())) {
                children.get(factory).remove(child);
                for (Node parent : parents) {
                    parent.removeChild(child);
                }
                return;
            }
        }
    }

    private void update(MbscFactory factory) throws IOException {
        for (Node parent : parents) {
            RegistrationFilter filter = parent.getFilter();
            final Set<ObjectName> found = factory.getMBeanServerConnection().queryNames(filter.getSourcePattern(),
                                                                                        filter.getSourceQuery());
            List<NodeMBean> list = children.get(factory);
            List<NodeMBean> removed = new ArrayList<>();
            for (NodeMBean child : list) {
                if (!found.contains(child.getName())) {
                    removed.add(child);
                }
            }
            for (ObjectName name : found) {
                addChild(parent, name, factory);
            }
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("update, Groo updated");
        }
    }

    void handleJMXConnectionNotification(Notification notification,
                                         MbscFactory factory) {
        if (!active.get()) {
            return;
        }
        final String nt = notification.getType();
        try {
            synchronized (this) {
                if (!active.get()) {
                    return;
                }
                if (JMXConnectionNotification.OPENED.equals(nt)
                    || JMXConnectionNotification.NOTIFS_LOST.equals(nt)) {
                    update(factory);
                } else if (JMXConnectionNotification.CLOSED.equals(nt)) {
                    cleanup(factory);
                } else if (JMXConnectionNotification.FAILED.equals(nt)) {
                    cleanup(factory);
                }
            }
        } catch (Exception x) {
            log.log(Level.FINE,
                    String.format("operation %s, Unexpected exception while handling %s",
                                  nt, null), x);
            log.log(Level.FINEST, nt, x);
        }
    }

    void handleMBeanServerNotification(MbscFactory factory,
                                       MBeanServerNotification notification,
                                       UUID handback) {
        if (!active.get()) {
            return;
        }
        final String type = notification.getType();
        final ObjectName sourceName = notification.getMBeanName();
        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            Node parent = filters.get(handback);
            if (parent != null) {
                addChild(parent, sourceName, factory);
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            if (children.containsKey(sourceName)) {
                removeChild(factory, sourceName);
            }
        }
    }
}
