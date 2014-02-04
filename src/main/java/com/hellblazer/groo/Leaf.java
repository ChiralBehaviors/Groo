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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistration;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.NotCompliantMBeanException;
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
public class Leaf implements LeafMBean, MBeanRegistration {
    private MBeanServer mbs;
    private ObjectName  name;

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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
        return mbs.getAttributes(name, attributes);
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount() {
        return mbs.getMBeanCount();
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getMBeanCount(javax.management.ObjectName)
     */
    @Override
    public int getMBeanCount(ObjectName filter) {
        return mbs.queryNames(filter, null).size();
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
        return mbs.getMBeanInfo(name);
    }

    /**
     * @return the name
     */
    public ObjectName getName() {
        return name;
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#getObjectInstance(javax.management.ObjectName)
     */
    public ObjectInstance getObjectInstance(ObjectName name)
                                                            throws InstanceNotFoundException {
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
        return mbs.queryMBeans(name, queryExpr);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#invoke(javax.management.ObjectName, javax.management.QueryExp, java.lang.String, java.lang.Object[], java.lang.String[])
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
        return mbs.invoke(name, operationName, params, signature);
    }

    /**
     * @param name
     * @param className
     * @return
     * @throws InstanceNotFoundException
     * @see javax.management.MBeanServer#isInstanceOf(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException {
        return mbs.isInstanceOf(name, className);
    }

    /**
     * @param name
     * @return
     * @see javax.management.MBeanServer#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered(ObjectName name) {
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
     * @see javax.management.MBeanServer#queryMBeans(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query) {
        return mbs.queryMBeans(name, query);
    }

    /**
     * @param name
     * @param query
     * @return
     * @see javax.management.MBeanServer#queryNames(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query) {
        return mbs.queryNames(name, query);
    }

    /**
     * @param object
     * @param name
     * @return
     * @throws InstanceAlreadyExistsException
     * @throws MBeanRegistrationException
     * @throws NotCompliantMBeanException
     * @see javax.management.MBeanServer#registerMBean(java.lang.Object,
     *      javax.management.ObjectName)
     */
    public ObjectInstance registerMBean(Object object, ObjectName name)
                                                                       throws InstanceAlreadyExistsException,
                                                                       MBeanRegistrationException,
                                                                       NotCompliantMBeanException {
        return mbs.registerMBean(object, name);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException {
        mbs.removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
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
                                                           ListenerNotFoundException {
        mbs.removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @see javax.management.MBeanServer#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener)
                                                                                throws InstanceNotFoundException,
                                                                                ListenerNotFoundException {
        mbs.removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
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
                                                           ListenerNotFoundException {
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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

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
        // TODO Auto-generated method stub

    }

    /**
     * @param name
     * @param attribute
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @see javax.management.MBeanServer#setAttribute(javax.management.ObjectName,
     *      javax.management.Attribute)
     */
    @Override
    public void setAttribute(ObjectName name, Attribute attribute)
                                                                  throws InstanceNotFoundException,
                                                                  AttributeNotFoundException,
                                                                  InvalidAttributeValueException,
                                                                  MBeanException,
                                                                  ReflectionException {
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
     * @see javax.management.MBeanServer#setAttributes(javax.management.ObjectName,
     *      javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException {
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
        for (ObjectName instance : mbs.queryNames(name, queryExpr)) {
            results.put(instance, mbs.setAttributes(instance, attributes));
        }
        return results;
    }

}
