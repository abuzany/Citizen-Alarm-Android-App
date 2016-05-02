package com.intelligentrescueagent.Framework.AIAgent;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.intelligentrescueagent.Framework.Settings.GlobalSettings;
import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.Alert;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class Agent extends Service{

    private final IBinder mBinder = new AgentBinder();

    private static final String GROUP_KEY_AlARMS = "group_key_alerts";

    private AppCompatActivity mContext;
    private ServerListener serverListener;
    private Timer mTimer;

    private double mLatitude;
    private double mLongitude;
    private String mUserId;

    private Socket socket;
    {
        try{
            socket = IO.socket(GlobalSettings.getInstance().getSocketIOAddress());
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCreate(){
        super.onCreate();

        mTimer = new Timer();

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

        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {

                //Avoid to add to tail the requests
                if(isServerConnected())
                    socket.emit("onIsUserSignedIn", mUserId);
            }
        }, 0,5000);

        //Listen events from server

        socket.on("onSignedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                Boolean status = Boolean.parseBoolean(data.optString("status"));
                String msg = data.optString("msg");

                serverListener.onSignedIn(status, msg);
            }
        });

        socket.on("onAlert", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    //Retrieving information
                    JSONObject data = (JSONObject) args[0];
                    Alert alert = new Alert();
                    alert.setAlertType(data.getInt("alertTypeId"));
                    alert.setLatitude(data.getDouble("latitude"));
                    alert.setLongitude(data.getDouble("longitude"));

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

                    Intent intent = new Intent(mContext, MainActivity.class);
                    intent.putExtra(String.valueOf(alert.getLatitude()), "alertLatitude");
                    intent.putExtra(String.valueOf(alert.getLongitude()), "alertLongitude");

                    PendingIntent pendientIntent = PendingIntent.getActivity(mContext, 0, intent, 0);

                    notification.setContentIntent(pendientIntent);

                    // Issue the notification
                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(10, notification.build());

                    //Dispatch event
                    serverListener.onAlertReceived(alert);
                } catch (JSONException e) {
                    Log.e("Agent", "onAlert: Error retrieving information, " + e.getMessage());
                } catch (Exception e) {
                    Log.e("Agent", "onAlert: " + e.getMessage());
                }
            }
        });

        socket.on("onIsUserSignedIn", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Boolean result = Boolean.parseBoolean(args[0].toString());

                if (!result)
                    signIn();
            }
        });


        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Toast.makeText(this, "Agent Destroyed", Toast.LENGTH_SHORT).show();
    }

    ///////////////////////////////////////////Methods//////////////////////////////////////////////

    public void connectToRemoteServer(){
        socket.connect();
    }

    public void signIn(){
        JSONObject data = new JSONObject();
        try {
            data.put("userId", mUserId);
            data.put("latitude", mLatitude);
            data.put("longitude", mLongitude);
        } catch (JSONException e) {
            Log.e("Agent", e.getMessage());
        }

        socket.emit("onSignIn", data);
    }

    public void sendAlert(int alertType, double latitude, double longitude){
        JSONObject data = new JSONObject();
        try {
            data.put("alertTypeId", alertType);
            data.put("latitude", latitude);
            data.put("longitude", longitude);
        } catch (JSONException e) {
            Log.e("Agent", e.getMessage());
        }

        socket.emit("onAlert", data);
    }

    public  boolean isServerConnected(){
        if(socket != null)
            return  socket.connected();
        else
            return  false;
    }

    public void registerClient(Activity activity){
        serverListener = (ServerListener)activity;
    }

    public interface ServerListener{
        void onAlertReceived(Alert data);
        void onSignedIn(boolean result, String msg);
    }

    ////////////////////////////////////////Getters and Setters/////////////////////////////////////

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude)  {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude)  {
        mLongitude = longitude;
}

    public String getUserId() {
        return mUserId;
    }

    public void setUserId(String userId)  {
        mUserId = userId;
    }

    public AppCompatActivity getContext() {
        return mContext;
    }

    public void setConetext(AppCompatActivity context) {
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
