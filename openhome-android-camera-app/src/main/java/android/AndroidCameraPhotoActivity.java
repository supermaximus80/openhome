package main.java.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.icontrol.android.openhomesimulator.camera.AndroidMjpegEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class AndroidCameraPhotoActivity extends Activity {

    TextView txt;  //text to show time left on timers
    Button btn; //invisible "taking picture" button
    PictureResource pictureResource; //customized controller on camera activity

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camerapreview);
        AndroidMjpegEncoder.cameraRunning=true;
        btn = new Button(this);
        btn.setVisibility(View.INVISIBLE);
        FrameLayout frameLayout=(FrameLayout)findViewById(R.id.cameraPreviewPicture);
        txt = (TextView)findViewById(R.id.tv_text);
        pictureResource = new PictureResource();
        pictureResource.createCamera(this,frameLayout,btn);
        Void v=null;
        new PhotoTask().execute(v);

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
                                    //transform the picture in right orientation
                                    Matrix matrix = new Matrix();
                                    matrix.preRotate(270);
                                    Bitmap workingImg = Bitmap.createBitmap(original, 0, 0,
                                            original.getWidth(), original.getHeight(),
                                            matrix, true);
                                    Matrix mirrorMatrix = new Matrix();
                                    mirrorMatrix.preScale(-1.0f, 1.0f);
                                    workingImg = Bitmap.createBitmap(workingImg, 0, 0, workingImg.getWidth(), workingImg.getHeight(), mirrorMatrix, false);
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    workingImg.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                                    AndroidCameraVariables.photoData=baos.toByteArray();
                                }
                            }
                    );
                    preview.restartPreview();
                }
            });
        }

        //create customized camera preview
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
                            camera.setDisplayOrientation(90);
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

    /*
        new thread to control when to take picture. Wait for camera to initiate first,
         take picture, then quit photo activity.
     */

    private class PhotoTask extends AsyncTask<Void, Void, Void>{
        protected Void doInBackground(Void... params){
            try{
                Thread.sleep(1500);
                btn.performClick();
                Thread.sleep(4000);
            }
            catch (InterruptedException e){
                e.printStackTrace();
                finish();
                AndroidCameraVariables.photoData=null;
            }
            return null;
        }
        protected void onPostExecute(Void result) {
           AndroidMjpegEncoder.cameraRunning=false;
           finish();
        }
    }

    //finish camera activity once app is onPause()
    protected void onPause(){
        super.onPause();
        finish();
    }

    //finish camera activity once app is onStop()
    protected void onStop(){
        super.onStop();
        finish();
    }
}