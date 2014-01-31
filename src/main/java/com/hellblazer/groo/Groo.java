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

import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.AttributeNotFoundException;
import javax.management.DynamicMBean;
import javax.management.InstanceAlreadyExistsException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.JMRuntimeException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanRegistrationException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.NotCompliantMBeanException;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.management.QueryEval;
import javax.management.QueryExp;
import javax.management.ReflectionException;
import javax.management.RuntimeOperationsException;
import javax.management.loading.ClassLoaderRepository;

import com.sun.jmx.mbeanserver.Repository;
import com.sun.jmx.mbeanserver.Repository.RegistrationContext;

@SuppressWarnings("restriction")
public class Groo implements GrooMBean {
    /*
    * A RegistrationContext that makes it possible to perform additional
    * post registration actions (or post unregistration actions) outside
    * of the repository lock, once postRegister (or postDeregister) has
    * been called.
    * The method {@code done()} will be called in registerMBean or
    * unregisterMBean, at the end.
    */
    @SuppressWarnings("unused")
    private static interface ResourceContext extends RegistrationContext {
        public void done();

        /** An empty ResourceContext which does nothing **/
        public static final ResourceContext NONE = new ResourceContext() {
                                                     public void done() {
                                                     }

                                                     public void registering() {
                                                     }

                                                     public void unregistered() {
                                                     }
                                                 };
    }

    private final static class SingleClassLoaderRepository implements
            ClassLoaderRepository {
        private final ClassLoader singleLoader;

        SingleClassLoaderRepository(ClassLoader loader) {
            singleLoader = loader;
        }

        @Override
        public Class<?> loadClass(String className)
                                                   throws ClassNotFoundException {
            return loadClass(className, getSingleClassLoader());
        }

        @Override
        public Class<?> loadClassBefore(ClassLoader stop, String className)
                                                                           throws ClassNotFoundException {
            return loadClassWithout(stop, className);
        }

        @Override
        public Class<?> loadClassWithout(ClassLoader exclude, String className)
                                                                               throws ClassNotFoundException {
            final ClassLoader loader = getSingleClassLoader();
            if (exclude != null && exclude.equals(loader)) {
                throw new ClassNotFoundException(className);
            }
            return loadClass(className, loader);
        }

        private Class<?> loadClass(String className, ClassLoader loader)
                                                                        throws ClassNotFoundException {
            return Class.forName(className, false, loader);
        }

        ClassLoader getSingleClassLoader() {
            return singleLoader;
        }
    }

    private static final Logger LOG = Logger.getLogger(Groo.class.getCanonicalName());

    public static <T> Set<T> equivalentEmptySet(Set<T> set) {
        if (set instanceof SortedSet) {
            SortedSet<T> sset = (SortedSet<T>) set;
            set = new TreeSet<T>(sset.comparator());
        } else {
            set = new HashSet<T>();
        }
        return set;
    }

    /**
     * Filters a set of ObjectName according to a given pattern.
     * 
     * @param pattern
     *            the pattern that the returned names must match.
     * @param all
     *            the set of names to filter.
     * @return a set of ObjectName from which non matching names have been
     *         removed.
     */
    public static Set<ObjectName> filterMatchingNames(ObjectName pattern,
                                                      Set<ObjectName> all) {
        if (pattern == null || all.isEmpty()
            || ObjectName.WILDCARD.equals(pattern)) {
            return all;
        }

        final Set<ObjectName> res = equivalentEmptySet(all);
        for (ObjectName n : all) {
            if (pattern.apply(n)) {
                res.add(n);
            }
        }
        return res;
    }

    public static ClassLoaderRepository getSingleClassLoaderRepository(final ClassLoader loader) {
        return new SingleClassLoaderRepository(loader);
    }

    private final static boolean apply(final QueryExp query,
                                       final ObjectName on,
                                       final MBeanServer srv) {
        boolean res = false;
        MBeanServer oldServer = QueryEval.getMBeanServer();
        query.setMBeanServer(srv);
        try {
            res = query.apply(on);
        } catch (Exception e) {
            LOG.finest("QueryExp.apply threw exception, returning false."
                       + " Cause: " + e);
            res = false;
        } finally {
            query.setMBeanServer(oldServer);
        }
        return res;
    }

