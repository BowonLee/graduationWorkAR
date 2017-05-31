package com.example.bowon.graduationworkdebug;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.example.bowon.graduationworkdebug.marker.Marker;
import com.example.bowon.graduationworkdebug.marker.MarkerForPlaceAPI;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionApi;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
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
public class GoogleMapsViewAcrivity extends FragmentActivity implements OnMapReadyCallback, LocationListener, GoogleMap.OnMarkerClickListener, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {


    /*
    * 전역적으로 사용 될 증강 데이터 핸들러
    * */
    private  DataHandlerForMarker mDataHandlerForMarker;

    // 전체적으로 사용할 마커들 - 넓은 반경
    public static List<Marker> markerList;
    public static List<Marker> persnalMarkerList;
    Marker markerOnMap;
    /*커스텀 마커용 아이템*/

    View markerRootView;
    ImageView markerImage;
    TextView markerTitle;
    TextView markerInformation;
    Context context;
    com.google.android.gms.maps.model.Marker selectedMarker;
    Boolean isMarkerImageReady = false;

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
    Button createMarkerButton;

    /*
    * 지도화면 출력 -> 내 위치로 지도화면 이동(map 객체 사용) -> 서버통신 (asynktask)->서버통신 완료시 마커 생성
    *
    * */


    int PLACE_PICKER_REQUEST =1 ;
    // placaapi 관련
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        markerList = new ArrayList<Marker>();
        persnalMarkerList = new ArrayList<Marker>();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

       // CameraUpdateFactory.newLatLng();

        viewChangeButton = (Button)findViewById(R.id.button_for_changeviewtype_camera);
        viewChangeButton.setOnClickListener(this);
        createMarkerButton = (Button)findViewById(R.id.button_for_write_map);
        createMarkerButton.setOnClickListener(this);


        setCustomMarkerView();

        mLocationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        mPermissionHelper = new PermissionHelper(this);


        mPermissionHelper.storageReadPerMission();
        mPermissionHelper.storageWritePerMission();
        mPermissionHelper.LocationPermission();
        mPermissionHelper.CameraPermission();

        /*
        * mGoogleAPI
        * detection을 이용하여 주변정보를 얻어온다.
        * */
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Places.GEO_DATA_API).addApi(Places.PLACE_DETECTION_API).enableAutoManage(this,this).build();

    }

    @Override
    protected void onResume() {
        super.onResume();

        try {

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

        createPlaceMarkers();

        drawMarkersOnMap();
    }

    private void drawMarkersOnMap(){
        for(int i = 0;i<mDataHandlerForMarker.getMarkerLisrSize();i++){
            createMarkerOnMap(mDataHandlerForMarker.getMarker(i),false);
        }
        //placePhotosTask();
    }

    private void drawPernalMarkersOnMap(){

    }


    public void createPlaceMarkers(){
        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {


            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {

                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                    markerList.add(
                            new MarkerForPlaceAPI(placeLikelihood.getPlace().getName().toString(),
                            placeLikelihood.getPlace().getLatLng().latitude,
                            placeLikelihood.getPlace().getLatLng().longitude,0,null,
                            placeLikelihood.getPlace().getId())
                    );
                }
                likelyPlaces.release();
                mDataHandlerForMarker.addMarkers(markerList);
                drawMarkersOnMap();
            }

        });
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
        Drawable markerImageDrawable;

        markerTitle.setText(markerTitleString);
        markerInformation.setText(markerInfomationString);

        if(isMarkerImageReady){

        markerImageDrawable = new BitmapDrawable(getResources(),marker.getImage());
            if(marker.getImage() == null){

                Log.e("markerImageLog","nullImage");

            }else{

                Log.e("markerImageLog",""+marker.getImage().toString());
                markerImage.setBackground(markerImageDrawable);

            }
            Toast.makeText(this,"BitMapRedraw",Toast.LENGTH_SHORT);

        }

        if(isSelectedMarker){
                    markerOptions.alpha(1);
        }
        else{
                    markerOptions.alpha(0.7f);
        }
        markerOptions.title(markerTitleString);
        markerOptions.position(position);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this,markerRootView)));

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
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),20));


        mLocationMarkerOp.position(new LatLng(location.getLatitude(),location.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_map_mlocation)).snippet(location.getProvider());
        updateMarkers();


        Toast.makeText(this,location.getProvider().toString(),Toast.LENGTH_SHORT);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }



    private void placePhotosTask() {

        // Create a new AsyncTask that displays the bitmap and attribution once loaded.
        new PhotoTask(markerImage.getWidth(), markerImage.getHeight()) {
            @Override
            protected void onPreExecute() {
                // Display a temporary image to show while bitmap is loading.
            }

            @Override
            protected void onPostExecute(Integer integer) {
                super.onPostExecute(integer);
                isMarkerImageReady = true;

                mMap.clear();
                drawMarkersOnMap();

            }
        }.execute("start");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){

            case R.id.button_for_changeviewtype_camera : {
                Intent intent = new Intent(GoogleMapsViewAcrivity.  this, MixedViewActivity.class);
                startActivity(intent);
            }break;
            case R.id.button_for_write_map : {
                Intent intent = new Intent(GoogleMapsViewAcrivity.  this, CreatePersonalMarkerActivity.class);
                startActivity(intent);

            }break;
        }
    }


    /*
    * 플레이스 api를 이용하여 비트맵을 받아오기 위해 구현된 Asynktask
    * */
    abstract class PhotoTask extends AsyncTask<String, Void, Integer> {

        private int mHeight;

        private int mWidth;

        public PhotoTask(int width, int height) {

            /*
            * 이미지 뷰에 맞게 비트맵 이미지 크기를 미리 지정
            * */
            mHeight = width;
            mWidth = height;
        }

        /**
         * Loads the first photo for a place id from the Geo Data API.
         * The place id must be the first (and only) parameter.
         */
        @Override
        protected Integer doInBackground(String... params) {

            String placeId ;

            PlacePhotoMetadataResult result;

            for(int i =0;i<markerList.size();i++){
                placeId = markerList.get(i).getID();
                result = Places.GeoDataApi
                        .getPlacePhotos(mGoogleApiClient, placeId).await();
                Log.e("Asynk",""+markerList.get(i).getTitle());

                if (result.getStatus().isSuccess()) {
                    PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
                    if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                        // Get the first bitmap and its attributions.
                        PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                        CharSequence attribution = photo.getAttributions();
                        // Load a scaled bitmap for this photo.



                        Bitmap image = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                                .getBitmap();
                        Log.e("AsynkImage",""+image.toString());
                        markerList.get(i).setImage(image);


                    }else{
                        Log.e("AsynkImage","Cancel Or BufferOut");

                    }
                    // Release the PlacePhotoMetadataBuffer.
                    photoMetadataBuffer.release();
                }else{
                    Log.e("Asynk","imageLoadFail");

                }

            }

            return 1;
        }


        /**
         * Holder for an image and its attribution.
         */
    }



}
