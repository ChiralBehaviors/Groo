package com.hellblazer.groo;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final Logger            log            = Logger.getLogger(MbscFactory.class.getCanonicalName());

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
        log.info(String.format("Deregistering filter: %s on :%s", filter, this));
        filters.remove(filter);
        try {
            getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                  mbsListener,
                                                                  filter,
                                                                  filter.getHandback());
        } catch (InstanceNotFoundException | ListenerNotFoundException
                | IOException e) {
            log.log(Level.FINE,
                    String.format("error deregistering filter: %s on :%s",
                                  filter, this), e);
        }
    }

    public void deregisterListeners() {
        try {
            getMBeanServerConnection().removeNotificationListener(MBSDelegateObjectName,
                                                                  mbsListener);
        } catch (InstanceNotFoundException | ListenerNotFoundException
                | IOException e) {
            log.log(Level.FINE,
                    String.format("error deregistering mbs listener on :%s",
                                  this), e);
        }
        deregisterConnectListener();
    }

    abstract public String getConnectionId() throws IOException;

    abstract public MBeanServerConnection getMBeanServerConnection()
                                                                    throws IOException;

    public void register(RegistrationFilter filter) {
        filters.add(filter);
        log.info(String.format("registering filter %s on: %s", filter, this));
        try {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter,
                                                               filter.getHandback());
        } catch (InstanceNotFoundException | IOException e) {
            log.log(Level.FINE,
                    String.format("error registering filter: %s on :%s",
                                  filter, this), e);
        }
    }

    public void registerBuilder(RegistrationFilter filter) {
        builderFilters.add(filter);
        log.info(String.format("registering builder filter %s on: %s", filter,
                               this));
        try {
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               builderListener,
                                                               filter,
                                                               filter.getHandback());
        } catch (InstanceNotFoundException | IOException e) {
            log.log(Level.FINE,
                    String.format("error registering builder filter: %s on :%s",
                                  filter, this), e);
        }
    }

    public void registerListeners() throws InstanceNotFoundException,
                                   IOException {
        for (RegistrationFilter filter : filters) {
            log.info(String.format("Registering filter: %s on :%s", filter,
                                   filter, this));
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               mbsListener,
                                                               filter,
                                                               filter.getHandback());
        }
        for (RegistrationFilter filter : builderFilters) {
            log.info(String.format("Registering builder filter: %s on :%s",
                                   filter, filter, this));
            getMBeanServerConnection().addNotificationListener(MBSDelegateObjectName,
                                                               builderListener,
                                                               filter,
                                                               filter.getHandback());
        }
        registerConnectListener();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        try {
            return String.format("%s [%s]", getClass().getSimpleName(),
                                 getConnectionId());
        } catch (IOException e) {
            return String.format("%s [UNKNOWN]", getClass().getSimpleName());
        }
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
