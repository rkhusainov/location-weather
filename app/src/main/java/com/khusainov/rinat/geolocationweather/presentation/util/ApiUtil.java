package com.khusainov.rinat.geolocationweather.presentation.util;

import com.google.gson.Gson;
import com.khusainov.rinat.geolocationweather.BuildConfig;
import com.khusainov.rinat.geolocationweather.data.api.ApiKeyInterceptor;
import com.khusainov.rinat.geolocationweather.data.api.WeatherApi;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.khusainov.rinat.geolocationweather.BuildConfig.API_URL;

public class ApiUtil {
    private static OkHttpClient sClient;
    private static Retrofit sRetrofit;
    private static Gson sGson;
    private static WeatherApi sApi;

    private static OkHttpClient getClient() {
        if (sClient == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
            builder.addInterceptor(new ApiKeyInterceptor());
            if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.level(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(logging);
            }
            sClient = builder.build();
        }
        return sClient;
    }

    private static Retrofit getRetrofit() {
        if (sGson == null) {
            sGson = new Gson();
        }

        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(API_URL)
                    // need for interceptors
                    .client(getClient())
                    .addConverterFactory(GsonConverterFactory.create(sGson))
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return sRetrofit;
    }

    public static WeatherApi getApi() {
        if (sApi == null) {
            sApi = getRetrofit().create(WeatherApi.class);
        }
        return sApi;
    }
}
