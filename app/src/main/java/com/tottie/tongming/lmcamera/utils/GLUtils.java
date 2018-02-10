package com.tottie.tongming.lmcamera.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.tottie.tongming.lmcamera.LMApplication;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tongming on 2018/1/22.
 */

public class GLUtils {

    /**
     * 创建OES纹理
     */
    public static int createOESTexture() {
        int[] tex = new int[1];
        GLES20.glGenTextures(1, tex, 0);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, tex[0]);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_S, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameterf(GLES11Ext.GL_TEXTURE_EXTERNAL_OES,
                GL10.GL_TEXTURE_WRAP_T, GL10.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        return tex[0];
    }



    public static boolean supportGlEs20() {
        ActivityManager activityManager = (ActivityManager) LMApplication.getContext().getSystemService(
                Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return false;
        }
        return activityManager.getDeviceConfigurationInfo().reqGlEsVersion >= 0x20000;
    }

}
