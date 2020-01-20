package com.ibashkimi.lockscheduler.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.ibashkimi.lockscheduler.App
import com.ibashkimi.lockscheduler.model.Profile
import com.ibashkimi.lockscheduler.model.condition.Condition
import com.ibashkimi.lockscheduler.model.condition.DaysOfWeek
import com.ibashkimi.lockscheduler.model.condition.Time
import com.ibashkimi.lockscheduler.model.condition.TimeCondition
import com.ibashkimi.lockscheduler.data.ProfilesDataSource
import com.ibashkimi.lockscheduler.receiver.AlarmReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.*

class TimeConditionScheduler(
    repository: ProfilesDataSource,
    private val listener: ConditionChangeListener
) : ConditionScheduler(Condition.Type.TIME, repository) {

    private val TAG = "TimeCondition"

    override suspend fun init() {
        for (profile in registeredProfiles)
            register(profile)
    }

    override suspend fun register(profile: Profile): Boolean {
        Log.d(TAG, "register() called with profile=$profile")
        super.register(profile)
            val condition = profile.conditions.timeCondition!!
            val now = Calendar.getInstance().timeInMillis
            condition.isTriggered = shouldBeActive(now, condition)
            setAlarm(profile.id, condition.getNextAlarm(now))
           return condition.isTriggered
    }

    override suspend fun unregister(profileId: String) {
        Log.d(TAG, "unregister() called with profile=$profileId")
        super.unregister(profileId)
        cancelAlarm(profileId)
    }

    suspend fun onAlarm(profileId: String) {
        Log.d(TAG, "onAlarm called with profile=$profileId")
        val profile = getProfile(profileId)
        if (profile == null) {
            unregister(profileId)
        } else {
            val condition = profile.conditions.timeCondition!!
            doAlarmJob(profile, condition)
        }
    }

    private fun doAlarmJob(profile: Profile, condition: TimeCondition) {
        val wasActive = profile.isActive()
        val isTrue = condition.isTriggered
        val now = Calendar.getInstance().timeInMillis
        val shouldBeActive = shouldBeActive(now, condition)
        condition.isTriggered = shouldBeActive
        if (condition.isTriggered != isTrue)
            listener.notifyConditionChanged(profile, condition, wasActive)

        val nextAlarm = condition.getNextAlarm(now)
        setAlarm(profile.id, nextAlarm)
    }

    private fun shouldBeActive(currTime: Long, condition: TimeCondition): Boolean {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currTime
        var dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        when (dayOfWeek) {
            Calendar.MONDAY -> dayOfWeek = 0
            Calendar.TUESDAY -> dayOfWeek = 1
            Calendar.WEDNESDAY -> dayOfWeek = 2
            Calendar.THURSDAY -> dayOfWeek = 3
            Calendar.FRIDAY -> dayOfWeek = 4
            Calendar.SATURDAY -> dayOfWeek = 5
            Calendar.SUNDAY -> dayOfWeek = 6
        }
        if (!condition.daysActive[dayOfWeek]) {
            return false
        }
        val now = Time(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE))
        val start = condition.startTime
        val end = condition.endTime
        Log.d(TAG, "now=$now, start=$start, end=$end")
        if (start.isMidnight && end.isMidnight) {
            return true
        }
        if (start.isMidnight) {
            return now.isLowerThan(end)
        }
        if (end.isMidnight) {
            return now.isHigherThan(start)
        }
        return now.isHigherThan(start) && now.isLowerThan(end)
    }

    private fun TimeCondition.getNextAlarm(currTimeMillis: Long): Long {
        checkDaysValidityForDebug(daysActive)

        val cal = Calendar.getInstance()
        cal.timeInMillis = currTimeMillis
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val alarmTime: Time
        if (isTriggered) {
            alarmTime = endTime
            if (alarmTime.isMidnight)
                cal.add(Calendar.DAY_OF_MONTH, 1)
        } else {
            alarmTime = startTime
            val now = Time.fromTimeStamp(currTimeMillis)
            if (startTime.isLowerThan(now)) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, alarmTime.hour)
        cal.set(Calendar.MINUTE, alarmTime.minute)
        return cal.timeInMillis
    }

    private fun checkDaysValidityForDebug(daysActive: DaysOfWeek) {
        if ((0..6).none { daysActive[it] })
            throw RuntimeException("All days are false.")
    }

    private fun setAlarm(profileId: String, nextAlarm: Long) {
        Log.d(TAG, "set alarm called with profileId=$profileId, next alarm = $nextAlarm")
        printTimestamp("nextAlarm", nextAlarm)
        val context = App.getInstance()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmId = profileId.toLong().toInt()
        intent.putExtra("profileId", profileId)
        val pi =
            PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi)
    }

    private fun cancelAlarm(profileId: String) {
        Log.d(TAG, "cancelAlarm. profileId=$profileId")
        val context = App.getInstance()
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmId = profileId.toLong().toInt()
        val sender = PendingIntent.getBroadcast(context, alarmId, intent, 0)
        val alarmManager = App.getInstance().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    private fun printCalendar(tag: String, calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
        val date = Date(calendar.timeInMillis)
        Log.d(TAG, "$tag: ${dateFormat.format(date)}")
    }

    private fun printTimestamp(tag: String, timestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        printCalendar(tag, calendar)
    }
}