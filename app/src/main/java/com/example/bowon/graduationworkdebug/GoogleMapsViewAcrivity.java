package com.example.bowon.graduationworkdebug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.example.bowon.graduationworkdebug.marker.Marker;
import com.example.bowon.graduationworkdebug.marker.MarkerForPlaceAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
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
public class GoogleMapsViewAcrivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener {


    /*
    * 전역적으로 사용 될 증강 데이터 핸들러
    * */
    private  DataHandlerForMarker mDataHandlerForMarker;

    // 전체적으로 사용할 마커들 - 넓은 반경
    public static List<Marker> markerList;
    Marker markerOnMap;
    /*커스텀 마커용 아이템*/

    View markerRootView;
    ImageView markerImage;
    TextView markerTitle;
    TextView markerInformation;
    Context context;
    com.google.android.gms.maps.model.Marker selectedMarker;

    //내위치 관력
    private LocationManager mLocationManager;
    private Location mLocation;
    private Boolean isLocationPossible= false;
    private MarkerOptions mLocationMarkerOp;
    private com.google.android.gms.maps.model.Marker mLocationMarker;

    //권한관련
    private PermissionHelper mPermissionHelper;

    private GoogleMap mMap;
    Button viewChangeButton;
    PermissionHelper permissionHelper;

    /*
    * 지도화면 출력 -> 내 위치로 지도화면 이동(map 객체 사용) -> 서버통신 (asynktask)->서버통신 완료시 마커 생성
    *
    * */


    // placaapi 관련
    private GoogleApiClient mGoogleApiClient;

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


        /*
        * mGoogleAPI
        * */
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this,this).build();



    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            permissionHelper.LocationPermission();
            Location gps, network;

            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);

            gps = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (gps != null) {
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 10, this);
            } else if (network != null) {
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 10, this);
            } else {
            }


        }catch (SecurityException e){
            e.printStackTrace();
        }
        mLocationMarkerOp = new MarkerOptions();

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
        mMap.setOnMarkerClickListener(this);
       // Add a marker in Sydney and move the camera
        mDataHandlerForMarker = new DataHandlerForMarker();

        createMarkers();
        drawMarkersOnMap();


    }

    private void drawMarkersOnMap(){
        for(int i = 0;i<mDataHandlerForMarker.getMarkerLisrSize();i++){
            createMarkerOnMap(mDataHandlerForMarker.getMarker(i),false);
        }


    }



    public void createMarkers(){

        markerList.add(new MarkerForPlaceAPI("부천역",37.484322,126.782747,0,null));
        markerList.add(new MarkerForPlaceAPI("공과대학",37.373268,126.634797,0,null));
        markerList.add(new MarkerForPlaceAPI("자연과학대",37.375009,126.636460,0,null));
        markerList.add(new MarkerForPlaceAPI("인천대 입구",37.386469,126.639383,0,null));
        markerList.add(new MarkerForPlaceAPI("정보기술대",37.374543,126.633457,0,null));

        mDataHandlerForMarker.addMarkers(markerList);

    }







    public  void setCustomMarkerView(){

        markerRootView = LayoutInflater.from(this).inflate(R.layout.item_maps_custommarker,null);
        markerImage = (ImageView)markerRootView.findViewById(R.id.item_maps_marker_image);
        markerTitle = (TextView)markerRootView.findViewById(R.id.item_maps_marker_title);
        markerInformation = (TextView) markerRootView.findViewById(R.id.item_maps_marker_imfomation);
    }


    private com.google.android.gms.maps.model.Marker createMarkerOnMap(Marker marker, boolean isSelectedMarker){

        LatLng position = new LatLng(marker.getLatitude(),marker.getLongitude());
        String markerTitleString = marker.getTitle();
        String markerInfomationString = NumberFormat.getCurrencyInstance().format(marker.getDistance());
        MarkerOptions markerOptions = new MarkerOptions();

        markerTitle.setText(markerTitleString);
        markerInformation.setText(markerInfomationString);

        if(isSelectedMarker){
                    markerOptions.alpha(1);
        }
        else{
                    markerOptions.alpha(0.7f);
        }
        markerOptions.title(markerTitleString);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,markerRootView)));
        markerOptions.alpha(0.7f);
        markerOptions.snippet(marker.getID());

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


    public void updateMarkers(){
        mMap.clear();
        drawMarkersOnMap();
        mMap.addMarker(mLocationMarkerOp).setZIndex(1);

    }

    @Override
    public void onLocationChanged(Location location) {

        mDataHandlerForMarker.onLocationChanged(location);

        mLocation = location;
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),16));


        mLocationMarkerOp.position(new LatLng(location.getLatitude(),location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map_mlocation));
        updateMarkers();


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


    @Override
    public boolean onMarkerClick(com.google.android.gms.maps.model.Marker marker) {
        CameraUpdate center = CameraUpdateFactory.newLatLng(marker.getPosition());
        mMap.animateCamera(center);
        marker.setAlpha(1);
        marker.hideInfoWindow();
        updateMarkers();

        return false;
    }


    /*연결 실패시 처리*/
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
