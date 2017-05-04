package com.example.bowon.graduationworkdebug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.example.bowon.graduationworkdebug.marker.Marker;
import com.example.bowon.graduationworkdebug.marker.MarkerForPlaceAPI;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
/*
* 시작 엑티비티 최초화면
* databus, 마커데이터 저장 시작점
*
* 엑티비티 시작 -> 맵 그림 - > API 통신 -> 마커(MAP) 생성 -> 마커뿌리기 ㄴ
*
* */


/*
* 지도가 그려져있는 부분이다.
* */
public class GoogleMapsViewAcrivity extends FragmentActivity implements OnMapReadyCallback, LocationListener{

    /*
    * 전역적으로 사용 될 증강 데이터 핸들러
    * */
    public static DataHandlerForMarker staticDataHandlerForMarker;

    // 전체적으로 사용할 마커들 - 넓은 반경
    public List<Marker> markerList;
    Marker markerOnMap;
    /*커스텀 마커용 아이템*/

    View markerRootView;
    ImageView markerImage;
    TextView markerTitle;
    TextView markerInformation;
    Context context;


    //내위치 관력
    private LocationManager mLocationManager;

    //권한관련
    private PermissionHelper mPermissionHelper;

    private GoogleMap mMap;
    Button viewChangeButton;
    PermissionHelper permissionHelper;

    /*
    * 지도화면 출력 -> 내 위치로 지도화면 이동(map 객체 사용) -> 서버통신 (asynktask)->서버통신 완료시 마커 생성
    *
    * */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        markerList = new ArrayList<Marker>();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionHelper = new PermissionHelper(this);
       // CameraUpdateFactory.newLatLng();

        viewChangeButton = (Button)findViewById(R.id.button_for_changeviewtype_camera);

        viewChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapsViewAcrivity.  this, MixedViewActivity.class);
                startActivity(intent);
            }
        });

        setCustomMarkerView();

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mPermissionHelper = new PermissionHelper(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

          Location gps,network;
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, this);
        mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,10, this);
        permissionHelper.LocationPermission();
        gps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        network = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        if(gps != null){
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, this);
        }else if(network != null){
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,10, this);
        }else{
        }

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.537523, 126.96558), 14));
        // Add a marker in Sydney and move the camera

        createMarkers();

        for(Marker marker: markerList){
            createMarkerOnMap(marker,false);
        }
       try {
            permissionHelper.LocationCoarsePermission();
           // mMap.setMyLocationEnabled(true);

        }catch (SecurityException e){
            e.printStackTrace();
        }


    }

    public void createMarkers(){

        markerList.add(new MarkerForPlaceAPI("부천역",37.484322,126.782747,0,null));


    }



    public void drawMarkersOnMap(){
        LatLng marker;

        for(int i = 0;i<=markerList.size();i++){

           // googleMap.addMarker();
        }


    }

    public  void setCustomMarkerView(){

        markerRootView = LayoutInflater.from(this).inflate(R.layout.item_maps_custommarker,null);
        markerImage = (ImageView)markerRootView.findViewById(R.id.item_maps_marker_image);
        markerTitle = (TextView)markerRootView.findViewById(R.id.item_maps_marker_title);
        markerInformation = (TextView) markerRootView.findViewById(R.id.item_maps_marker_imfomation);
    }


    public com.google.android.gms.maps.model.Marker createMarkerOnMap(Marker marker,boolean isSelectedMarker){

        LatLng position = new LatLng(marker.getLatitude(),marker.getLongitude());
        String markerTitleString = marker.getTitle();
        String markerInfomationString = NumberFormat.getCurrencyInstance().format(marker.getDistance());

        markerTitle.setText(markerTitleString);
        markerInformation.setText(markerInfomationString);

        if(isSelectedMarker){

        }
        else{

        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(markerTitleString);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,markerRootView)));

        return mMap.addMarker(markerOptions);
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

        return bitmap;
    }



    @Override
    public void onLocationChanged(Location location) {


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),10));
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
       // mMap.animateCamera();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
