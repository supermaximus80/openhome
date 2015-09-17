package com.icontrol.openhomesimulator.util;

import org.jivesoftware.util.ClassUtils;
import org.jivesoftware.util.XMLProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/*
 * @author rbitonio
 */

public class OpenHomeProperties {

    static public final String BASE_PATH = "basePath";
    static private final String OPENHOMEXML_FILENAME = "openhome.xml";

    private static final Logger log = LoggerFactory.getLogger(OpenHomeProperties.class);

    private static OpenHomeProperties openHomeProperties = new OpenHomeProperties();

    public static String getProperty(String name) {
        return openHomeProperties.xmlProperties.getProperty(name);
    }

    public static String getProperty(String name, String defaultValue) {
        if (openHomeProperties.xmlProperties!=null){
            String value = openHomeProperties.xmlProperties.getProperty(name);
            if (value == null) {
                return defaultValue;
            }
            return value;
        }
        else
            return null;
    }

    public static boolean getProperty(String name, boolean defaultValue) {
        if (openHomeProperties.xmlProperties!=null){
            String value = openHomeProperties.xmlProperties.getProperty(name);
            if (value == null) {
                return defaultValue;
            }
            if (value == null)
                return false;
            return (value.equalsIgnoreCase("true"));
        }
        else return false;
    }

    private XMLProperties xmlProperties = null;

    private OpenHomeProperties() {
        loadSetupProperties();
    }

    void loadSetupProperties() {
        try {
            // check if override file exists
            String baseDir = System.getProperty(BASE_PATH, "/data/ic");
            String propFileName = baseDir.endsWith("/")?baseDir:(baseDir+"/") + "conf/"+OPENHOMEXML_FILENAME;
            // test if propFile exists to override local resource
            File file = new File(propFileName);
            if (file.exists() && file.canRead()) {
                xmlProperties = new XMLProperties(file);
            } else {
                // read from resource
                InputStream is = ClassUtils.getResourceAsStream(OPENHOMEXML_FILENAME);
                if (is != null)
                    xmlProperties = new XMLProperties(is);
                else {
                    xmlProperties=null;
                    throw new IOException(OPENHOMEXML_FILENAME+" NOT found");
                }
            }
        } catch (IOException ioe) {
            xmlProperties=null;
            log.error("Failed to load openhome.xml", ioe.getMessage());
        }
    }
}