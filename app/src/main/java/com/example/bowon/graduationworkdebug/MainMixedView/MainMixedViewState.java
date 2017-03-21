package com.example.bowon.graduationworkdebug.MainMixedView;

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

   // public boolean handleEvent(MainMixedViewContext context,String Onpress )


}
