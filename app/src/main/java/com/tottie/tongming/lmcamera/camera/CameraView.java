package com.tottie.tongming.lmcamera.camera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.FrameLayout;

import com.tottie.tongming.lmcamera.utils.ScreenUtils;
import com.tottie.tongming.lmcamera.utils.ToastHelper;

import java.util.Arrays;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;

/**
 * Created by tongming on 2018/1/19.
 */

public class CameraView extends FrameLayout {

    private static final String TAG = CameraView.class.getSimpleName();
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    private GLSurfaceView mSurfaceView;
    private CameraDevice mCameraDevice;
    private String mCameraId;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private ImageReader mImageReader;
    private CaptureRequest.Builder mCaptureRequestBuilder;
    private CameraCaptureSession mCameraCaptureSession;
    private boolean mFlashSupported;
    private CaptureRequest mCaptureRequest;
    private CameraSurfaceRender mCameraSurfaceRender;

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
        mSurfaceView = new GLSurfaceView(getContext());
        mSurfaceView.setEGLContextClientVersion(2);
        mCameraSurfaceRender = new CameraSurfaceRender(this);
        CameraWindowSurfaceFactory windowSurfaceFactory = new CameraWindowSurfaceFactory();
        CameraWindowContextFactory windowContextFactory = new CameraWindowContextFactory();
        mSurfaceView.setEGLContextFactory(windowContextFactory);
        mSurfaceView.setEGLWindowSurfaceFactory(windowSurfaceFactory);
        mSurfaceView.setRenderer(mCameraSurfaceRender);
        mSurfaceView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        addView(mSurfaceView);
    }

    public void createCamera() {
        Log.d(TAG, "createCamera: ");
        if (!CameraHelper.getInstance().checkHardwareSupportLevel(String.valueOf(CameraMetadata.LENS_FACING_FRONT))) {
            ToastHelper.toast("你的手机不支持");
            return;
        }
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
            Surface surface = new Surface(mCameraSurfaceRender.getSurfaceTexture());
            mCaptureRequestBuilder.addTarget(surface);
            mCameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
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
        int screenWidth = ScreenUtils.getScreenWidth();
        int screenHeight = ScreenUtils.getScreenHeight();
        Rect rect = new Rect(screenWidth / 2 - 150, screenHeight / 2 - 150,
                screenWidth / 2 + 150, screenHeight / 2 + 150);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_REGIONS,
                new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_REGIONS,
                new MeteringRectangle[]{new MeteringRectangle(rect, 1000)});
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                CameraMetadata.CONTROL_AF_TRIGGER_START);
        mCaptureRequestBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,
                CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
        mCaptureRequest = mCaptureRequestBuilder.build();
        try {
            mCameraCaptureSession.setRepeatingRequest(mCaptureRequest, null, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void capture(@Nullable CameraCaptureSession.CaptureCallback callback) throws CameraAccessException {
        Log.d(TAG, "capture: ");
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.capture(mCaptureRequest, callback, mHandler);
        }
    }

    /**
     * 手动对焦
     */
    public void triggerFocus() {
        Log.d(TAG, "triggerFocus: ");

    }

    /**
     * 自动对焦
     */
    public void autoFocus() {
        Log.d(TAG, "autoFocus: ");

    }

    public GLSurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public void openBackCamera() {
        Log.d(TAG, "openBackCamera: ");
        CameraHelper.getInstance().openBackCamera(new CameraStateCallBack(), mHandler);
    }

    public void openFrontCamera() {
        CameraHelper.getInstance().openFrontCamera(new CameraStateCallBack(), mHandler);
    }

    /**
     * 切换到普通预览模式
     */
    public void switchNormalPreview() {

    }

    public void stopBackgroundThread() {
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
            try {
                mHandlerThread.join();
                mHandlerThread = null;
                mHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseCamera() {
        if (mCameraCaptureSession != null) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
            mCameraId = "";
        }
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
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

    class CaptureCallBack extends CameraCaptureSession.CaptureCallback {

        private void processResult(CaptureResult result) {
            Log.d(TAG, "processResult: result = " + result.get(CaptureResult.CONTROL_AF_STATE));
            Integer afState = result.get(CaptureResult.CONTROL_AF_STATE);
            if (null == afState) {
                return;
            }
            if (CaptureRequest.CONTROL_AF_STATE_FOCUSED_LOCKED == afState
                    || CaptureRequest.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED == afState) {
                switchNormalPreview();
            }
        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
            processResult(partialResult);
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
            processResult(result);
        }
    }

    class CameraWindowSurfaceFactory implements GLSurfaceView.EGLWindowSurfaceFactory {

        @Override
        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config, Object nativeWindow) {
            return egl.eglCreateWindowSurface(display, config, nativeWindow, null);
        }

        @Override
        public void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }
    }

    class CameraWindowContextFactory implements GLSurfaceView.EGLContextFactory {

        private static final int EGL_CONTEXT_VERSION = 0x3098;

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig) {
            int[] attrib_list = new int[]{EGL_CONTEXT_VERSION, 2, EGL10.EGL_NONE};
            return egl.eglCreateContext(display, eglConfig, null, attrib_list);
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context) {
            egl.eglDestroyContext(display, context);
        }
    }
}
