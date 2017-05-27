package com.ibashkimi.lockscheduler.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.ibashkimi.lockscheduler.model.ProfileManager;
import com.ibashkimi.lockscheduler.model.api.LockManager;
import com.ibashkimi.lockscheduler.model.prefs.AppPreferencesHelper;


public class BootCompletedReceiver extends BroadcastReceiver {

    private static final String TAG = "BootCompletedReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive");
        long delay = Long.parseLong(AppPreferencesHelper.INSTANCE.getBootDelay());
        if (delay < 0)
            throw new IllegalArgumentException("Delay cannot be negative. Delay = " + delay + ".");
        if (delay == 0)
            ProfileManager.Companion.getInstance().init();
        else {
            Toast.makeText(context, "Setting alarm. Delay = " + delay, Toast.LENGTH_SHORT).show();
            LockManager.resetPassword(context);
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            Intent alarmIntent = new Intent(context, AlarmReceiver.class);
            alarmIntent.putExtra("boot", "boot");
            PendingIntent pi = PendingIntent.getBroadcast(context, 1, alarmIntent, PendingIntent.FLAG_CANCEL_CURRENT);
            long now = System.currentTimeMillis();
            long nextAlarm = now + delay;
            am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi);
        }
    }
}
