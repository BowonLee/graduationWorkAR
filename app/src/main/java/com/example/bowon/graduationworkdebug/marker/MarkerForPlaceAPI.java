package com.example.bowon.graduationworkdebug.marker;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bowon.graduationworkdebug.R;

/**
 * Created by bowon on 2017-05-02.
 */

public class MarkerForPlaceAPI extends Marker{
    /**
     * placeAPI Item
     * 기존의 마커 아이템
     * 위도
     * 경도
     * 고도 - placeapi는 고도는 따로 계산 안하는 걸로 알고있ㅇㅁ
     * 링크 - 필요시 - 뭐 sns가 되면 쓰이겠지
     * 이미지 - > 이후 구현
     *
     * 여기까지가 대략적으로 표시되는 마커의 이미지이다.
     * */


    public MarkerForPlaceAPI(String title, double latitude, double longitude, double altitude, String link){
        super(title, latitude, longitude, altitude, link);



    }

    @Override
    public int getMaxObjects() {
        return 0;
    }
}
