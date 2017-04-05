package com.example.bowon.graduationworkdebug.marker;

/**
 * Created by bowon on 2017-04-05.
 */

public class DummyMarker extends Marker {
    public static final int MAX_OBJECT = 20;

    public DummyMarker(String title, double latitude, double longitude, double altitude, String link) {
        super(title, latitude, longitude, altitude, link);
    }



    @Override
    public int getMaxObjects() {
        return 0;
    }
}
