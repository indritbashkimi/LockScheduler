package com.ibashkimi.lockscheduler.model.api;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class AdminUtils {

    public static Intent buildAddAdminIntent(@NonNull Context context) {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, getComponentName(context));
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
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
