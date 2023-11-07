package com.example.arcamera.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class CameraPermissionHelper {
    public static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

    public static boolean hasCameraPermission(Activity activity) {
        int cameraPermission = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.CAMERA);
        return cameraPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestCameraPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }

    public static boolean shouldShowRequestPermissionRationale(Activity activity) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, android.Manifest.permission.CAMERA);
    }

    public static void launchPermissionSettings(Activity activity) {
        // 如果用户拒绝了相机权限并选择不再提醒，启动应用程序设置界面以允许用户手动授予权限。
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
