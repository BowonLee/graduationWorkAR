package com.example.bowon.graduationworkdebug.MainMixedView;

import android.app.Activity;
import android.content.Context;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.graphics.RectF;
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
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.widget.Toast;

import com.example.bowon.graduationworkdebug.AutoFitTextureView;
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

    private AutoFitTextureView textureView;
    private Context context;
    private CameraDevice cameraDevice;
    private CaptureRequest.Builder previewBuilder;
    private CameraCaptureSession previewSession;
    private Size previewSize;
    PermissionHelper permissionHelper;
    private MixedViewActivity mixedViewActivity;



    public Camera2Preview(Context context, AutoFitTextureView textureView){
        this.textureView = textureView;
        this.context = context;
        mixedViewActivity = (MixedViewActivity)context;

    }

    public void setCameraDevice(CameraDevice cameraDevice) {
        this.cameraDevice = cameraDevice;
    }
    private String getBackFacingCameraId(CameraManager cameraManager){
        /*후면 카메라를 사용하도록 설정한다*/
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

    public void openCamera(int width,int height){
        CameraManager cameraManager = (CameraManager)context.getSystemService(Context.CAMERA_SERVICE);


        configureTransform(width, height);
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

    private AutoFitTextureView.SurfaceTextureListener surfaceTextureListener = new AutoFitTextureView.SurfaceTextureListener() {

        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
           configureTransform(width,height);
            openCamera(width,height);
        }


        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width,height);
        }


        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
              //  updatePreview();
        }
    };


    private  CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened( CameraDevice cameraDevice) {
            Log.d(TAG,"onOpen");
            setCameraDevice(cameraDevice);
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
         if(textureView.isAvailable()){
             transformImage(textureView.getWidth(),textureView.getHeight());
             openCamera();
         }
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

    private void transformImage (int width, int height) {
        if (previewSize == null || textureView == null) {
            return;
        }
        Matrix matrix = new Matrix();

        int rotation = mixedViewActivity.getWindowManager().getDefaultDisplay().getRotation();
        RectF textureRectF = new RectF(0, 0, width, height);
        RectF previewRectF = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = textureRectF.centerX();
        float centery = textureRectF.centerY();

        if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_270) {
        } else if (rotation == Surface.ROTATION_90 || rotation == Surface.ROTATION_270) {
            previewRectF.offset(centerX - previewRectF.centerX(), centery - previewRectF.centerY());
            matrix.setRectToRect(textureRectF, previewRectF, Matrix.ScaleToFit.FILL);
            float scale = Math.max((float) width / previewSize.getWidth(), (float) height / previewSize.getHeight());

            matrix.postScale(scale, scale, centerX, centery);
            matrix.postRotate(90 * (rotation - 2), centerX, centery);
            textureView.setTransform(matrix);

        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = (Activity) context;
        if (null == textureView || null == previewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, previewSize.getHeight(), previewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / previewSize.getHeight(),
                    (float) viewWidth / previewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }

}
