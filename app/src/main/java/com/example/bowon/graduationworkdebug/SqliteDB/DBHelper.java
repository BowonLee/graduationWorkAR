package com.example.bowon.graduationworkdebug.SqliteDB;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.util.Log;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.GoogleMapsViewAcrivity;
import com.example.bowon.graduationworkdebug.marker.Marker;
import com.example.bowon.graduationworkdebug.marker.MarkerForPersonal;
import com.example.bowon.graduationworkdebug.marker.MarkerForPlaceAPI;

import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by bowon on 2017-05-22.
 */

/*
*
*  db의 생성과 관리를 담당한다.
*  저장 완료 클릭 시 -> 마커의 정보를 DB에 저장 하도록 한다.
*
*  마커의 정보
*  TABLE 이름 -> BASICM_MARKER
*  NAME         TYPE    TYPE(ANDROID)
*  -----------------------------------------------------------------------------------
*  TITLE        TEXT    String                          마커의 이름
*  IMG          BLOB    bitmap or String(path)          마커 이미지
*  LAT          REAL    double                          위도
*  LNG          REAL    double                          경도
*  ALT          READ    double                          고도
*  DATE         TEXT    String                          년 월 일 시 분 초   *고유키*
*
*
*  OnClickListener - 저장버튼, 카메라 버튼
* */



public class DBHelper extends SQLiteOpenHelper{
    private Context context;
    private final String INSERT_QUERY_START = "insert into BASIC_MARKER values(";
    private final String DELETE_QUERY_START = "delete from BASIC_MARKER where DATE = " ;



    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
        this.context = context;
    }

    /*
    * DB가 존재하지 않을 경우 최초의 생성을 한다
    * 한번만 실행
    * */
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        * DB생성용 쿼리는 스트링 버퍼를 이용하여 생성
        * 테이블 최초 생성
        * */
        StringBuffer query= new StringBuffer();
        query.append(" CREATE TABLE BASIC_MARKER ( ");
        query.append("TITLE TEXT,");
        query.append("LAT REAL,");
        query.append("LNG REAL,");
        query.append("ALT REAL,");
        query.append("IMGPATH TEXT,");
        query.append("DATE TEXT PRIMARY KEY);");

        db.execSQL(query.toString());

        Toast.makeText(context,"Table 생성",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 버전 변경에 따른 처리사항 게시
    }


    //DB입력
    public void insertMakrer(MarkerForPersonal marker){

        SQLiteDatabase db = getWritableDatabase();

        /**
         * 쿼리문 생성
         * 쿼리 순서(제목,위도,경도,고도,이미지패스,생성일자)
         * */
        StringBuffer query = new StringBuffer();
        query.append(INSERT_QUERY_START);
        query.append("\""+marker.getTitle()+"\""+",");
        query.append(marker.getLatitude()+",");
        query.append(marker.getLongitude()+",");
        query.append(marker.getAltitude()+",");
        query.append("\""+marker.getImagePath()+"\""+",");
        query.append("\""+marker.getDate()+"\""+")");

        db.execSQL(query.toString());

        Log.e("dbhelper","insertMarker");
        db.close();
    }

    public void updateMarkerTitle(Marker marker){
        //마커에 입력되었던 제목을 수정
        SQLiteDatabase db = getReadableDatabase();
        StringBuffer query = new StringBuffer();

        db.close();

    }
    //마커삭제
    public void deleteData(String markerDate){
        SQLiteDatabase db = getWritableDatabase();
        StringBuffer query = new StringBuffer();
        query.append(DELETE_QUERY_START);
        query.append(markerDate+";");
        db.close();
        Toast.makeText(context,"DeleteMarker",Toast.LENGTH_SHORT).show();

    }

    //DB열람(전부 가져오기 시작할때 모든 데이터 가져와 리스트 만들어 놓기)
    public void createMarkersFromDB(){
        /*
        * 제목 위도 경도 고도 링크 이미지 아이디(년월일시)
        * */
        // DB에 저장되어 있던 데이터들을 모두 가져온다.
        StringBuffer query = new StringBuffer();
        query.append("SELECT * FROM BASIC_MARKER");

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery(query.toString(),null);

        Marker tempMarker;
        String title;
        Double lat,lng,alt;
        String imagePath;
        String date;
        // 순서 - 제목 위도 고도 경도 이미지 날자
        while (cursor.moveToNext()){
            title = cursor.getString(0);
            lat = cursor.getDouble(1);
            lng = cursor.getDouble(2);
            alt = cursor.getDouble(3);
            imagePath = cursor.getString(4);
            date = cursor.getString(5);
            GoogleMapsViewAcrivity.persnalMarkerList.add(
                    new MarkerForPersonal(title,lat,lng,alt,null,imagePath,date)
            );
        }



    }

    /*
    * bitmap 클레스를 바이너리 클레스로 변환시켜준다 - > sqlite 저장 전용
    * */
    public byte[] getByteArrayFromDrawable(Bitmap bitmap) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] data = stream.toByteArray();

        return data;
    }

    public Bitmap getAppIcon(byte[] b) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(b, 0, b.length);
        return bitmap;
    }


}
