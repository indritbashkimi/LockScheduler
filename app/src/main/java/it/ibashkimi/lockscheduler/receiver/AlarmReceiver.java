package it.ibashkimi.lockscheduler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import it.ibashkimi.lockscheduler.model.scheduler.ProfileScheduler;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.
        Toast.makeText(context, "AlarmReceiver!", Toast.LENGTH_LONG).show();

        String profileId = intent.getStringExtra("profileId");
        ProfileScheduler.Companion.getInstance().getTimeHandler().onAlarm(profileId);

        wl.release();
    }
}