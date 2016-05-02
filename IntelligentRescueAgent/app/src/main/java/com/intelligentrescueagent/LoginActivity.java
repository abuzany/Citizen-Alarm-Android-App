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
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Framework.Settings.GlobalSettings;
import com.intelligentrescueagent.Models.User;

import org.json.JSONObject;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Angel Buzany on 06/01/2016.
 */
public class LoginActivity extends AppCompatActivity {

    private CallbackManager mCallbackManager;
    private APIService mHTTPClient;
    private LoginButton mBtnFbLogin;
    private LoginManager mLoginManager;

    private String mUserId;
    private String mEmail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        GlobalSettings.getInstance().setLoginManager(LoginManager.getInstance());

        setContentView(R.layout.activity_login);

        mBtnFbLogin = (LoginButton)findViewById(R.id.btnFBLogin);
        mBtnFbLogin.setReadPermissions("public_profile");
        mBtnFbLogin.setReadPermissions("email");
        mBtnFbLogin.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                OpenMainActivity();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException e) {
                Log.d("LoginActivity", "onError: " + e.getMessage());
            }
        });

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
            OpenMainActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Logs 'app deactivate' App Event.
        AppEventsLogger.deactivateApp(this);
    }

    ////////////////////////////////Methods////////////////////////////////

    private boolean isLoggedIn() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null;
    }

    private void OpenMainActivity(){

        //Open MainActivity
        /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.putExtra("userId", "1140560245961978");
        intent.putExtra("email", "angel_lr1908@hotmail.com");
        intent.putExtra("alias", "Enguel");*/

        //startActivity(intent);

        AccessToken token = AccessToken.getCurrentAccessToken();

        GraphRequest request = GraphRequest.newMeRequest(
                token,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject me, GraphResponse response) {
                        try {
                            if(me != null){
                                mUserId = me.optString("id");
                                mEmail = me.optString("email");

                                Call<User> getUserByFacebookIdCall = mHTTPClient.getUserByFacebookId(mUserId);
                                getUserByFacebookIdCall.enqueue(new Callback<User>() {
                                    @Override
                                    public void onResponse(Call<User> call, Response<User> response) {
                                        User user = response.body();
                                        //If the user doesn't exist so regiter it
                                        if(user == null){
                                            //Open RegisterActivity
                                            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                                            intent.putExtra("userId", mUserId);
                                            intent.putExtra("email", mEmail);

                                            startActivity(intent);
                                        }
                                        else{
                                            //Open MainActivity
                                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                            intent.putExtra("userId", user.getFacebookID());
                                            intent.putExtra("email", user.getEmail());
                                            intent.putExtra("alias", user.getAlias());

                                            startActivity(intent);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {
                                        Log.e("getUserByFacebookIdCall", "onFailure: " + t.getMessage());
                                    }
                                });
                            }
                        }
                        catch (Exception e){
                            Log.e("LoginActivity", "onCompleted: " +  e.getMessage());
                        }
                    }
                });

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link,email");
        request.setParameters(parameters);
        request.executeAsync();
    }
}
