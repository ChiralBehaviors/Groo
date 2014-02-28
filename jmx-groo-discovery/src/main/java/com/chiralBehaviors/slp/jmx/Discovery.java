/** (C) Copyright 2014 Hal Hildebrand, All Rights Reserved
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
package com.chiralBehaviors.slp.jmx;

import com.chiralBehaviors.groo.configuration.ChakaalConfiguration;
import com.chiralBehaviors.groo.configuration.YamlHelper;
import com.hellblazer.utils.Utils;

/**
 * @author hhildebrand
 * 
 */
public class Discovery {
    public static String DEFAULT_CONFIG = "config.yml";

    public static void main(String[] argv) throws Exception {
        String resource = null;
        if (argv.length == 0) {
            resource = DEFAULT_CONFIG;
        } else if (argv.length == 1) {
            resource = argv[0];
        } else {
            System.err.println("Usage: Discovery <config resource>");
            System.exit(1);
        }
        ChakaalConfiguration config = YamlHelper.chakaalFromYaml(Utils.resolveResource(ChakaalConfiguration.class,
                                                                                       resource));
        config.construct();
    }
}
