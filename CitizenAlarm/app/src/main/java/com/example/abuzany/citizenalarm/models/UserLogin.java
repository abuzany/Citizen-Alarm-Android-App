package com.example.abuzany.citizenalarm.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abuzany on 29/10/16.
 */
public class UserLogin {
    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
