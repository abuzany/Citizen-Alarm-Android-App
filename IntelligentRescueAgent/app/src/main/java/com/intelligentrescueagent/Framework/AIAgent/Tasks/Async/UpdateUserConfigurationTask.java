package com.intelligentrescueagent.Framework.AIAgent.Tasks.Async;

import android.os.AsyncTask;
import android.util.Log;

import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.UserConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buany on 20/07/2016.
 */
public class UpdateUserConfigurationTask extends AsyncTask<Object, Void, Object> {
    private static final String TAG  = "UptUsrConfigTask";

    private APIService mHTTPClient;

    public UpdateUserConfigurationTask(){
        mHTTPClient = ServiceGenertor.createService(APIService.class);
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            UserConfiguration usrConfig = (UserConfiguration) params[0];
            Socket socket = (Socket) params[1];

            JSONObject data = new JSONObject();
            data.put("userId", usrConfig.getFacebookID());
            data.put("isEnabledNotifications", usrConfig.isEnabledNotifications());
            data.put("range", usrConfig.getRange());

            socket.emit("onUpdateUserConfiguration", data);

            Call<UserConfiguration> putUserConfigurations = mHTTPClient.putUserConfiguration(usrConfig.getId(), usrConfig);
            putUserConfigurations.enqueue(new Callback<UserConfiguration>() {
                @Override
                public void onResponse(Call<UserConfiguration> call, Response<UserConfiguration> response) {
                    Log.d(TAG, "putUserConfiguration->onResponse: " + response.body());
                }

                @Override
                public void onFailure(Call<UserConfiguration> call, Throwable t) {
                    Log.e(TAG, "putUserConfiguration->onFailure: " + t.getMessage());
                }
            });
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
