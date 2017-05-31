package com.example.bowon.graduationworkdebug.marker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.example.bowon.graduationworkdebug.gui.PaintScreen;

/**
 * Created by bowon on 2017-05-15.
 */

public class MarkerForPersonal extends Marker{

    String personalMarkerId;
    String imagePath;
    public MarkerForPersonal(String title, double latitude, double longitude, double altitude, String link, String imagePath, String date) {
        super(title, latitude, longitude, altitude, link);
           this.personalMarkerId = date;
           this.imagePath = imagePath;

    }
    @Override
    public void draw(PaintScreen dw) {

        // 텍스트 블록을 그린다
        drawTextBlock(dw);

        // 보여지는 상황이라면
        if (isVisible) {
            float maxHeight = Math.round(dw.getHeight() / 10f) + 1;	// 최대 높이 계산
            // 데이터 소스의 비트맵 파일을 읽어온다
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            // 비트맵 파일이 읽혔다면 적절한 위치에 출력
            if(bitmap!=null) {
                dw.paintBitmap(bitmap, cMarker.x - maxHeight/1.5f, cMarker.y - maxHeight/1.5f);
            }
        }
    }
    public String getImagePath(){
        return imagePath;
    }
    public void setImagePath(){
        this.imagePath = imagePath;
    }

    @Override
    public int getMaxObjects() {
        return 0;
    }

    public String getDate(){
        return personalMarkerId;
    }


}
