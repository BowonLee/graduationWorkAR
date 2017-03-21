package com.example.bowon.graduationworkdebug;

/**
 * Created by bowon on 2017-03-21.
 */

import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedView;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewContext;
import com.example.bowon.graduationworkdebug.render.CameraData;


/**
 * mixare의 dataView 를 가져온 것
 * 구성이 View라고 보기에는 무리가 있어 handler로 이름을 바꾸었다.
 * 증강된 데이터들을 MainView에 출력되게 하기 위해 존제하는 클레스이며 대부분의 처리가 이루어진다.
 * 대표적으로는 Marker의 생성과 외부에서 값을 받는 DowonloadManager 이다.
 * Contextwrapper를 통해 main의 context를 얻어와 Context의 처리를 하기도 한다.
 * */
public class ArgumentedDataHandler {
    //현제 context
    private MainMixedViewContext mainMixedViewContext;
    //뷰의 초기 셋팅 여부 - 증강된 뷰는 셋팅이 이루어진 이후 처리가 이루어진다.
    //하지만 이 셋팅은 이미 되어 있으면 다시 할 필요가 없기에 리소스의 낭비방지를 위해 셋팅여부확인을 한다.
    private boolean isInit;

    private CameraData cameraData;






}
/*
* UI 상에서 발생하는 이벤트에 관련 된 리스너
* 이벤트는 크게 두가지로 나누어진다.
* 터치를통한 이벤트- 마커를 터치함 , UI상의 버튼을 터치함
* UI버튼터치를 통한 이벤트는 메인 뷰에서 처리하기에 여기서는 마커터치 이벤트만 처리한다.
*
* */
class MarkerTouchEveent {
    public float x,y; // 터치한 화면상의 좌표

    public MarkerTouchEveent(float x, float y){
        this.x = x;
        this.y = y;

    }
    public String toString(){
        return "( "+x+" , "+y+" )";
    }
}