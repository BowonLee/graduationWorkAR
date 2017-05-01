package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;
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


        MixedViewActivity.dWindow.setWidth(canvas.getWidth());
        MixedViewActivity.dWindow.setHeight(canvas.getHeight());

        MixedViewActivity.dWindow.setCanvas(canvas);

        if(!MixedViewActivity.argumentedDataHandler.isInit()){
            Log.e("WindowWH",""+ MixedViewActivity.dWindow.getWidth()+ " "+ MixedViewActivity.dWindow.getHeight());
            MixedViewActivity.argumentedDataHandler.init( MixedViewActivity.dWindow.getWidth(),MixedViewActivity.dWindow.getHeight());

        }

        MixedViewActivity.argumentedDataHandler.draw(MixedViewActivity.dWindow);

    }

}
