package com.example.abuzany.citizenalarm.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Angel Buzany on 17/02/2016.
 */

public class Alert {

    @SerializedName("id")
    private int id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("alert_type_id")
    private int alertType;

    @SerializedName("description")
    private String description;

    @SerializedName("created_at")
    private String creationDate;

    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    @SerializedName("address")
    private String address;

    public int getId(){
        return this.id;
    }

    public void setId(int id){
        this.id = id;
    }

    public String getUserId(){
        return this.userId;
    }

    public void setUserId(String userId){
        this.userId = userId;
    }

    public int getAlertType(){
        return this.alertType;
    }

    public void setAlertType(int alertType){
        this.alertType = alertType;
    }

    public String getCreationDate(){
        return this.creationDate;
    }

    public void setCreationDate(String creationDate){
        this.creationDate = creationDate;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public double getLatitude(){
        return this.latitude;
    }

    public void setLatitude(double latitude){
        this.latitude = latitude;
    }

    public double getLongitude(){
        return this.longitude;
    }

    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
