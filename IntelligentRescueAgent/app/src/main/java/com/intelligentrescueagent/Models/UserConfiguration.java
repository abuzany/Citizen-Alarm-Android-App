package com.intelligentrescueagent.Models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Angel Buzany on 29/03/2016.
 */
public class UserConfiguration {

    @SerializedName("Id")
    private int id;

    @SerializedName("FacebookID")
    private String facebookID;

    @SerializedName("Range")
    private double range;

    @SerializedName("EnabledNotifications")
    private boolean enabledNotifications;

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getFacebookID(){
        return this.facebookID;
    }

    public void setFacebookID(String facebookID){
        this.facebookID = facebookID;
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }

    public boolean isEnabledNotifications() {
        return enabledNotifications;
    }

    public void setEnabledNotifications(boolean enabledNotifications) {
        this.enabledNotifications = enabledNotifications;
    }
}
