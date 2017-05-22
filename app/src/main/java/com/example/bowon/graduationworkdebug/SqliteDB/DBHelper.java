package com.example.bowon.graduationworkdebug.SqliteDB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.marker.Marker;

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
*  IMGPATH      BLOB    bitmap or String(path)          마커 이미지
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
        query.append("DATE TEXT PRIMARY KEY)");

        db.execSQL(query.toString());

        Toast.makeText(context,"Table 생성",Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 버전 변경에 따른 처리사항 게시
    }


    //DB입력
    public void insertMakrer(Marker marker){
        SQLiteDatabase db = getWritableDatabase();
        /**
         * 쿼리문 생성
         * */
        StringBuffer query = new StringBuffer();


        db.close();
    }

    //DB열람(전부 가져오기 시작할때 모든 데이터 가져와 리스트 만들어 놓기)
    public void getDB(){

        SQLiteDatabase db = getReadableDatabase();
    }

    //DB삭제
    public void deleteData(){
        SQLiteDatabase db = getWritableDatabase();

        /**
         * 쿼리문 생성
         * */

        db.close();
    }

}
