package com.intelligentrescueagent.Framework.Networking.Http;

import com.intelligentrescueagent.Models.Alert;
import com.intelligentrescueagent.Models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by Angel Buzany on 17/02/2016.
 */

public interface APIService {
    @GET("Alerts")
    Call<List<Alert>> getAlerts();

    @POST("Alerts")
    Call<Alert> postAlert(@Body Alert alert);

    @POST("Users")
    Call<User> postUser(@Body User user);

    @GET("Users")
    Call<List<User>> getUsers();

    @GET("Users/{id}")
    Call<User> getUser(@Path("id") int id);

    @GET("Users/Facebook/{id}")
    Call<User> getUserByFacebookId(@Path("id") String id);
}
