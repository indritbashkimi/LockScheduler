package it.ibashkimi.lockscheduler.model.api;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;


public class AdminApiHelper {

    private DevicePolicyManager deviceManger;
    private ComponentName compName;

    public AdminApiHelper(Context context) {
        deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        compName = new ComponentName(context, LockSchedulerAdmin.class);
    }

    public Intent buildAddAdminIntent() {
        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, compName);
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                "Additional text explaining why this needs to be added.");
        return intent;
    }

    public boolean isAdminActive() {
        return deviceManger.isAdminActive(compName);
    }

    private void removeAdmin() {
        deviceManger.removeActiveAdmin(compName);
    }
}
