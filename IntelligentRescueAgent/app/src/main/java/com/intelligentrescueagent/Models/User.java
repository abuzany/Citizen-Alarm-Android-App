package com.intelligentrescueagent.Models;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Angel Buzany on 02/03/2016.
 */
public class User implements Serializable{

    @SerializedName("Id")
    private int userId;

    @SerializedName("FacebookID")
    private String facebookID;

    @SerializedName("Alias")
    private String alias;

    @SerializedName("Email")
    private String email;

    @SerializedName("CreationDate")
    private String creationDate;

    private int role;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId){
        this.userId = userId;
    }

    public String getFacebookID() {
        return facebookID;
    }

    public void setFacebookID(String facebookID) {
        this.facebookID = facebookID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(String creationDate) {
        this.creationDate = creationDate;
    }

    public int getRole() {
        return role;
    }

    public void setRole(int role) {
        this.role = role;
    }
}
