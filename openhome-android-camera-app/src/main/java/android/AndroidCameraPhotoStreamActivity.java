package main.java.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.icontrol.android.openhomesimulator.camera.AndroidMjpegEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


public class AndroidCameraPhotoStreamActivity extends Activity {

    TextView txt;
    Button btn;
    PictureResource pictureResource;
    static CountDownTimer waitCameraTimer;
    static CountDownTimer photoTimer;
    public static byte[] currentImage;
    public static boolean CAMERA_RUNNING=false;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerapreview);
        btn = new Button(this);
        btn.setVisibility(View.INVISIBLE);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.cameraPreviewPicture);
        pictureResource = new PictureResource();
        pictureResource.createCamera(this,frameLayout,btn);
        txt=(TextView)findViewById(R.id.tv_text);
        waitToInitiateCamera();
    }

    public static class PictureResource {
        Preview preview;
        public void createCamera(Activity activity, View v, Button btn) {
            preview = new Preview(activity);
            ((FrameLayout)v).addView(preview);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    preview.camera.takePicture(
                            null, null,
                            new Camera.PictureCallback() {
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    Bitmap original= BitmapFactory.decodeByteArray(data, 0, data.length);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    original.compress(Bitmap.CompressFormat.JPEG, 20, baos);
                                    //ByteArrayInputStream workingInputStream=new ByteArrayInputStream(baos.toByteArray());
                                    //AndroidMjpegEncoder.currentImage=data;
                                    AndroidMjpegEncoder.cameraRunning=true;
                                }
                            }
                    );
                    preview.restartPreview();
                }
            });
        }


        class Preview extends SurfaceView {
            public Camera camera;
            byte[] data;

            Preview(Context context) {
                super(context);
                SurfaceHolder surfaceHolder = getHolder();
                surfaceHolder.addCallback(new SurfaceHolder.Callback() {

                    @Override
                    public void surfaceCreated(SurfaceHolder surfaceHolder) {
                        try {
                            camera = Camera.open(0);
                            camera.setPreviewDisplay(Preview.this.getHolder());
                            //camera.setDisplayOrientation(90);
                            camera.setPreviewCallback(new Camera.PreviewCallback() {
                                public void onPreviewFrame(byte[] data, Camera arg1) {
                                    Preview.this.data = data;
                                    Preview.this.invalidate();
                                }
                            });
                        } catch (IOException e) {
                            System.out.println("exception here");
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int w, int h) {
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setPreviewSize(w, h);
                        camera.setParameters(parameters);
                        camera.startPreview();

                    }

                    @Override
                    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

                        camera.setPreviewCallback(null);
                        camera.stopPreview();
                        camera.release();
                        camera = null;
                    }
                });
                surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
            }

            @Override
            public void draw(Canvas canvas) {
                super.draw(canvas);
                Paint p = new Paint(Color.RED);
                canvas.drawText("PREVIEW", canvas.getWidth() / 2,  canvas.getHeight() / 2,p);
            }

            public void restartPreview(){
                camera.startPreview();
            }
        }
    }

    public void waitToInitiateCamera(){
        CAMERA_RUNNING=true;
        waitCameraTimer = new CountDownTimer(1500,1000) {
            @Override
            public void onTick(long l) {
                txt.setText("MilliSeconds Left: "+ l +" Initiating Camera...");
            }
            @Override
            public void onFinish() {
                photoTimer();
            }
        }.start();
    }

    public void photoTimer(){
        photoTimer = new CountDownTimer(500,1000){
            public void onFinish(){
                txt.setText("Picture Taken");
                btn.performClick();
                this.start();
            }
            public void onTick(long millisUntilFinished){
                txt.setText("MilliSeconds Left: "+ millisUntilFinished + " Interval is: " +".5"+ " sec");
            }
        }.start();
    }

    //stop camera activity once app is onPause()
    protected void onPause(){
        AndroidMjpegEncoder.cameraRunning=false;
        CAMERA_RUNNING=false;
        super.onPause();
        photoTimer.cancel();
        waitCameraTimer.cancel();
        finish();
    }

    protected void onStop(){
        AndroidMjpegEncoder.cameraRunning=false;
        CAMERA_RUNNING=false;
        super.onStop();
        photoTimer.cancel();
        waitCameraTimer.cancel();
        finish();
    }
}