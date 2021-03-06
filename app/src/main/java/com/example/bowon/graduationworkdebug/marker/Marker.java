package com.example.bowon.graduationworkdebug.marker;

import java.text.DecimalFormat;
import java.text.NumberFormat;


import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.example.bowon.graduationworkdebug.R;
import com.example.bowon.graduationworkdebug.render.CameraData;
import com.example.bowon.graduationworkdebug.render.MixVector;
import com.example.bowon.graduationworkdebug.PhysicalPlace;
import com.example.bowon.graduationworkdebug.gui.ScreenObj;
import com.example.bowon.graduationworkdebug.gui.PaintScreen;
import com.example.bowon.graduationworkdebug.gui.TextObj;
import com.example.bowon.graduationworkdebug.gui.ScreenLine;
import com.example.bowon.graduationworkdebug.CalculateUtil;


import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import  android.location.Location;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by bowon on 2017-03-13.
 */

// 화면에 찍힐 마커 클레스
// Mixare의 마커 카피


abstract public class Marker implements Comparable<Marker>{

    private final int circleSize = 20;

    //커스텀 마커용
    View markerRootView;
    ImageView markerImage;
    TextView markerTitle;
    TextView markerInformation;


    private String ID;	// ID값
    protected String title;	// 타이틀
    private boolean underline = false;	// 밑줄 여부
    private String URL;	// 연동될 URL
    protected PhysicalPlace mGeoLoc;	// 물리적 공간 객체. 실제 장소값을 저장
    // 유저와 물리적 공간 간의 거리(미터 단위)
    protected double distance;
    private Bitmap image;

    private boolean active;	// 활성화 여부

    // 드로우 속성
    protected boolean isVisible;	// 보여지는지 여부

    public MixVector cMarker = new MixVector();	// 카메라 마커
    protected MixVector signMarker = new MixVector();	// 기호 마커


    // 장소에 관련된 벡터값들
    protected MixVector locationVector = new MixVector();
    private MixVector origin = new MixVector(0, 0, 0);
    private MixVector upV = new MixVector(0, 1, 0);
    private ScreenLine pPt = new ScreenLine();	// 클릭 지점을 판단하기 위함

    // 라벨과 화면에 표시될 텍스트 블록
    protected Label txtLab = new Label();	// Label 클래스는 하단에서 정의한다
    protected TextObj textBlock;

    // 생성자. 타이틀과 위도, 경고, 고도값, 링크될 주소와 데이터 소스를 인자로 받는다
    public Marker(String title, double latitude, double longitude, double altitude, String link) {
        super();

        this.active = false;	// 일단 비활성화 상태로

        // 각 속성값 할당
        this.title = title;
        this.mGeoLoc = new PhysicalPlace(latitude,longitude,altitude);


        // 마커의 ID는 '데이터소스##타이틀' 형태이다
        this.ID = title;
    }

    // 타이틀을 리턴
    public String getTitle(){
        return title;
    }

    // URL을 리턴
    public String getURL(){
        return URL;
    }

    // 위도를 리턴
    public double getLatitude() {
        return mGeoLoc.getLatitude();
    }

    // 경도를 리턴
    public double getLongitude() {
        return mGeoLoc.getLongitude();
    }

    // 고도를 리턴
    public double getAltitude() {
        return mGeoLoc.getAltitude();
    }

    // 위치 벡터를 리턴
    public MixVector getLocationVector() {
        return locationVector;
    }



    // 카메라 마커. 최초 위치와 투영될 카메라, 추가되는 x, y 값을 인자로 받는다
    private void cCMarker(MixVector originalPoint, CameraData viewCam, float addX, float addY) {

        // 임시 속성들
        MixVector tmpa = new MixVector(originalPoint);
        MixVector tmpc = new MixVector(upV);

        // 위치 벡터를 더하고 뷰 카메라의 벡터값은 뺀 후
        tmpa.add(locationVector); //3
        tmpc.add(locationVector); //3
        tmpa.sub(viewCam.lco); //4
        tmpc.sub(viewCam.lco); //4
        // 카메라의 변환 행렬을 곱한다
        tmpa.prod(viewCam.transform); //5
        tmpc.prod(viewCam.transform); //5

        // 새 임시벡터를 선언하고
        MixVector tmpb = new MixVector();
        // 계산된 벡터들로 카메라 마커와 기호 마커를 사영한다
        viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
        cMarker.set(tmpb); //7
        viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
        signMarker.set(tmpb); //7
    }

