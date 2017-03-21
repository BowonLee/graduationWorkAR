package com.example.bowon.graduationworkdebug.DeviceLocationPackage;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by bowon on 2017-02-03.
 */
public class LocationHelper implements LocationListener{


    LocationManager locationManager;
    Object activityService;
    Location location;

    LocationHelper(Object activityService){
        this.activityService = activityService;

    }

    public Location getLocation(){
        return location;
    }
    public void getLocationState(){

    }
    private void getBestProvider(){

    }



    @Override
    public void onLocationChanged(Location location) {


    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }



}
