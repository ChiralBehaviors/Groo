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

/**
 * @author hhildebrand
 * 
 */
public interface GrooMBean {

    /**
     * A human readable string describing this aggregating agent.
     * 
     * @return A human readable string describing this aggregating agent.
     **/
    public String getDescription();

    /**
     * Stops the aggregation.
     * <p>
     * When this method completes, the NodeMBeans that were aggregated by this
     * <tt>Groo</tt> will no longer be mounted in the aggregating
     * <tt>MBeanServer</tt>. After a successful invocation of <tt>stop()</tt>,
     * the <tt>Groo</tt> becomes inactive (see {@link #isActive isActive()}).
     * </p>
     * 
     * @exception IOException
     *                if cascading couldn't be stopped.
     * @exception IllegalStateException
     *                if this cascading agent is not in a state where it can be
     *                stopped (for instance, a start operation is still in
     *                progress). The exact cases where
     *                <tt>IllegalStateException</tt> can be thrown is
     *                implementation dependent.
     * @see #start
     * @see #isActive
     */
    public void stop() throws IOException;
}
