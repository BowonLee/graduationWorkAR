package com.example.bowon.graduationworkdebug.MainMixedView;

import com.example.bowon.graduationworkdebug.CalculateUtil;
import com.example.bowon.graduationworkdebug.render.Matrix;
import com.example.bowon.graduationworkdebug.render.MixVector;

/**
 * Created by bowon on 2017-03-21.
 */

/*
* 뷰의 현제 상태에 대한 데이터 처리를 위한 클레스
*
* 주요한 기능으로는 마커 터치시의 이벤트 처리와
* 장치각과 방위각을 계산하는 것 이다.
* 이후 구조 파악이 진행되면 추가적인 리펙토링이 필요할 것 같다.
* 일단 함수호출은 Marker와 Datahandler에서 이루어진다.
* */
public class MainMixedViewState {

    public static int NOT_STARTED = 0;
    public static int PROCESSING = 1;
    public static int READY = 2;
    public static int DONE = 3;

    int nextStatus = MainMixedViewState.NOT_STARTED;

    private float currentBearing;
    private float currentPitch;

    //Detail뷰가 뭔지 잘 모르겠다 일단 해놓고 나중에 분석해봐야겠다.
    private boolean detailsView;

    public boolean handleEvent(MainMixedViewContext context,String onPress ) {
        //데이터랜들러 - 마커 - 마커 클릭시 이벤트 처리의 일부분
        //원래는 웹페이지를 파싱하였다.

        if(onPress != null && onPress.startsWith("webpage")){
            String webpage = CalculateUtil.parseAction(onPress);
            this.detailsView = true;
            //context에서 웹페이지를 부른다.
        }
        return true;
    }

    //현제 기기의 방위각
    public float getCurBearing(){return currentBearing;}

    //현제 기기의 장치각
    public float getCurPitch(){return currentPitch;}

    //디테일 뷰의 표시 여부를 리턴한다.
    public boolean isDetailsView(){return detailsView;}

    //디테일뷰의 표시 여부를 설정
    public void setDetailsView(boolean detailsView){this.detailsView = detailsView;}

    /*
    장치의 방위각을 계산한다.
    사실상의 핵심 기능
    */
    /*필요인자 - 결과
    * 카메라의 회전행렬 - 장치의 기기각
    * Datahandler에서 사용이 되며 이후 인자정보에 대하여 생각해봐야 한다.
    * */
    public void calculatePitchBearing(Matrix rotationM){
        MixVector looking = new MixVector();
        rotationM.transpose();
        looking.set(1,0,0);
        looking.prod(rotationM);
        this.currentBearing = (int)(CalculateUtil.getAngle(0,0,looking.x,looking.z)+360)%360;

        rotationM.transpose();
        looking.set(0,1,0);
        looking.prod(rotationM);
        this.currentPitch = -CalculateUtil.getAngle(0,0,looking.y,looking.z);

    }


}
