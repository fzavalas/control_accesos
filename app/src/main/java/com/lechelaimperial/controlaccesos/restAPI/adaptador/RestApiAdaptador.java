package com.lechelaimperial.controlaccesos.restAPI.adaptador;

import android.util.Log;

import com.lechelaimperial.controlaccesos.restAPI.EndPointsApi;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RestApiAdaptador {

    public EndPointsApi conexionRestAPI(){

        String ip= "200.92.206.26";
        String port = "4003";
        String BASE_URL = "http://"+ip+":"+port+"/";
        Log.i("TESTURL",""+BASE_URL);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                //.baseUrl(ConstantesRestApi.ROOT_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(EndPointsApi.class);
    }
}
