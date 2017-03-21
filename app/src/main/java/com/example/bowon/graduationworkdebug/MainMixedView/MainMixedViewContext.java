package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.content.ContextWrapper;

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
    public MainMixedView mainMixedView;

    MainMixedViewContext(Context context){
        super(context);

       this.context = context;
        mainMixedView = (MainMixedView)context;
    }







}
