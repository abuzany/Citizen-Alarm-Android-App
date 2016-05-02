package com.intelligentrescueagent.Framework.AIAgent;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

/**
 * Created by Angel Buzany on 14/04/2016.
 */
public class TaskSignin extends Goal{

    private double mLatitude;
    private double mLongitude;
    private String mUserId;

    @Override
    public void run() {
        Comunicator.getInstance().getSocket().on("onJoin", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                JSONObject data = (JSONObject) args[0];

                String status = data.optString("status");
            }
        });

        JSONObject data = new JSONObject();
        try {
            data.put("userId", mUserId);
            data.put("latitude", mLatitude);
            data.put("longitude", mLongitude);
        } catch (JSONException e) {
            Log.e("TaskSignin", e.getMessage());
        }

        Comunicator.getInstance().getSocket().emit("onJoin", data);
    }

    ////////////////////////////Getters and Setters///////////////////////////////////////
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
}
