package com.aam.mcu.notification;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    String BASE_URL = "https://fcm.googleapis.com/";

    Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAWOixCgM:APA91bH6HfpCGabUDhNGmn1hnxMU4dZkNjdlcGJzqf6oDDLOrIAvPkpZXBVJHLjVtVMYmqIYHH2WuLGQdYm2dAozxoq9q53kiJgKn9rZqCXMUtJrO-bi9Jgg5cmP2CA3LPr_Z3FPja1Z"
            }
    )
    @POST("fcm/send")
    Call<Response> createNotification(@Body Message message);
}
