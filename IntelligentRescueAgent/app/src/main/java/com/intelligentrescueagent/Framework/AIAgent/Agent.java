package com.intelligentrescueagent.Framework.AIAgent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.Alert;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class Agent extends Service{

    private final IBinder mBinder = new AgentBinder();

    private static final int READY = 1;
    private static final int WAITING = 2;
    private static final int WARNING = 3;
    private static final int REPORTING = 4;
    private static final String GROUP_KEY_AlARMS = "group_key_alarms";

    private IntentFilter intentFilter;
    private MainActivity mapsActivity;

    private double _latitude;
    private double _longitude;
    private String _userId;
    private int _status;

    private Socket socket;
    {
        try{
            socket = IO.socket("http://192.168.1.69:3000");
        }catch(URISyntaxException e){
            throw new RuntimeException(e);
        }
    }

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

        return mBinder;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();

        Toast.makeText(this, "Agent Destroyed", Toast.LENGTH_SHORT).show();
    }

    //Methods
    public void connectToRemoteServer(){
        socket.connect();
        socket.on("onJoin", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                String status = data.optString("status");

                if (status.equals("200")) {
                    _status = READY;
                    AgentTask agentTask = new AgentTask();
                    agentTask.run();
                } else
                    _status = WAITING;

            }
        });

        socket.on("onAlert", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                try {
                    //Retrieving information
                    JSONObject data = (JSONObject) args[0];
                    Alert alert = new Alert();
                    //alert.setUserId(data.getString("userId"));
                    alert.setAlertType(data.getInt("alertTypeId"));
                    alert.setLatitude(data.getDouble("latitude"));
                    alert.setLongitude(data.getDouble("longitude"));

                    //Build notification content
                    String notiTitle = "";
                    String notiContent = "";

                    switch (alert.getAlertType()){

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

                    //Create and throw notification
                    NotificationCompat.Builder notification = new NotificationCompat.Builder(mapsActivity);
                    notification.setSmallIcon(android.R.drawable.stat_notify_chat);
                    notification.setContentTitle(notiTitle);
                    notification.setContentText(notiContent);
                    notification.setGroup(GROUP_KEY_AlARMS);
                    notification.setTicker("Alerta!!!");
                    notification.setAutoCancel(true);

                    Intent intent = new Intent(mapsActivity, MainActivity.class);
                    intent.putExtra(String.valueOf(alert.getLatitude()), "alertLatitude");
                    intent.putExtra(String.valueOf(alert.getLongitude()), "alertLongitude");

                    PendingIntent pendientIntent =  PendingIntent.getActivity(mapsActivity, 0, intent, 0);

                    notification.setContentIntent(pendientIntent);

                    NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(10, notification.build());
                } catch (JSONException e) {
                    Log.e("Agent", "onAlert: Error retrieving information, " + e.getMessage());
                }catch (Exception e){
                    Log.e("Agent", "onAlert: " + e.getMessage());
                }
            }
        });

        JSONObject data = new JSONObject();
        try {
            data.put("userId", _userId);
            data.put("latitude", _latitude);
            data.put("longitude", _longitude);
        } catch (JSONException e) {
            Log.e("Agent", e.getMessage());
        }

        socket.emit("onJoin", data);
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

    //Getters and Setters
    public double getLatitude() {
        return  _latitude;
    }

    public void setLatitude(double latitude)  {
        _latitude = latitude;
    }

    public double getLongitude() {
        return  _longitude;
    }

    public void setLongitude(double longitude)  {
        _longitude = longitude;
    }

    public String getUserId() {
        return _userId ;
    }

    public void setUserId(String userId)  {
        _userId = userId;
    }

    public int getStatus(){
        return _status;
    }

    public void setStatus(int status){
        _status = status;
    }

    public MainActivity getMapsActivity() {
        return mapsActivity;
    }

    public void setMapsActivity(MainActivity mapsActivity) {
        this.mapsActivity = mapsActivity;
    }

    public  boolean isServerConnected(){

        if(socket != null)
            return  socket.connected();
        else
            return  false;
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
