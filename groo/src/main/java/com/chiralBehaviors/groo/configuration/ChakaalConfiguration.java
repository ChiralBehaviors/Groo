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
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.management.InstanceAlreadyExistsException;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import com.chiralBehaviors.disovery.configuration.DiscoveryModule;
import com.chiralBehaviors.groo.Chakaal;
import com.chiralBehaviors.groo.Groo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellblazer.slp.InvalidSyntaxException;
import com.hellblazer.slp.ServiceScope;
import com.hellblazer.slp.config.ServiceScopeConfiguration;

/**
 * @author hhildebrand
 * 
 */
public class ChakaalConfiguration {
    public String                    chakaalName    = "com.chiralBehaviors.groo:type=chakaal";
    public ServiceScopeConfiguration discovery;
    public GrooConfiguration         groo           = new GrooConfiguration();
    public String                    grooName       = "com.chiralBehaviors.groo:type=groo";
    public List<String>              queries        = Collections.emptyList();
    public Map<String, String>       serviceQueries = Collections.emptyMap();
    public List<String>              services       = Collections.emptyList();
    public Map<String, ?>            sourceMap;
    public Subject                   subject;

    public static ChakaalConfiguration fromYaml(InputStream yaml)
                                                                 throws JsonParseException,
                                                                 JsonMappingException,
                                                                 IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new DiscoveryModule());
        return mapper.readValue(yaml, ChakaalConfiguration.class);
    }

    public Chakaal construct() throws Exception {
        return construct(ManagementFactory.getPlatformMBeanServer());
    }

    public Chakaal construct(MBeanServer mbs) throws Exception {
        return construct(mbs, groo.construct(), subject);
    }

    public Chakaal construct(MBeanServer mbs, Groo groo, Subject subject)
                                                                         throws Exception {
        ServiceScope scope = discovery.construct();
        return construct(mbs, groo, subject, scope);
    }

    public Chakaal construct(MBeanServer mbs, Groo groo, Subject subject,
                             ServiceScope scope) throws InvalidSyntaxException,
                                                InstanceAlreadyExistsException,
                                                MBeanRegistrationException,
                                                NotCompliantMBeanException,
                                                MalformedObjectNameException {
        Chakaal chakaal = new Chakaal(groo, scope, sourceMap, subject);
        for (String service : services) {
            chakaal.listenForService(service);
        }
        for (String query : queries) {
            chakaal.listenFor(query);
        }
        for (Map.Entry<String, String> entry : serviceQueries.entrySet()) {
            chakaal.listenForService(entry.getKey(), entry.getValue());
        }
        mbs.registerMBean(groo, ObjectName.getInstance(grooName));
        mbs.registerMBean(chakaal, ObjectName.getInstance(chakaalName));
        return chakaal;
    }
}
