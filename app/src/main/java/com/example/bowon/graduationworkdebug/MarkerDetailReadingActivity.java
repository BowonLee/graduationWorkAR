package com.example.bowon.graduationworkdebug;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bowon on 2017-05-15.
 */

public class MarkerDetailReadingActivity extends AppCompatActivity{

    private ImageView mImageView;
    private TextView mTitleText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_marker_create);

        mImageView = (ImageView)findViewById(R.id.imageview_marker_detail_title);
        mTitleText = (TextView)findViewById(R.id.textview_marker_detail_title);
    }





}