    static RuntimeException newIllegalArgumentException(String msg) {
        return new RuntimeOperationsException(new IllegalArgumentException(msg));
    }

    static RuntimeException newUnsupportedException(String operation) {
        return new RuntimeOperationsException(
                                              new UnsupportedOperationException(
                                                                                operation
                                                                                        + ": Not supported in this namespace"));
    }

    private final VirtualEventManager vem = new VirtualEventManager();
    private final Repository          repository;

    public Groo(String domain) {
        repository = new Repository(domain);
    }

    @Override
    public void addNotificationListener(ObjectName name,
                                        NotificationListener listener,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        final NotificationEmitter emitter = getNonNullNotificationEmitterFor(name);
        emitter.addNotificationListener(listener, filter, handback);
    }

    @Override
    public void addNotificationListener(ObjectName name,
                                        ObjectName listenerName,
                                        NotificationFilter filter,
                                        Object handback)
                                                        throws InstanceNotFoundException {
        NotificationListener listener = getListenerMBean(listenerName);
        addNotificationListener(name, listener, filter, handback);
    }

    @Override
    public final ObjectInstance createMBean(String className, ObjectName name)
                                                                              throws ReflectionException,
                                                                              InstanceAlreadyExistsException,
                                                                              MBeanRegistrationException,
                                                                              MBeanException,
                                                                              NotCompliantMBeanException {
        try {
            return safeCreateMBean(className, name, null, null, null, true);
        } catch (InstanceNotFoundException ex) {
            throw new MBeanException(ex, "Unexpected exception: " + ex);
        }
    }

    @Override
    public final ObjectInstance createMBean(String className, ObjectName name,
                                            Object[] params, String[] signature)
                                                                                throws ReflectionException,
                                                                                InstanceAlreadyExistsException,
                                                                                MBeanRegistrationException,
                                                                                MBeanException,
                                                                                NotCompliantMBeanException {
        try {
            return safeCreateMBean(className, name, null, params, signature,
                                   true);
        } catch (InstanceNotFoundException ex) {
            throw new MBeanException(ex, "Unexpected exception: " + ex);
        }
    }

    @Override
    public final ObjectInstance createMBean(String className, ObjectName name,
                                            ObjectName loaderName)
                                                                  throws ReflectionException,
                                                                  InstanceAlreadyExistsException,
                                                                  MBeanRegistrationException,
                                                                  MBeanException,
                                                                  NotCompliantMBeanException,
                                                                  InstanceNotFoundException {
        return safeCreateMBean(className, name, loaderName, null, null, false);
    }

    @Override
    public final ObjectInstance createMBean(String className, ObjectName name,
                                            ObjectName loaderName,
                                            Object[] params, String[] signature)
                                                                                throws ReflectionException,
                                                                                InstanceAlreadyExistsException,
                                                                                MBeanRegistrationException,
                                                                                MBeanException,
                                                                                NotCompliantMBeanException,
                                                                                InstanceNotFoundException {
        return safeCreateMBean(className, name, loaderName, params, signature,
                               false);
    }

    public ObjectInstance createMBean(String className, ObjectName name,
                                      ObjectName loaderName, Object[] params,
                                      String[] signature, boolean useCLR)
                                                                         throws ReflectionException,
                                                                         InstanceAlreadyExistsException,
                                                                         MBeanRegistrationException,
                                                                         MBeanException,
                                                                         NotCompliantMBeanException,
                                                                         InstanceNotFoundException {
        throw newUnsupportedException("createMBean");
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(ObjectName name, byte[] data)
                                                                      throws InstanceNotFoundException,
                                                                      OperationsException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(String className, byte[] data)
                                                                       throws OperationsException,
                                                                       ReflectionException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    @Deprecated
    public ObjectInputStream deserialize(String className,
                                         ObjectName loaderName, byte[] data)
                                                                            throws InstanceNotFoundException,
                                                                            OperationsException,
                                                                            ReflectionException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    public Object getAttribute(ObjectName name, String attribute)
                                                                 throws MBeanException,
                                                                 AttributeNotFoundException,
                                                                 InstanceNotFoundException,
                                                                 ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        return mbean.getAttribute(attribute);
    }

