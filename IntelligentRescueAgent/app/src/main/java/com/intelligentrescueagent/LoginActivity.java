package com.intelligentrescueagent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.User;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class LoginActivity extends AppCompatActivity {

    private CallbackManager callbackManager;
    private APIService mHTTPClient;
    private LoginButton btnFbLogin;

    private String mUserId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);

        FacebookSdk.sdkInitialize(getApplicationContext());

        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.activity_login);

        btnFbLogin = (LoginButton)findViewById(R.id.btnFBLogin);
        btnFbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                OpenMapActivity();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Logs 'install' and 'app activate' App Events.
        AppEventsLogger.activateApp(this);

        if(isLoggedIn()){
            OpenMapActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    //////////////////////Methods//////////////////////

    private boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void OpenMapActivity(){

        AccessToken token = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {

                        mUserId = me.optString("id");

                        try {
                            Call<User> getUserByFacebookIdCall = mHTTPClient.getUserByFacebookId(mUserId);
                            getUserByFacebookIdCall.enqueue(new Callback<User>() {
                                @Override
                                public void onResponse(Call<User> call, Response<User> response) {
                                    User user = response.body();

                                    //If the user doesn't exist so regiter it
                                    if(user == null){

                                        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                        intent.putExtra("userId", mUserId);

                                        startActivity(intent);
                                    }
                                    else{
                                        //Open MainActivity
                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                        intent.putExtra("userId", user.getFacebookID());
                                        intent.putExtra("alias", user.getAlias());

                                        startActivity(intent);
                                    }
                                }

                                @Override
                                public void onFailure(Call<User> call, Throwable t) {
                                    Log.e("MainActivity", "onFailure: " + t.getMessage());
                                }
                            });
                        }
                        catch (Exception e){
                            Log.e("MainActivity", "onCompleted: " +  e.getMessage());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
