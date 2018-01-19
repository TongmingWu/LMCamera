package com.tottie.tongming.lmcamera.ui;

import android.Manifest;
import android.util.Log;
import android.view.View;

import com.tottie.tongming.lmcamera.R;
import com.tottie.tongming.lmcamera.base.BaseActivity;
import com.tottie.tongming.lmcamera.camera.CameraView;
import com.tottie.tongming.lmcamera.utils.PermissionUtils;
import com.tottie.tongming.lmcamera.utils.ToastHelper;

import butterknife.BindView;

public class MainActivity extends BaseActivity {

    @BindView(R.id.cv_content)
    CameraView cvContent;

    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void setupView() {
        Log.d(TAG, "setupView: ");
        grantCamera();
    }

    private void grantCamera() {
        PermissionUtils.grantPermission(this, Manifest.permission.CAMERA, result -> {
            if (result) {
                initView();
            } else {
                ToastHelper.toast("获取权限失败，即将退出");
                finish();
            }
        });
    }

    private void initView() {

    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        cvContent.releaseCamera();
        cvContent.stopBackgroundThread();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
