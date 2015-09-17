package com.icontrol.openhomesimulator.gateway;

import org.jivesoftware.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

/*
 * @author rbitonio
 */
public class DownloadFileResourceServlet extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(DownloadFileResourceServlet.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String resource = request.getParameter("resource");
        if (resource == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing resource parameter");
            return;
        }
        log.debug("Resource to download: " + resource);

        int length = 0;
        byte[] bbuf = new byte[2048];


        InputStream is = ClassUtils.getResourceAsStream(resource);
        if (is == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Failed to find resource: " + resource);
            return;
        }

        int bytesRead = 0;
        while ((is != null) && ((length = is.read(bbuf)) != -1)) {
            bytesRead += length;
        }
        log.debug("Read #bytes:" + bytesRead);

        response.setContentType("application/octet-stream");
        response.setContentLength(bytesRead);
        response.setHeader("Content-Disposition", "attachment; filename=\"" + resource + "\"");

        is = ClassUtils.getResourceAsStream(resource);
        ServletOutputStream op = response.getOutputStream();
        while ((is != null) && ((length = is.read(bbuf)) != -1)) {
            op.write(bbuf, 0, length);
            log.debug("Writing #bytes out: " + length);
        }

        try {
            op.flush();
        } catch (Exception e) {
            log.error("Faild to end stream: " + e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
            if (op != null) {
                op.close();
            }
        }
    }

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException            if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
