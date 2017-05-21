package it.ibashkimi.lockscheduler.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import it.ibashkimi.lockscheduler.model.scheduler.ProfileScheduler;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        Toast.makeText(context, "AlarmReceiver!", Toast.LENGTH_LONG).show();
        if (intent.getExtras().containsKey("profileId")) {
            String profileId = intent.getStringExtra("profileId");
            ProfileScheduler.Companion.getInstance().getTimeHandler().onAlarm(profileId);
        } else if (intent.getExtras().containsKey("boot")) {
            ProfileScheduler.Companion.getInstance().init();
        } else {
            throw new IllegalArgumentException("Don't know what to do with this intent: " + intent + ".");
        }

        wl.release();
    }
}