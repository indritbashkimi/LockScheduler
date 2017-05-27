package com.ibashkimi.lockscheduler.model.api;

import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

public class LockSchedulerAdmin extends DeviceAdminReceiver {

    private static final String TAG = "LockSchedulerAdmin";

    void showToast(Context context, CharSequence msg) {
        Log.d(TAG, msg.toString());
        Toast.makeText(context, "Lock Scheduler: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        showToast(context, "Device Admin enabled");
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return "This is an optional message to warn the user about disabling.";
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        showToast(context, "Device Admin disabled");
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        showToast(context, "Password changed");
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        showToast(context, "Password failed");
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        showToast(context, "Password succeeded");
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
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
