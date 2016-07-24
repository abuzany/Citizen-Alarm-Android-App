package com.intelligentrescueagent;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.intelligentrescueagent.Framework.DataBase.DataBaseHelper;
import com.intelligentrescueagent.Framework.Networking.Http.APIService;
import com.intelligentrescueagent.Framework.Networking.Http.ServiceGenertor;
import com.intelligentrescueagent.Models.User;
import com.intelligentrescueagent.Models.UserConfiguration;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity{

    private static final String TAG = "RegisterActivity";

    private APIService mHTTPClient;
    private User mUser;

    private EditText mTxtAlias;

    private String mUserId;
    private String mEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Retrieve Information
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            mUserId = extras.getString("userId");
            mEmail = extras.getString("email");
        }

        //Initialize
        mHTTPClient = ServiceGenertor.createService(APIService.class);

        mTxtAlias = (EditText) findViewById(R.id.txtAlias);

        Button btnRegister = (Button) findViewById(R.id.btnRegister);
        btnRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                Date date = new Date();

                UserConfiguration usrConfig = new UserConfiguration();
                usrConfig.setFacebookID(mUserId);
                usrConfig.setRange(1);
                usrConfig.setEnabledNotifications(true);

                //Create UserConfiguration
                Call<UserConfiguration> postUserConfiguration = mHTTPClient.postUserConfiguration(usrConfig);
                postUserConfiguration.enqueue(new Callback<UserConfiguration>() {
                    @Override
                    public void onResponse(Call<UserConfiguration> call, Response<UserConfiguration> response) {

                    }

                    @Override
                    public void onFailure(Call<UserConfiguration> call, Throwable t) {
                        Log.e(TAG, "PostUserConfiguration->onFailure: " + t.getMessage());
                    }
                });

                mUser = new User();
                mUser.setFacebookID(mUserId);
                mUser.setEmail(mEmail);
                mUser.setAlias(mTxtAlias.getText().toString());
                mUser.setCreationDate(dateFormat.format(date));

                //Create user
                Call<User> postUserCall = mHTTPClient.postUser(mUser);
                postUserCall.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        //Insert user in SQLite
                        DataBaseHelper db = new DataBaseHelper(RegisterActivity.this);
                        db.insertUser(mUser.getFacebookID(), mUser.getEmail(), mUser.getAlias());

                        //Open MainActivity
                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                        intent.putExtra("RegisterActivity","1");
                        intent.putExtra("userId", mUser.getFacebookID());
                        intent.putExtra("email", mUser.getEmail());
                        intent.putExtra("alias", mUser.getAlias());

                        startActivity(intent);
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Log.e(TAG, "PostUserCall->onFailure: " + t.getMessage());
                    }
                });
            }
        });
    }
}

