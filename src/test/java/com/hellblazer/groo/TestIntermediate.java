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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Map;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import org.junit.Before;
import org.junit.Test;

/**
 * @author hhildebrand
 * 
 */
public class TestIntermediate {
    private Node       intermediate;
    private NodeMBean  leaf1;
    private NodeMBean  leaf2;
    private ObjectName leaf1Name;
    private ObjectName leaf2Name;
    private ObjectName intermediateName;
    private ObjectName test1a;
    private ObjectName test2a;
    private ObjectName multiTest1;
    private ObjectName multiTest2;
    private ObjectName test1b;
    private ObjectName test2b;

    @Before
    public void initialize() throws Exception {
        MBeanServer mbs1 = MBeanServerFactory.newMBeanServer();
        MBeanServer mbs2 = MBeanServerFactory.newMBeanServer();
        MBeanServer mbs3 = MBeanServerFactory.newMBeanServer();
        test1a = ObjectName.getInstance("MyDomain", "test1", "a");
        test1b = ObjectName.getInstance("MyDomain", "test1", "b");
        test2a = ObjectName.getInstance("MyDomain", "test2", "a");
        test2b = ObjectName.getInstance("MyDomain", "test2", "b");
        multiTest1 = ObjectName.getInstance("MyDomain", "test1", "*");
        multiTest2 = ObjectName.getInstance("MyDomain", "test2", "*");
        mbs1.registerMBean(new Test1(), test1a);
        mbs2.registerMBean(new Test1(), test1b);
        mbs1.registerMBean(new Test2(), test2a);
        mbs2.registerMBean(new Test2(), test2b);
        leaf1Name = ObjectName.getInstance("leaf-domain", "id", "1");
        leaf2Name = ObjectName.getInstance("leaf-domain", "id", "2");
        intermediateName = ObjectName.getInstance("intermediate-domain", "id",
                                                  "1");
        leaf1 = new Node();
        leaf2 = new Node();
        intermediate = new Node();
        mbs1.registerMBean(leaf1, leaf1Name);
        mbs2.registerMBean(leaf2, leaf2Name);
        mbs3.registerMBean(intermediate, intermediateName);
        intermediate.addChild(leaf1);
        intermediate.addChild(leaf2);
    }

    @Test
    public void testGetAttribute() throws Exception {
        Object result = intermediate.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(-1, result);
    }

    @Test
    public void testGetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", null));
        AttributeList result = intermediate.getAttributes(test1a, new String[] {
                "Attribute1", "Attribute2" });
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Attribute1", result.asList().get(0).getName());
        assertEquals("Attribute2", result.asList().get(1).getName());
        assertEquals(-1, result.asList().get(0).getValue());
        assertEquals(-2, result.asList().get(1).getValue());
    }

    @Test
    public void testInvoke() throws Exception {
        Object result = intermediate.invoke(test1a, "operation1", null, null);
        assertNotNull(result);
        assertEquals("-1", result);
    }

    @Test
    public void testMultiGetAttribute() throws Exception {
        Map<ObjectName, Object> result = intermediate.getAttribute(multiTest1,
                                                                   null,
                                                                   "Attribute1");
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Map.Entry<ObjectName, Object> entry : result.entrySet()) {
            assertEquals(-1, entry.getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));
    }

    @Test
    public void testMultiGetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", null));
        Map<ObjectName, AttributeList> result = intermediate.getAttributes(multiTest1,
                                                                           null,
                                                                           new String[] {
                                                                                   "Attribute1",
                                                                                   "Attribute2" });
        assertNotNull(result);
        assertEquals(2, result.size());
        for (AttributeList list : result.values()) {
            assertEquals("Attribute1", list.asList().get(0).getName());
            assertEquals("Attribute2", list.asList().get(1).getName());
            assertEquals(-1, list.asList().get(0).getValue());
            assertEquals(-2, list.asList().get(1).getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));
    }

    @Test
    public void testMultiInvoke() throws Exception {
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
    public void testMultiSetAttribute() throws Exception {
        intermediate.setAttribute(multiTest1, null, new Attribute("Attribute1",
                                                                  1));
        Object result = intermediate.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
        result = intermediate.getAttribute(test1b, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    public void testMultiSetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", 1));
        attrs.add(new Attribute("Attribute2", 2));
        Map<ObjectName, AttributeList> result = intermediate.setAttributes(multiTest1,
                                                                           null,
                                                                           attrs);
        assertEquals(2, result.size());
        for (AttributeList list : result.values()) {
            assertEquals("Attribute1", list.asList().get(0).getName());
            assertEquals("Attribute2", list.asList().get(1).getName());
            assertEquals(1, list.asList().get(0).getValue());
            assertEquals(2, list.asList().get(1).getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));
    }

    @Test
    public void testSetAttribute() throws Exception {
        intermediate.setAttribute(test1a, new Attribute("Attribute1", 1));
        Object result = intermediate.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    public void testSetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", 1));
        attrs.add(new Attribute("Attribute2", 2));
        AttributeList result = intermediate.setAttributes(test1a, attrs);
        assertEquals(2, result.size());
        assertEquals("Attribute1", result.asList().get(0).getName());
        assertEquals("Attribute2", result.asList().get(1).getName());
        assertEquals(1, result.asList().get(0).getValue());
        assertEquals(2, result.asList().get(1).getValue());
    }
}
