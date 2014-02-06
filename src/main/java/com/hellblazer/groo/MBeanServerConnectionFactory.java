package com.hellblazer.groo;

import java.io.IOException;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public interface MBeanServerConnectionFactory {

    public void addConnectionNotificationListener(NotificationListener listener,
                                                  NotificationFilter filter,
                                                  Object handback);

    public String getConnectionId() throws IOException;

    public MBeanServerConnection getMBeanServerConnection() throws IOException;

    public void removeConnectionNotificationListener(NotificationListener listener)
                                                                                   throws ListenerNotFoundException;

    public void removeConnectionNotificationListener(NotificationListener l,
                                                     NotificationFilter f,
                                                     Object handback)
                                                                     throws ListenerNotFoundException;

}
