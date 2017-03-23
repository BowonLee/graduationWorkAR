package com.example.bowon.graduationworkdebug;

/**
 * Created by bowon on 2017-03-21.
 */

import android.location.Location;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedView;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewContext;
import com.example.bowon.graduationworkdebug.MainMixedView.MainMixedViewState;
import com.example.bowon.graduationworkdebug.render.CameraData;

import java.util.ArrayList;


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

 //   public void requestData(String url)




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