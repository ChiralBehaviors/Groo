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

import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ReflectionException;

/**
 * @author hhildebrand
 * 
 */
public interface NodeMXBean extends MBeanServerConnection {
    Map<ObjectName, Object> invoke(ObjectName name, QueryExp queryExpr,
                                   String operationName, Object params[],
                                   String signature[])
                                                      throws InstanceNotFoundException,
                                                      MBeanException,
                                                      ReflectionException,
                                                      IOException;

    int getMBeanCount(ObjectName filter);
}
