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
public interface NodeMBean {
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException;

    public void addNotificationListener(ObjectName name, ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException;

    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException;

    public void addNotificationListener(ObjectName name, QueryExp queryExpr,
                                        ObjectName listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException,
                                                        IOException;

    public <T> Map<ObjectName, OperationResult<T>> getAttribute(ObjectName name,
                                                                QueryExp queryExpr,
                                                                String attribute)
                                                                                 throws MBeanException,
                                                                                 AttributeNotFoundException,
                                                                                 InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException;

    public Object getAttribute(ObjectName name, String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException;

    public Map<ObjectName, OperationResult<AttributeList>> getAttributes(ObjectName name,
                                                                         QueryExp queryExpr,
                                                                         String[] attributes)
                                                                                             throws InstanceNotFoundException,
                                                                                             ReflectionException,
                                                                                             IOException;

    public AttributeList getAttributes(ObjectName name, String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException;

    public Integer getMBeanCount();

    public MBeanInfo getMBeanInfo(ObjectName name)
                                                  throws InstanceNotFoundException,
                                                  IntrospectionException,
                                                  ReflectionException;

    public abstract ObjectName getName();

    public abstract ObjectInstance getObjectInstance(ObjectName name)
                                                                     throws InstanceNotFoundException;

    public Set<ObjectInstance> getObjectInstances(ObjectName name,
                                                  QueryExp queryExpr)
                                                                     throws InstanceNotFoundException,
                                                                     IOException;

    public Object invoke(ObjectName name, String operationName,
                         Object[] params, String[] signature)
                                                             throws InstanceNotFoundException,
                                                             MBeanException,
                                                             ReflectionException;

    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException,
                                                                  IOException;

    public boolean isRegistered(ObjectName name) throws IOException;

    public Set<ObjectInstance> queryMBeans(ObjectName name, QueryExp query)
                                                                           throws IOException;

    public Set<ObjectName> queryNames(ObjectName name, QueryExp query)
                                                                      throws IOException;

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException;

    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException;

    public void removeNotificationListener(ObjectName name, ObjectName listener)
                                                                                throws InstanceNotFoundException,
                                                                                ListenerNotFoundException,
                                                                                IOException;

    public void removeNotificationListener(ObjectName name,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException;

    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException,
                                                                         IOException;

    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException;

    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener)
                                                               throws InstanceNotFoundException,
                                                               ListenerNotFoundException,
                                                               IOException;

    public void removeNotificationListener(ObjectName name, QueryExp queryExpr,
                                           ObjectName listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException,
                                                           IOException;

    public void setAttribute(ObjectName name, Attribute attribute)
                                                                  throws InstanceNotFoundException,
                                                                  AttributeNotFoundException,
                                                                  InvalidAttributeValueException,
                                                                  MBeanException,
                                                                  ReflectionException,
                                                                  IOException;

    public void setAttribute(ObjectName name, QueryExp queryExpr,
                             Attribute attribute)
                                                 throws InstanceNotFoundException,
                                                 AttributeNotFoundException,
                                                 InvalidAttributeValueException,
                                                 MBeanException,
                                                 ReflectionException,
                                                 IOException;

    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException,
                                                                                 IOException;

    public Map<ObjectName, OperationResult<AttributeList>> setAttributes(ObjectName name,
                                                                         QueryExp queryExpr,
                                                                         AttributeList attributes)
                                                                                                  throws InstanceNotFoundException,
                                                                                                  ReflectionException,
                                                                                                  IOException;

    int getMBeanCount(ObjectName filter, QueryExp queryExpr);

    <T> Map<ObjectName, OperationResult<T>> invoke(ObjectName name,
                                                   QueryExp queryExpr,
                                                   String operationName,
                                                   Object params[],
                                                   String signature[])
                                                                      throws InstanceNotFoundException,
                                                                      MBeanException,
                                                                      ReflectionException,
                                                                      IOException;
}
