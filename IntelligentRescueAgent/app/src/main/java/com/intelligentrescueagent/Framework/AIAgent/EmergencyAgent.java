package com.intelligentrescueagent.Framework.AIAgent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.intelligentrescueagent.MainActivity;
import com.intelligentrescueagent.Models.Alert;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.emitter.Emitter;

/**
 * Created by Angel Buzany on 14/04/2016.
 */
public class EmergencyAgent extends Service{

    private final IBinder mBinder = new EmergencyAgentBinder();

    private static final int READY = 1;
    private static final int WAITING = 2;
    private static final int WARNING = 3;
    private static final int REPORTING = 4;
    private static final String GROUP_KEY_AlARMS = "group_key_alarms";

    private AppCompatActivity mContext;

    private double mLatitude;
    private double mLongitude;
    private String mUserId;
    private int mStatus;

    private ArrayList<Behaviour> mBehaviourList = new ArrayList<Behaviour>();

    @Override
    public void onCreate(){
        super.onCreate();

        Toast.makeText(this, "Agent Created", Toast.LENGTH_SHORT).show();

        Behaviour bhrMonitor = new Behaviour("MONITOR");

        Capability capReceiveAlerts = new Capability("RECEIVE_ALERTS");
        Capability capSendAlerts = new Capability("SEND_ALERTS");

        capReceiveAlerts.setGoal(new TaskReceiveAlerts());

        bhrMonitor.addCapability(capReceiveAlerts);
        bhrMonitor.addCapability(capSendAlerts);

        bhrMonitor.init();
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

        //Toast.makeText(this, "Agent Destroyed", Toast.LENGTH_SHORT).show();
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class EmergencyAgentBinder extends Binder {
        public EmergencyAgent getService() {
            // Return this instance of Agent so clients can call public methods
            return EmergencyAgent.this;
        }
    }

    //////////////////////////////////////Methods /////////////////////////////////////////////////
    //Getters and Setters
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

    public int getStatus(){
        return mStatus;
    }

    public void setStatus(int status){
        mStatus = status;
    }

    public AppCompatActivity getContext() {
        return mContext;
    }

    public void setConetext(AppCompatActivity context) {
        this.mContext = context;
    }

    public  boolean isServerConnected(){
        return true;
    }

    public void connectToRemoteServer(){

    }

    public void sendAlert(int alertType, double latitude, double longitude){

    }
}
