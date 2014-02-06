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
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.remote.JMXConnectionNotification;

/**
 * @author hhildebrand
 * 
 */
public class Groo implements GrooMBean, NotificationEmitter {
    private class ConnectionListener implements NotificationListener {
        private boolean enabled = false;

        public synchronized void disable() {
            enabled = false;
        }

        public synchronized void enable() {
            enabled = true;
        }

        public synchronized boolean enabled() {
            return enabled;
        }

        @Override
        public void handleNotification(Notification notification,
                                       Object handback) {
            if (enabled()) {
                handleJMXConnectionNotification(notification, handback);
            }
        }
    }

    private static enum State {
        SHUTTING_DOWN, STARTED, STARTING, STOPPED;
    }

    public final static ObjectName                     MBSDelegateObjectName;
    private static final String[]                      jmxConnectionNotificationTypes = {
            JMXConnectionNotification.OPENED, JMXConnectionNotification.CLOSED,
            JMXConnectionNotification.FAILED,
            JMXConnectionNotification.NOTIFS_LOST                                    };
    private static final MBeanNotificationInfo         jmxConnectionNotificationInfo  = new MBeanNotificationInfo(
                                                                                                                  jmxConnectionNotificationTypes,
                                                                                                                  JMXConnectionNotification.class.getName(),
                                                                                                                  "Notifications relating to the underlying "
                                                                                                                          + "JMX Remote Connection.");
    private static final Logger                        log                            = Logger.getLogger(Groo.class.getCanonicalName());

