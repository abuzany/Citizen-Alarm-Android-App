package com.intelligentrescueagent.Framework.AIAgent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.Async.SendAlertTask;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.Async.UpdateUserConfigurationTask;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.Scheduled.CheckSginInTask;
import com.intelligentrescueagent.Framework.AIAgent.Tasks.Scheduled.TrackingLogTask;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Framework.Settings.GlobalSettings;
import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.AgentTrackingLog;
import com.intelligentrescueagent.Models.Alert;
import com.intelligentrescueagent.Models.KatinkResponse;
import com.intelligentrescueagent.Models.User;
import com.intelligentrescueagent.Models.UserConfiguration;
import com.intelligentrescueagent.R;
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
public class Agent extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final IBinder mBinder = new AgentBinder();

    private static final String GROUP_KEY_AlARMS = "group_key_alerts";
    private static final String TAG = "Agent";

    private AppCompatActivity mContext;
    private ServerListener mServerListener;
    private Timer mTimer;
    private Timer mTimerTL;
    private GPSTracker mGPS;
    private APIService mHTTPClient;
    private Location mLatLng;
    private Socket mSocket;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private String mUserId;
    {
        try{
            mSocket = IO.socket(GlobalSettings.getInstance().getSocketIOAddress());
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }
    private Boolean mIsSignedIn;

    ///////////////////////////////////////////////Events///////////////////////////////////////////
    @Override
    public void onCreate(){
        super.onCreate();

        init();

        Toast.makeText(this, "Agent Created", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        super.onStartCommand(intent, flags, startId);
        Toast.makeText(this, "Agent Started", Toast.LENGTH_SHORT).show();

        if (intent.hasExtra("action")) {
            String action = intent.getStringExtra("action");
            if(action.equalsIgnoreCase("up")){

                mUserId = intent.getStringExtra("userId");

                agentUp();
            }
        } else {
            Log.d(TAG, "Received intent with action="+intent.getAction()+"; now what?");
        }

        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(this, "Agent Binded", Toast.LENGTH_SHORT).show();

        init();

        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        googleApiClient.disconnect();
        Toast.makeText(this, "Agent Destroyed", Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////Methods//////////////////////////////////////////////
    private void init(){
        //Initialize objects and variables
        mTimer = new Timer();
        mTimerTL = new Timer();
        mHTTPClient = ServiceGenertor.createService(APIService.class);
        mIsSignedIn = false;

        startTracking();
    }

    private void listenContext(){
        mSocket.on("onSignedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try{
                    JSONObject data = (JSONObject) args[0];
                    KatinkResponse kr = new KatinkResponse();
                    kr.setCode(data.getInt("code"));
                    kr.setMsg(data.getString("msg"));

                    mServerListener.onSignedIn(kr);
                } catch (Exception e) {
                    Log.e(TAG, "onSignedIn: " + e.getMessage());
                }
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

                    //If the app is running dispatch an event with the alert content,
                    //else create an notification
                    if(MainActivity.IsRuning) {
                        KatinkResponse kr = new KatinkResponse();
                        kr.setCode(data.getInt("code"));
                        kr.setMsg(data.getString("msg"));
                        kr.setContent(alert);

                        mServerListener.onAlertReceived(kr);//Dispatch event
                    }
                    else{
                        //Build notification content
                        String notiTitle = "";
                        String notiContent = "";

                        switch (alert.getAlertType()) {
                            case 1:
                                notiTitle = R.string.noti_roobery_reported_text + "!!!";
                                break;

                            case 2:
                                notiTitle = R.string.noti_accident_reported_text + "!!!";
                                break;

                            case 3:
                                notiTitle = R.string.noti_kidnaping_reported_text +"!!!";
                                break;
                        }

                        // Build the notification, setting the group appropriately
                        NotificationCompat.Builder notification = new NotificationCompat.Builder(mContext);
                        notification.setSmallIcon(android.R.drawable.stat_notify_chat);
                        notification.setContentTitle(notiTitle);
                        notification.setContentText(notiContent);
                        notification.setGroup(GROUP_KEY_AlARMS);
                        notification.setTicker(R.string.alert + "!!!");
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
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "onAlert: " + e.getMessage());
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
            data.put("userId", mUserId);
            data.put("latitude", mLatLng.getLongitude());
            data.put("longitude", mLatLng.getLongitude());
            data.put("roleId", 2);

            mSocket.emit("onSignIn", data);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    private void beginTrackingLocation(){
        mTimerTL.schedule(new TrackingLogTask(mSocket, mUserId, mContext), 0, 3000);
    }

    private void beginCheckSignIn(){
        mTimer.schedule(new CheckSginInTask(mSocket, mUserId), 0,5000);
    }

    private void startTracking(){
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            googleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();

            if (!googleApiClient.isConnected() || !googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            Log.e(TAG, "unable to connect to google play services.");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000); // milliseconds
        locationRequest.setFastestInterval(1000); // the fastest rate in milliseconds at which your app can handle location updates
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            Log.e(TAG, "position: " + location.getLatitude() + ", " + location.getLongitude());
            mLatLng = location;
        }
    }

    public interface ServerListener{
        void onAlertReceived(KatinkResponse kr);
        void onSignedIn(KatinkResponse kr);
    }

    ////////////////////////////////////////Getters and Setters/////////////////////////////////////
    public void setContext(AppCompatActivity context) {
        this.mContext = context;

        mServerListener = (ServerListener)context;
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
