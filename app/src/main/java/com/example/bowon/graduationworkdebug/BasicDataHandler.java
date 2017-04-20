package com.example.bowon.graduationworkdebug;

import android.content.Context;

import com.example.bowon.graduationworkdebug.marker.Marker;

/**
 * Created by bowon on 2017-04-10.
 */

/*
* 전역적으로 사용되는 데이터 핸들링 클레스
*
* 여기서 말하는 데이터는 자신의 위치와 마커들의 정보이다
*
* 최초로 실행되는 엑티비티의 context를 받아 사용한다.
*
* 현제 최초 실행 엑티비트는 GoogleMapsViewAcitivty
*
* */
public class BasicDataHandler {

    Context firstActivityContext;

    BasicDataHandler(Context context){
        firstActivityContext = context;

    }

    public void SetMarker(){

    }






}
