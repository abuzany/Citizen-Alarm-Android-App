package com.example.abuzany.citizenalarm.framework.networking.http;

import com.example.abuzany.citizenalarm.framework.configuration.GlobalConfig;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by abuzany on 4/10/16.
 */
public class ServiceGenerator {

    public static final String API_BASE_URL = GlobalConfig.getInstance().getAPIRestAddress();

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
