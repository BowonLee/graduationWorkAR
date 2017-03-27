package com.example.bowon.graduationworkdebug.DataManagement;

import android.location.Location;

import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewContext;
import com.example.bowon.graduationworkdebug.Marker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

/**
 * Created by bowon on 2017-03-20.
 */

/**
 * datahandler구현중
 *
 * 기존의 Datahandler클레스를 가져왔다. 사용 용도가 Maker의 보조뿐이기에 이름을 변경하였다.
 * */
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

    //내장 정렬 클레스를 이용하여 정렬을 사킨다.
    public void sortMarkerList() {
        Collections.sort(markerList);

    }




    public void updateActivateStatus(MainMixedViewContext context){
        /*
        * 마커들의 활성화 상태를 지정해주는 메소드이다.
        * 마커들의 활성화 조건은 옵션클레스 정의 이후에야 만들어진다.
        * */
        Hashtable<Class, Integer> map = new Hashtable<Class, Integer>();
        // 임시로 사용할 Hashtable을 설정한다 Class- Integer로
        for(Marker ma:markerList){// 모든 마커에 적용되도록 마커리스트를 전부 돈다

            /*
            *마커를 받아와 처리하기 위해 Class객체를 이용한다.
            * 마커가 처음 들어올시, 즉 임의의 마커를 비교했을때 중복이 없는경우일때
            * 1으로 설정한다. 이후 들어오는 마커들은 1씩 증가되며 입력한다.
            */
            Class markerClass = ma.getClass();
            map.put(markerClass,(map.get(markerClass)!=null)?ma.getMaxObjects()+1:1);

            /*
            * 이후 특정 조건을 완수하면 활성화 상태를 지정하도록 한다.
            * 아직 조건 미지정
            * */
            ma.setActive(true);

        }

    }

    //자신의 위치가 변경되었을 경우 거리갱신을 위해 호출된다.
    public void onLocationChanged(Location location){
        updateDistances(location);
        sortMarkerList();
        for(Marker ma:markerList){
            ma.update(location);
        }
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

    /*
    * 외부에서 리스트 정보 열람시 사용
    * */

    public List getMarkerList(){return markerList;}
    public void setMarkerList(List markerList){this.markerList = markerList;}
    public int getMarkerLisrSize(){return markerList.size();}
    public Marker getMarker(int index){return markerList.get(index);}


}
