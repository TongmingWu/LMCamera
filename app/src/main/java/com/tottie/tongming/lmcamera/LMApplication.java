package com.tottie.tongming.lmcamera;

import android.app.Application;
import android.content.Context;

/**
 * Created by tongming on 2018/1/18.
 */

public class LMApplication extends Application {

    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
    }
}
