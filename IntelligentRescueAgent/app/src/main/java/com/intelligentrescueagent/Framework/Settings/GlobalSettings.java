package com.intelligentrescueagent.Framework.Settings;

import com.facebook.login.LoginManager;

/**
 * Created by Angel Buzany on 27/03/2016.
 */
public class GlobalSettings {

    private LoginManager mLoginManager;

    private static GlobalSettings instance = null;
    protected GlobalSettings() {
        // Exists only to defeat instantiation.
    }
    public static GlobalSettings getInstance() {
        if(instance == null) {
            instance = new GlobalSettings();
        }
        return instance;
    }

    public String getSocketIOAddress() {
        return "http://192.168.100.12:3000";
    }

    public String getAPIRestAddress() {
        return "http://192.168.100.12/BertholdAPIRest/api/";
    }

    public void setLoginManager(LoginManager loginManager) {
        mLoginManager = loginManager;
    }

    public LoginManager getmLoginManager(){
        return mLoginManager;
    }
}

