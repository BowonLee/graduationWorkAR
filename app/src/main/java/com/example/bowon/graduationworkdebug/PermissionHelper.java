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


    public static final int LOCATION_ACCESS__FINE = 1; // 위치정보 퍼미션
    public static final int CAMERA_PERMISSION = 2;
    public static final int LOCATION_ACCESS_COARSE = 3;
    public static final int READ_EXTERNAL_STORAGE = 4;
    public static final int WRITE_EXTERNAL_STORAGE = 5;


    Context currentActivity;
    int permissionCheck;

    public PermissionHelper(Context activity){
        this.currentActivity = activity;

    }

    //외부 저장소 쓰기 퍼미션
    public void storageWritePerMission(){
        getPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE);
    }
    //외부 저장소 읽기 퍼미션
    public void storageReadPerMission(){
        getPermission(Manifest.permission.READ_EXTERNAL_STORAGE,READ_EXTERNAL_STORAGE);
    }
    //위치정보 퍼미션
    public void LocationPermission() {
        getPermission(Manifest.permission.ACCESS_FINE_LOCATION,LOCATION_ACCESS__FINE);
    }
    //카메라 퍼미션
    public void CameraPermission(){
        getPermission(Manifest.permission.CAMERA,CAMERA_PERMISSION);
    }

    public void LocationCoarsePermission() {
        getPermission(Manifest.permission.ACCESS_COARSE_LOCATION,LOCATION_ACCESS_COARSE);
    }

    private void getPermission(String thisPermission,int permissionCode){
        permissionCheck = ContextCompat.checkSelfPermission(currentActivity,thisPermission);
        if(permissionCheck != PackageManager.PERMISSION_GRANTED){
            //권한 없음
            if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) currentActivity, thisPermission)){
            }else{
                ActivityCompat.requestPermissions((Activity) currentActivity,new String[]{thisPermission}
                        ,permissionCode);
            }
        }else{
        }



    }






}
