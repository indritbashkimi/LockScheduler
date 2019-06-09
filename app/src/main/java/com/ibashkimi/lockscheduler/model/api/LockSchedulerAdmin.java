package com.ibashkimi.lockscheduler.model.api;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class LockSchedulerAdmin extends DeviceAdminReceiver {

    private static final String TAG = "LockSchedulerAdmin";

    void showToast(Context context, CharSequence msg) {
        Log.d(TAG, msg.toString());
        Toast.makeText(context, "Lock Scheduler: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(@NonNull Context context, @NonNull Intent intent) {
        return "This is an optional message to warn the user about disabling.";
    }

    @Override
    public void onDisabled(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "Password changed");
    }

    @Override
    public void onPasswordFailed(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "Password failed");
    }

    @Override
    public void onPasswordSucceeded(@NonNull Context context, @NonNull Intent intent) {
        showToast(context, "Password succeeded");
    }

    @Override
    public void onPasswordExpiring(@NonNull Context context, @NonNull Intent intent) {
        super.onPasswordExpiring(context, intent);
        showToast(context, "password expired");
        LockManager.resetPassword(context);
    }

    public static boolean isAdminActive(@NonNull Context context) {
        return AdminUtils.getDevicePolicyManager(context).isAdminActive(AdminUtils.getComponentName(context));
    }

    public static void removeAdmin(@NonNull Context context) {
        AdminUtils.getDevicePolicyManager(context).removeActiveAdmin(AdminUtils.getComponentName(context));
    }
}
