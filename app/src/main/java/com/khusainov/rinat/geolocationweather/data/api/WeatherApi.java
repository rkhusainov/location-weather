package com.khusainov.rinat.geolocationweather.data.api;

import com.khusainov.rinat.geolocationweather.data.model.WeatherResponse;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApi {
    @GET("weather")
    Single<WeatherResponse> getWeather(@Query("lat") Double lat,
                                       @Query("lon") Double lon,
                                       @Query("units") String units,
                                       @Query("lang") String lang);
}