    static {
        try {
            MBSDelegateObjectName = new ObjectName(
                                                   "JMImplementation:type=MBeanServerDelegate");
        } catch (MalformedObjectNameException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    private final ConcurrentMap<ObjectName, NodeMBean> children                       = new ConcurrentHashMap<>();
    private final MBeanServerConnectionFactory         connectionFactory;
    private final AtomicReference<ConnectionListener>  connectionListener             = new AtomicReference<>();
    private final String                               description;
    private final NotificationBroadcasterSupport       emitter;
    private final NotificationListener                 mbsNotifHandler;
    private final Node                                 node;
    private final AtomicLong                           sequenceNumber                 = new AtomicLong();
    private final ObjectName                           sourcePattern;
    private final QueryExp                             sourceQuery;
    private final AtomicReference<State>               state                          = new AtomicReference<>();

    public Groo(Node node, MBeanServerConnectionFactory sourceConnection,
                ObjectName sourcePattern, QueryExp sourceQuery,
                String description) {
        this.node = node;
        mbsNotifHandler = new NotificationListener() {
            @Override
            public void handleNotification(Notification notification,
                                           Object handback) {
                handleMBeanServerNotification(notification, handback);
            }
        };
        this.sourcePattern = sourcePattern;
        this.sourceQuery = sourceQuery;
        this.description = description;
        connectionFactory = sourceConnection;
        emitter = new NotificationBroadcasterSupport() {
            @Override
            protected void handleNotification(NotificationListener listener,
                                              Notification notif,
                                              Object handback) {
                handleNotification(listener, notif, handback);
            }
        };
    }

    @Override
    public void addNotificationListener(NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws java.lang.IllegalArgumentException {
        emitter.addNotificationListener(listener, filter, handback);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getDescription()
     */
    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        return new MBeanNotificationInfo[] { jmxConnectionNotificationInfo };
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getPattern()
     */
    @Override
    public ObjectName getPattern() {
        return sourcePattern;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#getQuery()
     */
    @Override
    public QueryExp getQuery() {
        return sourceQuery;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#isActive()
     */
    @Override
    public boolean isActive() {
        return state.get().equals(State.STARTED);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener)
                                                                         throws ListenerNotFoundException {
        emitter.removeNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws ListenerNotFoundException {
        emitter.removeNotificationListener(listener, filter, handback);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#start()
     */
    @Override
    public void start() throws IOException {
        if (!state.get().equals(State.STOPPED)) {
            throw new IllegalStateException("Can't start when state is: "
                                            + state);
        }

        state.set(State.STARTING);
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("start %s", state));
        }
        try {
            connectionFactory.getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                                                 mbsNotifHandler,
                                                                                 null,
                                                                                 null);
            enableConnectionNotifications();
        } catch (IOException io) {
            state.set(State.STOPPED);
            log.log(Level.FINE,
                    String.format("operation %s, Unexpected exception ",
                                  "start"), io);
            log.log(Level.FINEST, "start", io);
            throw io;
        } catch (Exception x) {
            log.log(Level.FINE,
                    String.format("operation %s, Unexpected exception", "start"),
                    x);
            log.log(Level.FINEST, "start", x);
            state.set(State.STOPPED);
            throw new IOException("failed to start: " + x, x);
        } catch (Error e) {
            state.set(State.STOPPED);
            if (log.isLoggable(Level.FINE)) {
                log.log(Level.FINE, "start: failed to start", e);
            }
            throw e;
        }

        try {
            for (ObjectName name : connectionFactory.getMBeanServerConnection().queryNames(getPattern(),
                                                                                           getQuery())) {
                addChild(name);
            }
        } catch (Throwable t) {
            try {
                state.set(State.SHUTTING_DOWN);
                if (log.isLoggable(Level.FINEST)) {
                    log.finest(String.format("start: Failed to start: %s",
                                             state));
                }
                cleanup(false);
                throw t;
            } catch (IOException x) {
                throw x;
            } catch (Error e) {
                throw e;
            } catch (Throwable x) {
                final IOException io = new IOException("Failed to start: " + x,
                                                       x);
                throw io;
            } finally {
                state.set(State.STOPPED);
                if (log.isLoggable(Level.FINE)) {
                    log.fine(String.format("start: Not started: %s", state));
                }
            }
        }
        // Everything OK.
        state.set(State.STARTED);
        if (log.isLoggable(Level.FINEST)) {
            log.finest(String.format("start %s", state));
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.GrooMBean#stop()
     */
    @Override
    public void stop() throws IOException {
        stop(false);
    }

    private void addChild(ObjectName sourceName) {
        if (state.get().equals(State.STOPPED)
            || state.get().equals(State.SHUTTING_DOWN)) {
            return;
        }
        if (!children.containsKey(sourceName)) {
            MBeanServerConnectionWrapper child = new MBeanServerConnectionWrapper(
                                                                                  connectionFactory,
                                                                                  sourceName);
            children.put(sourceName, child);
            node.addChild(child);
        }
    }

    private void cleanup(boolean connectionDown) {
        try {
            try {
                if (!connectionDown) {
                    connectionFactory.getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                                            mbsNotifHandler,
                                                                                            null,
                                                                                            null);
                }
            } catch (Exception x) {
                log.fine("cleanup: Unexpected exception: " + x);
                log.log(Level.FINEST, "cleanup", x);
            }
            try {
                disableConnectionNotifications();
            } catch (Exception x) {
                log.fine("cleanup: Unexpected exception while handling " + null
                         + ": " + x);
                log.log(Level.FINEST, "cleanup", x);
            }
            children.clear();
        } catch (Exception x) {
            log.fine("cleanup: Unexpected exception while handling " + null
                     + ": " + x);
            log.log(Level.FINEST, "cleanup", x);
        }
    }

    private void disableConnectionNotifications() throws IOException {
        try {
            final ConnectionListener l = connectionListener.get();
            if (l == null) {
                return;
            }
            connectionListener.set(null);
            l.disable();
            connectionFactory.removeConnectionNotificationListener(l, null,
                                                                   null);
        } catch (ListenerNotFoundException x) {
            // Not logged
        }
    }

    private void enableConnectionNotifications() throws IOException {
        final ConnectionListener l = new ConnectionListener();
        connectionFactory.addConnectionNotificationListener(l, null, null);
        connectionListener.set(l);
        connectionListener.get().enable();
    }

    private void handleJMXConnectionNotification(Notification n, Object handback) {
        final String nt = n.getType();
        try {
            synchronized (this) {
                if (!state.get().equals(State.STARTED)) {
                    return;
                }
                if (JMXConnectionNotification.OPENED.equals(nt)
                    || JMXConnectionNotification.NOTIFS_LOST.equals(nt)) {
                    update();
                } else if (JMXConnectionNotification.CLOSED.equals(nt)) {
                    stopIfClosed();
                } else if (JMXConnectionNotification.FAILED.equals(nt)) {
                    stop(true);
                }
            }
        } catch (Exception x) {
            log.log(Level.FINE,
                    String.format("operation %s, Unexpected exception while handling %s",
                                  nt, null), x);
            log.log(Level.FINEST, nt, x);
        }
        final String connectionId = ((JMXConnectionNotification) n).getConnectionId();
        final JMXConnectionNotification newn = new JMXConnectionNotification(
                                                                             nt,
                                                                             this,
                                                                             connectionId,
                                                                             sequenceNumber.incrementAndGet(),
                                                                             n.getMessage(),
                                                                             n.getUserData());
        emitter.sendNotification(newn);
    }

    private void handleMBeanServerNotification(Notification notification,
                                               Object handback) {
        if (notification instanceof MBeanServerNotification) {
            final MBeanServerNotification n = (MBeanServerNotification) notification;
            final String type = notification.getType();
            final ObjectName sourceName = n.getMBeanName();
            if (MBeanServerNotification.REGISTRATION_NOTIFICATION.equals(type)) {
                addChild(sourceName);
            } else if (MBeanServerNotification.UNREGISTRATION_NOTIFICATION.equals(type)) {
                if (children.containsKey(sourceName)) {
                    removeChild(sourceName);
                }
            }
        }
    }

    private void removeChild(ObjectName sourceName) {
        if (state.get().equals(State.STOPPED)
            || state.get().equals(State.SHUTTING_DOWN)) {
            return;
        }

        NodeMBean child = children.remove(sourceName);
        if (child != null) {
            node.removeChild(child);
        }
    }

    private void stop(boolean connectionDown) throws IOException {
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
            cleanup(connectionDown);
        } finally {
            state.set(State.STOPPED);
            if (log.isLoggable(Level.FINEST)) {
                log.finest(String.format("stop: %s", state));
            }
        }
    }

    private void stopIfClosed() throws IOException {
        if (!state.get().equals(State.STARTED)) {
            return;
        }
        try {
            connectionFactory.getMBeanServerConnection().getDefaultDomain();
            return;
        } catch (IOException x) {
            //  failed...
        } catch (Exception x) {
            //  failed...
        }
        stop(true);
    }

    private void update() throws IOException {
        if (!state.get().equals(State.STARTED)) {
            if (log.isLoggable(Level.FINE)) {
                log.fine("update, Groo " + state);
            }
            return;
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("update, Groo " + state);
        }
        final Set<ObjectName> names = children.keySet();
        final Set<?> found = new HashSet<Object>(
                                                 connectionFactory.getMBeanServerConnection().queryNames(getPattern(),
                                                                                                         getQuery()));
        for (ObjectName name : names) {
            if (found.remove(name)) {
                addChild(name);
            } else {
                removeChild(name);
            }
        }
        for (Object name : found) {
            addChild((ObjectName) name);
        }
        if (log.isLoggable(Level.FINE)) {
            log.fine("update, Groo updated");
        }
    }
}
