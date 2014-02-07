package com.hellblazer.groo;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerNotification;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;

abstract public class MbscFactory {
    public final static ObjectName         MBSDelegateObjectName;

    static {
        try {
            MBSDelegateObjectName = new ObjectName(
                                                   "JMImplementation:type=MBeanServerDelegate");
        } catch (MalformedObjectNameException e) {
            throw new UnsupportedOperationException(e.getMessage());
        }
    }

    private final Groo                     groo;
    protected final NotificationListener   connectListener;
    protected final NotificationListener   mbsListener;
    private final List<RegistrationFilter> filters = new CopyOnWriteArrayList<>();

    /**
     * @param groo
     */
    public MbscFactory(Groo groo) {
        this.groo = groo;
        connectListener = connectionListener();
        mbsListener = mbsListener();
    }

    public void deregisterListeners() {
        try {
            getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                  mbsListener);
        } catch (InstanceNotFoundException | ListenerNotFoundException
                | IOException e) {
            // ignored
        }
        deregisterConnectListener();
    }

    public void registerListeners() throws InstanceNotFoundException,
                                   IOException {
        for (RegistrationFilter filter : filters) {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter, null);
        }
        registerConnectListener();
    }

    public void register(RegistrationFilter filter) {
        filters.add(filter);
        try {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter, null);
        } catch (InstanceNotFoundException | IOException e) {
            // ignored
        }
    }

    public void deregister(RegistrationFilter filter) {
        filters.remove(filter);
        try {
            getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                  mbsListener,
                                                                  filter, null);
        } catch (InstanceNotFoundException | ListenerNotFoundException
                | IOException e) {
            // ignored
        }
    }

    abstract protected void deregisterConnectListener();

    abstract protected void registerConnectListener();

    private NotificationListener connectionListener() {
        return new NotificationListener() {
            @Override
            public void handleNotification(Notification notification,
                                           Object handback) {
                groo.handleJMXConnectionNotification(notification,
                                                     MbscFactory.this);
            }
        };
    }

    private NotificationListener mbsListener() {
        return new NotificationListener() {
            @Override
            public void handleNotification(Notification notification,
                                           Object handback) {
                if (notification instanceof MBeanServerNotification) {
                    groo.handleMBeanServerNotification(MbscFactory.this,
                                                       (MBeanServerNotification) notification);
                }
            }
        };
    }

    abstract public String getConnectionId() throws IOException;

    abstract public MBeanServerConnection getMBeanServerConnection()
                                                                    throws IOException;

}
