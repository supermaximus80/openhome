package com.icontrol.openhomesimulator.camera.xmppclient;

import com.icontrol.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.openhomesimulator.camera.History;
import com.icontrol.openhomesimulator.gateway.GatewaySimulatorFactory;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.LocalRestClient;
import com.icontrol.rest.framework.RestClient;
import com.icontrol.rest.framework.RestConstants;
import com.icontrol.rest.framework.RestService;
import org.slf4j.Logger;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;


//TODO: this class is not called ever. need to check
public class LocalDirectRestRequest {
    Logger log;

    static RestService restService = GatewaySimulatorFactory.getInstance().getRestServiceInst();
    static Object monitor = new Object();

    public LocalDirectRestRequest(Logger log) {
        this.log = log;

        synchronized (monitor) {
            if (restService == null) {
                try {
                    System.out.println("Local direct rest request is running.");
                } catch (Exception ex) {
                    log.error("caught "+ex, ex);
                }
            }
        }
    }


    public OpenHomeResponseIQ process(OpenHomeRequestIQ reqIQ) {
        OpenHomeResponseIQ responseIQ = null;
        String errorString=null;

        try {
            RestClient.Response response = null;
            String pathStr = reqIQ.getAction().trim();
            LocalRestClient localRestClient = CameraSimulatorFactory.getInstance().getLocalRestClient();
            final String path = pathStr;
            final String method = reqIQ.getMethod();
            final String requestBody = reqIQ.getBodyText();
            if (requestBody==null || !requestBody.startsWith("<?xml")){
                response = localRestClient.invoke(method,false,path,null,null,requestBody);
            }
            else {
                final byte[] data = (reqIQ.getBodyText() != null) ? Utilities.getCleanXml(reqIQ.getBodyText()).getBytes() : null;
                String [] contentType={RestConstants.Header.CONTENT_TYPE, RestConstants.ContentType.TEXT_XML};
                String [][] headers=new String [1][];
                headers[0] = contentType;
                response = localRestClient.invoke(method,false,path,null,headers,data);

            }
            // fill in responseIQ
            responseIQ = new OpenHomeResponseIQ();
            responseIQ.setCode(response.getStatus());
            responseIQ.setContentType(response.getContentType());
            responseIQ.setBodyText(response.getContentAsString());
            responseIQ.setPacketID(reqIQ.getPacketID());
            //log history
            History.CommandEvent event = History.getInstance().createCommandEvent(path, new Date(), new Date(), 0);
            event.setResponseCode(response.getStatus());
            History.getInstance().add(event);

        } catch (Exception ex) {
            errorString = ex.toString();
            log.error("LocalDirectRestRequest - caught "+ex);
        } finally {
        }

        // if null responseIQ, create an error response
        if (responseIQ == null) {
            log.error("LocalDirectRestRequest NULL respoonseIQ. Generate error response");
            responseIQ = new OpenHomeResponseIQ();
            responseIQ.setCode(500);
            responseIQ.setBodyText("LocalDirectRestRequest - caught "+errorString);
            responseIQ.setPacketID(reqIQ.getPacketID());
        }

        return responseIQ;
    }

}
