package com.hellblazer.groo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.Notification;
import javax.management.NotificationEmitter;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;

public class VirtualEventManager {
    private static class ListenerInfo {
        private static boolean same(Object x, Object y) {
            if (x == y) {
                return true;
            }
            if (x == null) {
                return false;
            }
            return x.equals(y);
        }

        public final NotificationListener listener;
        public final NotificationFilter   filter;

        public final Object               handback;

        public ListenerInfo(NotificationListener listener,
                            NotificationFilter filter, Object handback) {

            if (listener == null) {
                throw new IllegalArgumentException("Null listener.");
            }

            this.listener = listener;
            this.filter = filter;
            this.handback = handback;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(o instanceof ListenerInfo)) {
                return false;
            }

            return listener.equals(((ListenerInfo) o).listener);
        }

        @Override
        public int hashCode() {
            return listener.hashCode();
        }

        boolean equals(NotificationListener listener,
                       NotificationFilter filter, Object handback) {
            return this.listener == listener && same(this.filter, filter)
                   && same(this.handback, handback);
        }
    }

    private static void sendNotif(List<ListenerInfo> listeners, Notification n) {
        for (ListenerInfo li : listeners) {
            if (li.filter == null || li.filter.isNotificationEnabled(n)) {
                try {
                    li.listener.handleNotification(n, li.handback);
                } catch (Exception e) {
                    logger.log(Level.FINEST, "sendNotif handleNotification", e);
                }
            }
        }
    }

    private final Map<ObjectName, List<ListenerInfo>> exactSubscriptionMap   = new HashMap<ObjectName, List<ListenerInfo>>();

    private final Map<ObjectName, List<ListenerInfo>> patternSubscriptionMap = new HashMap<ObjectName, List<ListenerInfo>>();

    private static final Logger                       logger                 = Logger.getLogger(VirtualEventManager.class.getCanonicalName());

    public VirtualEventManager() {
    }

    public NotificationEmitter getNotificationEmitterFor(final ObjectName name)
                                                                               throws InstanceNotFoundException {
        final NotificationEmitter emitter = new NotificationEmitter() {
            @Override
            public void addNotificationListener(NotificationListener listener,
                                                NotificationFilter filter,
                                                Object handback)
                                                                throws IllegalArgumentException {
                subscribe(name, listener, filter, handback);
            }

            @Override
            public MBeanNotificationInfo[] getNotificationInfo() {
                return null;
            }

            @Override
            public void removeNotificationListener(NotificationListener listener)
                                                                                 throws ListenerNotFoundException {
                unsubscribe(name, listener);
            }

            @Override
            public void removeNotificationListener(NotificationListener listener,
                                                   NotificationFilter filter,
                                                   Object handback)
                                                                   throws ListenerNotFoundException {
                unsubscribe(name, listener, filter, handback);
            }
        };
        return emitter;
    }

    public void publish(ObjectName emitterName, Notification n) {
        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("publish " + emitterName);
        }

        if (n == null) {
            throw new IllegalArgumentException("Null notification");
        }

        if (emitterName == null) {
            throw new IllegalArgumentException("Null emitter name");
        } else if (emitterName.isPattern()) {
            throw new IllegalArgumentException(
                                               "The emitter must not be an ObjectName pattern");
        }

        final List<ListenerInfo> listeners = new ArrayList<ListenerInfo>();

        synchronized (exactSubscriptionMap) {
            List<ListenerInfo> exactListeners = exactSubscriptionMap.get(emitterName);
            if (exactListeners != null) {
                listeners.addAll(exactListeners);
            }
        }
        synchronized (patternSubscriptionMap) {
            for (ObjectName on : patternSubscriptionMap.keySet()) {
                if (on.apply(emitterName)) {
                    listeners.addAll(patternSubscriptionMap.get(on));
                }
            }
        }

        sendNotif(listeners, n);
    }

    public void subscribe(ObjectName name, NotificationListener listener,
                          NotificationFilter filter, Object handback) {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("subscribe " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new IllegalArgumentException("Null listener");
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        final ListenerInfo li = new ListenerInfo(listener, filter, handback);
        List<ListenerInfo> list;

        synchronized (map) {
            list = map.get(name);
            if (list == null) {
                list = new ArrayList<ListenerInfo>();
                map.put(name, list);
            }
            list.add(li);
        }
    }

    public void unsubscribe(ObjectName name, NotificationListener listener)
                                                                           throws ListenerNotFoundException {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("unsubscribe2 " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new ListenerNotFoundException();
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        final ListenerInfo li = new ListenerInfo(listener, null, null);
        List<ListenerInfo> list;
        synchronized (map) {
            list = map.get(name);
            if (list == null || !list.remove(li)) {
                throw new ListenerNotFoundException();
            }

            if (list.isEmpty()) {
                map.remove(name);
            }
        }
    }

    public void unsubscribe(ObjectName name, NotificationListener listener,
                            NotificationFilter filter, Object handback)
                                                                       throws ListenerNotFoundException {

        if (logger.isLoggable(Level.FINEST)) {
            logger.finest("unsubscribe4 " + name);
        }

        if (name == null) {
            throw new IllegalArgumentException("Null MBean name");
        }

        if (listener == null) {
            throw new ListenerNotFoundException();
        }

        Map<ObjectName, List<ListenerInfo>> map = name.isPattern() ? patternSubscriptionMap
                                                                  : exactSubscriptionMap;

        List<ListenerInfo> list;
        synchronized (map) {
            list = map.get(name);
            boolean removed = false;
            for (Iterator<ListenerInfo> it = list.iterator(); it.hasNext();) {
                ListenerInfo li = it.next();
                if (li.equals(listener, filter, handback)) {
                    it.remove();
                    removed = true;
                    break;
                }
            }
            if (!removed) {
                throw new ListenerNotFoundException();
            }

            if (list.isEmpty()) {
                map.remove(name);
            }
        }
    }
}