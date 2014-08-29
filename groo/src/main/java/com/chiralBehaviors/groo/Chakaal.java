/** 
 * (C) Copyright 2014 Chiral Behaviors, All Rights Reserved
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

package com.chiralBehaviors.groo;

import static com.hellblazer.slp.ServiceScope.SERVICE_TYPE;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.management.InstanceNotFoundException;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hellblazer.slp.InvalidSyntaxException;
import com.hellblazer.slp.ServiceEvent;
import com.hellblazer.slp.ServiceListener;
import com.hellblazer.slp.ServiceReference;
import com.hellblazer.slp.ServiceScope;
import com.hellblazer.slp.ServiceURL;

/**
 * A discovery service for JMX services. Chakaal will listen for services
 * matching programmed queries against the discovery service and when they are
 * registered, they will be added to the Groo aggregator.
 * 
 * @author hhildebrand
 * 
 */
public class Chakaal implements ChakaalMBean {
    private class Listener implements ServiceListener {

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.hellblazer.slp.ServiceListener#serviceChanged(com.hellblazer.slp.ServiceEvent)
         */
        @Override
        public void serviceChanged(ServiceEvent event) {
            switch (event.getType()) {
                case REGISTERED: {
                    registered(event.getReference());
                    break;
                }
                case MODIFIED: {
                    modified(event.getReference());
                    break;
                }
                case UNREGISTERED: {
                    unregistered(event.getReference());
                    break;
                }
            }
        }

    }

    private final static Logger                    log                = LoggerFactory.getLogger(Chakaal.class);

    private final Subject                          delegationSubject;
    private final ConcurrentMap<UUID, MbscFactory> discovered         = new ConcurrentHashMap<>();
    private final Groo                             groo;
    private final ConcurrentMap<String, Listener>  outstandingQueries = new ConcurrentHashMap<>();
    private final ServiceScope                     scope;
    private final Map<String, ?>                   sourceMap;

    /**
     * @param groo
     * @param scope
     */
    public Chakaal(Groo groo, ServiceScope scope, Map<String, ?> sourceMap,
                   Subject delegationSubject) {
        this.groo = groo;
        this.scope = scope;
        this.sourceMap = sourceMap;
        this.delegationSubject = delegationSubject;
    }

    /* (non-Javadoc)
     * @see com.chiralBehaviors.groo.ChakaalMBean#getDiscovered()
     */
    @Override
    public String[] getDiscovered() {
        List<String> discoveredConnections = new ArrayList<>();
        for (MbscFactory factory : discovered.values()) {
            try {
                discoveredConnections.add(factory.getConnectionId());
            } catch (IOException e) {
                log.trace(String.format("unable to get connection id for %s",
                                        factory), e);
            }
        }
        return discoveredConnections.toArray(new String[discoveredConnections.size()]);
    }

    /**
     * @return the groo
     */
    public Groo getGroo() {
        return groo;
    }

    /* (non-Javadoc)
     * @see com.chiralBehaviors.groo.ChakaalMBean#getQueries()
     */
    @Override
    public String[] getQueries() {
        List<String> queries = new ArrayList<>();
        for (String query : outstandingQueries.keySet()) {
            queries.add(query);
        }
        return queries.toArray(new String[] {});
    }

    public ServiceScope getScope() {
        return scope;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#listenFor(java.lang.String)
     */
    @Override
    public void listenFor(String query) throws InvalidSyntaxException {
        Listener listener = new Listener();
        if (outstandingQueries.putIfAbsent(query, listener) == null) {
            log.info(String.format("Listening for %s", query));
            scope.addServiceListener(listener, query);
        } else {
            log.info(String.format("Already listening for %s", query));
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#listenForService(java.lang.String)
     */
    @Override
    public void listenForService(String service) throws InvalidSyntaxException {
        listenFor(serviceQuery(service));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#listenForService(java.lang.String, java.lang.String)
     */
    @Override
    public void listenForService(String service, String q)
                                                          throws InvalidSyntaxException {
        listenFor(combine(service, q));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#removeQuery(java.lang.String)
     */
    @Override
    public void removeQuery(String query) throws InvalidSyntaxException {
        Listener listener = outstandingQueries.remove(query);
        if (listener == null) {
            log.info(String.format("No listener registered for query '%s'",
                                   query));
            return;
        }
        log.info(String.format("No longer listening for %s", query));
        scope.removeServiceListener(listener, query);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#removeServiceNameQuery(java.lang.String)
     */
    @Override
    public void removeServiceNameQuery(String serviceName)
                                                          throws InvalidSyntaxException {
        removeQuery(serviceQuery(serviceName));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.ChakallMBean#removeServiceNameQuery(java.lang.String, java.lang.String)
     */
    @Override
    public void removeServiceNameQuery(String serviceName, String query)
                                                                        throws InvalidSyntaxException {
        removeQuery(combine(serviceName, query));
    }

    /**
     * @param service
     * @param query
     * @return
     */
    private String combine(String service, String query) {
        return query == null ? serviceQuery(service)
                            : String.format("((%s=%s) && q)", SERVICE_TYPE,
                                            service, query);
    }

    /**
     * @param reference
     */
    private void modified(ServiceReference reference) {
        log.info(String.format("Modified: %s", reference));
    }

    /**
     * @param reference
     */
    private void registered(ServiceReference reference) {
        log.info(String.format("Discovered: %s", reference));
        JMXServiceURL url;
        try {
            url = toServiceURL(reference);
        } catch (MalformedURLException e) {
            log.warn(String.format("Unable to construct JMX service URL from: %s",
                                   reference.getUrl().toString()), e);
            return;
        }
        JMXConnector connection;
        try {
            connection = JMXConnectorFactory.newJMXConnector(url, sourceMap);
        } catch (IOException e) {
            log.warn(String.format("Error connecting to: %s", url.toString()),
                     e);
            return;
        }
        MbscFactory factory = new BasicMbscFactory(groo, connection,
                                                   delegationSubject);
        discovered.put(reference.getRegistration(), factory);
        try {
            groo.addConnection(factory);
        } catch (InstanceNotFoundException | IOException e) {
            log.warn(String.format("Cannot add connection: %s to: %s", factory,
                                   groo), e);
        }
    }

    /**
     * @param serviceName
     * @return
     */
    private String serviceQuery(String serviceName) {
        return String.format("(%s=%s)", SERVICE_TYPE, serviceName);
    }

    private JMXServiceURL toServiceURL(ServiceReference reference)
                                                                  throws MalformedURLException {
        ServiceURL url = reference.getUrl();
        String jmxUrl = "jmx".equals(url.getServiceType().getAbstractTypeName()) ? String.format("%s://%s:%s%s",
                                                                                                 url.getServiceType().toString(),
                                                                                                 url.getHost(),
                                                                                                 url.getPort(),
                                                                                                 url.getUrlPath())
                                                                                : String.format("service:%s://%s:%s%s",
                                                                                                url.getServiceType().getConcreteTypeName().toString(),
                                                                                                url.getHost(),
                                                                                                url.getPort(),
                                                                                                url.getUrlPath());
        return new JMXServiceURL(jmxUrl);
    }

    /**
     * @param reference
     */
    private void unregistered(ServiceReference reference) {
        log.info(String.format("Unregistering: %s", reference));
    }

}
