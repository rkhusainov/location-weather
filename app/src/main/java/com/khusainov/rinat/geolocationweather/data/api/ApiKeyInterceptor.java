package com.khusainov.rinat.geolocationweather.data.api;

import com.khusainov.rinat.geolocationweather.BuildConfig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class ApiKeyInterceptor implements Interceptor {

    private static final String API_NAME = "appid";

    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        HttpUrl httpUrl = request
                .url()
                .newBuilder()
                .addQueryParameter(API_NAME, BuildConfig.API_KEY)
                .build();
        return chain.proceed(request
                .newBuilder()
                .url(httpUrl)
                .build());
    }
}
