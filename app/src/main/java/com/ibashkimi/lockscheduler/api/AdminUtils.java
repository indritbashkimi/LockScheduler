package com.ibashkimi.lockscheduler.api;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.ibashkimi.lockscheduler.R;

public class AdminUtils {

    public static Intent buildAddAdminIntent(@NonNull Context context) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context));
        String permissionRationale = context.getString(R.string.admin_permission_rationale);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, permissionRationale);
        return intent;
    }

    @NonNull
    static ComponentName getComponentName(@NonNull final Context context) {
        return new ComponentName(context, LockSchedulerAdmin.class);
    }

    @NonNull
    static DevicePolicyManager getDevicePolicyManager(@NonNull final Context context) {
        DevicePolicyManager devicePolicyManager = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (devicePolicyManager == null) {
            throw new IllegalStateException("Can't get DevicePolicyManagerService.");
        }
        return devicePolicyManager;
    }
}
