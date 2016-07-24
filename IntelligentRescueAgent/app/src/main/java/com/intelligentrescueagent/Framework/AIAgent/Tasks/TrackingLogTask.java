package com.intelligentrescueagent.Framework.AIAgent.Tasks;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.intelligentrescueagent.Framework.GPS.GPSTracker;
import com.intelligentrescueagent.Models.AgentTrackingLog;
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
    @Override
    public void run() {

    }
}
