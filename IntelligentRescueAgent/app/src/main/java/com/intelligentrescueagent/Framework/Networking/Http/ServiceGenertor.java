package com.intelligentrescueagent.Framework.Networking.Http;

import com.intelligentrescueagent.Framework.Settings.GlobalSettings;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Angel Buzany on 17/02/2016.
 */
public class ServiceGenertor {

    public static final String API_BASE_URL = GlobalSettings.getInstance().getAPIRestAddress();

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(httpClient.build()).build();
        return retrofit.create(serviceClass);
    }
}
