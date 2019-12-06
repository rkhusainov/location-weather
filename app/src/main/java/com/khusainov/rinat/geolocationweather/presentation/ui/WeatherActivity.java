package com.khusainov.rinat.geolocationweather.presentation.ui;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.khusainov.rinat.geolocationweather.R;
import com.khusainov.rinat.geolocationweather.data.model.WeatherResponse;
import com.squareup.picasso.Picasso;

public class WeatherActivity extends AppCompatActivity implements IWeatherView {

    private static final int REQUEST_CODE = 101;
    private static final int REQUEST_CHECK_SETTINGS = 102;

    public static final String ICON_BASE_URL = "http://openweathermap.org/img/wn/";
    public static final String ICON_END_URL = "@2x.png";

    private static final String TAG = "WeatherActivity";

    private TextView mWeatherTextView;
    private TextView mAddressTextView;
    private TextView mDescriptionTextView;
    private TextView mCityTextView;
    private ImageView mWeatherImageView;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationCallback locationCallback = new MainLocationCallback();

    private WeatherPresenter mWeatherPresenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_weather);

        initViews();

        mWeatherPresenter = new WeatherPresenter(this, WeatherActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkGooglePlayServices();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        }
    }

    private void initViews() {
        mWeatherTextView = findViewById(R.id.tv_weather);
        mAddressTextView = findViewById(R.id.tv_address);
        mDescriptionTextView = findViewById(R.id.tv_description);
        mCityTextView = findViewById(R.id.tv_city);
        mWeatherImageView = findViewById(R.id.iv_weather);
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
        } else {
            checkDeviceSettings();
        }
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE) {
            if (grantResults.length > 1) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startLocationService();
                } else {
                    finish();
                }
            }
        }
    }

    private void checkGooglePlayServices() {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int statusCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (statusCode != ConnectionResult.SUCCESS) {
            Dialog errorDialog = googleApiAvailability.getErrorDialog(this, statusCode,
                    0, dialogInterface -> finish());

            errorDialog.show();
        } else {
            checkPermission();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startLocationService();
                        break;
                    case Activity.RESULT_CANCELED:
                        finish();
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private void checkDeviceSettings() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(getLocationRequest());

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationService();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(WeatherActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });
    }

    private void startLocationService() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(getLocationRequest(), locationCallback, null);
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000L);
        locationRequest.setFastestInterval(5000L);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        return locationRequest;
    }

    @Override
    public void loadData(WeatherResponse weatherResponse) {
        mWeatherTextView.setText(formatTemp(String.valueOf(weatherResponse.getMain().getTemp())));
        mDescriptionTextView.setText(String.valueOf(weatherResponse.getWeather().get(0).getDescription()));


        String url = ICON_BASE_URL + weatherResponse.getWeather().get(0).getIcon() + ICON_END_URL;
        Log.d(TAG, "loadData: " + url);
        Picasso.get().load(url)
                .into(mWeatherImageView);

    }

    private String formatTemp(String text) {
        String formattedText = getResources().getString(R.string.formatTemperature, text);
        return formattedText;
    }

    @Override
    public void loadAddress(Address address) {
        mAddressTextView.setText(address.getAddressLine(0));
        mCityTextView.setText(address.getLocality());
    }

    @Override
    public void showError() {
        Toast.makeText(this, getResources().getString(R.string.error_request), Toast.LENGTH_SHORT).show();
    }

    private class MainLocationCallback extends LocationCallback {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for (Location location : locationResult.getLocations()) {
                mWeatherPresenter.getAddressByCoordinates(location);
                mWeatherPresenter.getWeatherByCoordinates(
                        location.getLatitude(),
                        location.getLongitude());
            }
        }
    }
}
