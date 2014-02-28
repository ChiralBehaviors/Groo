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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.hellblazer.nexus.config.GossipScopeModule;

/**
 * @author hhildebrand
 * 
 */
public class YamlHelper {

    public static GrooConfiguration grooFromYaml(File yaml)
                                                           throws JsonParseException,
                                                           JsonMappingException,
                                                           IOException {
        return grooFromYaml(new FileInputStream(yaml));
    }

    public static GrooConfiguration grooFromYaml(InputStream yaml)
                                                                  throws JsonParseException,
                                                                  JsonMappingException,
                                                                  IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new GossipScopeModule());
        return mapper.readValue(yaml, GrooConfiguration.class);
    }

    public static ChakaalConfiguration chakaalFromYaml(File yaml)
                                                                 throws JsonParseException,
                                                                 JsonMappingException,
                                                                 IOException {
        return chakaalFromYaml(new FileInputStream(yaml));
    }

    public static ChakaalConfiguration chakaalFromYaml(InputStream yaml)
                                                                        throws JsonParseException,
                                                                        JsonMappingException,
                                                                        IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        mapper.registerModule(new GossipScopeModule());
        return mapper.readValue(yaml, ChakaalConfiguration.class);
    }
}
