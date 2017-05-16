package com.example.bowon.graduationworkdebug;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bowon on 2017-05-15.
 */



public class CreatePersonalMarkerActivity extends AppCompatActivity implements View.OnClickListener {
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
}
