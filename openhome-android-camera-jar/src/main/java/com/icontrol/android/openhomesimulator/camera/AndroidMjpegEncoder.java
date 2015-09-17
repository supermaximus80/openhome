package com.icontrol.android.openhomesimulator.camera;


import java.io.OutputStream;

public class AndroidMjpegEncoder {
    public static boolean cameraRunning=false;
    public static OutputStream outputStream;

    public AndroidMjpegEncoder(){
        super();
    }

    public void Render(OutputStream st)
    {
        outputStream=st;
        System.out.println("=======Get the outputStream=======");
        while (AndroidMjpegEncoder.cameraRunning){
        }
    }
}
