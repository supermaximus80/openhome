package com.icontrol.openhomesimulator.camera.resources;

import com.icontrol.openhome.data.*;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.ohsimsolver.ResponseStatusFactory;
import com.icontrol.ohsimsolver.Wrappers;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.openhomesimulator.gateway.resources.MediaPutGetResource;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Resource("Event")
public class CustomEventResource {
    protected static final Logger log = LoggerFactory.getLogger(CustomEventResource.class);
    private static int DEFAULT_INTERVAL_BETWEEN_EVENTS = 15 ;      // 15 sec

    private static Map<String, EventTrigger> eventTriggerMap = new HashMap<String, EventTrigger>();
    private static EventNotificationMethods eventNotificationMethods = new EventNotificationMethods();
    private static Map<String, HostNotification> hostNotificationMap = new ConcurrentHashMap<String, HostNotification>();

    public static void init(){
        try{
            EventTrigger e = TriggersResource.IdResource.ExampleResource.get();
            eventTriggerMap.put(e.getId().getValue(), e);
            HostNotification notification = NotificationHostResource.IdResource.ExampleResource.get();
            hostNotificationMap.put(notification.getId().getValue(),notification);
            eventNotificationMethods = NotificationMethodsResource.ExampleResource.get();
        }
        catch (Exception e){
            System.out.println("Failed to initialize custom event resources.");
        }
    }


    public static String getNotificationMethod() {
        EventTrigger t = eventTriggerMap.get("0");
        if (t == null)
            return null;
        EventTriggerNotification n = t.getEventTriggerNotificationList().getEventTriggerNotification().get(0);
        if (n == null)
            return null;
        return n.getNotificationMethod().getValue();
    }

    public static boolean isNonMediaNotificationEnabled() {
        return eventNotificationMethods.getNonMediaEvent().getEnabled().isValue();
    }

    @Endpoint
    public EventNotification get() throws Exception {
        EventNotification n = new EventNotification();
        EventTriggerList triggerList = new EventTriggerList();
        triggerList.getEventTrigger().addAll(eventTriggerMap.values());
        n.setEventTriggerList(triggerList);
        EventNotificationMethods methods = eventNotificationMethods;
        HostNotificationList list = new HostNotificationList();
        list.getHostNotification().addAll(hostNotificationMap.values());
        methods.setHostNotificationList(list);
        n.setEventNotificationMethods(methods);
        return n;
    }

    @Endpoint
    public ResponseStatus put(EventNotification eventNotification) throws Exception {
        EventTriggerList list = eventNotification.getEventTriggerList();
        if (list != null) {
            eventTriggerMap.clear();
            Iterator<EventTrigger> iter = list.getEventTrigger().iterator();
            while (iter.hasNext()) {
                EventTrigger a = iter.next();
                eventTriggerMap.put(a.getId().getValue(),a);
            }
        }

        EventNotificationMethods m = eventNotification.getEventNotificationMethods();
        if (m != null) {
            eventNotificationMethods = m;
            HostNotificationList hostNotificationList = m.getHostNotificationList();
            if (hostNotificationList!=null){
                hostNotificationMap.clear();
                Iterator<HostNotification> iter = hostNotificationList.getHostNotification().iterator();
                while (iter.hasNext()) {
                    HostNotification a = iter.next();
                    hostNotificationMap.put(a.getId().getValue(),a);
                }
            }
        }
        return ResponseStatusFactory.getResponseOK();
    }

    @Resource("example")
    public static class ExampleResource{
        @Endpoint
        public EventNotification get() throws Exception {
            EventNotification e = new EventNotification();
            EventTriggerList list = TriggersResource.ExampleResource.get();
            e.setEventTriggerList(list);
            EventNotificationMethods m = NotificationMethodsResource.ExampleResource.get();
            e.setEventNotificationMethods(m);
            return e;
        }
    }


