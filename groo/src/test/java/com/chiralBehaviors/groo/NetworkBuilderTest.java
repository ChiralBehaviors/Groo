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

import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;

import com.hellblazer.utils.Condition;
import com.hellblazer.utils.Utils;

/**
 * @author hhildebrand
 * 
 */
public class NetworkBuilderTest {
    private Groo        groo1;
    private ObjectName  groo1Name;
    private Groo        groo2;
    private ObjectName  groo2Name;
    private Groo        groo3;
    private ObjectName  groo3Name;
    private MBeanServer mbs1;
    private MBeanServer mbs2;
    private MBeanServer mbs3;
    private MBeanServer mbs4;
    private MBeanServer mbs5;
    private MBeanServer mbs6;
    private MBeanServer mbs7;
    private ObjectName  multiTest1;
    private ObjectName  multiTest2;
    private ObjectName  test1a;
    private ObjectName  test1b;
    private ObjectName  test2a;
    private ObjectName  test2b;

    @Before
    public void initialize() throws Exception {
        groo1 = new Groo("groo 1");
        groo2 = new Groo("groo 2");
        groo3 = new Groo("groo 3");

        mbs1 = MBeanServerFactory.newMBeanServer();
        mbs2 = MBeanServerFactory.newMBeanServer();
        mbs3 = MBeanServerFactory.newMBeanServer();
        mbs4 = MBeanServerFactory.newMBeanServer();
        mbs5 = MBeanServerFactory.newMBeanServer();
        mbs6 = MBeanServerFactory.newMBeanServer();
        mbs7 = MBeanServerFactory.newMBeanServer();

        test1a = ObjectName.getInstance("MyDomain", "test1", "a");
        test1b = ObjectName.getInstance("MyDomain", "test1", "b");
        test2a = ObjectName.getInstance("MyDomain", "test2", "a");
        test2b = ObjectName.getInstance("MyDomain", "test2", "b");

        multiTest1 = ObjectName.getInstance("MyDomain", "test1", "*");
        multiTest2 = ObjectName.getInstance("MyDomain", "test2", "*");

        mbs1.registerMBean(new Test1(), test1a);
        mbs2.registerMBean(new Test1(), test1b);
        mbs3.registerMBean(new Test2(), test2a);
        mbs4.registerMBean(new Test2(), test2b);

        groo1Name = ObjectName.getInstance("groo-domain:id=1");
        groo2Name = ObjectName.getInstance("groo-domain:id=2");
        groo3Name = ObjectName.getInstance("groo-domain:id=3");

        mbs5.registerMBean(groo1, groo1Name);
        mbs6.registerMBean(groo2, groo2Name);
        mbs7.registerMBean(groo3, groo3Name);

        groo1.addConnection(new LocalMbscFactory(groo1, mbs6, "mbs 6"));
        groo1.addConnection(new LocalMbscFactory(groo1, mbs7, "mbs 7"));

        groo2.addConnection(new LocalMbscFactory(groo2, mbs1, "mbs 1"));
        groo2.addConnection(new LocalMbscFactory(groo2, mbs2, "mbs 2"));

        groo3.addConnection(new LocalMbscFactory(groo3, mbs3, "mbs 3"));
        groo3.addConnection(new LocalMbscFactory(groo3, mbs4, "mbs 4"));
        groo1.addNetworkBuilder(ObjectName.getInstance("management-domain:dc=*,r=*"),
                                null, new String[] { "dc" });
        groo2.addNetworkBuilder(ObjectName.getInstance("management-domain:dc=*,r=*,n=*"),
                                null, new String[] { "dc", "r" });
        groo3.addNetworkBuilder(ObjectName.getInstance("management-domain:dc=*,r=*,n=*"),
                                null, new String[] { "dc", "r" });
    }

    @Test
    public void testAssemble() throws Exception {

        ObjectName node1Name = ObjectName.getInstance("management-domain:dc=atlanta,r=1,n=1");
        ObjectName node2Name = ObjectName.getInstance("management-domain:dc=atlanta,r=1,n=2");
        ObjectName node3Name = ObjectName.getInstance("management-domain:dc=atlanta,r=2,n=3");
        ObjectName node4Name = ObjectName.getInstance("management-domain:dc=atlanta,r=2,n=4");

        Node node1 = new Node();
        Node node2 = new Node();
        Node node3 = new Node();
        Node node4 = new Node();
        mbs1.registerMBean(node1, node1Name);
        mbs2.registerMBean(node2, node2Name);
        mbs3.registerMBean(node3, node3Name);
        mbs4.registerMBean(node4, node4Name);

        assertTrue(Utils.waitForCondition(1000, 100, new Condition() {
            @Override
            public boolean isTrue() {
                return groo1.getParents().size() == 1
                       && groo2.getParents().size() == 1
                       && groo3.getParents().size() == 1;
            }
        }));

        final Node root = groo1.getParents().get(0);
        final Node child1 = groo2.getParents().get(0);
        final Node child2 = groo3.getParents().get(0);
        assertTrue(Utils.waitForCondition(1000, 100, new Condition() {
            @Override
            public boolean isTrue() {
                return root.getChildren().size() == 2
                       && child1.getChildren().size() == 2
                       && child2.getChildren().size() == 2;
            }
        }));

        Map<ObjectName, Object> result = root.invoke(multiTest1, null,
                                                     "operation1", null, null);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Map.Entry<ObjectName, Object> entry : result.entrySet()) {
            assertEquals("-1", entry.getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));

        result = root.invoke(multiTest2, null, "operationFoo",
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
}
