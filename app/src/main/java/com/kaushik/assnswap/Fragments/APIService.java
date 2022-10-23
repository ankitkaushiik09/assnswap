package com.kaushik.assnswap.Fragments;

import com.kaushik.assnswap.Notifications.MyResponse;
import com.kaushik.assnswap.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAt_VfsHo:APA91bE9V021KrAvONCKhenGvBQez2hiveb41koraswbAFOvb19fnd4KUNraMRU2gZ2a3qNBOdg0T3SEFdUVXxRNWB-PoHT3-dRvNHrFpIUucf4_NpvtEGFmHgSj7J6gICeI_P7E7WnX"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
