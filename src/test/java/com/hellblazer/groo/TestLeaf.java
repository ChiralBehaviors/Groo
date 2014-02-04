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
public class TestLeaf {
    private Leaf        leaf;
    private MBeanServer mbs;
    private ObjectName  leafName;
    private ObjectName  test1a;
    private ObjectName  test2a;
    private ObjectName  multiTest1;
    private ObjectName  multiTest2;
    private ObjectName  test1b;
    private ObjectName  test2b;

    @Before
    public void initialize() throws Exception {
        mbs = MBeanServerFactory.newMBeanServer();
        test1a = ObjectName.getInstance("MyDomain", "test1", "a");
        test1b = ObjectName.getInstance("MyDomain", "test1", "b");
        test2a = ObjectName.getInstance("MyDomain", "test2", "a");
        test2b = ObjectName.getInstance("MyDomain", "test2", "b");
        multiTest1 = ObjectName.getInstance("MyDomain", "test1", "*");
        multiTest2 = ObjectName.getInstance("MyDomain", "test2", "*");
        mbs.registerMBean(new Test1(), test1a);
        mbs.registerMBean(new Test1(), test1b);
        mbs.registerMBean(new Test2(), test2a);
        mbs.registerMBean(new Test2(), test2b);
        leafName = ObjectName.getInstance("leaf-domain", "type", "leaf");
        leaf = new Leaf();
        mbs.registerMBean(leaf, leafName);
    }

    @Test
    public void testGetAttribute() throws Exception {
        Object result = leaf.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(-1, result);
    }

    @Test
    public void testSetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", 1));
        attrs.add(new Attribute("Attribute2", 2));
        AttributeList result = leaf.setAttributes(test1a, attrs);
        assertEquals(2, result.size());
        assertEquals("Attribute1", result.asList().get(0).getName());
        assertEquals("Attribute2", result.asList().get(1).getName());
        assertEquals(1, result.asList().get(0).getValue());
        assertEquals(2, result.asList().get(1).getValue());
    }

    @Test
    public void testMultiSetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", 1));
        attrs.add(new Attribute("Attribute2", 2));
        Map<ObjectName, AttributeList> result = leaf.setAttributes(multiTest1,
                                                                   null, attrs);
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
        leaf.setAttribute(test1a, new Attribute("Attribute1", 1));
        Object result = leaf.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    public void testMultiSetAttribute() throws Exception {
        leaf.setAttribute(multiTest1, null, new Attribute("Attribute1", 1));
        Object result = leaf.getAttribute(test1a, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
        result = leaf.getAttribute(test1b, "Attribute1");
        assertNotNull(result);
        assertEquals(1, result);
    }

    @Test
    public void testGetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", null));
        AttributeList result = leaf.getAttributes(test1a, new String[] {
                "Attribute1", "Attribute2" });
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Attribute1", result.asList().get(0).getName());
        assertEquals("Attribute2", result.asList().get(1).getName());
        assertEquals(-1, result.asList().get(0).getValue());
        assertEquals(-2, result.asList().get(1).getValue());
    }

    @Test
    public void testMultiGetAttributes() throws Exception {
        AttributeList attrs = new AttributeList();
        attrs.add(new Attribute("Attribute1", null));
        Map<ObjectName, AttributeList> result = leaf.getAttributes(multiTest1,
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
    public void testMultiGetAttribute() throws Exception {
        Map<ObjectName, Object> result = leaf.getAttribute(multiTest1, null,
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
    public void testInvoke() throws Exception {
        Object result = leaf.invoke(test1a, "operation1", null, null);
        assertNotNull(result);
        assertEquals("-1", result);
    }

    @Test
    public void testMultiInvoke() throws Exception {
        Map<ObjectName, Object> result = leaf.invoke(multiTest1, null,
                                                     "operation1", null, null);
        assertNotNull(result);
        assertEquals(2, result.size());
        for (Map.Entry<ObjectName, Object> entry : result.entrySet()) {
            assertEquals("-1", entry.getValue());
        }
        assertNotNull(result.get(test1a));
        assertNotNull(result.get(test1b));

        result = leaf.invoke(multiTest2, null, "operationFoo",
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