    // 고도를 계산. 이제는 쓰이지 않는듯?
    private void calcV(CameraData viewCam) {
        isVisible = false;	// 해당 마커가 현제 보이는지 판단

        // 마커의 z 값에 따른 처리를 한다
        if (cMarker.z < -1f) {
            isVisible = true;

            // 카메라 마커가 현재 카메라 뷰의 공간 안에 있는지 판단
            if (CalculateUtil.pointInside(cMarker.x, cMarker.y, 0, 0,
                    viewCam.width, viewCam.height)) {

            }
        }
    }

    // 마커 위치를 업데이트
    public void update(Location curGPSFix) {
    // 고도 값이 0.0일 경우 현재의 GPS픽스를 이용해 다시 고도값을 얻어온다
        if(mGeoLoc.getAltitude()==0.0)
            mGeoLoc.setAltitude(curGPSFix.getAltitude());

        // compute the relative position vector from user position to POI location
        // 유저 위치로부터 POI 위치 까지의 관계 지점의 벡터를 계산한다
        PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, locationVector);

    }

    // 그려질 위치를 계산
    public void calcPaint(CameraData viewCam, float addX, float addY) {
        cCMarker(origin, viewCam, addX, addY);	// 카메라 마커를 생성
        Log.e("calcPaint",""+this.ID+"  "+this.getTitle()+viewCam.width+" "+viewCam.height+" "+viewCam.toString());
        calcV(viewCam);	// 카메라의 고도를 계산
    }


    // 클릭이 허용되어 있는지 조사
    private boolean isClickValid(float x, float y) {
        // 현재각. 카메라 마커의 좌표와 기호 마커의 좌표 사이의 각을 구한다
        float currentAngle = CalculateUtil.getAngle(cMarker.x, cMarker.y,
                signMarker.x, signMarker.y);

        // 마커가 활성되어 있지 않은 경우(AR 뷰에 표시되지 않은 경우)
        if (!isActive())
            return false;	// 클릭을 체크할 필요가 없으므로 false

        //TODO adapt the following to the variable radius!

        // 기호 마커와 라벨의 위치로 클릭 부분을 계산
        pPt.x = x - signMarker.x;
        pPt.y = y - signMarker.y;
        pPt.rotate(Math.toRadians(-(currentAngle + 90)));
        pPt.x += txtLab.getX();
        pPt.y += txtLab.getY();

        // 라벨의 위치로 클릭 가능 영역을 계산한다
        float objX = txtLab.getX() - txtLab.getWidth() / 2;
        float objY = txtLab.getY() - txtLab.getHeight() / 2;
        float objW = txtLab.getWidth();
        float objH = txtLab.getHeight();

        // 가능한 영역을 클릭했는지 판단 후 리턴
        if (pPt.x > objX && pPt.x < objX + objW && pPt.y > objY
                && pPt.y < objY + objH) {
            return true;
        } else {
            return false;
        }
    }

    // 스크린에 실제로 그려주는 메소드
    public void draw(PaintScreen dw) {

        drawTextBlock(dw);

    }

    // 스크린에 원을 그린다

    // 텍스트 블록을 그린다. 일반적으로 URL 등을 담고있는 데이터 소스 등에 사용된다
    public void drawTextBlock(PaintScreen dw) {
        //TODO: 그려지게 될 상한선(최대높이)를 지정
        float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

        //TODO: 거리가 변경되었을 경우에만 텍스트 블록을 변경한다
        String textStr="";	// 출력될 텍스트
        double d = distance;	// 거리. 미터 단위
        DecimalFormat df = new DecimalFormat("@#");	// 숫자 포맷은 @숫자

        // 위치에 따른 자신과의 거리 출력. 1000m 이상은 km로 대체한다
        if(d<1000.0) {
            textStr = title + " ("+ df.format(d) + "m)";
        }
        else {
            d=d/1000.0;
            textStr = title + " (" + df.format(d) + "km)";
        }

        // 텍스트 블록(텍스트 오브젝트) 생성
        textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1,
                250, dw, underline);

        // 출력되는 상황일 경우
       // isVisible = true;
        if (isVisible) {

            // 데이터 소스에 따른 컬러를 지정

            // 현재 각을 얻어온다
            float currentAngle = CalculateUtil.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);

            // 세팅된 텍스트 블록으로 텍스트 라벨을 준비
            txtLab.prepare(textBlock);

            // 페인트 스크린을 설정하고
            dw.setStrokeWidth(1f);
            dw.setFill(true);

            // 준비된 값으로 객체를 스크린에 그린다
            dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
                    / 2, signMarker.y + maxHeight, currentAngle + 90, 1);


            Log.e("Marker","Drawtext"+this.ID+"  "+this.getTitle()+signMarker.x+" "+signMarker.y+" "+txtLab.getWidth() + " "+maxHeight+ " " + currentAngle);
        }

    }
