/*
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

package com.chiralBehaviors.groo.configuration;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import com.chiralBehaviors.groo.Groo;

/**
 * @author hhildebrand
 * 
 */
public class GrooConfiguration {
    public String                            description     = "Every day I be wandering...";
    public List<NetworkBuilderConfiguration> networkBuilders = Collections.emptyList();

    public Groo construct() throws MalformedObjectNameException,
                           NullPointerException, IOException {
        Groo groo = new Groo(description);
        for (NetworkBuilderConfiguration config : networkBuilders) {
            groo.addNetworkBuilder(ObjectName.getInstance(config.networkPattern),
                                   null, config.parentProperties);
        }
        return groo;
    }
}
