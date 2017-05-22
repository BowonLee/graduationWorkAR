package com.example.bowon.graduationworkdebug;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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


public class CreatePersonalMarkerActivity extends AppCompatActivity implements View.OnClickListener {
    private int CAMERA_CAPTURE = 1;




    private Button mCompleteButton;
    private ImageView mImageView;
    private TextView mTitleTextView;

    private Bitmap markerImage;
    private String markerTitle;
    private Double lat,lng,alt;
    private String dateStamp;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        findViewById(R.layout.activity_marker_create);

        mImageView = (ImageView)findViewById(R.id.imageview_marker_create);
        mTitleTextView = (TextView)findViewById(R.id.textview_marker_create_title);
        mCompleteButton = (Button)findViewById(R.id.button_marker_create_complete);

        mImageView.setOnClickListener(this);
        mCompleteButton.setOnClickListener(this);

    }


    @Override
    public void onClick(View v) {




    }

    private void openCamera(){

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent,CAMERA_CAPTURE);
    }


    private boolean saveMarker(){

        return false;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){

            case CAMERA_CAPTURE : {}break;
        }



    }
}
