package com.icontrol.openhomesimulator.camera;

import com.icontrol.openhome.data.*;

import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.ohsimsolver.Wrappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class EventAlertMessage {

    private static final Logger log = LoggerFactory.getLogger(EventAlertMessage.class);

    private static int postCount = 0;

    public static String createMessagePIR(String alertID, boolean active) throws javax.xml.datatype.DatatypeConfigurationException, Exception {
        return createMessage(EventType.PIR_MD, alertID, active);

    }

    public static String createMessage(EventType type, String alertID, boolean active) throws javax.xml.datatype.DatatypeConfigurationException, Exception {

        EventAlert a = new EventAlert();
        a.setId(Wrappers.createIdCap(alertID));

        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(new Date());
        XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        DateTimeCap dateTime = new DateTimeCap();
        dateTime.setValue(xgcal);
        a.setDateTime(dateTime);

        a.setActivePostCount(Wrappers.createIntegerCap(postCount++));

        a.setEventType(type);

        AlertEventState s = new AlertEventState();
        
        s.setValue(active?"active":"inactive");
        a.setEventState(s);

        String ret = RestClient.toString(a, RestConstants.ContentType.TEXT_XML);

        return ret;
    }
}
