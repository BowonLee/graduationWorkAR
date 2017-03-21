package com.example.bowon.graduationworkdebug;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by bowon on 2017-03-06.
 */
/* geocode를 이용해 위치좌표를 지오코딩한다.*/
public class GetAddress {
    Context context;
    Geocoder geocoder;
    List<Address> address;



    public GetAddress(Context context){
        this.context = context;
        geocoder = new Geocoder(context, Locale.KOREA);
    }


    public String getAddressName(double lat,double lng){
    String returnAddress = "현제위치를 확인 할 수 없습니다.";
        try {
            address = geocoder.getFromLocation(lat, lng, 1);
        }catch (IOException e){
            e.printStackTrace();
        }

        if(address!=null &&address.size()>0){
            returnAddress = address.get(0).getAddressLine(0).toString();
        }


        return returnAddress;
    }

}
