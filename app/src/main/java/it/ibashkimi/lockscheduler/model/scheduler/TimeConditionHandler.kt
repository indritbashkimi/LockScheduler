package it.ibashkimi.lockscheduler.model.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import it.ibashkimi.lockscheduler.App
import it.ibashkimi.lockscheduler.model.Condition
import it.ibashkimi.lockscheduler.model.Profile
import it.ibashkimi.lockscheduler.model.TimeCondition
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository
import it.ibashkimi.lockscheduler.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar.HOUR_OF_DAY
import java.util.Calendar.MINUTE

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@studio.unibo.it)
 */
class TimeConditionHandler(repository: ProfilesRepository, listener: ConditionChangeListener) : ConditionHandler(repository, listener) {

    private val TAG = "TimeConditionHandler"

    override val sharedPreferences = App.getInstance().getSharedPreferences("time_condition_handler", Context.MODE_PRIVATE)!!

    override fun init() {
        Log.d(TAG, "init() called.")
        for (profile in getProfiles(getRegisteredProfiles()))
            register(profile)
    }

    override fun register(profile: Profile) {
        Log.d(TAG, "register() called with profile: $profile.")
        val registeredProfileIds = getRegisteredProfiles()
        if (registeredProfileIds.add(profile.id)) {
            setRegisteredProfiles(registeredProfileIds)
            val condition = profile.getCondition(Condition.Type.TIME) as TimeCondition
            doAlarmJob(profile, condition)
        }
    }

    override fun unregister(profileId: String) {
        Log.d(TAG, "unregister() called with profileId = $profileId.")
        removeProfileId(profileId)
        cancelAlarm(profileId)
    }

    fun onAlarm(profileId: String) {
        Log.d(TAG, "onAlarm called with profileId=$profileId")
        val profile = repository.get(profileId) as Profile
        val condition = profile.getCondition(Condition.Type.TIME) as TimeCondition
        doAlarmJob(profile, condition)
    }

    fun doAlarmJob(profile: Profile, condition: TimeCondition) {
        Log.d(TAG, "doAlarmJob() called with profile=$profile")
        val isTrue = condition.isTrue
        val now = Calendar.getInstance().timeInMillis
        val shouldBeActive = shouldBeActive(now, condition)
        Log.d(TAG, "shouldBeActive = $shouldBeActive")
        condition.isTrue = shouldBeActive
        if (condition.isTrue != isTrue)
            listener.notifyConditionChanged(profile, condition)

        val nextAlarm = condition.getNextAlarm(now)
        setAlarm(profile.id, nextAlarm)
    }

    fun shouldBeActive(currTime: Long, condition: TimeCondition): Boolean {
        Log.d(TAG, "shouldBeActive() called with currTime=$currTime condition=$condition")
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
        val now = TimeCondition.Time(calendar.get(HOUR_OF_DAY), calendar.get(MINUTE))
        val start = condition.startTime
        val end = condition.endTime
        Log.d(TAG, "now=$now, start=$start, end=$end")
        if (start.isMidnight && end.isMidnight) {
            return true
        }
        if (start.isMidnight) {
            val res = now.compareTo(end).isLower
            return res
        }
        if (end.isMidnight) {
            val res = now.compareTo(start).isNotLower
            return res
        }
        return start.compareTo(now).isNotHigher && now.compareTo(end).isLower
    }

    fun TimeCondition.getNextAlarm(currTimeMillis: Long): Long {
        checkDaysValidityForDebug(daysActive)

        val cal = Calendar.getInstance()
        cal.timeInMillis = currTimeMillis
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val alarmTime: TimeCondition.Time
        if (isTrue) {
            alarmTime = endTime
            if (alarmTime.isMidnight)
                cal.add(Calendar.DAY_OF_MONTH, 1)
        } else {
            alarmTime = startTime
            val now = TimeCondition.Time.fromTimeStamp(currTimeMillis)
            if (startTime.compareTo(now).isLower) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        cal.set(Calendar.HOUR_OF_DAY, alarmTime.hour)
        cal.set(Calendar.MINUTE, alarmTime.minute)
        return cal.timeInMillis
    }

    private fun checkDaysValidityForDebug(daysActive: BooleanArray) {
        if ((0..6).none { daysActive[it] })
            throw RuntimeException("All days are false.")
    }

    private fun nextDay(day: Int): Int {
        return when (day) {
            Calendar.MONDAY -> Calendar.TUESDAY
            Calendar.TUESDAY -> Calendar.WEDNESDAY
            Calendar.WEDNESDAY -> Calendar.THURSDAY
            Calendar.THURSDAY -> Calendar.FRIDAY
            Calendar.FRIDAY -> Calendar.SATURDAY
            Calendar.SATURDAY -> Calendar.SUNDAY
            Calendar.SUNDAY -> Calendar.MONDAY
            else -> throw RuntimeException("Invalid day number $day.")
        }
    }

    private fun previousDay(day: Int): Int {
        return when (day) {
            Calendar.MONDAY -> Calendar.SUNDAY
            Calendar.TUESDAY -> Calendar.MONDAY
            Calendar.WEDNESDAY -> Calendar.TUESDAY
            Calendar.THURSDAY -> Calendar.WEDNESDAY
            Calendar.FRIDAY -> Calendar.THURSDAY
            Calendar.SATURDAY -> Calendar.FRIDAY
            Calendar.SUNDAY -> Calendar.SATURDAY
            else -> throw RuntimeException("Invalid day number $day.")
        }
    }

    fun setAlarm(profileId: String, nextAlarm: Long) {
        Log.d(TAG, "set alarm called with profileId=$profileId, next alarm = $nextAlarm")
        printTimestamp("nextAlarm", nextAlarm)
        val context = App.getInstance()
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmId = profileId.toLong().toInt()
        intent.putExtra("profileId", profileId)
        val pi = PendingIntent.getBroadcast(context, alarmId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        am.set(AlarmManager.RTC_WAKEUP, nextAlarm, pi)
    }

    fun cancelAlarm(profileId: String) {
        Log.d(TAG, "cancelAlarm. profileId=$profileId")
        val context = App.getInstance()
        val intent = Intent(context, AlarmReceiver::class.java)
        val alarmId = profileId.toLong().toInt()
        val sender = PendingIntent.getBroadcast(context, alarmId, intent, 0)
        val alarmManager = App.getInstance().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(sender)
    }

    fun printCalendar(tag: String, calendar: Calendar) {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
        val date: Date = Date(calendar.timeInMillis)
        Log.d(TAG, "$tag: ${dateFormat.format(date)}")
    }

    fun printTimestamp(tag: String, timestamp: Long) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        printCalendar(tag, calendar)
    }

    fun timestampToString(timestamp: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendarToString(calendar)
    }

    fun calendarToString(calendar: Calendar): String {
        val dateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH)
        val date: Date = Date(calendar.timeInMillis)
        return dateFormat.format(date)
    }
}