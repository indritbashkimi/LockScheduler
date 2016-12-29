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
    //private ActivityManager activityManager;
    private ComponentName compName;

    public LockManager(Context context) {
        deviceManger = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        compName = new ComponentName(context, LockSchedulerAdmin.class);
        this.context = context;
    }

    public boolean setPassword(final String password) {
        Log.d(TAG, "setPin() called");
        //if (deviceManger.hasGrantedPolicy(compName, ))
        deviceManger.setPasswordQuality(compName, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
        deviceManger.setPasswordMinimumLength(compName, 4);
        deviceManger.setPasswordExpirationTimeout(compName, Constants.PASSWORD_EXPIRATION_TIMEOUT);
        boolean result = deviceManger.resetPassword(password,
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        return result;
    }

    public boolean setPin(final String pin) {
        Log.d(TAG, "setPin() called");
        //if (deviceManger.hasGrantedPolicy(compName, ))
        deviceManger.setPasswordQuality(compName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
        deviceManger.setPasswordMinimumLength(compName, 4);
        deviceManger.setPasswordExpirationTimeout(compName, Constants.PASSWORD_EXPIRATION_TIMEOUT);
        boolean result = deviceManger.resetPassword(pin,
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        return result;
        //deviceManger.lockNow();
    }

    public boolean resetPassword() {
        Log.d(TAG, "resetPassword() called");
        boolean result = deviceManger.resetPassword("",
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password removed successfully" : "Password remove failed.";
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
        return result;
    }
}
