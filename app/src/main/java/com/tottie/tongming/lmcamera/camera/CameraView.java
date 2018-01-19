package com.tottie.tongming.lmcamera.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.util.Arrays;

/**
 * Created by tongming on 2018/1/19.
 */

public class CameraView extends FrameLayout implements SurfaceHolder.Callback2 {

    private static final String TAG = CameraView.class.getSimpleName();

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private boolean mFlashSupported;

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);

        addView(mSurfaceView);
    }

    private void createCamera() {
        Log.d(TAG, "createCamera: ");
        mHandlerThread = new HandlerThread(CameraView.class.getSimpleName());
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mImageReader = ImageReader.newInstance(mSurfaceView.getWidth(), mSurfaceView.getHeight(), ImageFormat.JPEG, 5);
        mImageReader.setOnImageAvailableListener(reader -> {

        }, mHandler);
        openBackCamera();
    }

    private void createCameraPreviewSession() {
        Log.d(TAG, "createCameraPreviewSession: ");
        try {
            CameraCharacteristics characteristics = CameraHelper.getInstance().getCameraCharacteristics(mCameraId);
            if (characteristics != null) {
                Boolean result = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                mFlashSupported = result == null ? false : result;
            }
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(mSurfaceHolder.getSurface());
            mCameraDevice.createCaptureSession(Arrays.asList(mSurfaceHolder.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigured: ");
                    if (mCameraDevice == null) {
                        return;
                    }
                    mCameraCaptureSession = session;
                    configCameraCaptureSession();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigureFailed: ");
                }
            }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void configCameraCaptureSession() {
        Log.d(TAG, "configCameraCaptureSession: ");
        //设置自动对焦
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        CaptureRequest captureRequest = mCaptureRequestBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(captureRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void capture(@Nullable CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.capture(mCaptureRequestBuilder.build(), callback, mHandler);
        }
    }

    public void openBackCamera() {
        Log.d(TAG, "openBackCamera: ");
        CameraHelper.getInstance().openBackCamera(new CameraStateCallBack(), mHandler);
    }

    public void openFrontCamera() {
        CameraHelper.getInstance().openFrontCamera(new CameraStateCallBack(), mHandler);
    }

    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surfaceCreated: ");
        createCamera();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    class CameraStateCallBack extends CameraDevice.StateCallback {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.d(TAG, "onOpened: " + camera.getId());
            mCameraDevice = camera;
            mCameraId = camera.getId();
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.d(TAG, "onDisconnected: " + camera.getId());
            releaseCamera();
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.d(TAG, "onError: error = " + error + " id = " + camera.getId());
            releaseCamera();
        }
    }

    public void releaseCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraId = "";
        }
    }
}
