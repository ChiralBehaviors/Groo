package com.hellblazer.groo;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.Notification;
import javax.management.NotificationBroadcasterSupport;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectionNotification;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

/**
 * A basic {@link MBeanServerConnectionFactory} that wraps a
 * {@link JMXConnector}. This implementation does not support transparent
 * reconnection.
 * 
 * @since Java DMK 5.1
 **/
public class BasicMBeanServerConnectionFactory implements
        MBeanServerConnectionFactory {

    public static MBeanServerConnectionFactory newInstance(JMXConnector c) {
        return newInstance(c, null);
    }

    public static MBeanServerConnectionFactory newInstance(JMXConnector c,
                                                           Subject subject) {
        return new BasicMBeanServerConnectionFactory(c, subject);
    }

    public static MBeanServerConnectionFactory newInstance(JMXServiceURL url)
                                                                             throws IOException {
        return newInstance(url, null, null);
    }

    public static MBeanServerConnectionFactory newInstance(JMXServiceURL url,
                                                           Map<String, ?> map)
                                                                              throws IOException {
        return newInstance(url, map, null);

    }

    public static MBeanServerConnectionFactory newInstance(JMXServiceURL url,
                                                           Map<String, ?> map,
                                                           Subject subject)
                                                                           throws IOException {
        return newInstance(JMXConnectorFactory.connect(url, map), subject);
    }

    private final AtomicReference<MBeanServerConnection> connection = new AtomicReference<>();
    private final JMXConnector                           connector;
    private final NotificationBroadcasterSupport         emitter;
    private final AtomicBoolean                          failed     = new AtomicBoolean();
    private final NotificationListener                   listener;
    private final Subject                                subject;

    public BasicMBeanServerConnectionFactory(JMXConnector connector,
                                             Subject delegationSubject) {
        this.connector = connector;
        subject = delegationSubject;
        connection.set(null);
        ;
        emitter = new NotificationBroadcasterSupport();
        listener = new NotificationListener() {
            @Override
            public void handleNotification(Notification n, Object handback) {
                handleConnectionNotification(n, handback);
            }
        };
        failed.set(false);
        if (connector != null) {
            this.connector.addConnectionNotificationListener(listener, null,
                                                             connector);
        }
    }

    // MBeanServerConnectionFactory
    //
    @Override
    public void addConnectionNotificationListener(NotificationListener listener,
                                                  NotificationFilter filter,
                                                  Object handback) {
        emitter.addNotificationListener(listener, filter, handback);
    }

    @Override
    public String getConnectionId() throws IOException {
        if (failed.get()) {
            throw new IOException("connection already failed");
        }
        return getJMXConnector().getConnectionId();
    }

    public final Subject getDelegationSubject() {
        return subject;
    }

    public JMXConnector getJMXConnector() {
        return connector;
    }

    @Override
    public synchronized MBeanServerConnection getMBeanServerConnection()
                                                                        throws IOException {
        if (failed.get()) {
            throw new IOException("connection already failed");
        }
        connection.compareAndSet(null,
                                 connector.getMBeanServerConnection(subject));
        return connection.get();
    }

    @Override
    public void removeConnectionNotificationListener(NotificationListener listener)
                                                                                   throws ListenerNotFoundException {
        emitter.removeNotificationListener(listener);
    }

    @Override
    public void removeConnectionNotificationListener(NotificationListener l,
                                                     NotificationFilter f,
                                                     Object handback)
                                                                     throws ListenerNotFoundException {
        emitter.removeNotificationListener(l, f, handback);
    }

    private void handleConnectionNotification(Notification n, Object handback) {

        try {
            if (JMXConnectionNotification.FAILED.equals(n.getType())) {
                synchronized (this) {
                    if (!failed.get() && handback == getJMXConnector()) {
                        failed.set(true);
                    }
                }
            }
        } catch (Exception x) {
        } finally {
            emitter.sendNotification(n);
        }
    }

}
