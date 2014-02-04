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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationEmitter;
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
public class Node implements NodeMXBean {

    private static class ListenerInfo {
        private static boolean same(Object x, Object y) {
            if (x == y) {
                return true;
            }
            if (x == null) {
                return false;
            }
            return x.equals(y);
        }

        public final NotificationFilter   filter;
        public final Object               handback;

        public final NotificationListener listener;

        public ListenerInfo(NotificationListener listener,
                            NotificationFilter filter, Object handback) {

            if (listener == null) {
                throw new IllegalArgumentException("Null listener.");
            }

            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof ListenerInfo)) {
                return false;
            }

            return listener.equals(((ListenerInfo) o).listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }

        boolean equals(NotificationListener listener,
                       NotificationFilter filter, Object handback) {
            return this.listener == listener && same(this.filter, filter)
                   && same(this.handback, handback);
        }
    }

    private static final Logger logger = Logger.getLogger(Node.class.getCanonicalName());

    private static void sendNotif(List<ListenerInfo> listeners, Notification n) {
        for (ListenerInfo li : listeners) {
            if (li.filter == null || li.filter.isNotificationEnabled(n)) {
                try {
                    li.listener.handleNotification(n, li.handback);
                } catch (Exception e) {
                    logger.log(Level.FINEST, "sendNotif handleNotification", e);
                }
            }
        }
    }

    private List<NodeMXBean>                          children;
    private final Map<ObjectName, List<ListenerInfo>> exactSubscriptionMap   = new HashMap<ObjectName, List<ListenerInfo>>();
    private final Map<ObjectName, List<ListenerInfo>> patternSubscriptionMap = new HashMap<ObjectName, List<ListenerInfo>>();

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
        for (NodeMXBean child : children) {
            child.addNotificationListener(name, listener, filter, handback);
        }
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
        for (NodeMXBean child : children) {
            child.addNotificationListener(name, listener, filter, handback);
        }
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
        // TODO Auto-generated method stub
        return null;
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
        return null; // TODO
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
        // TODO Auto-generated method stub
        return null;
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
        return null; // TODO
    }

    /**
     * @return
     * @see javax.management.MBeanServer#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount() {
        return null; // TODO
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getMBeanCount(javax.management.ObjectName)
     */
    @Override
    public int getMBeanCount(ObjectName filter) {
        int count = 0;
        for (NodeMXBean child : children) {
            count += child.getMBeanCount(filter);
        }
        return count;
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
        return null;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#getObjectInstance(javax.management.ObjectName, javax.management.QueryExp)
     */
    @Override
    public Map<ObjectName, ObjectInstance> getObjectInstance(ObjectName name,
                                                             QueryExp queryExpr)
                                                                                throws InstanceNotFoundException,
                                                                                IOException {
        // TODO Auto-generated method stub
        return null;
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
        for (NodeMXBean child : children) {
            results.putAll(child.invoke(name, queryExpr, operationName, params,
                                        signature));
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
        return null; // TODO
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
        for (NodeMXBean child : children) {
            if (child.isInstanceOf(name, className)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param name
     * @return
     * @throws IOException
     * @see javax.management.MBeanServer#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered(ObjectName name) throws IOException {
        for (NodeMXBean child : children) {
            if (child.isRegistered(name)) {
                return true;
            }
        }
        return false;
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
        for (NodeMXBean child : children) {
            result.addAll(child.queryMBeans(name, query));
        }
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
        for (NodeMXBean child : children) {
            result.addAll(child.queryNames(name, query));
        }
        return result;
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
        for (NodeMXBean child : children) {
            child.removeNotificationListener(name, listener);
        }
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
        for (NodeMXBean child : children) {
            child.removeNotificationListener(name, listener, filter, handback);
        }
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
        for (NodeMXBean child : children) {
            child.removeNotificationListener(name, listener);
        }
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
        for (NodeMXBean child : children) {
            child.removeNotificationListener(name, listener, filter, handback);
        }
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
        for (NodeMXBean child : children) {
            child.setAttribute(name, attribute);
        }
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
        // TODO Auto-generated method stub

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
        AttributeList list = new AttributeList();
        for (NodeMXBean child : children) {
            list.addAll(child.setAttributes(name, attributes));
        }
        return list;
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMXBean#setAttributes(javax.management.ObjectName, javax.management.QueryExp, javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(ObjectName name, QueryExp queryExpr,
                                       AttributeList attributes)
                                                                throws InstanceNotFoundException,
                                                                ReflectionException,
                                                                IOException {
        // TODO Auto-generated method stub
        return null;
    }

    protected NotificationEmitter getNotificationEmitterFor(final ObjectName name)
                                                                                  throws InstanceNotFoundException {
        final NotificationEmitter emitter = new NotificationEmitter() {
            @Override
            public void addNotificationListener(NotificationListener listener,
                                                NotificationFilter filter,
                                                Object handback)
                                                                throws IllegalArgumentException {
                subscribe(name, listener, filter, handback);
            }

            @Override
            public MBeanNotificationInfo[] getNotificationInfo() {
                return null;
            }

            @Override
            public void removeNotificationListener(NotificationListener listener)
                                                                                 throws ListenerNotFoundException {
                unsubscribe(name, listener);
            }

            @Override
            public void removeNotificationListener(NotificationListener listener,
                                                   NotificationFilter filter,
                                                   Object handback)
                                                                   throws ListenerNotFoundException {
                unsubscribe(name, listener, filter, handback);
            }
        };
        return emitter;
    }

    protected void publish(ObjectName emitterName, Notification n) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("publish " + emitterName);
        }

        if (n == null) {
            throw new IllegalArgumentException("Null notification");
        }

        if (emitterName == null) {
            throw new IllegalArgumentException("Null emitter name");
        } else if (emitterName.isPattern()) {
            throw new IllegalArgumentException(
                                               "The emitter must not be an ObjectName pattern");
        }

        final List<ListenerInfo> listeners = new ArrayList<ListenerInfo>();

        synchronized (exactSubscriptionMap) {
            List<ListenerInfo> exactListeners = exactSubscriptionMap.get(emitterName);
            if (exactListeners != null) {
                listeners.addAll(exactListeners);
            }
        }
        synchronized (patternSubscriptionMap) {
            for (ObjectName on : patternSubscriptionMap.keySet()) {
                if (on.apply(emitterName)) {
                    listeners.addAll(patternSubscriptionMap.get(on));
                }
            }
        }

        sendNotif(listeners, n);
    }

    protected void subscribe(ObjectName name, NotificationListener listener,
                             NotificationFilter filter, Object handback) {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("subscribe " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Null listener");
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        final ListenerInfo li = new ListenerInfo(listener, filter, handback);
        List<ListenerInfo> list;

        synchronized (map) {
            list = map.get(name);
            if (list == null) {
                list = new ArrayList<ListenerInfo>();
                map.put(name, list);
            }
            list.add(li);
        }
    }

    protected void unsubscribe(ObjectName name, NotificationListener listener)
                                                                              throws ListenerNotFoundException {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("unsubscribe2 " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new ListenerNotFoundException();
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        final ListenerInfo li = new ListenerInfo(listener, null, null);
        List<ListenerInfo> list;
        synchronized (map) {
            list = map.get(name);
            if (list == null || !list.remove(li)) {
                throw new ListenerNotFoundException();
            }

            if (list.isEmpty()) {
                map.remove(name);
            }
        }
    }

    protected void unsubscribe(ObjectName name, NotificationListener listener,
                               NotificationFilter filter, Object handback)
                                                                          throws ListenerNotFoundException {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("unsubscribe4 " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new ListenerNotFoundException();
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        List<ListenerInfo> list;
        synchronized (map) {
            list = map.get(name);
            boolean removed = false;
            for (Iterator<ListenerInfo> it = list.iterator(); it.hasNext();) {
                ListenerInfo li = it.next();
                if (li.equals(listener, filter, handback)) {
                    it.remove();
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                throw new ListenerNotFoundException();
            }

            if (list.isEmpty()) {
                map.remove(name);
            }
        }
    }
}
