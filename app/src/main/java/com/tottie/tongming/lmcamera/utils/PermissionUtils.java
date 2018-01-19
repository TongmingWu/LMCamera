package com.tottie.tongming.lmcamera.utils;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.tottie.tongming.lmcamera.LMApplication;
import com.tottie.tongming.lmcamera.listener.GrantResultListener;

/**
 * Created by tongming on 2018/1/18.
 */

public class PermissionUtils {

    private static final String TAG = PermissionUtils.class.getSimpleName();

    public static void grantPermission(@NonNull Activity activity, String permission, GrantResultListener listener) {
        new RxPermissions(activity)
                .request(permission)
                .subscribe(aBoolean -> {
                    Log.d(TAG, "grantPermission: aBoolean = " + aBoolean);
                    if (listener != null) {
                        listener.grantResult(aBoolean);
                    }
                }, throwable -> {
                    throwable.printStackTrace();
                    ToastHelper.toast("获取权限异常");
                });
    }

    public static boolean checkSelfPermission(@RequiresPermission String permission) {
        return ActivityCompat.checkSelfPermission(LMApplication.getContext(), permission) == PackageManager.PERMISSION_GRANTED;
    }

}
