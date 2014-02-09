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

import java.io.Serializable;
import java.util.UUID;

import javax.management.BadAttributeValueExpException;
import javax.management.BadBinaryOpValueExpException;
import javax.management.BadStringOperationException;
import javax.management.InvalidApplicationException;
import javax.management.MBeanServerNotification;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.ObjectName;
import javax.management.QueryExp;

/**
 * @author hhildebrand
 * 
 */
public class RegistrationFilter implements NotificationFilter, Serializable {
    private static final long serialVersionUID = 1L;

    private UUID              handback;
    private ObjectName        sourcePattern;
    private QueryExp          sourceQuery;

    public RegistrationFilter() {
    }

    public RegistrationFilter(ObjectName sourcePattern, QueryExp sourceQuery) {
        this.sourcePattern = sourcePattern;
        this.sourceQuery = sourceQuery;
        this.handback = UUID.randomUUID();
    }

    /**
     * @return the handback
     */
    public UUID getHandback() {
        return handback;
    }

    /**
     * @return the sourcePattern
     */
    public ObjectName getSourcePattern() {
        return sourcePattern;
    }

    /**
     * @return the sourceQuery
     */
    public QueryExp getSourceQuery() {
        return sourceQuery;
    }

    /* (non-Javadoc)
     * @see javax.management.NotificationFilter#isNotificationEnabled(javax.management.Notification)
     */
    @Override
    public boolean isNotificationEnabled(Notification notification) {
        if (notification instanceof MBeanServerNotification) {
            final MBeanServerNotification n = (MBeanServerNotification) notification;
            final ObjectName sourceName = n.getMBeanName();
            try {
                if (sourcePattern.apply(sourceName)) {
                    if (sourceQuery != null) {
                        return sourceQuery.apply(sourceName);
                    } else {
                        return true;
                    }
                }
            } catch (BadStringOperationException | BadBinaryOpValueExpException
                    | BadAttributeValueExpException
                    | InvalidApplicationException e) {
                throw new IllegalStateException(e);
            }
        }
        return false;
    }
}
