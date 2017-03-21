package com.example.bowon.graduationworkdebug.DataManagement;

import android.location.Location;

import com.example.bowon.graduationworkdebug.MainMixedViewContext;
import com.example.bowon.graduationworkdebug.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by bowon on 2017-03-20.
 */

public class DataHandlerForMarker {
    //마커들의 리스트 생성
    private List<Marker> markerList = new ArrayList<Marker>();

    public void addMarkers(List<Marker> markers){
        //인자로 받은 마커들을 추가한다.
        //중복은 방지한다.
        for(Marker ma:markers){
            if(!markerList.contains(ma)){
                markerList.add(ma);
            }
        }
    }
/*
    public void sortMarkerList(){
        Collections.sort(markerList);
    }
*/

    public void updateActivateStatus(MainMixedViewContext context){
        /*
        * 마커들의 활성화 상태를 지정해주는 메소드이다.
        * 마커들의 활성화 조건은 옵션클레스 정의 이후에야 만들어진다.
        * */
        Hashtable<Class, Integer> map = new Hashtable<Class, Integer>();

    }
    public void updateDistances(Location location){
    /*
    * 인자로 받은 값과 마커의 거리를 계산하는 클레스
    * 내부의 임시변수를 이용하여 마커에 갱신시켜준다.
    * 여기서 위치는 통상적으로 자신의 현제 위치를 의미한다.
    * */
        for(Marker ma: markerList){
            float[] dist = new float[3];
            Location.distanceBetween(ma.getLatitude(),ma.getLongitude(),location.getLatitude(),location.getLongitude(),dist);
            ma.setDistance(dist[0]);
        }

    }


}
