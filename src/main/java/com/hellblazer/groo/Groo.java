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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.remote.JMXConnectionNotification;

/**
 * @author hhildebrand
 * 
 */
public class Groo implements GrooMBean {

    private static enum State {
        SHUTTING_DOWN, STARTED, STARTING, STOPPED;
    }

    private static final Logger                               log      = Logger.getLogger(Groo.class.getCanonicalName());

    private final ConcurrentMap<MbscFactory, List<NodeMBean>> children = new ConcurrentHashMap<>();
    private final ConcurrentMap<Node, RegistrationFilter>     filters  = new ConcurrentHashMap<>();
    private final String                                      description;
    private final List<Node>                                  parents  = new CopyOnWriteArrayList<>();
    private final AtomicReference<State>                      state    = new AtomicReference<>();

    public Groo(String description) {
        this.description = description;
    }

    public void addConnection(MbscFactory factory)
                                                  throws InstanceNotFoundException,
                                                  IOException {
        children.putIfAbsent(factory, new CopyOnWriteArrayList<NodeMBean>());
        factory.registerListeners();
        for (Node parent : parents) {
            for (ObjectName name : factory.getMBeanServerConnection().queryNames(parent.getSourcePattern(),
                                                                                 parent.getSourceQuery())) {
                addChild(parent, name, factory);
            }
        }
    }

    public void addParent(Node parent) {
        RegistrationFilter filter = new RegistrationFilter(
                                                           parent.getSourcePattern(),
                                                           parent.getSourceQuery());
        parents.add(parent);
        for (MbscFactory factory : children.keySet()) {
            factory.register(filter);
        }
        // TODO add existing children that match
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#isActive()
     */
    @Override
    public boolean isActive() {
        return state.get().equals(State.STARTED);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#stop()
     */
    @Override
    public void stop() throws IOException {
        if (state.get().equals(State.STOPPED)) {
            if (log.isLoggable(Level.FINEST)) {
                log.finest(String.format("stop, Already: %s ", state));
            }
            return;
        }
        if (!state.get().equals(State.STARTED)) {
            throw new IllegalStateException("Can't stop when state is: "
                                            + state);
        }
        state.set(State.SHUTTING_DOWN);
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("stop: %s", state));
        }
        try {
            cleanup();
        } finally {
            state.set(State.STOPPED);
            if (log.isLoggable(Level.FINEST)) {
                log.finest(String.format("stop: %s", state));
            }
        }
    }

    private void addChild(Node node, ObjectName sourceName, MbscFactory factory) {
        if (state.get().equals(State.STOPPED)
            || state.get().equals(State.SHUTTING_DOWN)) {
            return;
        }
        MbscNodeWrapper child = new MbscNodeWrapper(factory, sourceName);
        node.addChild(child);
    }

    private void cleanup() {
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

    private void cleanup(MbscFactory factory) {
        factory.deregisterListeners();
        for (Node parent : parents) {
            for (NodeMBean child : children.remove(factory)) {
                parent.removeChild(child);
            }
        }

    }

    void handleJMXConnectionNotification(Notification notification,
                                         MbscFactory factory) {

        final String nt = notification.getType();
        try {
            synchronized (this) {
                if (!state.get().equals(State.STARTED)) {
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
                                       MBeanServerNotification notification) {
        final String type = notification.getType();
        final ObjectName sourceName = notification.getMBeanName();
        if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
            for (Node node : parents) {
                if (isIncluded(sourceName, node.getSourcePattern(),
                               node.getSourceQuery(), wrap(factory))) {
                    addChild(node, sourceName, factory);
                }
            }
        } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
            if (children.containsKey(sourceName)) {
                removeChild(factory, sourceName);
            }
        }
    }

    private MBeanServer wrap(final MbscFactory factory) {
        return new MbscWrapper() {
            @Override
            protected MBeanServerConnection getMBeanServerConnection()
                                                                      throws IOException {
                return factory.getMBeanServerConnection();
            }
        };
    }

    private boolean isIncluded(ObjectName sourceName, ObjectName sourcePattern,
                               QueryExp sourceQuery, MBeanServer server) {
        try {
            if (sourcePattern != null && !sourcePattern.apply(sourceName)) {
                return false;
            }

            // We can't simly do: if (sourceQuery == null) return true;
            // we must verify that this CascadingAgent has the permissions
            // to access the proxied MBean...
            //
            return server.queryNames(sourceName, sourceQuery).size() == 1;
        } catch (Exception x) {
            log.log(Level.FINE, "operation include, Unexpected exception ", x);
            log.log(Level.FINEST, "include", x);
        }
        return false;
    }

    private void removeChild(MbscFactory factory, ObjectName sourceName) {
        if (state.get().equals(State.STOPPED)
            || state.get().equals(State.SHUTTING_DOWN)) {
            return;
        }

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
        if (!state.get().equals(State.STARTED)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("update, Groo " + state);
            }
            return;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("update, Groo " + state);
        }
        for (Node parent : parents) {
            final Set<ObjectName> found = factory.getMBeanServerConnection().queryNames(parent.getSourcePattern(),
                                                                                        parent.getSourceQuery());
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
}
