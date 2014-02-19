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
import java.util.Map;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
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
public class MbscNodeWrapper implements NodeMBean {
    private final MbscFactory connectionFactory;
    private final ObjectName  source;

    /**
     * @param connectionFactory
     */
    public MbscNodeWrapper(MbscFactory connectionFactory, ObjectName source) {
        this.connectionFactory = connectionFactory;
        this.source = source;
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see com.hellblazer.groo.NodeMBean#addNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        getProxy().addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see com.hellblazer.groo.NodeMBean#addNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName, javax.management.NotificationFilter,
     *      java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        getProxy().addNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @see com.hellblazer.groo.NodeMBean#addNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        getProxy().addNotificationListener(name, queryExpr, listener, filter,
                                           handback);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#addNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.ObjectName,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException,
                                                        IOException {
        getProxy().addNotificationListener(name, queryExpr, listener, filter,
                                           handback);
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
        MbscNodeWrapper other = (MbscNodeWrapper) obj;
        if (source == null) {
            if (other.source != null) {
                return false;
            }
        } else if (!source.equals(other.source)) {
            return false;
        }
        return true;
    }

    /**
     * @param name
     * @param queryExpr
     * @param attribute
     * @return
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#getAttribute(javax.management.ObjectName,
     *      javax.management.QueryExp, java.lang.String)
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
        return getProxy().getAttribute(name, queryExpr, attribute);
    }

    /**
     * @param name
     * @param attribute
     * @return
     * @throws MBeanException
     * @throws AttributeNotFoundException
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see com.hellblazer.groo.NodeMBean#getAttribute(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public Object getAttribute(ObjectName name, String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException {
        return getProxy().getAttribute(name, attribute);
    }

    /**
     * @param name
     * @param queryExpr
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#getAttributes(javax.management.ObjectName,
     *      javax.management.QueryExp, java.lang.String[])
     */
    @Override
    public Map<ObjectName, AttributeList> getAttributes(ObjectName name,
                                                        QueryExp queryExpr,
                                                        String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException,
                                                                            IOException {
        return getProxy().getAttributes(name, queryExpr, attributes);
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @see com.hellblazer.groo.NodeMBean#getAttributes(javax.management.ObjectName,
     *      java.lang.String[])
     */
    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException {
        return getProxy().getAttributes(name, attributes);
    }

    /**
     * @return
     * @see com.hellblazer.groo.NodeMBean#getMBeanCount()
     */
    @Override
    public Integer getMBeanCount() {
        return getProxy().getMBeanCount();
    }

    /**
     * @param filter
     * @return
     * @see com.hellblazer.groo.NodeMBean#getMBeanCount(javax.management.ObjectName,
     *      QueryExpr)
     */
    @Override
    public int getMBeanCount(ObjectName filter, QueryExp queryExpr) {
        return getProxy().getMBeanCount(filter, queryExpr);
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @throws IntrospectionException
     * @throws ReflectionException
     * @see com.hellblazer.groo.NodeMBean#getMBeanInfo(javax.management.ObjectName)
     */
    @Override
    public MBeanInfo getMBeanInfo(ObjectName name)
                                                  throws InstanceNotFoundException,
                                                  IntrospectionException,
                                                  ReflectionException {
        return getProxy().getMBeanInfo(name);
    }

    /**
     * @return
     * @see com.hellblazer.groo.NodeMBean#getName()
     */
    @Override
    public ObjectName getName() {
        return source;
    }

    /**
     * @param name
     * @return
     * @throws InstanceNotFoundException
     * @see com.hellblazer.groo.NodeMBean#getObjectInstance(javax.management.ObjectName)
     */
    @Override
    public ObjectInstance getObjectInstance(ObjectName name)
                                                            throws InstanceNotFoundException {
        return getProxy().getObjectInstance(name);
    }

    /**
     * @param name
     * @param queryExpr
     * @return
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#getObjectInstances(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> getObjectInstances(ObjectName name,
                                                  QueryExp queryExpr)
                                                                     throws InstanceNotFoundException,
                                                                     IOException {
        return getProxy().getObjectInstances(name, queryExpr);
    }

    /* (non-Javadoc)
     * @see com.hellblazer.groo.NodeMBean#hasChild(javax.management.ObjectName)
     */
    public boolean hasChild(ObjectName nodeName) {
        return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (source == null ? 0 : source.hashCode());
        return result;
    }

    /**
     * @param name
     * @param queryExpr
     * @param operationName
     * @param params
     * @param signature
     * @return
     * @throws InstanceNotFoundException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#invoke(javax.management.ObjectName,
     *      javax.management.QueryExp, java.lang.String, java.lang.Object[],
     *      java.lang.String[])
     */
    @Override
    public Map<ObjectName, Object> invoke(ObjectName name, QueryExp queryExpr,
                                          String operationName,
                                          Object[] params, String[] signature)
                                                                              throws InstanceNotFoundException,
                                                                              MBeanException,
                                                                              ReflectionException,
                                                                              IOException {
        return getProxy().invoke(name, queryExpr, operationName, params,
                                 signature);
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
     * @see com.hellblazer.groo.NodeMBean#invoke(javax.management.ObjectName,
     *      java.lang.String, java.lang.Object[], java.lang.String[])
     */
    @Override
    public Object invoke(ObjectName name, String operationName,
                         Object[] params, String[] signature)
                                                             throws InstanceNotFoundException,
                                                             MBeanException,
                                                             ReflectionException {
        return getProxy().invoke(name, operationName, params, signature);
    }

    /**
     * @param name
     * @param className
     * @return
     * @throws InstanceNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#isInstanceOf(javax.management.ObjectName,
     *      java.lang.String)
     */
    @Override
    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException,
                                                                  IOException {
        return getProxy().isInstanceOf(name, className);
    }

    /**
     * @param name
     * @return
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#isRegistered(javax.management.ObjectName)
     */
    @Override
    public boolean isRegistered(ObjectName name) throws IOException {
        return getProxy().isRegistered(name);
    }

    /**
     * @param name
     * @param query
     * @return
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#queryMBeans(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
                                                                           throws IOException {
        return getProxy().queryMBeans(name, query);
    }

    /**
     * @param name
     * @param query
     * @return
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#queryNames(javax.management.ObjectName,
     *      javax.management.QueryExp)
     */
    @Override
    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
                                                                      throws IOException {
        return getProxy().queryNames(name, query);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException {
        getProxy().removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
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
        getProxy().removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(ObjectName name, ObjectName listener)
                                                                                throws InstanceNotFoundException,
                                                                                ListenerNotFoundException,
                                                                                IOException {
        getProxy().removeNotificationListener(name, listener);
    }

    /**
     * @param name
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
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
        getProxy().removeNotificationListener(name, listener, filter, handback);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.NotificationListener)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException {
        getProxy().removeNotificationListener(name, queryExpr, listener);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.NotificationListener,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        getProxy().removeNotificationListener(name, queryExpr, listener,
                                              filter, handback);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.ObjectName)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener)
                                                               throws InstanceNotFoundException,
                                                               ListenerNotFoundException,
                                                               IOException {
        getProxy().removeNotificationListener(name, queryExpr, listener);
    }

    /**
     * @param name
     * @param queryExpr
     * @param listener
     * @param filter
     * @param handback
     * @throws InstanceNotFoundException
     * @throws ListenerNotFoundException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#removeNotificationListener(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.ObjectName,
     *      javax.management.NotificationFilter, java.lang.Object)
     */
    @Override
    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException {
        getProxy().removeNotificationListener(name, queryExpr, listener,
                                              filter, handback);
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
     * @see com.hellblazer.groo.NodeMBean#setAttribute(javax.management.ObjectName,
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
        getProxy().setAttribute(name, attribute);
    }

    /**
     * @param name
     * @param queryExpr
     * @param attribute
     * @throws InstanceNotFoundException
     * @throws AttributeNotFoundException
     * @throws InvalidAttributeValueException
     * @throws MBeanException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#setAttribute(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.Attribute)
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
        getProxy().setAttribute(name, queryExpr, attribute);
    }

    /**
     * @param name
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#setAttributes(javax.management.ObjectName,
     *      javax.management.AttributeList)
     */
    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException {
        return getProxy().setAttributes(name, attributes);
    }

    /**
     * @param name
     * @param queryExpr
     * @param attributes
     * @return
     * @throws InstanceNotFoundException
     * @throws ReflectionException
     * @throws IOException
     * @see com.hellblazer.groo.NodeMBean#setAttributes(javax.management.ObjectName,
     *      javax.management.QueryExp, javax.management.AttributeList)
     */
    @Override
    public Map<ObjectName, AttributeList> setAttributes(ObjectName name,
                                                        QueryExp queryExpr,
                                                        AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException {
        return getProxy().setAttributes(name, queryExpr, attributes);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "MbscNodeWrapper [source=" + source + "]";
    }

    private MBeanServerConnection getConnection() {
        try {
            return connectionFactory.getMBeanServerConnection();
        } catch (IOException e) {
            throw new IllegalStateException(
                                            String.format("Cannot get connection for: %s",
                                                          connectionFactory));
        }
    }

    /**
     * @return the proxy
     */
    private NodeMBean getProxy() {
        return MBeanServerInvocationHandler.newProxyInstance(getConnection(),
                                                             source,
                                                             NodeMBean.class,
                                                             false);
    }
}
