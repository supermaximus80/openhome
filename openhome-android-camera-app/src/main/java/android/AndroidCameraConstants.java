package main.java.android;


public class AndroidCameraConstants {
    public static String Device_IP_Address;
    public static final int RTSP_Port=28080;
    public static final int HTTP_Port=38080;

    public static final String Camera_Running="Remote camera has already been running now!";
    public static final String No_Image_Or_Video_Data="Remote camera error! No image or video is taken by remote camera";
    public static final String Camera_Not_Running="Remote camera is not running now!";
    public static final String Error_Retrieve_Info= "Error in retrieving info from gateway";

    public static void setDeviceIpAddress(String ipAddress){
       Device_IP_Address=ipAddress;
    }
}
