package com.example.fitness_app.services;

import com.example.fitness_app.models.ActivityData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface FitbitService {
    @GET("1/user/-/activities/date/{date}.json")
    Call<ActivityData> getActivityData(
            @Header("Authorization") String authorization,
            @Query("date") String date,
            @Query("period") String period
    );
}