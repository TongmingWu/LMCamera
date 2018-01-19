package com.tottie.tongming.lmcamera.utils;

import android.widget.Toast;

import com.tottie.tongming.lmcamera.LMApplication;

/**
 * Created by tongming on 2018/1/18.
 */

public class ToastHelper {

    public static void toast(String content) {
        Toast.makeText(LMApplication.getContext(), content, Toast.LENGTH_SHORT).show();
    }

}
