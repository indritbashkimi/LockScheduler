package it.ibashkimi.lockscheduler.receiver;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import it.ibashkimi.lockscheduler.App;
import it.ibashkimi.lockscheduler.model.Condition;
import it.ibashkimi.lockscheduler.model.Profile;
import it.ibashkimi.lockscheduler.model.TimeCondition;


public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();

        // Put here YOUR code.
        Toast.makeText(context, "AlarmReceiver !!!!!!!!!!", Toast.LENGTH_LONG).show(); // For example
        Log.d(TAG, "onReceive: ALARM RECEIVED PORCA PUTTANA!!!");

        long nextAlarm = Long.MAX_VALUE;
        for (Profile profile : App.getProfileApiHelper().getProfiles()) {
            TimeCondition timeCondition = (TimeCondition) profile.getCondition(Condition.Type.TIME);
            if (timeCondition != null) {
                timeCondition.checkNow();
                profile.notifyConditionChanged(timeCondition);
                long nNextAlarm = timeCondition.getNextAlarm();
                if (nNextAlarm < nextAlarm)
                    nextAlarm = nNextAlarm;
            }
        }
        if (nextAlarm != Long.MAX_VALUE) {
            setAlarm(context, nextAlarm);
        }

        wl.release();
    }

    public static void setAlarm(Context context, long nextAlarm) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, intent, 0);
        am.set(AlarmManager.ELAPSED_REALTIME, nextAlarm, pi);
    }

    public static void cancelAlarm(Context context) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }
}