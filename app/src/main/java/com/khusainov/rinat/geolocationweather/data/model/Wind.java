package com.khusainov.rinat.geolocationweather.data.model;

import com.google.gson.annotations.SerializedName;

public class Wind {

    @SerializedName("speed")
    private Integer speed;

    @SerializedName("deg")
    private Integer deg;

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getDeg() {
        return deg;
    }

    public void setDeg(Integer deg) {
        this.deg = deg;
    }

}
