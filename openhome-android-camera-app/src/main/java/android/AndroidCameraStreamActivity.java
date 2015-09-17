package main.java.android;


import java.io.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.icontrol.android.openhomesimulator.camera.AndroidMjpegEncoder;


public class AndroidCameraStreamActivity extends Activity
{
    private SurfaceView surfaceView = null;
    private SurfaceHolder surfaceHolder = null;
    private Camera camera;
    public static Context context;
    private static Thread writingFrameThread;
    private static WriteFrameThread writingFrameRunnable;
    private static boolean isCriticalFrame;
    private static byte[] imageBuf;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_surface);
        context=this;
        surfaceView = (SurfaceView) findViewById( R.id.surface_camera );
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback( surfaceCallback );
        Timer timer=new Timer();
        timer.schedule(timerTask,0,100);
        writingFrameRunnable=new WriteFrameThread(null, null, null);
        isCriticalFrame=true;
        writingFrameThread=new Thread(writingFrameRunnable);
        writingFrameThread.start();
        Log.e( getLocalClassName(), "END: onCreate" );
    }

    @Override
    public void onConfigurationChanged( Configuration newConfig )
    {
        super.onConfigurationChanged( newConfig );
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated( SurfaceHolder holder )
        {
            //set camera running true. Start writing data to multi-part response
            try{
                camera = Camera.open(0);
                Camera.Parameters parameters = camera.getParameters();
                int w = parameters.getPreviewSize().width;
                int h = parameters.getPreviewSize().height;
                int wpp = ImageFormat.getBitsPerPixel(ImageFormat.NV21);
                int size = w*h*wpp/8;
                imageBuf=new byte[size];
                Timer photoTimer= new Timer();
                photoTimer.schedule(photoTask,0,200);
            }
            catch(Exception e) {
                surfaceDestroyed(holder);
                finish();
            }

            try {
                camera.setPreviewDisplay( holder );
            } catch ( Throwable t )
            {
                Log.e( "surfaceCallback", "Exception in setPreviewDisplay()", t );
                t.printStackTrace();
            }
            Log.e( getLocalClassName(), "END: surfaceCreated" );
        }

        public void surfaceChanged( SurfaceHolder holder, int format, int width, int height )
        {
            if ( camera != null && AndroidMjpegEncoder.cameraRunning){
                camera.setPreviewCallbackWithBuffer( new PreviewCallback() {
                    public void onPreviewFrame( byte[] data, Camera camera ) {
                        if (AndroidMjpegEncoder.outputStream!=null)
                        {
                            //support camera with preview image format as NV21
                            Camera.Parameters parameters = camera.getParameters();
                            int w = parameters.getPreviewSize().width;
                            int h = parameters.getPreviewSize().height;
                            YuvImage yuvImage = new YuvImage( data, ImageFormat.NV21, w, h, null );
                            Rect rect = new Rect( 0, 0, w, h );
                            writingFrameRunnable.setParameters(AndroidMjpegEncoder.outputStream,yuvImage,rect);
                        }
                        else {
                            Log.e( getLocalClassName(), "AndroidMjpegEncoder.outputstream is null." );
                        }

                    }
                });

                Camera.Parameters parameters = camera.getParameters();

                if ( parameters != null )
                {
                    parameters.setPreviewSize( width, height );
                    camera.setParameters( parameters );
                    camera.setDisplayOrientation(90);
                    camera.startPreview();
                }
            }
            else surfaceDestroyed(holder);
        }

        //when preview surface destroyed, stop preview and set cameraRunning false, currentImage null.
        public void surfaceDestroyed(SurfaceHolder holder) {
            if ( camera != null )
            {
                camera.setPreviewCallback(null);
                camera.stopPreview();
                camera.release();
                camera = null;
                AndroidMjpegEncoder.cameraRunning=false;
                AndroidMjpegEncoder.outputStream=null;
            }
            Log.e( getLocalClassName(), "END: surfaceDestroyed" );
        }
    };


    protected void onPause(){
        super.onPause();
        if (camera!=null)
            camera=null;
        AndroidMjpegEncoder.cameraRunning=false;
        AndroidMjpegEncoder.outputStream=null;
        finish();
    }

    private TimerTask timerTask=new TimerTask() {
        @Override
        public void run() {
            if (!AndroidMjpegEncoder.cameraRunning){
                ((AndroidCameraStreamActivity)context).finish();
            }
        }
    };

    private TimerTask photoTask=new TimerTask() {
        @Override
        public void run() {
            if (camera!=null){
                camera.addCallbackBuffer(imageBuf);
            }
        }
    };

    private static byte[] CreateHeader (int length)
    {
        //Header
        String BOUNDARY="ThisRandomString";//TODO : change later
        String header =
                "--" + BOUNDARY + "\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: " + length + "\r\n"+
                        "\r\n";
        return header.getBytes();

    }
    public static byte[] CreateFooter()
    {
        return "\r\n".getBytes();
    }

    public class WriteFrameThread implements Runnable{
        OutputStream st;
        YuvImage yuvImage;
        Rect rect;
        boolean isNewFrame;

        public WriteFrameThread(OutputStream st, YuvImage yuvImage, Rect rect){
            this.st=st;
            this.yuvImage=yuvImage;
            this.rect=rect;
            this.isNewFrame=false;
        }

        public void run(){
            while (true){
                if (isNewFrame){
                    try{
                        byte [] footer=CreateFooter();
                        // prepare header and imageData
                        ByteArrayOutputStream bao= new ByteArrayOutputStream();
                        yuvImage.compressToJpeg(rect, 10, bao);
                        byte[] imageData=bao.toByteArray();
                        int length=imageData.length;
                        byte[] header = CreateHeader(length);
                        Log.v( getLocalClassName(), "start writing." );
                        // Start writing data
                        st.write(header, 0, header.length);
                        st.write(imageData,0, length);
                        st.write(footer, 0, footer.length);
                        st.flush();
                        Log.v( getLocalClassName(), "Written. " + "size: "+(header.length+length+footer.length));
                    }
                    catch (Exception e){
                        e.printStackTrace();
                        if (e instanceof IllegalStateException){
                            Log.e( getLocalClassName(), "IllegalStateException caught in WriteFrameTask." );
                        }
                        else{
                            AndroidMjpegEncoder.cameraRunning=false;
                            AndroidMjpegEncoder.outputStream=null;
                            Log.e( getLocalClassName(), "Exception caught in WriteFrameTask." );
                        }
                    }
                    isNewFrame=false;
                }
            }
        }

        public void setParameters(OutputStream st, YuvImage yuvImage, Rect rect){
            if (!isNewFrame){
                this.st=st;
                this.yuvImage=yuvImage;
                this.rect=rect;
                isNewFrame=true;
            }
        }
    }
}