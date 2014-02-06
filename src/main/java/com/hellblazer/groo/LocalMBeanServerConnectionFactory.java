package com.hellblazer.groo;

import java.io.IOException;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

public class LocalMBeanServerConnectionFactory implements
        MBeanServerConnectionFactory {

    public static MBeanServerConnectionFactory newInstance(MBeanServer s) {
        String mbsid = "unknown_MBeanServerId";
        try {
            mbsid = (String) s.getAttribute(Groo.MBSDelegateObjectName,
                                            "MBeanServerId");
        } catch (Exception x) {
            // OK: should never happen...
        }
        final String cid = "local://" + mbsid;
        return new LocalMBeanServerConnectionFactory(s, cid);
    }

    final private String                connectionId;
    final private MBeanServerConnection localConnection;

    public LocalMBeanServerConnectionFactory(MBeanServerConnection local,
                                             String localID) {
        localConnection = local;
        connectionId = localID;
    }

    @Override
    public void addConnectionNotificationListener(NotificationListener listener,
                                                  NotificationFilter filter,
                                                  Object handback) {
        // localConnection are never broken etc..
    }

    @Override
    public final String getConnectionId() throws IOException {
        return connectionId;
    }

    @Override
    public final MBeanServerConnection getMBeanServerConnection()
                                                                 throws IOException {
        return localConnection;
    }

    @Override
    public void removeConnectionNotificationListener(NotificationListener listener)
                                                                                   throws ListenerNotFoundException {
        // localConnection are never broken etc..
    }

    @Override
    public void removeConnectionNotificationListener(NotificationListener l,
                                                     NotificationFilter f,
                                                     Object handback)
                                                                     throws ListenerNotFoundException {
        // localConnection are never broken etc..
    }
}
