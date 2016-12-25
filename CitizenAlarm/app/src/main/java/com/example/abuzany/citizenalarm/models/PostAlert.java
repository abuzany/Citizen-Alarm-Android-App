package com.example.abuzany.citizenalarm.models;

import com.google.gson.annotations.SerializedName;

/**
 * Created by abuzany on 4/12/16.
 */
public class PostAlert {

    @SerializedName("alert")
    private Alert alert;

    public void setAlert(Alert alert) {
        this.alert = alert;
    }

    public Alert getAlert() {
        return alert;
    }
}
