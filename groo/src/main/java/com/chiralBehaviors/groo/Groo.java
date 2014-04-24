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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.remote.JMXConnectionNotification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhildebrand
 * 
 */
public class Groo implements GrooMBean, MBeanRegistration {

    private static final Logger                               log      = LoggerFactory.getLogger(Groo.class);

    private final AtomicBoolean                               active   = new AtomicBoolean(
                                                                                           true);
    private final ConcurrentMap<UUID, NetworkBuilder>         builders = new ConcurrentHashMap<>();
    private final ConcurrentMap<MbscFactory, List<NodeMBean>> children = new ConcurrentHashMap<>();
    private final String                                      description;
    private final ConcurrentMap<UUID, Node>                   filters  = new ConcurrentHashMap<>();
    private MBeanServer                                       mbs;
    private final Set<Node>                                   parents  = new CopyOnWriteArraySet<>();

    public Groo(String description) {
        this.description = description;
    }

    public void addBuilder(NetworkBuilder builder) throws IOException {
        log.info(String.format("Adding builder: %s on: %s", builder, this));
        builders.put(builder.getFilter().getHandback(), builder);
        for (MbscFactory factory : children.keySet()) {
            update(factory, builder);
            factory.registerBuilder(builder.getFilter());
        }
    }

    public void addConnection(MbscFactory factory)
                                                  throws InstanceNotFoundException,
                                                  IOException {
        log.info(String.format("Adding connection factory: %s for: %s",
                               factory, this));
        children.putIfAbsent(factory, new CopyOnWriteArrayList<NodeMBean>());
        factory.registerListeners();
        for (Node parent : parents) {
            update(factory, parent);
            factory.register(parent.getFilter());
        }
        for (NetworkBuilder builder : builders.values()) {
            update(factory, builder);
            factory.registerBuilder(builder.getFilter());
        }
    }

    public void addNetworkBuilder(ObjectName networkPattern,
                                  QueryExp networkQuery,
                                  String[] parentProperties) throws IOException {
        addBuilder(new NetworkBuilder(this, networkPattern, networkQuery,
                                      parentProperties));
    }

    public void addParent(Node parent) throws IOException {
        if (!parents.add(parent)) {
            return;
        }
        log.info(String.format("Adding parent: %s on: %s", parent, this));
        filters.put(parent.getFilter().getHandback(), parent);
        for (MbscFactory factory : children.keySet()) {
            update(factory, parent);
            factory.register(parent.getFilter());
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see com.chiralBehaviors.groo.GrooMXBean#getManagedNetworks()
     */
    @Override
    public String[] getManagedNetworks() {
        List<String> managed = new ArrayList<>();
        for (NetworkBuilder builder : builders.values()) {
            for (ObjectName name : builder.getManaged()) {
                managed.add(name.getCanonicalName());
            }
        }
        return managed.toArray(new String[] {});
    }

    /**
     * @return the mbs
     */
    public MBeanServer getMbs() {
        return mbs;
    }

    /* (non-Javadoc)
     * @see com.chiralBehaviors.groo.GrooMBean#getNetworkBuilderFilters()
     */
    @Override
    public String[] getNetworkBuilderFilters() {
        List<String> filters = new ArrayList<>();
        for (NetworkBuilder builder : builders.values()) {
            filters.add(builder.getFilter().getFilterString());
        }
        return filters.toArray(new String[] {});
    }

    public List<Node> getParents() {
        return new ArrayList<Node>(parents);
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    @Override
    public void postDeregister() {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    @Override
    public void postRegister(Boolean registrationDone) {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    @Override
    public void preDeregister() throws Exception {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name)
                                                                      throws Exception {
        mbs = server;
        return name;
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
            log.trace("cleanup: Unexpected exception while handling " + null
                      + ": " + x);
            log.trace("cleanup", x);
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Groo [active=" + active + ", " + description + "]";
    }

    private void addChild(Node parent, ObjectName childName, MbscFactory factory) {
        if (!active.get()) {
            return;
        }
        MbscNodeWrapper child = new MbscNodeWrapper(factory, childName);
        if (!parent.addChild(child)) {
            return;
        }
        log.info(String.format("Adding child: %s to parent: %s on: %s", child,
                               parent, factory));
        children.get(factory).add(child);
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
        NodeMBean n = null;
        for (NodeMBean child : children.get(factory)) {
            if (sourceName.equals(child.getName())) {
                n = child;
                break;
            }
        }
        if (n == null) {
            return;
        }
        children.get(factory).remove(n);
        for (Node parent : parents) {
            parent.removeChild(n);
        }
        return;
    }

    private void update(MbscFactory factory) throws IOException {
        log.info(String.format("Updating factory: %s", factory));
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
        if (log.isDebugEnabled()) {
            log.debug("update, Groo updated");
        }
    }

    /**
     * @param factory
     * @param builder
     * @throws IOException
     */
    private void update(MbscFactory factory, NetworkBuilder builder)
                                                                    throws IOException {
        RegistrationFilter filter = builder.getFilter();
        log.info(String.format("Querying for builder: %s on: %s for: %s",
                               builder, factory, this));
        for (ObjectName name : factory.getMBeanServerConnection().queryNames(filter.getSourcePattern(),
                                                                             filter.getSourceQuery())) {
            builder.addParent(name);
        }
    }

    /**
     * @param factory
     * @param parent
     * @throws IOException
     */
    private void update(MbscFactory factory, Node parent) throws IOException {
        RegistrationFilter filter = parent.getFilter();
        log.info(String.format("Querying for node: %s filter: %s on: %s for: %s",
                               parent, filter, factory, this));
        for (ObjectName name : factory.getMBeanServerConnection().queryNames(filter.getSourcePattern(),
                                                                             filter.getSourceQuery())) {
            addChild(parent, name, factory);
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
            log.debug(String.format("operation %s, Unexpected exception while handling %s",
                                    nt, null), x);
            log.debug(nt, x);
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

    /**
     * @param notification
     * @param handback
     */
    void handleNetworkBuilderNotification(MBeanServerNotification notification,
                                          UUID handback) {
        if (!MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(notification.getType())) {
            return;
        }
        NetworkBuilder builder = builders.get(handback);
        if (builder != null) {
            builder.addParent(notification.getMBeanName());
        }
    }
}
