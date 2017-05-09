package com.example.bowon.graduationworkdebug.DataManagement;

import android.location.Location;

import com.example.bowon.graduationworkdebug.Manifest;
import com.example.bowon.graduationworkdebug.R;

import javax.sql.DataSource;

/**
 * Created by bowon on 2017-03-23.
 */

/*
* Dataview와 연관된 클레스 데이터 소스의 파싱도 관련하여 사용한다.
*
* 기존 miare의 Data페키지를 담당하며 그중 Source를 중심으로 리펙토링한다.
*
* 데이터의 소스 외부파싱 기능 구현을 위해 사용된다.
*
* 이 부분은 추후 데이터 파싱을 어디서 받아올지 정한 이후에여 사용 가능할 듯 하다.
*
* 기존의JSON과 XML 부분도 같은 이유이다.
*
* 코드의 실행 구조는 어떤 데이터를 만들지에 대한 인자값을 기준으로 호출을 받은 후
* 각종 데이터를 하위의 JSON이나 XML파서를 통해 읽어 온 후 그 데이터를 기준으로
* 마커를 만들어서 해당 마커를 리턴하는 형태이다.
*
* */
public class DatasourceManagement {

    //place url
    private final String GOOGLE_PLACE_API_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";

    //구글 키값
    private  final String GOOGLE_KEY = "&key=AIzaSyDKFM4Ctcx8exyAR1mN0_sR_EL4epbDrRk";

    //반경 단위 500
    private final String RADIUS= "&radius=500";


    public DatasourceManagement(){
        /* 기본 생성자 */

    }

    public String makeRequestURL(Location location,String fillter){
        String requestURL;
        String locationText;
        locationText = "location="+location.getLatitude()+","+location.getLongitude();




        requestURL = GOOGLE_PLACE_API_URL+locationText+RADIUS+GOOGLE_KEY;
        return requestURL;
    }



}
