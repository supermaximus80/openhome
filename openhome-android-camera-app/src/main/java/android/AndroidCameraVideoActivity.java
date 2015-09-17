package main.java.android;


import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.*;
import android.util.Base64;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.icontrol.android.openhomesimulator.camera.AndroidMjpegEncoder;
import com.icontrol.android.openhomesimulator.camera.MediaResourceLoader;
import com.icontrol.android.openhomesimulator.camera.MediaUploader;
import com.icontrol.openhome.data.MediaUpload;
import org.apache.commons.io.FileUtils;


public class AndroidCameraVideoActivity extends Activity implements SurfaceHolder.Callback{

    private static final String TAG = "VIDEO_CAMERA";

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private MediaRecorder mediaRecorder;
    private final int maxDurationInMs = 20000;
    private final long maxFileSizeInBytes = 500000;
    private final int videoFramesPerSecond = 20;
    private boolean recording=false;
    public static File mediaFile;
    static PowerManager.WakeLock wl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_surface);
        AndroidMjpegEncoder.cameraRunning=true; //set cameraRunning True

        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceView.setClickable(true);
        surfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (recording==false){
                    startRecording();
                    recording=true;
                }
                else{
                    stopRecording();
                    recording=false;
                }
            }
        });
        waitForCameraInitiation();

        //keep device awake
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        AndroidMjpegEncoder.cameraRunning=true;
        camera = Camera.open(0);
        if (camera != null){
            Camera.Parameters params = camera.getParameters();
            camera.setParameters(params);
        }
        else {
            Toast.makeText(getApplicationContext(), "Camera not available!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(height, width);
        camera.setParameters(parameters);

        try {
            camera.setPreviewDisplay(holder);
            camera.setDisplayOrientation(90);
            camera.startPreview();
        }
        catch (IOException e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
    }


    public boolean startRecording(){
        try {
            camera.unlock();
            mediaRecorder = new MediaRecorder();
            mediaRecorder.setCamera(camera);
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setMaxDuration(maxDurationInMs);

            mediaFile=new File(createOutputFileName());

            mediaRecorder.setOutputFile(mediaFile.toString());
            mediaRecorder.setVideoFrameRate(videoFramesPerSecond);
            mediaRecorder.setVideoSize(surfaceView.getHeight(), surfaceView.getWidth());

            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);

            mediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

            mediaRecorder.setMaxFileSize(maxFileSizeInBytes);

            mediaRecorder.prepare();
            mediaRecorder.start();

            return true;
        } catch (IllegalStateException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void stopRecording(){

        AndroidCameraVariables.videoFile=mediaFile;
        mediaRecorder.stop();
        camera.lock();

    }

    //create OutputFileName for output Video file in SDcard/picture/MyCameraApp/VID_Openhome_Video.mp4
    private static String createOutputFileName(){
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                //return null;
                mediaFile=null;
            }
        }
        return mediaStorageDir.getPath() + File.separator +"VID_"+ "Openhome_Video" + ".mp4";
    }

    //timers to start and stop video taking, and then quit Camera Activity

    public void waitForCameraInitiation(){
        new CountDownTimer(1000,1000){
            public void onFinish(){
                videoTakingTimer();
            }
            public void onTick(long l){

            }
        }.start();
    }

    public void videoTakingTimer(){
        surfaceView.performClick();
        new CountDownTimer(5000,1000){
            public void onFinish(){
                surfaceView.performClick();
                quitTimer();
            }
            public void onTick(long millisUntilFinished){
            }
        }.start();
    }

    public void quitTimer(){
        new CountDownTimer(10000,1000){
            public void onFinish(){
                AndroidMjpegEncoder.cameraRunning=false;
                wl.release();
                finish();
            }
            public void onTick(long millisUntilFinished){
            }
        }.start();
    }


}
