package com.example.abuzany.citizenalarm.framework.configuration;

/**
 * Created by abuzany on 4/10/16.
 */
public class GlobalConfig {
    private static GlobalConfig instance = null;

    protected GlobalConfig() {
        // Exists only to defeat instantiation.
    }

    public static GlobalConfig getInstance() {
        if(instance == null) {
            instance = new GlobalConfig();
        }
        return instance;
    }

    public String getSocketIOAddress() {
        return "http://192.168.1.71:3000";
    }

    public String getAPIRestAddress() {
        return "http://192.168.1.71:3000/";
    }
}