/*
    public  void setCustomMarkerView(){

        markerRootView = LayoutInflater.from(this).inflate(R.layout.item_maps_custommarker,null);
        markerImage = (ImageView)markerRootView.findViewById(R.id.item_maps_marker_image);
        markerTitle = (TextView)markerRootView.findViewById(R.id.item_maps_marker_title);
        markerInformation = (TextView) markerRootView.findViewById(R.id.item_maps_marker_imfomation);
    }
*/
    public void drawCustomMarker(){

    }
    private Bitmap createDrawableFromView(Context context,View view) {


        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        Log.e("displayMt",displayMetrics.toString());
        return bitmap;
    }

    // 데이터 뷰에서 터치시의 이벤트 처리 여부를 리턴
    /*
    public boolean fClick(float x, float y, MixContext ctx, MixState state) {
        boolean evtHandled = false;

        if (isClickValid(x, y)) {	// 클릭 가능한 지점인 경우(클릭된 걸로 파악된 경우)
            evtHandled = state.handleEvent(ctx, URL);	// 마커의 URL 을 넘겨 이벤트 처리
        }
        return evtHandled;	// 성공했을 경우 true 를 리턴할 것이다
    }*/

    // 거리를 리턴
    public double getDistance() {
        return distance;
    }

    // 거리를 세팅
    public void setDistance(double distance) {
        this.distance = distance;
    }

    // ID를 리턴
    public String getID() {
        return ID;
    }

    // ID를 세팅
    public void setID(String iD) {
        ID = iD;
    }

    public Bitmap getImage(){return  image;}

    public void setImage(Bitmap image){this.image = image;}

    // 두 마커를 비교한다. 정확하게는 두 마커의 거리를 비교하여 동일한지 판단한다

    public int compareTo(Marker another) {

        Marker leftPm = this;
        Marker rightPm = another;

        return Double.compare(leftPm.getDistance(), rightPm.getDistance());

    }

    // 두 마커가 동일한지 ID로 판단한다
    @Override
    public boolean equals (Object marker) {
        return this.ID.equals(((Marker) marker).getID());
    }

    // 활성화 상태를 리턴
    public boolean isActive() {
        return active;
    }

    // 활성화 상태를 세팅
    public void setActive(boolean active) {
        this.active = active;
    }

    abstract public int getMaxObjects();



}


// 스크린에 출력될 라벨 클래스
class Label implements ScreenObj {

    private float x, y;	// 위치
    private float width, height;	// 넓이와 높이
    private ScreenObj obj;	// 표시될 객체

    // 표시될 객체를 준비한다
    public void prepare(ScreenObj drawObj) {
        obj = drawObj;
        float w = obj.getWidth();
        float h = obj.getHeight();

        x = w / 2;
        y = 0;

        width = w * 2;
        height = h * 2;
    }

    // 객체(라벨) 출력
    public void paint(PaintScreen dw) {
        dw.paintObj(obj, x, y, 0, 1);
    }

    // x 위치를 리턴
    public float getX() {
        return x;
    }

    // y 위치를 리턴
    public float getY() {
        return y;
    }

    // 넓이를 리턴
    public float getWidth() {
        return width;
    }

    // 높이를 리턴
    public float getHeight() {
        return height;
    }

}
