package com.example.bowon.graduationworkdebug;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.SqliteDB.DBHelper;
import com.example.bowon.graduationworkdebug.marker.Marker;
import com.example.bowon.graduationworkdebug.marker.MarkerForPersonal;
import com.example.bowon.graduationworkdebug.marker.MarkerForPlaceAPI;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by bowon on 2017-05-15.
 */
/*
* 마커의 정보를 입력하는 페이지
*  카메라 아이콘(이미지 뷰) 클릭 -> 카메라 인턴트 -> 사진 저장 및 이미지뷰 저장
*  타이틀에는 마커의 이름 글자 수 제한 적용
*
*  저장 완료 클릭 시 -> 마커의 정보를 DB에 저장 하도록 한다.
*
*  마커의 정보
*  NAME         TYPE    TYPE(ANDROID)
*
*  title        TEXT    String                          마커의 이름
*  image        BLOB    bitmap or String(path)          마커 이미지
*  lattitude    REAL    double                          위도
*  longitude    REAL    double                          경도
*  alltitude    READ    double                          고도
*  date         TEXT    String                          년 월 일 시 분 초
*
*
*  OnClickListener - 저장버튼, 카메라 버튼
* */


public class CreatePersonalMarkerActivity extends AppCompatActivity implements View.OnClickListener,LocationListener {
    final int CAMERA_CAPTURE = 1;

    private String markerImagePath;
    private String markerTitle;
    private Double lat,lng,alt;
    private Double photoLat=0.0,photoLng=0.0,photoLAlt=0.0;

    private String dateStamp;


    private Button mCompleteButton;
    private ImageView mImageView;
    private EditText mTitleTextView;
    private LocationManager mLocationManager;


    private void setPhotoDate(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        dateStamp = sdfNow.format(date);
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_create);


        mImageView = (ImageView)findViewById(R.id.imageview_marker_create);
        mTitleTextView = (EditText) findViewById(R.id.textview_marker_create_title);
        mCompleteButton = (Button)findViewById(R.id.button_marker_create_complete);

        mImageView.setOnClickListener(this);
        mCompleteButton.setOnClickListener(this);
        mTitleTextView.setSingleLine();

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);


    }

    @Override
    protected void onResume() {
        super.onResume();
        try {

            Location gps, network;

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);

            gps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gps != null) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            } else if (network != null) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
            } else {
            }


        }catch (SecurityException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.imageview_marker_create : {openCamera(); break;}
            case R.id.button_marker_create_complete : {saveMarker(); break;}
            default:break;


        }


    }

    private void openCamera(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_CAPTURE);
    }


    private void saveMarker(){
        createTempMarker();
        DBHelper dbHelper = new DBHelper(this,"Trace.db",null,1);

        dbHelper.insertMakrer(createTempMarker());

    }





    private MarkerForPersonal createTempMarker(){
        /*
        * 제목    위도 경도 고도 링크 이미지 아이디(년월일시)
        * title                  null image  timestamp
        *
        * */



        markerTitle = mTitleTextView.getText().toString();

        MarkerForPersonal tempMarker = new MarkerForPersonal(markerTitle,photoLat,photoLng,photoLAlt,null,markerImagePath, dateStamp);



        return tempMarker;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            switch (requestCode){
                case CAMERA_CAPTURE : {
                    //찍은 이미지는 우선적으로 이미지 뷰에 등록
                    //사진을 찍은 시간과 장소도 함께 기록한다.
                    mImageView.setImageBitmap(resizeBitmap(data.getData()));
                    setPhotoDate();
                    setPhotoLocation();
                    markerImagePath = getPathFromUri(data.getData());

            }break;


            default:break;
        }

        }
    }

    public String getPathFromUri(Uri uri){
        Cursor cursor = getContentResolver().query(uri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();

        return path;
    }


    private Bitmap resizeBitmap(Uri uri){

        String filePath = uri.getPath();
        //이미지뷰 사이즈에 맞게 리사이징
        Bitmap bitmap = null;
        int width = mImageView.getWidth();
        int height = mImageView.getHeight();
        try {
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(),uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  Bitmap.createScaledBitmap(imgRotate(bitmap,90),width,height,true);

    }
    private Bitmap imgRotate(Bitmap bitmap, int angle){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        bitmap.recycle();

        return resizedBitmap;
    }





    private void setPhotoLocation(){
        photoLng = lng;
        photoLat = lat;
        photoLAlt = alt;
        GetAddress gd = new GetAddress(this);

    }

    private void setLocation(Location location){
        lat = location.getLatitude();
        lng = location.getLongitude();
        alt = location.getAltitude();
    }
    @Override
    public void onLocationChanged(Location location) {
        setLocation(location);
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
