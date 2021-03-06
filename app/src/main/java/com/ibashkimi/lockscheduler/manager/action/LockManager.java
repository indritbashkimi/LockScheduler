package com.ibashkimi.lockscheduler.manager.action;

import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class LockManager {

    private static final String TAG = "LockManager";

    public static synchronized boolean setPassword(@NonNull final Context context, @NonNull final String password) {
        boolean result = setPassword(context, password, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
        showToast(context, result ? "Password changed successfully" : "Password change failed.");
        return result;
    }

    public static synchronized boolean setPin(@NonNull final Context context, @NonNull final String pin) {
        boolean result = setPassword(context, pin, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
        showToast(context, result ? "Password changed successfully" : "Password change failed.");
        return result;
    }

    public static synchronized boolean resetPassword(@NonNull final Context context) {
        boolean result = setPassword(context, "", DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        showToast(context, result ? "Password removed successfully" : "Password remove failed.");
        return result;
    }

    private static boolean setPassword(@NonNull final Context context, @NonNull final String password, final int passwordQuality) {
        DevicePolicyManager devicePolicyManager = AdminUtils.getDevicePolicyManager(context);
        //ComponentName componentName = AdminUtils.getComponentName(context);
        //devicePolicyManager.setPasswordQuality(componentName, passwordQuality);
        //devicePolicyManager.setPasswordMinimumLength(componentName, 4);
        //devicePolicyManager.setPasswordExpirationTimeout(componentName, passwordExpirationTimeout);
        // TODO
        //  For device owner and profile owners targeting SDK level
        //  {@link android.os.Build.VERSION_CODES#O} or above, this API is no longer available and will
        //  throw {@link SecurityException}
        return devicePolicyManager.resetPassword(password, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
    }

    private static void showToast(@NonNull Context context, @NonNull CharSequence msg) {
        Log.d(TAG, msg.toString());
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
