package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.PermissionHelper;
import com.example.bowon.graduationworkdebug.render.Matrix;

import java.util.Date;
import java.util.Random;

/**
 * Created by bowon on 2017-03-20.
 */
/*
* contextwrapper
* context자원을 끌어오는 경우 대신 사용되는 클레스이다
* 대표적으로 서버와의 통신이 있으며 그 외에도 geocoding, location등을 가져올 것이다.
*
* 우선적으로 marker와의 연동부터 신경을 쓸 것이다.
*
*
* */


public class MainMixedViewContext extends ContextWrapper {

    Context context;
    public MainMixedViewActivity mainMixedViewActivity;
    Random rand;// 헤쉬값 계산을 위한 난수발생함수

    Location currentLocation;
    Location locationAtLastDownload ;
    Matrix rotationMatrix = new Matrix();

    float declination = 0f;//경사, 적위

    /*자신의 위치가 정확한지에 대하여 특정 기준에 따라 참, 거짓을 나타내도록 한다.*/
    private boolean actualLocation = false;

    LocationManager locationManager;

    PermissionHelper permissionHelper;


    /*생성자로서 메인 뷰로부터 context자체를 받는다.*/
    /*생성자 에서는 자신의 위치를 provider를 이용해 받아오고
    * 받은 자신의 위치가 믿을만한지 아닌지까지 판단하여 Flag변수로 알려준다.
    * */
    MainMixedViewContext(Context context){
        super(context);

        /*뷰와 context를 할당 받는다.*/

       this.context = context;
        mainMixedViewActivity = (MainMixedViewActivity)context;

        SharedPreferences sharedSettings = getSharedPreferences(MainMixedViewActivity.PREFS_CODE,0);

        /*
        * 원래 데이터 소스를 통해 데이터를 필터링하는 기능이 있어 해당 내용에 대한
        * 소스가 Mixare에는 있지만 데이터소스를 통한 필터링까지는 사용계획이 현제는 없기에
        * 있었다는 것만 표시하도록 한다.
        * */

        rotationMatrix.toIdentity();// 우선 회전행렬을 단위행렬로 설정한다.

        int locationHash = 0;

        try {
//            permissionHelper.LocationPermission();
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            /*우선 자신의 위치를 GPS로 받아보고 GPS수신이 어려울 경우 NetworkProvider를 사용해 받는다. */


            Location lastFIxLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                if(lastFIxLocation == null){
                    lastFIxLocation = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
                    Toast.makeText(this,"NetworkProvider",Toast.LENGTH_SHORT);
                }
            }else{
                Toast.makeText(this,"GPSProvider",Toast.LENGTH_SHORT);


            }




            /*만일 자신의 위치를 받아온 기록이 있을 시 그것을 이용하되 위치를 받고 시간이 너무 오래 날 경우 정확도가 없음을 알린다. */
            if (lastFIxLocation != null){
                /*위치를 기반으로 해쉬값을 생성한다.*/
                locationHash = ("HASH_" + lastFIxLocation.getLatitude() + "_"+lastFIxLocation.getLongitude()).hashCode();

                long actualTime = new Date().getTime();
                long lastFixTime = lastFIxLocation.getTime();
                long timeDifference = actualTime - lastFixTime;

                /*위치를 받은 후 너무 오랜 시간이 지나면 actualLocation값을 변경시켜 값을 조절한다.*/
                actualLocation = timeDifference <= 1200000; //  마지막 갱신으로부터 20분 이후이면 거짓으로 설정
            }
            /*자신의 위치를 받아오지 못했다면 당연히 현제 사용하고 있는 위치 데이터는 불 확실 한 것 이다.*/
            else{actualLocation = false;}


        }catch (SecurityException e){
            e.printStackTrace();
        }
        rand = new Random(System.currentTimeMillis() + locationHash);
    }

    public boolean isActualLocation(){return  actualLocation;}

    /**
     * 현제 자신의 상태를 return 해 주는 클레스이다.
     * 추가해야 할 것으로 다운로드관리자쓰레드 관련하여 관리자를 리턴하는 것이 필요하다.
     *
     * */

    public void getRotationMatrix(Matrix dest){
        /** 회전행렬은 계산중에 바뀌게 되면 오류를 유발함으로 회전행렬설정중에는 동기화(synchronized를 설정한다.)*/
        synchronized (rotationMatrix){
            dest.set(rotationMatrix);
        }
    }
    public Location getCurrentLocation(){
        synchronized (currentLocation){
            return currentLocation;
        }
    }
    public Location getLocationAtLastDownload(){return locationAtLastDownload;}
    public void setLocationAtLastDownload(Location locationAtLastDownload){
        this.locationAtLastDownload = locationAtLastDownload;
    }
    /*
    * 이후로는 본래 서버와의 통신을 위해 url을 받아 서버와 통신 이후
    * 원하는 데이터를 리턴하는 부분들이 있다. 하지만 안드로이드 버젼의 차이와 호출할 서버의 환경이 다르기에
    * 현제는 구현을 하지 않고 이후 서버 부분이 확정된 이후 서버 통신을 구현 할 것이다.
    * */





}
