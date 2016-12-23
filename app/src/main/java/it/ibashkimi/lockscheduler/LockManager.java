package it.ibashkimi.lockscheduler;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Indrit Bashkimi <indrit.bashkimi@gmail.com>
 */

public class LockManager {
    private static final String TAG = "LockManager";

    private Context context;
    private DevicePolicyManager deviceManger;
    private ActivityManager activityManager;
    private ComponentName compName;

    public LockManager(Context context) {
        deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(context, LockSchedulerAdmin.class);
        this.context = context;
    }

    public boolean setLockPin(final String pin) {
        Log.d(TAG, "setLockPin() called");
        //deviceManger.setPasswordQuality(compName, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        //deviceManger.setPasswordMinimumLength(compName, 4);

        //deviceManger.isAdminActive(compName)
        boolean result = deviceManger.resetPassword(pin,
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        return result;
        //deviceManger.lockNow();
    }

    public boolean removeLockPin() {
        Log.d(TAG, "removeLockPin() called");
        boolean result = deviceManger.resetPassword("",
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password removed successfully" : "Password remove failed.";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        return result;
    }
}
