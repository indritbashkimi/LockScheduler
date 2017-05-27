package com.ibashkimi.lockscheduler.model.api;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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
        DevicePolicyManager devicePolicyManager = getDevicePolicyManager(context);
        //ComponentName componentName = getComponentName(context);
        //devicePolicyManager.setPasswordQuality(componentName, passwordQuality);
        //devicePolicyManager.setPasswordMinimumLength(componentName, 4);
        //devicePolicyManager.setPasswordExpirationTimeout(componentName, passwordExpirationTimeout);
        return devicePolicyManager.resetPassword(password, DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
    }

    @NonNull
    private static ComponentName getComponentName(@NonNull final Context context) {
        return new ComponentName(context, LockSchedulerAdmin.class);
    }

    @NonNull
    private static DevicePolicyManager getDevicePolicyManager(@NonNull final Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager == null) {
            throw new IllegalStateException("Can't get DevicePolicyManagerService.");
        }
        return devicePolicyManager;
    }

    private static void showToast(@NonNull Context context, @NonNull CharSequence msg) {
        Log.d(TAG, msg.toString());
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
}
