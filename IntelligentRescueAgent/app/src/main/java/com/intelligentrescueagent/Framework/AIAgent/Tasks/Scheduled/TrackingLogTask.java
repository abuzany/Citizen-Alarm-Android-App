package com.intelligentrescueagent.Framework.AIAgent.Tasks.Scheduled;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.AgentTrackingLog;
import com.intelligentrescueagent.Models.User;
import com.intelligentrescueagent.Models.UserConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzan y on 20/07/2016.
 */
public class TrackingLogTask extends TimerTask {
    private static final String TAG = "TrackLogTask";

    private Socket mSocket;
    private String mUserId;
    private AppCompatActivity mContext;
    private LatLng mLatLng;
    private APIService mHTTPClient;

    public TrackingLogTask(Socket socket, String userId, AppCompatActivity context){
        this.mSocket = socket;
        this.mUserId = userId;
        this.mContext = context;

        mHTTPClient = ServiceGenertor.createService(APIService.class);
    }

    @Override
    public void run() {
        //Avoid to add to tail the requests
        /* if(isServerConnected() && mIsSignedIn) {
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

                data.put("userId", mUser.getFacebookID());
                data.put("latitude", mLatLng.latitude);
                data.put("longitude", mLatLng.longitude);

                mSocket.emit("onAgentTracking", data); //update agent location

                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                //Insert in db
                AgentTrackingLog atl = new AgentTrackingLog();
                atl.setUserId(mUser.getFacebookID());
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
        }*/
        try {
            //Get the location from main thread
            Handler mainHandler = new Handler(mContext.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    GPSTracker mGPS = new GPSTracker(mContext);
                    //Check if GPS enabled
                    if (mGPS.canGetLocation()) {
                        mLatLng = new LatLng(mGPS.getLatitude(), mGPS.getLongitude());
                    }
                }
            });

            JSONObject data = new JSONObject();
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
        }catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        catch (Exception e){
            Log.e(TAG, e.getMessage());
        }
    }
}
