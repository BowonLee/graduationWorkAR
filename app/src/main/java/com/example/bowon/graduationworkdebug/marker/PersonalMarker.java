package com.example.bowon.graduationworkdebug.marker;

import android.graphics.Bitmap;

import com.example.bowon.graduationworkdebug.gui.PaintScreen;

/**
 * Created by bowon on 2017-05-15.
 */

public class PersonalMarker extends Marker{
    Bitmap bitmap;
    String personalMarkerId;

    public PersonalMarker(String title, double latitude, double longitude, double altitude, String link, Bitmap image,String personalMarkerId) {
        super(title, latitude, longitude, altitude, link);
        bitmap = image;
        personalMarkerId = personalMarkerId;
    }


    @Override
    public int getMaxObjects() {
        return 0;
    }
}
