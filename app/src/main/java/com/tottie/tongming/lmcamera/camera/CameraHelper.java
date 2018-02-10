package com.tottie.tongming.lmcamera.camera;

import android.Manifest;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.WindowManager;

import com.tottie.tongming.lmcamera.LMApplication;
import com.tottie.tongming.lmcamera.utils.PermissionUtils;
import com.tottie.tongming.lmcamera.utils.ToastHelper;

/**
 * Created by tongming on 2018/1/18.
 */

public class CameraHelper {

    private static final String TAG = CameraHelper.class.getSimpleName();
    private static CameraHelper instance;
    private final CameraManager cameraManager;

    public static CameraHelper getInstance() {
        if (instance == null) {
            synchronized (CameraHelper.class) {
                if (instance == null) {
                    instance = new CameraHelper();
                }
            }
        }
        return instance;
    }

    private CameraHelper() {
        cameraManager = (CameraManager) LMApplication.getContext().getSystemService(Context.CAMERA_SERVICE);
    }

    @Nullable
    public CameraCharacteristics getCameraCharacteristics(@NonNull String cameraId) {
        try {
            return cameraManager.getCameraCharacteristics(cameraId);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 打开后置摄像头
     */
    public void openBackCamera(@NonNull CameraDevice.StateCallback callback,
                               @Nullable Handler handler) {
        internalOpenCamera(String.valueOf(CameraMetadata.LENS_FACING_FRONT), callback, handler);
    }

    /**
     * 打开前置摄像头
     */
    public void openFrontCamera(@NonNull CameraDevice.StateCallback callback,
                                @Nullable Handler handler) {
        internalOpenCamera(String.valueOf(CameraMetadata.LENS_FACING_BACK), callback, handler);
    }

    private void internalOpenCamera(@NonNull String cameraId, @NonNull CameraDevice.StateCallback callback,
                                    @Nullable Handler handler) {
        if (!checkCameraPermission()) {
            return;
        }
        try {
            cameraManager.openCamera(cameraId, callback, handler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
            ToastHelper.toast("开启相机异常");
        }
    }

    public boolean checkHardwareSupportLevel(String cameraId) {
        boolean result = false;
        int level = getHardwareSupportLevel(cameraId);
        switch (level) {
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED:
                result = true;
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_FULL:
                result = true;
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY:
                result = true;
                break;
            case CameraMetadata.INFO_SUPPORTED_HARDWARE_LEVEL_3:
                result = false;
                break;
        }
        Log.d(TAG, "checkHardwareSupportLevel: level = " + level);
        return result;
    }

    private int getHardwareSupportLevel(String cameraId) {
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            Integer level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
            return level == null ? 0 : level;
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int getDisplayOrientation() {
        WindowManager windowManager = (WindowManager) LMApplication.getContext().getSystemService(Context.WINDOW_SERVICE);
        if (windowManager == null) {
            return 0;
        }
        Display display = windowManager.getDefaultDisplay();
        int rotation = display.getRotation();
        int degress = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degress = 0;
                break;
            case Surface.ROTATION_90:
                degress = 90;
            case Surface.ROTATION_180:
                degress = 180;
            case Surface.ROTATION_270:
                degress = 270;
        }
        // TODO: 2018/1/20 需修改
        return 0;
    }

    public void releaseCamera() {

    }

    private boolean checkCameraPermission() {
        return PermissionUtils.checkSelfPermission(Manifest.permission.CAMERA);
    }

}
