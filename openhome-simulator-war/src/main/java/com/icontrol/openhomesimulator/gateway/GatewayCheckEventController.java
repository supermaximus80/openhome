/*
 * 
 */

package com.icontrol.openhomesimulator.gateway;

import com.icontrol.openhomesimulator.gateway.resources.MediaPutGetResource;
import com.icontrol.openhomesimulator.util.Utilities;
import com.icontrol.rest.framework.HttpCodeException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 *
 */
public class GatewayCheckEventController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(GatewayCheckEventController.class);

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException      if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        OutputStream os = null;
        try {
            int status = 200;
            String body = null;
            Map<String,String> queryMap = Utilities.getQueryMap(request.getQueryString());
            String id = queryMap.get("id");
            if (!GatewaySimulatorFactory.getInstance().getUniqueID().equalsIgnoreCase(id)) {
                // invalid id, return error
                response.setStatus(401);
                return;
            }
            MediaInstance m = GatewaySimulatorFactory.getInstance().getUserNotification();
            if (m != null && !m.isNotified()) {
                m.setNotified(true);
                status = 200;
                // use simplified contentType
                String outContentType = m.getContentType();
                int pos = outContentType.indexOf("/");
                if (pos > 0)
                    outContentType = outContentType.substring(0, pos);

                if (m.getContentType().startsWith("image"))
                    body = GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.ImageResource.getPathName()+m.getId()+"&"+outContentType;
                else if (m.getContentType().startsWith("video"))
                    body = GatewaySimulatorFactory.getInstance().getMediaUploadURLPrefix()+ MediaPutGetResource.VideoResource.getPathName()+m.getId()+"&"+outContentType;
                else if (m.getContentType().startsWith("alert"))
                    body = new String(m.getMedia()) +"&"+outContentType;
                else if (m.getContentType().startsWith("text"))
                    body = new String(m.getMedia()) +"&"+outContentType;
                else if (m.getContentType().startsWith(GatewaySimulatorFactory.ALERT_CONNECTION))
                    body = new String(m.getMedia()) +"&"+outContentType;
                else
                    throw new IOException("Unknown content type for HasNewMedia notification");
            }

            if (status != 200) {
                response.setStatus(status);
                return;
            }

            // write response
            if (body != null && body.length() > 0) {
                response.setContentLength(body.length());
                os = response.getOutputStream();
                os.write(body.getBytes());
                os.flush();
            }
        } catch (HttpCodeException e) {
            log.error(e.getMessage(), e);
            response.sendError(e.getCode(), e.getMessage());
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Internal ohsimservice error");
        } catch (UnsupportedOperationException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage() + "!");
            log.error(e.getMessage(), e);
        } catch (Error e) {
            log.error(e.getMessage(), e);
            throw e;
        } finally {
            if (os != null)
                os.close();
        }
    }


    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
