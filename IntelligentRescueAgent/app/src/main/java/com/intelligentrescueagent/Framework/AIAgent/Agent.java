package com.intelligentrescueagent.Framework.AIAgent;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.SendAlertTask;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.UpdateUserConfigurationTask;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Framework.Settings.GlobalSettings;
import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.AgentTrackingLog;
import com.intelligentrescueagent.Models.Alert;
import com.intelligentrescueagent.Models.UserConfiguration;
import com.intelligentrescueagent.ValidateLoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class Agent extends Service{

    private final IBinder mBinder = new AgentBinder();

    private static final String GROUP_KEY_AlARMS = "group_key_alerts";
    private static final String TAG = "Agent";

    private AppCompatActivity mContext;
    private ServerListener mServerListener;
    private Timer mTimer;
    private Timer mTimerTL;
    private GPSTracker mGPS;
    private APIService mHTTPClient;
    private LatLng mLatLng;
    private Socket mSocket;
    {
        try{
            mSocket = IO.socket(GlobalSettings.getInstance().getSocketIOAddress());
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    private String mUserId;
    private Boolean mIsSignedIn;

    @Override
    public void onCreate(){
        super.onCreate();
        Toast.makeText(this, "Agent Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Agent Started", Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "Agent Binded", Toast.LENGTH_SHORT).show();

        init();
        agentUp();

        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        Toast.makeText(this, "Agent Destroyed", Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////Methods//////////////////////////////////////////////
    private void init(){
        //Initialize objects and variables
        mTimer = new Timer();
        mTimerTL = new Timer();
        mHTTPClient = ServiceGenertor.createService(APIService.class);
        mIsSignedIn = false;
    }

    private void listenContext(){
        //Listen events from server
        mSocket.on("onSignedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                Integer status = Integer.parseInt(data.optString("status"));
                String msg = data.optString("msg");

                mServerListener.onSignedIn(status, msg);
            }
        });

        mSocket.on("onAlert", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    //Retrieving information
                    JSONObject data = (JSONObject) args[0];
                    Alert alert = new Alert();
                    alert.setAlertType(data.getInt("alertTypeId"));
                    alert.setLatitude(data.getDouble("latitude"));
                    alert.setLongitude(data.getDouble("longitude"));
                    alert.setCreationDate(data.getString("creationDate"));
                    alert.setDescription(data.getString("description"));
                    alert.setAddress(data.getString("address"));

                    //Build notification content
                    String notiTitle = "";
                    String notiContent = "";

                    switch (alert.getAlertType()) {

                        case 1:
                            notiTitle = "Robo Reportado!!!";
                            break;

                        case 2:
                            notiTitle = "Accidente Reportado!!!";
                            break;

                        case 3:
                            notiTitle = "Secuestro Reportado!!!";
                            break;
                    }

                    // Build the notification, setting the group appropriately
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
                    notification.setSmallIcon(android.R.drawable.stat_notify_chat);
                    notification.setContentTitle(notiTitle);
                    notification.setContentText(notiContent);
                    notification.setGroup(GROUP_KEY_AlARMS);
                    notification.setTicker("Alerta!!!");
                    notification.setAutoCancel(true);

                    Intent intent = new Intent(mContext, ValidateLoginActivity.class);
                    intent.putExtra("Agent", "1");
                    intent.putExtra("alertType", String.valueOf(alert.getAlertType()));
                    intent.putExtra("latitude", String.valueOf(alert.getLatitude()));
                    intent.putExtra("longitude", String.valueOf(alert.getLongitude()));
                    intent.putExtra("description", String.valueOf(alert.getDescription()));
                    intent.putExtra("address", String.valueOf(alert.getAddress()));

                    PendingIntent pendientIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                    notification.setContentIntent(pendientIntent);

                    // Issue the notification
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(10, notification.build());

                    if(MainActivity.IsRuning)
                        mServerListener.onAlertReceived(alert);//Dispatch event
                } catch (JSONException e) {
                    Log.e(TAG, "onAlert: Error retrieving information, " + e.getMessage());
                } catch (Exception e) {
                    Log.e(TAG, "onAlert: " + e.getMessage());
                }
            }
        });

        mSocket.on("onIsUserSignedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Boolean result = Boolean.parseBoolean(args[0].toString());
                if (!result){
                    mIsSignedIn = false;
                    signIn();
                }
                else
                    mIsSignedIn = true;
            }
        });
    }

    private void agentUp(){
        connectToRemoteServer();
        listenContext();
        beginCheckSignIn();
        beginTrackingLocation();
    }

    private void connectToRemoteServer(){
        mSocket.connect();
    }

    private boolean isServerConnected(){
        if(mSocket != null)
            return  mSocket.connected();
        else
            return  false;
    }

    private void signIn(){
        JSONObject data = new JSONObject();
        try {
            //Get the location from main thread
            Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mGPS = new GPSTracker(mContext);
                    //Check if GPS enabled
                    if (mGPS.canGetLocation()) {
                        mLatLng = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
                    }
                }
            });

            data.put("userId", mUserId);
            data.put("latitude", mLatLng.latitude);
            data.put("longitude", mLatLng.longitude);
            data.put("roleId", 2);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        mSocket.emit("onSignIn", data);
    }

    private void beginTrackingLocation(){
        mTimerTL.schedule(new TimerTask() {
            @Override
            public void run() {
                //Avoid to add to tail the requests
                if(isServerConnected() && mIsSignedIn){
                    JSONObject data = new JSONObject();
                    try {
                        //Get the location from main thread
                        Handler mainHandler = new Handler(mContext.getMainLooper());
                        mainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mGPS = new GPSTracker(mContext);
                                //Check if GPS enabled
                                if (mGPS.canGetLocation()) {
                                    mLatLng = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
                                }
                            }
                        });

                        data.put("userId", mUserId);
                        data.put("latitude", mLatLng.latitude);
                        data.put("longitude", mLatLng.longitude);

                        mSocket.emit("onAgentTracking", data); //update agent location

                        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                        Date date = new Date();

                        //Insert in db
                        AgentTrackingLog atl = new AgentTrackingLog();
                        atl.setUserId(mUserId);
                        atl.setLatitude(mLatLng.latitude);
                        atl.setLongitude(mLatLng.longitude);
                        atl.setLogDate(dateFormat.format(date));

                        Call<AgentTrackingLog> callPostATL = mHTTPClient.postATL(atl);

                        callPostATL.enqueue(new Callback<AgentTrackingLog>() {
                            @Override
                            public void onResponse(Call<AgentTrackingLog> call, Response<AgentTrackingLog> response) {
                                Log.d(TAG, "PostATL-onResponse: " + response.code());
                            }

                            @Override
                            public void onFailure(Call<AgentTrackingLog> call, Throwable t) {
                                Log.e(TAG, "PostATL-onFailure: " + t.getMessage());
                            }
                        });
                    } catch (JSONException e) {
                        Log.e(TAG, "beginTrackingLocation" + e.getMessage());
                    }
                }
            }
        }, 0,60000);
    }

    private void beginCheckSignIn(){
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //Avoid to add to tail the requests
                if(isServerConnected()){
                    mSocket.emit("onIsUserSignedIn", mUserId);
                }
            }
        }, 0,5000);
    }

    public void registerClient(Activity activity){
        mServerListener = (ServerListener)activity;
    }

    public void updateUserConfiguration(UserConfiguration usrConfig){
        new UpdateUserConfigurationTask().execute(usrConfig, mSocket);
    }

    public void sendAlert(Alert alert){
        new SendAlertTask().execute(alert, mSocket);
    }

    public interface ServerListener{
        void onAlertReceived(Alert alert);
        void onSignedIn(int result, String msg);
    }

    ////////////////////////////////////////Getters and Setters/////////////////////////////////////

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId)  {
        mUserId = userId;
    }

    public AppCompatActivity getContext() {
        return mContext;
    }

    public void setContext(AppCompatActivity context) {
        this.mContext = context;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class AgentBinder extends Binder {
        public Agent getService() {
            // Return this instance of Agent so clients can call public methods
            return Agent.this;
        }
    }
}
