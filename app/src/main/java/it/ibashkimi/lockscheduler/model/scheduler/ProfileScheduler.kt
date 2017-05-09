package it.ibashkimi.lockscheduler.model.scheduler

import android.content.Intent
import android.util.Log
import it.ibashkimi.lockscheduler.App
import it.ibashkimi.lockscheduler.model.*
import it.ibashkimi.lockscheduler.model.api.GeofenceApiHelper
import it.ibashkimi.lockscheduler.model.source.ProfilesRepository
import it.ibashkimi.lockscheduler.service.TransitionsIntentService
import java.util.*

/**
 * @author Indrit Bashkimi (mailto: indrit.bashkimi@gmail.it)
 */
class ProfileScheduler private constructor(val geofenceApiHelper: GeofenceApiHelper, val profilesRepository: ProfilesRepository) : ConditionChangeListener {

    val priorities: IntArray = intArrayOf(Condition.Type.TIME, Condition.Type.PLACE, Condition.Type.WIFI)

    val placeHandler: PlaceConditionHandler by lazy { PlaceConditionHandler(geofenceApiHelper, profilesRepository, this) }

    val timeHandler: TimeConditionHandler by lazy { TimeConditionHandler(profilesRepository, this) }

    val wifiHandler: WifiConditionHandler by lazy { WifiConditionHandler(profilesRepository, this) }

    private object Holder {
        val INSTANCE = ProfileScheduler(App.getGeofenceApiHelper(), ProfilesRepository.getInstance())
    }

    fun init() {
        placeHandler.init()
        timeHandler.init()
        wifiHandler.init()
    }

    fun register(profile: Profile) {
        if (profile.isActive) {
            for (condition in getConditionInversePriorityOrdered(profile.conditions))
                register(profile, condition)
        } else {
            val condition = getHighestPriorityCondition(profile.conditions)
            register(profile, condition)
        }
    }

    private fun register(profile: Profile, condition: Condition) {
        Log.d(TAG, "register() called with condition=$condition, profile=${profile.name}")
        when (condition) {
            is TimeCondition -> {
                timeHandler.register(profile)
            }
            is PlaceCondition -> {
                placeHandler.register(profile)
            }
            is WifiCondition -> {
                wifiHandler.register(profile)
            }
            else -> {
                throw RuntimeException("Cannot register unknown condition: $condition.")
            }
        }
    }

    fun unregister(profile: Profile) {
        Log.d(TAG, "unregister called with profile=${profile.name}.")
        for (condition in profile.conditions) {
            unregister(profile, condition)
        }
    }

    private fun unregister(profile: Profile, condition: Condition) {
        Log.d(TAG, "unregister called with profile=${profile.name}, condition=$condition")
        when (condition) {
            is PlaceCondition -> {
                placeHandler.unregister(profile.id)
            }
            is TimeCondition -> {
                timeHandler.unregister(profile.id)
            }
            is WifiCondition -> {
                wifiHandler.unregister(profile.id)
            }
            else -> throw RuntimeException("Cannot unregister unknown condition $condition.")
        }
    }

    private fun getHighestPriorityCondition(conditions: List<Condition>): Condition {
        if (priorities.isEmpty()) {
            throw RuntimeException("Condition list cannot be empty.")
        }
        for (priority in priorities) {
            val condition = getCondition(conditions, priority)
            condition?.let {
                return condition
            }
        }
        throw RuntimeException("Cannot find the highest priority.")
    }

    private fun getCondition(conditions: List<Condition>, priority: Int): Condition? {
        for (condition in conditions)
            if (condition.type == priority)
                return condition
        return null
    }

    fun notifyConditionRegistered(profile: Profile, condition: Condition) {
        Log.d(TAG, "notifyConditionRegistered() called with condition: $condition")
        profile.save()
        if (condition.isTrue) {
            Log.d(TAG, "condition is true")
            val nextCondition = getNextCondition(profile.conditions, condition)
            nextCondition?.let {
                register(profile, nextCondition)
            }
            if (!profile.isActive && profile.areConditionsTrue()) {
                profile.isActive = true
                profile.save()
                notifyProfileStateChanged(profile)
            }
        } else {

        }
    }

    override fun notifyConditionChanged(profile: Profile, condition: Condition) {
        Log.d(TAG, "notifyConditionChanged called with condition=$condition, profile=${profile.name}.")
        profile.save()
        if (condition.isTrue) {
            Log.d(TAG, "condition is true")
            val nextCondition = getNextCondition(profile.conditions, condition)
            nextCondition?.let {
                register(profile, nextCondition)
            }
            if (!profile.isActive && profile.areConditionsTrue()) {
                profile.isActive = true
                profile.save()
                notifyProfileStateChanged(profile)
            }
        } else {
            Log.d(TAG, "condition is false")
            val nextConditions = getNextConditions(profile.conditions, condition)
            if (nextConditions.isNotEmpty()) {
                for (nextCondition in nextConditions)
                    unregister(profile, nextCondition)
            }
            if (profile.isActive) {
                profile.isActive = false
                profile.save()
                notifyProfileStateChanged(profile)
            }
        }
    }

    private fun notifyProfileStateChanged(profile: Profile) {
        ActionManager.instance.performActions(if (profile.isActive) profile.enterActions else profile.exitActions)
        val intent = Intent(App.getInstance(), TransitionsIntentService::class.java)
        intent.action = "profile_state_changed"
        intent.putExtra("profile_id", profile.id)
        intent.putExtra("profile_name", profile.name)
        intent.putExtra("profile_active", profile.isActive)
        App.getInstance().startService(intent)
    }

    private fun getNextCondition(conditions: List<Condition>, condition: Condition): Condition? {
        for (priorityIndex in priorities.indices) {
            if (priorities[priorityIndex] == condition.type) {
                for (index in priorityIndex + 1..priorities.size - 1) {
                    val resCondition = getCondition(conditions, priorities[index])
                    resCondition?.let {
                        return resCondition
                    }
                }
                break
            }
        }
        return null
    }

    private fun getNextConditions(conditions: List<Condition>, condition: Condition): List<Condition> {
        val resConditions: MutableList<Condition> = mutableListOf()
        for (priorityIndex in priorities.indices) {
            if (priorities[priorityIndex] == condition.type) {
                for (index in priorityIndex + 1..priorities.size - 1) {
                    val resCondition = getCondition(conditions, priorities[index])
                    resCondition?.let {
                        resConditions.add(resCondition)
                    }
                }
                break
            }
        }
        return resConditions
    }

    fun Profile.areConditionsTrue(): Boolean {
        for (condition in conditions)
            if (!condition.isTrue)
                return false
        return true
    }

    fun Profile.save() {
        profilesRepository.update(this)
    }

    private fun getConditionPriorityOrdered(conditions: List<Condition>): List<Condition> {
        val result: ArrayList<Condition> = ArrayList(conditions.size)
        result.add(getHighestPriorityCondition(conditions))
        for (condition in getNextConditions(conditions, result[0])) {
            result.add(condition)
        }
        return result
    }

    private fun getConditionInversePriorityOrdered(conditions: List<Condition>): List<Condition> {
        return getConditionPriorityOrdered(conditions).asReversed()
    }

    companion object {

        private val TAG = "ProfileScheduler"

        val instance: ProfileScheduler by lazy { Holder.INSTANCE }
    }
}