package com.hellblazer.groo;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
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

    private final List<RegistrationFilter> builderFilters = new CopyOnWriteArrayList<>();
    private final List<RegistrationFilter> filters        = new CopyOnWriteArrayList<>();
    private final Groo                     groo;
    protected final NotificationListener   builderListener;
    protected final NotificationListener   connectListener;
    protected final NotificationListener   mbsListener;

    /**
     * @param groo
     */
    public MbscFactory(Groo groo) {
        this.groo = groo;
        connectListener = connectionListener();
        mbsListener = mbsListener();
        builderListener = nbListener();
    }

    public void deregister(RegistrationFilter filter) {
        filters.remove(filter);
        try {
            getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                  mbsListener,
                                                                  filter,
                                                                  filter.getHandback());
        } catch (InstanceNotFoundException | ListenerNotFoundException
                | IOException e) {
            // ignored
        }
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

    abstract public String getConnectionId() throws IOException;

    abstract public MBeanServerConnection getMBeanServerConnection()
                                                                    throws IOException;

    public void register(RegistrationFilter filter) {
        filters.add(filter);
        try {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter,
                                                               filter.getHandback());
        } catch (InstanceNotFoundException | IOException e) {
            // ignored
        }
    }

    public void registerBuilder(RegistrationFilter filter) {
        builderFilters.add(filter);
        try {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               nbListener(),
                                                               filter,
                                                               filter.getHandback());
        } catch (InstanceNotFoundException | IOException e) {
            // ignored
        }
    }

    public void registerListeners() throws InstanceNotFoundException,
                                   IOException {
        for (RegistrationFilter filter : filters) {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter,
                                                               filter.getHandback());
        }
        for (RegistrationFilter filter : builderFilters) {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               builderListener,
                                                               filter,
                                                               filter.getHandback());
        }
        registerConnectListener();
    }

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
                                                       (MBeanServerNotification) notification,
                                                       (UUID) handback);
                }
            }
        };
    }

    private NotificationListener nbListener() {
        return new NotificationListener() {
            @Override
            public void handleNotification(Notification notification,
                                           Object handback) {
                if (notification instanceof MBeanServerNotification) {
                    groo.handleNetworkBuilderNotification((MBeanServerNotification) notification,
                                                          (UUID) handback);
                }
            }
        };
    }

    abstract protected void deregisterConnectListener();

    abstract protected void registerConnectListener();

}