    @Override
    public AttributeList getAttributes(ObjectName name, String[] attributes)
                                                                            throws InstanceNotFoundException,
                                                                            ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        return mbean.getAttributes(attributes);
    }

    @Override
    public ClassLoader getClassLoader(ObjectName loaderName)
                                                            throws InstanceNotFoundException {
        final UnsupportedOperationException failed = new UnsupportedOperationException(
                                                                                       "getClassLoader");
        final InstanceNotFoundException x = new InstanceNotFoundException(
                                                                          String.valueOf(loaderName));
        x.initCause(failed);
        throw x;
    }

    @Override
    public ClassLoader getClassLoaderFor(ObjectName mbeanName)
                                                              throws InstanceNotFoundException {
        final DynamicMBean mbean = nonNullMBeanFor(mbeanName);
        return mbean.getClass().getClassLoader();
    }

    @Override
    public ClassLoaderRepository getClassLoaderRepository() {
        ClassLoader ccl = Thread.currentThread().getContextClassLoader();
        return getSingleClassLoaderRepository(ccl);
    }

    @Override
    public String getDefaultDomain() {
        return "DefaultDomain";
    }

    @Override
    public String[] getDomains() {
        final Set<ObjectName> names = getNames();
        final Set<String> res = new TreeSet<String>();
        for (ObjectName n : names) {
            if (n == null) {
                continue; // not allowed but you never know.
            }
            res.add(n.getDomain());
        }
        return res.toArray(new String[res.size()]);
    }

    public DynamicMBean getDynamicMBeanFor(ObjectName name)
                                                           throws InstanceNotFoundException {
        DynamicMBean instance = repository.retrieve(name);
        if (instance != null) {
            return instance;
        } else {
            throw new InstanceNotFoundException(
                                                String.format("The instance %s was not fount",
                                                              name));
        }
    }

    @Override
    public Integer getMBeanCount() {
        return getNames().size();
    }

    @Override
    public MBeanInfo getMBeanInfo(ObjectName name)
                                                  throws InstanceNotFoundException,
                                                  IntrospectionException,
                                                  ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        return mbean.getMBeanInfo();
    }

    public NotificationEmitter getNotificationEmitterFor(ObjectName name)
                                                                         throws InstanceNotFoundException {
        // Check that the name is a valid Virtual MBean.
        // This is the easiest way to do that, but not always the
        // most efficient:
        getDynamicMBeanFor(name);

        // Return an object that supports add/removeNotificationListener
        // through the VirtualEventManager.
        return vem.getNotificationEmitterFor(name);
    }

    @Override
    public ObjectInstance getObjectInstance(ObjectName name)
                                                            throws InstanceNotFoundException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        final String className = mbean.getMBeanInfo().getClassName();
        return new ObjectInstance(name, className);
    }

    @Override
    public Object instantiate(String className) throws ReflectionException,
                                               MBeanException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    public Object instantiate(String className, Object[] params,
                              String[] signature) throws ReflectionException,
                                                 MBeanException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName)
                                                                      throws ReflectionException,
                                                                      MBeanException,
                                                                      InstanceNotFoundException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    public Object instantiate(String className, ObjectName loaderName,
                              Object[] params, String[] signature)
                                                                  throws ReflectionException,
                                                                  MBeanException,
                                                                  InstanceNotFoundException {
        throw new UnsupportedOperationException("Not applicable.");
    }

    @Override
    public Object invoke(ObjectName name, String operationName,
                         Object[] params, String[] signature)
                                                             throws InstanceNotFoundException,
                                                             MBeanException,
                                                             ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        return mbean.invoke(operationName, params, signature);
    }

    @Override
    public boolean isInstanceOf(ObjectName name, String className)
                                                                  throws InstanceNotFoundException {

        final DynamicMBean instance = nonNullMBeanFor(name);

        try {
            final String mbeanClassName = instance.getMBeanInfo().getClassName();

            if (mbeanClassName.equals(className)) {
                return true;
            }

            final Object resource;
            final ClassLoader cl;
            resource = instance;
            cl = instance.getClass().getClassLoader();

            final Class<?> classNameClass = Class.forName(className, false, cl);

            if (classNameClass.isInstance(resource)) {
                return true;
            }

            if (classNameClass == NotificationBroadcaster.class
                || classNameClass == NotificationEmitter.class) {
                try {
                    getNotificationEmitterFor(name);
                    return true;
                } catch (Exception x) {
                    LOG.finest("MBean " + name
                               + " is not a notification emitter. Ignoring: "
                               + x);
                    return false;
                }
            }

            final Class<?> resourceClass = Class.forName(mbeanClassName, false,
                                                         cl);
            return classNameClass.isAssignableFrom(resourceClass);
        } catch (Exception x) {
            /* Could be SecurityException or ClassNotFoundException */
            LOG.logp(Level.FINEST, Groo.class.getName(), "isInstanceOf",
                     "Exception calling isInstanceOf", x);
            return false;
        }
    }

    @Override
    public boolean isRegistered(ObjectName name) {
        try {
            final DynamicMBean mbean = getDynamicMBeanFor(name);
            return mbean != null;
        } catch (InstanceNotFoundException x) {
            if (LOG.isLoggable(Level.FINEST)) {
                LOG.finest("MBean " + name + " is not registered: " + x);
            }
            return false;
        }
    }

    @Override
    public Set<ObjectInstance> queryMBeans(ObjectName pattern, QueryExp query) {
        final Set<ObjectName> names = queryNames(pattern, query);
        if (names.isEmpty()) {
            return Collections.emptySet();
        }
        final Set<ObjectInstance> mbeans = new HashSet<ObjectInstance>();
        for (ObjectName name : names) {
            try {
                mbeans.add(getObjectInstance(name));
            } catch (SecurityException x) { // DLS: OK
                continue;
            } catch (InstanceNotFoundException x) { // DLS: OK
                continue;
            }
        }
        return mbeans;
    }

    @Override
    public Set<ObjectName> queryNames(ObjectName pattern, QueryExp query) {
        try {
            final Set<ObjectName> res = getMatchingNames(pattern);
            return filterListOfObjectNames(res, query);
        } catch (Exception x) {
            LOG.fine("Unexpected exception raised in queryNames: " + x);
            LOG.log(Level.FINEST, "Unexpected exception raised in queryNames",
                    x);
        }
        // We reach here only when an exception was raised.
        //
        return Collections.emptySet();
    }

    @Override
    public ObjectInstance registerMBean(Object object, ObjectName name)
                                                                       throws InstanceAlreadyExistsException,
                                                                       MBeanRegistrationException,
                                                                       NotCompliantMBeanException {
        throw newUnsupportedException("registerMBean");
    }

    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener)
                                                                         throws InstanceNotFoundException,
                                                                         ListenerNotFoundException {
        final NotificationEmitter emitter = getNonNullNotificationEmitterFor(name);
        emitter.removeNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name,
                                           NotificationListener listener,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException {
        NotificationEmitter emitter = getNonNullNotificationEmitterFor(name);
        emitter.removeNotificationListener(listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listenerName)
                                                                   throws InstanceNotFoundException,
                                                                   ListenerNotFoundException {
        NotificationListener listener = getListenerMBean(listenerName);
        removeNotificationListener(name, listener);
    }

    @Override
    public void removeNotificationListener(ObjectName name,
                                           ObjectName listenerName,
                                           NotificationFilter filter,
                                           Object handback)
                                                           throws InstanceNotFoundException,
                                                           ListenerNotFoundException {
        NotificationListener listener = getListenerMBean(listenerName);
        removeNotificationListener(name, listener, filter, handback);
    }

    @Override
    public void setAttribute(ObjectName name, Attribute attribute)
                                                                  throws InstanceNotFoundException,
                                                                  AttributeNotFoundException,
                                                                  InvalidAttributeValueException,
                                                                  MBeanException,
                                                                  ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        mbean.setAttribute(attribute);
    }

    @Override
    public AttributeList setAttributes(ObjectName name, AttributeList attributes)
                                                                                 throws InstanceNotFoundException,
                                                                                 ReflectionException {
        final DynamicMBean mbean = nonNullMBeanFor(name);
        return mbean.setAttributes(attributes);
    }

    @Override
    public void unregisterMBean(ObjectName name)
                                                throws InstanceNotFoundException,
                                                MBeanRegistrationException {
        throw newUnsupportedException("unregisterMBean");
    }

    public final ObjectName withDomain(String newDomain, ObjectName org)
                                                                        throws NullPointerException,
                                                                        MalformedObjectNameException {
        return construct(newDomain, org);
    }

    private ObjectName construct(String newDomain, ObjectName aname)
                                                                    throws MalformedObjectNameException {
        return copyToOtherDomain(newDomain, aname);
    }

    private ObjectName copyToOtherDomain(String domain, ObjectName aname)
                                                                         throws MalformedObjectNameException,
                                                                         NullPointerException {

        if (domain == null) {
            throw new NullPointerException("domain cannot be null");
        }

        if (aname == null) {
            throw new MalformedObjectNameException(
                                                   "key property list cannot be empty");
        }

        if (!isDomain(domain)) {
            throw new MalformedObjectNameException("Invalid domain: " + domain);
        }

        return null;
    }

    private NotificationListener getListenerMBean(ObjectName listenerName)
                                                                          throws InstanceNotFoundException {
        Object mbean = getDynamicMBeanFor(listenerName);
        if (mbean instanceof NotificationListener) {
            return (NotificationListener) mbean;
        } else {
            throw newIllegalArgumentException("MBean is not a NotificationListener: "
                                              + listenerName);
        }
    }

    private NotificationEmitter getNonNullNotificationEmitterFor(ObjectName name)
                                                                                 throws InstanceNotFoundException {
        NotificationEmitter emitter = getNotificationEmitterFor(name);
        if (emitter == null) {
            IllegalArgumentException iae = new IllegalArgumentException(
                                                                        "Not a NotificationEmitter: "
                                                                                + name);
            throw new RuntimeOperationsException(iae);
        }
        return emitter;
    }

    private boolean isDomain(String domain) {
        if (domain == null) {
            return true;
        }
        final int len = domain.length();
        int next = 0;
        while (next < len) {
            final char c = domain.charAt(next++);
            switch (c) {
                case ':':
                case '\n':
                    return false;
                case '*':
                case '?':
                    break;
            }
        }
        return true;
    }

    private DynamicMBean nonNullMBeanFor(ObjectName name)
                                                         throws InstanceNotFoundException {
        if (name == null) {
            throw newIllegalArgumentException("Null ObjectName");
        }
        if (name.getDomain().equals("")) {
            String defaultDomain = getDefaultDomain();
            try {
                name = withDomain(getDefaultDomain(), name);
            } catch (Exception e) {
                throw newIllegalArgumentException("Illegal default domain: "
                                                  + defaultDomain);
            }
        }
        final DynamicMBean mbean = getDynamicMBeanFor(name);
        if (mbean != null) {
            return mbean;
        }
        throw new InstanceNotFoundException(String.valueOf(name));
    }

    private ObjectInstance safeCreateMBean(String className, ObjectName name,
                                           ObjectName loaderName,
                                           Object[] params, String[] signature,
                                           boolean useRepository)
                                                                 throws ReflectionException,
                                                                 InstanceAlreadyExistsException,
                                                                 MBeanRegistrationException,
                                                                 MBeanException,
                                                                 NotCompliantMBeanException,
                                                                 InstanceNotFoundException {
        try {
            return createMBean(className, name, loaderName, params, signature,
                               useRepository);
        } catch (ReflectionException x) {
            throw x;
        } catch (InstanceAlreadyExistsException x) {
            throw x;
        } catch (MBeanRegistrationException x) {
            throw x;
        } catch (MBeanException x) {
            throw x;
        } catch (NotCompliantMBeanException x) {
            throw x;
        } catch (InstanceNotFoundException x) {
            throw x;
        } catch (SecurityException x) {
            throw x;
        } catch (JMRuntimeException x) {
            throw x;
        } catch (RuntimeException x) {
            throw new RuntimeOperationsException(x, x.toString());
        } catch (Exception x) {
            throw new MBeanException(x, x.toString());
        }
    }

    protected Set<ObjectName> getMatchingNames(ObjectName pattern) {
        return filterMatchingNames(pattern, getNames());
    }

    protected Set<ObjectName> getNames() {
        return null;
    }

    Set<ObjectName> filterListOfObjectNames(Set<ObjectName> list, QueryExp query) {
        if (list.isEmpty() || query == null) {
            return list;
        }

        // create a new result set
        final Set<ObjectName> result = new HashSet<ObjectName>();

        for (ObjectName on : list) {
            // if on doesn't match query exclude it.
            if (apply(query, on, this)) {
                result.add(on);
            }
        }
        return result;
    }

}
