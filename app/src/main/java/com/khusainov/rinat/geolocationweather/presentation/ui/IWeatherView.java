package com.khusainov.rinat.geolocationweather.presentation.ui;

import android.location.Address;

import com.khusainov.rinat.geolocationweather.data.model.WeatherResponse;

public interface IWeatherView {
    void loadData(WeatherResponse weatherResponse);

    void loadAddress(Address address);

    void showError();
}
