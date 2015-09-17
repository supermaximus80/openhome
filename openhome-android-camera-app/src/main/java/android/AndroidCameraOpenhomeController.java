package main.java.android;


import android.content.Intent;
import android.os.CountDownTimer;
import com.icontrol.android.ohsimsolver.HttpCodeException;
import com.icontrol.android.openhomesimulator.camera.AndroidMjpegEncoder;
import com.icontrol.android.openhomesimulator.camera.CameraSimulatorFactory;
import com.icontrol.android.openhomesimulator.camera.MediaResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 *
 */
public class AndroidCameraOpenhomeController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AndroidCameraOpenhomeController.class);

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

        try {
            /*
            if request to picture upload, start photo camera, take a picture, and give response after
            picture is taken
            */
            if (request.getPathInfo().contains("picture/upload")){
                if ( !AndroidMjpegEncoder.cameraRunning){
                    AndroidCameraVariables.photoData=null;
                    Intent myIntent = new Intent(AndroidCameraMainActivity.context, AndroidCameraPhotoActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AndroidCameraMainActivity.context.startActivity(myIntent);
                    Thread.sleep(3000); //wait for the camera activity to finish
                    if (AndroidCameraVariables.photoData!=null){
                        MediaResourceLoader.setImageFromAndroid(AndroidCameraVariables.photoData);
                    }
                    else throw new Exception(AndroidCameraConstants.No_Image_Or_Video_Data);
                }
                else throw new Exception(AndroidCameraConstants.Camera_Running);
            }

            /*
            if request to picture upload, start video camera, take video, and give response after
            video is taken
            */
            else if (request.getPathInfo().contains("video/upload")){
                if (!AndroidMjpegEncoder.cameraRunning){
                    AndroidCameraVariables.videoFile=null;
                    Intent myIntent = new Intent(AndroidCameraMainActivity.context, AndroidCameraVideoActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    AndroidCameraMainActivity.context.startActivity(myIntent);
                    Thread.sleep(14000);  //wait for the camera activity to finish
                    if (AndroidCameraVariables.videoFile!=null){
                        MediaResourceLoader.setVideoStreamFromAndroidCamera(AndroidCameraVariables.videoFile);
                    }
                    else throw new Exception(AndroidCameraConstants.No_Image_Or_Video_Data);
                }
                else throw new Exception(AndroidCameraConstants.Camera_Running);
            }

            else if (request.getPathInfo().contains("http/mjpg")){
                if (request.getMethod().equals("GET")){
                    if (!AndroidMjpegEncoder.cameraRunning){
                        AndroidMjpegEncoder.cameraRunning=true;
                        Intent myIntent = new Intent(AndroidCameraMainActivity.context, AndroidCameraStreamActivity.class);
                        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        AndroidCameraMainActivity.context.startActivity(myIntent);
                    }
                    else throw new Exception(AndroidCameraConstants.Camera_Running);
                }
            }

            HttpSession session = request.getSession();
            Logger log = (Logger) session.getAttribute("cameralog");
            if (log==null) {
                log = LoggerFactory.getLogger(AndroidCameraSimController.class);
                session.setAttribute("cameralog", log);
            }
            CameraSimulatorFactory.getInstance().getRestServiceInst().service(request, response);
            System.out.println("request path: " + request.getPathInfo());

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
            System.out.println(e.toString());
            e.printStackTrace();
        } catch (Error e) {
            log.error(e.getMessage(), e);
            throw e;
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

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request  servlet request
     * @param response servlet response
     * @throws javax.servlet.ServletException if a servlet-specific error occurs
     * @throws java.io.IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "OpenHomeAPI";
    }// </editor-fold>

}