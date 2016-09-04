package com.intelligentrescueagent;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.widget.LoginButton;
import com.intelligentrescueagent.Framework.DataBase.DataBaseHelper;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.User;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ValidateLoginActivity extends Activity {

    private static String TAG = "ValidateLoginActivity";

    private CallbackManager mCallbackManager;
    private AccessTokenTracker mTokenTracker;
    private APIService mHTTPClient;

    private String mUserId;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_validate_login);

        FacebookSdk.sdkInitialize(getApplicationContext());

        mCallbackManager = CallbackManager.Factory.create();

        mTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken newAccessToken) {
                updateWithToken(newAccessToken);
            }
        };

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);

        //Create db
        DataBaseHelper db = new DataBaseHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateWithToken(AccessToken.getCurrentAccessToken());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    //////////////////////////////////////////////Methods///////////////////////////////////////////
    private void updateWithToken(AccessToken currentAccessToken) {
        if (currentAccessToken != null)
            OpenMainActivity();
        else
            OpenLoginActivity();

    }

    private void OpenLoginActivity() {
        Intent intent = new Intent(ValidateLoginActivity.this, LoginActivity.class);

        startActivity(intent);
    }

    private void OpenMainActivity(){
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
                                        //If the user doesn't exist so register it
                                        if(user == null){
                                            //Open RegisterActivity
                                            Intent intent = new Intent(ValidateLoginActivity.this, RegisterActivity.class);
                                            intent.putExtra("userId", mUserId);
                                            intent.putExtra("email", mEmail);

                                            startActivity(intent);
                                        }
                                        else{
                                            //Open MainActivity
                                            Intent intent = new Intent(ValidateLoginActivity.this, MainActivity.class);
                                            intent.putExtra("user", user);

                                            startActivity(intent);
                                        }
                                    }
                                    @Override
                                    public void onFailure(Call<User> call, Throwable t) {
                                        Log.e(TAG, "onFailure: " + t.getMessage());
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
