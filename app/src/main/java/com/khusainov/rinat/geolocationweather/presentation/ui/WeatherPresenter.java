package com.khusainov.rinat.geolocationweather.presentation.ui;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;

import com.khusainov.rinat.geolocationweather.data.model.WeatherResponse;
import com.khusainov.rinat.geolocationweather.presentation.util.ApiUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class WeatherPresenter {

    private static final String UNITS = "metric";
    public static final String LANG = "ru";

    private IWeatherView mWeatherView;
    private Geocoder mGeocoder;
    private Context mContext;

    public WeatherPresenter(IWeatherView weatherView, Context context) {
        mWeatherView = weatherView;
        mContext = context.getApplicationContext();
        mGeocoder = new Geocoder(mContext, Locale.getDefault());
    }

    @SuppressLint("CheckResult")
    public void getWeatherByCoordinates(double lat, double lon) {
        ApiUtil.getApi().getWeather(lat, lon, UNITS, LANG)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<WeatherResponse>() {
                    @Override
                    public void accept(WeatherResponse weatherResponse) throws Exception {
                        mWeatherView.loadData(weatherResponse);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mWeatherView.showError();
                    }
                });
    }

    public void getAddressByCoordinates(Location location) {
        new GeoCodingAsyncTask(new Geocoder(mContext, Locale.getDefault()), new OnGeocodeListener() {
            @Override
            public void onGeocode(Address address) {
                mWeatherView.loadAddress(address);
            }
        }).execute(location);
    }

    interface OnGeocodeListener {
        void onGeocode(Address address);
    }


    private static class GeoCodingAsyncTask extends AsyncTask<Location, Void, Address> {
        private final Geocoder geocoder;
        private final OnGeocodeListener mOnGeocodeListener;

        private GeoCodingAsyncTask(Geocoder geocoder, OnGeocodeListener onGeocodeListener) {
            this.geocoder = geocoder;
            this.mOnGeocodeListener = onGeocodeListener;
        }

        @Override
        protected Address doInBackground(Location... locations) {
            Location location = locations[0];
            try {
                List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),
                        location.getLongitude(), 1);
                return addressList.get(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Address address) {
            super.onPostExecute(address);
            if (mOnGeocodeListener != null) {
                mOnGeocodeListener.onGeocode(address);
            }
        }
    }

}
