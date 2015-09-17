package main.java.android;


/*
    This main view is the default start activity. It create interface where user can start activities including
    bootstrap with Gateway, photo and video camera, etc.
 */

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.icontrol.android.openhomesimulator.camera.CameraSimulatorFactory;
import java.util.List;

public class AndroidCameraMainActivity extends Activity {

    static String registryGwURL;
    static String activationKey;
    static String serialNo;
    static String error_text;
    TextView tv_camera_host_info;
    TextView tv_siteID;
    TextView tv_sharedSecret;
    TextView tv_sessionGw;
    TextView tv_error;
    Button btn_bootStrap;
    Button btn_startServer;
    AndroidHttpRestServer androidHttpRestServer;
    EditText et_gwhost;
    EditText et_camhost;
    EditText et_registryGwUrl;
    EditText et_sericalNumber;
    EditText et_activationKey;
    CameraSimulatorFactory simulatorFactory;
    SharedPreferences appSharedPrefs;
    public static Context context;


    /**
     * Called when the activity is first created.
     */

    public void onCreate(Bundle savedInstanceState) {


        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_demo);
        context=getApplicationContext();

        /*
        btn_bootStrap=(Button)findViewById(R.id.btn_bootStrap);
        btn_bootStrap.setOnClickListener(bootStrapClickListener);
        btn_startServer=(Button)findViewById(R.id.btn_start_server);
        btn_startServer.setOnClickListener(startServerClickListener);
        et_gwhost=(EditText)findViewById(R.id.et_gwhost);
        et_camhost=(EditText)findViewById(R.id.et_camhost);
        et_registryGwUrl=(EditText)findViewById(R.id.et_registryGwURL);
        et_sericalNumber=(EditText)findViewById(R.id.et_serialNumber);
        et_activationKey=(EditText)findViewById(R.id.et_activationKey);
        tv_siteID=(TextView)findViewById(R.id.tv_siteID);
        tv_sharedSecret=(TextView)findViewById(R.id.tv_sharedSecret);
        tv_sessionGw=(TextView)findViewById(R.id.tv_sessionGw);
        tv_camera_host_info=(TextView)findViewById(R.id.tv_camera_host_info);
        tv_error=(TextView)findViewById(R.id.tv_error);
        */
        btn_startServer=(Button)findViewById(R.id.btn_start_server_demo);
        et_camhost=(EditText)findViewById(R.id.et_camhost_demo);
        tv_camera_host_info=(TextView)findViewById(R.id.tv_camera_host_info_demo);
        tv_error=(TextView)findViewById(R.id.tv_error_demo);
        btn_startServer.setOnClickListener(startServerClickListener);

        //obtain device ip address in the LAN
        AndroidCameraConstants.setDeviceIpAddress(getDeviceIpAddress());
        et_camhost.setText("http://"+AndroidCameraConstants.Device_IP_Address+":"+AndroidCameraConstants.HTTP_Port);


        //restore from previous preference settings
        appSharedPrefs= PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());

        Runtime rt = Runtime.getRuntime();
        long maxMemory = rt.maxMemory();
        Log.v("onCreate", "maxMemory:" + Long.toString(maxMemory));

        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        int memoryClass = am.getMemoryClass();
        Log.v("onCreate", "memoryClass:" + Integer.toString(memoryClass));
    }

    // bootStrap button
    private View.OnClickListener bootStrapClickListener=new View.OnClickListener(){
        public void onClick(View v){
            //get gateway url from input and create camera simulator instance
            try{
                simulatorFactory = CameraSimulatorFactory.createInstance();
                String gwhost=et_gwhost.getText().toString();
                simulatorFactory.setContextPath(gwhost);
            }
            catch (Exception e){
                System.out.println("Failed to create cameraSimulatorFactory");
            }
            registryGwURL=et_registryGwUrl.getText().toString();
            activationKey=et_activationKey.getText().toString();
            serialNo=et_sericalNumber.getText().toString();
            new ClientTask().execute("");
            if (error_text==null){
                try{
                    tv_siteID.setText(simulatorFactory.getSiteID(serialNo));
                    tv_sharedSecret.setText(simulatorFactory.getSharedSecret(serialNo));
                    tv_sessionGw.setText(simulatorFactory.getSessionGw(serialNo));
                }
                catch (Exception e){
                    e.printStackTrace();
                    tv_error.setText("Failed to get SharedSecret from Gateway");
                }
            }
            else{
                tv_error.setText(error_text);
                error_text=null;
            }
        }
    };

    private View.OnClickListener startServerClickListener=new View.OnClickListener(){
        public void onClick(View v){
           startServer();
        }
    };

    //start camera server
    private void startServer(){
        try{
            String camhost=et_camhost.getText().toString();
            androidHttpRestServer = new AndroidHttpRestServer(camhost);
            try {
                androidHttpRestServer.start();
                tv_camera_host_info.setText("Camera host started @ "+camhost);
                System.out.println("Server started @ "+camhost);
            }
            catch (Exception e){
                System.out.println("Error to1 start the Rest Server.");
                tv_camera_host_info.setText("Camera host failed.");
                androidHttpRestServer.stop();
            }
        }
        catch (Exception e){
            System.out.println("Error in start Http Server.");
        }
    }

    //run bootStrap process on a separate thread
    private class ClientTask extends AsyncTask<String, Void, String>{
        int retCode;
        protected String doInBackground(String... params){
            try{
                retCode=simulatorFactory.startBootStrap(registryGwURL, serialNo, activationKey, null);
                if (retCode!=200){
                    error_text= AndroidCameraConstants.Error_Retrieve_Info;
                }
            }
            catch (Exception e){
                e.printStackTrace();
                error_text=e.getMessage();
            }
            return null;
        }
        protected void onProgressUpdate(Void... values) {
        }

        protected void onPostExecute(String result) {

        }
    }

    //get device ip address in the LAN
    public String getDeviceIpAddress(){
        WifiManager wim= (WifiManager) getSystemService(WIFI_SERVICE);
        List<WifiConfiguration> l =  wim.getConfiguredNetworks();
        if (l.size()==0){
            Toast.makeText(getApplicationContext(), "No wifi connected. App shut down.", Toast.LENGTH_SHORT).show();
            onDestroy();
            return null;
        }
        else{
        WifiConfiguration wc = l.get(0);
        return Formatter.formatIpAddress(wim.getConnectionInfo().getIpAddress());
        }
    }

    protected void onDestroy(){
        super.onDestroy();
        try{
            androidHttpRestServer.stop();
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}