    @Resource("notification/methods")
    public static class NotificationMethodsResource {
        @Endpoint
        public EventNotificationMethods get() throws Exception {
            HostNotificationList hostNotificationList = new HostNotificationList();
            hostNotificationList.getHostNotification().addAll(hostNotificationMap.values());
            eventNotificationMethods.setHostNotificationList(hostNotificationList);
            return eventNotificationMethods;
        }

        @Endpoint
        public ResponseStatus put(EventNotificationMethods m) throws Exception{
            if (m==null){

            }
            HostNotificationList hostNotificationList = m.getHostNotificationList();
            if (hostNotificationList!=null){
                hostNotificationMap.clear();
                Iterator<HostNotification> iter = hostNotificationList.getHostNotification().iterator();
                while (iter.hasNext()) {
                    HostNotification a = iter.next();
                    hostNotificationMap.put(a.getId().getValue(),a);
                }
            }
            eventNotificationMethods=m;
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            public static EventNotificationMethods get() throws Exception {
                EventNotificationMethods e = new EventNotificationMethods();
                EventNonMediaFormat nonMediaEvent = new EventNonMediaFormat();
                nonMediaEvent.setEnabled(Wrappers.createBooleanCap(true));
                e.setNonMediaEvent(nonMediaEvent);
                // set HostNotification when not using XMPP mode
                e.setHostNotificationList(NotificationHostResource.ExampleResource.get());
                return e;
            }
        }
    }

    @Resource("triggers")
    public static class TriggersResource {

        @Endpoint
        public static EventTriggerList get() throws Exception {
            EventTriggerList list = new EventTriggerList();
            list.getEventTrigger().addAll(eventTriggerMap.values());
            return list;
        }

