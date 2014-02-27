package com.chiralBehaviors.groo;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.management.ListenerNotFoundException;
import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.security.auth.Subject;

/**
 * A basic {@link MbscFactory} that wraps a {@link JMXConnector}. This
 * implementation does not support transparent reconnection.
 * 
 * @since Java DMK 5.1
 **/
public class BasicMbscFactory extends MbscFactory {

    private final AtomicReference<MBeanServerConnection> connection = new AtomicReference<>();
    private final JMXConnector                           connector;
    private final AtomicBoolean                          failed     = new AtomicBoolean();
    private final Subject                                subject;

    public BasicMbscFactory(Groo groo, JMXConnector connector,
                            Subject delegationSubject) {
        super(groo);
        this.connector = connector;
        subject = delegationSubject;
        connection.set(null);
        failed.set(false);
    }

    @Override
    public String getConnectionId() throws IOException {
        if (failed.get()) {
            throw new IOException("connection already failed");
        }
        return getJMXConnector().getConnectionId();
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

    /* (non-Javadoc)
     * @see com.hellblazer.groo.MbscFactory#deregisterConnectListener()
     */
    @Override
    protected void deregisterConnectListener() {
        try {
            connector.removeConnectionNotificationListener(connectListener,
                                                           null, null);
        } catch (ListenerNotFoundException e) {
            // ignored
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.MbscFactory#registerConnectListener()
     */
    @Override
    protected void registerConnectListener() {
        connector.addConnectionNotificationListener(connectListener, null, null);
    }

}
