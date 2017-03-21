package com.example.bowon.graduationworkdebug;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by bowon on 2017-01-30
 * RuntimePermeission.
 * 각종 퍼미션들에 대하여 인증을 얻어 올 수있도록 하는 클레스이다
 */

public class PermissionHelper {


    static final int LOCATION_ACCESS__FINE = 1; // 위치정보 퍼미션
    static final int CAMERA_PERMISSION = 2;

    Context currentActivity;
    int permissionCheck;

    PermissionHelper(Context activity){
        this.currentActivity = activity;

    }


    //위치정보 퍼미션
    public Boolean LocationPermission() {
        permissionCheck = ContextCompat.checkSelfPermission(currentActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck == PackageManager.PERMISSION_DENIED) {
            // 권한없음
               //권한 요청
                ActivityCompat.requestPermissions((Activity) currentActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_ACCESS__FINE);
                Log.d("LocationPermission", "apply");
                return true;
        }else {
            // 권한있음
            Log.d("LocationPermission", "already");
        }
        return false;
    }

    //카메라 퍼미션
    public boolean CameraPermission(){
        permissionCheck = ContextCompat.checkSelfPermission(currentActivity,Manifest.permission.CAMERA);
        if(permissionCheck == PackageManager.PERMISSION_DENIED){
            //권한 없음
           ActivityCompat.requestPermissions((Activity) currentActivity,new String[]{Manifest.permission.CAMERA},CAMERA_PERMISSION);
                Log.d("CameraPermission","apply");
            return true;
        }else{
            Log.d("CameraPermission","already");

        }
    return false;
    }






}
