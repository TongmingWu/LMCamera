package com.tottie.tongming.lmcamera.camera;

import android.graphics.SurfaceTexture;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.tottie.tongming.lmcamera.utils.GLUtils;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by tongming on 2018/1/22.
 */

public class CameraSurfaceRender implements GLSurfaceView.Renderer {

    private static final String TAG = CameraSurfaceRender.class.getSimpleName();

    private final float[] mMatrix = new float[16];
    private int oesTexture;
    private SurfaceTexture mSurfaceTexture;
    private CameraView mCameraView;

    public CameraSurfaceRender(CameraView cameraView) {
        this.mCameraView = cameraView;
    }

    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        GLES20.glClearColor(0, 0, 0, 1);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);
        oesTexture = GLUtils.createOESTexture();
        mCameraView.createCamera();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged: width = " + width + " height = " + height);
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.d(TAG, "onDrawFrame: ");
        GLES20.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        mSurfaceTexture = new SurfaceTexture(oesTexture);
        mSurfaceTexture.setOnFrameAvailableListener(surfaceTexture -> {
            mCameraView.getSurfaceView().requestRender();
        });
        if (mSurfaceTexture != null) {
            //需要将上一帧的数据消耗掉,不然无法获取最新的数据
            mSurfaceTexture.updateTexImage();
            mSurfaceTexture.getTransformMatrix(mMatrix);
        }
    }
}
