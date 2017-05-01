package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.ArgumentedDataHandler;
import com.example.bowon.graduationworkdebug.AutoFitTextureView;
import com.example.bowon.graduationworkdebug.Datatype.LocationCoordinate;
import com.example.bowon.graduationworkdebug.GetAddress;
import com.example.bowon.graduationworkdebug.PermissionHelper;
import com.example.bowon.graduationworkdebug.R;
import com.example.bowon.graduationworkdebug.gui.PaintScreen;
import com.example.bowon.graduationworkdebug.render.Matrix;

import java.util.List;

public class MixedViewActivity extends AppCompatActivity implements SensorEventListener, LocationListener{

    /*Log용 태그*/
    private static final String TAG = "MixedViewActivity";
    /**
     * 이전 데이터 표시들을 위해 임시로 사용하는 UI
     * */
    TextView tempText1;
    TextView tempText2;
    TextView tempText3;

    /*UI 구현을 위한 UI item 선언부*/
    Button mapChangeButton;



    /*angle 계산을 위한 메트릭스*/
    Matrix matrix1 = new Matrix();
    Matrix matrix2 = new Matrix();
    Matrix matrix3 = new Matrix();
    Matrix matrix4 = new Matrix();

    private int rHistIdx = 0;
    private Matrix tempR = new Matrix();
    private Matrix finalR = new Matrix();
    private Matrix smoothR = new Matrix();
    private Matrix histR[] = new Matrix[60];

    /**/

    /*초기세팅 여부 판단*/
    private boolean isInited = false;

    /*메인뷰를 서포트하기 위한 뷰들*/
    private MixedViewContext mainMixedViewContext;
    private MixedViewState mainMixedViewState;
    static ArgumentedDataHandler argumentedDataHandler;

    /*카메라 프리뷰 사용을 위한 설정*/
    private AutoFitTextureView cameraTexturePreview;
    private Camera2Preview camera2Preview;


    /*
    * sensor 사용을 휘해 사용되는 객체들
    * */
    private SensorManager sensorManager;
    private Sensor sensorAccelerometer;
    private Sensor sensorGeoScope;
    private List<Sensor> sensors;
    private Sensor sensorRotationVector;

    private boolean isGpsProviderEnable;

    LocationManager locationManager;
    PermissionHelper permissionHelper;

    /* 증강데이터 부분을 다룰 증강뷰*/
    private AugmentedView argumentedView;

    /*센서 합성을 위해 사용되는 센서 배열값*/
    float[] mAccelerometerReading= new float[3];
    float[] mMagnetometerReading= new float[3];

    float[] mRotationMatrix = new float[9];
    float[] mOrientationAngles = new float[3];

    static PaintScreen dWindow;


    private GetAddress getAddress;

    public static final String PREFS_CODE = "MainMixedViewSettings";

    /*화면이 꺼지지 않도록 웨이크락 섷정*/
    private PowerManager.WakeLock mWakeLock;
    /*private*/



    double angleX,angleY;
    float Rot[] = new float[9];
    float I[] = new float[9];


    //위치정보
    LocationCoordinate locationCoordinate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        /*UI구성*/
        mapChangeButton = (Button)findViewById(R.id.button_for_changeviewtype_map);

        mapChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MixedViewActivity.this, GoogleMapsViewAcrivity.class);
                startActivity(intent);*/
                MixedViewActivity.this.finish();

            }
        });

        /*자체 센서 메니저 사용을 휘해 설정*/
        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);

        /*추후 웨이크락 사용시 사용*/


        locationCoordinate = new LocationCoordinate();
        cameraTexturePreview = (AutoFitTextureView)findViewById(R.id.cameraTexturePreicew);
        camera2Preview = new Camera2Preview(this,cameraTexturePreview);

        /*내부 메모리 프리퍼런스 호출 - 셋팅 저장용*/
        SharedPreferences settings = getSharedPreferences(PREFS_CODE, 0);
        SharedPreferences.Editor editor = settings.edit();

        /*프로그레스바와 줌 바가 설정이 되어있긴하다.*/
        /*이 부분은 나중에 좀더 생각해 보도록 한다.*/


        /*증강 스크린 설정*/
        argumentedView = new AugmentedView(this);
        addContentView(argumentedView, new ActionBar.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT));

        if(!isInited){
            mainMixedViewContext = new MixedViewContext(this);
            // 서버로부터 다운로드를 할 다운로드 관리자 설정

            argumentedDataHandler = new ArgumentedDataHandler(mainMixedViewContext);
            dWindow = new PaintScreen();
            isInited = true;
        }
        if(settings.getBoolean("firstAccess",false)==false){
            /*어플리케이션 최초 실행시의 처리
            * 추후 구현될 로그인 및 최초 처리에 있어 사용하기 위해 남겨놓는다.
            * */
        }


      // sensorRotationVector = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        /*geocodeing 사용을 위한 geocode 객체*/
        getAddress = new GetAddress(this);



    }

    /*센서값을 회전시켜 엥글을 계산시킨다.*/
    /*
    * matrix 1~3을 각각 삼각행렬을 설정시켜놓고 matrix4를 기준행렬로 만들어 놓는다.
    * */
    private void setViewAngleMatrix(){

        angleX = Math.toRadians(-90);
        matrix1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
                .sin(angleX), 0f, (float) Math.sin(angleX), (float) Math
                .cos(angleX));

        angleX = Math.toRadians(-90);
        angleY = Math.toRadians(-90);

        matrix2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX), (float) -Math
                .sin(angleX), 0f, (float) Math.sin(angleX), (float) Math.cos(angleX));
        matrix3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
                0f, 1f, 0f, (float) -Math.sin(angleY), 0f, (float) Math.cos(angleY));
        matrix4.toIdentity();
        for(int i=0;i<histR.length;i++){
            histR[i] = new Matrix();
        }

    }

    /*cos - sin =1
    *
    *
    * */


    @Override
    protected void onResume() {
        super.onResume();
        setTempTextview();
        getPermissionGroup();

        mainMixedViewContext.mainMixedViewActivity = this;
        argumentedDataHandler.doStart();


        /*ViewAngle설정을 위해 사용*/
       setViewAngleMatrix();

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);

        sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if(sensors.size()>0){
            sensorAccelerometer = sensors.get(0);
        }
        sensors = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
        if(sensors.size()>0){
            sensorGeoScope = sensors.get(0);
        }

        /* 지자기센서와 가속도센서 등록*/

        sensorManager.registerListener(this, sensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorGeoScope, SensorManager.SENSOR_DELAY_NORMAL);



        try {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            /*
            이걸 왜 여기서 다시 호출하는지는 의문 나중에 디버그 단계에서 create나 resume에서
            한번씩 지워보고 다시 해봐야 한다.
             *  */
            locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10, this);

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,1,this);

            /*각 provider로부터 데이터를 각각 읽어온다.*/
            Location gps,network;

            gps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            network = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            /*정확도가 더 좋은  gps를 우선적으로 등록하며 만일 gps 수신이 불가능할 경우에만 network를
            * 사용하여 등록하도록 한다.
            * */
            if(gps != null){
                mainMixedViewContext.currentLocation = gps;
            }else if(network != null){
                mainMixedViewContext.currentLocation = network;
            }else{
            }
            mainMixedViewContext.setLocationAtLastDownload(mainMixedViewContext.currentLocation);
            /**자신의 위치 등록 종려*/
        }catch (SecurityException e){
            Log.d("Exception","security");
        }

        GeomagneticField geomagneticField = new GeomagneticField((float)mainMixedViewContext.currentLocation.getLatitude(),
                (float)mainMixedViewContext.getCurrentLocation().getLongitude(),(float)mainMixedViewContext.currentLocation.getAltitude(),
                System.currentTimeMillis());

        angleY = Math.toRadians(-geomagneticField.getDeclination());
        matrix4.set((float) Math.cos(angleY), 0f,
                (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math
                        .sin(angleY), 0f, (float) Math.cos(angleY)
        );
        mainMixedViewContext.declination = geomagneticField.getDeclination();


        /**
         * 다운로드 쓰레드 활성화
         */

        updateTextview();
        camera2Preview.onResume();


         if(mainMixedViewContext.isActualLocation()==false){
            Toast.makeText(this, "Location is Not Actualable",Toast.LENGTH_LONG).show();
        }

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

        tempText1.setText("la : " + mainMixedViewContext.currentLocation.getLatitude() +"\nlo : " + mainMixedViewContext.currentLocation.getLongitude()+"\nat : " + mainMixedViewContext.currentLocation.getAltitude());
        tempText2.setText("provider : " + mainMixedViewContext.currentLocation.getProvider() + "\nacc : " +
                mainMixedViewContext.currentLocation.getAccuracy()+"\naddress : " +getAddress.getAddressName(mainMixedViewContext.currentLocation.getLatitude(),mainMixedViewContext.currentLocation.getLongitude()) );
        Log.e(TAG,"la : " + mainMixedViewContext.currentLocation.getLatitude() +"\nlo : " + mainMixedViewContext.currentLocation.getLongitude()+"\nat : " + mainMixedViewContext.currentLocation.getAltitude());
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
                    cameraTexturePreview = (AutoFitTextureView)findViewById(R.id.cameraTexturePreicew);
                    camera2Preview = new Camera2Preview(this,cameraTexturePreview);

                } else {
                    // 권한요청 거부 콜백

                }
            }break;

        }

    }//end of





    //Start of sensorListener

    @Override
    public void onSensorChanged(SensorEvent event) {


        //0 : 방위각 z 1 : 피치(x) 2:롤(y)
       if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
           mAccelerometerReading[0] = event.values[0];
           mAccelerometerReading[1] = event.values[1];
           mAccelerometerReading[2] = event.values[2];

        /*증강뷰에 변경을 알려야함*/
           argumentedView.postInvalidate();
       }else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
        //   System.arraycopy(event.values,0,mMagnetometerReading,0,mMagnetometerReading.length);
           mMagnetometerReading[0] = event.values[0];
           mMagnetometerReading[1] = event.values[1];
           mMagnetometerReading[2] = event.values[2];
       /*여기도*/
           argumentedView.postInvalidate();
       }



        //위에서 구한 데이터를 기준으로 각도 계산
        updateOrientationAngles();

         //tempText3.setBackgroundColor(Color.WHITE);

    }


    //위에서 구한 벡터를 이용하여 방위데이터를 구한다.
    /*잘 모르겠다. 나중에 삼각변환행렬 학습 이후에야 조금 알 수 있을 것 같다.*/
    private void updateOrientationAngles(){


        // 메트릭스 데이터
        sensorManager.getRotationMatrix(mRotationMatrix,I,mAccelerometerReading,mMagnetometerReading);

        sensorManager.remapCoordinateSystem(mRotationMatrix,SensorManager.AXIS_X,SensorManager.AXIS_MINUS_Z,Rot);

        tempR.set(Rot[0], Rot[1], Rot[2], Rot[3], Rot[4], Rot[5], Rot[6], Rot[7],
                Rot[8]);

        finalR.toIdentity();
        finalR.prod(matrix4);
        finalR.prod(matrix1);
        finalR.prod(tempR);
        finalR.prod(matrix3);
        finalR.prod(matrix2);
        finalR.invert();

        // 이후 부분에 대해서는 더 분석이 필요할 것 같다...
        histR[rHistIdx].set(finalR);
        rHistIdx++;
        if (rHistIdx >= histR.length)
            rHistIdx = 0;

        smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);

        for (int i = 0; i < histR.length; i++) {
            smoothR.add(histR[i]);
        }
        smoothR.mult(1 / (float) histR.length);

        /*결국 최종값을 MainMixedViewContext의 변환행렬에 등록을 하였다.
        * 아마도 이 값은 이 어플리케이션 사용환경에 맞게 회전되어진 센서의 산출값일 것 이다.
        * */
        synchronized (mainMixedViewContext.rotationMatrix) {
            mainMixedViewContext.rotationMatrix.set(smoothR);
        }

        tempText3.setText(mainMixedViewContext.rotationMatrix.toString());
        // 방위각도데이터
        // sensorManager.getOrientation(mRotationMatrix,mOrientationAngles);
        // 0 azimuth방위 1 pitch상하경사 2 roll좌우경사

        /*
        * 회전시 변화
        * 방위        : ?
        * 상사경사    : ?
        * 좌우경사    :-> 방위
        * */
    }


    /*start of LocationListener*/

    /*위치데이터의 수신을 받고 이를 처리한다*/
    private void LocationDataUpdate(Location location){
        //location좌표 객체 셋팅
        locationCoordinate.setLocation(location);

    }

    @Override
    public void onLocationChanged(Location location) {

       // updateTextview();
        Log.d("location","update");



        if(LocationManager.GPS_PROVIDER.equals(location.getProvider())){
           /*
           *  가장 정확한 위치 제공자인 GPS를 통한 값을 우선해서 받으며
           *  위치 정확도를 유지한다.
           * */
            synchronized (mainMixedViewContext.currentLocation){
                mainMixedViewContext.currentLocation = location;
            }

            //if(argumentedDataHandler.isFrozen()){
                /*이후에 데이터 핸들러의 상태가 정상적이라면 데이터 헨들러를 통해
                * 자신의 위치 변경을 알린다.
                * 여기서 위치변경이 알려지면 마커의 거리표시와, 현제 표시할 마커등의 데이터를
                * 바뀐 위치 기준으로 정렬시킨다.
                *
                *
                * */
                argumentedDataHandler.getDataHandlerForMarker().onLocationChanged(location);
         //   }

            /**
             데이터핸들러 실행 이후 일정 거리가 넘어가면 해당 위치 기준으로 다시
             서버와의 통신을 통해 데이터를 받아야 하므로 거리를 판단하여 재 할당 받는 것 이다.
             * */
            Location tempLastLocation = mainMixedViewContext.getLocationAtLastDownload();
            if(tempLastLocation == null) {
                /*만일 현제 데이터 핸들러 상의 위치정보가 없다면 (오류나 최초실행의 경우)
                * 현제 위치를 등록해준다.
                * */
                mainMixedViewContext.setLocationAtLastDownload(location);
            }else{
                float threshold = argumentedDataHandler.getRadius()*1000f/3f;
                if(location.distanceTo(tempLastLocation)>threshold){
                    argumentedDataHandler.doStart();
                    Log.d(TAG,"Restarting download due to location change");

                }
                /*gps가 현제 사용 가능한 상태라는 것을 알려준다.*/
                isGpsProviderEnable = true;
            }

        }else{
            synchronized (mainMixedViewContext.currentLocation){
                mainMixedViewContext.currentLocation = location;
            }

            // mainMixedViewContext.currentLocation = location;
            //if (argumentedDataHandler.isFrozen()){
                argumentedDataHandler.getDataHandlerForMarker().onLocationChanged(location);
          //  }

            Location tempLastLocation = mainMixedViewContext.getLocationAtLastDownload();
            if(tempLastLocation == null) {
                /*만일 현제 데이터 핸들러 상의 위치정보가 없다면 (오류나 최초실행의 경우)
                * 현제 위치를 등록해준다.
                * */
                mainMixedViewContext.setLocationAtLastDownload(location);
            }else{
                float threshold = argumentedDataHandler.getRadius()*1000f/3f;
                if(location.distanceTo(tempLastLocation)>threshold){
                    argumentedDataHandler.doStart();

                }
                /*gps가 현제 사용 가능한 상태라는 것을 알려준다.*/
                isGpsProviderEnable = true;
            }

        }


        LocationDataUpdate(location);
        updateTextview();




    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
     /*센서의 정확도가 변경되었을 경우*/

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        /*위치제공자의 유형이 변경되었을 경우 처리*/
        isGpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onProviderDisabled(String provider) {
        /*위치를 제공받지 못하는 경우의 처리를 한다.*/
        isGpsProviderEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    /*end of LocationListener*/
}
