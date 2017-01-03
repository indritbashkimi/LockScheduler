package it.ibashkimi.lockscheduler;

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

    private Context mContext;
    private DevicePolicyManager mDevicePolicyManager;
    private ComponentName mCompName;
    private int mPasswordExpirationTimeout;

    public LockManager(Context context) {
        //mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mCompName = new ComponentName(context, LockSchedulerAdmin.class);
        mContext = context;
        mPasswordExpirationTimeout = Integer.parseInt(context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
                .getString("password_expiration", "0"));
    }

    public DevicePolicyManager getDevicePolicyManager() {
        if (mDevicePolicyManager == null) {
            mDevicePolicyManager =
                    (DevicePolicyManager) mContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (mDevicePolicyManager == null) {
                Log.e(TAG, "Can't get DevicePolicyManagerService: is it running?",
                        new IllegalStateException("Stack trace:"));
            }
        }
        return mDevicePolicyManager;
    }

    public boolean setPassword(final String password) {
        Log.d(TAG, "setPin() called");
        getDevicePolicyManager();
        mDevicePolicyManager.setPasswordQuality(mCompName, DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC);
        mDevicePolicyManager.setPasswordMinimumLength(mCompName, 4);
        mDevicePolicyManager.setPasswordExpirationTimeout(mCompName, mPasswordExpirationTimeout);
        boolean result = mDevicePolicyManager.resetPassword(password,
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();

        return result;
    }

    public boolean setPin(final String pin) {
        Log.d(TAG, "setPin() called");
        getDevicePolicyManager();
        mDevicePolicyManager.setPasswordQuality(mCompName, DevicePolicyManager.PASSWORD_QUALITY_NUMERIC);
        mDevicePolicyManager.setPasswordMinimumLength(mCompName, 4);
        mDevicePolicyManager.setPasswordExpirationTimeout(mCompName, mPasswordExpirationTimeout);
        boolean result = mDevicePolicyManager.resetPassword(pin,
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password changed successfully" : "Password change failed.";
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        return result;
    }

    public boolean resetPassword() {
        Log.d(TAG, "resetPassword() called");
        getDevicePolicyManager().setPasswordQuality(mCompName, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
        mDevicePolicyManager.setPasswordMinimumLength(mCompName, 0); // TODO: restore default system values?
        boolean result = getDevicePolicyManager().resetPassword("",
                DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
        String msg = result ? "Password removed successfully" : "Password remove failed.";
        Log.d(TAG, "resetPassword: " + msg);
        Toast.makeText(mContext, msg, Toast.LENGTH_LONG).show();
        return result;
    }
}
