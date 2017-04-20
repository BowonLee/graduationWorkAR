package com.example.bowon.graduationworkdebug;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.bowon.graduationworkdebug.DataManagement.DataHandlerForMarker;
import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



/*
* 지도가 그려져있는 부분이다.
*
* */
public class GoogleMapsViewAcrivity extends FragmentActivity implements OnMapReadyCallback{

    /*
    * 전역적으로 사용 될 증강 데이터 핸들러
    * */
    public static DataHandlerForMarker staticDataHandlerForMarker;



    private GoogleMap mMap;
    Button viewChangeButton;
    PermissionHelper permissionHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionHelper = new PermissionHelper(this);


        viewChangeButton = (Button)findViewById(R.id.button_for_changeviewtype_camera);

        viewChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GoogleMapsViewAcrivity.  this, MixedViewActivity.class);
                startActivity(intent);
            }
        });





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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(37.3751636,126.6339779);
        LatLng sydney2 = new LatLng(37.3748073,126.6335562);



        mMap.addMarker(new MarkerOptions().position(sydney).title("학산도서관").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_basic)));

        mMap.addMarker(new MarkerOptions().position(sydney2).title("정보기술대").icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_basic)));



        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

       try {
            permissionHelper.LocationCoarsePermission();
            mMap.setMyLocationEnabled(true);
        }catch (SecurityException e){
            e.printStackTrace();
        }


    }
}
