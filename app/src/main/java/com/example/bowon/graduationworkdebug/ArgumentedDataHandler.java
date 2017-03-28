package com.example.bowon.graduationworkdebug;

/**
 * Created by bowon on 2017-03-21.
 */

import android.location.Location;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewContext;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewState;
import com.example.bowon.graduationworkdebug.gui.PaintScreen;
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

    private  int width,height;

    /*카메라 객체를 다루고 있다.*/
    private CameraData cameraData;

    private MainMixedViewState mainMixedViewState = new MainMixedViewState();

    /*디버그 목적으로 뷰를 얼리는 기능을 위해 */
    private boolean frozen;

    /*웹 파싱에서의 다운로드 재시도 횟수 에러 상황에서의 처리를위해*/
    private  boolean retry;

    private Location currentFixLocation;
    private DataHandlerForMarker dataHandlerForMarker = new DataHandlerForMarker();
    private  float radius = 20;// 검색 반경 설정

    /*마커의 표시에서 사용되는 추가 수치 - 아직 정확이 뭔지 모름*/
    private float addX = 0,addY = 0;

    private boolean isLauncherStarted;

    /** 스트링 객체 값 추후 생성되면 입력  **/

    /** 이벤트 리스트 **/

    /**레이더 관련해서 사용하게 되면 입력**/


    // 객체상태의 설정 및 생성자, 상태리턴을 위한 부분들이다.
    //생성자
    public ArgumentedDataHandler(MainMixedViewContext context){this.mainMixedViewContext =context;}

    //메인뷰의 컨텍스트를 리턴한다.
    public MainMixedViewContext getMainMixedViewContext(){return  mainMixedViewContext;}

    //런쳐? 의 시작여부를 리턴한다.
    public boolean isLauncherStarted(){return isLauncherStarted;}

    //화면이 얼어있는지의 여부를 리턴 및 설정
    public boolean isFrozen(){return frozen;}

    public void setFrozen(boolean state){this.frozen = frozen;}

    //검색반경 리턴 - 설정
    public float getRadius(){return  radius;}

    public  void setRadius(float radius){this.radius = radius;}

    //데이터 핸들러 리턴
    public DataHandlerForMarker getDataHandlerForMarker(){return  dataHandlerForMarker;}

    // 디테일뷰의 상태와 설정
    public boolean isDetailsView(){return  mainMixedViewState.isDetailsView();}

    public void setDetailsView(boolean detailsView){mainMixedViewState.setDetailsView(detailsView);}

    //데이터뷰의 동작을 시작
    public void doStart(){
        //상태를 지정한다
        //현제의 위치를 마지막 다운로드 위치로 설정한다. 즉 , 현제의 위치를 등록한다.
        mainMixedViewState.nextStatus = MainMixedViewState.NOT_STARTED;
        mainMixedViewContext.setLocationAtLastDownload(currentFixLocation);
    }

    //초기 세팅이 외어 있는지의 여부 확인 및 초기 세팅
    public boolean isInit(){return  isInit;}

    public void init(int widthinit, int heightinit){

        width = widthinit;
        height = heightinit;

        //
        cameraData = new CameraData(width,height,true);
        cameraData.setViewAngle(CameraData.DEFAULT_VIEW_ANGLE);//뷰의 각도를 설정한다.

        /*
        * 레이더 관련
        * */

        frozen = false;
        isInit = true;

    }

    /**
     * 기존에 사용되는 DATAFORMAT을 사용하여 호출 할 URL을 만들고
     * 그URL을 context의 다운로더에 제출하여 데이터를 받아오느 ㄴ것 이다.
     * */
 //   public void requestData(String url)

    /**
     * 실제로 스크린에 여러가지 정보들을 그려주는 클레스이다.
     * 마커의 표시부터 시작하여
     * 사실상 이 프로그렘의 핵심 구현 부분이라고 해도 된다.
     * 수식을 이용하여 기기의 특성을 계산하고 기기의 화면에 위치정보값을 출력시키기도 한다.
     * */
    public void draw(PaintScreen dw){

        //카메라 객체의 회전행렬에 context가 가지고 있는 회전 행렬을 할당한다.
        mainMixedViewContext.getRotationMatrix(cameraData.transform);
        //현제위치를 context에 요청하여 할당받는다.
        currentFixLocation = mainMixedViewContext.getCurrentLocation();

        mainMixedViewState.calculatePitchBearing(cameraData.transform);


        /**
         * 다운로드 메니져를 이용하여 데이터들을 파싱받은 이후 마커들을 만들어 준다.
         * 마커들은 여기서 생성된다.
         * 우선 마커들은 임의로 생성하도록 한다.
         *
         *
         * 마커 1 - 인천대학산도서관
         * la 37.3751636 lo 126.6339779  at 0.0
         *
         * 마커 2 - 인천대학교 프로젝트 실무실
         * la 37.3748073 lo 126.6335562  at 0.0
         * 마커 3 - 인천대 입구역
         *
         * */

        /**
         * 위에서 생성된 마커들을 선별하여 표시하도록 설정한다.
         * */

        dataHandlerForMarker.updateActivateStatus(mainMixedViewContext);

        for(int i = dataHandlerForMarker.getMarkerLisrSize() -1;i>=0;i++){
            Marker marker = dataHandlerForMarker.getMarker(i);
            if(!frozen){marker.calcPaint(cameraData,addX,addY);}
            marker.draw(dw);
        }

        /*이후 레이더를 그리거나 이벤트들을 설정한다.*/
        mainMixedViewState.nextStatus = MainMixedViewState.PROCESSING;// 처리중 설정
    }


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