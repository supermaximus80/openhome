package com.icontrol.android.openhomesimulator.camera.resources;

import com.icontrol.android.ohsimsolver.ResponseStatusFactory;
import com.icontrol.android.ohsimsolver.Wrappers;
import com.icontrol.openhome.data.*;
import com.icontrol.rest.framework.HttpCodeException;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.service.Endpoint;
import com.icontrol.rest.framework.service.PathVar;
import com.icontrol.rest.framework.service.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Resource("system/time")
public class SystemTimeResource {

    protected static final Logger log = LoggerFactory.getLogger(SystemTimeResource.class);


    private static Time mTime;
    private static Map<String, NTPServer> mNTPServerMap = new ConcurrentHashMap<String, NTPServer>();


    @Endpoint
    public Time get() throws Exception {
        synchronized (mTime){
            return mTime;
        }
    }

    @Endpoint
    public ResponseStatus put(Time inputTime) throws Exception {
        synchronized (mTime){
            mTime = inputTime;
        }
        log.debug("PutSystemTime set");
        return ResponseStatusFactory.getResponseOK();
    }

    @Resource("example")
    public static class ExampleResource{
        @Endpoint
        static public Time get() throws Exception {
            Time time = new Time();
            // time mode
            Time.TimeMode timeMode = (new Time.TimeMode());
            timeMode.setValue("NTP");
            time.setTimeMode(timeMode);
            // local time
            GregorianCalendar gcal = new GregorianCalendar();
            gcal.setTime(new Date());
            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
            DateTimeCap dateTime = new DateTimeCap();
            dateTime.setValue(xgcal);
            time.setLocalTime(dateTime);
            // time zone
            time.setTimeZone(Wrappers.createStringCap("PDT"));
            return time;
        }
    }

    @Resource("localtime")
    public static class SystemLocalTime {
        @Endpoint
        public String get() throws Exception {
            synchronized (mTime){
                XMLGregorianCalendar xgc = mTime.getLocalTime().getValue();
                return xgc.toXMLFormat();
            }
        }

        @Endpoint
        public ResponseStatus put(String localTimeStr) throws Exception {
            if (localTimeStr==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(localTimeStr);
            synchronized (mTime){
                mTime.getLocalTime().setValue(xgcal);
            }
            log.debug("PuttSystemTime/LocalTime set");
            return ResponseStatusFactory.getResponseOK();

        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public String get() throws Exception {
                synchronized (mTime){
                    mTime = SystemTimeResource.ExampleResource.get();
                    XMLGregorianCalendar xgc = mTime.getLocalTime().getValue();
                    return xgc.toXMLFormat();
                }
            }
        }
    }

    @Resource("timezone")
    public static class SystemTimeZone {
        @Endpoint
        public String get() throws Exception {
            synchronized (mTime){
                return mTime.getTimeZone().getValue();
            }
        }

        @Endpoint
        public ResponseStatus put(String tzStr) throws Exception {
            if (tzStr==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            synchronized (mTime){
                mTime.getTimeZone().setValue(tzStr);
            }
            log.debug("PUT system/time/timeZone");
            return ResponseStatusFactory.getResponseOK();

        }

        @Resource("example")
        public static class ExampleResource{
            @Endpoint
            static public String get() throws Exception {
                return "PDT";
            }
        }
    }

    @Resource("ntpservers")
    public static class NtpServersResource {
        @Endpoint
        public NTPServerList get() throws Exception {
            NTPServerList ntpServerList = new NTPServerList();
            synchronized (mNTPServerMap){
                ntpServerList.getNTPServer().addAll(mNTPServerMap.values());
            }
            return ntpServerList;
        }

        @Endpoint
        public ResponseStatus put(NTPServerList list) throws Exception {
            if (list==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            Iterator<NTPServer> iter = list.getNTPServer().iterator();
            synchronized (mNTPServerMap){
                mNTPServerMap.clear();
                while (iter.hasNext()) {
                    NTPServer s = iter.next();
                    mNTPServerMap.put(s.getId().getValue(), s);
                }
            }
            log.debug("PUT system/time/ntpServers");
            return ResponseStatusFactory.getResponseOK();
        }

        @Endpoint
        public ResponseStatus post(NTPServer server) throws Exception {
            if (server==null){
                throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
            }
            synchronized (mNTPServerMap){
                mNTPServerMap.put(server.getId().getValue(),server) ;
            }
            log.debug("POST system/time/ntpServers");
            return ResponseStatusFactory.getResponseOK();
        }



        @Resource("[id]")
        public static class IdResource{
            @Endpoint
            public NTPServer get(@PathVar("id") String id) throws Exception {
                synchronized (mNTPServerMap){
                    NTPServer server = mNTPServerMap.get(id)  ;
                    if (server == null )
                        throw new HttpCodeException(RestConstants.Status.SC_NOT_FOUND,"");
                    return server;
                }
            }

            @Endpoint
            public ResponseStatus put(@PathVar("id") String id, NTPServer server) throws Exception {
                if (server == null){
                    throw new HttpCodeException(RestConstants.Status.SC_BAD_REQUEST, "");
                }
                log.debug("PUT system/time/ntpServers");
                synchronized (mNTPServerMap){
                    mNTPServerMap.put(id,server);
                }
                return ResponseStatusFactory.getResponseOK();

            }

            @Endpoint
            public ResponseStatus delete(@PathVar("id") String id) throws Exception {
                log.debug("DELETE system/time/ntpServers/[id]");
                synchronized (mNTPServerMap){
                    mNTPServerMap.remove(id);
                }
                return ResponseStatusFactory.getResponseOK();

            }


        }
    }
}
