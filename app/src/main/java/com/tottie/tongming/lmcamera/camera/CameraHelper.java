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

import com.tottie.tongming.lmcamera.LMApplication;
import com.tottie.tongming.lmcamera.utils.PermissionUtils;
import com.tottie.tongming.lmcamera.utils.ToastHelper;

/**
 * Created by tongming on 2018/1/18.
 */

public class CameraHelper {

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

    public void releaseCamera() {

    }

    private boolean checkCameraPermission() {
        return PermissionUtils.checkSelfPermission(Manifest.permission.CAMERA);
    }

}
