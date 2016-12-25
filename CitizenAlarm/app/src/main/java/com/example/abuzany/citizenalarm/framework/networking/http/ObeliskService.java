package com.example.abuzany.citizenalarm.framework.networking.http;

/**
 * Created by abuzany on 29/10/16.
 */
public class ObeliskService {

    private static ObeliskService instance = null;

    private ApiService apiService = null;

    protected ObeliskService(){
        apiService = ServiceGenerator.createService(ApiService.class);
    }

    public static ObeliskService getInstance(){
        return instance == null ? instance = new ObeliskService(): instance;
    }

    public ApiService getService(){
        return apiService;
    }
}
