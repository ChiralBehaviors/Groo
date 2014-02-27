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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorServer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.chiralBehaviors.groo.BasicMbscFactory;
import com.chiralBehaviors.groo.Groo;
import com.chiralBehaviors.groo.Node;
import com.chiralBehaviors.groo.NodeMBean;
import com.hellblazer.utils.Condition;
import com.hellblazer.utils.Utils;
import com.hellblazer.utils.jmx.RmiJmxServerFactory;

/**
 * @author hhildebrand
 * 
 */
public class RemoteTest {
    private Node               intermediate;
    private MBeanServer        intermediateMbs;
    private ObjectName         intermediateName;
    private NodeMBean          leaf1;
    private JMXConnector       leaf1Connector;
    private MBeanServer        leaf1Mbs;
    private ObjectName         leaf1Name;
    private JMXConnectorServer leaf1Server;
    private NodeMBean          leaf2;
    private JMXConnector       leaf2Connector;
    private MBeanServer        leaf2Mbs;
    private ObjectName         leaf2Name;
    private JMXConnectorServer leaf2Server;
    private ObjectName         multiTest1;
    private ObjectName         multiTest2;
    private ObjectName         test1a;
    private ObjectName         test1b;
    private ObjectName         test2a;
    private ObjectName         test2b;

    @After
    public void cleanUp() throws IOException {
        leaf1Server.stop();
        leaf2Server.stop();
    }

    @Before
    public void initialize() throws Exception {
        leaf1Mbs = MBeanServerFactory.newMBeanServer();
        leaf2Mbs = MBeanServerFactory.newMBeanServer();
        intermediateMbs = MBeanServerFactory.newMBeanServer();
        test1a = ObjectName.getInstance("MyDomain", "test1", "a");
        test1b = ObjectName.getInstance("MyDomain", "test1", "b");
        test2a = ObjectName.getInstance("MyDomain", "test2", "a");
        test2b = ObjectName.getInstance("MyDomain", "test2", "b");
        multiTest1 = ObjectName.getInstance("MyDomain", "test1", "*");
        multiTest2 = ObjectName.getInstance("MyDomain", "test2", "*");
        leaf1Mbs.registerMBean(new Test1(), test1a);
        leaf2Mbs.registerMBean(new Test1(), test1b);
        leaf1Mbs.registerMBean(new Test2(), test2a);
        leaf2Mbs.registerMBean(new Test2(), test2b);
        leaf1Name = ObjectName.getInstance("leaf-domain", "id", "1");
        leaf2Name = ObjectName.getInstance("leaf-domain", "id", "2");
        intermediateName = ObjectName.getInstance("intermediate-domain", "id",
                                                  "1");
        leaf1 = new Node();
        leaf2 = new Node();
        intermediate = new Node(
                                ObjectName.getInstance("leaf-domain", "id", "*"),
                                null);
        intermediateMbs.registerMBean(intermediate, intermediateName);
        leaf1Server = RmiJmxServerFactory.contruct(new InetSocketAddress(
                                                                         Utils.allocatePort()),
                                                   leaf1Mbs);
        leaf1Server.start();
        leaf1Connector = leaf1Server.toJMXConnector(new HashMap<String, Object>());
        leaf2Server = RmiJmxServerFactory.contruct(new InetSocketAddress(
                                                                         Utils.allocatePort()),
                                                   leaf2Mbs);
        leaf2Server.start();
        leaf2Connector = leaf2Server.toJMXConnector(new HashMap<String, Object>());
    }

    @Test
    public void testChildrenDelegation() throws Exception {
        Groo groo = new Groo("Groo the wanderer");
        intermediateMbs.registerMBean(groo,
                                      ObjectName.getInstance("groo", "id", "1"));
        groo.addParent(intermediate);
        leaf1Mbs.registerMBean(leaf1, leaf1Name);
        leaf1Connector.connect();
        groo.addConnection(new BasicMbscFactory(groo, leaf1Connector, null));
        leaf2Mbs.registerMBean(leaf2, leaf2Name);
        leaf2Connector.connect();
        groo.addConnection(new BasicMbscFactory(groo, leaf2Connector, null));
        assertEquals(2, intermediate.getChildren().size());
        Map<ObjectName, Object> result = intermediate.invoke(multiTest1, null,
                                                             "operation1",
                                                             null, null);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Map.Entry<ObjectName, Object> entry : result.entrySet()) {
            assertEquals("-1", entry.getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));

        result = intermediate.invoke(multiTest2,
                                     null,
                                     "operationFoo",
                                     new Object[] { "testy" },
                                     new String[] { String.class.getCanonicalName() });
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Map.Entry<ObjectName, Object> entry : result.entrySet()) {
            assertEquals("testy", entry.getValue());
        }
        assertNotNull(result.get(test2a));
        assertNotNull(result.get(test2b));
    }

    @Test
    public void testRegistrationNotifications() throws Exception {
        Groo groo = new Groo("Groo the wanderer");
        intermediateMbs.registerMBean(groo,
                                      ObjectName.getInstance("groo", "id", "1"));
        groo.addParent(intermediate);
        assertEquals(0, intermediate.getChildren().size());
        leaf1Connector.connect();
        groo.addConnection(new BasicMbscFactory(groo, leaf1Connector, null));
        assertEquals(0, intermediate.getChildren().size());
        leaf1Mbs.registerMBean(leaf1, leaf1Name);
        assertTrue(Utils.waitForCondition(1000, new Condition() {
            @Override
            public boolean isTrue() {
                return intermediate.getChildren().size() > 0;
            }
        }));
        assertEquals(1, intermediate.getChildren().size());
        leaf2Mbs.registerMBean(leaf2, leaf2Name);
        leaf2Connector.connect();
        groo.addConnection(new BasicMbscFactory(groo, leaf2Connector, null));
        assertEquals(2, intermediate.getChildren().size());
    }

}
