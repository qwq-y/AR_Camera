package com.example.arcamera;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.arcamera.utils.CameraPermissionHelper;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Earth;
import com.google.ar.core.GeospatialPose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;

public class CameraActivity extends AppCompatActivity {

    private String TAG = "ww";

    // 是否请求 ARCore 安装
    private boolean mUserRequestedInstall = true;
    private Session mSession;
    private Config mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        // 检查 ArCore 的安装和更新，请求相机权限
        checkPermissionsAndInstallations();

    }

//    @Override
//    protected void onResume() {
//        super.onResume();
//        checkPermissionsAndInstallations();
//    }

    private void checkPermissionsAndInstallations() {
        // 检查 ARCore 支持状态
        checkARCoreSupportAndInstall();
        // 请求相机权限
        if (!CameraPermissionHelper.hasCameraPermission(this)) {
            CameraPermissionHelper.requestCameraPermission(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSession.close();
    }

    private void checkARCoreSupportAndInstall() {
        // 检查 ARCore 的支持状态
        ArCoreApk.Availability arAvailability = ArCoreApk.getInstance().checkAvailability(this);

        switch (arAvailability) {
            case SUPPORTED_INSTALLED:
                // ARCore 已经安装并可用
                break;

            case SUPPORTED_APK_TOO_OLD:
            case SUPPORTED_NOT_INSTALLED:
                try {
                    // 请求 ARCore 安装或更新
                    ArCoreApk.InstallStatus installStatus = ArCoreApk.getInstance().requestInstall(this, mUserRequestedInstall);
                    switch (installStatus) {
                        case INSTALL_REQUESTED:
                            // ARCore 安装请求已触发，应用程序可以继续运行
                            mUserRequestedInstall = false;
                            break;
                        case INSTALLED:
                            // ARCore 已成功安装，可以继续使用
                            break;
                    }
                } catch (UnavailableUserDeclinedInstallationException e) {
                    // 用户拒绝了 ARCore 安装请求
                    Toast.makeText(this, R.string.arcore_install_declined, Toast.LENGTH_LONG).show();
                } catch (UnavailableException e) {
                    // ARCore 不可用，处理异常
                    Toast.makeText(this, getString(R.string.arcore_unavailable, e), Toast.LENGTH_LONG).show();
                }
                break;

            case UNSUPPORTED_DEVICE_NOT_CAPABLE:
                // 这个设备不支持 ARCore
                Toast.makeText(this, R.string.unsupported_device, Toast.LENGTH_LONG).show();
                break;

            default:
                // 处理其他未知情况
                Toast.makeText(this, R.string.unknown_error, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CameraPermissionHelper.CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 用户授予了相机权限
                createArSession();
                try {
                    mSession.resume();
                    getPose();
                } catch (Exception e) {
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, R.string.camera_permission_required, Toast.LENGTH_LONG).show();
                if (!CameraPermissionHelper.shouldShowRequestPermissionRationale(this)) {
                    // 用户选择不再提醒时，显示权限设置界面。
                    CameraPermissionHelper.launchPermissionSettings(this);
                }
                finish();
            }
        }
    }

    private void createArSession() {
        try {
            mSession = new Session(this);
            mConfig = new Config(mSession);

            mConfig.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
            mConfig.setGeospatialMode(Config.GeospatialMode.ENABLED);

            mSession.configure(mConfig);
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void getPose() {
        Earth earth = mSession.getEarth();

        // 检查 Earth 是否处于跟踪状态
        if (earth.getTrackingState() == TrackingState.TRACKING) {
            // 获取 Earth-relative 虚拟相机姿态
            GeospatialPose geospatialPose = earth.getCameraGeospatialPose();
            Log.d(TAG, geospatialPose.toString());
        } else {
            // 处理 Earth 不在 TRACKING 状态的情况
        }
    }
}
