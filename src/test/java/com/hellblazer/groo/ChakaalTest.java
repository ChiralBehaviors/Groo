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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.util.UUID;

import javax.management.remote.JMXServiceURL;

import org.junit.Test;
import org.mockito.ArgumentCaptor;

import com.hellblazer.slp.ServiceEvent;
import com.hellblazer.slp.ServiceEvent.EventType;
import com.hellblazer.slp.ServiceListener;
import com.hellblazer.slp.ServiceReference;
import com.hellblazer.slp.ServiceScope;
import com.hellblazer.slp.ServiceType;
import com.hellblazer.slp.ServiceURL;

/**
 * @author hhildebrand
 * 
 */
public class ChakaalTest {

    @Test
    public void testListen() throws Exception {
        ServiceURL url = constructServiceURL(new JMXServiceURL(
                                                               "service:jmx:rmi://0.0.0.0:1024/stub/rO0ABXNy"));
        ServiceScope scope = mock(ServiceScope.class);
        Groo groo = mock(Groo.class);
        ServiceReference ref = mock(ServiceReference.class);
        when(ref.getUrl()).thenReturn(url);
        when(ref.getRegistration()).thenReturn(new UUID(0, 0));

        Chakaal chakaal = new Chakaal(groo, scope, null, null);
        String query = "hey there sailor";

        chakaal.listenFor(query);

        ArgumentCaptor<ServiceListener> captor = ArgumentCaptor.forClass(ServiceListener.class);
        verify(scope).addServiceListener(captor.capture(), eq(query));
        captor.getValue().serviceChanged(new ServiceEvent(EventType.REGISTERED,
                                                          ref));
        verify(groo).addConnection(any(BasicMbscFactory.class));
    }

    protected ServiceURL constructServiceURL(JMXServiceURL url)
                                                               throws MalformedURLException {
        StringBuilder builder = new StringBuilder();
        builder.append(ServiceType.SERVICE_PREFIX);
        builder.append("foo");
        builder.append(':');
        builder.append("jmx:");
        builder.append(url.getProtocol());
        builder.append("://");
        builder.append(url.getHost());
        builder.append(':');
        builder.append(url.getPort());
        builder.append(url.getURLPath());
        ServiceURL jmxServiceURL = new ServiceURL(builder.toString());
        return jmxServiceURL;
    }

}
