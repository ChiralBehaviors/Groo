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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

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

/**
 * @author hhildebrand
 * 
 */
public class Node implements NodeMBean, MBeanRegistration {
    private final Set<NodeMBean>     children = new CopyOnWriteArraySet<>();
    private MBeanServer              mbs;
    private ObjectName               name;
    private final RegistrationFilter filter;

    public Node() {
        this(null, null);
    }

    public Node(ObjectName sourcePattern, QueryExp sourceQuery) {
        filter = new RegistrationFilter(sourcePattern, sourceQuery);
    }

    /**
     * @param child
     */
    public void addChild(NodeMBean child) {
        children.add(child);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        for (NodeMBean child : children) {
            try {
                child.addNotificationListener(name, listener, filter, handback);
                return;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        mbs.addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#addNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter,
     *      java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        for (NodeMBean child : children) {
            try {
                child.addNotificationListener(name, listener, filter, handback);
                return;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        mbs.addNotificationListener(name, listener, filter, handback);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#addNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.addNotificationListener(name, queryExpr, listener,
                                              filter, handback);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : names) {
            mbs.addNotificationListener(n, listener, filter, handback);
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#addNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException,
                                                        IOException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.addNotificationListener(name, queryExpr, listener,
                                              filter, handback);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : names) {
            mbs.addNotificationListener(n, listener, filter, handback);
        }
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
    public Map<ObjectName, Object> getAttribute(ObjectName name,
                                                QueryExp queryExpr,
                                                String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException,
                                                                 IOException {
        Map<ObjectName, Object> attributes = new HashMap<>();
        for (NodeMBean child : children) {
            attributes.putAll(child.getAttribute(name, queryExpr, attribute));
        }
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            attributes.put(instance, mbs.getAttribute(instance, attribute));
        }
        return attributes;
    }

    /**
     * @param name
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
    public Object getAttribute(ObjectName name, String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException {
        for (NodeMBean child : children) {
            try {
                return child.getAttribute(name, attribute);
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        return mbs.getAttribute(name, attribute);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getAttributes(javax.management.ObjectName, javax.management.QueryExp, java.lang.String[])
     */
    @Override
    public Map<ObjectName, AttributeList> getAttributes(ObjectName name,
                                                        QueryExp queryExpr,
                                                        String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException,
                                                                            IOException {
        Map<ObjectName, AttributeList> attrs = new HashMap<>();
        for (NodeMBean child : children) {
            attrs.putAll(child.getAttributes(name, queryExpr, attributes));
        }
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            attrs.put(instance, mbs.getAttributes(instance, attributes));
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
    public AttributeList getAttributes(ObjectName name, String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException {
        for (NodeMBean child : children) {
            AttributeList attrs = child.getAttributes(name, attributes);
            if (attrs != null) {
                return attrs;
            }
        }
        return mbs.getAttributes(name, attributes);
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
        int count = 0;
        for (NodeMBean child : children) {
            count += child.getMBeanCount();
        }
        return count + mbs.getMBeanCount();
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getMBeanCount(javax.management.ObjectName)
     */
    @Override
    public int getMBeanCount(ObjectName filter) {
        int count = 0;
        for (NodeMBean child : children) {
            count += child.getMBeanCount(filter);
        }
        return count + mbs.queryNames(filter, null).size();
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#getMBeanInfo(javax.management.ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo(ObjectName name)
                                                  throws InstanceNotFoundException,
                                                  IntrospectionException,
                                                  ReflectionException {
        for (NodeMBean child : children) {
            MBeanInfo info = child.getMBeanInfo(name);
            if (info != null) {
                return info;
            }
        }
        return mbs.getMBeanInfo(name);
    }

    public Set<NodeMBean> getChildren() {
        return Collections.unmodifiableSet(children);
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
    public ObjectInstance getObjectInstance(ObjectName name)
                                                            throws InstanceNotFoundException {
        for (NodeMBean child : children) {
            ObjectInstance instance = child.getObjectInstance(name);
            if (instance != null) {
                return instance;
            }
        }
        return mbs.getObjectInstance(name);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getObjectInstance(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> getObjectInstances(ObjectName name,
                                                  QueryExp queryExpr)
                                                                     throws InstanceNotFoundException,
                                                                     IOException {
        Set<ObjectInstance> instances = new HashSet<>();
        for (NodeMBean child : children) {
            instances.addAll(child.getObjectInstances(name, queryExpr));
        }
        instances.addAll(mbs.queryMBeans(name, queryExpr));
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
    public Map<ObjectName, Object> invoke(ObjectName name, QueryExp queryExpr,
                                          String operationName,
                                          Object[] params, String[] signature)
                                                                              throws InstanceNotFoundException,
                                                                              MBeanException,
                                                                              ReflectionException,
                                                                              IOException {
        Map<ObjectName, Object> results = new HashMap<>();
        for (NodeMBean child : children) {
            results.putAll(child.invoke(name, queryExpr, operationName, params,
                                        signature));
        }
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            results.put(instance,
                        mbs.invoke(instance, operationName, params, signature));
        }
        return results;
    }

    /**
     * @param name
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
    public Object invoke(ObjectName name, String operationName,
                         Object[] params, String[] signature)
                                                             throws InstanceNotFoundException,
                                                             MBeanException,
                                                             ReflectionException {
        for (NodeMBean child : children) {
            Object result = child.invoke(name, operationName, params, signature);
            if (result != null) {
                return result;
            }
        }
        return mbs.invoke(name, operationName, params, signature);
    }

    /**
     * @param name
     * @param className
     * @return
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#isInstanceOf(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException,
                                                                  IOException {
        for (NodeMBean child : children) {
            if (child.isInstanceOf(name, className)) {
                return true;
            }
        }
        return mbs.isInstanceOf(name, className);
    }

    /**
     * @param name
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered(ObjectName name) throws IOException {
        for (NodeMBean child : children) {
            if (child.isRegistered(name)) {
                return true;
            }
        }
        return mbs.isRegistered(name);
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
     * @param name
     * @param query
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#queryMBeans(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
                                                                           throws IOException {
        Set<ObjectInstance> result = new HashSet<>();
        for (NodeMBean child : children) {
            result.addAll(child.queryMBeans(name, query));
        }
        result.addAll(mbs.queryMBeans(name, query));
        return result;
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
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
                                                                      throws IOException {
        Set<ObjectName> result = new HashSet<>();
        for (NodeMBean child : children) {
            result.addAll(child.queryNames(name, query));
        }
        result.addAll(mbs.queryNames(name, query));
        return result;
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
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException {
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, listener);
                return;
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        mbs.removeNotificationListener(name, listener);
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
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, listener, filter,
                                                 handback);
                return;
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        mbs.removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener)
                                                                                throws InstanceNotFoundException,
                                                                                ListenerNotFoundException,
                                                                                IOException {
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, listener);
                return;
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        mbs.removeNotificationListener(name, listener);
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
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, listener, filter,
                                                 handback);
                return;
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        mbs.removeNotificationListener(name, listener, filter, handback);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, queryExpr, listener);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : mbs.queryNames(name, queryExpr)) {
            mbs.removeNotificationListener(n, listener);
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.NotificationListener, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, queryExpr, listener,
                                                 filter, handback);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : mbs.queryNames(name, queryExpr)) {
            mbs.removeNotificationListener(n, listener, filter, handback);
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener)
                                                               throws InstanceNotFoundException,
                                                               ListenerNotFoundException,
                                                               IOException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, queryExpr, listener);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : mbs.queryNames(name, queryExpr)) {
            mbs.removeNotificationListener(n, listener);
        }
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#removeNotificationListener(javax.management.ObjectName, javax.management.QueryExp, javax.management.ObjectName, javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        boolean success = false;
        for (NodeMBean child : children) {
            try {
                child.removeNotificationListener(name, queryExpr, listener,
                                                 filter, handback);
                success = true;
            } catch (InstanceNotFoundException e) {
                // continue
            }
        }
        Set<ObjectName> names = mbs.queryNames(name, queryExpr);
        if (!success && names.size() == 0) {
            throw new InstanceNotFoundException(
                                                String.format("No instance found for %s, %s",
                                                              name, queryExpr));
        }
        for (ObjectName n : mbs.queryNames(name, queryExpr)) {
            mbs.removeNotificationListener(n, listener, filter, handback);
        }
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
    public void setAttribute(ObjectName name, Attribute attribute)
                                                                  throws InstanceNotFoundException,
                                                                  AttributeNotFoundException,
                                                                  InvalidAttributeValueException,
                                                                  MBeanException,
                                                                  ReflectionException,
                                                                  IOException {
        for (NodeMBean child : children) {
            try {
                child.setAttribute(name, attribute);
                return;
            } catch (InstanceNotFoundException e) {
                // ignored
            }
        }
        mbs.setAttribute(name, attribute);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#setAttribute(javax.management.ObjectName, javax.management.QueryExp, javax.management.Attribute)
     */
    @Override
    public void setAttribute(ObjectName name, QueryExp queryExpr,
                             Attribute attribute)
                                                 throws InstanceNotFoundException,
                                                 AttributeNotFoundException,
                                                 InvalidAttributeValueException,
                                                 MBeanException,
                                                 ReflectionException,
                                                 IOException {
        for (NodeMBean child : children) {
            child.setAttribute(name, queryExpr, attribute);
        }
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            mbs.setAttribute(instance, attribute);
        }
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
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException {
        for (NodeMBean child : children) {
            AttributeList list = child.setAttributes(name, attributes);
            if (list != null) {
                return list;
            }
        }
        return mbs.setAttributes(name, attributes);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#setAttributes(javax.management.ObjectName, javax.management.QueryExp, javax.management.AttributeList)
     */
    @Override
    public Map<ObjectName, AttributeList> setAttributes(ObjectName name,
                                                        QueryExp queryExpr,
                                                        AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException {
        Map<ObjectName, AttributeList> results = new HashMap<>();
        for (NodeMBean child : children) {
            results.putAll(child.setAttributes(name, queryExpr, attributes));
        }
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            results.put(instance, mbs.setAttributes(instance, attributes));
        }
        return results;
    }
}
