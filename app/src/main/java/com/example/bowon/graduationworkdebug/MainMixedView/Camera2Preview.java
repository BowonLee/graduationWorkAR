package com.example.bowon.graduationworkdebug.MainMixedView;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import com.example.bowon.graduationworkdebug.AutoFitTextureView;
import com.example.bowon.graduationworkdebug.MainMixedView.MixedViewActivity;
import com.example.bowon.graduationworkdebug.PermissionHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by bowon on 2017-04-24.
 */
/*
* Camera2를 이용한 프리뷰 형태의 구현 함수, 쓰래도로 구현되었으며 프리뷰의 출력만을 담당한다
 *
* */


public class Camera2Preview extends Thread  {

    Context context;
    MixedViewActivity mixedViewActivity;
    PermissionHelper permissionHelper;

    public Camera2Preview(Context context, AutoFitTextureView textureView){
        this.context = context;
        mTextureView = textureView;
        mixedViewActivity = (MixedViewActivity)context;
        permissionHelper = new PermissionHelper(context);
    }


    private static final String TAG = "Camera@preview";
    /**
     * Max preview width that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_WIDTH = 1920;

    /**
     * Max preview height that is guaranteed by Camera2 API
     */
    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(width,height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            configureTransform(width,height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private  String mCameraId;

    private TextureView mTextureView;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private Size mPreviewSize;

    private final CameraDevice.StateCallback mStateCallBack = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            mCameraDevice = cameraDevice;
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = mixedViewActivity;
            if (null!=activity){
            activity.finish();
            }
        }
    };

    private HandlerThread mBackgroundThread;

    private Handler mBakcgroundHandler;

    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    private int mSensorOrientation;

    private CaptureRequest.Builder mPreviewRequestBuilder;

    private  CaptureRequest mPreviewRequest;


    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }
// Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.e(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

    public void onResume(){
        startBackgroundThread();
        if(mTextureView.isAvailable()){
            openCamera(mTextureView.getWidth(),mTextureView.getHeight());
        }else{
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }


    }
    public void onPause(){
        closeCamera();
        stopBackgroundThread();

    }
    /*
    * 카메라 셋팅에 필요한 값들을 설정한다.
    * 사용할 카메라 ID,
    * */
    private void setUpCameraOutputs(int width,int height){
        Activity activity  = mixedViewActivity;
        CameraManager manager = (CameraManager)activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            /* 사용할 카메라 ID 값을 알아온다.*/
            for(String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    /*
                    전면 카메라흫 사용하지 않기에 제외시킨다.
                    * */
                    continue;
                }
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map == null) {
                    continue;

                }
                Size largest = Collections.max(
                        Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),
                        new CompareSizesByArea());
                //수직, 수평 판단 이후 그에따른 카메라 회전
                int displayRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                boolean swappedDimensions = false;
                switch (displayRotation) {
                    case Surface.ROTATION_0:
                    case Surface.ROTATION_180:
                        if (mSensorOrientation == 90 || mSensorOrientation == 270) {
                            swappedDimensions = true;
                        }
                        break;
                    case Surface.ROTATION_90:
                    case Surface.ROTATION_270:
                        if (mSensorOrientation == 0 || mSensorOrientation == 180) {
                            swappedDimensions = true;
                        }
                        break;
                    default:
                        Log.e(TAG, "Display rotation is invalid: " + displayRotation);
                }

                Point displaySize = new Point();
                activity.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;

                if (swappedDimensions) {
                    rotatedPreviewWidth = height;
                    rotatedPreviewHeight = width;
                    maxPreviewWidth = displaySize.y;
                    maxPreviewHeight = displaySize.x;
                }

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                int orientation = mixedViewActivity.getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                  //  mTextureView.setAspectRatio(
                   //          mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                  //  mTextureView.setAspectRatio(
                  //          mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    /*
    * 카메라 디바이스를 연다
    * */
    private void openCamera(int width, int height) {

        setUpCameraOutputs(width, height);
        configureTransform(width, height);
        Activity activity = mixedViewActivity;
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            permissionHelper.CameraPermission();
            manager.openCamera(mCameraId, mStateCallBack, mBakcgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e){
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*카메라 디바이스 종료시 */
    private void closeCamera() {
        try {

            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBakcgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
           mBakcgroundHandler= null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * 카메라 세션 열기
     * */
    private void createCameraPreviewSession(){

        SurfaceTexture texture = mTextureView.getSurfaceTexture();
        assert texture !=null;
        texture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());

        Surface surface = new Surface(texture);

        try {
            //surface설정
            mPreviewRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            /*capturesession 설정*/
            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                            if(mCameraDevice == null){
                                return;
                            }
                          mCaptureSession = cameraCaptureSession;
                            mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE,
                                    CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

                            mPreviewRequest = mPreviewRequestBuilder.build();
                            HandlerThread handlerThread = new HandlerThread("Camera2Preview");
                            handlerThread.start();
                            android.os.Handler backgroundHandler = new android.os.Handler(handlerThread.getLooper());

                            try {
                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, backgroundHandler);
                            }catch(CameraAccessException e){
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {

                        }
                    },null
            );
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }



    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }

    /*
    * 상황에 따른 프리뷰 회전을 위한 수치계산
    * */
    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = mixedViewActivity;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }


}
