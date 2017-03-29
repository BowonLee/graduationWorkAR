package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;

/**
 * Created by bowon on 2017-02-23.
 */
/*
* 메인뷰에 증강되어 함께 그려지는 뷰이다
* 실질적으로 '그려지는' 즉 draw객체를 통해 표현되는 정보들이 그려지며
 * Datahandler가 이 뷰를 통해 이 뷰의 draw객체값을 가지고
 * Marker를 그려낸다.
* */
public class AugmentedView extends View {

    /**/
    Context context;

    public AugmentedView(Context context){
        super(context);
        this.context = context;

    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);




    }

}
