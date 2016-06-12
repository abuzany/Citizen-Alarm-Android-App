package com.intelligentrescueagent.Framework.Networking.Http;

import com.intelligentrescueagent.Models.Alert;
import com.intelligentrescueagent.Models.User;
import com.intelligentrescueagent.Models.UserConfiguration;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

/**
 * Created by Angel Buzany on 17/02/2016.
 */

public interface APIService {

    /////////////////////////////////Alerts/////////////////////////////////

    @GET("Alerts")
    Call<List<Alert>> getAlerts();

    @GET("Alerts/Today")
    Call<List<Alert>> getTodayAlerts();

    @GET("Alerts/User/{id}")
    Call<List<Alert>> getUserAlerts(@Path("id") String id);

    @GET("Alerts/User/{id}/Today")
    Call<List<Alert>> getTodayUserAlerts(@Path("id") String id);

    @GET("Alerts/User/{id}/Week")
    Call<List<Alert>> getWeekUserAlerts(@Path("id") String id);

    @GET("Alerts/User/{id}/Month")
    Call<List<Alert>> getMonthUserAlerts(@Path("id") String id);

    @POST("Alerts")
    Call<Alert> postAlert(@Body Alert alert);

    /////////////////////////////////Use/////////////////////////////////

    @POST("CAUsers")
    Call<User> postUser(@Body User user);

    @GET("CAUsers")
    Call<List<User>> getUsers();

    @GET("CAUsers/{id}")
    Call<User> getUser(@Path("id") int id);

    @GET("CAUsers/Facebook/{id}")
    Call<User> getUserByFacebookId(@Path("id") String id);

    /////////////////////////////////UserConfiguration/////////////////////////////////

    @POST("UserConfigurations")
    Call<UserConfiguration> postUserConfiguration(@Body UserConfiguration userConfiguration);

    @GET("UserConfigurations/facebook/{id}")
    Call<UserConfiguration> getUserConfigurationByFbId(@Path("id") String id);

    @PUT("UserConfigurations/{id}")
    Call<UserConfiguration> putUserConfiguration(@Path("id") Integer id, @Body UserConfiguration userConfiguration);
}
