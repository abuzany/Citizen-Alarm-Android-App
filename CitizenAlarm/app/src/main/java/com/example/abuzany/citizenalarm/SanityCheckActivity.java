package com.example.abuzany.citizenalarm;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.abuzany.citizenalarm.framework.networking.http.ApiService;
import com.example.abuzany.citizenalarm.framework.networking.http.ServiceGenerator;

public class SanityCheckActivity extends AppCompatActivity {

    private final String TAG = "SanityCheckActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sanity_check);

        // SanityCheck
        checkRailsServerConnection();

        // If is loged in open main activity
        // else open login activity
        if(checkIsLogedIn()){
            openActivity("Main");
        }else{
            openActivity("Login");
        }
    }

    private boolean checkIsLogedIn(){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPreferences.getBoolean("isLogedIn", false);
    }

    private void openActivity(String activityName){
        try {
            Intent intent = null;

            switch (activityName){
                case "Main":
                    intent = new Intent(this, MainActivity.class);
                    break;
                case "Login":
                    intent = new Intent(this, LoginActivity.class);
                    break;
            }

            startActivity(intent);
        }catch (Exception e){
            Log.e(TAG, "openActivity->" + e.getMessage());
        }
    }

    private void checkRailsServerConnection(){
        try {
            ApiService hTTPClient = ServiceGenerator.createService(ApiService.class);
        }catch (Exception e){
            Log.e(TAG, "checkRailsServerConnection->" + e.getMessage());
        }
    }
}
