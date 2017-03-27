package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.widget.TextView;

import com.example.bowon.graduationworkdebug.AugmentedView;
import com.example.bowon.graduationworkdebug.Datatype.LocationCoordinate;
import com.example.bowon.graduationworkdebug.GetAddress;
import com.example.bowon.graduationworkdebug.PermissionHelper;
import com.example.bowon.graduationworkdebug.R;

public class MainMixedView extends AppCompatActivity implements SensorEventListener{


    TextView tempText1;
    TextView tempText2;
    TextView tempText3;

    private TextureView cameraTexturePreview;
    private Camera2Preview camera2Preview;


    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGeoScope;
    private Sensor sensorRotationVector;

    LocationManager locationManager;
    PermissionHelper permissionHelper;

    private AugmentedView argumentedView;


    float[] mAccelerometerReading= new float[3];
    float[] mMagnetometerReading= new float[3];

    float[] mRotationMatrix = new float[9];
    float[] mOrientationAngles = new float[3];



    private GetAddress getAddress;

    public static final String PREFS_CODE = "MainMixedViewSettings";

    //위치정보
    LocationCoordinate locationCoordinate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);



        locationCoordinate = new LocationCoordinate();
        cameraTexturePreview = (TextureView)findViewById(R.id.cameraTexturePreicew);
        camera2Preview = new Camera2Preview(this,cameraTexturePreview);

        argumentedView = new AugmentedView(this);
        addContentView(argumentedView, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.MATCH_PARENT));

        sensorAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorGeoScope = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
       // sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);


        getAddress = new GetAddress(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        setTempTextview();
        getPermissionGroup();

        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,mLocationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,1,mLocationListener);

        }catch (SecurityException e){
            Log.d("Exception","security");
        }
        updateTextview();
        camera2Preview.onResume();

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorGeoScope, SensorManager.SENSOR_DELAY_NORMAL);




    }

    @Override
    protected void onPause() {
        //사용 중지시 사용한 센서들 해제
        super.onPause();
        camera2Preview.onPause();
        sensorManager.unregisterListener(this);
    }

    public void setTempTextview(){
        tempText1 = (TextView)findViewById(R.id.tempText1);//내 좌표
        tempText2 = (TextView)findViewById(R.id.tempText2);//위치정보제공자
        tempText3 = (TextView)findViewById(R.id.tempText3); //센서정보
        tempText1.setText("위치정보 수신 전");
        tempText3.setText("Sensor 정보 미 수신");


    }


    public void getPermissionGroup(){
        // 권한요청 - 시작과 동시에 필요한 권한들
        permissionHelper = new PermissionHelper(this);
        permissionHelper.LocationPermission();
        permissionHelper.CameraPermission();
    }
    public void updateTextview(){

        tempText1.setText("la : " + locationCoordinate.getLatitude() +"\nlo : " + locationCoordinate.getLongitude()+"\nat : " + locationCoordinate.getAltitude());
        tempText2.setText("provider : " + locationCoordinate.getProvider() + "\nacc : " + locationCoordinate.getAccuracy()+"\naddress : " +getAddress.getAddressName(locationCoordinate.getLatitude(),locationCoordinate.getLongitude()) );

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case PermissionHelper.LOCATION_ACCESS__FINE : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한요청 성공 콜백

                } else {
                    // 권한요청 거부 콜백

                }
            }break;

            case PermissionHelper.CAMERA_PERMISSION : {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //권한요청 성공 콜백
                    cameraTexturePreview = (TextureView)findViewById(R.id.cameraTexturePreicew);
                    camera2Preview = new Camera2Preview(this,cameraTexturePreview);

                } else {
                    // 권한요청 거부 콜백

                }
            }break;

        }

    }//end of




    private  LocationListener mLocationListener = new LocationListener() {


        private void LocationDataUpdate(Location location){
            //location좌표 객체 셋팅
            locationCoordinate.setLocation(location);

        }

        @Override
        public void onLocationChanged(Location location) {
            LocationDataUpdate(location);
            updateTextview();
            Log.d("location","update");
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
    };//end of LoccationListener

    //Start of sensorListener

    @Override
    public void onSensorChanged(SensorEvent event) {


        //0 : 방위각 z 1 : 피치(x) 2:롤(y)
       if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
        System.arraycopy(event.values,0,mAccelerometerReading,0,mAccelerometerReading.length);

       }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
           System.arraycopy(event.values,0,mMagnetometerReading,0,mMagnetometerReading.length);
       }


        //위에서 구한 데이터를 기준으로 각도 계산
        updateOrientationAngles();


        tempText3.setText("방위 : "+Math.toDegrees(mOrientationAngles[0])+"\n상하경사 : "+Math.toDegrees(mOrientationAngles[1])+"\n좌우경사 : "+Math.toDegrees(mOrientationAngles[2]));
        //tempText3.setBackgroundColor(Color.WHITE);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    //End of sensorListener

    //위에서 구한 벡터를 이용하여 방위데이터를 구한다.
    public void updateOrientationAngles(){
        // 메트릭스 데이터
        sensorManager.getRotationMatrix(mRotationMatrix,null,mAccelerometerReading,mMagnetometerReading);

        // 방위각도데이터
        sensorManager.getOrientation(mRotationMatrix,mOrientationAngles);
        //0 azimuth방위 1 pitch상하경사 2 roll좌우경사

        /*
        * 회전시 변화
        * 방위        : ?
        * 상사경사    : ?
        * 좌우경사    :-> 방위
        * */
    }
}
