package com.example.bowon.graduationworkdebug.marker;

/**
 * Created by bowon on 2017-05-02.
 */

public class MarkerForPlaceAPI extends Marker {
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


    private String placeId;
    public MarkerForPlaceAPI(String title, double latitude, double longitude, double altitude, String link){
        super(title, latitude, longitude, altitude, link);
    }
    public MarkerForPlaceAPI(String title, double latitude, double longitude, double altitude, String link,String placeId){
        super(title, latitude, longitude, altitude, link);
        setPlaceId(placeId);
    }

    public String getPlaceId(){
        return placeId;
    }
    public void setPlaceId(String placeId){
        this.placeId = placeId;
    }



    @Override
    public int getMaxObjects() {
        return 0;
    }
}
