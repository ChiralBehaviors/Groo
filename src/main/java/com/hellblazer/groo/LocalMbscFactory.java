package com.hellblazer.groo;

import java.io.IOException;

import javax.management.MBeanServerConnection;

public class LocalMbscFactory extends MbscFactory {

    final private String                connectionId;
    final private MBeanServerConnection localConnection;

    public LocalMbscFactory(Groo groo, MBeanServerConnection local,
                            String localID) {
        super(groo);
        localConnection = local;
        connectionId = localID;
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

    /* (non-Javadoc)
     * @see com.hellblazer.groo.MbscFactory#deregisterConnectListener()
     */
    @Override
    protected void deregisterConnectListener() {
        // do nothing
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.MbscFactory#registerConnectListener()
     */
    @Override
    protected void registerConnectListener() {
        // do nothing
    }
}