        @Endpoint
        public ResponseStatus put(EventTriggerList list) throws Exception{
            if (list==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            eventTriggerMap.clear();
            Iterator<EventTrigger> iter = list.getEventTrigger().iterator();
            while (iter.hasNext()) {
                EventTrigger a = iter.next();
                eventTriggerMap.put(a.getId().getValue(), a);
            }
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public ResponseStatus post(EventTrigger trigger) throws Exception{
            if (trigger==null||trigger.getId()==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            eventTriggerMap.put(trigger.getId().getValue(), trigger);
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public ResponseStatus delete() throws Exception{
            eventTriggerMap.clear();
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            public static EventTriggerList get() throws Exception {
                EventTriggerList list = new EventTriggerList();
                list.getEventTrigger().add(IdResource.ExampleResource.get());
                return list;
            }
        }

        @Resource("[UID]")
        public static class IdResource{
            @Endpoint
            public EventTrigger get(@PathVar("UID") String id) throws Exception {
                EventTrigger e = eventTriggerMap.get(id);
                if (e == null) {
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                return e;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("UID") String id, EventTrigger trigger) throws Exception{
                if (trigger==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
                }
                eventTriggerMap.put(id, trigger);
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ResponseStatus delete(@PathVar("UID") String id) throws Exception{
                eventTriggerMap.remove(id);
                return ResponseStatusFactory.getResponseOK();
            }


            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                public static EventTrigger get() throws Exception {
                    EventTrigger e = new EventTrigger();
                    e.setId(Wrappers.createIdCap("0"));
                    e.setEventType(EventType.PIR_MD);
                    e.setEventTypeInputID(Wrappers.createIdCap("0"));
                    e.setIntervalBetweenEvents(Wrappers.createIntegerCap(DEFAULT_INTERVAL_BETWEEN_EVENTS));
                    e.setEventTriggerNotificationList(NofiticationsResource.ExampleResource.get());
                    return e;
                }
            }

            @Resource("notifications")
            public static class NofiticationsResource {

                @Endpoint
                public EventTriggerNotificationList get(@PathVar("UID") String id) throws Exception {
                    EventTrigger e = eventTriggerMap.get(id);
                    if (e == null) {
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                    }
                    EventTriggerNotificationList list = e.getEventTriggerNotificationList();
                    return list;
                }

                @Endpoint
                public static ResponseStatus put(@PathVar("UID") String id, EventTriggerNotificationList list) throws Exception{
                    if (list==null){
                        throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
                    }
                    EventTrigger e = eventTriggerMap.get(id);
                    if (e == null) {
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                    }
                    e.setEventTriggerNotificationList(list);
                    return ResponseStatusFactory.getResponseOK();
                }

                @Endpoint
                public ResponseStatus delete(@PathVar("UID") String id) throws Exception{
                    EventTrigger e = eventTriggerMap.get(id);
                    if (e == null) {
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                    }
                    EventTriggerNotificationList list = new EventTriggerNotificationList();
                    e.setEventTriggerNotificationList(list);
                    return ResponseStatusFactory.getResponseOK();
                }

                @Resource("example")
                public static class ExampleResource{
                    @Endpoint
                    public static EventTriggerNotificationList get() throws Exception {
                        EventTriggerNotificationList l = new EventTriggerNotificationList();
                        EventTriggerNotification n =  NotifyIdResource.ExampleResource.get("0");
                        l.getEventTriggerNotification().add(n);
                        return l;
                    }
                }

                @Resource("[NOTIFYID]")
                public static class NotifyIdResource{

                    @Endpoint
                    public EventTriggerNotification get(@PathVar("UID") String id, @PathVar("NOTIFYID") String notifyId) throws Exception {
                        EventTrigger e = eventTriggerMap.get(id);
                        if (e == null) {
                            throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                        }
                        EventTriggerNotificationList list = e.getEventTriggerNotificationList();
                        Iterator<EventTriggerNotification> iter = list.getEventTriggerNotification().iterator();
                        while (iter.hasNext()) {
                            EventTriggerNotification n = iter.next();
                            if (notifyId.equalsIgnoreCase(n.getNotificationID().getValue())) {
                                return n;
                            }
                        }
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                    }

                    @Endpoint
                    public ResponseStatus put(@PathVar("UID") String id, @PathVar("NOTIFYID") String notifyId, EventTriggerNotification n) throws Exception{
                        EventTrigger e = eventTriggerMap.get(id);
                        if (e == null) {
                            throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                        }
                        EventTriggerNotificationList list = e.getEventTriggerNotificationList();
                        Iterator<EventTriggerNotification> iter = list.getEventTriggerNotification().iterator();
                        int index = 0;
                        int replace = -1;
                        while (iter.hasNext()) {
                            EventTriggerNotification en = iter.next();
                            if (notifyId.equalsIgnoreCase(en.getNotificationID().getValue())) {
                                replace = index;
                            }
                            index++;
                        }
                        if (replace >= 0)
                            list.getEventTriggerNotification().set(replace, n);
                        else
                            list.getEventTriggerNotification().add(n);
                        return ResponseStatusFactory.getResponseOK();
                    }

                    @Endpoint
                    public ResponseStatus delete(@PathVar("UID") String id, @PathVar("NOTIFYID") String notifyId) throws Exception
                    {
                        EventTrigger e = eventTriggerMap.get(id);
                        if (e == null) {
                            throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                        }
                        EventTriggerNotificationList list = e.getEventTriggerNotificationList();
                        Iterator<EventTriggerNotification> iter = list.getEventTriggerNotification().iterator();
                        EventTriggerNotification toDelete = null;
                        while (iter.hasNext()) {
                            EventTriggerNotification n = iter.next();
                            if (notifyId.equalsIgnoreCase(n.getNotificationID().getValue())) {
                                toDelete = n;
                            }
                        }
                        list.getEventTriggerNotification().remove(toDelete);
                        return ResponseStatusFactory.getResponseOK();
                    }

                    @Resource("example")
                    public static class ExampleResource{
                        @Endpoint
                        public static EventTriggerNotification get(@PathVar("NOTIFYID") String notifyId) throws javax.xml.datatype.DatatypeConfigurationException {
                            EventTriggerNotification e = new EventTriggerNotification();
                            e.setNotificationID(Wrappers.createIdCap("0"));
                            EventNotificationMethod m = new EventNotificationMethod();
                            if (GatewaySimulatorFactory.getInstance().numXmppClientSessions() > 0)
                                m.setValue("XMPP");
                            else
                                m.setValue("HTTP");
                            e.setNotificationMethod(m);
                            EventNotificationRecurrence r = new EventNotificationRecurrence();
                            r.setValue("beginningandend");
                            e.setNotificationRecurrence(r);
                            return e;
                        }
                    }
                }
            }
        }
    }

    @Resource("notification/host")
    public static class NotificationHostResource {

        @Endpoint
        public HostNotificationList get() throws Exception {
            HostNotificationList list = new HostNotificationList();
            list.getHostNotification().addAll(hostNotificationMap.values());
            return list;
        }

        @Endpoint
        public ResponseStatus put(HostNotificationList list) throws Exception{
            if (list==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
            }
            hostNotificationMap.clear();
            Iterator<HostNotification> iter = list.getHostNotification().iterator();
            while(iter.hasNext()) {
                HostNotification hostNotification = iter.next();
                hostNotificationMap.put(hostNotification.getId().getValue(),hostNotification);
            }
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public ResponseStatus delete() throws Exception{
            hostNotificationMap.clear();
            return ResponseStatusFactory.getResponseOK();
        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            public static HostNotificationList get() throws Exception {
                HostNotificationList l = new HostNotificationList();
                HostNotification n = IdResource.ExampleResource.get();
                l.getHostNotification().add(n);
                return l;
            }
        }


        @Resource("[UID]")
        public static class IdResource{

            @Endpoint
            public HostNotification get(@PathVar("UID") String id) throws Exception {
                HostNotification notification = hostNotificationMap.get(id);
                if (notification==null){
                    throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND, "");
                }
                else return notification;
            }

            @Endpoint
            public ResponseStatus put(@PathVar("UID") String id, HostNotification hostNotification) throws Exception{
                if (hostNotification==null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST,"");
                }
                hostNotificationMap.put(id, hostNotification);
                return ResponseStatusFactory.getResponseOK();
            }

            @Endpoint
            public ResponseStatus delete(@PathVar("UID") String id) throws Exception{
                hostNotificationMap.remove(id);
                return ResponseStatusFactory.getResponseOK();
            }

            @Resource("example")
            public static class ExampleResource{
                @Endpoint
                public static HostNotification get() throws Exception {
                    HostNotification n = new HostNotification();
                    n.setId(Wrappers.createIdCap("0"));
                    // HostNotify URL
                    StringCap url = new StringCap() ;
                    if (GatewaySimulatorFactory.getInstance().numXmppClientSessions() > 0) {
                        url.setValue("xmpp://"+GatewaySimulatorFactory.getInstance().getXmppServerDomainName()+"/"+ MediaPutGetResource.EventAlertResource.getPathName());
                        n.setUrl(url);
                    } else {
                        url.setValue(GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.EventAlertResource.getPathName());
                        n.setUrl(url);
                        HTTPAuthenticationMethodEnum authenticationMethod = new HTTPAuthenticationMethodEnum();
                        authenticationMethod.setValue("BASIC");
                        n.setHttpAuthenticationMethod(authenticationMethod);
                    }
                    return n;
                }
            }
        }
    }



    public static String getNotifyMethodsHostURL() {
        //init();

        HostNotification n = hostNotificationMap.get("0");
        if (n == null)
            return null;
        return n.getUrl().getValue();
    }

    public static int getIntervalBetweenEvents() {
        EventTrigger t = eventTriggerMap.get("0");
        if (t == null)
            return 0;
        return t.getIntervalBetweenEvents().getValue().intValue();
    }

}
