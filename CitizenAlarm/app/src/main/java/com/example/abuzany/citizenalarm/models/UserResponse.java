package com.example.abuzany.citizenalarm.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abuzany on 29/10/16.
 */
public class UserResponse {
    @SerializedName("status")
    private String status;

    @SerializedName("data")
    private User user;
}
