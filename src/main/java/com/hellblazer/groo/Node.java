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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author hhildebrand
 * 
 */
public class Node implements NodeMBean, MBeanRegistration {
    private static final Logger      log      = LoggerFactory.getLogger(Node.class);

    private final Set<NodeMBean>     children = new CopyOnWriteArraySet<>();
    private final Executor           executor;
    private final RegistrationFilter filter;
    private MBeanServer              mbs;
    private ObjectName               name;

    public Node() {
        this(null, null);
    }

    public Node(ObjectName sourcePattern, QueryExp sourceQuery) {
        this(sourcePattern, sourceQuery, Executors.newCachedThreadPool());
    }

    public Node(ObjectName sourcePattern, QueryExp sourceQuery,
                Executor executor) {
        filter = new RegistrationFilter(sourcePattern, sourceQuery);
        this.executor = executor;
    }

    /**
     * @param child
     */
    public void addChild(NodeMBean child) {
        children.add(child);
    }

    /**
     * @param objectName
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(final ObjectName objectName,
                                        final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback)
                                                              throws InstanceNotFoundException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.addNotificationListener(objectName, listener,
                                                    filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.addNotificationListener(objectName, listener,
                                                      filter, handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when adding notification listener on %s for %s",
                                        this, objectName, listener), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param objectName
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter,
     *      java.lang.Object)
     */
    @Override
    public void addNotificationListener(final ObjectName objectName,
                                        final ObjectName listener,
                                        final NotificationFilter filter,
                                        final Object handback)
                                                              throws InstanceNotFoundException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.addNotificationListener(objectName, listener,
                                                    filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.addNotificationListener(objectName, listener,
                                                      filter, handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when adding notification listener on %s for %s",
                                        this, objectName, listener), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#addNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(final ObjectName pattern,
                                        final QueryExp queryExpr,
                                        final NotificationListener listener,
                                        final NotificationFilter filter,
                                        final Object handback)
                                                              throws InstanceNotFoundException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.addNotificationListener(objectName, listener,
                                                    filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.addNotificationListener(pattern, queryExpr,
                                                      listener, filter,
                                                      handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when adding notification listener %s, %s",
                                        this, pattern, queryExpr), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          pattern, queryExpr));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#addNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(final ObjectName pattern,
                                        final QueryExp queryExpr,
                                        final ObjectName listener,
                                        final NotificationFilter filter,
                                        final Object handback)
                                                              throws InstanceNotFoundException,
                                                              IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.addNotificationListener(objectName, listener,
                                                    filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.addNotificationListener(pattern, queryExpr,
                                                      listener, filter,
                                                      handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when adding notification listener %s, %s",
                                        this, pattern, queryExpr), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          name, queryExpr));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Node other = (Node) obj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getAttribute(javax.management.ObjectName, javax.management.QueryExp, java.lang.String)
     */
    @Override
    public Map<ObjectName, Object> getAttribute(final ObjectName pattern,
                                                final QueryExp queryExpr,
                                                final String attribute)
                                                                       throws MBeanException,
                                                                       AttributeNotFoundException,
                                                                       InstanceNotFoundException,
                                                                       ReflectionException,
                                                                       IOException {
        Map<ObjectName, Object> attributes = new HashMap<>();
        ExecutorCompletionService<Map<ObjectName, Object>> completionService = new ExecutorCompletionService<>(
                                                                                                               executor);
        TaskGenerator<Map<ObjectName, Object>> generator = new TaskGenerator<Map<ObjectName, Object>>() {
            @Override
            public Callable<Map<ObjectName, Object>> localTask(final ObjectName objectName) {
                return new Callable<Map<ObjectName, Object>>() {
                    @Override
                    public Map<ObjectName, Object> call() throws Exception {
                        Map<ObjectName, Object> attributes = new HashMap<>();
                        attributes.put(objectName,
                                       mbs.getAttribute(objectName, attribute));
                        return attributes;
                    }
                };
            }

            @Override
            public Callable<Map<ObjectName, Object>> remoteTask(final NodeMBean child) {
                return new Callable<Map<ObjectName, Object>>() {
                    @Override
                    public Map<ObjectName, Object> call() throws Exception {
                        return child.getAttribute(pattern, queryExpr, attribute);
                    }
                };
            }
        };
        List<Future<Map<ObjectName, Object>>> futures = forAll(completionService,
                                                               generator,
                                                               pattern,
                                                               queryExpr);

        for (int i = 0; i < futures.size(); i++) {
            try {
                attributes.putAll(completionService.take().get());
            } catch (InterruptedException e) {
                return Collections.emptyMap();
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when collecting attributes %s, %s",
                                        this, pattern, queryExpr), e);
            }
        }
        if (attributes.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s, %s",
                                                              pattern,
                                                              queryExpr));
        }
        return attributes;
    }

    /**
     * @param objectName
     * @param attribute
     * @return
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getAttribute(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public Object getAttribute(final ObjectName objectName,
                               final String attribute)
                                                      throws MBeanException,
                                                      AttributeNotFoundException,
                                                      InstanceNotFoundException,
                                                      ReflectionException {
        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(
                                                                                              executor);
        TaskGenerator<Object> generator = new TaskGenerator<Object>() {
            @Override
            public Callable<Object> localTask(final ObjectName objectName) {
                return new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return mbs.getAttribute(objectName, attribute);
                    }
                };
            }

            @Override
            public Callable<Object> remoteTask(final NodeMBean child) {
                return new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return child.getAttribute(objectName, attribute);
                    }
                };
            }
        };
        List<Future<Object>> futures = forAll(completionService, generator,
                                              objectName);

        boolean attributeNotFound = false;
        for (int i = 0; i < futures.size(); i++) {
            try {
                Object attributeValue = completionService.take().get();
                for (Future<Object> future : futures) {
                    future.cancel(true);
                }
                return attributeValue;
            } catch (InterruptedException e) {
                return Collections.emptyMap();
            } catch (ExecutionException e) {
                if (e.getCause() instanceof AttributeNotFoundException) {
                    attributeNotFound |= true;
                } else {
                    log.debug(String.format("%s experienced exception when retriving attribute %s, %s",
                                            this, objectName, attribute), e);
                }
            }
        }
        if (attributeNotFound) {
            throw new AttributeNotFoundException(
                                                 String.format("Attribute not found: %s for %s",
                                                               attribute,
                                                               objectName));
        } else {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s",
                                                              objectName));
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getAttributes(javax.management.ObjectName, javax.management.QueryExp, java.lang.String[])
     */
    @Override
    public Map<ObjectName, AttributeList> getAttributes(final ObjectName pattern,
                                                        final QueryExp queryExpr,
                                                        final String[] attributes)
                                                                                  throws InstanceNotFoundException,
                                                                                  ReflectionException,
                                                                                  IOException {
        Map<ObjectName, AttributeList> attrs = new HashMap<>();
        ExecutorCompletionService<Map<ObjectName, AttributeList>> completionService = new ExecutorCompletionService<>(
                                                                                                                      executor);
        TaskGenerator<Map<ObjectName, AttributeList>> generator = new TaskGenerator<Map<ObjectName, AttributeList>>() {
            @Override
            public Callable<Map<ObjectName, AttributeList>> localTask(final ObjectName objectName) {
                return new Callable<Map<ObjectName, AttributeList>>() {
                    @Override
                    public Map<ObjectName, AttributeList> call()
                                                                throws Exception {
                        Map<ObjectName, AttributeList> attrs = new HashMap<>();
                        attrs.put(objectName,
                                  mbs.getAttributes(objectName, attributes));
                        return attrs;
                    }
                };
            }

            @Override
            public Callable<Map<ObjectName, AttributeList>> remoteTask(final NodeMBean child) {
                return new Callable<Map<ObjectName, AttributeList>>() {
                    @Override
                    public Map<ObjectName, AttributeList> call()
                                                                throws Exception {
                        return child.getAttributes(pattern, queryExpr,
                                                   attributes);
                    }
                };
            }
        };
        List<Future<Map<ObjectName, AttributeList>>> futures = forAll(completionService,
                                                                      generator,
                                                                      pattern,
                                                                      queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                attrs.putAll(completionService.take().get());
            } catch (InterruptedException e) {
                return Collections.emptyMap();
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving attributes %s, %s, %s, %s",
                                        this, pattern, queryExpr,
                                        Arrays.asList(attributes)), e);
            }
        }
        if (attrs.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s, %s",
                                                              pattern,
                                                              queryExpr));
        }
        return attrs;
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getAttributes(javax.management.ObjectName,
     *      java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(final ObjectName objectName,
                                       final String[] attributes)
                                                                 throws InstanceNotFoundException,
                                                                 ReflectionException {
        ExecutorCompletionService<AttributeList> completionService = new ExecutorCompletionService<>(
                                                                                                     executor);
        TaskGenerator<AttributeList> generator = new TaskGenerator<AttributeList>() {
            @Override
            public Callable<AttributeList> localTask(final ObjectName objectName) {
                return new Callable<AttributeList>() {
                    @Override
                    public AttributeList call() throws Exception {
                        return mbs.getAttributes(objectName, attributes);
                    }
                };
            }

            @Override
            public Callable<AttributeList> remoteTask(final NodeMBean child) {
                return new Callable<AttributeList>() {
                    @Override
                    public AttributeList call() throws Exception {
                        return child.getAttributes(objectName, attributes);
                    }
                };
            }
        };
        List<Future<AttributeList>> futures = forAll(completionService,
                                                     generator, objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                AttributeList attrs = completionService.take().get();
                for (Future<AttributeList> future : futures) {
                    future.cancel(true);
                }
                return attrs;
            } catch (InterruptedException e) {
                return new AttributeList();
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving attributes %s, %s",
                                        this, objectName,
                                        Arrays.asList(attributes)), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    public Set<NodeMBean> getChildren() {
        return Collections.unmodifiableSet(children);
    }

    /**
     * @return the filter
     */
    public RegistrationFilter getFilter() {
        return filter;
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount() {
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Integer> generator = new TaskGenerator<Integer>() {
            @Override
            public Callable<Integer> localTask(final ObjectName objectName) {
                return new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return mbs.getMBeanCount();
                    }
                };
            }

            @Override
            public Callable<Integer> remoteTask(final NodeMBean child) {
                return new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return child.getMBeanCount();
                    }
                };
            }
        };
        List<Future<Integer>> futures = forAll(completionService, generator,
                                               null);
        int count = 0;
        for (int i = 0; i < futures.size(); i++) {
            try {
                count += completionService.take().get();
            } catch (InterruptedException e) {
                return 0;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving mbean count %s",
                                        this), e);
            }
        }
        return count;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getMBeanCount(javax.management.ObjectName)
     */
    @Override
    public int getMBeanCount(final ObjectName filter, final QueryExp queryExp) {
        ExecutorCompletionService<Integer> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Integer> generator = new TaskGenerator<Integer>() {
            @Override
            public Callable<Integer> localTask(final ObjectName objectName) {
                return new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return mbs.queryNames(filter, queryExp).size();
                    }
                };
            }

            @Override
            public Callable<Integer> remoteTask(final NodeMBean child) {
                return new Callable<Integer>() {
                    @Override
                    public Integer call() throws Exception {
                        return child.getMBeanCount(filter, queryExp);
                    }
                };
            }
        };
        List<Future<Integer>> futures = forAll(completionService, generator,
                                               null);
        int count = 0;
        for (int i = 0; i < futures.size(); i++) {
            try {
                count += completionService.take().get();
            } catch (InterruptedException e) {
                return 0;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving mbean count %s, %s, %s",
                                        this, filter, queryExp), e);
            }
        }
        return count;
    }

    /**
     * @param objectName
     * @return
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getMBeanInfo(javax.management.ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo(final ObjectName objectName)
                                                              throws InstanceNotFoundException,
                                                              IntrospectionException,
                                                              ReflectionException {
        ExecutorCompletionService<MBeanInfo> completionService = new ExecutorCompletionService<>(
                                                                                                 executor);
        TaskGenerator<MBeanInfo> generator = new TaskGenerator<MBeanInfo>() {
            @Override
            public Callable<MBeanInfo> localTask(final ObjectName objectName) {
                return new Callable<MBeanInfo>() {
                    @Override
                    public MBeanInfo call() throws Exception {
                        return mbs.getMBeanInfo(objectName);
                    }
                };
            }

            @Override
            public Callable<MBeanInfo> remoteTask(final NodeMBean child) {
                return new Callable<MBeanInfo>() {
                    @Override
                    public MBeanInfo call() throws Exception {
                        return child.getMBeanInfo(objectName);
                    }
                };
            }
        };
        List<Future<MBeanInfo>> futures = forAll(completionService, generator,
                                                 objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                MBeanInfo info = completionService.take().get();
                for (Future<MBeanInfo> future : futures) {
                    future.cancel(true);
                }
                return info;
            } catch (InterruptedException e) {
                return null;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving mbean info %s, %s",
                                        this, objectName), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @return the name
     */
    @Override
    public ObjectName getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMBean#getObjectInstance(javax.management.ObjectName)
     */
    @Override
    public ObjectInstance getObjectInstance(final ObjectName objectName)
                                                                        throws InstanceNotFoundException {
        ExecutorCompletionService<ObjectInstance> completionService = new ExecutorCompletionService<>(
                                                                                                      executor);
        TaskGenerator<ObjectInstance> generator = new TaskGenerator<ObjectInstance>() {
            @Override
            public Callable<ObjectInstance> localTask(final ObjectName objectName) {
                return new Callable<ObjectInstance>() {
                    @Override
                    public ObjectInstance call() throws Exception {
                        return mbs.getObjectInstance(objectName);
                    }
                };
            }

            @Override
            public Callable<ObjectInstance> remoteTask(final NodeMBean child) {
                return new Callable<ObjectInstance>() {
                    @Override
                    public ObjectInstance call() throws Exception {
                        return child.getObjectInstance(objectName);
                    }
                };
            }
        };
        List<Future<ObjectInstance>> futures = forAll(completionService,
                                                      generator, objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                ObjectInstance instance = completionService.take().get();
                for (Future<ObjectInstance> future : futures) {
                    future.cancel(true);
                }
                return instance;
            } catch (InterruptedException e) {
                return null;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retriving object instance %s, %s",
                                        this, objectName), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getObjectInstance(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> getObjectInstances(final ObjectName filter,
                                                  final QueryExp queryExpr)
                                                                           throws InstanceNotFoundException,
                                                                           IOException {
        ExecutorCompletionService<Set<ObjectInstance>> completionService = new ExecutorCompletionService<>(
                                                                                                           executor);
        TaskGenerator<Set<ObjectInstance>> generator = new TaskGenerator<Set<ObjectInstance>>() {
            @Override
            public Callable<Set<ObjectInstance>> localTask(final ObjectName objectName) {
                return new Callable<Set<ObjectInstance>>() {
                    @Override
                    public Set<ObjectInstance> call() throws Exception {
                        return mbs.queryMBeans(objectName, queryExpr);
                    }
                };
            }

            @Override
            public Callable<Set<ObjectInstance>> remoteTask(final NodeMBean child) {
                return new Callable<Set<ObjectInstance>>() {
                    @Override
                    public Set<ObjectInstance> call() throws Exception {
                        return child.getObjectInstances(filter, queryExpr);
                    }
                };
            }
        };
        List<Future<Set<ObjectInstance>>> futures = forAll(completionService,
                                                           generator, filter);
        Set<ObjectInstance> instances = new HashSet<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                instances.addAll(completionService.take().get());
            } catch (InterruptedException e) {
                return instances;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when retreiving object instances %s, %s, %s",
                                        this, filter, queryExpr), e);
            }
        }
        if (instances.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s, %s",
                                                              filter, queryExpr));
        }
        return instances;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#invoke(javax.management.ObjectName, javax.management.Query, java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Map<ObjectName, Object> invoke(final ObjectName filter,
                                          final QueryExp queryExpr,
                                          final String operationName,
                                          final Object[] params,
                                          final String[] signature)
                                                                   throws InstanceNotFoundException,
                                                                   MBeanException,
                                                                   ReflectionException,
                                                                   IOException {
        ExecutorCompletionService<Map<ObjectName, Object>> completionService = new ExecutorCompletionService<>(
                                                                                                               executor);
        TaskGenerator<Map<ObjectName, Object>> generator = new TaskGenerator<Map<ObjectName, Object>>() {
            @Override
            public Callable<Map<ObjectName, Object>> localTask(final ObjectName objectName) {
                return new Callable<Map<ObjectName, Object>>() {
                    @Override
                    public Map<ObjectName, Object> call() throws Exception {
                        Map<ObjectName, Object> result = new HashMap<>();
                        result.put(objectName, mbs.invoke(objectName,
                                                          operationName,
                                                          params, signature));
                        return result;
                    }
                };
            }

            @Override
            public Callable<Map<ObjectName, Object>> remoteTask(final NodeMBean child) {
                return new Callable<Map<ObjectName, Object>>() {
                    @Override
                    public Map<ObjectName, Object> call() throws Exception {
                        return child.invoke(filter, queryExpr, operationName,
                                            params, signature);
                    }
                };
            }
        };
        List<Future<Map<ObjectName, Object>>> futures = forAll(completionService,
                                                               generator,
                                                               filter,
                                                               queryExpr);
        Map<ObjectName, Object> results = new HashMap<>();
        for (int i = 0; i < futures.size(); i++) {
            try {
                results.putAll(completionService.take().get());
            } catch (InterruptedException e) {
                return results;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when invoking %s, %s, %s, %s, %s",
                                        this,
                                        filter,
                                        queryExpr,
                                        operationName,
                                        params != null ? Arrays.asList(params)
                                                      : null,
                                        signature != null ? Arrays.asList(signature)
                                                         : null), e.getCause());
            }
        }
        if (results.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s, %s",
                                                              filter, queryExpr));
        }
        return results;
    }

    /**
     * @param objectName
     * @param operationName
     * @param params
     * @param signature
     * @return
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#invoke(javax.management.ObjectName,
     *      java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke(final ObjectName objectName,
                         final String operationName, final Object[] params,
                         final String[] signature)
                                                  throws InstanceNotFoundException,
                                                  MBeanException,
                                                  ReflectionException {
        ExecutorCompletionService<Object> completionService = new ExecutorCompletionService<>(
                                                                                              executor);
        TaskGenerator<Object> generator = new TaskGenerator<Object>() {
            @Override
            public Callable<Object> localTask(final ObjectName objectName) {
                return new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return mbs.invoke(objectName, operationName, params,
                                          signature);
                    }
                };
            }

            @Override
            public Callable<Object> remoteTask(final NodeMBean child) {
                return new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        return child.invoke(objectName, operationName, params,
                                            signature);
                    }
                };
            }
        };
        List<Future<Object>> futures = forAll(completionService, generator,
                                              objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                Object result = completionService.take().get();
                for (Future<Object> future : futures) {
                    future.cancel(true);
                }
                return result;
            } catch (InterruptedException e) {
                return null;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when invoking %s, %s, %s, %s",
                                        this,
                                        objectName,
                                        operationName,
                                        params != null ? Arrays.asList(params)
                                                      : null,
                                        signature != null ? Arrays.asList(signature)
                                                         : null), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param objectName
     * @param className
     * @return
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#isInstanceOf(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public boolean isInstanceOf(final ObjectName objectName,
                                final String className)
                                                       throws InstanceNotFoundException,
                                                       IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return mbs.isInstanceOf(objectName, className);
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return child.isInstanceOf(objectName, className);
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                Boolean result = completionService.take().get();
                for (Future<Boolean> future : futures) {
                    future.cancel(true);
                }
                return result;
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when determining instance of %s, %s",
                                        this, objectName, className), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param objectName
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered(final ObjectName objectName) throws IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return mbs.isRegistered(objectName);
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return child.isRegistered(objectName);
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                Boolean result = completionService.take().get();
                for (Future<Boolean> future : futures) {
                    future.cancel(true);
                }
                return result;
            } catch (InterruptedException e) {
                return false;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when determining is registered %s",
                                        this, objectName), e);
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#postDeregister()
     */
    @Override
    public void postDeregister() {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
     */
    @Override
    public void postRegister(Boolean registrationDone) {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#preDeregister()
     */
    @Override
    public void preDeregister() throws Exception {
    }

    /* (non-Javadoc)
     * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
     */
    @Override
    public ObjectName preRegister(MBeanServer server, ObjectName name)
                                                                      throws Exception {
        mbs = server;
        this.name = name;
        return name;
    }

    /**
     * @param filter
     * @param query
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#queryMBeans(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans(final ObjectName filter,
                                           final QueryExp query)
                                                                throws IOException {
        ExecutorCompletionService<Set<ObjectInstance>> completionService = new ExecutorCompletionService<>(
                                                                                                           executor);
        TaskGenerator<Set<ObjectInstance>> generator = new TaskGenerator<Set<ObjectInstance>>() {
            @Override
            public Callable<Set<ObjectInstance>> localTask(final ObjectName objectName) {
                return new Callable<Set<ObjectInstance>>() {
                    @Override
                    public Set<ObjectInstance> call() throws Exception {
                        return mbs.queryMBeans(filter, query);
                    }
                };
            }

            @Override
            public Callable<Set<ObjectInstance>> remoteTask(final NodeMBean child) {
                return new Callable<Set<ObjectInstance>>() {
                    @Override
                    public Set<ObjectInstance> call() throws Exception {
                        return child.queryMBeans(filter, query);
                    }
                };
            }
        };
        Set<ObjectInstance> instances = new HashSet<>();
        List<Future<Set<ObjectInstance>>> futures = forAll(completionService,
                                                           generator, filter,
                                                           query);
        for (int i = 0; i < futures.size(); i++) {
            try {
                instances.addAll(completionService.take().get());
            } catch (InterruptedException e) {
                return instances;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when querying mbeans %s, %s",
                                        this, filter, query), e);
            }
        }
        return instances;
    }

    /**
     * @param name
     * @param query
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#queryNames(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectName> queryNames(final ObjectName filter,
                                      final QueryExp query) throws IOException {
        ExecutorCompletionService<Set<ObjectName>> completionService = new ExecutorCompletionService<>(
                                                                                                       executor);
        TaskGenerator<Set<ObjectName>> generator = new TaskGenerator<Set<ObjectName>>() {
            @Override
            public Callable<Set<ObjectName>> localTask(final ObjectName objectName) {
                return new Callable<Set<ObjectName>>() {
                    @Override
                    public Set<ObjectName> call() throws Exception {
                        return mbs.queryNames(filter, query);
                    }
                };
            }

            @Override
            public Callable<Set<ObjectName>> remoteTask(final NodeMBean child) {
                return new Callable<Set<ObjectName>>() {
                    @Override
                    public Set<ObjectName> call() throws Exception {
                        return child.queryNames(filter, query);
                    }
                };
            }
        };
        Set<ObjectName> names = new HashSet<>();
        List<Future<Set<ObjectName>>> futures = forAll(completionService,
                                                       generator, filter, query);
        for (int i = 0; i < futures.size(); i++) {
            try {
                names.addAll(completionService.take().get());
            } catch (InterruptedException e) {
                return names;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when querying names %s, %s",
                                        this, filter, query), e);
            }
        }
        return names;
    }

    /**
     * @param child
     */
    public void removeChild(NodeMBean child) {
        children.remove(child);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName,
                                           final NotificationListener listener)
                                                                               throws InstanceNotFoundException,
                                                                               ListenerNotFoundException,
                                                                               IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener on %s for %s",
                                        this, objectName, listener), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param objectName
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName,
                                           final NotificationListener listener,
                                           final NotificationFilter filter,
                                           final Object handback)
                                                                 throws InstanceNotFoundException,
                                                                 ListenerNotFoundException,
                                                                 IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener,
                                                       filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(objectName, listener,
                                                         filter, handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener on %s for %s, %s",
                                        this, objectName, listener, filter), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param objectName
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName,
                                           final ObjectName listener)
                                                                     throws InstanceNotFoundException,
                                                                     ListenerNotFoundException,
                                                                     IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener on %s for %s",
                                        this, objectName, listener), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter,
     *      java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName objectName,
                                           final ObjectName listener,
                                           final NotificationFilter filter,
                                           final Object handback)
                                                                 throws InstanceNotFoundException,
                                                                 ListenerNotFoundException,
                                                                 IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener,
                                                       filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(objectName, listener,
                                                         filter, handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               objectName);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return; // don't even log this ;)
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener on %s for %s, %s, %s",
                                        this, objectName, listener, filter,
                                        handback), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(final ObjectName pattern,
                                           final QueryExp queryExpr,
                                           final NotificationListener listener)
                                                                               throws InstanceNotFoundException,
                                                                               ListenerNotFoundException,
                                                                               IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(pattern, queryExpr,
                                                         listener);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                if (completionService.take().get()) {
                    for (Future<Boolean> future : futures) {
                        future.cancel(true);
                    }
                    return;
                }
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when adding notification listener %s, %s",
                                        this, pattern, queryExpr), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          pattern, queryExpr));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName pattern,
                                           final QueryExp queryExpr,
                                           final NotificationListener listener,
                                           final NotificationFilter filter,
                                           final Object handback)
                                                                 throws InstanceNotFoundException,
                                                                 ListenerNotFoundException,
                                                                 IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener,
                                                       filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(pattern, queryExpr,
                                                         listener, filter,
                                                         handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener %s, %s, %s, %s, %s",
                                        this, pattern, queryExpr, listener,
                                        filter, handback), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          name, queryExpr));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(final ObjectName pattern,
                                           final QueryExp queryExpr,
                                           final ObjectName listener)
                                                                     throws InstanceNotFoundException,
                                                                     ListenerNotFoundException,
                                                                     IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(pattern, queryExpr,
                                                         listener);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener %s, %s, %s",
                                        this, pattern, queryExpr, listener), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          name, queryExpr));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(final ObjectName pattern,
                                           final QueryExp queryExpr,
                                           final ObjectName listener,
                                           final NotificationFilter filter,
                                           final Object handback)
                                                                 throws InstanceNotFoundException,
                                                                 ListenerNotFoundException,
                                                                 IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.removeNotificationListener(objectName, listener,
                                                       filter, handback);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.removeNotificationListener(pattern, queryExpr,
                                                         listener, filter,
                                                         handback);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when removing notification listener %s, %s, %s, %s, %s",
                                        this, pattern, queryExpr, listener,
                                        filter, handback), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          name, queryExpr));
    }

    /**
     * @param name
     * @param attribute
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServer#setAttribute(javax.management.ObjectName,
     *      javax.management.Attribute)
     */
    @Override
    public void setAttribute(final ObjectName objectName,
                             final Attribute attribute)
                                                       throws InstanceNotFoundException,
                                                       AttributeNotFoundException,
                                                       InvalidAttributeValueException,
                                                       MBeanException,
                                                       ReflectionException,
                                                       IOException {
        ExecutorCompletionService<Void> completionService = new ExecutorCompletionService<>(
                                                                                            executor);
        TaskGenerator<Void> generator = new TaskGenerator<Void>() {
            @Override
            public Callable<Void> localTask(final ObjectName objectName) {
                return new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        mbs.setAttribute(objectName, attribute);
                        return null;
                    }
                };
            }

            @Override
            public Callable<Void> remoteTask(final NodeMBean child) {
                return new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        child.setAttribute(objectName, attribute);
                        return null;
                    }
                };
            }
        };
        List<Future<Void>> futures = forAll(completionService, generator,
                                            objectName);

        boolean attributeNotFound = false;
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
                for (Future<Void> future : futures) {
                    future.cancel(true);
                }
                return;
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                if (e.getCause() instanceof AttributeNotFoundException) {
                    attributeNotFound |= true;
                } else {
                    log.debug(String.format("%s experienced exception when setting attribute %s, %s",
                                            this, objectName, attribute), e);
                }
            }
        }
        if (attributeNotFound) {
            throw new AttributeNotFoundException(
                                                 String.format("Attribute not found: %s for %s",
                                                               attribute,
                                                               objectName));
        } else {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s",
                                                              objectName));
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#setAttribute(javax.management.ObjectName, javax.management.QueryExp, javax.management.Attribute)
     */
    @Override
    public void setAttribute(final ObjectName pattern,
                             final QueryExp queryExpr, final Attribute attribute)
                                                                                 throws InstanceNotFoundException,
                                                                                 AttributeNotFoundException,
                                                                                 InvalidAttributeValueException,
                                                                                 MBeanException,
                                                                                 ReflectionException,
                                                                                 IOException {
        ExecutorCompletionService<Boolean> completionService = new ExecutorCompletionService<>(
                                                                                               executor);
        TaskGenerator<Boolean> generator = new TaskGenerator<Boolean>() {
            @Override
            public Callable<Boolean> localTask(final ObjectName objectName) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        mbs.setAttribute(objectName, attribute);
                        return true;
                    }
                };
            }

            @Override
            public Callable<Boolean> remoteTask(final NodeMBean child) {
                return new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        child.setAttribute(pattern, queryExpr, attribute);
                        return true;
                    }
                };
            }
        };
        List<Future<Boolean>> futures = forAll(completionService, generator,
                                               pattern, queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                completionService.take().get();
                return;
            } catch (InterruptedException e) {
                return;
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when setting attribute %s, %s, %s, %s",
                                        this, pattern, queryExpr, attribute), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s, %s",
                                                          pattern, queryExpr));
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see javax.management.MBeanServer#setAttributes(javax.management.ObjectName,
     *      javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(final ObjectName objectName,
                                       final AttributeList attributes)
                                                                      throws InstanceNotFoundException,
                                                                      ReflectionException,
                                                                      IOException {
        ExecutorCompletionService<AttributeList> completionService = new ExecutorCompletionService<>(
                                                                                                     executor);
        TaskGenerator<AttributeList> generator = new TaskGenerator<AttributeList>() {
            @Override
            public Callable<AttributeList> localTask(final ObjectName objectName) {
                return new Callable<AttributeList>() {
                    @Override
                    public AttributeList call() throws Exception {
                        return mbs.setAttributes(objectName, attributes);
                    }
                };
            }

            @Override
            public Callable<AttributeList> remoteTask(final NodeMBean child) {
                return new Callable<AttributeList>() {
                    @Override
                    public AttributeList call() throws Exception {
                        return child.setAttributes(objectName, attributes);
                    }
                };
            }
        };
        List<Future<AttributeList>> futures = forAll(completionService,
                                                     generator, objectName);

        for (int i = 0; i < futures.size(); i++) {
            try {
                AttributeList attrs = completionService.take().get();
                for (Future<AttributeList> future : futures) {
                    future.cancel(true);
                }
                return attrs;
            } catch (InterruptedException e) {
                return new AttributeList();
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when setting attributes %s, %s",
                                        this, objectName, attributes), e);
            }
        }
        throw new InstanceNotFoundException(
                                            String.format("Instance not found: %s",
                                                          objectName));
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#setAttributes(javax.management.ObjectName, javax.management.QueryExp, javax.management.AttributeList)
     */
    @Override
    public Map<ObjectName, AttributeList> setAttributes(final ObjectName pattern,
                                                        final QueryExp queryExpr,
                                                        final AttributeList attributes)
                                                                                       throws InstanceNotFoundException,
                                                                                       ReflectionException,
                                                                                       IOException {
        Map<ObjectName, AttributeList> attrs = new HashMap<>();
        ExecutorCompletionService<Map<ObjectName, AttributeList>> completionService = new ExecutorCompletionService<>(
                                                                                                                      executor);
        TaskGenerator<Map<ObjectName, AttributeList>> generator = new TaskGenerator<Map<ObjectName, AttributeList>>() {
            @Override
            public Callable<Map<ObjectName, AttributeList>> localTask(final ObjectName objectName) {
                return new Callable<Map<ObjectName, AttributeList>>() {
                    @Override
                    public Map<ObjectName, AttributeList> call()
                                                                throws Exception {
                        Map<ObjectName, AttributeList> attrs = new HashMap<>();
                        attrs.put(objectName,
                                  mbs.setAttributes(objectName, attributes));
                        return attrs;
                    }
                };
            }

            @Override
            public Callable<Map<ObjectName, AttributeList>> remoteTask(final NodeMBean child) {
                return new Callable<Map<ObjectName, AttributeList>>() {
                    @Override
                    public Map<ObjectName, AttributeList> call()
                                                                throws Exception {
                        return child.setAttributes(pattern, queryExpr,
                                                   attributes);
                    }
                };
            }
        };
        List<Future<Map<ObjectName, AttributeList>>> futures = forAll(completionService,
                                                                      generator,
                                                                      pattern,
                                                                      queryExpr);
        for (int i = 0; i < futures.size(); i++) {
            try {
                attrs.putAll(completionService.take().get());
            } catch (InterruptedException e) {
                return Collections.emptyMap();
            } catch (ExecutionException e) {
                log.debug(String.format("%s experienced exception when setting attributes %s, %s, %s, %s",
                                        this, pattern, queryExpr,
                                        Arrays.asList(attributes)), e);
            }
        }
        if (attrs.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("Instance not found: %s, %s",
                                                              pattern,
                                                              queryExpr));
        }
        return attrs;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "Node [" + name + ", " + filter + "]";
    }

    /**
     * @param completionService
     * @param generator
     * @param objectName
     * @return
     */
    private <V> List<Future<V>> forAll(ExecutorCompletionService<V> completionService,
                                       TaskGenerator<V> generator,
                                       ObjectName objectName) {
        List<Future<V>> futures = new ArrayList<>();
        for (NodeMBean child : children) {
            futures.add(completionService.submit(generator.remoteTask(child)));
        }
        futures.add(completionService.submit(generator.localTask(objectName)));
        return futures;
    }

    private <V> List<Future<V>> forAll(ExecutorCompletionService<V> completionService,
                                       TaskGenerator<V> generator,
                                       ObjectName pattern, QueryExp queryExpr) {
        List<Future<V>> futures = new ArrayList<>();
        for (NodeMBean child : children) {
            futures.add(completionService.submit(generator.remoteTask(child)));
        }
        for (ObjectName n : mbs.queryNames(pattern, queryExpr)) {
            futures.add(completionService.submit(generator.localTask(n)));
        }
        return futures;
    }
}
