package com.icontrol.ohsimsolver;

import com.icontrol.openhome.data.*;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.Date;
import java.util.GregorianCalendar;

public class Wrappers {

    static public StringCap createStringCap(String str) {
        StringCap strcap = new StringCap();
        strcap.setValue(str);
        return strcap;
    }

    static public IntegerCap createIntegerCap(int val) {
        IntegerCap intCap = new IntegerCap();
        intCap.setValue(new BigInteger(Integer.toString(val)));
        return intCap;
    }

    static public BooleanCap createBooleanCap(boolean val) {
        BooleanCap cap = new BooleanCap();
        cap.setValue(val);
        return cap;
    }

    static public IdCap createIdCap(String val) {
        IdCap cap = new IdCap();
        cap.setValue(val);
        return cap;
    }

    static public DateTimeCap createDateTimeCap(Date date) throws javax.xml.datatype.DatatypeConfigurationException {
        GregorianCalendar gcal = new GregorianCalendar();
        gcal.setTime(date);
        XMLGregorianCalendar xgcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        DateTimeCap dateTime =new DateTimeCap();
        dateTime.setValue(xgcal);
        return dateTime;
    }

    static public PercentageCap createPercentageCap(int val) {
        PercentageCap cap = new PercentageCap();
        cap.setValue(val);
        return cap;
    }
}
