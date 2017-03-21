package com.example.bowon.graduationworkdebug.MainMixedView;

import android.content.Context;
import android.graphics.Camera;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.nfc.Tag;
import android.os.HandlerThread;
import android.service.media.CameraPrewarmService;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.PermissionHelper;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;

/**
 * Created by bowon on 2017-02-14.
 */

//표준 camera2 라이브러리 참고
public class Camera2Preview extends Thread {

    // 카메라 화면을 프리뷰 형태로 띄워줄 쓰레드
    // 프리뷰 만을 구현한다.
    // camera2 구현
    final String TAG = "Camera2Preview : ";

    private TextureView textureView;
    private Context context;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private Size previewSize;
    PermissionHelper permissionHelper;




    public Camera2Preview(Context context, TextureView textureView){
        this.textureView = textureView;
        this.context = context;

    }


    private String getBackFacingCameraId(CameraManager cameraManager){
        try{
            for(final String cameraId : cameraManager.getCameraIdList()){
                 CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
                 int cameraOrientation = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
                 if(cameraOrientation == CameraCharacteristics.LENS_FACING_BACK) return cameraId;
            }
        }catch (CameraAccessException e){
                e.printStackTrace();
        }
        return null;
    }

    public void openCamera(){
        CameraManager cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);
        try{
            String cameraId = getBackFacingCameraId(cameraManager);
            CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            previewSize = streamConfigurationMap.getOutputSizes(SurfaceTexture.class)[0];


            permissionHelper = new PermissionHelper(context);
            permissionHelper.CameraPermission();
                Log.d(TAG,"cameraOpen");
                cameraManager.openCamera(cameraId, stateCallback, null);



        }catch (CameraAccessException e){
            e.printStackTrace();
        }catch (SecurityException e){
            e.printStackTrace();
        }

    }

    private TextureView.SurfaceTextureListener surfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };


    private  CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            Log.d(TAG,"onOpen");
            cameraDevice = camera;
            startPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.d(TAG,"onDisconnected");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.d(TAG,"onError");
        }
    };

    protected  void startPreview(){
        if (cameraDevice==null||!textureView.isAvailable()||previewSize==null){
            Log.e(TAG,"startPreviewFail");
        }
        SurfaceTexture texture = textureView.getSurfaceTexture();
        if(texture ==null){
            Log.e(TAG,"textureNULL");
            return;
        }
        texture.setDefaultBufferSize(previewSize.getWidth(),previewSize.getHeight());
        Surface surface = new Surface(texture);

        try{
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

        }catch (CameraAccessException e){
            e.printStackTrace();
        }

        previewBuilder.addTarget(surface);

        try{
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    previewSession = session;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                    Toast.makeText(context,"onConfigFail",Toast.LENGTH_LONG).show();
                }
            },null);
        }catch (CameraAccessException e){
            e.printStackTrace();
        }

    }
    protected void updatePreview(){
        if(cameraDevice==null){
            Log.e(TAG,"updatePreviewError");
        }
        previewBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        HandlerThread handlerThread = new HandlerThread("Camera2Preview");
        handlerThread.start();
        android.os.Handler backgroundHandler = new android.os.Handler(handlerThread.getLooper());

        try {
            previewSession.setRepeatingRequest(previewBuilder.build(), null, backgroundHandler);
        }catch(CameraAccessException e){
            e.printStackTrace();
        }
    }

    public void setSurfaceTextureListener(){
        textureView.setSurfaceTextureListener(surfaceTextureListener);
    }

     public void onResume(){
         Log.d(TAG,"onResume");
         setSurfaceTextureListener();
     }
    private Semaphore cameraOpenCloseLock = new Semaphore(1);

    public void onPause(){
        Log.d(TAG,"onPause");
        try {
            cameraOpenCloseLock.acquire();
            if(cameraDevice != null){
                cameraDevice.close();
                cameraDevice=  null;
                Log.d(TAG,"CameraClose");
            }
        }catch(InterruptedException e){
            e.printStackTrace();
            throw  new RuntimeException("Interrupted while trying to lock CameraClosing");
        }finally {
            cameraOpenCloseLock.release();
        }
    }


}
