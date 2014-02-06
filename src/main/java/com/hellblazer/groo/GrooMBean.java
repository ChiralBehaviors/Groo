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

import javax.management.InstanceAlreadyExistsException;
import javax.management.ObjectName;
import javax.management.QueryExp;

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
     * Returns the source {@link ObjectName} pattern filter that the source
     * NodeMBean names must satisfy in order to be aggregated. This pattern is
     * to be evaluated in the context of the source MBeanServer.
     * 
     * @return the source <tt>ObjectName</tt> pattern filter.
     */
    public ObjectName getPattern();

    /**
     * Returns the source {@link QueryExp} query filter that the source
     * NodeMBean names must satisfy in order to be aggregated. This query is to
     * be evaluated in the context of the source MBeanServer.
     * 
     * @return the source <tt>QueryExp</tt> query filter.
     */
    public QueryExp getQuery();

    /**
     * Tests if the <CODE>Groo</CODE> is active.
     * 
     * @return <code>true</code> if the cascading agent is active.
     */
    public boolean isActive();

    /**
     * Starts this aggregating agent.
     * <p>
     * When this method successfully completes, the source NodeMBeans from the
     * source (aggregated) MBeanServer which satisfy the source
     * <tt>ObjectName</tt> {@link #getPattern pattern} filter and the source
     * <tt>QueryExp</tt> {@link #getQuery query} filter will have been mounted
     * in the target (aggregating) <tt>MBeanServer</tt> under the specified
     * {@link #getTargetPath targetPath}. <br>
     * After a successful invocation of <tt>start()</tt>, the <tt>Groo</tt>
     * becomes active (see {@link GrooMBean#isActive isActive()}).
     * </p>
     * <p>
     * <tt>Groos</tt> may be started and stopped multiple times, long as their
     * underlying {@link MBeanServerConnectionFactory} is able to return valid
     * <tt>MBeanServerConnections</tt>.
     * </p>
     * <p>
     * If this method raises an exception, then no MBeans will have been
     * cascaded as a result of this invocation.
     * </p>
     * 
     * 
     * @exception IOException
     *                if the connection with the source <tt>MBeanServer</tt>
     *                fails.
     * @exception IllegalStateException
     *                if this cascading agent is not stopped, or if the target
     *                <tt>MBeanServer</tt> can't be obtained (e.g. the
     *                <tt>Groo</tt> MBean was not registered).
     * @exception InstanceAlreadyExistsException
     *                if a name conflict is detected while starting.
     * 
     * @see CascadingAgentMBean#start
     **/
    public void start() throws IOException, InstanceAlreadyExistsException;

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
