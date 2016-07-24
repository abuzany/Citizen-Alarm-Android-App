package com.intelligentrescueagent.Framework.AIAgent.Tasks;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.Alert;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzany on 10/01/2016.
 */
public class SendAlertTask extends AsyncTask<Object, Void, Object> {
    private static final String TAG  = "SendAlertTask";

    private APIService mHTTPClient;

    public SendAlertTask(){
        mHTTPClient = ServiceGenertor.createService(APIService.class);
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            Alert alert = (Alert) params[0];
            Socket socket = (Socket) params[1];

            JSONObject data = new JSONObject();
            data.put("userId", alert.getUserId());
            data.put("alertTypeId", alert.getAlertType());
            data.put("latitude", alert.getLatitude());
            data.put("longitude", alert.getLongitude());

            socket.emit("onAlert", data);

            Call<Alert> callPostAlert = mHTTPClient.postAlert(alert);
            callPostAlert.enqueue(new Callback<Alert>() {
                @Override
                public void onResponse(Call<Alert> call, Response<Alert> response) {
                    Log.d(TAG, "PostAlert->onResponse: " + response.body());
                }

                @Override
                public void onFailure(Call<Alert> call, Throwable t) {
                    Log.e(TAG, "PostAlert->onFailure: " + t.getMessage());
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
