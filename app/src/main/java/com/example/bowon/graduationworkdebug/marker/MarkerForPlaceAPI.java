package com.example.bowon.graduationworkdebug.marker;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bowon.graduationworkdebug.R;
import com.example.bowon.graduationworkdebug.gui.PaintScreen;

import java.text.NumberFormat;

/**
 * Created by bowon on 2017-05-02.
 */

public class MarkerForPlaceAPI extends Marker{
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

    View markerRootView;
    ImageView markerImage;
    TextView markerTitle;
    TextView markerInformation;
    Context context;


    public MarkerForPlaceAPI(String title, double latitude, double longitude, double altitude, String link){
        super(title, latitude, longitude, altitude, link);



    }


    public void drawMarker(PaintScreen dw,Context context,View view) {
        super.draw(dw);
        String markerInfomationString = NumberFormat.getCurrencyInstance().format(getDistance());
        setCustomMarkerView();
        markerTitle.setText(title);
        markerInformation.setText(markerInfomationString);

        dw.paintBitmap(createDrawableFromView(context,view),cMarker.x,cMarker.y);

    }


    public  void setCustomMarkerView(){

        markerRootView = LayoutInflater.from(context).inflate(R.layout.item_maps_custommarker,null);
        markerImage = (ImageView)markerRootView.findViewById(R.id.item_maps_marker_image);
        markerTitle = (TextView)markerRootView.findViewById(R.id.item_maps_marker_title);
        markerInformation = (TextView) markerRootView.findViewById(R.id.item_maps_marker_imfomation);
    }



    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }

    @Override
    public int getMaxObjects() {
        return 0;
    }
}
