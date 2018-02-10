package com.tottie.tongming.lmcamera.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.tottie.tongming.lmcamera.LMApplication;

/**
 * Created by tongming on 2018/1/22.
 */

public class ScreenUtils {

    public static int getScreenWidth() {
        return getScreenWidth(LMApplication.getContext());
    }

    public static int getScreenHeight() {
        return getScreenHeight(LMApplication.getContext());
    }

    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

}
