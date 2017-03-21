package com.example.bowon.graduationworkdebug.Datatype;

import android.location.Location;

/**
 * Created by bowon on 2017-02-10.
 */

//위치정보를 담고 있는 클레스
public class LocationCoordinate {
    double longitude;
    double latitude;
    double altitude;
    double accuracy;
    String provider;




    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }


    public void setLocation(Location location){
        this.longitude = location.getLongitude();
        this.latitude = location.getLatitude();
        this.altitude = location.getAltitude();
        this.accuracy = location.getAccuracy();
        this.provider = location.getProvider();

    }


    public double getAccuracy() {
        return accuracy;
    }

    public double getAltitude() {
        return altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getProvider() {
        return provider;
    }
    public LocationCoordinate getLocationCoordinate(){

        return this;
    }
}
