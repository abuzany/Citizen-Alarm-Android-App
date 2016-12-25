package com.example.abuzany.citizenalarm.framework.networking.http;

import com.example.abuzany.citizenalarm.models.Alert;
import com.example.abuzany.citizenalarm.models.PostAlert;
import com.example.abuzany.citizenalarm.models.User;
import com.example.abuzany.citizenalarm.models.UserConfiguration;
import com.example.abuzany.citizenalarm.models.UserLogin;
import com.example.abuzany.citizenalarm.models.UserRegister;
import com.example.abuzany.citizenalarm.models.UserResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;


/**
 * Created by abuzany on 4/10/16.
 */
public interface ApiService {

    /////////////////////////////////Alerts/////////////////////////////////

    @GET("api/alerts")
    Call<List<Alert>> getAlerts();

    @GET("api/alerts/today")
    Call<List<Alert>> getTodayAlerts();

    @GET("api/alerts/user/{id}")
    Call<List<Alert>> getUserAlerts(@Path("id") String id);

    @GET("api/alerts/user/{id}/today")
    Call<List<Alert>> getTodayUserAlerts(@Path("id") String id);

    @GET("api/alerts/user/{id}/week")
    Call<List<Alert>> getWeekUserAlerts(@Path("id") String id);

    @GET("api/alerts/user/{id}/Month")
    Call<List<Alert>> getMonthUserAlerts(@Path("id") String id);

    @POST("api/alerts")
    Call<PostAlert> postAlert(@Body PostAlert postAlert);

    /////////////////////////////////Use/////////////////////////////////

    @POST("auth")
    Call<UserResponse> postUserRegister(@Body UserRegister userRegister);

    @POST("auth/sign_in")
    Call<UserResponse> postUserSignIn(@Body UserLogin userLogin);

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
