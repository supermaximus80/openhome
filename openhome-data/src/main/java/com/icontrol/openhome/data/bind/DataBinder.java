package com.icontrol.openhome.data.bind;

import java.io.InputStream;


public class DataBinder {

    //methods to be invoked by DataBinder in DataModel project
    public static String toXML(Object obj) throws Exception{
        return LocalDataBinder.toString(obj);
    }

    public static <T> T valueOfXML(InputStream is, Class<T> paramType) throws Exception {
        return LocalDataBinder.valueOf(is, paramType);
    }

    public static Object valueOfXML(InputStream is) throws Exception{
        return valueOfXML(is, Object.class);
    }
    
    public static Object valueOfXML(String xmlString) throws Exception {
        return LocalDataBinder.valueOf(xmlString);
    }
    
}
